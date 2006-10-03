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
package org.seasar.dolteng.core.template;

import org.seasar.dolteng.core.entity.ClassMetaData;
import org.seasar.dolteng.core.entity.FieldMetaData;
import org.seasar.dolteng.core.entity.TableMetaData;
import org.seasar.framework.convention.NamingConvention;

/**
 * @author taichi
 * 
 */
public class RootModel {

    private String typeName;

    private NamingConvention namingConvention;

    private TableMetaData table;

    private ClassMetaData clazz;

    private FieldMetaData[] fields;

    public RootModel() {
        super();
    }

    /**
     * @return Returns the clazz.
     */
    public ClassMetaData getClazz() {
        return clazz;
    }

    /**
     * @param clazz
     *            The clazz to set.
     */
    public void setClazz(ClassMetaData clazz) {
        this.clazz = clazz;
    }

    /**
     * @return Returns the fields.
     */
    public FieldMetaData[] getFields() {
        return fields;
    }

    /**
     * @param fields
     *            The fields to set.
     */
    public void setFields(FieldMetaData[] fields) {
        this.fields = fields;
    }

    /**
     * @param typeName
     *            The typeName to set.
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * @return Returns the typeName.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * @return Returns the table.
     */
    public TableMetaData getTable() {
        return table;
    }

    /**
     * @param table
     *            The table to set.
     */
    public void setTable(TableMetaData table) {
        this.table = table;
    }

    /**
     * @return Returns the namingConvention.
     */
    public NamingConvention getNamingConvention() {
        return namingConvention;
    }

    /**
     * @param namingConvention The namingConvention to set.
     */
    public void setNamingConvention(NamingConvention namingConvention) {
        this.namingConvention = namingConvention;
    }

}
