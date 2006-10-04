/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.dolteng.core.template;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author taichi
 * 
 */
public class TemplateConfig {

    private String templatePath;

    private String outputPath;

    private String outputFile;

    public TemplateConfig() {
        super();
    }

    /**
     * @return Returns the outputFile.
     */
    public String getOutputFile() {
        return outputFile;
    }

    public String resolveOutputFile(Map values) {
        return resolveString(outputFile, values);
    }

    private static String resolveString(String string, Map values) {
        Pattern p = Pattern.compile("\\$\\{[^\\$\\{\\}]*\\}");
        StringBuffer stb = new StringBuffer(string);
        Matcher m = p.matcher(stb);
        while (m.find()) {
            String s = m.group();
            stb.replace(m.start(), m.end(), toString(values.get(s.substring(2,
                    s.length() - 1))));
            m = p.matcher(stb);
        }
        return stb.toString();
    }

    private static String toString(Object o) {
        return o == null ? "" : o.toString();
    }

    /**
     * @param outputFile
     *            The outputFile to set.
     */
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * @return Returns the outputPath.
     */
    public String getOutputPath() {
        return outputPath;
    }

    public String resolveOutputPath(Map values) {
        return resolveString(outputPath, values);
    }

    /**
     * @param outputPath
     *            The outputPath to set.
     */
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    /**
     * @return Returns the templatePath.
     */
    public String getTemplatePath() {
        return templatePath;
    }

    /**
     * @param templatePath
     *            The templatePath to set.
     */
    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

}
