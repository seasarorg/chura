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

import java.util.HashMap;
import java.util.Map;

import org.seasar.dolteng.core.types.AsTypeResolver;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class AsTypeResolverImpl implements AsTypeResolver {

    private Map<String, String> mapping = new HashMap<String, String>();

    public void initialize() {
        mapping.put("java.lang.String", "String");
        mapping.put("boolean", "Boolean");
        mapping.put("java.lang.Boolean", "Boolean");
        mapping.put("byte", "int");
        mapping.put("java.lang.Byte", "int");
        mapping.put("short", "int");
        mapping.put("java.lang.Short", "int");
        mapping.put("int", "int");
        mapping.put("java.lang.Integer", "int");
        mapping.put("java.math.BigInteger", "String");
        mapping.put("java.math.BigDecimal", "String");
        mapping.put("double", "Number");
        mapping.put("java.lang.Double", "Number");
        mapping.put("long", "Number");
        mapping.put("java.lang.Long", "Number");
        mapping.put("java.util.Date", "Date");
        mapping.put("java.util.Collection", "Array");
        mapping.put("java.util.ArrayList", "Array");
        mapping.put("java.util.LinkedList", "Array");
        mapping.put("java.util.Vector", "Array");
        mapping.put("java.util.List", "Array");
        mapping.put("java.util.Set", "Array");
        mapping.put("java.util.HashSet", "Array");
        mapping.put("java.util.TreeSet", "Array");
        mapping.put("byte[]", "flash.utils.ByteArray");
        mapping.put("java.lang.Byte[]", "flash.utils.ByteArray");
        mapping.put("char", "String");
        mapping.put("java.lang.Character", "String");
        mapping.put("org.w3g.dom.Document", "XML");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.types.AsTypeResolver#resolve(java.lang.String)
     */
    public String resolve(String javaType) {
        String result = "Object";
        if (StringUtil.isEmpty(javaType) == false) {
            result = mapping.get(javaType);
        }
        return result;
    }

}
