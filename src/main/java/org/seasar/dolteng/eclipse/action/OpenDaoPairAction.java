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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wizard.NewOrmXmlWizard;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class OpenDaoPairAction extends AbstractEditorActionDelegate {

    private static final Pattern ormXmlSuffix = Pattern.compile(".*Orm\\.xml",
            Pattern.CASE_INSENSITIVE);

    /**
     * 
     */
    public OpenDaoPairAction() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.action.AbstractEditorActionDelegate#processJava(org.eclipse.core.resources.IProject,
     *      org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences,
     *      org.eclipse.jdt.core.IJavaElement)
     */
    protected void processJava(IProject project,
            DoltengProjectPreferences pref, IJavaElement element)
            throws Exception {
        if (element instanceof ICompilationUnit) {
            ICompilationUnit unit = (ICompilationUnit) element;
            IType type = unit.findPrimaryType();
            String name = type.getFullyQualifiedName();
            NamingConvention nc = pref.getNamingConvention();
            if (nc.isTargetClassName(name)) {
                boolean willbeOpen = false;
                String en = type.getElementName();
                String pkg = type.getPackageFragment().getElementName();
                String entityName = "";
                if (willbeOpen = nc.isTargetClassName(name, nc.getDaoSuffix())) {
                    entityName = en.substring(0, en.length() - 3);
                } else {
                    if (willbeOpen = pkg.endsWith(nc.getEntityPackageName())) {
                        entityName = en;
                    }
                }
                if (willbeOpen) {
                    IWorkspaceRoot workspaceRoot = project.getWorkspace()
                            .getRoot();
                    Pattern ormXml = Pattern.compile(entityName + "Orm\\.xml",
                            Pattern.CASE_INSENSITIVE);
                    if (findDir(project, new Path("META-INF"), workspaceRoot,
                            ormXml) == false) {
                        IPath path = new Path(pkg.replace('.', '/'));
                        if (findDir(project, path, workspaceRoot, ormXml) == false) {
                            NewOrmXmlWizard wiz = new NewOrmXmlWizard();
                            wiz
                                    .setContainerFullPath(pref
                                            .getOrmXmlOutputPath());
                            wiz.setEntityName(entityName);
                            WorkbenchUtil.startWizard(wiz);
                        }
                    }
                }
            }
        }
    }

    private boolean findDir(IProject project, IPath path,
            IWorkspaceRoot workspaceRoot, Pattern ormXml)
            throws JavaModelException, CoreException {

        IJavaProject javap = JavaCore.create(project);
        IPackageFragmentRoot[] roots = javap.getPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            IPackageFragmentRoot root = roots[i];
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                IPath p = root.getPath().append(path);
                if (workspaceRoot.exists(p)) {
                    if (findDir(workspaceRoot.getFolder(p), ormXml)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean findDir(IContainer dir, Pattern ormXml)
            throws CoreException {
        IResource[] files = dir.members(IResource.FILE);
        for (int j = 0; j < files.length; j++) {
            IResource file = files[j];
            if (file.getType() == IResource.FILE
                    && ormXml.matcher(file.getName()).matches()) {
                WorkbenchUtil.openResource((IFile) file);
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.action.AbstractEditorActionDelegate#processResource(org.eclipse.core.resources.IProject,
     *      org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences,
     *      org.eclipse.core.resources.IResource)
     */
    protected void processResource(IProject project,
            DoltengProjectPreferences pref, IResource resource)
            throws Exception {
        if (ormXmlSuffix.matcher(resource.getName()).matches()) {
            NamingConvention nc = pref.getNamingConvention();
            IJavaProject javap = JavaCore.create(project);
            String name = resource.getName();
            name = StringUtil.capitalize(name.substring(0, name.length() - 7));
            String[] names = nc.getRootPackageNames();
            for (int i = 0; i < names.length; i++) {
                String typeName = getOpenTypeName(names[i], name, nc);
                IType type = javap.findType(typeName);
                if (type != null && type.exists()) {
                    JavaUI.openInEditor(type);
                    break;
                }
            }
        }
    }

    protected String getOpenTypeName(String root, String entityName,
            NamingConvention nc) {
        String result = root + "." + nc.getDaoPackageName() + "." + entityName
                + nc.getDaoSuffix();
        return result;
    }
}