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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.ActionScriptUtil;
import org.seasar.dolteng.eclipse.util.JavaElementUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.framework.convention.NamingConvention;

/**
 * @author taichi
 * 
 */
public class OpenAsPagePairAction extends AbstractWorkbenchWindowActionDelegate {
    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.action.AbstractEditorActionDelegate#processJava(org.eclipse.core.resources.IProject,
     *      org.seasar.dolteng.eclipse.preferences.DoltengPreferences,
     *      org.eclipse.jdt.core.IJavaElement)
     */
    @Override
    protected void processJava(IProject project, DoltengPreferences pref,
            IJavaElement element) throws Exception {
        if (Constants.VIEW_TYPE_FLEX2.equals(pref.getViewType()) == false) {
            return;
        }
        ICompilationUnit unit = JavaElementUtil.toCompilationUnit(element);
        if (unit == null) {
            return;
        }
        IType type = unit.findPrimaryType();
        String fqn = type.getFullyQualifiedName();
        NamingConvention nc = pref.getNamingConvention();
        for (String pkgName : nc.getRootPackageNames()) {
            if (fqn.startsWith(pkgName)) {
                String service = type.getElementName().replaceAll(
                        nc.getServiceSuffix() + "("
                                + nc.getImplementationSuffix() + ")?", "");
                IPath p = pref.getFlexSourceFolderPath();
                p = p.append(type.getPackageFragment().getElementName()
                        .replace('.', '/'));
                if (p.segment(p.segmentCount() - 1).equals(
                        nc.getImplementationPackageName())) {
                    p = p.removeLastSegments(1);
                }

                IWorkspaceRoot root = ProjectUtil.getWorkspaceRoot();
                IFile file = ResourcesUtil.findFile(service
                        + nc.getPageSuffix() + ".as", root.getFolder(p));
                if (file != null && file.exists()) {
                    WorkbenchUtil.openResource(file);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.action.AbstractEditorActionDelegate#processResource(org.eclipse.core.resources.IProject,
     *      org.seasar.dolteng.eclipse.preferences.DoltengPreferences,
     *      org.eclipse.core.resources.IResource)
     */
    @Override
    protected void processResource(IProject project, DoltengPreferences pref,
            IResource resource) throws Exception {
        if (Constants.VIEW_TYPE_FLEX2.equals(pref.getViewType()) == false) {
            return;
        }
        IType type = ActionScriptUtil.findAsPairType((IFile) resource);
        if (type != null && type.exists()) {
            JavaUI.openInEditor(type);
        }
    }
}
