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
package org.seasar.dolteng.core.entity;

/**
 * @author taichi
 * 
 */
public interface ColumnMetaData extends NamedMetaData {

    /**
     * @see java.sql.Types
     */
    public int getSqlType();

    /**
     * @see java.sql.Types
     */
    public void setSqlType(int type);

    public String getSqlTypeName();

    public void setSqlTypeName(String name);

    public int getColumnSize();

    public void setColumnSize(int size);

    public int getDecimalDigits();

    public void setDecimalDigits(int size);

    public boolean isPrimaryKey();

    public void setPrimaryKey(boolean primaryKey);

    // public boolean isForeignKey(); // TODO : 対応出来ないかも。

    public boolean isNullable();

    public void setNullable(boolean nullable);
}
