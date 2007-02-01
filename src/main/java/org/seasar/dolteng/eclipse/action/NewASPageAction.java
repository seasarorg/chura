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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.seasar.dolteng.core.template.TemplateExecutor;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.template.ASPageTemplateHandler;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wigets.ResourceTreeSelectionDialog;

/**
 * @author taichi
 * 
 */
public class NewASPageAction extends AbstractEditorActionDelegate {

    /**
     * 
     */
    public NewASPageAction() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.action.AbstractEditorActionDelegate#processResource(org.eclipse.core.resources.IProject,
     *      org.seasar.dolteng.eclipse.preferences.DoltengPreferences,
     *      org.eclipse.core.resources.IResource)
     */
    @Override
    protected void processResource(IProject project, DoltengPreferences pref,
            final IResource resource) throws Exception {
        if (resource.getType() != IResource.FILE) {
            return;
        }
        // DTO の選択ダイアログ
        ResourceTreeSelectionDialog dialog = new ResourceTreeSelectionDialog(
                WorkbenchUtil.getShell(), ProjectUtil.getWorkspaceRoot(),
                IResource.PROJECT | IResource.FOLDER | IResource.FILE);
        dialog.setTitle(Messages.SELECT_ACTION_SCRIPT_DTO);
        dialog.setAllowMultiple(false);
        dialog.setInitialSelection(resource.getParent());
        dialog.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement,
                    Object element) {
                if (element instanceof IFile) {
                    IFile file = (IFile) element;
                    return file.getFileExtension().endsWith("as");
                }
                return true;
            }
        });
        if (dialog.open() != Window.OK) {
            return;
        }
        Object[] selected = dialog.getResult();
        if (selected == null || selected.length < 1) {
            return;
        }
        if ((selected[0] instanceof IFile) == false) {
            return;
        }
        final IFile asdto = (IFile) selected[0];
        final IFile mxml = (IFile) resource;

        WorkbenchUtil.getWorkbenchWindow().run(false, false,
                new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
                        try {
                            // TemplateHandlerの生成
                            ASPageTemplateHandler handler = new ASPageTemplateHandler(
                                    mxml, asdto, monitor);
                            // TemplateExecutorの実行
                            TemplateExecutor executor = DoltengCore
                                    .getTemplateExecutor();
                            executor.proceed(handler);
                            IFile page = handler.getGenarated();
                            // 生成されたリソースへのPersistantProperty設定。(mxmlにBindingタグを埋めるのに使う。)
                            page.setPersistentProperty(
                                    Constants.PROP_FLEX_PAGE_DTO_PATH, asdto
                                            .getFullPath().toString());
                            WorkbenchUtil.openResource(page);
                        } catch (Exception e) {
                            DoltengCore.log(e);
                            throw new InvocationTargetException(e);
                        }
                    }
                });

    }
}
