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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.ClassUtil;

/**
 * @author taichi
 * 
 */
public class NewQueryDtoWizard extends Wizard implements INewWizard {

    public static final String NAME = NewQueryDtoWizard.class.getName();

    private IWorkbench workbench;

    private IStructuredSelection selection;

    private NewClassWizardPage mainPage;

    private ConnectionWizardPage configPage;

    private QueryDtoMappingPage mappingPage;

    /**
     * 
     */
    public NewQueryDtoWizard() {
        super();
        setNeedsProgressMonitor(true);
        setDialogSettings(DoltengCore.getDefault().getDialogSettings().getSection(NAME));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        configPage = new ConnectionWizardPage();
        mappingPage = new QueryDtoMappingPage();
        mainPage = new NewQueryDtoWizardPage(mappingPage);

        addPage(mainPage);
        addPage(configPage);
        addPage(mappingPage);

        configPage.init(selection);
        mappingPage.init(selection);
        mainPage.init(selection);

        Object adaptable = selection.getFirstElement();
        IProject project = ProjectUtil.getProject(adaptable);
        if (project != null && project.exists()) {
            DoltengPreferences pref = DoltengCore.getPreferences(project);
            if (pref != null) {
                IPackageFragmentRoot root = ProjectUtil
                        .getDefaultSrcPackageFragmentRoot(JavaCore
                                .create(project));
                if (root != null) {
                    NamingConvention nc = pref.getNamingConvention();
                    String pkgName = ClassUtil.concatName(pref
                            .getDefaultRootPackageName(), nc
                            .getDtoPackageName());
                    IPackageFragment fragment = root
                            .getPackageFragment(pkgName);
                    mainPage.setPackageFragmentRoot(root, true);
                    mainPage.setPackageFragment(fragment, true);
                }
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
                    monitor.beginTask(Messages.bind(Messages.PROCESS, mainPage
                            .getTypeName()), 5);
                    mainPage.createType(new SubProgressMonitor(monitor, 5));
                } catch (Exception e) {
                    DoltengCore.log(e);
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            if (finishPage(progress)) {
                JavaUI.openInEditor(mainPage.getCreatedType());
//                DoltengCore.saveDialogSettings(getDialogSettings());
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
            workbench.getProgressService().runInUI(getContainer(), op,
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
        this.workbench = workbench;
        this.selection = selection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
     */
    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        IWizardPage next = super.getNextPage(page);
        if (page instanceof ConnectionWizardPage
                && next instanceof QueryDtoMappingPage) {
            ConnectionWizardPage cwp = (ConnectionWizardPage) page;
            QueryDtoMappingPage qdmp = (QueryDtoMappingPage) next;
            qdmp.setConfig(cwp.getConfig());
        }
        return next;
    }
}
