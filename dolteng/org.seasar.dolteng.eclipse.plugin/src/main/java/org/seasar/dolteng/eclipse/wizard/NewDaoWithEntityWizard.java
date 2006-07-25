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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.NewInterfaceWizardPage;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.impl.ColumnNode;
import org.seasar.dolteng.eclipse.model.impl.ProjectNode;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class NewDaoWithEntityWizard extends Wizard implements INewWizard {

    private IWorkbench workbench;

    private IStructuredSelection selection;

    private NewEntityWizardPage entityWizardPage;

    private MetaDataMappingPage mappingPage;

    private NewInterfaceWizardPage daoWizardPage;

    private TableNode currentSelection;

    /**
     * 
     */
    public NewDaoWithEntityWizard() {
        super();
        setNeedsProgressMonitor(true);
        setDefaultPageImageDescriptor(Images.ENTITY_WIZARD);
        setDialogSettings(new DialogSettings("")); // FIXME : 保存先を用意する事。
        setWindowTitle(Labels.WIZARD_ENTITY_CREATION_TITLE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    public void addPages() {
        this.mappingPage = new MetaDataMappingPage(getCurrentSelection());
        this.entityWizardPage = new NewEntityWizardPage(this.mappingPage);
        if (isUseS2Dao()) {
            this.daoWizardPage = new NewDaoWizardPage(this.entityWizardPage,
                    this.mappingPage);
        } else {
            this.daoWizardPage = new KuinaDaoWizardPage(this.entityWizardPage,
                    this.mappingPage);
        }

        addPage(this.entityWizardPage);
        addPage(this.mappingPage);
        addPage(this.daoWizardPage);

        this.entityWizardPage.init(getSelection());
        String typeName = createDefaultTypeName();
        this.entityWizardPage.setTypeName(typeName, true);
        this.entityWizardPage.setCurrentSelection(this.getCurrentSelection());
        this.daoWizardPage.init(getSelection());
        this.daoWizardPage.setTypeName(typeName + "Dao", true);

        ProjectNode pn = (ProjectNode) getCurrentSelection().getRoot();
        IJavaProject javap = pn.getJavaProject();
        try {
            DoltengProjectPreferences pref = DoltengCore.getPreferences(javap);
            IPackageFragmentRoot[] roots = javap.getPackageFragmentRoots();
            if (pref != null && roots != null && 0 < roots.length) {
                IPackageFragmentRoot root = null;
                for (int i = 0; i < roots.length; i++) {
                    root = roots[i];
                    if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                        break;
                    }
                }
                if (root != null) {
                    this.entityWizardPage.setPackageFragment(
                            root.getPackageFragment(pref
                                    .getDefaultEntityPackage()), true);
                    this.daoWizardPage.setPackageFragment(root
                            .getPackageFragment(pref.getDefaultDaoPackage()),
                            true);
                }
            }
        } catch (JavaModelException e) {
            DoltengCore.log(e);
        }
    }

    private boolean isUseS2Dao() {
        TableNode node = getCurrentSelection();
        TreeContent tc = node.getRoot();
        if (tc instanceof ProjectNode) {
            ProjectNode pn = (ProjectNode) tc;
            return isUseS2Dao(pn.getJavaProject());
        }
        return false;
    }

    private boolean isUseS2Dao(IJavaProject javap) {
        DoltengProjectPreferences pref = DoltengCore.getPreferences(javap);
        if (pref != null) {
            return pref.isUseS2Dao();
        }
        return false;
    }

    public String createDefaultTypeName() {
        String name = this.currentSelection.getText().toLowerCase();
        StringBuffer stb = new StringBuffer();
        String[] ary = name.split("_");
        for (int i = 0; i < ary.length; i++) {
            stb.append(StringUtil.capitalize(ary[i]));
        }
        return stb.toString();
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
                    entityWizardPage.createType(monitor);
                    daoWizardPage.createType(monitor);
                } catch (CoreException e) {
                    DoltengCore.log(e);
                    throw new InvocationTargetException(e);
                }
            }
        };
        if (finishPage(progress)) {
            IType entity = entityWizardPage.getCreatedType();
            IType dao = daoWizardPage.getCreatedType();
            IResource entityRes = entity.getCompilationUnit().getResource();
            IResource daoRes = dao.getCompilationUnit().getResource();
            if (entityRes != null && daoRes != null) {
                WorkbenchUtil.selectAndReveal(entityRes);
                WorkbenchUtil.selectAndReveal(daoRes);
                WorkbenchUtil.openResource((IFile) entityRes);
                WorkbenchUtil.openResource((IFile) daoRes);
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

    /**
     * @return Returns the currentSelection.
     */
    public TableNode getCurrentSelection() {
        return this.currentSelection;
    }

    /**
     * @param currentSelection
     *            The currentSelection to set.
     */
    public void setCurrentSelection(TableNode currentSelection) {
        this.currentSelection = currentSelection;
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

        // FIXME : イマイチ。もう少しスマートなコードを考える事。
        Object elem = selection.getFirstElement();
        if (elem instanceof ColumnNode) {
            TreeContent tc = (ColumnNode) elem;
            setCurrentSelection((TableNode) tc.getParent());
        } else if (elem instanceof TableNode) {
            setCurrentSelection((TableNode) elem);
        }
    }

    /**
     * @return Returns the selection.
     */
    public IStructuredSelection getSelection() {
        return this.selection;
    }

    /**
     * @return Returns the workbench.
     */
    public IWorkbench getWorkbench() {
        return this.workbench;
    }

}
