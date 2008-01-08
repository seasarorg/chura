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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class NewEJB3ServicePage extends NewClassWizardPage {

    private ICompilationUnit injectionTarget;

    public NewEJB3ServicePage(ICompilationUnit injectionTarget) {
        super();
        this.injectionTarget = injectionTarget;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.ui.wizards.NewTypeWizardPage#constructCUContent(org.eclipse.jdt.core.ICompilationUnit,
     *      java.lang.String, java.lang.String)
     */
    @Override
    protected String constructCUContent(ICompilationUnit cu,
            String typeContent, String lineDelimiter) throws CoreException {
        StringBuffer stb = new StringBuffer();
        stb.append("@Stateless");
        stb.append(lineDelimiter);
        stb.append(typeContent);
        typeContent = stb.toString();
        return super.constructCUContent(cu, typeContent, lineDelimiter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.ui.wizards.NewClassWizardPage#createTypeMembers(org.eclipse.jdt.core.IType,
     *      org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void createTypeMembers(IType type, ImportsManager imports,
            IProgressMonitor monitor) throws CoreException {
        imports.addImport("javax.ejb.Stateless");

        String lineDelimiter = ProjectUtil.getProjectLineDelimiter(type
                .getJavaProject());

        IType target = this.injectionTarget.findPrimaryType();
        String dxoName = NewServiceWizard.toDxoName(target);
        dxoName = dxoName + ".java";
        IContainer container = this.injectionTarget.getResource().getParent();
        IResource dxoRsc = container.findMember(dxoName);
        if (dxoRsc == null || dxoRsc.exists() == false) {
            NamingConvention nc = DoltengCore.getPreferences(
                    type.getJavaProject()).getNamingConvention();

            dxoName = StringUtil.capitalize(container.getName())
                    + nc.getDxoSuffix() + ".java";
            dxoRsc = container.findMember(dxoName);
        }
        if (dxoRsc != null && dxoRsc.exists()) {
            ICompilationUnit dxoUnit = (ICompilationUnit) JavaCore
                    .create(dxoRsc);
            if (dxoUnit != null && dxoUnit.exists()) {
                IType dxoType = dxoUnit.findPrimaryType();
                IField field = createField(type, imports, dxoType, monitor,
                        lineDelimiter);
                createGetter(type, imports, field, new SubProgressMonitor(
                        monitor, 1), lineDelimiter);
                createSetter(type, imports, field, new SubProgressMonitor(
                        monitor, 1), lineDelimiter);
            }
        }

        super.createTypeMembers(type, imports, monitor);
    }

    protected IField createField(IType type, ImportsManager imports,
            IType field, IProgressMonitor monitor, String lineDelimiter)
            throws CoreException {

        String fieldName = StringUtil.decapitalize(field.getElementName());
        StringBuffer stb = new StringBuffer();
        if (isAddComments()) {
            String comment = CodeGeneration.getFieldComment(type
                    .getCompilationUnit(), type.getElementName(), fieldName,
                    lineDelimiter);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(lineDelimiter);
            }
        }
        stb.append("@");
        stb.append(imports.addImport("javax.annotation.Resource"));
        stb.append(" ");
        stb.append(lineDelimiter);
        stb.append("private ");
        stb.append(imports.addImport(field.getFullyQualifiedName()));
        stb.append(' ');
        stb.append(fieldName);
        stb.append(';');
        stb.append(lineDelimiter);

        return type.createField(stb.toString(), null, false, monitor);
    }

    protected void createGetter(IType type, ImportsManager imports,
            IField field, IProgressMonitor monitor, String lineDelimiter)
            throws CoreException {
        String fieldName = field.getElementName();
        IType parentType = field.getDeclaringType();
        String getterName = NamingConventions.suggestGetterName(type
                .getJavaProject(), fieldName, field.getFlags(), field
                .getTypeSignature().equals(Signature.SIG_BOOLEAN),
                StringUtil.EMPTY_STRINGS);

        String typeName = Signature.toString(field.getTypeSignature());
        String accessorName = NamingConventions
                .removePrefixAndSuffixForFieldName(field.getJavaProject(),
                        fieldName, field.getFlags());

        StringBuffer stb = new StringBuffer();
        if (isAddComments()) {
            String comment = CodeGeneration.getGetterComment(field
                    .getCompilationUnit(),
                    parentType.getTypeQualifiedName('.'), getterName, field
                            .getElementName(), typeName, accessorName,
                    lineDelimiter);
            if (comment != null) {
                stb.append(comment);
                stb.append(lineDelimiter);
            }
        }

        stb.append("public");
        stb.append(' ');
        stb.append(imports.addImport(typeName));
        stb.append(' ');
        stb.append(getterName);
        stb.append("() {");
        stb.append(lineDelimiter);

        if (useThisForFieldAccess(field)) {
            fieldName = "this." + fieldName;
        }

        String body = CodeGeneration.getGetterMethodBodyContent(field
                .getCompilationUnit(), parentType.getTypeQualifiedName('.'),
                getterName, fieldName, lineDelimiter);
        if (body != null) {
            stb.append(body);
        }
        stb.append(lineDelimiter);
        stb.append("}");
        stb.append(lineDelimiter);
        type.createMethod(stb.toString(), null, false, monitor);
    }

    private static boolean useThisForFieldAccess(IField field) {
        boolean useThis = Boolean.valueOf(
                PreferenceConstants.getPreference(
                        PreferenceConstants.CODEGEN_KEYWORD_THIS, field
                                .getJavaProject())).booleanValue();
        return useThis;
    }

    protected void createSetter(IType type, ImportsManager imports,
            IField field, IProgressMonitor monitor, String lineDelimiter)
            throws CoreException {
        String fieldName = field.getElementName();
        IType parentType = field.getDeclaringType();
        String setterName = NamingConventions.suggestSetterName(type
                .getJavaProject(), fieldName, field.getFlags(), field
                .getTypeSignature().equals(Signature.SIG_BOOLEAN),
                StringUtil.EMPTY_STRINGS);

        String returnSig = field.getTypeSignature();
        String typeName = Signature.toString(returnSig);

        IJavaProject project = field.getJavaProject();

        String accessorName = NamingConventions
                .removePrefixAndSuffixForFieldName(project, fieldName, field
                        .getFlags());
        String argname = accessorName;

        StringBuffer stb = new StringBuffer();
        if (isAddComments()) {
            String comment = CodeGeneration.getSetterComment(field
                    .getCompilationUnit(),
                    parentType.getTypeQualifiedName('.'), setterName, field
                            .getElementName(), typeName, argname, accessorName,
                    lineDelimiter);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(lineDelimiter);
            }
        }

        stb.append("public void ");
        stb.append(setterName);
        stb.append('(');
        stb.append(imports.addImport(typeName));
        stb.append(' ');
        stb.append(argname);
        stb.append(") {");
        stb.append(lineDelimiter);

        boolean useThis = useThisForFieldAccess(field);
        if (argname.equals(fieldName) || useThis) {
            fieldName = "this." + fieldName;
        }
        String body = CodeGeneration.getSetterMethodBodyContent(field
                .getCompilationUnit(), parentType.getTypeQualifiedName('.'),
                setterName, fieldName, argname, lineDelimiter);
        if (body != null) {
            stb.append(body);
        }
        stb.append(lineDelimiter);
        stb.append("}");
        stb.append(lineDelimiter);
        type.createMethod(stb.toString(), null, false, monitor);
    }
}
