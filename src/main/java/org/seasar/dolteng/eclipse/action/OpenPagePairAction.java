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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wizard.NewPageWizard;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class OpenPagePairAction extends AbstractEditorActionDelegate {

    protected void processJava(IProject project,
            DoltengProjectPreferences pref, IJavaElement element) {
        if (element instanceof ICompilationUnit) {
            IFile file = DoltengProjectUtil.findHtmlByJava(project, pref,
                    (ICompilationUnit) element);
            WorkbenchUtil.openResource(file);
        }
    }

    protected void processResource(IProject project,
            DoltengProjectPreferences pref, IResource resource) {
        try {
            if (resource instanceof IFile) {
                IFile f = (IFile) resource;
                if (DoltengProjectUtil.isInViewPkg(f)) {
                    NamingConvention nc = pref.getNamingConvention();
                    String pkgName = DoltengProjectUtil.calculatePagePkg(
                            resource, pref);
                    String fqName = pkgName + "."
                            + getOpenTypeName(resource, nc);
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

    protected String getOpenTypeName(IResource html, NamingConvention nc) {
        String name = html.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        return getOpenTypeName(StringUtil.capitalize(name), nc);
    }

    protected String getOpenTypeName(String baseName, NamingConvention nc) {
        return baseName + nc.getPageSuffix();

    }
}
