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
public class BasicTypeMappingRegistry implements TypeMappingRegistry {

    protected static final TypeMapping DEFAULT = new ObjectType();

    protected Map primitiveTypes = new CaseInsensitiveMap();

    protected Map sqlTypes = new CaseInsensitiveMap();

    protected Map javaTypeNames = new CaseInsensitiveMap();

    public BasicTypeMappingRegistry() {
    }

    public void initialize() {
        register(new PrimitiveBoolean());
        register(new PrimitiveDouble());
        register(new PrimitiveFloat());
        register(new PrimitiveInt());
        register(new PrimitiveLong());
        register(new PrimitiveShort());

        register(new BooleanType());
        register(new ByteArrayType());
        register(new DecimalType());
        register(new DoubleType());
        register(new FloatType());
        register(new IntegerType());
        register(new LongType());
        register(new ShortType());
        register(new StringType());
        register(new DateType());
        register(new TimeType());
        register(new TimestampType());

        register(DEFAULT);
    }

    public void register(TypeMapping mapping) {
        if (mapping.isPrimitive()) {
            register(this.primitiveTypes, mapping);
        }
        register(this.sqlTypes, mapping);
        javaTypeNames.put(mapping.getJavaClassName(), mapping);
    };

    protected void register(Map m, TypeMapping mapping) {
        int[] nums = mapping.getSqlType();
        for (int i = 0; i < nums.length; i++) {
            m.put(String.valueOf(nums[i]), mapping);
        }
        String[] names = mapping.getSqlTypeName();
        for (int i = 0; i < names.length; i++) {
            m.put(names[i], mapping);
        }
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
        List result = new ArrayList(this.javaTypeNames.values());
        return (TypeMapping[]) result.toArray(new TypeMapping[result.size()]);
    }

}
