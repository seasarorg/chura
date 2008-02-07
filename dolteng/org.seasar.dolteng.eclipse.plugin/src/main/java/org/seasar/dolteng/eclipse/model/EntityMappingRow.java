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
package org.seasar.dolteng.eclipse.model;


/**
 * @author taichi
 * 
 */
public interface EntityMappingRow extends Comparable<EntityMappingRow>, IsGenerateDescriptor {

    public boolean isPrimaryKey();

    public boolean isNullable();

    public boolean isNumeric();

    public boolean isDate();

    public boolean isPrimitive();

    public String getSqlTypeName();

    public void setSqlTypeName(String name);

    public String getSqlColumnName();

    public void setSqlColumnName(String name);

    public int getJavaModifiers();

    public void setJavaModifiers(int modifiers);

    public String getJavaClassName();

    public void setJavaClassName(String name);

    public String getJavaFieldName();

    public void setJavaFieldName(String name);
}
