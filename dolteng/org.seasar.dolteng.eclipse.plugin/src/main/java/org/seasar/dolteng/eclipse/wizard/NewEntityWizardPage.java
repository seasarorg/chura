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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.ast.ImportsStructure;
import org.seasar.dolteng.eclipse.model.EntityMappingRow;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.TypeUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class NewEntityWizardPage extends NewClassWizardPage {
    protected EntityMappingPage mappingPage = null;

    protected TableNode currentSelection = null;

    public NewEntityWizardPage(EntityMappingPage page) {
        this.mappingPage = page;
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

        if (created.getElementName().equalsIgnoreCase(
                this.currentSelection.getMetaData().getName()) == false) {
            DoltengPreferences pref = DoltengCore.getPreferences(created
                    .getJavaProject());
            if (ProjectUtil.enableAnnotation(created.getJavaProject())
                    && pref != null
                    && Constants.DAO_TYPE_S2DAO.equals(pref.getDaoType())) {
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
                                                ASTNode entity = rewrite
                                                        .createStringPlaceholder(
                                                                '@'
                                                                        + imports
                                                                                .addImport("org.seasar.dao.annotation.tiger.Bean")
                                                                        + "(table=\""
                                                                        + currentSelection
                                                                                .getMetaData()
                                                                                .getName()
                                                                        + "\")",
                                                                ASTNode.NORMAL_ANNOTATION);
                                                lr
                                                        .insertBefore(
                                                                entity,
                                                                (org.eclipse.jdt.core.dom.Modifier) em,
                                                                null);

                                                break;
                                            }
                                        }
                                    }
                                });
                            }
                        });
            }
        }
    }

    @Override
    protected void createTypeMembers(IType type, ImportsManager imports,
            IProgressMonitor monitor) throws CoreException {
        String lineDelimiter = ProjectUtil.getProjectLineDelimiter(type
                .getJavaProject());
        // メンバフィールド及び、アクセサの生成。処理順が超微妙。
        if (type.getElementName().equalsIgnoreCase(
                this.currentSelection.getMetaData().getName()) == false) {
            if (ProjectUtil.enableAnnotation(type.getJavaProject()) == false) {
                // TABLEアノテーション
                StringBuffer stb = new StringBuffer();
                stb.append("public static final ");
                stb.append(imports.addImport("java.lang.String"));
                stb.append(" TABLE = \"");
                stb.append(this.currentSelection.getMetaData().getName());
                stb.append("\";");
                stb.append(lineDelimiter);
                type.createField(stb.toString(), null, false, monitor);
            }
        }

        // 後で、設定通りの改行コードに変換して貰える。
        List<EntityMappingRow> fields = mappingPage.getMappingRows();
        for (EntityMappingRow meta : fields) {
            IField field = createField(type, imports, meta, mappingPage
                    .getUsePublicField(), new SubProgressMonitor(monitor, 1),
                    lineDelimiter);
            if (mappingPage.getUsePublicField() == false) {
                createGetter(type, imports, meta, field,
                        new SubProgressMonitor(monitor, 1), lineDelimiter);
                createSetter(type, imports, meta, field,
                        new SubProgressMonitor(monitor, 1), lineDelimiter);
            }
        }

        super.createTypeMembers(type, imports, monitor);
    }

    protected IField createField(IType type, ImportsManager imports,
            EntityMappingRow meta, boolean usePublicField,
            IProgressMonitor monitor, String lineDelimiter)
            throws CoreException {
        StringBuffer stb = new StringBuffer();
        if (isAddComments()) {
            String comment = CodeGeneration.getFieldComment(type
                    .getCompilationUnit(), meta.getJavaClassName(), meta
                    .getJavaFieldName(), lineDelimiter);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(lineDelimiter);
            }
        }
        stb.append(usePublicField ? "public " : "private ");
        stb.append(imports.addImport(meta.getJavaClassName()));
        stb.append(' ');
        stb.append(meta.getJavaFieldName());
        stb.append(';');
        stb.append(lineDelimiter);
        IField result = type.createField(stb.toString(), null, false, monitor);
        if (ProjectUtil.enableAnnotation(type.getJavaProject()) == false) {
            if (meta.getSqlColumnName().equalsIgnoreCase(
                    meta.getJavaFieldName()) == false) {
                // カラムアノテーション
                stb = new StringBuffer();
                stb.append("public static final ");
                stb.append(imports.addImport("java.lang.String"));
                stb.append(' ');
                stb.append(meta.getJavaFieldName());
                stb.append("_COLUMN = \"");
                stb.append(meta.getSqlColumnName());
                stb.append("\";");
                stb.append(lineDelimiter);
                type.createField(stb.toString(), null, false, monitor);
            }
        }
        return result;
    }

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

        if (ProjectUtil.enableAnnotation(type.getJavaProject())
                && Constants.DAO_TYPE_S2DAO.equals(DoltengCore.getPreferences(
                        type.getJavaProject()).getDaoType())) {
            if (meta.getSqlColumnName().equalsIgnoreCase(
                    meta.getJavaFieldName()) == false) {
                String s = imports
                        .addImport("org.seasar.dao.annotation.tiger.Column");
                stb.append('@');
                stb.append(s);
                stb.append('(');
                stb.append('"');
                stb.append(meta.getSqlColumnName());
                stb.append('"');
                stb.append(')');
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

    private boolean useThisForFieldAccess(IField field) {
        boolean useThis = Boolean.valueOf(
                PreferenceConstants.getPreference(
                        PreferenceConstants.CODEGEN_KEYWORD_THIS, field
                                .getJavaProject())).booleanValue();
        return useThis;
    }

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

    /**
     * @param currentSelection
     *            The currentSelection to set.
     */
    public void setCurrentSelection(TableNode currentSelection) {
        this.currentSelection = currentSelection;
    }

}
