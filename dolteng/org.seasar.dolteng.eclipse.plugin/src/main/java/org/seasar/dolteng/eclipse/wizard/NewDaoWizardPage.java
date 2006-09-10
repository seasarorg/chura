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
package org.seasar.dolteng.eclipse.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.wizards.NewInterfaceWizardPage;
import org.seasar.dolteng.eclipse.model.EntityMappingRow;
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

    protected void createTypeMembers(IType type, ImportsManager imports,
            IProgressMonitor monitor) throws CoreException {
        String beanTypeName = imports.addImport(this.entityWizardPage
                .getCreatedType().getFullyQualifiedName());

        // BEAN アノテーション
        StringBuffer stb = new StringBuffer();
        stb.append("public static final ");
        stb.append(imports.addImport("java.lang.Class"));
        stb.append(" BEAN = ");
        stb.append(beanTypeName);
        stb.append(".class");
        stb.append(";");
        stb.append(LINE_DELIM);
        type.createField(stb.toString(), null, false, monitor);

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

        stb = new StringBuffer();
        stb.append("public static final ");
        stb.append(imports.addImport("java.lang.String"));
        stb.append(' ');
        stb.append(methodName);
        stb.append("_ARGS = \"");
        for (int i = 0; i < paramNames.length; i++) {
            stb.append(paramNames[i]);
            stb.append(", ");
        }
        stb.setLength(stb.length() - 2);
        stb.append("\";");
        stb.append(LINE_DELIM);
        type.createField(stb.toString(), null, false, monitor);

    }

    protected String[] getPKClassNames(ImportsManager imports) {
        List results = new ArrayList();
        List rows = this.mappingPage.getMappingRows();
        for (final Iterator i = rows.iterator(); i.hasNext();) {
            EntityMappingRow row = (EntityMappingRow) i.next();
            if (row.isPrimaryKey()) {
                results.add(imports.addImport(row.getJavaClassName()));
            }

        }

        return (String[]) results.toArray(new String[results.size()]);
    }

    protected String[] getParameterNames() {
        List results = new ArrayList();
        List rows = this.mappingPage.getMappingRows();
        for (final Iterator i = rows.iterator(); i.hasNext();) {
            EntityMappingRow row = (EntityMappingRow) i.next();
            if (row.isPrimaryKey()) {
                results.add(row.getJavaFieldName());
            }
        }
        return (String[]) results.toArray(new String[results.size()]);
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