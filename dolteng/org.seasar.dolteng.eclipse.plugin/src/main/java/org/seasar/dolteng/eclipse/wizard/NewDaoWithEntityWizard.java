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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.wizards.NewInterfaceWizardPage;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.impl.ColumnNode;
import org.seasar.dolteng.eclipse.model.impl.ProjectNode;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.NameConverter;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.ClassUtil;

/**
 * @author taichi
 * 
 */
public class NewDaoWithEntityWizard extends Wizard implements INewWizard {

    public static final String NAME = NewDaoWithEntityWizard.class.getName();

    private IWorkbench workbench;

    private IStructuredSelection selection;

    private NewEntityWizardPage entityWizardPage;

    private EntityMappingPage mappingPage;

    private NewInterfaceWizardPage daoWizardPage;

    private TableNode currentSelection;

    private Map<String, WizardPageFactory> pageFactories = new HashMap<String, WizardPageFactory>();

    private static final WizardPageFactory DEFAULT_FACTORY = new S2DaoWizardPageFactory();

    /**
     * 
     */
    public NewDaoWithEntityWizard() {
        super();
        setNeedsProgressMonitor(true);
        setDefaultPageImageDescriptor(Images.ENTITY_WIZARD);
        setDialogSettings(DoltengCore.getDialogSettings());
        setWindowTitle(Labels.WIZARD_ENTITY_CREATION_TITLE);
        pageFactories.put(Constants.DAO_TYPE_S2DAO, DEFAULT_FACTORY);
        pageFactories.put(Constants.DAO_TYPE_KUINADAO,
                new KuinaDaoWizardPageFactory());
        pageFactories.put(Constants.DAO_TYPE_UUJI, new UujiWizardPageFactory());
    }

    private interface WizardPageFactory {

        EntityMappingPage createMappingPage(TableNode currentSelection);

        NewEntityWizardPage createNewEntityWizardPage(
                EntityMappingPage mappingPage);

        NewInterfaceWizardPage createDaoWizardPage(
                NewEntityWizardPage entityWizardPage,
                EntityMappingPage mappingPage);
    }

    private static class S2DaoWizardPageFactory implements WizardPageFactory {
        public EntityMappingPage createMappingPage(TableNode currentSelection) {
            return new EntityMappingPage(currentSelection, true);
        }

        public NewInterfaceWizardPage createDaoWizardPage(
                NewEntityWizardPage entityWizardPage,
                EntityMappingPage mappingPage) {
            return new NewDaoWizardPage(entityWizardPage, mappingPage);
        }

        public NewEntityWizardPage createNewEntityWizardPage(
                EntityMappingPage mappingPage) {
            return new NewEntityWizardPage(mappingPage);
        }
    }

    private static class KuinaDaoWizardPageFactory implements WizardPageFactory {
        public EntityMappingPage createMappingPage(TableNode currentSelection) {
            return new EntityMappingPage(currentSelection, false);
        }

        public NewInterfaceWizardPage createDaoWizardPage(
                NewEntityWizardPage entityWizardPage,
                EntityMappingPage mappingPage) {
            return new KuinaDaoWizardPage(entityWizardPage, mappingPage);
        }

        public NewEntityWizardPage createNewEntityWizardPage(
                EntityMappingPage mappingPage) {
            return new JPAEntityWizardPage(mappingPage);
        }
    }

    private static class UujiWizardPageFactory implements WizardPageFactory {
        public EntityMappingPage createMappingPage(TableNode currentSelection) {
            return new EntityMappingPage(currentSelection, false);
        }

        public NewInterfaceWizardPage createDaoWizardPage(
                NewEntityWizardPage entityWizardPage,
                EntityMappingPage mappingPage) {
            return new UujiWizardPage(mappingPage);
        }

        public NewEntityWizardPage createNewEntityWizardPage(
                EntityMappingPage mappingPage) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        WizardPageFactory factory = getWizardPageFactory();
        this.mappingPage = factory.createMappingPage(getCurrentSelection());
        this.mappingPage.createRows();
        this.entityWizardPage = factory.createNewEntityWizardPage(mappingPage);
        this.daoWizardPage = factory.createDaoWizardPage(entityWizardPage,
                mappingPage);

        if (this.entityWizardPage != null) {
            addPage(this.entityWizardPage);
            addPage(this.mappingPage);
        }
        addPage(this.daoWizardPage);

        String typeName = createDefaultTypeName();
        if (this.entityWizardPage != null) {
            this.entityWizardPage.init(getSelection());
            this.entityWizardPage.setTypeName(typeName, true);
            this.entityWizardPage.setCurrentSelection(this
                    .getCurrentSelection());
        }
        this.daoWizardPage.init(getSelection());

        ProjectNode pn = (ProjectNode) getCurrentSelection().getRoot();
        IJavaProject javap = pn.getJavaProject();
        DoltengPreferences pref = DoltengCore.getPreferences(javap);
        if (pref != null) {
            NamingConvention nc = pref.getNamingConvention();
            this.daoWizardPage.setTypeName(typeName + nc.getDaoSuffix(), true);
            IPackageFragmentRoot root = ProjectUtil
                    .getDefaultSrcPackageFragmentRoot(javap);
            if (root != null) {
                String rootPkg = pref.getDefaultRootPackageName();
                if (this.entityWizardPage != null) {
                    this.entityWizardPage.setPackageFragmentRoot(root, true);
                    this.entityWizardPage.setPackageFragment(root
                            .getPackageFragment(ClassUtil.concatName(rootPkg,
                                    nc.getEntityPackageName())), true);
                }
                if (this.daoWizardPage != null) {
                    this.daoWizardPage.setPackageFragmentRoot(root, true);
                    this.daoWizardPage.setPackageFragment(root
                            .getPackageFragment(ClassUtil.concatName(rootPkg,
                                    nc.getDaoPackageName())), true);
                }
            }
        }
    }

    private WizardPageFactory getWizardPageFactory() {
        TableNode node = getCurrentSelection();
        TreeContent tc = node.getRoot();
        if (tc instanceof ProjectNode) {
            ProjectNode pn = (ProjectNode) tc;
            DoltengPreferences pref = DoltengCore.getPreferences(pn
                    .getJavaProject());
            if (pref != null) {
                WizardPageFactory w = (WizardPageFactory) pageFactories
                        .get(pref.getDaoType());
                if (w != null) {
                    return w;
                }
            }
        }
        return DEFAULT_FACTORY;
    }

    public String createDefaultTypeName() {
        return NameConverter.toCamelCase(this.currentSelection.getText());
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
                    throws InvocationTargetException, InterruptedException {
                try {
                    monitor = ProgressMonitorUtil.care(monitor);
                    if (entityWizardPage != null) {
                        entityWizardPage.createType(monitor);
                    }
                    daoWizardPage.createType(monitor);
                } catch (CoreException e) {
                    DoltengCore.log(e);
                    throw new InvocationTargetException(e);
                }
            }
        };
        try {
            if (finishPage(progress)) {
                if (entityWizardPage != null) {
                    JavaUI.openInEditor(entityWizardPage.getCreatedType());
                }
                JavaUI.openInEditor(daoWizardPage.getCreatedType());
                DoltengCore.saveDialogSettings(getDialogSettings());
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
