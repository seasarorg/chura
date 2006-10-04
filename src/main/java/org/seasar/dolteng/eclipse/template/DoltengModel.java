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
package org.seasar.dolteng.eclipse.template;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.seasar.dolteng.core.entity.ColumnMetaData;
import org.seasar.dolteng.core.entity.FieldMetaData;
import org.seasar.dolteng.core.entity.TableMetaData;
import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
import org.seasar.dolteng.core.template.RootModel;
import org.seasar.dolteng.core.types.TypeMapping;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.impl.ColumnNode;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class DoltengModel extends RootModel {

    /**
     * 
     */
    public DoltengModel(Map configs) {
        super(configs);
    }

    public void initialize(TableNode node) {
        TableMetaData table = node.getMetaData();
        setTable(table);
        TreeContent[] contents = node.getChildren();
        List fields = new ArrayList(contents.length);
        for (int i = 0; i < contents.length; i++) {
            TreeContent content = contents[i];
            if (content instanceof ColumnNode) {
                ColumnNode cn = (ColumnNode) content;
                fields.add(createFieldMetaData(cn));
            }
        }
        setFields((FieldMetaData[]) fields.toArray(new FieldMetaData[fields
                .size()]));
    }

    private FieldMetaData createFieldMetaData(ColumnNode node) {
        FieldMetaData field = new BasicFieldMetaData();
        ColumnMetaData meta = node.getColumnMetaData();
        TypeMapping mapping = DoltengCore.getTypeMappingRegistry().toJavaClass(
                meta);
        field.setModifiers(Modifier.PUBLIC);
        field.setDeclaringClassName(mapping.getJavaClassName());
        field.setName(convertText(meta.getName()));

        return field;
    }

    public static String convertText(String name) {
        String[] ary = name.toLowerCase().split("_");
        StringBuffer stb = new StringBuffer();
        stb.append(ary[0]);
        for (int i = 1; i < ary.length; i++) {
            stb.append(StringUtil.capitalize(ary[i]));
        }
        return stb.toString();
    }

}
