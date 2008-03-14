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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.seasar.dolteng.core.template.TemplateExecutor;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.template.ASDtoTemplateHandler;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;

/**
 * @author taichi
 * 
 */
public class NewASDtoWizard extends BasicNewResourceWizard {

    public static final String NAME = NewASDtoWizard.class.getName();

    private NewASDtoWizardPage mainPage;

    private ICompilationUnit compilationUnit;

    /**
     * 
     */
    public NewASDtoWizard() {
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
        mainPage = new NewASDtoWizardPage();
        mainPage.setTitle(Messages.SELECT_ACTION_SCRIPT_ROOT);
        DoltengPreferences pref = DoltengCore.getPreferences(compilationUnit
                .getJavaProject());
        if (pref != null) {
            IWorkspaceRoot root = ProjectUtil.getWorkspaceRoot();
            IPath p = pref.getFlexSourceFolderPath();
            IResource r = root.findMember(p);
            if (r instanceof IContainer) {
                IContainer c = (IContainer) r;
                if (c.exists()) {
                    mainPage.setInitialSelection(c);
                }
            } else {
                // 出力先が明確に定まらない時は、とりあえずプロジェクト直下に出力する。
                mainPage.setInitialSelection(this.compilationUnit
                        .getJavaProject().getProject());
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
        try {
            getContainer().run(false, false, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) {
                    IType type = compilationUnit.findPrimaryType();
                    String s = type.getPackageFragment().getElementName();
                    s = s.replace('.', '/');

                    ASDtoTemplateHandler handler = new ASDtoTemplateHandler(
                            compilationUnit, monitor, mainPage
                                    .getContainerFullPath().append(s));
                    TemplateExecutor executor = DoltengCore
                            .getTemplateExecutor();
                    executor.proceed(handler);
                    WorkbenchUtil.openResource(handler.getCreated());
                }
            });
        } catch (Exception e) {
            DoltengCore.log(e);
            return false;
        }
        return true;
    }

    /**
     * @param compilationUnit
     *            The compilationUnit to set.
     */
    public void setCompilationUnit(ICompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

}
