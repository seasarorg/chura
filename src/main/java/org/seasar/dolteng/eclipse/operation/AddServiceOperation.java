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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class AddServiceOperation implements IWorkspaceRunnable {

    private ICompilationUnit unit;

    private IType fieldType;

    public AddServiceOperation(ICompilationUnit unit, IType type) {
        super();
        this.unit = unit;
        this.fieldType = type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws CoreException {
        IJavaElement[] elements = unit.getChildren();
        IJavaElement sibling = null;
        for (int i = 0; i < elements.length; i++) {
            IJavaElement elem = elements[i];
            if (IJavaElement.FIELD == elem.getElementType()) {
                int j = i + 1;
                if (j < elements.length) {
                    sibling = elements[j];
                }
            }
        }
        String lineDelimiter = ProjectUtil.getLineDelimiterPreference(unit
                .getJavaProject().getProject());
        IType type = unit.findPrimaryType();
        IField field = createField(type, monitor, sibling, lineDelimiter);
        createGetter(type, field, monitor, lineDelimiter);
        createSetter(type, field, monitor, lineDelimiter);
    }

    private IField createField(IType type, IProgressMonitor monitor,
            IJavaElement sibling, String lineDelimiter) throws CoreException {
        StringBuffer stb = new StringBuffer();
        String[] names = NamingConventions.suggestFieldNames(unit
                .getJavaProject(), fieldType.getPackageFragment()
                .getElementName(), fieldType.getFullyQualifiedName(), 0,
                Modifier.PRIVATE, StringUtil.EMPTY_STRINGS);
        String fieldName = StringUtil.decapitalize(fieldType.getElementName());
        if (names != null && 0 < names.length) {
            fieldName = names[0];
        }
        String comment = CodeGeneration.getFieldComment(unit, fieldType
                .getFullyQualifiedName(), fieldName, lineDelimiter);
        if (StringUtil.isEmpty(comment) == false) {
            stb.append(comment);
            stb.append(lineDelimiter);
        }
        stb.append("private ");
        stb.append(fieldType.getElementName());
        stb.append(' ');
        stb.append(fieldName);
        stb.append(';');
        stb.append(lineDelimiter);

        return type.createField(stb.toString(), sibling, true, monitor);
    }

    protected void createGetter(IType type, IField field,
            IProgressMonitor monitor, String lineDelimiter)
            throws CoreException {
        String fieldName = field.getElementName();
        IType parentType = field.getDeclaringType();
        String getterName = NamingConventions.suggestGetterName(type
                .getJavaProject(), fieldName, field.getFlags(), field
                .getTypeSignature().equals(Signature.SIG_BOOLEAN),
                StringUtil.EMPTY_STRINGS);

        String typeName = fieldType.getElementName();
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
        type.createMethod(stb.toString(), null, false, monitor);
    }

    protected void createSetter(IType type, IField field,
            IProgressMonitor monitor, String lineDelimiter)
            throws CoreException {
        String fieldName = field.getElementName();
        IType parentType = field.getDeclaringType();
        String setterName = NamingConventions.suggestSetterName(type
                .getJavaProject(), fieldName, field.getFlags(), field
                .getTypeSignature().equals(Signature.SIG_BOOLEAN),
                StringUtil.EMPTY_STRINGS);

        String typeName = fieldType.getElementName();

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
        type.createMethod(stb.toString(), null, false, monitor);
    }

    /**
     * @param field
     * @return
     */
    private boolean useThisForFieldAccess(IField field) {
        boolean useThis = Boolean.valueOf(
                PreferenceConstants.getPreference(
                        PreferenceConstants.CODEGEN_KEYWORD_THIS, field
                                .getJavaProject())).booleanValue();
        return useThis;
    }

}