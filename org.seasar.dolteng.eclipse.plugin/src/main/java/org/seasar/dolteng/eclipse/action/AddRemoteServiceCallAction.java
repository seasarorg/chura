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
package org.seasar.dolteng.eclipse.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.operation.AddRemoteServiceCallJob;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wigets.ServiceMethodSelectionDialog;
import org.seasar.eclipse.common.util.ResouceUtil;

/**
 * @author taichi
 * 
 */
public class AddRemoteServiceCallAction implements
        IWorkbenchWindowActionDelegate {

    // FIXME : 未完成

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        IResource resource = ResouceUtil.getCurrentSelectedResouce();
        if (resource == null) {
            return;
        }
        IProject project = resource.getProject();

        DoltengPreferences pref = DoltengCore.getPreferences(project);
        if (pref == null) {
            return;
        }

        if ("as".equals(resource.getFileExtension())) {
            IFile as = (IFile) resource;
            ServiceMethodSelectionDialog dialog = new ServiceMethodSelectionDialog(
                    WorkbenchUtil.getShell(), as);
            if (dialog.open() == Dialog.OK) {
                String[] selected = dialog.getSelectedMethods();
                AddRemoteServiceCallJob job = new AddRemoteServiceCallJob(as,
                        selected);
                job.schedule();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

}
