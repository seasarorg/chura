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
package org.seasar.dolteng.core.convention;

import junit.framework.TestCase;

import org.seasar.dolteng.eclipse.convention.NamingConventionMirror;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.convention.impl.NamingConventionImpl;

/**
 * @author taichi
 * 
 */
public class NamingConventionMirrorTest extends TestCase {

    private NamingConventionImpl source;

    private NamingConvention target;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.source = new NamingConventionImpl();
        this.source.addRootPackageName("hoge.fuga.moge");
        this.target = new NamingConventionMirror(null, NamingConvention.class,
                this.source);
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getViewRootPath()'
     */
    public void testGetViewRootPath() {
        assertEquals(this.source.getViewRootPath(), this.target
                .getViewRootPath());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getViewExtension()'
     */
    public void testGetViewExtension() {
        assertEquals(this.source.getViewExtension(), this.target
                .getViewExtension());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getImplementationSuffix()'
     */
    public void testGetImplementationSuffix() {
        assertEquals(this.source.getImplementationSuffix(), this.target
                .getImplementationSuffix());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getPageSuffix()'
     */
    public void testGetPageSuffix() {
        assertEquals(this.source.getPageSuffix(), this.target.getPageSuffix());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getActionSuffix()'
     */
    public void testGetActionSuffix() {
        assertEquals(this.source.getActionSuffix(), this.target
                .getActionSuffix());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getServiceSuffix()'
     */
    public void testGetServiceSuffix() {
        assertEquals(this.source.getServiceSuffix(), this.target
                .getServiceSuffix());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getDxoSuffix()'
     */
    public void testGetDxoSuffix() {
        assertEquals(this.source.getDxoSuffix(), this.target.getDxoSuffix());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getLogicSuffix()'
     */
    public void testGetLogicSuffix() {
        assertEquals(this.source.getLogicSuffix(), this.target.getLogicSuffix());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getDaoSuffix()'
     */
    public void testGetDaoSuffix() {
        assertEquals(this.source.getDaoSuffix(), this.target.getDaoSuffix());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getHelperSuffix()'
     */
    public void testGetHelperSuffix() {
        assertEquals(this.source.getHelperSuffix(), this.target
                .getHelperSuffix());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getInterceptorSuffix()'
     */
    public void testGetInterceptorSuffix() {
        assertEquals(this.source.getInterceptorSuffix(), this.target
                .getInterceptorSuffix());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getValidatorSuffix()'
     */
    public void testGetValidatorSuffix() {
        assertEquals(this.source.getValidatorSuffix(), this.target
                .getValidatorSuffix());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getConverterSuffix()'
     */
    public void testGetConverterSuffix() {
        assertEquals(this.source.getConverterSuffix(), this.target
                .getConverterSuffix());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getDtoSuffix()'
     */
    public void testGetDtoSuffix() {
        assertEquals(this.source.getDtoSuffix(), this.target.getDtoSuffix());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getSubApplicationRootPackageName()'
     */
    public void testGetSubApplicationRootPackageName() {
        assertEquals(this.source.getSubApplicationRootPackageName(),
                this.target.getSubApplicationRootPackageName());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getImplementationPackageName()'
     */
    public void testGetImplementationPackageName() {
        assertEquals(this.source.getImplementationPackageName(), this.target
                .getImplementationPackageName());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getDxoPackageName()'
     */
    public void testGetDxoPackageName() {
        assertEquals(this.source.getDxoPackageName(), this.target
                .getDxoPackageName());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getLogicPackageName()'
     */
    public void testGetLogicPackageName() {
        assertEquals(this.source.getLogicPackageName(), this.target
                .getLogicPackageName());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getDaoPackageName()'
     */
    public void testGetDaoPackageName() {
        assertEquals(this.source.getDaoPackageName(), this.target
                .getDaoPackageName());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getEntityPackageName()'
     */
    public void testGetEntityPackageName() {
        assertEquals(this.source.getEntityPackageName(), this.target
                .getEntityPackageName());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getDtoPackageName()'
     */
    public void testGetDtoPackageName() {
        assertEquals(this.source.getDtoPackageName(), this.target
                .getDtoPackageName());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getServicePackageName()'
     */
    public void testGetServicePackageName() {
        assertEquals(this.source.getServicePackageName(), this.target
                .getServicePackageName());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getInterceptorPackageName()'
     */
    public void testGetInterceptorPackageName() {
        assertEquals(this.source.getInterceptorPackageName(), this.target
                .getInterceptorPackageName());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getValidatorPackageName()'
     */
    public void testGetValidatorPackageName() {
        assertEquals(this.source.getValidatorPackageName(), this.source
                .getValidatorPackageName());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getConverterPackageName()'
     */
    public void testGetConverterPackageName() {
        assertEquals(this.source.getConverterPackageName(), this.target
                .getConverterPackageName());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getHelperPackageName()'
     */
    public void testGetHelperPackageName() {
        assertEquals(this.source.getHelperPackageName(), this.target
                .getHelperPackageName());
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.convention.NamingConventionMirror.getRootPackageNames()'
     */
    public void testGetRootPackageNames() {
        assertEquals(this.source.getRootPackageNames(), this.target
                .getRootPackageNames());
    }

}
