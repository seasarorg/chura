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

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.seasar.dolteng.eclipse.ast.AddJPAAssociationVisitor;
import org.seasar.dolteng.eclipse.ast.ImportsStructure;
import org.seasar.dolteng.eclipse.ast.JPAAssociationElements;
import org.seasar.dolteng.eclipse.ast.ReplaceJPAAssociationVisitor;
import org.seasar.dolteng.eclipse.util.TypeUtil;

/**
 * @author taichi
 * 
 */
public class AddJPAAssociationOperation implements IWorkspaceRunnable {

    private JPAAssociationElements elements;

    private ICompilationUnit rootAst;

    private IField target;

    public AddJPAAssociationOperation(ICompilationUnit rootAst, IField target,
            JPAAssociationElements elements) {
        super();
        this.elements = elements;
        this.rootAst = rootAst;
        this.target = target;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) {
        TypeUtil.modifyType(rootAst, monitor, new TypeUtil.ModifyTypeHandler() {
            public void modify(ASTNode node, ASTRewrite rewrite,
                    ImportsStructure imports) {
                ASTVisitor editor = null;
                if (elements.isExists()) {
                    editor = new ReplaceJPAAssociationVisitor(rewrite, imports,
                            target, elements);
                } else {
                    editor = new AddJPAAssociationVisitor(rewrite, imports,
                            target, elements);
                }

                node.accept(editor);
            }
        });
    }
}
