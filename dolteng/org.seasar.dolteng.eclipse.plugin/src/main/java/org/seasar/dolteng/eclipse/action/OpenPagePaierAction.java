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

import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;

/**
 * @author taichi
 * 
 */
public class OpenPagePaierAction implements IEditorActionDelegate {

    private IEditorPart targetEditor;

    private ISelection selection;

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
        this.selection = selection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        IEditorInput input = targetEditor.getEditorInput();
        IResource resource = (IResource) input.getAdapter(IResource.class);
        IProject project = resource.getProject();

        DoltengProjectPreferences pref = DoltengCore.getPreferences(project);
        if (pref == null) {
            return;
        }

        IJavaElement javaElem = (IJavaElement) input
                .getAdapter(IJavaElement.class);

        if (javaElem != null) {
            if (javaElem instanceof ICompilationUnit) {
                ICompilationUnit unit = (ICompilationUnit) javaElem;
                IType type = unit.findPrimaryType();
                String typeName = type.getElementName();
                if (typeName.endsWith("Page")) {
                    typeName = typeName.substring(0, typeName.indexOf("Page"));
                } else if (typeName.endsWith("Action")) {
                    typeName = typeName
                            .substring(0, typeName.indexOf("Action"));
                } else {
                    return;
                }
                String pkg = type.getPackageFragment().getElementName();
                String webPkg = pref.getRawPreferences().getString(
                        Constants.PREF_DEFAULT_WEB_PACKAGE);
                if (pkg.startsWith(webPkg)) {
                    pkg = pkg.substring(webPkg.length() + 1);
                    pkg = pkg.replace('.', '/');
                    IFolder folder = project.getFolder(pref
                            .getWebContentsRoot());
                    if (folder.exists() == false) {
                        return;
                    }
                    folder = folder.getFolder(pref.getRawPreferences()
                            .getString(Constants.PREF_DEFAULT_VIEW_ROOT_PATH));
                    folder = folder.getFolder(new Path(pkg));
                    findHtml(folder, typeName);
                }
            }
        }
    }

    private void findHtml(IFolder folder, String typeName) {
        try {
            final Pattern ptn = Pattern.compile(typeName + "\\..*htm.*",
                    Pattern.CASE_INSENSITIVE);
            folder.accept(new IResourceVisitor() {
                public boolean visit(IResource resource) throws CoreException {
                    if (ptn.matcher(resource.getName()).matches()
                            && resource instanceof IFile) {
                        // TODO :設定出来る様にする？
                        // WorkbenchUtil.selectAndReveal(resource);
                        WorkbenchUtil.openResource((IFile) resource);
                        return false;
                    }
                    return true;
                }
            }, IResource.DEPTH_ONE, false);
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }

}
