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
package org.seasar.dolteng.eclipse.action;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.ast.JPAAssociationElements;
import org.seasar.dolteng.eclipse.operation.AddJPAAssociationOperation;
import org.seasar.dolteng.eclipse.util.JavaProjectClassLoader;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.TypeUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wigets.JPAAssociationDialog;
import org.seasar.framework.util.CaseInsensitiveMap;

/**
 * @author taichi
 * 
 */
@SuppressWarnings("unchecked")
public class JPAAssociateAction implements IEditorActionDelegate {

    private static final Map<String, AssociateAnnotationReader> ASSOCIATE_ANNOTATION_READERS = new CaseInsensitiveMap();

    private IEditorPart targetEditor;

    private ITextSelection selection;

    static {
        ASSOCIATE_ANNOTATION_READERS.put("targetEntity",
                new TargetEntityReader());
        ASSOCIATE_ANNOTATION_READERS.put("cascade", new CascadeReader());
        ASSOCIATE_ANNOTATION_READERS.put("fetch", new FetchReader());
        ASSOCIATE_ANNOTATION_READERS.put("optional", new OptionalReader());
        ASSOCIATE_ANNOTATION_READERS.put("mappedBy", new MappedByReader());
    }

    /**
     * 
     */
    public JPAAssociateAction() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
     *      org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        this.targetEditor = targetEditor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof ITextSelection) {
            this.selection = (ITextSelection) selection;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        try {
            final ICompilationUnit cu = getCompilationUnit();
            if (this.selection != null && cu != null && cu.exists()) {
                IJavaElement[] elems = cu.codeSelect(
                        this.selection.getOffset(), this.selection.getLength());
                if (elems != null && 0 < elems.length
                        && elems[0] instanceof IField) {
                    final IField field = (IField) elems[0];

                    ASTParser parser = ASTParser.newParser(AST.JLS3);
                    parser.setSource(field.getSource().toCharArray());
                    parser.setProject(cu.getJavaProject());
                    parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
                    ASTNode node = parser.createAST(new NullProgressMonitor());
                    final JPAAssociationElements ae = new JPAAssociationElements();
                    node.accept(new ASTVisitor() {
                        @Override
                        public boolean visit(FieldDeclaration node) {
                            JavaProjectClassLoader loader = null;
                            try {
                                VariableDeclarationFragment fragment = (VariableDeclarationFragment) node
                                        .fragments().get(0);
                                boolean is = fragment.getName().getIdentifier()
                                        .equals(field.getElementName());
                                if (is) {
                                    loader = new JavaProjectClassLoader(field
                                            .getJavaProject());
                                    String type = TypeUtil.getResolvedTypeName(
                                            field.getTypeSignature(), field
                                                    .getDeclaringType());
                                    Class sig = loadType(type, loader);
                                    Class<?> collection = loader
                                            .loadClass("java.util.Collection");
                                    if (collection.isAssignableFrom(sig)) {
                                        ae.setName("OneToMany");
                                    } else {
                                        ae.setName("ManyToOne");
                                    }
                                }
                                return is;
                            } catch (Exception e) {
                                DoltengCore.log(e);
                            } finally {
                                JavaProjectClassLoader.dispose(loader);
                            }
                            return false;
                        }

                        private Class loadType(String type,
                                JavaProjectClassLoader loader)
                                throws JavaModelException,
                                ClassNotFoundException {
                            int dimension = Signature.getArrayCount(field
                                    .getTypeSignature());
                            Class sig = null;
                            if (dimension < 1) {
                                sig = loader.loadClass(type);
                            } else {
                                sig = loader.loadClass(type.substring(0, type
                                        .indexOf('[')));
                            }
                            return sig;
                        }

                        @Override
                        public boolean visit(MarkerAnnotation node) {
                            return getAnnotationName(node.getTypeName());
                        }

                        private boolean getAnnotationName(Name name) {
                            String s = name.getFullyQualifiedName();
                            if (JPAAssociationElements.ASSOCIATE_ANNOTATIONS
                                    .contains(s)) {
                                ae.setName(s);
                                ae.setExists(true);
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public boolean visit(NormalAnnotation node) {
                            return getAnnotationName(node.getTypeName());
                        }

                        @Override
                        public boolean visit(MemberValuePair node) {
                            String name = node.getName().getIdentifier();
                            if (ASSOCIATE_ANNOTATION_READERS.containsKey(name)) {
                                AssociateAnnotationReader aar = ASSOCIATE_ANNOTATION_READERS
                                        .get(name);
                                aar.read(node.getValue(), ae);
                            }
                            return true;
                        }

                    });

                    JPAAssociationDialog dialog = new JPAAssociationDialog(
                            WorkbenchUtil.getShell());
                    dialog.setElements(ae);
                    if (dialog.open() == Dialog.OK) {
                        ProjectUtil.getWorkspace().run(
                                new AddJPAAssociationOperation(cu, field,
                                        dialog.getElements()), null);

                    }
                }
            }
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }

    private interface AssociateAnnotationReader {
        void read(Expression value, JPAAssociationElements ae);
    }

    private static class TargetEntityReader implements
            AssociateAnnotationReader {
        public void read(Expression value, JPAAssociationElements ae) {
            TypeLiteral tl = (TypeLiteral) value;
            Type type = tl.getType();
            if (type.isQualifiedType()) {
                ae.setTargetEntity(((QualifiedType) type).getName()
                        .getFullyQualifiedName());
            } else if (type.isSimpleType()) {
                ae.setTargetEntity(((SimpleType) type).getName()
                        .getFullyQualifiedName());
            }
        }
    }

    private static class CascadeReader implements AssociateAnnotationReader {
        public void read(Expression value, JPAAssociationElements ae) {
            if (value instanceof Name) {
                Name name = (Name) value;
                ae.getCascade().add(name.getFullyQualifiedName());
            } else if (value instanceof ArrayInitializer) {
                ArrayInitializer ai = (ArrayInitializer) value;
                for (Iterator i = ai.expressions().iterator(); i.hasNext();) {
                    Name n = (Name) i.next();
                    ae.getCascade().add(n.getFullyQualifiedName());
                }
            }
        }
    }

    private static class FetchReader implements AssociateAnnotationReader {
        public void read(Expression value, JPAAssociationElements ae) {
            Name name = (Name) value;
            ae.setFetch(name.getFullyQualifiedName());
        }
    }

    private static class OptionalReader implements AssociateAnnotationReader {
        public void read(Expression value, JPAAssociationElements ae) {
            BooleanLiteral bl = (BooleanLiteral) value;
            ae.setOptional(bl.booleanValue());
        }
    }

    private static class MappedByReader implements AssociateAnnotationReader {
        public void read(Expression value, JPAAssociationElements ae) {
            StringLiteral sl = (StringLiteral) value;
            ae.setMappedBy(sl.getLiteralValue());
        }
    }

    private ICompilationUnit getCompilationUnit() {
        if (this.targetEditor == null) {
            return null;
        }
        Object unit = this.targetEditor.getEditorInput().getAdapter(
                IJavaElement.class);
        if (unit instanceof ICompilationUnit) {
            return (ICompilationUnit) unit;
        }
        return null;
    }

}
