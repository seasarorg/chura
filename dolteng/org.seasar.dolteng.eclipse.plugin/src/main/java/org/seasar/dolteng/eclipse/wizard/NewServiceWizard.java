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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jdt.ui.wizards.NewInterfaceWizardPage;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.operation.AddServiceOperation;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;

/**
 * @author taichi
 * 
 */
public class NewServiceWizard extends Wizard implements INewWizard {

    public static final String NAME = NewServiceWizard.class.getName();

    private ICompilationUnit injectionTarget;

    private NewInterfaceWizardPage interfaceWizardPage;

    private NewClassWizardPage classWizardPage;

    /**
     * 
     */
    public NewServiceWizard() {
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
        this.interfaceWizardPage = new NewInterfaceWizardPage();
        this.classWizardPage = new NewClassWizardPage();

        setUpWizardPages();

        addPage(this.interfaceWizardPage);
        addPage(this.classWizardPage);
    }

    private void setUpWizardPages() {
        try {
            IType type = this.injectionTarget.findPrimaryType();

            IPackageFragmentRoot root = ProjectUtil
                    .getFirstSrcPackageFragmentRoot(type.getJavaProject());
            IPackageFragment pkg = type.getPackageFragment();
            String serviceName = toServiceName(type);

            this.interfaceWizardPage.setPackageFragmentRoot(root, true);
            this.interfaceWizardPage.setPackageFragment(pkg, true);
            this.interfaceWizardPage.setTypeName(serviceName, true);

            this.classWizardPage.setPackageFragmentRoot(root, true);
            this.classWizardPage.setPackageFragment(root.getPackageFragment(pkg
                    .getElementName()
                    + ".impl"), false);
            this.classWizardPage.setTypeName(serviceName + "Impl", true);
            List infs = Arrays.asList(new String[] { pkg.getElementName() + "."
                    + serviceName });
            this.classWizardPage.setSuperInterfaces(infs, true);
        } catch (Exception e) {
            DoltengCore.log(e);
            throw new IllegalStateException();
        }
    }

    /**
     * @param type
     * @return
     */
    public static String toServiceName(IType type) {
        String serviceName = "";
        String typeName = type.getElementName();
        if (typeName.endsWith("Page")) {
            serviceName = typeName.substring(0, typeName.lastIndexOf("Page"))
                    + "Service";
        } else if (typeName.endsWith("Action")) {
            serviceName = typeName.substring(0, typeName.lastIndexOf("Action"))
                    + "Service";
        } else {
            serviceName = typeName + "Service";
        }
        return serviceName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish() {
        IRunnableWithProgress runnable = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                if (monitor == null) {
                    monitor = new NullProgressMonitor();
                }
                monitor.beginTask("Create Service ....", 3);
                try {
                    interfaceWizardPage.createType(new SubProgressMonitor(
                            monitor, 1));
                    classWizardPage.createType(new SubProgressMonitor(monitor,
                            1));
                    AddServiceOperation op = new AddServiceOperation(
                            injectionTarget, interfaceWizardPage
                                    .getCreatedType());
                    op.run(new SubProgressMonitor(monitor, 1));
                } catch (Exception e) {
                    DoltengCore.log(e);
                } finally {
                    monitor.done();
                }
            };
        };
        try {
            getContainer().run(false, false, runnable);

            WorkbenchUtil.openResource(interfaceWizardPage.getCreatedType());
            WorkbenchUtil.openResource(classWizardPage.getCreatedType());
            return true;
        } catch (Exception e) {
            DoltengCore.log(e);
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
     *      org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    public void setInjectionTarget(ICompilationUnit unit) {
        this.injectionTarget = unit;
    }

}
