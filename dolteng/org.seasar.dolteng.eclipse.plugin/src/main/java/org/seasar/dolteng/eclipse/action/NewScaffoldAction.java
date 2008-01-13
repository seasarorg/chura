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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.seasar.dolteng.core.template.TemplateExecutor;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.impl.ColumnNode;
import org.seasar.dolteng.eclipse.model.impl.ProjectNode;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.scaffold.ScaffoldConfig;
import org.seasar.dolteng.eclipse.template.DoltengTemplateExecutor;
import org.seasar.dolteng.eclipse.template.ScaffoldTemplateHandler;
import org.seasar.dolteng.eclipse.util.SelectionUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wigets.OutputLocationDialog;

/**
 * @author taichi
 * 
 */
@SuppressWarnings("unchecked")
public class NewScaffoldAction extends Action {

    public static final String ID = NewScaffoldAction.class.getName();

    private ISelectionProvider provider;

    public NewScaffoldAction(ISelectionProvider provider) {
        super();
        this.provider = provider;
        setId(ID);
        setText(Labels.ACTION_SCAFFOLD_CREATION);
        setImageDescriptor(Images.GENERATE_CODE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        final TableNode content = getCurrentSelection();
        if (content != null) {
            IJavaProject javap = ((ProjectNode) content.getRoot())
                    .getJavaProject();
            final IProject project = javap.getProject();
            DoltengPreferences pref = DoltengCore.getPreferences(project);
            if (pref != null) {
                final OutputLocationDialog dialog = new OutputLocationDialog(
                        WorkbenchUtil.getShell(), javap);
                if (dialog.open() == Dialog.OK) {
                    final ScaffoldConfig config = dialog.getSelectedConfig();
                    if (config != null) {
                        WorkspaceJob job = new WorkspaceJob(
                                "Process Scaffold ....") {
                            @Override
                            public IStatus runInWorkspace(
                                    IProgressMonitor monitor) {
                                ScaffoldTemplateHandler handler = new ScaffoldTemplateHandler(
                                        config, project, content, monitor);
                                handler.setJavaSrcRoot(dialog.getRootPkg());
                                handler.setRootPkg(dialog.getRootPkgName());
                                TemplateExecutor executor = new DoltengTemplateExecutor(
                                        config.getResourceLoader());
                                executor.proceed(handler);
                                return Status.OK_STATUS;
                            }
                        };
                        job.schedule();
                    }
                }
            }
        }
    }

    protected TableNode getCurrentSelection() {
        Object elem = SelectionUtil.getCurrentSelection(this.provider);
        TableNode content = null;
        if (elem instanceof TableNode) {
            content = (TableNode) elem;
            content.findChildren();
        } else if (elem instanceof ColumnNode) {
            ColumnNode cn = (ColumnNode) elem;
            content = (TableNode) cn.getParent();
        }
        return content;
    }
}
