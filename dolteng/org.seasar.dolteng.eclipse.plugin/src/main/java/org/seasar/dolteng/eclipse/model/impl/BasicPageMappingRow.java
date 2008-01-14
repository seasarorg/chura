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

import org.seasar.dolteng.core.entity.FieldMetaData;
import org.seasar.dolteng.eclipse.model.PageMappingRow;

/**
 * @author taichi
 * 
 */
public class BasicPageMappingRow implements PageMappingRow {

    private boolean isSuperGenerate = false;

    private boolean isThisGenerate = false;

    private FieldMetaData entityField;

    private FieldMetaData pageField;

    public BasicPageMappingRow(FieldMetaData entityField,
            FieldMetaData pageField) {
        super();
        this.entityField = entityField;
        this.pageField = pageField;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#isThisGenerate()
     */
    public boolean isThisGenerate() {
        return this.isThisGenerate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#setThisGenerate(boolean)
     */
    public void setThisGenerate(boolean is) {
        this.isThisGenerate = is;
        if (this.isThisGenerate) {
            this.isSuperGenerate = false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#isSuperGenerate()
     */
    public boolean isSuperGenerate() {
        return this.isSuperGenerate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#setSuperGenerate()
     */
    public void setSuperGenerate(boolean is) {
        this.isSuperGenerate = is;
        if (this.isSuperGenerate) {
            this.isThisGenerate = false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#getEntityClassName()
     */
    public String getSrcClassName() {
        return this.entityField.getDeclaringClassName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#setEntityClassName(java.lang.String)
     */
    public void setSrcClassName(String name) {
        this.entityField.setDeclaringClassName(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#getEntityFieldName()
     */
    public String getSrcFieldName() {
        return this.entityField.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#setEntityFieldName(java.lang.String)
     */
    public void setSrcFieldName(String name) {
        this.entityField.setName(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#getPageModifiers()
     */
    public int getPageModifiers() {
        return this.pageField.getModifiers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#setPageModifiers(int)
     */
    public void setPageModifiers(int modifiers) {
        this.pageField.setModifiers(modifiers);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#getPageClassName()
     */
    public String getPageClassName() {
        return this.pageField.getDeclaringClassName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#setPageClassName(java.lang.String)
     */
    public void setPageClassName(String name) {
        this.pageField.setDeclaringClassName(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#getPageFieldName()
     */
    public String getPageFieldName() {
        return this.pageField.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#setPageFieldName(java.lang.String)
     */
    public void setPageFieldName(String name) {
        this.pageField.setName(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(PageMappingRow o) {
        PageMappingRow pmr = o;
        return this.getPageFieldName().compareTo(pmr.getPageFieldName());
    }

}
