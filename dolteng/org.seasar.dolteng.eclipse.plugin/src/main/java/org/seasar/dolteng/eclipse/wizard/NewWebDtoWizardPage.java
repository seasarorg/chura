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

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.seasar.dolteng.eclipse.model.PageMappingRow;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class NewWebDtoWizardPage extends NewClassWizardPage {

    private final DtoMappingPage mappingPage;

    public NewWebDtoWizardPage(DtoMappingPage mappingPage) {
        super();
        this.mappingPage = mappingPage;
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

        final String lineDelimiter = ProjectUtil.getProjectLineDelimiter(type
                .getJavaProject());
        final List mappingRows = mappingPage.getMappingRows();
        for (final Iterator i = mappingRows.iterator(); i.hasNext();) {
            final PageMappingRow meta = (PageMappingRow) i.next();
            if (meta.isThisGenerate()) {
                final IField field = createField(type, imports, meta,
                        mappingPage.getUsePublicField(),
                        new SubProgressMonitor(monitor, 1), lineDelimiter);
                if (! mappingPage.getUsePublicField()) {
                    createGetter(type, imports, meta, field,
                            new SubProgressMonitor(monitor, 1), lineDelimiter);
                    createSetter(type, imports, meta, field,
                            new SubProgressMonitor(monitor, 1), lineDelimiter);
                }
            }
        }

        super.createTypeMembers(type, imports, monitor);
    }

    protected IField createField(final IType type, final ImportsManager imports,
            final PageMappingRow meta, final boolean usePublicField,
            final IProgressMonitor monitor, final String lineDelimiter)
            throws CoreException {

        final String pageFieldName = meta.getPageClassName();

        final StringBuffer stb = new StringBuffer();
        if (isAddComments()) {
            final String comment = CodeGeneration.getFieldComment(type
                    .getCompilationUnit(), pageFieldName, meta
                    .getPageFieldName(), lineDelimiter);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(lineDelimiter);
            }
        }
        stb.append(usePublicField ? "public " : "private ");
        stb.append(imports.addImport(pageFieldName));
        stb.append(' ');
        stb.append(meta.getPageFieldName());
        stb.append(';');
        stb.append(lineDelimiter);

        return type.createField(stb.toString(), null, false, monitor);
    }

    /**
     * @param type
     * @param imports
     * @param meta
     * @param field
     * @param monitor
     * @param lineDelimiter
     */
    protected void createGetter(IType type, ImportsManager imports,
            PageMappingRow meta, IField field, IProgressMonitor monitor,
            String lineDelimiter) throws CoreException {
        String fieldName = field.getElementName();
        final IType parentType = field.getDeclaringType();
        final String getterName = NamingConventions.suggestGetterName(type
                .getJavaProject(), fieldName, field.getFlags(), field
                .getTypeSignature().equals(Signature.SIG_BOOLEAN),
                StringUtil.EMPTY_STRINGS);

        final String typeName = Signature.toString(field.getTypeSignature());
        final String accessorName = NamingConventions
                .removePrefixAndSuffixForFieldName(field.getJavaProject(),
                        fieldName, field.getFlags());

        final StringBuffer stb = new StringBuffer();
        if (isAddComments()) {
            final String comment = CodeGeneration.getGetterComment(field
                    .getCompilationUnit(),
                    parentType.getTypeQualifiedName('.'), getterName, field
                            .getElementName(), typeName, accessorName,
                    lineDelimiter);
            if (comment != null) {
                stb.append(comment);
                stb.append(lineDelimiter);
            }
        }

        stb.append(Modifier.toString(meta.getPageModifiers()));
        stb.append(' ');
        stb.append(imports.addImport(typeName));
        stb.append(' ');
        stb.append(getterName);
        stb.append("() {");
        stb.append(lineDelimiter);

        if (useThisForFieldAccess(field)) {
            fieldName = "this." + fieldName;
        }

        final String body = CodeGeneration.getGetterMethodBodyContent(field
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

    /**
     * @param field
     * @return
     */
    private static boolean useThisForFieldAccess(IField field) {
        final boolean useThis = Boolean.valueOf(
                PreferenceConstants.getPreference(
                        PreferenceConstants.CODEGEN_KEYWORD_THIS, field
                                .getJavaProject())).booleanValue();
        return useThis;
    }

    /**
     * @param type
     * @param imports
     * @param meta
     * @param field
     * @param monitor
     * @param lineDelimiter
     */
    protected void createSetter(IType type, ImportsManager imports,
            PageMappingRow meta, IField field, IProgressMonitor monitor,
            String lineDelimiter) throws CoreException {
        String fieldName = field.getElementName();
        final IType parentType = field.getDeclaringType();
        final String setterName = NamingConventions.suggestSetterName(type
                .getJavaProject(), fieldName, field.getFlags(), field
                .getTypeSignature().equals(Signature.SIG_BOOLEAN),
                StringUtil.EMPTY_STRINGS);

        final String returnSig = field.getTypeSignature();
        final String typeName = Signature.toString(returnSig);

        final IJavaProject project = field.getJavaProject();

        final String accessorName = NamingConventions
                .removePrefixAndSuffixForFieldName(project, fieldName, field
                        .getFlags());
        final String argname = accessorName;

        final StringBuffer stb = new StringBuffer();
        if (isAddComments()) {
            final String comment = CodeGeneration.getSetterComment(field
                    .getCompilationUnit(),
                    parentType.getTypeQualifiedName('.'), setterName, field
                            .getElementName(), typeName, argname, accessorName,
                    lineDelimiter);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(lineDelimiter);
            }
        }

        stb.append(Modifier.toString(meta.getPageModifiers()));
        stb.append(" void ");
        stb.append(setterName);
        stb.append('(');
        stb.append(imports.addImport(typeName));
        stb.append(' ');
        stb.append(argname);
        stb.append(") {");
        stb.append(lineDelimiter);

        final boolean useThis = useThisForFieldAccess(field);
        if (argname.equals(fieldName) || useThis) {
            fieldName = "this." + fieldName;
        }
        final String body = CodeGeneration.getSetterMethodBodyContent(field
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
