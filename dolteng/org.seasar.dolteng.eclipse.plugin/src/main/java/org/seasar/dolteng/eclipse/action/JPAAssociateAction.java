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
package org.seasar.dolteng.eclipse.action;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.operation.AddJPAAssociationOperation;

/**
 * @author taichi
 * 
 */
public class JPAAssociateAction implements IEditorActionDelegate {

    private IEditorPart targetEditor;

    private ITextSelection selection;

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
            ICompilationUnit cu = getCompilationUnit();
            if (this.selection != null && cu != null && cu.exists()) {
                IJavaElement[] elems = cu.codeSelect(
                        this.selection.getOffset(), this.selection.getLength());
                if (elems != null && 0 < elems.length
                        && elems[0] instanceof IField) {
                    IField field = (IField) elems[0];
                    ASTParser parser = ASTParser.newParser(AST.JLS3);
                    parser.setSource(field.getSource().toCharArray());
                    ASTNode node = parser.createAST(new NullProgressMonitor());
                    AddJPAAssociationOperation op = new AddJPAAssociationOperation(
                            cu, field, null);
                    op.run(new NullProgressMonitor());
                }
            }
        } catch (CoreException e) {
            DoltengCore.log(e);
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
