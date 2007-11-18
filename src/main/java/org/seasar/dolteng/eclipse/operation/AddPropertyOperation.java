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
package org.seasar.dolteng.eclipse.operation;

import java.lang.reflect.Modifier;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class AddPropertyOperation implements IWorkspaceRunnable {

    private final ICompilationUnit unit;

    private String fieldPkgName = "";

    private String fieldFQName = "";

    private String fieldName = "";
    
    private boolean usePublicField = false;

    private IMethod newGetter;

    private IMethod newSetter;

    public AddPropertyOperation(final ICompilationUnit unit, final String typeFQName,
            final String fieldName, final boolean usePublicField) {
        this.unit = unit;
        this.fieldFQName = typeFQName;
        this.fieldName = fieldName;
        this.usePublicField = usePublicField;

    }

    public AddPropertyOperation(final ICompilationUnit unit, final IType fieldType,
            final String fieldName, final boolean usePublicField) {
        this(unit, fieldType.getFullyQualifiedName(), fieldName, usePublicField);
        this.fieldPkgName = fieldType.getPackageFragment().getElementName();
    }

    public AddPropertyOperation(final ICompilationUnit unit, final IType fieldType, final boolean usePublicField) {
        this(unit, fieldType, "", usePublicField);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(final IProgressMonitor monitor) throws CoreException {
        final IType type = unit.findPrimaryType();
        final IJavaElement[] elements = type.getChildren();
        IJavaElement sibling = null;
        final String fieldName = calculateFieldName();
        for (int i = 0; i < elements.length; i++) {
            final IJavaElement elem = elements[i];
            if (IJavaElement.FIELD == elem.getElementType()) {
                final int j = i + 1;
                if (j < elements.length) {
                    sibling = elements[j];
                }
                if (elem.getElementName().equals(fieldName)) {
                    return;
                }
            }
        }
        final String lineDelimiter = ProjectUtil.getLineDelimiterPreference(unit
                .getJavaProject().getProject());
        if (StringUtil.isEmpty(fieldPkgName) == false
                && type.getPackageFragment().getElementName().equals(
                        fieldPkgName) == false) {
            unit.createImport(fieldFQName, null, monitor);
        }
        final IField field = createField(type, monitor, sibling, fieldName,
                lineDelimiter);
        if(! usePublicField) {
            this.newGetter = createGetter(type, field, monitor, lineDelimiter);
            this.newSetter = createSetter(type, field, monitor, lineDelimiter);
        }
    }

    private IField createField(final IType type, final IProgressMonitor monitor,
            final IJavaElement sibling, final String fieldName, final String lineDelimiter)
            throws CoreException {
        final StringBuffer stb = new StringBuffer();

        final String comment = CodeGeneration.getFieldComment(unit, fieldFQName,
                fieldName, lineDelimiter);
        if (StringUtil.isEmpty(comment) == false) {
            stb.append(comment);
            stb.append(lineDelimiter);
        }
        stb.append(usePublicField ? "public " : "private ");
        stb.append(calculateFieldType(
                ClassUtil.getShortClassName(fieldFQName)));
        stb.append(' ');
        stb.append(fieldName);
        stb.append(';');
        stb.append(lineDelimiter);

        return type.createField(stb.toString(), sibling, true, monitor);
    }

    protected String calculateFieldType(final String typeName) {
        return typeName;
    }

    /**
     * @return
     */
    private String calculateFieldName() {
        if (StringUtil.isEmpty(this.fieldName)) {
            final String[] names = NamingConventions.suggestFieldNames(unit
                    .getJavaProject(), fieldPkgName, fieldFQName, 0,
                    Modifier.PRIVATE, StringUtil.EMPTY_STRINGS);
            fieldName = StringUtil.decapitalize(ClassUtil
                    .getShortClassName(fieldFQName));
            if (names != null && 0 < names.length) {
                fieldName = names[names.length - 1];
            }
        }
        return fieldName;
    }

    protected IMethod createGetter(final IType type, final IField field,
            final IProgressMonitor monitor, final String lineDelimiter)
            throws CoreException {
        String fieldName = field.getElementName();
        final IType parentType = field.getDeclaringType();
        final String getterName = NamingConventions.suggestGetterName(type
                .getJavaProject(), fieldName, field.getFlags(), field
                .getTypeSignature().equals(Signature.SIG_BOOLEAN),
                StringUtil.EMPTY_STRINGS);

        final String typeName = calculateFieldType(ClassUtil
                .getShortClassName(fieldFQName));
        final String accessorName = NamingConventions
                .removePrefixAndSuffixForFieldName(field.getJavaProject(),
                        fieldName, field.getFlags());

        final StringBuffer stb = new StringBuffer();
        final String comment = CodeGeneration.getGetterComment(field
                .getCompilationUnit(), parentType.getTypeQualifiedName('.'),
                getterName, field.getElementName(), typeName, accessorName,
                lineDelimiter);
        if (comment != null) {
            stb.append(comment);
            stb.append(lineDelimiter);
        }

        stb.append("public");
        stb.append(' ');
        stb.append(typeName);
        stb.append(' ');
        stb.append(getterName);
        stb.append("() {");
        stb.append(lineDelimiter);

        if (useThisForFieldAccess(field)) {
            fieldName = "this." + fieldName;
        }

        stb.append(ProjectUtil.createIndentString(1, unit.getJavaProject()));

        final String body = CodeGeneration.getGetterMethodBodyContent(field
                .getCompilationUnit(), parentType.getTypeQualifiedName('.'),
                getterName, fieldName, lineDelimiter);
        if (body != null) {
            stb.append(body);
        }
        stb.append(lineDelimiter);
        stb.append("}");
        stb.append(lineDelimiter);
        return type.createMethod(stb.toString(), null, false, monitor);
    }

    protected IMethod createSetter(final IType type, final IField field,
            final IProgressMonitor monitor, final String lineDelimiter)
            throws CoreException {
        String fieldName = field.getElementName();
        final IType parentType = field.getDeclaringType();
        final String setterName = NamingConventions.suggestSetterName(type
                .getJavaProject(), fieldName, field.getFlags(), field
                .getTypeSignature().equals(Signature.SIG_BOOLEAN),
                StringUtil.EMPTY_STRINGS);

        final String typeName = calculateFieldType(ClassUtil
                .getShortClassName(fieldFQName));

        final IJavaProject project = field.getJavaProject();

        final String accessorName = NamingConventions
                .removePrefixAndSuffixForFieldName(project, fieldName, field
                        .getFlags());
        final String argname = accessorName;

        final StringBuffer stb = new StringBuffer();
        final String comment = CodeGeneration.getSetterComment(field
                .getCompilationUnit(), parentType.getTypeQualifiedName('.'),
                setterName, field.getElementName(), typeName, argname,
                accessorName, lineDelimiter);
        if (StringUtil.isEmpty(comment) == false) {
            stb.append(comment);
            stb.append(lineDelimiter);
        }

        stb.append("public");
        stb.append(" void ");
        stb.append(setterName);
        stb.append('(');
        stb.append(typeName);
        stb.append(' ');
        stb.append(argname);
        stb.append(") {");
        stb.append(lineDelimiter);

        stb.append(ProjectUtil.createIndentString(1, unit.getJavaProject()));

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
        return type.createMethod(stb.toString(), null, false, monitor);
    }

    /**
     * @param field
     * @return
     */
    private boolean useThisForFieldAccess(final IField field) {
        final boolean useThis = Boolean.valueOf(
                PreferenceConstants.getPreference(
                        PreferenceConstants.CODEGEN_KEYWORD_THIS, field
                                .getJavaProject())).booleanValue();
        return useThis;
    }

    /**
     * @return Returns the newGetter.
     */
    public IMethod getNewGetter() {
        return newGetter;
    }

    /**
     * @return Returns the newSetter.
     */
    public IMethod getNewSetter() {
        return newSetter;
    }
}