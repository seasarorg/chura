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
package org.seasar.dolteng.eclipse.model.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.RootModel;
import org.seasar.dolteng.eclipse.util.TypeUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class AsModel implements RootModel {

    private NamingConvention convention;

    private ICompilationUnit unit;

    private IType type;

    private Map<String, String> configs;

    public AsModel(ICompilationUnit unit) {
        this.unit = unit;
        configs = new HashMap<String, String>();
    }

    public AsModel(Map<String, String> configs) {
        this.configs = configs;
    }

    public void initialize() {
        if (unit != null) {
            type = unit.findPrimaryType();
        }
    }

    public IType getType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.RootModel#getConfigs()
     */
    public Map<String, String> getConfigs() {
        return configs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.RootModel#getNamingConvention()
     */
    public NamingConvention getNamingConvention() {
        return convention;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.RootModel#setNamingConvention(org.seasar.framework.convention.NamingConvention)
     */
    public void setNamingConvention(NamingConvention namingConvention) {
        convention = namingConvention;
    }

    public String toAsType(IField field) {
        String result = "Object";
        try {
            String s = TypeUtil.getResolvedTypeName(field.getTypeSignature(),
                    type);
            s = DoltengCore.getAsTypeResolver(unit.getJavaProject()).resolve(s);
            if (StringUtil.isEmpty(s) == false) {
                result = s;
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return result;
    }

    public boolean isOutputField(IField field) {
        try {
            int flags = field.getFlags();
            return Flags.isStatic(flags) == false;
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return false;
    }
}
