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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.ast.ImportsStructure;
import org.seasar.dolteng.eclipse.util.TextFileBufferUtil;
import org.seasar.dolteng.eclipse.util.TypeUtil;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class AddJPAAssociationOperation implements IWorkspaceRunnable {

    public static final String DEFAULT_TARGET_ENTITY = void.class.getName();

    public static final String DEFAULT_FETCH = "EAGER";

    public static class JPAAssociationElements {
        public boolean exists = false;

        public String name = "";

        public String targetEntity = DEFAULT_TARGET_ENTITY;

        public List cascade = new ArrayList();

        public String fetch = DEFAULT_FETCH;

        public boolean optional = true;

        public String mappedBy = "";

        public boolean isMarker() {
            return DEFAULT_TARGET_ENTITY.equals(targetEntity)
                    && cascade.size() < 1 && DEFAULT_FETCH.equals(fetch)
                    && optional == true && StringUtil.isEmpty(mappedBy);
        }

    }

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
    public void run(IProgressMonitor monitor) throws CoreException {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(rootAst);
        CompilationUnit node = (CompilationUnit) parser.createAST(monitor);
        ASTRewrite rewrite = ASTRewrite.create(node.getAST());
        IDocument document = null;
        ITextFileBuffer buffer = null;
        try {
            if (rootAst.getOwner() != null) {
                document = new Document(rootAst.getBuffer().getContents());
            } else {
                buffer = TextFileBufferUtil.acquire(rootAst);
                document = buffer.getDocument();
            }
            ImportsStructure structure = new ImportsStructure(rootAst);
            ASTVisitor editor = null;
            if (elements.exists) {
                editor = new ReplaceJPAAssociationVisitor(rewrite, structure,
                        target, elements);
            } else {
                editor = new AddJPAAssociationVisitor(rewrite, structure,
                        target, elements);
            }

            node.accept(editor);

            MultiTextEdit edit = structure.getResultingEdits(document, monitor);
            edit.addChild(rewrite.rewriteAST(document, rootAst.getJavaProject()
                    .getOptions(true)));
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

    private abstract class AbstractJPAAssociationVisitor extends ASTVisitor {
        protected ASTRewrite rewrite;

        protected ImportsStructure structure;

        protected IField target;

        protected JPAAssociationElements elements;

        protected AbstractJPAAssociationVisitor(ASTRewrite rewrite,
                ImportsStructure structure, IField target,
                JPAAssociationElements elements) {
            this.rewrite = rewrite;
            this.structure = structure;
            this.target = target;
            this.elements = elements;
        }

        protected Annotation createMarkerAnnotation() {
            Annotation annon = rewrite.getAST().newMarkerAnnotation();
            annon.setTypeName(rewrite.getAST().newSimpleName(
                    structure.addImport(elements.name)));
            return annon;
        }

        protected Annotation createNormalAnnotation() {
            NormalAnnotation annon = rewrite.getAST().newNormalAnnotation();
            annon.setTypeName(rewrite.getAST().newSimpleName(
                    structure.addImport(elements.name)));
            List children = annon.values();
            addTargetEntity(children);
            addCascade(children);
            addFetch(children);
            addOptional(children);
            addMappedBy(children);
            return annon;
        }

        private void addTargetEntity(List list) {
            if (DEFAULT_TARGET_ENTITY.equals(elements.targetEntity) == false) {
                MemberValuePair targetEntity = create("targetEntity");
                Type q = rewrite.getAST().newSimpleType(
                        rewrite.getAST().newSimpleName(
                                structure.addImport(elements.targetEntity)));
                TypeLiteral type = rewrite.getAST().newTypeLiteral();
                type.setType(q);
                targetEntity.setValue(type);
                list.add(targetEntity);
            }
        }

        private void addCascade(List list) {
            if (0 < elements.cascade.size()) {
                MemberValuePair cascade = create("cascade");
                if (elements.cascade.size() == 1) {
                    String name = ClassUtil.getShortClassName(elements.cascade
                            .get(0).toString());
                    name = structure.addStaticImport(
                            "javax.persistence.CascadeType", name, true);
                    cascade.setValue(rewrite.getAST().newSimpleName(name));
                } else {
                    ArrayInitializer initializer = rewrite.getAST()
                            .newArrayInitializer();
                    List exps = initializer.expressions();
                    for (Iterator i = elements.cascade.iterator(); i.hasNext();) {
                        String name = ClassUtil.getShortClassName(i.next()
                                .toString());
                        name = structure.addStaticImport(
                                "javax.persistence.CascadeType", name, true);
                        exps.add(rewrite.getAST().newSimpleName(name));
                    }
                    cascade.setValue(initializer);
                }
                list.add(cascade);
            }
        }

        private void addFetch(List list) {
            if (DEFAULT_FETCH.equals(elements.fetch)) {
                MemberValuePair fetch = create("fetch");
                Name name = rewrite.getAST().newSimpleName(
                        structure.addStaticImport(
                                "javax.persistence.FetchType", elements.fetch,
                                true));
                fetch.setValue(name);
                list.add(fetch);
            }
        }

        private void addOptional(List list) {
            if (elements.optional == false) {
                MemberValuePair optional = create("optional");
                BooleanLiteral literal = rewrite.getAST().newBooleanLiteral(
                        false);
                optional.setValue(literal);
                list.add(optional);
            }
        }

        private void addMappedBy(List list) {
            if (StringUtil.isEmpty(elements.mappedBy) == false) {
                MemberValuePair mappedBy = create("mappedBy");
                StringLiteral literal = rewrite.getAST().newStringLiteral();
                literal.setLiteralValue(elements.mappedBy);
                mappedBy.setValue(literal);
                list.add(mappedBy);
            }
        }

        private MemberValuePair create(String name) {
            MemberValuePair mvp = rewrite.getAST().newMemberValuePair();
            mvp.setName(rewrite.getAST().newSimpleName(name));
            return mvp;
        }

        /* ---- skip visit ---- */
        public boolean visit(MethodDeclaration node) {
            return false;
        }

        public boolean visit(Initializer node) {
            return false;
        }
    }

    private class ReplaceJPAAssociationVisitor extends
            AbstractJPAAssociationVisitor {

        public ReplaceJPAAssociationVisitor(ASTRewrite rewrite,
                ImportsStructure structure, IField target,
                JPAAssociationElements elements) {
            super(rewrite, structure, target, elements);
        }

        public boolean visit(FieldDeclaration node) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) node
                    .fragments().get(0);
            return fragment.getName().getIdentifier().equals(
                    target.getElementName());
        }

        public boolean visit(MarkerAnnotation node) {
            String name = TypeUtil.resolveType(node.getTypeName()
                    .getFullyQualifiedName(), target.getDeclaringType());
            if (elements.name.equals(name) && elements.isMarker()) {
                rewrite.replace(node, createMarkerAnnotation(), null);
            }
            return false;
        }

        public boolean visit(NormalAnnotation node) {
            String name = TypeUtil.resolveType(node.getTypeName()
                    .getFullyQualifiedName(), target.getDeclaringType());
            if (elements.name.equals(name) && elements.isMarker() == false) {
                rewrite.replace(node, createNormalAnnotation(), null);
            }
            return false;
        }

    }

    private class AddJPAAssociationVisitor extends
            AbstractJPAAssociationVisitor {

        public AddJPAAssociationVisitor(ASTRewrite rewrite,
                ImportsStructure structure, IField target,
                JPAAssociationElements elements) {
            super(rewrite, structure, target, elements);
        }

        public boolean visit(FieldDeclaration node) {
            try {
                VariableDeclarationFragment fragment = (VariableDeclarationFragment) node
                        .fragments().get(0);
                if (fragment.getName().getIdentifier().equals(
                        target.getElementName())) {
                    Annotation annon = null;
                    if (elements.isMarker()) {
                        annon = createMarkerAnnotation();
                    } else {
                        annon = createNormalAnnotation();
                    }
                    rewrite.getListRewrite(node,
                            FieldDeclaration.MODIFIERS2_PROPERTY).insertLast(
                            annon, null);
                }
            } catch (Exception e) {
                DoltengCore.log(e);
            }
            return false;
        }

    }
}
