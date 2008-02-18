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

import java.lang.reflect.Modifier;
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
import org.seasar.dolteng.eclipse.model.EntityMappingRow;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class NewQueryDtoWizardPage extends NewClassWizardPage {

    private QueryDtoMappingPage mappingPage;

    /**
     * 
     */
    public NewQueryDtoWizardPage(QueryDtoMappingPage mappingPage) {
        super();
        this.mappingPage = mappingPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.ui.wizards.NewTypeWizardPage#createType(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void createTypeMembers(IType type, ImportsManager imports,
            IProgressMonitor monitor) throws CoreException {

        String lineDelimiter = ProjectUtil.getProjectLineDelimiter(type
                .getJavaProject());

        List<EntityMappingRow> rows = mappingPage.getMappingRows();
        for (EntityMappingRow meta : rows) {
            if (meta.isGenerate()) {
                boolean isPublic = mappingPage.getUsePublicField();
                IField field = createField(type, imports, meta,
                        new SubProgressMonitor(monitor, 1), lineDelimiter,
                        isPublic);
                if (isPublic == false) {
                    createGetter(type, imports, meta, field,
                            new SubProgressMonitor(monitor, 1), lineDelimiter);
                    createSetter(type, imports, meta, field,
                            new SubProgressMonitor(monitor, 1), lineDelimiter);
                }
            }
        }

        super.createTypeMembers(type, imports, monitor);
    }

    protected IField createField(IType type, ImportsManager imports,
            EntityMappingRow meta, IProgressMonitor monitor,
            String lineDelimiter, boolean isPublic) throws CoreException {

        String className = meta.getJavaClassName();

        StringBuffer stb = new StringBuffer();
        if (isAddComments()) {
            String comment = CodeGeneration.getFieldComment(type
                    .getCompilationUnit(), className, meta.getJavaFieldName(),
                    lineDelimiter);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(lineDelimiter);
            }
        }
        if (isPublic) {
            stb.append("public ");
        } else {
            stb.append("private ");
        }
        stb.append(imports.addImport(className));
        stb.append(' ');
        stb.append(meta.getJavaFieldName());
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
            EntityMappingRow meta, IField field, IProgressMonitor monitor,
            String lineDelimiter) throws CoreException {
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

        stb.append(Modifier.toString(meta.getJavaModifiers()));
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

    /**
     * @param field
     * @return
     */
    private static boolean useThisForFieldAccess(IField field) {
        boolean useThis = Boolean.valueOf(
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
            EntityMappingRow meta, IField field, IProgressMonitor monitor,
            String lineDelimiter) throws CoreException {
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

        stb.append(Modifier.toString(meta.getJavaModifiers()));
        stb.append(" void ");
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
