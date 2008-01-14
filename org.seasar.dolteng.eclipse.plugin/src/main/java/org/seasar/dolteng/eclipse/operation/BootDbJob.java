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
package org.seasar.dolteng.eclipse.operation;

import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;

/**
 * @author taichi
 * 
 */
public class BootDbJob extends WorkspaceJob {

    private IProject project;

    public BootDbJob(IProject project) {
        super("BootDbJob");
        this.project = project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) {
        monitor.beginTask("", 10);
        IWorkbench workbench = PlatformUI.getWorkbench();
        ICommandService service = (ICommandService) workbench
                .getAdapter(ICommandService.class);
        if (service != null) {
            Command cmd = service
                    .getCommand(Constants.ID_DB_LAUNCHER_START_SERVER_CMD);
            try {
                cmd.executeWithChecks(new ExecutionEvent(cmd,
                        Collections.EMPTY_MAP, null, project));
            } catch (Exception e) {
                DoltengCore.log(e);
            }
            monitor.worked(5);
            monitor.done();
        }
        return Status.OK_STATUS;
    }

    public static boolean enableFor(IProject project) {
        if (Platform.getBundle(Constants.ID_DB_LAUNCHER_PLUGIN) != null
                && ProjectUtil.hasNature(project,
                        Constants.ID_DB_LAUNCHER_NATURE)) {
            return MessageDialog.openConfirm(WorkbenchUtil.getShell(),
                    Constants.ID_DB_LAUNCHER_PLUGIN, "Start Db ?");
        }
        return false;
    }

}
