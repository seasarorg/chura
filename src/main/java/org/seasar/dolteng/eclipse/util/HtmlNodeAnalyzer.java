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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class HtmlNodeAnalyzer {

    private IFile htmlfile;

    private Set actionMethods = new HashSet();

    private Set conditionMethods = new HashSet();

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
                FuzzyXMLElement e = (FuzzyXMLElement) attr.getParentNode();
                String id = attr.getValue();
                if (StringUtil.isEmpty(id) == false) {
                    FuzzyXMLAttribute a = e.getAttributeNode("class");
                    if (a != null) {
                        id = a.getValue();
                    }
                }
                if (StringUtil.isEmpty(id)) {
                    continue;
                }
                id = TeedaEmulator.calcMappingId(e, id);
                if (TeedaEmulator.isCommandId(e, id)) {
                    BasicMethodMetaData meta = new BasicMethodMetaData();
                    meta.setModifiers(Modifier.PUBLIC);
                    meta.setName(id);
                    this.actionMethods.add(meta);
                } else if (TeedaEmulator.isConditionId(e, id)) {
                    BasicMethodMetaData meta = new BasicMethodMetaData();
                    meta.setModifiers(Modifier.PUBLIC);
                    meta.setName(id);
                    this.conditionMethods.add(meta);
                } else if (TeedaEmulator.isNotSkipId(e, id)) {
                    BasicFieldMetaData meta = new BasicFieldMetaData();
                    meta.setModifiers(Modifier.PUBLIC);
                    if (TeedaEmulator.MAPPING_MULTI_ITEM.matcher(id).matches()) {
                        meta.setDeclaringClassName(getDefineClassName());
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

    private String getDefineClassName() {
        String result = "java.util.List";
        DoltengProjectPreferences pref = DoltengCore
                .getPreferences(this.htmlfile.getProject());
        if (pref != null && Constants.DAO_TYPE_UUJI.equals(pref.getDaoType())) {
            result = "java.util.Map[]";
        }
        return result;
    }

    public Set getActionMethods() {
        return this.actionMethods;
    }

    public Set getConditionMethods() {
        return this.conditionMethods;
    }

    public Map getPageFields() {
        return this.pageFields;
    }
}
