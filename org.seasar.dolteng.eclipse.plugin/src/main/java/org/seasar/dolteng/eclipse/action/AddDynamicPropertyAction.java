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
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.texteditor.ITextEditor;
import org.seasar.dolteng.eclipse.operation.AddDinamicPropertyOperation;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;
import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.TextEditorUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wigets.AddDynamicPropertyDialog;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class AddDynamicPropertyAction extends AbstractWorkbenchWindowActionDelegate {

    /**
     * 
     */
    public AddDynamicPropertyAction() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.action.AbstractEditorActionDelegate#processResource(org.eclipse.core.resources.IProject,
     *      org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences,
     *      org.eclipse.core.resources.IResource)
     */
    @Override
    protected void processResource(IProject project, DoltengPreferences pref,
            IResource resource) throws Exception {
        ITextEditor txtEditor = TextEditorUtil.toTextEditor(WorkbenchUtil
                .getActiveEditor());
        if (resource.getFileExtension().startsWith("htm") == false
                || txtEditor == null) {
            return;
        }
        ISelectionProvider provider = txtEditor.getSelectionProvider();
        if (provider != null) {
            ISelection selection = provider.getSelection();
            if (selection instanceof ITextSelection) {
                ITextSelection ts = (ITextSelection) selection;
                FuzzyXMLDocument doc = FuzzyXMLUtil.parse((IFile) resource);
                FuzzyXMLElement element = doc
                        .getElementByOffset(ts.getOffset());
                if (element != null && element.equals(doc) == false
                        && element.hasAttribute("id")) {
                    String[] pkgNames = DoltengProjectUtil.calculatePagePkg(
                            resource, pref);
                    String pageType = getOpenTypeName(resource, pref
                            .getNamingConvention());
                    IJavaProject javap = JavaCore.create(project);
                    IType type = null;
                    for (String pkgName : pkgNames) {
                        String fqn = pkgName + "." + pageType;
                        type = javap.findType(fqn);
                        if (type != null && type.exists()) {
                            AddDynamicPropertyDialog dialog = new AddDynamicPropertyDialog(
                                    WorkbenchUtil.getShell());
                            dialog.setCurrentElement(element);
                            dialog.setEditorOffset(ts.getOffset(), ts
                                    .getLength());
                            if (dialog.open() == Window.OK) {
                                FuzzyXMLAttribute[] attrs = dialog
                                        .getSelectedAttributes();
                                AddDinamicPropertyOperation op = new AddDinamicPropertyOperation(
                                        type, element.getAttributeNode("id")
                                                .getValue(), attrs);
                                ProjectUtil.getWorkspace().run(op, null);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    protected String getOpenTypeName(IResource html, NamingConvention nc) {
        String name = html.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        return StringUtil.capitalize(name) + nc.getPageSuffix();
    }
}
