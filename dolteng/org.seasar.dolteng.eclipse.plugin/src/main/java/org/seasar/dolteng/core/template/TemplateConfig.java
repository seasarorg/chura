/*
 * Copyright 2004-2008 the Seasar Foundation and the Others.
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.XPath;

import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ScriptingUtil;
import org.seasar.framework.util.BooleanConversionUtil;

/**
 * @author taichi
 * 
 */
public class TemplateConfig {

    private String templatePath;

    private boolean override = false;

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

    public String resolveOutputFile(Map<String, String> values) {
        return ScriptingUtil.resolveString(outputFile, values);
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

    public String resolveOutputPath(Map<String, String> values) {
        return ScriptingUtil.resolveString(outputPath, values);
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

    /**
     * @return Returns the override.
     */
    public boolean isOverride() {
        return override;
    }

    /**
     * @param override
     *            The override to set.
     */
    public void setOverride(boolean override) {
        this.override = override;
    }

    public static TemplateConfig[] loadConfigs(URL url) {
        List<TemplateConfig> result = new ArrayList<TemplateConfig>();
        try {
            FuzzyXMLParser parser = new FuzzyXMLParser();
            FuzzyXMLDocument doc = parser.parse(new BufferedInputStream(url
                    .openStream()));
            FuzzyXMLNode[] list = XPath.selectNodes(doc.getDocumentElement(),
                    "//template");

            for (FuzzyXMLNode node : list) {
                FuzzyXMLElement element = (FuzzyXMLElement) node;
                TemplateConfig tc = new TemplateConfig();
                tc.setTemplatePath(element.getAttributeNode("path").getValue());
                for (FuzzyXMLNode childNode : element.getChildren()) {
                    if (childNode instanceof FuzzyXMLElement) {
                        element = (FuzzyXMLElement) childNode;
                        tc.setOverride(BooleanConversionUtil
                                .toPrimitiveBoolean(element.getAttributeNode(
                                        "override").getValue()));
                        tc.setOutputPath(element.getAttributeNode("path").getValue());
                        tc.setOutputFile(element.getAttributeNode("name").getValue());
                        break;
                    }
                }
                result.add(tc);
            }
        } catch (IOException e) {
            DoltengCore.log(e);
        }

        return result.toArray(new TemplateConfig[result.size()]);
    }
}
