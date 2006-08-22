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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class NewPageWizard extends Wizard implements INewWizard {

    private IFile resource;

    private NewPageWizardPage pagePage;

    private NewActionWizardPage actionPage;

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
            this.pagePage = new NewPageWizardPage(this.mappingPage);
            this.actionPage = new NewActionWizardPage(this.mappingPage);
            this.mappingPage.setWizardPage(this.pagePage);
            addPage(this.pagePage);
            addPage(this.actionPage);
            addPage(this.mappingPage);

            this.pagePage.init(null);
            this.actionPage.init(null);

            DoltengProjectPreferences pref = DoltengCore
                    .getPreferences(this.project);
            if (pref != null) {
                String pkgName = DoltengProjectUtil.calculatePagePkg(
                        this.resource, pref);

                IPackageFragmentRoot root = ProjectUtil
                        .getFirstSrcPackageFragmentRoot(project);
                if (root != null) {
                    String baseName = StringUtil.capitalize(this.resource
                            .getFullPath().removeFileExtension().lastSegment());

                    IPackageFragment fragment = root
                            .getPackageFragment(pkgName);

                    this.pagePage.setPackageFragmentRoot(root, true);
                    this.pagePage.setPackageFragment(fragment, true);
                    this.pagePage.setTypeName(baseName + "Page", false);

                    this.actionPage.setPackageFragmentRoot(root, true);
                    this.actionPage.setPackageFragment(fragment, true);
                    this.actionPage.setTypeName(baseName + "Action", false);
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
            throw new IllegalStateException();
        }
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
                    pagePage.createType(monitor);
                    if (pagePage.isSeparateAction()) {
                        actionPage.createType(monitor);
                    }
                } catch (CoreException e) {
                    DoltengCore.log(e);
                    throw new InvocationTargetException(e);
                }
            }
        };

        if (finishPage(progress)) {
            boolean is = false;
            IType pageType = pagePage.getCreatedType();
            IResource pageRes = pageType.getCompilationUnit().getResource();
            if (pageRes != null) {
                WorkbenchUtil.selectAndReveal(pageRes);
                WorkbenchUtil.openResource((IFile) pageRes);
                is = true;
            }

            if (this.pagePage.isSeparateAction()) {
                IType actionType = actionPage.getCreatedType();
                IResource actionRes = actionType.getCompilationUnit()
                        .getResource();
                if (actionRes != null) {
                    WorkbenchUtil.selectAndReveal(actionRes);
                    WorkbenchUtil.openResource((IFile) actionRes);
                    is = true;
                }
                DoltengCore.saveDialogSettings(getDialogSettings());
            }

            return is;
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
            init(f);
        }
    }

    public void init(IFile file) {
        IProject p = file.getProject();
        IJavaProject javap = JavaCore.create(p);
        if (javap.exists() && javap.isOpen()) {
            this.resource = file;
            this.project = javap;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
     */
    public IWizardPage getNextPage(IWizardPage page) {
        if (this.pagePage.isSeparateAction() == false
                && page instanceof NewPageWizardPage) {
            return this.mappingPage;
        }
        return super.getNextPage(page);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
     */
    public IWizardPage getPreviousPage(IWizardPage page) {
        if (this.pagePage.isSeparateAction() == false
                && page instanceof PageMappingPage) {
            return this.pagePage;
        }
        return super.getPreviousPage(page);
    }
}