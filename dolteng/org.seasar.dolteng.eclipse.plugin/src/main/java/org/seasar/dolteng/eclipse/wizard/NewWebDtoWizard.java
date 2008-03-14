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
package org.seasar.dolteng.eclipse.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.impl.ProjectNode;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class NewWebDtoWizard extends Wizard implements INewWizard {

    public static final String NAME = NewWebDtoWizard.class.getName();

    private NewWebDtoWizardPage dtoWizardPage;

    private DtoMappingPage mappingPage;

    private IStructuredSelection selection;

    private IProject project;

    private IFile htmlfile;

    private PageMappingPage parentMapper;

    private String dtoBaseName;

    /**
     * 
     */
    public NewWebDtoWizard() {
        super();
        setNeedsProgressMonitor(true);
        setDialogSettings(DoltengCore.getDefault().getDialogSettings().getSection(NAME));
    }

    public NewWebDtoWizard(IFile htmlfile, PageMappingPage parentMapper,
            String dtoBaseName) {
        this();
        this.htmlfile = htmlfile;
        this.parentMapper = parentMapper;
        this.dtoBaseName = dtoBaseName;
        this.project = htmlfile.getProject();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        mappingPage = new DtoMappingPage(this, htmlfile, parentMapper);
        dtoWizardPage = new NewWebDtoWizardPage(mappingPage);
        mappingPage.setWizardPage(dtoWizardPage);
        addPage(dtoWizardPage);
        addPage(mappingPage);
        dtoWizardPage.init(selection);
        DoltengPreferences pref = DoltengCore.getPreferences(this.project);
        if (pref != null) {
            NamingConvention nc = pref.getNamingConvention();
            IPackageFragmentRoot root = ProjectUtil
                    .getDefaultSrcPackageFragmentRoot(JavaCore.create(project));
            if (root != null) {

                String pkgName = DoltengProjectUtil.calculatePagePkg(
                        this.htmlfile, pref, ClassUtil.concatName(pref
                                .getDefaultRootPackageName(), nc
                                .getSubApplicationRootPackageName()));
                IPackageFragment fragment = root.getPackageFragment(pkgName);
                dtoWizardPage.setPackageFragmentRoot(root, true);
                dtoWizardPage.setPackageFragment(fragment, true);
                dtoWizardPage.setTypeName(StringUtil.capitalize(dtoBaseName)
                        + nc.getDtoSuffix(), true);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        IRunnableWithProgress progress = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException {
                try {
                    if (monitor == null) {
                        monitor = new NullProgressMonitor();
                    }
                    dtoWizardPage.createType(monitor);
                    mappingPage.reMapping();
                } catch (Exception e) {
                    DoltengCore.log(e);
                    throw new InvocationTargetException(e);
                }
            }
        };
        try {
            if (finishPage(progress)) {
                JavaUI.openInEditor(dtoWizardPage.getCreatedType());
//                DoltengCore.getDefault().setDialogSettings(getDialogSettings());
                return true;
            }
        } catch (Exception e) {
            DoltengCore.log(e);
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
        this.selection = selection;
        Object o = selection.getFirstElement();
        if (o instanceof IAdaptable) {
            IAdaptable a = (IAdaptable) o;
            IResource rs = (IResource) a.getAdapter(IResource.class);
            project = rs.getProject();
        }
        if (o instanceof TreeContent) {
            TreeContent t = (TreeContent) o;
            ProjectNode p = (ProjectNode) t.getRoot();
            project = p.getJavaProject().getProject();
        }
    }

    public IType getCreatedType() {
        return dtoWizardPage.getCreatedType();
    }
}
