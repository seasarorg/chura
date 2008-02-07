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
package org.seasar.dolteng.eclipse.convention;

import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.beans.PropertyDesc;
import org.seasar.framework.beans.factory.BeanDescFactory;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.convention.impl.NamingConventionImpl;
import org.seasar.framework.util.CaseInsensitiveMap;
import org.seasar.framework.util.ClassUtil;

/**
 * @author taichi
 * 
 */
@SuppressWarnings("unchecked")
public class NamingConventionMirror extends NamingConventionImpl implements
        NamingConvention {

    private IJavaProject project;

    public static Map<String, Object> DEFAULT_VALUES = new CaseInsensitiveMap();
    static {
        NamingConventionImpl impl = new NamingConventionImpl();
        parse(NamingConvention.class, impl, DEFAULT_VALUES);
    }

    private Map<String, Object> mirror;

    public NamingConventionMirror(IJavaProject project, Class clazz,
            Object original) {
        super();
        this.project = project;
        this.mirror = new CaseInsensitiveMap();
        this.mirror.putAll(DEFAULT_VALUES);
        parse(clazz, original, this.mirror);
        String[] ary = (String[]) this.mirror.remove("RootPackageNames");
        for (int i = 0; ary != null && i < ary.length; i++) {
            addRootPackageName(ary[i]);
        }
    }

    public NamingConventionMirror(IJavaProject project, Map<String, Object> content) {
        this.project = project;
        this.mirror = new CaseInsensitiveMap();
        this.mirror.putAll(DEFAULT_VALUES);
        this.mirror.putAll(content);
    }

    private static void parse(Class clazz, Object original, Map<String, Object> store) {
        try {
            BeanDesc desc = BeanDescFactory.getBeanDesc(clazz);
            for (int i = 0; i < desc.getPropertyDescSize(); i++) {
                PropertyDesc pd = desc.getPropertyDesc(i);
                if (pd.hasReadMethod()) {
                    store.put(pd.getPropertyName(), pd.getValue(original));
                }
            }
        } finally {
            BeanDescFactory.clear();
        }
    }

    @SuppressWarnings("unchecked")
    public static Map toMap(NamingConvention nc) {
        NamingConventionMirror ncm = null;
        if (nc instanceof NamingConventionMirror) {
            ncm = (NamingConventionMirror) nc;
            return ncm.mirror;
        }
        Map<String, Object> store = new CaseInsensitiveMap();
        store.putAll(DEFAULT_VALUES);
        parse(nc.getClass(), nc, store);
        return store;
    }

    @Override
    protected void addExistChecker(final String rootPackageName) {
        // Noting to do.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.impl.NamingConventionImpl#isExist(java.lang.String,
     *      java.lang.String)
     */
    @Override
    protected boolean isExist(String rootPackageName, String lastClassName) {
        String fqn = ClassUtil.concatName(rootPackageName, lastClassName);
        try {
            IType t = project.findType(fqn);
            return t != null && t.exists();
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getViewRootPath()
     */
    @Override
    public String getViewRootPath() {
        return toString(mirror.get("ViewRootPath"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getViewExtension()
     */
    @Override
    public String getViewExtension() {
        return toString(mirror.get("ViewExtension"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getImplementationSuffix()
     */
    @Override
    public String getImplementationSuffix() {
        return toString(mirror.get("ImplementationSuffix"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getPageSuffix()
     */
    @Override
    public String getPageSuffix() {
        return toString(mirror.get("PageSuffix"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getActionSuffix()
     */
    @Override
    public String getActionSuffix() {
        return toString(mirror.get("ActionSuffix"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getServiceSuffix()
     */
    @Override
    public String getServiceSuffix() {
        return toString(mirror.get("ServiceSuffix"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getDxoSuffix()
     */
    @Override
    public String getDxoSuffix() {
        return toString(mirror.get("DxoSuffix"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getLogicSuffix()
     */
    @Override
    public String getLogicSuffix() {
        return toString(mirror.get("LogicSuffix"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getDaoSuffix()
     */
    @Override
    public String getDaoSuffix() {
        return toString(mirror.get("DaoSuffix"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getHelperSuffix()
     */
    @Override
    public String getHelperSuffix() {
        return toString(mirror.get("HelperSuffix"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getInterceptorSuffix()
     */
    @Override
    public String getInterceptorSuffix() {
        return toString(mirror.get("InterceptorSuffix"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getValidatorSuffix()
     */
    @Override
    public String getValidatorSuffix() {
        return toString(mirror.get("ValidatorSuffix"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getConverterSuffix()
     */
    @Override
    public String getConverterSuffix() {
        return toString(mirror.get("ConverterSuffix"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getDtoSuffix()
     */
    @Override
    public String getDtoSuffix() {
        return toString(mirror.get("DtoSuffix"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getSubApplicationRootPackageName()
     */
    @Override
    public String getSubApplicationRootPackageName() {
        return toString(mirror.get("SubApplicationRootPackageName"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getImplementationPackageName()
     */
    @Override
    public String getImplementationPackageName() {
        return toString(mirror.get("ImplementationPackageName"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getDxoPackageName()
     */
    @Override
    public String getDxoPackageName() {
        return toString(mirror.get("DxoPackageName"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getLogicPackageName()
     */
    @Override
    public String getLogicPackageName() {
        return toString(mirror.get("LogicPackageName"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getDaoPackageName()
     */
    @Override
    public String getDaoPackageName() {
        return toString(mirror.get("DaoPackageName"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getEntityPackageName()
     */
    @Override
    public String getEntityPackageName() {
        return toString(mirror.get("EntityPackageName"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getDtoPackageName()
     */
    @Override
    public String getDtoPackageName() {
        return toString(mirror.get("DtoPackageName"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getServicePackageName()
     */
    @Override
    public String getServicePackageName() {
        return toString(mirror.get("ServicePackageName"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getInterceptorPackageName()
     */
    @Override
    public String getInterceptorPackageName() {
        return toString(mirror.get("InterceptorPackageName"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getValidatorPackageName()
     */
    @Override
    public String getValidatorPackageName() {
        return toString(mirror.get("ValidatorPackageName"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getConverterPackageName()
     */
    @Override
    public String getConverterPackageName() {
        return toString(mirror.get("ConverterPackageName"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.convention.NamingConvention#getHelperPackageName()
     */
    @Override
    public String getHelperPackageName() {
        return toString(mirror.get("HelperPackageName"));
    }

    private static String toString(Object o) {
        return o == null ? "" : o.toString();
    }
}
