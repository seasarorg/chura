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

/**
 * @author taichi
 * 
 */
public class NodeNameBuilder {

    public static String getName(ColumnMetaData meta) {
        StringBuffer stb = new StringBuffer();
        stb.append(meta.getName());
        stb.append(" ");
        stb.append(':');
        stb.append(" ");
        stb.append(getTypeName(meta));

        return stb.toString();
    }

    public static String getTypeName(ColumnMetaData meta) {
        StringBuffer stb = new StringBuffer();
        stb.append(meta.getSqlTypeName());
        if (0 < meta.getColumnSize()) {
            stb.append('(');
            stb.append(meta.getColumnSize());
            if (0 < meta.getDecimalDigits()) {
                stb.append('.');
                stb.append(meta.getDecimalDigits());
            }
            stb.append(')');
        }
        return stb.toString();
    }
}
