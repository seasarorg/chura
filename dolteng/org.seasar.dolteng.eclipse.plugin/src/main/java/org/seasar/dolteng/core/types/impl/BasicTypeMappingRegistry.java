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
package org.seasar.dolteng.core.types.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.seasar.dolteng.core.entity.ColumnMetaData;
import org.seasar.dolteng.core.entity.FieldMetaData;
import org.seasar.dolteng.core.types.TypeMapping;
import org.seasar.dolteng.core.types.TypeMappingRegistry;
import org.seasar.framework.util.CaseInsensitiveMap;

/**
 * @author taichi
 * 
 */
@SuppressWarnings("unchecked")
public class BasicTypeMappingRegistry implements TypeMappingRegistry {

    protected static final TypeMapping DEFAULT = new ObjectType();

    protected Map<String, TypeMapping> primitiveTypes = new CaseInsensitiveMap();

    protected Map<String, TypeMapping> sqlTypes = new CaseInsensitiveMap();

    protected Map<String, TypeMapping> javaTypeNames = new CaseInsensitiveMap();

    protected Map<String, TypeMapping> numericTypes = new CaseInsensitiveMap();

    protected Map<String, TypeMapping> dateTypes = new CaseInsensitiveMap();

    public BasicTypeMappingRegistry() {
    }

    public void initialize() {
        TypeMapping tm = new PrimitiveBoolean();
        register(tm);

        tm = new PrimitiveDouble();
        register(tm);
        registerNumeric(tm);

        tm = new PrimitiveFloat();
        register(tm);
        registerNumeric(tm);

        tm = new PrimitiveInt();
        register(tm);
        registerNumeric(tm);

        tm = new PrimitiveLong();
        register(tm);
        registerNumeric(tm);

        tm = new PrimitiveShort();
        register(tm);
        registerNumeric(tm);

        tm = new BooleanType();
        register(tm);
        tm = new ByteArrayType();
        register(tm);
        tm = new DecimalType();
        register(tm);
        registerNumeric(tm);

        tm = new DoubleType();
        register(tm);
        registerNumeric(tm);

        tm = new FloatType();
        register(tm);
        registerNumeric(tm);

        tm = new IntegerType();
        register(tm);
        registerNumeric(tm);

        tm = new LongType();
        register(tm);
        registerNumeric(tm);

        tm = new ShortType();
        register(tm);
        registerNumeric(tm);

        tm = new StringType();
        register(tm);
        tm = new DateType();
        register(tm);
        registerDate(tm);

        tm = new TimeType();
        register(tm);
        registerDate(tm);

        tm = new TimestampType();
        register(tm);
        registerDate(tm);

        register(DEFAULT);
    }

    public void register(TypeMapping mapping) {
        if (mapping.isPrimitive()) {
            register(this.primitiveTypes, mapping);
        }
        register(this.sqlTypes, mapping);
        javaTypeNames.put(mapping.getJavaClassName(), mapping);
    }

    protected void register(Map<String, TypeMapping> m, TypeMapping mapping) {
        for (int num : mapping.getSqlType()) {
            m.put(String.valueOf(num), mapping);
        }
        for (String name : mapping.getSqlTypeName()) {
            m.put(name, mapping);
        }
    }

    public void registerNumeric(TypeMapping mapping) {
        register(this.numericTypes, mapping);
    }

    public void registerDate(TypeMapping mapping) {
        register(this.dateTypes, mapping);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.types.TypeMapper#toJavaClass(org.seasar.dolteng.core.entity.ColumnMetaData)
     */
    public TypeMapping toJavaClass(ColumnMetaData meta) {
        TypeMapping tm = null;
        if (meta.isPrimaryKey() || meta.isNullable() == false) {
            tm = find(this.primitiveTypes, meta.getSqlTypeName());
        }
        if (tm == null) {
            tm = find(this.sqlTypes, meta.getSqlTypeName());
        }
        if (tm == null) {
            tm = find(this.sqlTypes, String.valueOf(meta.getSqlType()));
        }
        return tm == null ? DEFAULT : tm;
    }

    protected TypeMapping find(Map m, Object key) {
        return (TypeMapping) m.get(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.types.TypeMapper#toSqlType(org.seasar.dolteng.core.entity.FieldMetaData)
     */
    public TypeMapping toSqlType(FieldMetaData meta) {
        TypeMapping tm = find(this.javaTypeNames, meta.getDeclaringClassName());
        return tm == null ? DEFAULT : tm;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.types.TypeMappingRegistry#findAllTypes()
     */
    public TypeMapping[] findAllTypes() {
        List<TypeMapping> result = new ArrayList<TypeMapping>(this.javaTypeNames.values());
        return result.toArray(new TypeMapping[result.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.types.TypeMappingRegistry#isDateType(org.seasar.dolteng.core.entity.ColumnMetaData)
     */
    public boolean isDateType(ColumnMetaData meta) {
        TypeMapping tm = find(this.dateTypes, meta.getSqlTypeName());
        if (tm == null) {
            tm = find(this.dateTypes, String.valueOf(meta.getSqlType()));
        }
        return tm != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.types.TypeMappingRegistry#isNumericType(org.seasar.dolteng.core.entity.ColumnMetaData)
     */
    public boolean isNumericType(ColumnMetaData meta) {
        TypeMapping tm = find(this.numericTypes, meta.getSqlTypeName());
        if (tm == null) {
            tm = find(this.numericTypes, String.valueOf(meta.getSqlType()));
        }
        return tm != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.types.TypeMappingRegistry#isPrimitive(org.seasar.dolteng.core.entity.ColumnMetaData)
     */
    public boolean isPrimitive(ColumnMetaData meta) {
        TypeMapping tm = find(this.primitiveTypes, meta.getSqlTypeName());
        if (tm == null) {
            tm = find(this.primitiveTypes, String.valueOf(meta.getSqlType()));
        }
        return tm != null;
    }

}
