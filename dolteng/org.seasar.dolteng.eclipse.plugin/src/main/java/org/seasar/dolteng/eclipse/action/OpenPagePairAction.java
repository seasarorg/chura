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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wizard.NewPageWizard;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class OpenPagePairAction extends AbstractEditorActionDelegate {

    protected void processJava(IProject project,
            DoltengProjectPreferences pref, IJavaElement element) {
        if (element instanceof ICompilationUnit) {
            ICompilationUnit unit = (ICompilationUnit) element;
            IType type = unit.findPrimaryType();
            String typeName = type.getElementName();
            if (typeName.endsWith("Page")) {
                typeName = typeName.substring(0, typeName.indexOf("Page"));
            } else if (typeName.endsWith("Action")) {
                typeName = typeName.substring(0, typeName.indexOf("Action"));
            } else {
                return;
            }
            String pkg = type.getPackageFragment().getElementName();
            String webPkg = pref.getRawPreferences().getString(
                    Constants.PREF_DEFAULT_WEB_PACKAGE);
            if (pkg.startsWith(webPkg)) {
                pkg = pkg.substring(webPkg.length() + 1);
                pkg = pkg.replace('.', '/');
                IPath path = new Path(pref.getWebContentsRoot()).append(
                        pref.getRawPreferences().getString(
                                Constants.PREF_DEFAULT_VIEW_ROOT_PATH)).append(
                        pkg);
                IFolder folder = project.getFolder(path);
                if (folder.exists()) {
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

    protected void processResource(IProject project,
            DoltengProjectPreferences pref, IResource resource) {
        try {
            if (resource instanceof IFile) {
                IFile f = (IFile) resource;
                if (DoltengProjectUtil.isInViewPkg(f, pref)) {
                    String pkgName = DoltengProjectUtil.calculatePagePkg(
                            resource, pref);
                    String fqName = pkgName + "." + getOpenTypeName(resource);
                    IJavaProject javap = JavaCore.create(project);
                    IType type = javap.findType(fqName);
                    if (type != null && type.exists()) {
                        JavaUI.openInEditor(type);
                    } else {
                        NewPageWizard wiz = new NewPageWizard();
                        wiz.init(f);
                        WorkbenchUtil.startWizard(wiz);
                    }
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    protected String getOpenTypeName(IResource html) {
        String name = html.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        return getOpenTypeName(StringUtil.capitalize(name));
    }

    protected String getOpenTypeName(String baseName) {
        return baseName + "Page";

    }
}