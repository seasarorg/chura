package org.seasar.dolteng.eclipse.part;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.action.ActionRegistry;
import org.seasar.dolteng.eclipse.action.ConnectionConfigAction;
import org.seasar.dolteng.eclipse.action.DeleteConnectionConfigAction;
import org.seasar.dolteng.eclipse.action.FindChildrenAction;
import org.seasar.dolteng.eclipse.action.NewEntityAction;
import org.seasar.dolteng.eclipse.action.NewScaffoldAction;
import org.seasar.dolteng.eclipse.action.RefreshDatabaseViewAction;
import org.seasar.dolteng.eclipse.model.TreeContent;
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
     */
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
        Display disp = this.viewer.getControl().getDisplay();
        disp.asyncExec(new TreeContentInitializer(this.contentProvider));
    }

    public static void reloadView() {
        IViewPart part = WorkbenchUtil.findView(Constants.ID_DATABASE_VIEW);
        if (part instanceof DatabaseView) {
            DatabaseView dv = (DatabaseView) part;
            dv.contentProvider.dispose();
            dv.loadView();
        }
    }

    private class TreeContentInitializer implements Runnable {
        private TableTreeContentProvider tcp;

        public TreeContentInitializer(TableTreeContentProvider tcp) {
            this.tcp = tcp;
        }

        public void run() {
            tcp.initialize();
            TreeContent[] roots = (TreeContent[]) tcp.getElements(null);
            for (int i = 0; i < roots.length; i++) {
                Event e = new Event();
                e.data = roots[i];
                registry.runWithEvent(FindChildrenAction.ID, e);
            }
            viewer.expandToLevel(2);
            viewer.refresh(true);
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
        // manager.add(this.registry.find(RefreshDatabaseViewAction.ID));
        // manager.add(new Separator());
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    public ActionRegistry getActionRegistry() {
        return this.registry;
    }
}