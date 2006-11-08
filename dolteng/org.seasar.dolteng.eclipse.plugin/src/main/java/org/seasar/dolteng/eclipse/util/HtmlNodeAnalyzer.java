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

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
import org.seasar.dolteng.core.entity.impl.BasicMethodMetaData;
import org.seasar.dolteng.core.teeda.TeedaEmulator;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.framework.convention.NamingConvention;
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
        try {
            FuzzyXMLNode[] nodes = FuzzyXMLUtil.selectNodes(this.htmlfile,
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
                        meta.setDeclaringClassName(getDefineClassName(id));
                    } else {
                        meta.setDeclaringClassName("java.lang.String");
                    }
                    meta.setName(id);
                    this.pageFields.put(id, meta);
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    private String getDefineClassName(String id) {
        String result = "java.util.List";
        try {
            DoltengProjectPreferences pref = DoltengCore
                    .getPreferences(this.htmlfile.getProject());
            if (pref != null) {
                if (Constants.DAO_TYPE_S2DAO.equals(pref.getDaoType())) {
                    IJavaProject jp = JavaCore.create(this.htmlfile
                            .getProject());
                    String typeName = StringUtil.capitalize(id.replaceAll(
                            "Items", ""));
                    NamingConvention nc = pref.getNamingConvention();
                    String[] pkgs = nc.getRootPackageNames();
                    for (int i = 0; i < pkgs.length; i++) {
                        String fqn = pkgs[i] + "." + nc.getEntityPackageName()
                                + "." + typeName;
                        IType t = jp.findType(fqn);
                        if (t != null && t.exists()) {
                            result = fqn + "[]";
                        }
                    }
                }
                if (Constants.DAO_TYPE_UUJI.equals(pref.getDaoType())) {
                    result = "java.util.Map[]";
                }
            }
        } catch (JavaModelException e) {
            DoltengCore.log(e);
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
