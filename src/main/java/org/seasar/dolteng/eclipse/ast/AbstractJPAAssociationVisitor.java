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
package org.seasar.dolteng.eclipse.ast;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
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

    protected Annotation createMarkerAnnotation() {
        Annotation annon = rewrite.getAST().newMarkerAnnotation();
        annon.setTypeName(rewrite.getAST().newSimpleName(
                structure.addImport(elements.getName())));
        return annon;
    }

    protected Annotation createNormalAnnotation() {
        NormalAnnotation annon = rewrite.getAST().newNormalAnnotation();
        annon.setTypeName(rewrite.getAST().newSimpleName(
                structure.addImport(elements.getName())));
        List children = annon.values();
        addTargetEntity(children);
        addCascade(children);
        addFetch(children);
        addOptional(children);
        addMappedBy(children);
        return annon;
    }

    private void addTargetEntity(List list) {
        if (isDefaultTargetEntity() == false) {
            MemberValuePair targetEntity = create("targetEntity");
            Type q = rewrite.getAST().newSimpleType(
                    rewrite.getAST().newSimpleName(
                            structure.addImport(elements.getTargetEntity())));
            TypeLiteral type = rewrite.getAST().newTypeLiteral();
            type.setType(q);
            targetEntity.setValue(type);
            list.add(targetEntity);
        }
    }

    private boolean isDefaultTargetEntity() {
        try {
            String type = TypeUtil.getResolvedTypeName(target
                    .getTypeSignature(), target.getDeclaringType());
            JavaProjectClassLoader loader = new JavaProjectClassLoader(target
                    .getJavaProject());
            Class sig = loader.loadClass(type);
        } catch (Exception e) {
            // FIXME : Generics な配列の場合のエラーを何とかする。
            DoltengCore.log(e);
        }
        return true;
    }

    private void addCascade(List list) {
        if (0 < elements.getCascade().size()) {
            MemberValuePair cascade = create("cascade");
            if (elements.getCascade().size() == 1) {
                String name = ClassUtil.getShortClassName(elements.getCascade()
                        .get(0).toString());
                name = structure.addStaticImport(
                        "javax.persistence.CascadeType", name, true);
                cascade.setValue(rewrite.getAST().newSimpleName(name));
            } else {
                ArrayInitializer initializer = rewrite.getAST()
                        .newArrayInitializer();
                List exps = initializer.expressions();
                for (Iterator i = elements.getCascade().iterator(); i.hasNext();) {
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
        if (elements.isDefaultFetch() == false) {
            MemberValuePair fetch = create("fetch");
            Name name = rewrite.getAST().newSimpleName(
                    structure.addStaticImport("javax.persistence.FetchType",
                            elements.getFetch(), true));
            fetch.setValue(name);
            list.add(fetch);
        }
    }

    private void addOptional(List list) {
        if (elements.isOptional() == false) {
            MemberValuePair optional = create("optional");
            BooleanLiteral literal = rewrite.getAST().newBooleanLiteral(false);
            optional.setValue(literal);
            list.add(optional);
        }
    }

    private void addMappedBy(List list) {
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
    public boolean visit(MethodDeclaration node) {
        return false;
    }

    public boolean visit(Initializer node) {
        return false;
    }
}