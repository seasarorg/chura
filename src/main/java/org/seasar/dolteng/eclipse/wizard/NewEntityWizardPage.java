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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.seasar.dolteng.eclipse.model.EntityMappingRow;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class NewEntityWizardPage extends NewClassWizardPage {

    private static final String LINE_DELIM = System
            .getProperty("line.separator");

    private MetaDataMappingPage mappingPage = null;

    private TableNode currentSelection = null;

    public NewEntityWizardPage(MetaDataMappingPage page) {
        this.mappingPage = page;
    }

    protected void createTypeMembers(IType type, ImportsManager imports,
            IProgressMonitor monitor) throws CoreException {
        // メンバフィールド及び、アクセサの生成。処理順が超微妙。
        if (type.getCompilationUnit().getElementName().toLowerCase().equals(
                this.currentSelection.getMetaData().getName().toLowerCase()) == false) {
            // TABLEアノテーション
            StringBuffer stb = new StringBuffer();
            stb.append("public static final ");
            stb.append(imports.addImport("java.lang.String"));
            stb.append(" TABLE = \"");
            stb.append(this.currentSelection.getMetaData().getName());
            stb.append("\";");
            stb.append(LINE_DELIM);
            type.createField(stb.toString(), null, false, monitor);
        }

        // 後で、設定通りの改行コードに変換して貰える。
        List fields = mappingPage.getMappingRows();
        for (final Iterator i = fields.iterator(); i.hasNext();) {
            EntityMappingRow meta = (EntityMappingRow) i.next();
            IField field = createField(type, imports, meta,
                    new SubProgressMonitor(monitor, 1));
            createGetter(type, imports, meta, field, new SubProgressMonitor(
                    monitor, 1));
            createSetter(type, imports, meta, field, new SubProgressMonitor(
                    monitor, 1));
        }

        super.createTypeMembers(type, imports, monitor);
    }

    /**
     * @param type
     * @param imports
     * @param meta
     * @param monitor
     * @return
     * @throws CoreException
     * @throws JavaModelException
     */
    private IField createField(IType type, ImportsManager imports,
            EntityMappingRow meta, IProgressMonitor monitor) throws CoreException,
            JavaModelException {
        StringBuffer stb = new StringBuffer();
        if (isAddComments()) {
            String comment = CodeGeneration.getFieldComment(type
                    .getCompilationUnit(), meta.getJavaClassName(), meta
                    .getJavaFieldName(), LINE_DELIM);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(LINE_DELIM);
            }
        }
        stb.append("private ");
        stb.append(imports.addImport(meta.getJavaClassName()));
        stb.append(' ');
        stb.append(meta.getJavaFieldName());
        stb.append(';');
        stb.append(LINE_DELIM);
        IField result = type.createField(stb.toString(), null, false, monitor);
        if (meta.getSqlColumnName().toLowerCase().equals(
                meta.getJavaFieldName().toLowerCase()) == false) {
            // カラムアノテーション
            stb = new StringBuffer();
            stb.append("public static final ");
            stb.append(imports.addImport("java.lang.String"));
            stb.append(' ');
            stb.append(meta.getJavaFieldName());
            stb.append("_COLUMN = \"");
            stb.append(meta.getSqlColumnName());
            stb.append("\";");
            stb.append(LINE_DELIM);
            type.createField(stb.toString(), null, false, monitor);
        }

        return result;
    }

    protected void createGetter(IType type, ImportsManager imports,
            EntityMappingRow meta, IField field, IProgressMonitor monitor)
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
                    LINE_DELIM);
            if (comment != null) {
                stb.append(comment);
                stb.append(LINE_DELIM);
            }
        }

        stb.append(Modifier.toString(meta.getJavaModifiers()));
        stb.append(' ');
        stb.append(imports.addImport(typeName));
        stb.append(' ');
        stb.append(getterName);
        stb.append("() {");
        stb.append(LINE_DELIM);

        if (useThisForFieldAccess(field)) {
            fieldName = "this." + fieldName;
        }

        String body = CodeGeneration.getGetterMethodBodyContent(field
                .getCompilationUnit(), parentType.getTypeQualifiedName('.'),
                getterName, fieldName, LINE_DELIM);
        if (body != null) {
            stb.append(body);
        }
        stb.append("}");
        stb.append(LINE_DELIM);
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

    protected void createSetter(IType type, ImportsManager imports,
            EntityMappingRow meta, IField field, IProgressMonitor monitor)
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
                    LINE_DELIM);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(LINE_DELIM);
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
        stb.append(LINE_DELIM);

        boolean useThis = useThisForFieldAccess(field);
        if (argname.equals(fieldName) || useThis) {
            fieldName = "this." + fieldName;
        }
        String body = CodeGeneration.getSetterMethodBodyContent(field
                .getCompilationUnit(), parentType.getTypeQualifiedName('.'),
                setterName, fieldName, argname, LINE_DELIM);
        if (body != null) {
            stb.append(body);
        }
        stb.append("}");
        stb.append(LINE_DELIM);
        type.createMethod(stb.toString(), null, false, monitor);
    }

    /**
     * @param currentSelection
     *            The currentSelection to set.
     */
    public void setCurrentSelection(TableNode currentSelection) {
        this.currentSelection = currentSelection;
    }

}
