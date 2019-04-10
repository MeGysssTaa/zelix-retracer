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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

public class ZelixRetracer {

    private static final String DOT_ESC                 = Pattern.quote(".");
    private static final String CLOSING_PARENTHESES_ESC = Pattern.quote(")");
    private static final String DOLLAR_SIGN_ESC         = Pattern.quote("$");

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Missing arguments. Usage: java -jar <...>.jar <path to file with ZKM mappings>");
            return;
        }

        File mappingsFile = new File(args[0].trim());

        if (!(mappingsFile.exists())) {
            System.out.println("The specified mappings file does not exist.");
            return;
        }

        System.out.println("Reading and parsing mappings from " + mappingsFile.getAbsolutePath() + "...");
        System.out.println();

        byte[] bytes = Files.readAllBytes(mappingsFile.toPath());
        String mappingsText = new String(bytes, StandardCharsets.UTF_8);
        ZelixMappings mappings = new ZelixMappings(mappingsText);

        System.out.println("Complete. Enter the stacktrace you want to deobfuscate,");
        System.out.println("then enter \"/retrace\" to begin the remapping process.");
        System.out.println();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder stacktraceBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.trim().equalsIgnoreCase("/retrace"))
                break;

            stacktraceBuilder.append(line).append('\n');
        }

        reader.close();

        System.out.println();
        System.out.println("Please wait...");
        System.out.println();

        String obfStacktraceStr = stacktraceBuilder.toString();
        String[] obfLines = obfStacktraceStr.
                replace("\r\n", "\n").
                replace("\r", "\n").
                split("\n");

        System.out.println("obf lines: " + obfLines.length);

        StringBuilder retraced = new StringBuilder();

        for (String ln : obfLines) {
            ln = "  " + ln;

            if (ln.contains(" at ")) {
                try {
                    String stacktraceElement = ln.split(" at ")[1];
                    String[] splByParentheses = stacktraceElement.
                            split(ZelixMappings.OPENING_PARENTHESES_ESC);

                    String classMethod = splByParentheses[0];
                    String[] splByDot = classMethod.split(DOT_ESC);

                    String methodName = splByDot[splByDot.length - 1];
                    String className = classMethod.replace("." + methodName, "");
                    ClassData classData = mappings.obfGetClassData(className);

                    String origClassName = classData.getOrigName();
                    String origMethodName = classData.obfTranslateMethodName(methodName);
                    String lineNum = splByParentheses[1].
                            split(":")[1].split(CLOSING_PARENTHESES_ESC)[0];
                    String origLineNum;

                    try {
                        origLineNum = classData.obfTranslateLineNumber(lineNum);
                    } catch (NoSuchElementException ex) {
                        // Invalid line number (bad stacktrace?) or unknown source / native code
                        origLineNum = lineNum;
                    }

                    String[] classNameSpl = className.split(DOT_ESC);
                    String sourceFileName = classNameSpl[classNameSpl.length - 1] + ".java";
                    String[] origClassNameSpl = origClassName.split(DOT_ESC);
                    String origSourceFileName = origClassNameSpl[origClassNameSpl.length - 1].
                            split(DOLLAR_SIGN_ESC)[0] + ".java";

                    // Apply deobfuscations/remappings to this line
                    ln = ln.replace(className, origClassName).
                            replace(methodName, origMethodName).
                            replace(lineNum, origLineNum).
                            replace(sourceFileName, origSourceFileName);

                    retraced.append(ln).append('\n');
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("Failed to retrace the following line:");
                    System.out.println(ln);
                    System.out.println("The error that occurred was: " + ex.toString());
                    System.out.println();

                    retraced.append(ln).append('\n'); // append original line
                }
            } else
                retraced.append(ln).append('\n');
        }

        System.out.println();
        System.out.println("-------------------------------------------------------------");
        System.out.println("  Retraced:");
        System.out.println("-------------------------------------------------------------");
        System.out.println();
        System.out.println(retraced.toString());
        System.out.println();
    }

}
