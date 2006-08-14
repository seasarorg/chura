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
package org.seasar.dolteng.eclipse.model.impl;

import org.seasar.dolteng.core.entity.FieldMetaData;
import org.seasar.dolteng.eclipse.model.PageMappingRow;

/**
 * @author taichi
 * 
 */
public class BasicPageMappingRow implements PageMappingRow {

    private boolean isGenerate = false;

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
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#isGenerate()
     */
    public boolean isGenerate() {
        return this.isGenerate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#setGenerate(boolean)
     */
    public void setGenerate(boolean is) {
        this.isGenerate = is;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#getEntityClassName()
     */
    public String getEntityClassName() {
        return this.entityField.getDeclaringClassName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#setEntityClassName(java.lang.String)
     */
    public void setEntityClassName(String name) {
        this.entityField.setDeclaringClassName(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#getEntityFieldName()
     */
    public String getEntityFieldName() {
        return this.entityField.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.PageMappingRow#setEntityFieldName(java.lang.String)
     */
    public void setEntityFieldName(String name) {
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
    public int compareTo(Object o) {
        if (o instanceof PageMappingRow) {
            PageMappingRow pmr = (PageMappingRow) o;
            return pmr.getPageFieldName().compareTo(this.getPageFieldName());
        }
        return 0;
    }

}
