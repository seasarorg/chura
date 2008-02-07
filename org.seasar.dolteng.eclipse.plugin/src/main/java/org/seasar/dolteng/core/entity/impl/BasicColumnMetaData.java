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
package org.seasar.dolteng.core.entity.impl;

import java.sql.Types;

import org.seasar.dolteng.core.entity.ColumnMetaData;

/**
 * @author taichi
 * 
 */
public class BasicColumnMetaData extends AbstractNamedMetaData implements
        ColumnMetaData {

    private int sqlType = Types.NULL;

    private String sqlTypeName = "";

    private int columnSize = 0;

    private int decimalDigits = 0;

    private boolean primaryKey = false;

    private boolean foreignKey = false;

    private boolean nullable = false;

    /**
     * @return Returns the foreignKey.
     */
    public boolean isForeignKey() {
        return this.foreignKey;
    }

    /**
     * @param foreignKey
     *            The foreignKey to set.
     */
    public void setForeignKey(boolean foreignKey) {
        this.foreignKey = foreignKey;
    }

    /**
     * @return Returns the primaryKey.
     */
    public boolean isPrimaryKey() {
        return this.primaryKey;
    }

    /**
     * @param primaryKey
     *            The primaryKey to set.
     */
    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * @return Returns the sqlType.
     */
    public int getSqlType() {
        return this.sqlType;
    }

    /**
     * @param sqlType
     *            The sqlType to set.
     */
    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    /**
     * @return Returns the size.
     */
    public int getColumnSize() {
        return this.columnSize;
    }

    /**
     * @param size
     *            The size to set.
     */
    public void setColumnSize(int size) {
        this.columnSize = size;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.entity.ColumnMetaData#getDecimalDigits()
     */
    public int getDecimalDigits() {
        return decimalDigits;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.entity.ColumnMetaData#setDecimalDigits(int)
     */
    public void setDecimalDigits(int size) {
        this.decimalDigits = size;
    }

    /**
     * @return Returns the sqlTypeName.
     */
    public String getSqlTypeName() {
        return this.sqlTypeName;
    }

    /**
     * @param sqlTypeName
     *            The sqlTypeName to set.
     */
    public void setSqlTypeName(String sqlTypeName) {
        this.sqlTypeName = sqlTypeName;
    }

    /**
     * @return Returns the nullable.
     */
    public boolean isNullable() {
        return this.nullable;
    }

    /**
     * @param nullable
     *            The nullable to set.
     */
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    // public int compareTo(Object other) {
    // if (other instanceof ColumnMetaData) {
    // ColumnMetaData cmd = (ColumnMetaData) other;
    // if (this.isPrimaryKey() && cmd.isPrimaryKey() == false) {
    // return -1;
    // } else if (this.isPrimaryKey() == false && cmd.isPrimaryKey()) {
    // return 1;
    // } else {
    // return super.compareTo(other);
    // }
    // }
    // return super.compareTo(other);
    // }
}
