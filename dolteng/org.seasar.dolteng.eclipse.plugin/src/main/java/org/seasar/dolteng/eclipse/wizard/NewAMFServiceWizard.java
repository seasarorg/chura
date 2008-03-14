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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jdt.ui.wizards.NewInterfaceWizardPage;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class NewAMFServiceWizard extends BasicNewResourceWizard {

    public static final String NAME = NewAMFServiceWizard.class.getName();

    private IFile mxml;

    private NewInterfaceWizardPage mainPage;

    private NewClassWizardPage implPage;

    /**
     * 
     */
    public NewAMFServiceWizard() {
        super();
        setNeedsProgressMonitor(true);
        setDialogSettings(DoltengCore.getDefault().getDialogSettings().getSection(NAME));
    }

    public void setCallerMxml(IFile mxml) {
        this.mxml = mxml;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        mainPage = new NewInterfaceWizardPage();
        implPage = new NewAMFServiceWizardPage();

        mainPage.init(StructuredSelection.EMPTY);
        implPage.init(StructuredSelection.EMPTY);

        if (this.mxml != null) {
            // FlexBuilderによるプロジェクトと、Churaプロジェクトは、同一であると仮定する。
            IJavaProject javap = JavaCore.create(this.mxml.getProject());
            DoltengPreferences pref = DoltengCore.getPreferences(javap);
            NamingConvention nc = pref.getNamingConvention();

            IPackageFragmentRoot root = ProjectUtil
                    .getDefaultSrcPackageFragmentRoot(javap);
            if (pref != null && root != null && root.exists()) {
                mainPage.setPackageFragmentRoot(root, true);
                implPage.setPackageFragmentRoot(root, true);

                String baseName = StringUtil.capitalize(this.mxml.getFullPath()
                        .removeFileExtension().lastSegment());
                StringBuffer stb = new StringBuffer();
                String pn = pref.getDefaultRootPackageName() + '.'
                        + nc.getSubApplicationRootPackageName() + '.'
                        + mxml.getParent().getName().toLowerCase();
                stb.append(pn);
                IPackageFragment pf = root.getPackageFragment(pn);
                mainPage.setPackageFragment(pf, true);
                pf = root.getPackageFragment(pn + '.'
                        + nc.getImplementationPackageName());
                implPage.setPackageFragment(pf, true);
                String typename = baseName + nc.getServiceSuffix();
                stb.append('.');
                stb.append(typename);
                mainPage.setTypeName(typename, false);
                implPage.setTypeName(typename + nc.getImplementationSuffix(),
                        false);
                implPage.addSuperInterface(stb.toString());
            }

        }
        addPage(mainPage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        IRunnableWithProgress progress = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) {
                try {
                    mainPage.createType(monitor);
                    implPage.createType(new NullProgressMonitor());
                } catch (Exception e) {
                    DoltengCore.log(e);
                }
            }
        };

        try {
            if (finishPage(progress)) {
                JavaUI.openInEditor(implPage.getCreatedType());
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

}
