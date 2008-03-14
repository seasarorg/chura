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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.seasar.dolteng.core.entity.ColumnMetaData;
import org.seasar.dolteng.core.entity.FieldMetaData;
import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
import org.seasar.dolteng.core.types.TypeMapping;
import org.seasar.dolteng.core.types.TypeMappingRegistry;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.convention.NamingUtil;
import org.seasar.dolteng.eclipse.model.EntityMappingRow;
import org.seasar.dolteng.eclipse.model.RootModel;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.util.NameConverter;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class ScaffoldModel implements RootModel {

    private String typeName;

    private NamingConvention namingConvention;

    private EntityMappingRow[] mappings;

    private Map<String, String> configs;

    private IJavaProject project;

    public ScaffoldModel(Map<String, String> configs, TableNode node) {
        super();
        this.configs = configs;
        initialize(node);
    }

    @SuppressWarnings("unchecked")
    protected void initialize(TableNode node) {
        ProjectNode n = (ProjectNode) node.getRoot();
        this.project = n.getJavaProject();

        List<TreeContent> columns = Arrays.asList(node.getChildren());
        Collections.sort(columns);
        List rows = new ArrayList(columns.size());
        for (TreeContent content : columns) {
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
        TypeMappingRegistry registry = DoltengCore
                .getTypeMappingRegistry(this.project);
        TypeMapping mapping = registry.toJavaClass(column);
        field.setModifiers(Modifier.PUBLIC);
        field.setDeclaringClassName(mapping.getJavaClassName());
        field.setName(StringUtil.decapitalize(NameConverter.toCamelCase(column
                .getName())));

        return new BasicEntityMappingRow(column, field, registry);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.impl.RootModel#setNamingConvention(org.seasar.framework.convention.NamingConvention)
     */
    public void setNamingConvention(NamingConvention namingConvention) {
        this.namingConvention = namingConvention;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.impl.RootModel#getConfigs()
     */
    public Map<String, String> getConfigs() {
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
        Set<String> imports = new HashSet<String>();
        for (EntityMappingRow row : mappings) {
            if (row.isPrimitive()) {
                continue;
            }
            String pkg = row.getJavaClassName();
            if (pkg.startsWith("java.lang") == false) {
                imports.add(pkg);
            }
        }
        String separator = System.getProperty("line.separator", "\n");
        StringBuffer stb = new StringBuffer();
        for (String element : imports) {
            stb.append("import ");
            stb.append(element);
            stb.append(';');
            stb.append(separator);
        }

        return stb.toString();
    }

    public String getJavaClassName(EntityMappingRow row) {
        return ClassUtil.getShortClassName(row.getJavaClassName());
    }

    public String createPkeyMethodArgs(boolean includeVersion) {
        StringBuffer stb = new StringBuffer();
        boolean is = false;
        for (EntityMappingRow row : mappings) {
            if (row.isPrimaryKey()
                    || (includeVersion && NamingUtil.isVersionNo(row
                            .getSqlColumnName()))) {
                String s = row.getJavaClassName();
                if (s.startsWith("java.lang")) {
                    s = ClassUtil.getShortClassName(s);
                }
                stb.append(s);
                stb.append(' ');
                stb.append(row.getJavaFieldName());
                stb.append(',');
                is |= true;
            }
        }
        if (is) {
            stb.setLength(stb.length() - 1);
        }
        return stb.toString();

    }

    public String createPkeyMethodArgs() {
        return createPkeyMethodArgs(false);
    }

    public String createPkeyMethodArgNames() {
        if (isTigerResource()) {
            return createAnnotationArgNames();
        }
        return createConstArgNames();
    }

    private String createAnnotationArgNames() {
        List<EntityMappingRow> prows = new ArrayList<EntityMappingRow>();
        for (EntityMappingRow row : mappings) {
            if (row.isPrimaryKey()) {
                prows.add(row);
            }
        }

        StringBuffer stb = new StringBuffer();
        if (0 < prows.size()) {
            boolean is = 1 < prows.size();
            if (is) {
                stb.append('{');
            }
            for (EntityMappingRow row : prows) {
                stb.append('"');
                stb.append(row.getSqlColumnName());
                stb.append('"');
                stb.append(',');
            }
            stb.setLength(stb.length() - 1);
            if (is) {
                stb.append('}');
            }
        }
        return stb.toString();
    }

    private String createConstArgNames() {
        StringBuffer stb = new StringBuffer();
        boolean is = false;
        stb.append('"');
        for (EntityMappingRow row : mappings) {
            if (row.isPrimaryKey()) {
                stb.append(row.getSqlColumnName());
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
        for (EntityMappingRow row : mappings) {
            if (row.isPrimaryKey()) {
                result++;
            }
        }
        return result;
    }

    public String createPkeyMethodCallArgs(boolean includeVersion, String prefix) {
        StringBuffer stb = new StringBuffer();
        boolean is = false;
        for (EntityMappingRow row : mappings) {
            if (row.isPrimaryKey()
                    || (includeVersion && NamingUtil.isVersionNo(row
                            .getSqlColumnName()))) {
                stb.append(prefix);
                stb.append("get");
                stb.append(StringUtil.capitalize(row.getJavaFieldName()));
                stb.append("()");
                stb.append(',');
                stb.append(' ');
                is = true;
            }
        }
        if (is) {
            stb.setLength(stb.length() - 2);
        }
        return stb.toString();
    }

    public String createPkeyMethodCallArgs(boolean includeVersion) {
        return createPkeyMethodCallArgs(includeVersion, "");
    }

    public String createPkeyMethodCallArgs() {
        return createPkeyMethodCallArgs(false);
    }

    public String createPkeyMethodCallArgsCopy(boolean includeVersion) {
        StringBuffer stb = new StringBuffer();
        boolean is = false;
        for (EntityMappingRow row : mappings) {
            if (row.isPrimaryKey()
                    || (includeVersion && NamingUtil.isVersionNo(row
                            .getSqlColumnName()))) {
                stb.append(row.getJavaFieldName());
                stb.append(',');
                stb.append(' ');
                is = true;
            }
        }
        if (is) {
            stb.setLength(stb.length() - 2);
        }
        return stb.toString();
    }

    public String createPkeyMethodCallArgsCopy() {
        return createPkeyMethodCallArgsCopy(false);
    }

    public String createPkeyLink(boolean includeVersion) {
        StringBuffer stb = new StringBuffer();
        for (EntityMappingRow row : mappings) {
            if (row.isPrimaryKey()
                    || (includeVersion && NamingUtil.isVersionNo(row
                            .getSqlColumnName()))) {
                stb.append("&amp;");
                stb.append(row.getJavaFieldName());
                stb.append('=');
                stb.append(row.getJavaFieldName());
            }
        }
        return stb.toString();
    }

    public String createPkeyLink() {
        return createPkeyLink(false);
    }

    public boolean isTigerResource() {
        return ProjectUtil.enableAnnotation(this.project);
    }

    public String toAsType(EntityMappingRow row) {
        String result = "Object";
        try {
            String s = row.getJavaClassName();
            s = DoltengCore.getAsTypeResolver(this.project).resolve(s);
            if (StringUtil.isEmpty(s) == false) {
                result = s;
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return result;
    }

    public boolean isVersionColumn(EntityMappingRow row) {
        return NamingUtil.isVersionNo(row.getSqlColumnName());
    }
}
