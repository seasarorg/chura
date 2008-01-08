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

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.seasar.dolteng.eclipse.util.TypeUtil;

public class ReplaceJPAAssociationVisitor extends AbstractJPAAssociationVisitor {

    public ReplaceJPAAssociationVisitor(ASTRewrite rewrite,
            ImportsStructure structure, IField target,
            JPAAssociationElements elements) {
        super(rewrite, structure, target, elements);
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) node
                .fragments().get(0);
        return fragment.getName().getIdentifier().equals(
                target.getElementName());
    }

    @Override
    public boolean visit(MarkerAnnotation node) {
        String name = TypeUtil.resolveType(node.getTypeName()
                .getFullyQualifiedName(), target.getDeclaringType());
        if (JPAAssociationElements.ASSOCIATE_ANNOTATIONS.contains(name)) {
            rewrite.remove(node, null);
        }
        return false;
    }

    @Override
    public boolean visit(NormalAnnotation node) {
        String name = TypeUtil.resolveType(node.getTypeName()
                .getFullyQualifiedName(), target.getDeclaringType());
        if (JPAAssociationElements.ASSOCIATE_ANNOTATIONS.contains(name)) {
            rewrite.remove(node, null);
        }
        return false;
    }

}