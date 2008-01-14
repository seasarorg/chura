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
package org.seasar.dolteng.eclipse.util;

import java.io.BufferedInputStream;
import java.io.IOException;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.XPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * @author taichi
 * 
 */
public class FuzzyXMLUtil {

    public static String getChildText(FuzzyXMLElement e) {
        return e.getValue().replaceAll("\"|\r|\n", "");
    }

    public static FuzzyXMLNode[] selectNodes(IFile file, String query)
            throws IOException, CoreException {
        FuzzyXMLDocument doc = parse(file);
        FuzzyXMLNode[] list = XPath
                .selectNodes(doc.getDocumentElement(), query);
        return list;
    }

    public static FuzzyXMLDocument parse(IFile file) throws IOException,
            CoreException {
        FuzzyXMLParser parser = new FuzzyXMLParser();
        FuzzyXMLDocument doc = parser.parse(new BufferedInputStream(file
                .getContents(true)));
        return doc;
    }

    public static String getAttribute(FuzzyXMLElement e, String name) {
        String result = "";
        FuzzyXMLAttribute a = e.getAttributeNode(name);
        if (a != null) {
            result = a.getValue();
        }
        return result;
    }

    public static FuzzyXMLElement getFirstChild(FuzzyXMLElement element) {
        for (FuzzyXMLNode kid : element.getChildren()) {
            if (kid instanceof FuzzyXMLElement) {
                return (FuzzyXMLElement) kid;
            }
        }
        return null;
    }

}
