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

import java.util.*;

class ClassData {

    private final String origName, obfName;

    private final Map<String, String> fields, methods;
    private final Map<String, String> lineNumbers; // string because in ZKM line nums may be like "3, 7 and 9"

    ClassData(String origName, String obfName) {
        this.origName = origName;
        this.obfName = obfName;

        fields = new HashMap<>();
        methods = new HashMap<>();
        lineNumbers = new HashMap<>();
    }

    String translateFieldName(String fieldName) {
        return translateName("field", fieldName, fields);
    }

    String translateMethodName(String methodName) {
        return translateName("method", methodName, methods);
    }

    String obfTranslateMethodName(String obfMethodName) {
        return methods.keySet().stream().filter(origName -> methods.get(origName).equals(obfMethodName)).
                findFirst().orElseThrow(() -> new NoSuchElementException("[obf] unknown method: " + obfMethodName));
    }

    String translateLineNumber(String lineNumber) {
        String obf = lineNumbers.get(lineNumber);

        if (obf == null)
            throw new NoSuchElementException("unknown line number: " + lineNumber);

        return obf;
    }

    String obfTranslateLineNumber(String obfLineNumber) {
        return lineNumbers.keySet().stream().filter(origNum -> lineNumbers.get(origNum).equals(obfLineNumber)).
                findFirst().orElseThrow(() -> new NoSuchElementException("[obf] unknown line number: " + obfLineNumber));
    }

    private String translateName(String what, String name, Map<String, String> mappings) {
        String obf = mappings.get(name);

        if (obf == null)
            throw new NoSuchElementException("unknown " + what + ": " + name);

        return obf;
    }

    void defineField(String origName, String obfName) {
        fields.put(origName, obfName);
    }

    void defineMethod(String origName, String obfName) {
        methods.put(origName, obfName);
    }

    void defineLineNumber(String origNum, String obfNum) {
        lineNumbers.put(origNum, obfNum);
    }

    String getOrigName() {
        return origName;
    }

    String getObfName() {
        return obfName;
    }

}
