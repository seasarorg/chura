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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.TextFileBufferUtil;

/**
 * @author taichi
 * 
 */
public class AddJPAAssociationOperation implements IWorkspaceRunnable {

    public static class AnnotationElements {
        public String targetEntity = void.class.getName();

        public List cascade = new ArrayList();

        public String fetch = "EAGER";

        public boolean optional = true;

        public String mappedBy = "";
    }

    private AnnotationElements elements;

    private ICompilationUnit rootAst;

    private IField target;

    public AddJPAAssociationOperation(ICompilationUnit rootAst, IField target,
            AnnotationElements elements) {
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
    public void run(IProgressMonitor monitor) throws CoreException {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(rootAst);
        ASTNode node = parser.createAST(monitor);
        final ASTRewrite rewrite = ASTRewrite.create(node.getAST());
        IDocument document = null;
        ITextFileBuffer buffer = null;
        try {
            if (rootAst.getOwner() != null) {
                document = new Document(rootAst.getBuffer().getContents());
            } else {
                buffer = TextFileBufferUtil.acquire(rootAst);
                document = buffer.getDocument();
            }
            node.accept(new ASTVisitor() {
                public boolean visit(FieldDeclaration node) {
                    VariableDeclarationFragment fragment = (VariableDeclarationFragment) node
                            .fragments().get(0);
                    if (fragment.getName().getIdentifier().equals(
                            target.getElementName())) {
                        return true;
                    }
                    return false;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Initializer)
                 */
                public boolean visit(Initializer node) {
                    return false;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MarkerAnnotation)
                 */
                public boolean visit(MarkerAnnotation node) {
                    return super.visit(node);
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
                 */
                public boolean visit(MethodDeclaration node) {
                    return false;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.NormalAnnotation)
                 */
                public boolean visit(NormalAnnotation node) {
                    return super.visit(node);
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SingleMemberAnnotation)
                 */
                public boolean visit(SingleMemberAnnotation node) {
                    return super.visit(node);
                }

            });

            TextEdit edit = rewrite.rewriteAST(document, rootAst
                    .getJavaProject().getOptions(true));
            edit.apply(document);
            if (buffer != null) {
                buffer.commit(new SubProgressMonitor(monitor, 1), true);
            }
        } catch (Exception e) {
            DoltengCore.log(e);
            if (buffer != null) {
                TextFileBufferUtil.release(rootAst);
            }
        }
    }

}
