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

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.ui.CodeGeneration;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.ast.ImportsStructure;
import org.seasar.dolteng.eclipse.convention.NamingUtil;
import org.seasar.dolteng.eclipse.model.EntityMappingRow;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.TypeUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class JPAEntityWizardPage extends NewEntityWizardPage {

    public JPAEntityWizardPage(EntityMappingPage page) {
        super(page);
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
        final ICompilationUnit unit = created.getCompilationUnit();
        DoltengPreferences pref = DoltengCore.getPreferences(created
                .getJavaProject());
        if (ProjectUtil.enableAnnotation(created.getJavaProject())
                && pref != null
                && Constants.DAO_TYPE_KUINADAO.equals(pref.getDaoType())) {
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
                                                            '@' + imports
                                                                    .addImport("javax.persistence.Entity"),
                                                            ASTNode.MARKER_ANNOTATION);
                                            lr.insertBefore(entity,
                                                    (Modifier) em, null);

                                            String metaName = currentSelection
                                                    .getMetaData().getName();
                                            if (getPrimaryName(unit)
                                                    .equalsIgnoreCase(metaName) == false) {
                                                StringBuffer stb = new StringBuffer();
                                                stb.append('@');
                                                stb
                                                        .append(imports
                                                                .addImport("javax.persistence.Table"));
                                                stb.append("(name=\"");
                                                stb.append(metaName);
                                                stb.append("\")");

                                                ASTNode table = rewrite
                                                        .createStringPlaceholder(
                                                                stb.toString(),
                                                                ASTNode.NORMAL_ANNOTATION);
                                                lr.insertBefore(table,
                                                        (Modifier) em, null);
                                            }
                                            break;
                                        }
                                    }
                                }
                            });
                        }
                    });
        }
    }

    private String getPrimaryName(ICompilationUnit cu) {
        return cu.getPath().removeFileExtension().lastSegment();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.wizard.NewEntityWizardPage#createField(org.eclipse.jdt.core.IType,
     *      org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager,
     *      org.seasar.dolteng.eclipse.model.EntityMappingRow, boolean,
     *      org.eclipse.core.runtime.IProgressMonitor, java.lang.String)
     */
    @Override
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
        if (meta.isPrimaryKey()) {
            stb.append('@');
            stb.append(imports.addImport("javax.persistence.Id"));
            stb.append(lineDelimiter);
            stb.append('@');
            stb.append(imports.addImport("javax.persistence.GeneratedValue"));
            stb.append(lineDelimiter);
        }
        if (NamingUtil.isVersionNo(meta.getSqlColumnName())) {
            stb.append('@');
            stb.append(imports.addImport("javax.persistence.Version"));
            stb.append(lineDelimiter);
        }
        if (meta.isDate()) {
            stb.append('@');
            stb.append(imports.addImport("javax.persistence.Temporal"));
            stb.append('(');
            stb.append(imports.addImport("javax.persistence.TemporalType"));
            stb.append(".DATE");
            stb.append(')');
            stb.append(lineDelimiter);
        }
        if (meta.getSqlColumnName().equalsIgnoreCase(meta.getJavaFieldName()) == false) {
            stb.append('@');
            stb.append(imports.addImport("javax.persistence.Column"));
            stb.append("(name=\"");
            stb.append(meta.getSqlColumnName());
            stb.append("\")");
            stb.append(lineDelimiter);
        }
        stb.append("private ");
        stb.append(imports.addImport(meta.getJavaClassName()));
        stb.append(' ');
        stb.append(meta.getJavaFieldName());
        stb.append(';');
        stb.append(lineDelimiter);
        stb.append(lineDelimiter);
        IField result = type.createField(stb.toString(), null, false, monitor);

        return result;
    }
}