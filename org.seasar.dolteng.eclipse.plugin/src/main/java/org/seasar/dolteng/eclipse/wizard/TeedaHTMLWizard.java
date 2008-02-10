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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.framework.convention.NamingConvention;

/**
 * @author taichi
 * 
 */
public class TeedaHTMLWizard extends BasicNewResourceWizard implements
        INewWizard {

    private TeedaHTMLWizardPage mainPage;

    /**
     * 
     */
    public TeedaHTMLWizard() {
        super();
        setNeedsProgressMonitor(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        mainPage = new TeedaHTMLWizardPage(getSelection());

        IStructuredSelection selection = getSelection();
        if (selection != null) {
            Object selected = selection.getFirstElement();
            IProject project = ProjectUtil.getProject(selected);
            DoltengPreferences pref = DoltengCore
                    .getPreferences(project);
            if (pref != null) {
                NamingConvention nc = pref.getNamingConvention();
                if (nc != null) {
                    IFolder f = project
                            .getFolder(new Path(pref.getWebContentsRoot())
                                    .append(nc.getViewRootPath()));
                    if (f != null && f.exists()) {
                        IPath pp = f.getFullPath();
                        IResource r = ResourcesUtil.toResource(selected);
                        if (r != null) {
                            IPath rp = r.getFullPath();
                            if (pp.isPrefixOf(rp)) {
                                pp = rp;
                            }
                        }
                        mainPage.setContainerFullPath(pp);
                    }
                }
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
        IFile file = this.mainPage.createNewFile();
        if (file != null) {
            WorkbenchUtil.openResource(file);
            return true;
        }
        return false;
    }

}
