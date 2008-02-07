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
package org.seasar.dolteng.eclipse.part;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.WorkbenchJob;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.action.ActionRegistry;
import org.seasar.dolteng.eclipse.action.ConnectionConfigAction;
import org.seasar.dolteng.eclipse.action.DeleteConnectionConfigAction;
import org.seasar.dolteng.eclipse.action.FindChildrenAction;
import org.seasar.dolteng.eclipse.action.NewEntityAction;
import org.seasar.dolteng.eclipse.action.NewScaffoldAction;
import org.seasar.dolteng.eclipse.action.RefreshDatabaseViewAction;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.SelectionUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.viewer.TableTreeContentProvider;
import org.seasar.dolteng.eclipse.viewer.TableTreeViewer;
import org.seasar.dolteng.eclipse.viewer.TreeContentUtil;

/**
 * 
 * @author taichi
 * 
 */
public class DatabaseView extends ViewPart {
    private TreeViewer viewer;

    private ActionRegistry registry;

    private TableTreeContentProvider contentProvider;

    /**
     * The constructor.
     */
    public DatabaseView() {
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        this.contentProvider = new TableTreeContentProvider();
        viewer = new TableTreeViewer(parent, contentProvider);
        viewer.setInput(getViewSite());

        this.registry = new ActionRegistry();
        makeActions();
        hookContextMenu();
        TreeContentUtil.hookDoubleClickAction(this.viewer, this.registry);
        TreeContentUtil.hookTreeEvent(this.viewer, this.registry);
        contributeToActionBars();

        loadView();
    }

    private void loadView() {
        WorkbenchJob job = new WorkbenchJob("") {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                try {
                    monitor.beginTask("Reloading Database View ...", 100);
                    contentProvider.initialize();
                    ProgressMonitorUtil.isCanceled(monitor, 10);
                    TreeContent[] roots = (TreeContent[]) contentProvider
                            .getElements(null);
                    for (TreeContent root : roots) {
                        Event e = new Event();
                        e.data = root;
                        registry.runWithEvent(FindChildrenAction.ID, e);
                        ProgressMonitorUtil.isCanceled(monitor, 5);
                    }
                    ProgressMonitorUtil.isCanceled(monitor, 10);
                    viewer.refresh(true);
                    ProgressMonitorUtil.isCanceled(monitor, 10);
                } finally {
                    monitor.done();
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    public static void reloadView() {
        IViewPart part = WorkbenchUtil.findView(Constants.ID_DATABASE_VIEW);
        if (part instanceof DatabaseView) {
            DatabaseView dv = (DatabaseView) part;
            dv.contentProvider.dispose();
            dv.loadView();
        }
    }

    private void makeActions() {
        this.registry.register(new RefreshDatabaseViewAction(this.viewer));
        this.registry.register(new ConnectionConfigAction(this.viewer));
        this.registry.register(new DeleteConnectionConfigAction(this.viewer));
        this.registry.register(new FindChildrenAction(this.viewer));
        this.registry.register(new NewEntityAction(this.viewer));
        this.registry.register(new NewScaffoldAction(this.viewer));
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                Object element = SelectionUtil
                        .getCurrentSelection(DatabaseView.this.viewer);
                if (element instanceof TreeContent) {
                    TreeContent tc = (TreeContent) element;
                    tc.fillContextMenu(manager, DatabaseView.this.registry);
                    // Other plug-ins can contribute there actions here
                    manager.add(new Separator(
                            IWorkbenchActionConstants.MB_ADDITIONS));
                }
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        // manager.add(this.registry.find(RefreshDatabaseViewAction.ID));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        // manager.add(this.registry.find(ConnectionConfigAction.ID));
        // manager.add(this.registry.find(DeleteConnectionConfigAction.ID));
        manager.add(this.registry.find(RefreshDatabaseViewAction.ID));
        // manager.add(new Separator());
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    public ActionRegistry getActionRegistry() {
        return this.registry;
    }
}