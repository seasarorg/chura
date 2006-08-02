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

import org.seasar.dolteng.core.entity.ColumnMetaData;
import org.seasar.dolteng.core.entity.FieldMetaData;
import org.seasar.dolteng.eclipse.model.EntityMappingRow;

/**
 * @author taichi
 * 
 */
public class BasicEntityMappingRow implements EntityMappingRow {
    private ColumnMetaData column;

    private FieldMetaData field;

    public BasicEntityMappingRow(ColumnMetaData column, FieldMetaData field) {
        this.column = column;
        this.field = field;
    }

    public boolean isPrimaryKey() {
        return this.column.isPrimaryKey();
    }

    public String getSqlTypeName() {
        return NodeNameBuilder.getTypeName(column);
    }

    public void setSqlTypeName(String name) {
        this.column.setSqlTypeName(name);
    }

    public String getSqlColumnName() {
        return this.column.getName();
    }

    public void setSqlColumnName(String name) {
        this.column.setName(name);
    }

    public int getJavaModifiers() {
        return this.field.getModifiers();
    }

    public void setJavaModifiers(int modifiers) {
        this.field.setModifiers(modifiers);
    }

    public String getJavaClassName() {
        return this.field.getDeclaringClassName();
    }

    public void setJavaClassName(String name) {
        this.field.setDeclaringClassName(name);
    }

    public String getJavaFieldName() {
        return this.field.getName();
    }

    public void setJavaFieldName(String name) {
        this.field.setName(name);
    }

    public int compareTo(Object o) {
        if (o instanceof BasicEntityMappingRow) {
            BasicEntityMappingRow bmr = (BasicEntityMappingRow) o;
            return this.column.compareTo(bmr.column);
        }
        return 0;
    }

}
