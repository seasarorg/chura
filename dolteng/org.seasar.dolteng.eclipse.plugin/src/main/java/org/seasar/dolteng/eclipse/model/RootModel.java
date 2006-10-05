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
package org.seasar.dolteng.eclipse.model;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.seasar.dolteng.core.entity.ColumnMetaData;
import org.seasar.dolteng.core.entity.FieldMetaData;
import org.seasar.dolteng.core.entity.TableMetaData;
import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
import org.seasar.dolteng.core.types.TypeMapping;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.impl.BasicEntityMappingRow;
import org.seasar.dolteng.eclipse.model.impl.ColumnNode;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class RootModel {

    private String typeName;

    private NamingConvention namingConvention;

    private TableMetaData table;

    private EntityMappingRow[] mappings;

    private Map configs;

    public RootModel(Map configs) {
        super();
        this.configs = configs;
    }

    public void initialize(TableNode node) {
        TableMetaData table = node.getMetaData();
        setTable(table);
        TreeContent[] contents = node.getChildren();
        List rows = new ArrayList(contents.length);
        for (int i = 0; i < contents.length; i++) {
            TreeContent content = contents[i];
            if (content instanceof ColumnNode) {
                ColumnNode cn = (ColumnNode) content;
                ColumnMetaData meta = cn.getColumnMetaData();
                rows.add(createEntityMappingRow(meta));
            }
        }
        setMappings((EntityMappingRow[]) rows.toArray(new EntityMappingRow[rows
                .size()]));
    }

    private EntityMappingRow createEntityMappingRow(ColumnMetaData column) {
        FieldMetaData field = new BasicFieldMetaData();
        TypeMapping mapping = DoltengCore.getTypeMappingRegistry().toJavaClass(
                column);
        field.setModifiers(Modifier.PUBLIC);
        field.setDeclaringClassName(mapping.getJavaClassName());
        field.setName(convertText(column.getName()));

        return new BasicEntityMappingRow(column, field);
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

    /**
     * @param typeName
     *            The typeName to set.
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * @return Returns the typeName.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * @return Returns the table.
     */
    public TableMetaData getTable() {
        return table;
    }

    /**
     * @param table
     *            The table to set.
     */
    public void setTable(TableMetaData table) {
        this.table = table;
    }

    /**
     * @return Returns the namingConvention.
     */
    public NamingConvention getNamingConvention() {
        return namingConvention;
    }

    /**
     * @param namingConvention
     *            The namingConvention to set.
     */
    public void setNamingConvention(NamingConvention namingConvention) {
        this.namingConvention = namingConvention;
    }

    /**
     * @return Returns the configs.
     */
    public Map getConfigs() {
        return configs;
    }

    /**
     * @return Returns the mappings.
     */
    public EntityMappingRow[] getMappings() {
        return mappings;
    }

    /**
     * @param mappings
     *            The mappings to set.
     */
    public void setMappings(EntityMappingRow[] mappings) {
        this.mappings = mappings;
    }

}
