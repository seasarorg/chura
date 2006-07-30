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

import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.operation.AddJPAAssociationOperation;

public class AddJPAAssociationVisitor extends AbstractJPAAssociationVisitor {

    public AddJPAAssociationVisitor(AddJPAAssociationOperation operation,
            ASTRewrite rewrite, ImportsStructure structure, IField target,
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
        return false;
    }

}