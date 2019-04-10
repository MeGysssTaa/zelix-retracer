/*
 * Copyright 2019 DarksideCode
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.darksidecode.zelixretracer;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

@SuppressWarnings ("Duplicates")
class ZelixMappings {

    private static final String TRANSFORMED_TO   =              "\t=>\t";
    private static final String NAME_NOT_CHANGED =      "NameNotChanged";
    private static final String SIGN_NOT_CHANGED = "SignatureNotChanged";

    private static final String PACKAGE_IDENTIFIER =       "Package: ";
    private static final String CLASS_IDENTIFIER   =         "Class: ";
    private static final String FIELDS_IDENTIFIER  =      "FieldsOf: ";
    private static final String METHODS_IDENTIFIER =     "MethodsOf: ";
    private static final String LN_NUMS_IDENTIFIER = "LineNumbersOf: ";

    static final String OPENING_PARENTHESES_ESC = Pattern.quote("(");

    private final Map<String, String> packages = new HashMap<>();
    private final Map<String, ClassData> classes = new HashMap<>();

    private ClassData parsingMethodsOf, parsingFieldsOf, parsingLineNumbersOf;

    ZelixMappings(String s) {
        String[] lines = s.split("\n");

        for (String line : lines) {
            try {
                line = line.trim().
                        replace("\t" + NAME_NOT_CHANGED, TRANSFORMED_TO + NAME_NOT_CHANGED).
                        replace("\t" + SIGN_NOT_CHANGED, TRANSFORMED_TO + SIGN_NOT_CHANGED);

                if ((line.startsWith("//")) || (line.isEmpty())) {
                    resetStates();
                    continue;
                }

                if (line.startsWith(PACKAGE_IDENTIFIER))
                    parsePackage(line);
                else if (line.startsWith(CLASS_IDENTIFIER))
                    parseClass(line);
                else if (line.startsWith(FIELDS_IDENTIFIER))
                    parseFields(line);
                else if (line.startsWith(METHODS_IDENTIFIER))
                    parseMethods(line);
                else if (line.startsWith(LN_NUMS_IDENTIFIER))
                    parseLineNumbers(line);
                else if (parsingMethodsOf != null)
                    // We are in process of parsing methods list.
                    parseMethod(line);
                else if (parsingFieldsOf != null)
                    // We are in process of parsing fields list.
                    parseField(line);
                else if (parsingLineNumbersOf != null)
                    // We are in process of parsing line numbers list.
                    parseLineNumber(line);
            } catch (Exception ex) {
                System.out.println("Failed to parse the following line because of an unhandled error:");
                System.out.println(line);
                System.out.println("It will be ignored. Make sure the ZKM mappings file you specified is valid!");
                System.out.println("The error that occurred was: " + ex.toString());
                System.out.println();
            }
        }
    }

    String translatePackageName(String pkgName) {
        String orig = packages.get(pkgName);

        if (orig == null)
            throw new NoSuchElementException("unknown package: " + pkgName);

        return orig;
    }

    ClassData getClassData(String className) {
        ClassData data = classes.get(className);

        if (data == null)
            throw new NoSuchElementException("unknown class: " + className);

        return data;
    }

    ClassData obfGetClassData(String obfClassName) {
        for (ClassData data : classes.values())
            if (data.getObfName().equals(obfClassName))
                return data;

        throw new NoSuchElementException("[obf] unknown class: " + obfClassName);
    }

    private void parsePackage(String line) {
        resetStates();

        String[] s = line.replace(PACKAGE_IDENTIFIER, "").split(TRANSFORMED_TO);

        String origName = s[0];
        String obfName = s[1];

        if (obfName.equals(NAME_NOT_CHANGED))
            obfName = origName;

        packages.put(origName, obfName);
    }

    private void parseClass(String line) {
        resetStates();

        String[] s = line.replace(CLASS_IDENTIFIER, "").split(TRANSFORMED_TO);
        String[] splBySpaces = s[0].split(" ");

        String origName = splBySpaces[splBySpaces.length - 1];
        String obfName = s[1];

        if (classes.get(origName) != null)
            throw new IllegalStateException("duplicate class entry: " + origName);

        if (obfName.equals(NAME_NOT_CHANGED))
            obfName = origName;

        classes.put(origName, new ClassData(origName, obfName));
    }

    private void parseFields(String line) {
        resetStates();

        String ownerClass = line.replace(FIELDS_IDENTIFIER, "");
        ClassData data = classes.get(ownerClass);

        if (data == null)
            throw new IllegalStateException("class " + ownerClass + " has not been defined yet");

        parsingFieldsOf = data;
    }

    private void parseField(String line) {
        String[] s = line.split(TRANSFORMED_TO);
        String[] splBySpaces = s[0].split(" ");

        String origName = splBySpaces[splBySpaces.length - 1];
        String obfName = s[1];

        if (obfName.equals(NAME_NOT_CHANGED))
            obfName = origName;

        parsingFieldsOf.defineField(origName, obfName);
    }

    private void parseMethods(String line) {
        resetStates();

        String ownerClass = line.replace(METHODS_IDENTIFIER, "");
        ClassData data = classes.get(ownerClass);

        if (data == null)
            throw new IllegalStateException("class " + ownerClass + " has not been defined yet");

        parsingMethodsOf = data;
    }

    private void parseMethod(String line) {
        String[] splByParentheses = line.split(OPENING_PARENTHESES_ESC);
        String[] splBySpaces = splByParentheses[0].split(" ");

        String origName = splBySpaces[splBySpaces.length - 1];
        String transformedTo = line.split(TRANSFORMED_TO)[1];
        String obfName = transformedTo.split(OPENING_PARENTHESES_ESC)[0];

        if (obfName.equals(SIGN_NOT_CHANGED))
            obfName = origName;

        parsingMethodsOf.defineMethod(origName, obfName);
    }

    private void parseLineNumbers(String line) {
        resetStates();

        String ownerClass = line.replace(LN_NUMS_IDENTIFIER, "");
        ClassData data = classes.get(ownerClass);

        if (data == null)
            throw new IllegalStateException("class " + ownerClass + " has not been defined yet");

        parsingLineNumbersOf = data;
    }

    private void parseLineNumber(String line) {
        String[] s = line.split(TRANSFORMED_TO);

        String origNum = s[0];
        String obfNum = s[1];

        parsingLineNumbersOf.defineLineNumber(origNum, obfNum);
    }

    private void resetStates() {
        parsingMethodsOf = parsingFieldsOf = parsingLineNumbersOf = null;
    }

}
