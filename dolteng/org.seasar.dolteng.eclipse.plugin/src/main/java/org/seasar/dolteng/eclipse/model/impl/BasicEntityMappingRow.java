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

import org.seasar.dolteng.core.entity.ColumnMetaData;
import org.seasar.dolteng.core.entity.FieldMetaData;
import org.seasar.dolteng.core.types.TypeMappingRegistry;
import org.seasar.dolteng.eclipse.model.EntityMappingRow;

/**
 * @author taichi
 * 
 */
public class BasicEntityMappingRow implements EntityMappingRow {
    protected ColumnMetaData column;

    protected FieldMetaData field;

    protected boolean generate;

    protected TypeMappingRegistry registry;

    public BasicEntityMappingRow(ColumnMetaData column, FieldMetaData field,
            TypeMappingRegistry registry) {
        this.column = column;
        this.field = field;
        this.registry = registry;
    }

    public boolean isPrimaryKey() {
        return column.isPrimaryKey();
    }

    public boolean isNullable() {
        return column.isNullable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.EntityMappingRow#isDate()
     */
    public boolean isDate() {
        return registry.isDateType(this.column);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.EntityMappingRow#isNumeric()
     */
    public boolean isNumeric() {
        return registry.isNumericType(this.column);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.EntityMappingRow#isPrimitive()
     */
    public boolean isPrimitive() {
        return registry.isPrimitive(this.column);
    }

    public String getSqlTypeName() {
        return NodeNameBuilder.getTypeName(column);
    }

    public void setSqlTypeName(String name) {
        column.setSqlTypeName(name);
    }

    public String getSqlColumnName() {
        return column.getName();
    }

    public void setSqlColumnName(String name) {
        column.setName(name);
    }

    public int getJavaModifiers() {
        return field.getModifiers();
    }

    public void setJavaModifiers(int modifiers) {
        field.setModifiers(modifiers);
    }

    public String getJavaClassName() {
        return field.getDeclaringClassName();
    }

    public void setJavaClassName(String name) {
        field.setDeclaringClassName(name);
    }

    public String getJavaFieldName() {
        return field.getName();
    }

    public void setJavaFieldName(String name) {
        field.setName(name);
    }

    public boolean isGenerate() {
        return generate;
    }

    public void setGenerate(boolean is) {
        generate = is;
    }

    public int compareTo(EntityMappingRow o) {
        if (o instanceof BasicEntityMappingRow) {
            BasicEntityMappingRow bmr = (BasicEntityMappingRow) o;
            return column.compareTo(bmr.column);
        }
        return 0;
    }

}
