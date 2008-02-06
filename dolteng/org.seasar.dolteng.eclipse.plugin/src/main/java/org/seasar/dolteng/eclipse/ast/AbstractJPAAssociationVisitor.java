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
package org.seasar.dolteng.eclipse.ast;

import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.JavaProjectClassLoader;
import org.seasar.dolteng.eclipse.util.TypeUtil;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

abstract class AbstractJPAAssociationVisitor extends ASTVisitor {
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

    @Override
    public void endVisit(FieldDeclaration node) {
        try {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) node
                    .fragments().get(0);
            if (fragment.getName().getIdentifier().equals(
                    target.getElementName())) {
                Annotation annon = null;
                if (isMarker()) {
                    annon = createMarkerAnnotation();
                } else {
                    annon = createNormalAnnotation();
                }
                List mods = node.modifiers();
                for (int i = 0; i < mods.size(); i++) {
                    IExtendedModifier im = (IExtendedModifier) mods.get(i);
                    if (im.isModifier()) {
                        rewrite.getListRewrite(node,
                                FieldDeclaration.MODIFIERS2_PROPERTY)
                                .insertBefore(annon, (Modifier) im, null);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    protected Annotation createMarkerAnnotation() {
        Annotation annon = rewrite.getAST().newMarkerAnnotation();
        annon.setTypeName(rewrite.getAST().newSimpleName(
                structure.addImport(elements.getName())));
        return annon;
    }

    @SuppressWarnings("unchecked")
    protected Annotation createNormalAnnotation() {
        NormalAnnotation annon = rewrite.getAST().newNormalAnnotation();
        annon.setTypeName(rewrite.getAST().newSimpleName(
                structure.addImport(elements.getName())));
        List<MemberValuePair> children = annon.values();
        addTargetEntity(children);
        addCascade(children);
        addFetch(children);
        addOptional(children);
        addMappedBy(children);
        return annon;
    }

    private void addTargetEntity(List<MemberValuePair> list) {
        if (isDefaultTargetEntity() == false) {
            MemberValuePair targetEntity = create("targetEntity");
            String name = TypeUtil.resolveType(elements.getTargetEntity(),
                    target.getDeclaringType());
            Type q = rewrite.getAST().newSimpleType(
                    rewrite.getAST().newSimpleName(structure.addImport(name)));
            TypeLiteral type = rewrite.getAST().newTypeLiteral();
            type.setType(q);
            targetEntity.setValue(type);
            list.add(targetEntity);
        }
    }

    private boolean isDefaultTargetEntity() {
        JavaProjectClassLoader loader = null;
        try {
            if (StringUtil.isEmpty(elements.getTargetEntity())) {
                return true;
            }
            loader = new JavaProjectClassLoader(target.getJavaProject());
            String type = TypeUtil.getResolvedTypeName(target
                    .getTypeSignature(), target.getDeclaringType());
            Class sig = loadType(type, loader);
            Class<?> collection = loader.loadClass("java.util.Collection");
            if (collection.isAssignableFrom(sig)) {
                type = target.getTypeSignature();
                type = type.substring(type.indexOf('<') + 1, type.indexOf('>'));
                type = TypeUtil.getResolvedTypeName(type, target
                        .getDeclaringType());
                sig = loadType(type, loader);
            }
            String enttype = TypeUtil.resolveType(elements.getTargetEntity(),
                    target.getDeclaringType());
            Class ent = loader.loadClass(enttype);
            return sig.equals(ent);
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            JavaProjectClassLoader.dispose(loader);
        }
        return true;
    }

    private Class loadType(String type, JavaProjectClassLoader loader)
            throws JavaModelException, ClassNotFoundException {
        int dimension = Signature.getArrayCount(target.getTypeSignature());
        Class sig = null;
        if (dimension < 1) {
            sig = loader.loadClass(type);
        } else {
            sig = loader.loadClass(type.substring(0, type.indexOf('[')));
        }
        return sig;
    }

    @SuppressWarnings("unchecked")
    private void addCascade(List<MemberValuePair> list) {
        if (0 < elements.getCascade().size()) {
            MemberValuePair cascade = create("cascade");
            if (elements.getCascade().size() == 1) {
                cascade.setValue(rewrite.getAST().newSimpleName(
                        importCascadeType(elements.getCascade().get(0)
                                .toString())));
            } else {
                ArrayInitializer initializer = rewrite.getAST()
                        .newArrayInitializer();
                List<Object> exps = initializer.expressions();
                for (Object element : elements.getCascade()) {
                    exps.add(rewrite.getAST().newSimpleName(
                            importCascadeType(element.toString())));
                }
                cascade.setValue(initializer);
            }
            list.add(cascade);
        }
    }

    private String importCascadeType(String type) {
        String name = ClassUtil.getShortClassName(type);
        return structure.addStaticImport("javax.persistence.CascadeType", name,
                true);
    }

    private void addFetch(List<MemberValuePair> list) {
        if (elements.isDefaultFetch() == false) {
            MemberValuePair fetch = create("fetch");
            Name name = rewrite.getAST().newSimpleName(
                    structure.addStaticImport("javax.persistence.FetchType",
                            elements.getFetch(), true));
            fetch.setValue(name);
            list.add(fetch);
        }
    }

    private void addOptional(List<MemberValuePair> list) {
        if (elements.isOptional() == false) {
            MemberValuePair optional = create("optional");
            BooleanLiteral literal = rewrite.getAST().newBooleanLiteral(false);
            optional.setValue(literal);
            list.add(optional);
        }
    }

    private void addMappedBy(List<MemberValuePair> list) {
        if (StringUtil.isEmpty(elements.getMappedBy()) == false) {
            MemberValuePair mappedBy = create("mappedBy");
            StringLiteral literal = rewrite.getAST().newStringLiteral();
            literal.setLiteralValue(elements.getMappedBy());
            mappedBy.setValue(literal);
            list.add(mappedBy);
        }
    }

    private MemberValuePair create(String name) {
        MemberValuePair mvp = rewrite.getAST().newMemberValuePair();
        mvp.setName(rewrite.getAST().newSimpleName(name));
        return mvp;
    }

    public boolean isMarker() {
        return isDefaultTargetEntity() && elements.getCascade().size() < 1
                && elements.isDefaultFetch() && elements.isOptional() == true
                && StringUtil.isEmpty(elements.getMappedBy());
    }

    /* ---- skip visit ---- */
    @Override
    public boolean visit(MethodDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(final Initializer node) {
        return false;
    }
}