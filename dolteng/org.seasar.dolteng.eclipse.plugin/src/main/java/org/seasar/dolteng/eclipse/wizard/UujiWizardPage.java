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
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class UujiWizardPage extends NewInterfaceWizardPage {

    private EntityMappingPage mappingPage;

    public UujiWizardPage(EntityMappingPage mappingPage) {
        this.mappingPage = mappingPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.ui.wizards.NewInterfaceWizardPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            List<String> l = new ArrayList<String>();
            l.add("org.seasar.uuji.GenericDao");
            setSuperInterfaces(l, false);
        }
        super.setVisible(visible);
    }

    @Override
    protected void createTypeMembers(IType type, ImportsManager imports,
            IProgressMonitor monitor) throws CoreException {
        String lineDelimiter = ProjectUtil.getProjectLineDelimiter(type
                .getJavaProject());

        createFind(type, imports, new SubProgressMonitor(monitor, 1),
                lineDelimiter);

    }

    protected void createFind(IType type, ImportsManager imports,
            IProgressMonitor monitor, String lineDelimiter)
            throws CoreException {
        StringBuffer stb = new StringBuffer();
        String methodName = "find";
        String[] paramTypes = getPKClassNames(imports);
        String[] paramNames = getParameterNames();

        if (paramTypes.length < 2 || paramNames.length < 2) {
            return;
        }

        String beanTypeName = imports.addImport("java.util.Map");

        if (isAddComments()) {
            String comment = CodeGeneration.getMethodComment(type
                    .getCompilationUnit(), type.getFullyQualifiedName(),
                    methodName, paramNames, StringUtil.EMPTY_STRINGS, Signature
                            .createTypeSignature(beanTypeName, true), null,
                    lineDelimiter);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(lineDelimiter);
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
        stb.append(lineDelimiter);

        type.createMethod(stb.toString(), null, true, monitor);
    }

    protected String[] getPKClassNames(ImportsManager imports) {
        List<String> results = new ArrayList<String>();
        List<EntityMappingRow> rows = this.mappingPage.getMappingRows();
        for (EntityMappingRow row : rows) {
            if (row.isPrimaryKey()) {
                results.add(imports.addImport(row.getJavaClassName()));
            }

        }

        return results.toArray(new String[results.size()]);
    }

    protected String[] getParameterNames() {
        List<String> results = new ArrayList<String>();
        List<EntityMappingRow> rows = this.mappingPage.getMappingRows();
        for (EntityMappingRow row : rows) {
            if (row.isPrimaryKey()) {
                results.add(row.getJavaFieldName());
            }
        }
        return results.toArray(new String[results.size()]);
    }

    protected void createMethod(IType type, String beanTypeName,
            String retType, String methodName, IProgressMonitor monitor,
            String lineDelimiter) throws CoreException {
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
                    lineDelimiter);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(lineDelimiter);
            }
        }

        stb.append("public ");
        stb.append(retType);
        stb.append(' ');
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
