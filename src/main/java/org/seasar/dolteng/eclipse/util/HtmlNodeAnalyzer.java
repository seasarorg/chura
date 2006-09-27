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
package org.seasar.dolteng.eclipse.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.XPath;

import org.eclipse.core.resources.IFile;
import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
import org.seasar.dolteng.core.entity.impl.BasicMethodMetaData;
import org.seasar.dolteng.core.teeda.TeedaEmulator;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.impl.PageClassColumn;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.teeda.extension.ExtensionConstants;

/**
 * @author taichi
 * 
 */
public class HtmlNodeAnalyzer {

    private IFile htmlfile;

    private List actionMethods = new ArrayList();

    private Map pageFields = new HashMap();

    public HtmlNodeAnalyzer(IFile htmlfile) {
        super();
        this.htmlfile = htmlfile;
    }

    public void analyze() {
        InputStream in = null;
        try {
            in = htmlfile.getContents();
            FuzzyXMLParser parser = new FuzzyXMLParser();
            FuzzyXMLDocument doc = parser.parse(new BufferedInputStream(in));
            FuzzyXMLNode[] nodes = XPath.selectNodes(doc.getDocumentElement(),
                    "//html//@id");
            for (int i = 0; i < nodes.length; i++) {
                FuzzyXMLAttribute attr = (FuzzyXMLAttribute) nodes[i];
                String id = attr.getValue();
                if (StringUtil.isEmpty(id) == false) {
                    FuzzyXMLElement e = (FuzzyXMLElement) attr.getParentNode();
                    FuzzyXMLAttribute a = e.getAttributeNode("class");
                    if (a != null) {
                        id = a.getValue();
                    }
                }
                if (StringUtil.isEmpty(id)) {
                    continue;
                }
                if (0 == id.indexOf(ExtensionConstants.DO_PREFIX)) {
                    BasicMethodMetaData meta = new BasicMethodMetaData();
                    meta.setModifiers(Modifier.PUBLIC);
                    meta.setName(id);
                    this.actionMethods.add(meta);
                } else if (TeedaEmulator.MAPPING_SKIP_ID.matcher(id).matches() == false) {
                    BasicFieldMetaData meta = new BasicFieldMetaData();
                    meta.setModifiers(Modifier.PUBLIC);
                    if (PageClassColumn.multiItemRegx.matcher(id).matches()) {
                        meta.setDeclaringClassName("java.util.List");
                    } else {
                        meta.setDeclaringClassName("java.lang.String");
                    }
                    meta.setName(id);
                    this.pageFields.put(id, meta);
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            InputStreamUtil.close(in);
        }
    }

    public List getActionMethods() {
        return this.actionMethods;
    }

    public Map getPageFields() {
        return this.pageFields;
    }
}
