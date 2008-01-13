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
package org.seasar.dolteng.eclipse.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.wizards.NewInterfaceWizardPage;
import org.seasar.dolteng.eclipse.ast.ImportsStructure;
import org.seasar.dolteng.eclipse.model.EntityMappingRow;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.TypeUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class NewDaoWizardPage extends NewInterfaceWizardPage {

    private static final String LINE_DELIM = System
            .getProperty("line.separator");

    private NewEntityWizardPage entityWizardPage;

    private EntityMappingPage mappingPage;

    public NewDaoWizardPage(NewEntityWizardPage entityWizardPage,
            EntityMappingPage mappingPage) {
        this.entityWizardPage = entityWizardPage;
        this.mappingPage = mappingPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.ui.wizards.NewTypeWizardPage#createType(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void createType(IProgressMonitor monitor) throws CoreException,
            InterruptedException {
        super.createType(monitor);
        IType created = getCreatedType();
        if (ProjectUtil.enableAnnotation(created.getJavaProject())) {
            final ICompilationUnit unit = created.getCompilationUnit();
            TypeUtil.modifyType(unit, monitor,
                    new TypeUtil.ModifyTypeHandler() {
                        public void modify(final ASTNode node,
                                final ASTRewrite rewrite,
                                final ImportsStructure imports) {
                            node.accept(new ASTVisitor() {
                                @Override
                                public void endVisit(TypeDeclaration node) {
                                    ListRewrite lr = rewrite
                                            .getListRewrite(
                                                    node,
                                                    TypeDeclaration.MODIFIERS2_PROPERTY);
                                    for (Iterator i = node.modifiers()
                                            .iterator(); i.hasNext();) {
                                        IExtendedModifier em = (IExtendedModifier) i
                                                .next();
                                        if (em.isModifier()) {
                                            String beanTypeName = imports
                                                    .addImport(entityWizardPage
                                                            .getCreatedType()
                                                            .getFullyQualifiedName());
                                            ASTNode entity = rewrite
                                                    .createStringPlaceholder(
                                                            '@'
                                                                    + imports
                                                                            .addImport("org.seasar.dao.annotation.tiger.S2Dao")
                                                                    + "(bean="
                                                                    + beanTypeName
                                                                    + ".class)",
                                                            ASTNode.NORMAL_ANNOTATION);
                                            lr.insertBefore(entity,
                                                    (Modifier) em, null);

                                            break;
                                        }
                                    }
                                }
                            });
                        }
                    });
        }
    }

    @Override
    protected void createTypeMembers(IType type, ImportsManager imports,
            IProgressMonitor monitor) throws CoreException {
        String beanTypeName = imports.addImport(this.entityWizardPage
                .getCreatedType().getFullyQualifiedName());

        // BEAN アノテーション
        if (ProjectUtil.enableAnnotation(type.getJavaProject()) == false) {
            StringBuffer stb = new StringBuffer();
            stb.append("public static final ");
            stb.append(imports.addImport("java.lang.Class"));
            stb.append(" BEAN = ");
            stb.append(beanTypeName);
            stb.append(".class");
            stb.append(";");
            stb.append(LINE_DELIM);
            type.createField(stb.toString(), null, false, monitor);
        }

        createSelectAll(type, beanTypeName, new SubProgressMonitor(monitor, 1));
        createSelectById(type, imports, beanTypeName, new SubProgressMonitor(
                monitor, 1));

        createMethod(type, beanTypeName, "insert", new SubProgressMonitor(
                monitor, 1));
        createMethod(type, beanTypeName, "update", new SubProgressMonitor(
                monitor, 1));
        createMethod(type, beanTypeName, "delete", new SubProgressMonitor(
                monitor, 1));
    }

    protected void createSelectAll(IType type, String beanTypeName,
            IProgressMonitor monitor) throws CoreException {
        StringBuffer stb = new StringBuffer();
        String methodName = "selectAll";
        String retType = beanTypeName + "[]";
        if (isAddComments()) {
            String comment = CodeGeneration.getMethodComment(type
                    .getCompilationUnit(), type.getFullyQualifiedName(),
                    methodName, StringUtil.EMPTY_STRINGS,
                    StringUtil.EMPTY_STRINGS, Signature.createTypeSignature(
                            retType, true), null, LINE_DELIM);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(LINE_DELIM);
            }
        }

        stb.append("public ");
        stb.append(retType);
        stb.append(' ');
        stb.append(methodName);
        stb.append("();");
        stb.append(LINE_DELIM);

        type.createMethod(stb.toString(), null, true, monitor);
    }

    protected void createSelectById(IType type, ImportsManager imports,
            String beanTypeName, IProgressMonitor monitor) throws CoreException {
        StringBuffer stb = new StringBuffer();
        String methodName = "selectById";
        String[] paramTypes = getPKClassNames(imports);
        String[] paramNames = getParameterNames();
        String[] columnNames = getParameterColumns();

        if (paramTypes.length < 1 || paramNames.length < 1) {
            return;
        }

        if (isAddComments()) {
            String comment = CodeGeneration.getMethodComment(type
                    .getCompilationUnit(), type.getFullyQualifiedName(),
                    methodName, paramNames, StringUtil.EMPTY_STRINGS, Signature
                            .createTypeSignature(beanTypeName, true), null,
                    LINE_DELIM);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(LINE_DELIM);
            }
        }
        if (ProjectUtil.enableAnnotation(type.getJavaProject())) {
            String anon = imports
                    .addImport("org.seasar.dao.annotation.tiger.Arguments");
            stb.append('@');
            stb.append(anon);
            stb.append('(');

            boolean isMultiple = 1 < columnNames.length;
            if (isMultiple) {
                stb.append('{');
            }
            for (String columnName : columnNames) {
                stb.append("\"");
                stb.append(columnName);
                stb.append("\"");
                stb.append(", ");
            }
            stb.setLength(stb.length() - 2);
            if (isMultiple) {
                stb.append('}');
            }
            stb.append(')');
            stb.append(LINE_DELIM);
        }
        stb.append("public ");
        stb.append(beanTypeName);
        stb.append(' ');
        stb.append(methodName);
        stb.append('(');
        for (int i = 0; i < paramTypes.length; i++) {
            stb.append(paramTypes[i]);
            stb.append(' ');
            stb.append(paramNames[i]);
            stb.append(", ");
        }
        stb.setLength(stb.length() - 2);
        stb.append(");");
        stb.append(LINE_DELIM);

        type.createMethod(stb.toString(), null, true, monitor);

        if (ProjectUtil.enableAnnotation(type.getJavaProject()) == false) {
            stb = new StringBuffer();
            stb.append("public static final ");
            stb.append(imports.addImport("java.lang.String"));
            stb.append(' ');
            stb.append(methodName);
            stb.append("_ARGS = \"");
            for (String columnName : columnNames) {
                stb.append(columnName);
                stb.append(", ");
            }
            stb.setLength(stb.length() - 2);
            stb.append("\";");
            stb.append(LINE_DELIM);
            type.createField(stb.toString(), null, false, monitor);
        }
    }

    protected String[] getPKClassNames(ImportsManager imports) {
        List<String> results = new ArrayList<String>();
        List rows = this.mappingPage.getMappingRows();
        for (final Iterator i = rows.iterator(); i.hasNext();) {
            EntityMappingRow row = (EntityMappingRow) i.next();
            if (row.isPrimaryKey()) {
                results.add(imports.addImport(row.getJavaClassName()));
            }

        }

        return results.toArray(new String[results.size()]);
    }

    protected String[] getParameterNames() {
        List<String> results = new ArrayList<String>();
        List rows = this.mappingPage.getMappingRows();
        for (final Iterator i = rows.iterator(); i.hasNext();) {
            EntityMappingRow row = (EntityMappingRow) i.next();
            if (row.isPrimaryKey()) {
                results.add(row.getJavaFieldName());
            }
        }
        return results.toArray(new String[results.size()]);
    }

    protected String[] getParameterColumns() {
        List<String> results = new ArrayList<String>();
        List rows = this.mappingPage.getMappingRows();
        for (final Iterator i = rows.iterator(); i.hasNext();) {
            EntityMappingRow row = (EntityMappingRow) i.next();
            if (row.isPrimaryKey()) {
                results.add(row.getSqlColumnName());
            }
        }
        return results.toArray(new String[results.size()]);
    }

    protected void createMethod(IType type, String beanTypeName,
            String methodName, IProgressMonitor monitor) throws CoreException {
        StringBuffer stb = new StringBuffer();

        String[] argNames = NamingConventions.suggestArgumentNames(type
                .getJavaProject(), type.getPackageFragment().getElementName(),
                beanTypeName, 0, StringUtil.EMPTY_STRINGS);
        String arg = "";
        if (argNames != null && 0 < argNames.length) {
            arg = argNames[0];
        } else {
            arg = beanTypeName.toLowerCase();
        }

        if (isAddComments()) {
            String comment = CodeGeneration.getMethodComment(type
                    .getCompilationUnit(), type.getFullyQualifiedName(),
                    methodName, new String[] { arg }, StringUtil.EMPTY_STRINGS,
                    Signature.createTypeSignature("int", true), null,
                    LINE_DELIM);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(LINE_DELIM);
            }
        }

        stb.append("public int ");
        stb.append(methodName);
        stb.append('(');
        stb.append(beanTypeName);
        stb.append(' ');
        stb.append(arg);
        stb.append(')');
        stb.append(';');

        type.createMethod(stb.toString(), null, true, monitor);
    }

}
