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

    private ICompilationUnit unit;

    private String fieldPkgName = "";

    private String fieldFQName = "";

    private String fieldName = "";

    private boolean usePublicField = false;

    private IMethod newGetter;

    private IMethod newSetter;

    public AddPropertyOperation(ICompilationUnit unit, String typeFQName,
            String fieldName, boolean usePublicField) {
        this.unit = unit;
        this.fieldFQName = typeFQName;
        this.fieldName = fieldName;
        this.usePublicField = usePublicField;

    }

    public AddPropertyOperation(ICompilationUnit unit, IType fieldType,
            String fieldName, boolean usePublicField) {
        this(unit, fieldType.getFullyQualifiedName(), fieldName, usePublicField);
        this.fieldPkgName = fieldType.getPackageFragment().getElementName();
    }

    public AddPropertyOperation(ICompilationUnit unit, IType fieldType,
            boolean usePublicField) {
        this(unit, fieldType, "", usePublicField);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws CoreException {
        IType type = unit.findPrimaryType();
        IJavaElement[] elements = type.getChildren();
        IJavaElement sibling = null;
        String fieldName = calculateFieldName();
        for (int i = 0; i < elements.length; i++) {
            IJavaElement elem = elements[i];
            if (IJavaElement.FIELD == elem.getElementType()) {
                int j = i + 1;
                if (j < elements.length) {
                    sibling = elements[j];
                }
                if (elem.getElementName().equals(fieldName)) {
                    return;
                }
            }
        }
        String lineDelimiter = ProjectUtil.getLineDelimiterPreference(unit
                .getJavaProject().getProject());
        if (StringUtil.isEmpty(fieldPkgName) == false
                && type.getPackageFragment().getElementName().equals(
                        fieldPkgName) == false) {
            unit.createImport(fieldFQName, null, monitor);
        }
        IField field = createField(type, monitor, sibling, fieldName,
                lineDelimiter);
        if (!usePublicField) {
            this.newGetter = createGetter(type, field, monitor, lineDelimiter);
            this.newSetter = createSetter(type, field, monitor, lineDelimiter);
        }
    }

    private IField createField(IType type, IProgressMonitor monitor,
            IJavaElement sibling, String fieldName, String lineDelimiter)
            throws CoreException {
        StringBuffer stb = new StringBuffer();

        String comment = CodeGeneration.getFieldComment(unit, fieldFQName,
                fieldName, lineDelimiter);
        if (StringUtil.isEmpty(comment) == false) {
            stb.append(comment);
            stb.append(lineDelimiter);
        }
        stb.append(usePublicField ? "public " : "private ");
        stb
                .append(calculateFieldType(ClassUtil
                        .getShortClassName(fieldFQName)));
        stb.append(' ');
        stb.append(fieldName);
        stb.append(';');
        stb.append(lineDelimiter);

        return type.createField(stb.toString(), sibling, true, monitor);
    }

    protected String calculateFieldType(String typeName) {
        return typeName;
    }

    private String calculateFieldName() {
        if (StringUtil.isEmpty(this.fieldName)) {
            String[] names = NamingConventions.suggestFieldNames(unit
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

    protected IMethod createGetter(IType type, IField field,
            IProgressMonitor monitor, String lineDelimiter)
            throws CoreException {
        String fieldName = field.getElementName();
        IType parentType = field.getDeclaringType();
        String getterName = NamingConventions.suggestGetterName(type
                .getJavaProject(), fieldName, field.getFlags(), field
                .getTypeSignature().equals(Signature.SIG_BOOLEAN),
                StringUtil.EMPTY_STRINGS);

        String typeName = calculateFieldType(ClassUtil
                .getShortClassName(fieldFQName));
        String accessorName = NamingConventions
                .removePrefixAndSuffixForFieldName(field.getJavaProject(),
                        fieldName, field.getFlags());

        StringBuffer stb = new StringBuffer();
        String comment = CodeGeneration.getGetterComment(field
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

        String body = CodeGeneration.getGetterMethodBodyContent(field
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

    protected IMethod createSetter(IType type, IField field,
            IProgressMonitor monitor, String lineDelimiter)
            throws CoreException {
        String fieldName = field.getElementName();
        IType parentType = field.getDeclaringType();
        String setterName = NamingConventions.suggestSetterName(type
                .getJavaProject(), fieldName, field.getFlags(), field
                .getTypeSignature().equals(Signature.SIG_BOOLEAN),
                StringUtil.EMPTY_STRINGS);

        String typeName = calculateFieldType(ClassUtil
                .getShortClassName(fieldFQName));

        IJavaProject project = field.getJavaProject();

        String accessorName = NamingConventions
                .removePrefixAndSuffixForFieldName(project, fieldName, field
                        .getFlags());
        String argname = accessorName;

        StringBuffer stb = new StringBuffer();
        String comment = CodeGeneration.getSetterComment(field
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
        return type.createMethod(stb.toString(), null, false, monitor);
    }

    private boolean useThisForFieldAccess(IField field) {
        boolean useThis = Boolean.valueOf(
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