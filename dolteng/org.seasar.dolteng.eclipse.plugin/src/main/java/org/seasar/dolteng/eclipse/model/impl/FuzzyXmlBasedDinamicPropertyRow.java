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

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;

import org.seasar.dolteng.eclipse.model.DynamicPropertyRow;

/**
 * @author taichi
 * 
 */
public class FuzzyXmlBasedDinamicPropertyRow implements DynamicPropertyRow {

    private boolean isCreate;

    private FuzzyXMLAttribute attr;

    public FuzzyXmlBasedDinamicPropertyRow(FuzzyXMLAttribute attr) {
        super();
        this.attr = attr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.DinamicPropertyRow#isCreate()
     */
    public boolean isCreate() {
        return this.isCreate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.DinamicPropertyRow#setCreate(boolean)
     */
    public void setCreate(boolean is) {
        isCreate = is;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.DinamicPropertyRow#getName()
     */
    public String getName() {
        return attr.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.DinamicPropertyRow#getValue()
     */
    public String getValue() {
        return attr.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.DinamicPropertyRow#setValue(java.lang.String)
     */
    public void setValue(String value) {
        this.attr.setValue(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if (o instanceof FuzzyXmlBasedDinamicPropertyRow) {
            FuzzyXmlBasedDinamicPropertyRow row = (FuzzyXmlBasedDinamicPropertyRow) o;
            return this.attr.getName().compareTo(row.attr.getName());
        }
        return 0;
    }

    public FuzzyXMLAttribute getAttribute() {
        return this.attr;
    }
}
