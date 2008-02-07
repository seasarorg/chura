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

import org.seasar.dolteng.eclipse.model.MxBindingMappingRow;

/**
 * @author taichi
 * 
 */
public class BasicMxBindingMappingRow implements MxBindingMappingRow {

    private String componentId = "";

    private String srcAttr = "";

    private String destId = "";

    private boolean isGenerate = false;

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.MxBindingMappingRow#getComponentId()
     */
    public String getComponentId() {
        return componentId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.MxBindingMappingRow#getDestId()
     */
    public String getDestId() {
        return destId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.MxBindingMappingRow#getSrcAttr()
     */
    public String getSrcAttr() {
        return srcAttr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.MxBindingMappingRow#isGenerate()
     */
    public boolean isGenerate() {
        return isGenerate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.MxBindingMappingRow#setComponentId(java.lang.String)
     */
    public void setComponentId(String id) {
        this.componentId = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.MxBindingMappingRow#setDestId(java.lang.String)
     */
    public void setDestId(String id) {
        this.destId = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.MxBindingMappingRow#setGenerate(boolean)
     */
    public void setGenerate(boolean is) {
        this.isGenerate = is;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.MxBindingMappingRow#setSrcAttr(java.lang.String)
     */
    public void setSrcAttr(String attr) {
        this.srcAttr = attr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.MxBindingMappingRow#toXml()
     */
    public String toXml() {
        StringBuffer stb = new StringBuffer(500);
        stb.append("<mx:Binding source=\"");
        stb.append(getComponentId());
        stb.append('.');
        stb.append(getSrcAttr());
        stb.append("\" destination=\"");
        stb.append(getDestId());
        stb.append("\"/>");
        stb.append(System.getProperty("line.separator", "\r\n"));
        stb.append("<mx:Binding source=\"");
        stb.append(getDestId());
        stb.append("\" destination=\"");
        stb.append(getComponentId());
        stb.append('.');
        stb.append(getSrcAttr());
        stb.append("\"/>");
        return stb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if (o instanceof MxBindingMappingRow) {
            MxBindingMappingRow other = (MxBindingMappingRow) o;
            return compareTo(other);
        }
        return 0;
    }

    public int compareTo(MxBindingMappingRow row) {
        return getComponentId().compareTo(row.getComponentId());
    }

}
