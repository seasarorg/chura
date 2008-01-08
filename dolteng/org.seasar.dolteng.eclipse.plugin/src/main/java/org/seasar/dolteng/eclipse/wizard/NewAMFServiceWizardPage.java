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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.seasar.dolteng.eclipse.ast.ImportsStructure;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.TypeUtil;

/**
 * @author taichi
 * 
 */
public class NewAMFServiceWizardPage extends NewClassWizardPage {

    private static final String S2FLEX2_REMOTING = "org.seasar.flex2.rpc.remoting.service.annotation.RemotingService";

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

        if (ProjectUtil.enableAnnotation(created.getJavaProject())) {
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
                                                            '@' + imports
                                                                    .addImport(S2FLEX2_REMOTING),
                                                            ASTNode.MARKER_ANNOTATION);
                                            lr.insertBefore(entity,
                                                    (Modifier) em, null);

                                            break;
                                        }
                                    }
                                }
                            });
                        }
                    });
        }
    }

    @Override
    protected void createTypeMembers(IType type, ImportsManager imports,
            IProgressMonitor monitor) throws CoreException {
        if (ProjectUtil.enableAnnotation(type.getJavaProject()) == false) {
            StringBuffer stb = new StringBuffer();
            stb.append("public static final ");
            stb.append(imports.addImport("java.lang.String"));
            stb.append(" REMOTING_SERVICE = \"");
            // TODO メンドーなので、取り敢えず空文字。
            stb.append("\";");
            type.createField(stb.toString(), null, true, monitor);
        }
    }
}
