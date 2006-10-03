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
package org.seasar.dolteng.eclipse.action;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.PlatformUI;
import org.seasar.dolteng.core.template.TemplateExecutor;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.impl.ColumnNode;
import org.seasar.dolteng.eclipse.model.impl.ProjectNode;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.template.DoltengTemplateHandler;
import org.seasar.dolteng.eclipse.util.SelectionUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;

/**
 * @author taichi
 * 
 */
public class NewScaffoldAction extends Action {

    public static final String ID = NewScaffoldAction.class.getName();

    private ISelectionProvider provider;

    /**
     * 
     */
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
    public void run() {
        Object elem = SelectionUtil.getCurrentSelection(this.provider);
        TableNode content = null;
        if (elem instanceof TableNode) {
            content = (TableNode) elem;
            content.findChildren();
        } else if (elem instanceof ColumnNode) {
            ColumnNode cn = (ColumnNode) elem;
            content = (TableNode) cn.getParent();
        }

        if (content != null
                && MessageDialog.openQuestion(WorkbenchUtil.getShell(),
                        Labels.PLUGIN_NAME, Messages.GENERATE_SCAFFOLD_CODES)) {
            runInWorkspace(content);
        }
    }

    private void runInWorkspace(final TableNode content) {
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                DoltengTemplateHandler handler = new DoltengTemplateHandler(
                        "scaffold", ((ProjectNode) content.getRoot())
                                .getJavaProject().getProject(), content,
                        monitor);
                TemplateExecutor executor = DoltengCore
                        .getTemplateExecutor("scaffold");
                executor.proceed(handler);
            }
        };
        try {
            PlatformUI.getWorkbench().getProgressService().runInUI(
                    WorkbenchUtil.getWorkbenchWindow(), op,
                    ResourcesPlugin.getWorkspace().getRoot());
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

}
