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

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLElement;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.texteditor.ITextEditor;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.ActionScriptUtil;
import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.TextEditorUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.framework.convention.NamingConvention;

public class AddServiceAction extends AbstractWorkbenchWindowActionDelegate {

    public AddServiceAction() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.action.AbstractEditorActionDelegate#processResource(org.eclipse.core.resources.IProject,
     *      org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences,
     *      org.eclipse.core.resources.IResource)
     */
    @Override
    protected void processResource(IProject project,
            final DoltengPreferences pref, IResource resource) throws Exception {
        if (resource.getType() != IResource.FILE) {
            return;
        }
        ITextEditor txtEditor = TextEditorUtil.toTextEditor(WorkbenchUtil
                .getActiveEditor());
        final IFile mxml = (IFile) resource;
        ActionScriptUtil.modifyMxml(mxml, txtEditor,
                new ActionScriptUtil.MxmlMdifyHandler() {
                    public void modify(FuzzyXMLElement root, IDocument document)
                            throws Exception {
                        addSeviceDefine(pref, mxml, document, root);
                    }
                });
    }

    private void addSeviceDefine(DoltengPreferences pref, IFile mxml,
            IDocument doc, FuzzyXMLElement root) throws JavaModelException,
            Exception, BadLocationException {
        MultiTextEdit edits = new MultiTextEdit();
        SelectionDialog dialog = JavaUI.createTypeDialog(Display.getCurrent()
                .getActiveShell(), WorkbenchUtil.getWorkbenchWindow(), mxml
                .getProject(),
                IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES,
                false);
        dialog.setTitle(Messages.SELECT_FLEX2_SERVICE);
        if (dialog.open() != Window.OK) {
            return;
        }
        Object[] results = dialog.getResult();
        if (results == null || results.length < 1) {
            return;
        }
        IType selected = (IType) results[0];
        String fqn = selected.getFullyQualifiedName();
        NamingConvention nc = pref.getNamingConvention();
        String componentName = nc.fromClassNameToComponentName(fqn);

        if (root.hasAttribute("xmlns:seasar") == false) {
            FuzzyXMLAttribute[] attrs = root.getAttributes();
            if (attrs != null && 0 < attrs.length) {
                FuzzyXMLAttribute a = attrs[attrs.length - 1];
                edits
                        .addChild(new InsertEdit(a.getOffset() + a.getLength(),
                                " xmlns:seasar=\"http://www.seasar.org/s2flex2/mxml\""));
            }
        }

        StringBuffer remoting = new StringBuffer();
        remoting.append("<seasar:S2Flex2Service id=\"service\"");
        remoting.append(" destination=\"");
        remoting.append(componentName);
        remoting.append("\" showBusyCursor=\"true\"");
        remoting.append("/>");
        remoting.append(ProjectUtil.getLineDelimiterPreference(mxml
                .getProject()));

        edits.addChild(new InsertEdit(calcInsertOffset(doc, root), remoting
                .toString()));

        edits.apply(doc);
    }

    private int calcInsertOffset(IDocument doc, FuzzyXMLElement root)
            throws Exception {
        int result = 0;
        ITextEditor txtEditor = TextEditorUtil.toTextEditor(WorkbenchUtil
                .getActiveEditor());
        if (txtEditor != null) {
            ISelectionProvider sp = txtEditor.getSelectionProvider();
            if (sp != null) {
                ISelection s = sp.getSelection();
                if (s instanceof ITextSelection) {
                    ITextSelection ts = (ITextSelection) s;
                    return ts.getOffset();
                }
            }
        }

        FuzzyXMLElement kid = FuzzyXMLUtil.getFirstChild(root);
        if (kid != null) {
            int line = doc.getLineOfOffset(kid.getOffset());
            return doc.getLineOffset(line);
        }
        return result;
    }
}
