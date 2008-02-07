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

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;

import org.apache.commons.collections.map.ListOrderedMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.dolteng.core.entity.FieldMetaData;
import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
import org.seasar.dolteng.core.entity.impl.BasicMethodMetaData;
import org.seasar.dolteng.core.teeda.TeedaEmulator;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
@SuppressWarnings("unchecked")
public class HtmlNodeAnalyzer {

    private IFile htmlfile;

    private Set<BasicMethodMetaData> actionMethods = new HashSet<BasicMethodMetaData>();

    private Set<BasicMethodMetaData> conditionMethods = new HashSet<BasicMethodMetaData>();

    private Map<String, FieldMetaData> pageFields = new ListOrderedMap();

    public HtmlNodeAnalyzer(IFile htmlfile) {
        super();
        this.htmlfile = htmlfile;
    }

    public void analyze() {
        try {
            FuzzyXMLNode[] nodes = FuzzyXMLUtil.selectNodes(this.htmlfile,
                    "//@id");
            for (FuzzyXMLNode node : nodes) {
                FuzzyXMLAttribute attr = (FuzzyXMLAttribute) node;
                FuzzyXMLElement e = (FuzzyXMLElement) attr.getParentNode();
                String id = attr.getValue();
                if (StringUtil.isEmpty(id)) {
                    continue;
                }
                analyze(e, id);
                if (TeedaEmulator.isSelect(e)) {
                    analyze(e, id + "Items");
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    private void analyze(FuzzyXMLElement e, String id) {
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
                if (TeedaEmulator.needIndex(e, id)) {
                    String s = TeedaEmulator.calcMultiItemIndexId(id);
                    BasicFieldMetaData indexField = new BasicFieldMetaData();
                    indexField.setModifiers(Modifier.PUBLIC);
                    indexField.setDeclaringClassName("int");
                    indexField.setName(s);
                    this.pageFields.put(s, indexField);
                }
            } else {
                meta.setDeclaringClassName("java.lang.String");
            }
            meta.setName(id);
            this.pageFields.put(id, meta);
        }
    }

    private String getDefineClassName(String id) {
        String result = "java.util.List";
        try {
            DoltengPreferences pref = DoltengCore.getPreferences(this.htmlfile
                    .getProject());
            if (pref != null) {
                if (Constants.DAO_TYPE_S2DAO.equals(pref.getDaoType())) {
                    IJavaProject project = JavaCore.create(this.htmlfile
                            .getProject());
                    String typeName = StringUtil.capitalize(id.replaceAll(
                            "Items", ""));
                    NamingConvention nc = pref.getNamingConvention();
                    String[] pkgs = nc.getRootPackageNames();
                    for (int i = 0; i < pkgs.length; i++) {
                        String fqn = pkgs[i] + "." + nc.getEntityPackageName()
                                + "." + typeName;
                        IType type = project.findType(fqn);
                        if (type != null && type.exists()) {
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

    public Set<BasicMethodMetaData> getActionMethods() {
        return this.actionMethods;
    }

    public Set<BasicMethodMetaData> getConditionMethods() {
        return this.conditionMethods;
    }

    public Map<String, FieldMetaData> getPageFields() {
        return this.pageFields;
    }
}
