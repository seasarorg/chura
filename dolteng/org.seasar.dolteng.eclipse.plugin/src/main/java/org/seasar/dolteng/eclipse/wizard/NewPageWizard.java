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
package org.seasar.dolteng.eclipse.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class NewPageWizard extends Wizard implements INewWizard {

    private IFile resource;

    private NewPageWizardPage wizardPage;

    private PageMappingPage mappingPage;

    private IJavaProject project;

    /**
     * 
     */
    public NewPageWizard() {
        super();
        setNeedsProgressMonitor(true);
        setDialogSettings(DoltengCore.getDialogSettings());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    public void addPages() {
        super.addPages();
        try {
            this.mappingPage = new PageMappingPage(this.resource);
            this.wizardPage = new NewPageWizardPage(this.mappingPage);
            addPage(this.wizardPage);
            addPage(this.mappingPage);

            DoltengProjectPreferences pref = DoltengCore
                    .getPreferences(this.project);
            if (pref != null) {
                String viewRoot = pref.getRawPreferences().getString(
                        Constants.PREF_DEFAULT_VIEW_ROOT_PATH);
                IFolder rootFolder = this.project.getProject().getFolder(
                        pref.getWebContentsRoot());
                rootFolder = rootFolder.getFolder(viewRoot);
                IPath rootPath = rootFolder.getFullPath();
                IPath htmlPath = this.resource.getParent().getFullPath();
                String[] segroot = rootPath.segments();
                String[] seghtml = htmlPath.segments();
                StringBuffer stb = new StringBuffer(pref.getRawPreferences()
                        .getString(Constants.PREF_DEFAULT_WEB_PACKAGE));
                for (int i = (segroot.length - 1); i < seghtml.length; i++) {
                    stb.append('.');
                    stb.append(seghtml[i]);
                }

                IPackageFragmentRoot root = getFirstSrcPackageFragmentRoot(project);
                if (root != null) {
                    this.wizardPage.setPackageFragmentRoot(root, true);
                    IPackageFragment fragment = root.getPackageFragment(stb
                            .toString());
                    this.wizardPage.setPackageFragment(fragment, true);
                    this.wizardPage.setTypeName(StringUtil
                            .capitalize(this.resource.getFullPath()
                                    .removeFileExtension().lastSegment()),
                            false);
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
            throw new IllegalStateException();
        }
    }

    private IPackageFragmentRoot getFirstSrcPackageFragmentRoot(
            IJavaProject javap) throws CoreException {
        IPackageFragmentRoot[] roots = javap.getPackageFragmentRoots();
        for (int i = 0; roots != null && i < roots.length; i++) {
            IPackageFragmentRoot root = roots[i];
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                return root;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish() {
        IRunnableWithProgress progress = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                try {
                    if (monitor == null) {
                        monitor = new NullProgressMonitor();
                    }
                    wizardPage.createType(monitor);
                } catch (CoreException e) {
                    DoltengCore.log(e);
                    throw new InvocationTargetException(e);
                }
            }
        };

        if (finishPage(progress)) {
            IType pageType = wizardPage.getCreatedType();
            IResource pageRes = pageType.getCompilationUnit().getResource();
            if (pageRes != null) {
                WorkbenchUtil.selectAndReveal(pageRes);
                WorkbenchUtil.openResource((IFile) pageRes);
                DoltengCore.saveDialogSettings(getDialogSettings());
                return true;
            }
        }
        return false;
    }

    protected boolean finishPage(IRunnableWithProgress runnable) {
        IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(
                runnable);
        try {
            PlatformUI.getWorkbench().getProgressService().runInUI(
                    getContainer(), op,
                    ResourcesPlugin.getWorkspace().getRoot());

        } catch (InvocationTargetException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
     *      org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        Object o = selection.getFirstElement();
        if (o instanceof IFile) {
            IFile f = (IFile) o;
            IProject p = f.getProject();
            IJavaProject javap = JavaCore.create(p);
            if (javap.exists() && javap.isOpen()) {
                this.resource = f;
                this.project = javap;
            }
        }
    }
}