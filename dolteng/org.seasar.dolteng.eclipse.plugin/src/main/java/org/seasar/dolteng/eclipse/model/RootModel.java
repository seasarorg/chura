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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.dolteng.core.entity.ColumnMetaData;
import org.seasar.dolteng.core.entity.FieldMetaData;
import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
import org.seasar.dolteng.core.types.TypeMapping;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.impl.BasicEntityMappingRow;
import org.seasar.dolteng.eclipse.model.impl.ColumnNode;
import org.seasar.dolteng.eclipse.model.impl.ProjectNode;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.dolteng.eclipse.util.NameConverter;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class RootModel {

    private String typeName;

    private NamingConvention namingConvention;

    private EntityMappingRow[] mappings;

    private Map configs;

    private IJavaProject project;

    public RootModel(Map configs) {
        super();
        this.configs = configs;
    }

    public void initialize(TableNode node) {
        List columns = Arrays.asList(node.getChildren());
        Collections.sort(columns);
        List rows = new ArrayList(columns.size());
        for (int i = 0; i < columns.size(); i++) {
            TreeContent content = (TreeContent) columns.get(i);
            if (content instanceof ColumnNode) {
                ColumnNode cn = (ColumnNode) content;
                ColumnMetaData meta = cn.getColumnMetaData();
                rows.add(createEntityMappingRow(meta));
            }
        }
        setMappings((EntityMappingRow[]) rows.toArray(new EntityMappingRow[rows
                .size()]));
        ProjectNode n = (ProjectNode) node.getRoot();
        this.project = n.getJavaProject();
    }

    private EntityMappingRow createEntityMappingRow(ColumnMetaData column) {
        FieldMetaData field = new BasicFieldMetaData();
        TypeMapping mapping = DoltengCore.getTypeMappingRegistry().toJavaClass(
                column);
        field.setModifiers(Modifier.PUBLIC);
        field.setDeclaringClassName(mapping.getJavaClassName());
        field.setName(StringUtil.decapitalize(NameConverter.toCamelCase(column
                .getName())));

        return new BasicEntityMappingRow(column, field);
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

    public String getImports() {
        Set imports = new HashSet();
        for (int i = 0; i < mappings.length; i++) {
            EntityMappingRow row = mappings[i];
            String pkg = row.getJavaClassName();
            if (pkg.startsWith("java.lang") == false) {
                imports.add(pkg);
            }
        }
        String separator = System.getProperty("line.separator", "\n");
        StringBuffer stb = new StringBuffer();
        for (final Iterator i = imports.iterator(); i.hasNext();) {
            stb.append("import ");
            stb.append(i.next());
            stb.append(';');
            stb.append(separator);
        }

        return stb.toString();
    }

    public String getJavaClassName(EntityMappingRow row) {
        return ClassUtil.getShortClassName(row.getJavaClassName());
    }

    public String createPkeyMethodArgs() {
        StringBuffer stb = new StringBuffer();
        boolean is = false;
        for (int i = 0; i < mappings.length; i++) {
            EntityMappingRow row = mappings[i];
            if (row.isPrimaryKey()) {
                String s = row.getJavaClassName();
                if (s.startsWith("java.lang")) {
                    s = ClassUtil.getShortClassName(s);
                }
                stb.append(s);
                stb.append(' ');
                stb.append(row.getJavaFieldName().toLowerCase());
                stb.append(',');
                is = true;
            }
        }
        if (is) {
            stb.setLength(stb.length() - 1);
        }
        return stb.toString();
    }

    public String createPkeyMethodArgNames() {
        StringBuffer stb = new StringBuffer();
        boolean is = false;
        stb.append('"');
        for (int i = 0; i < mappings.length; i++) {
            EntityMappingRow row = mappings[i];
            if (row.isPrimaryKey()) {
                stb.append(row.getJavaFieldName().toLowerCase());
                stb.append(',');
                is = true;
            }
        }
        if (is) {
            stb.setLength(stb.length() - 1);
        }
        stb.append('"');
        return stb.toString();
    }

    public int countPkeys() {
        int result = 0;
        for (int i = 0; i < mappings.length; i++) {
            EntityMappingRow row = mappings[i];
            if (row.isPrimaryKey()) {
                result++;
            }
        }
        return result;
    }

    public String createPkeyMethodCallArgs() {
        StringBuffer stb = new StringBuffer();
        boolean is = false;
        for (int i = 0; i < mappings.length; i++) {
            EntityMappingRow row = mappings[i];
            if (row.isPrimaryKey()) {
                stb.append("get");
                stb.append(StringUtil.capitalize(row.getJavaFieldName()));
                stb.append("()");
                stb.append(' ');
                stb.append(',');
                is = true;
            }
        }
        if (is) {
            stb.setLength(stb.length() - 2);
        }
        return stb.toString();
    }

    public String createPkeyMethodCallArgsCopy() {
        StringBuffer stb = new StringBuffer();
        boolean is = false;
        for (int i = 0; i < mappings.length; i++) {
            EntityMappingRow row = mappings[i];
            if (row.isPrimaryKey()) {
                stb.append(row.getJavaFieldName());
                stb.append(' ');
                stb.append(',');
                is = true;
            }
        }
        if (is) {
            stb.setLength(stb.length() - 2);
        }
        return stb.toString();
    }

    public String createPkeyLink() {
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < mappings.length; i++) {
            EntityMappingRow row = mappings[i];
            if (row.isPrimaryKey()) {
                stb.append('&');
                stb.append(row.getJavaFieldName());
                stb.append('=');
                stb.append('$');
                stb.append(row.getJavaFieldName());
            }
        }
        return stb.toString();
    }

    public boolean isTigerResource() {
        return this.project.getOption(JavaCore.COMPILER_COMPLIANCE, true)
                .startsWith(JavaCore.VERSION_1_5);
    }
}
