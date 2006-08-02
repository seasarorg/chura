package org.seasar.dolteng.eclipse.part;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.seasar.dolteng.eclipse.action.ActionRegistry;
import org.seasar.dolteng.eclipse.action.ConnectionConfigAction;
import org.seasar.dolteng.eclipse.action.DeleteConnectionConfigAction;
import org.seasar.dolteng.eclipse.action.FindChildrenAction;
import org.seasar.dolteng.eclipse.action.NewEntityAction;
import org.seasar.dolteng.eclipse.action.RefreshDatabaseViewAction;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.util.SelectionUtil;
import org.seasar.dolteng.eclipse.viewer.ComparableViewerSorter;
import org.seasar.dolteng.eclipse.viewer.TreeContentLabelProvider;
import org.seasar.dolteng.eclipse.viewer.TreeContentProvider;
import org.seasar.dolteng.eclipse.viewer.TreeContentUtil;

/**
 * 
 * @author taichi
 * 
 */
public class DatabaseView extends ViewPart {
    private TreeViewer viewer;

    private ActionRegistry registry;

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
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        final TreeContentProvider tcp = new TreeContentProvider();
        viewer.setContentProvider(tcp);
        viewer.setLabelProvider(new TreeContentLabelProvider());
        viewer.setSorter(new ComparableViewerSorter());
        // Trick ...
        // AbstractLeafに実装されているequalsやhashCodeは、それぞれが属するNode内においてのみ、
        // 有効である様実装されている為。表示領域に対するイベントハンドリングでは、適切に動作しない為。
        viewer.setComparer(new IElementComparer() {
            public boolean equals(Object a, Object b) {
                return a == b;
            }

            public int hashCode(Object element) {
                return element.hashCode() ^ System.identityHashCode(element);
            }
        });
        viewer.setInput(getViewSite());

        this.registry = new ActionRegistry();
        makeActions();
        hookContextMenu();
        TreeContentUtil.hookDoubleClickAction(this.viewer, this.registry);
        TreeContentUtil.hookTreeEvent(this.viewer, this.registry);
        contributeToActionBars();

        Display disp = this.viewer.getControl().getDisplay();
        disp.asyncExec(new Runnable() {
            public void run() {
                TreeContent[] roots = (TreeContent[]) tcp.getElements(null);
                for (int i = 0; i < roots.length; i++) {
                    Event e = new Event();
                    e.data = roots[i];
                    registry.runWithEvent(FindChildrenAction.ID, e);
                }
                viewer.expandToLevel(2);
            }
        });
    }

    private void makeActions() {
        this.registry.register(new RefreshDatabaseViewAction(this.viewer));
        this.registry.register(new ConnectionConfigAction(this.viewer));
        this.registry.register(new DeleteConnectionConfigAction(this.viewer));
        this.registry.register(new FindChildrenAction(this.viewer));
        this.registry.register(new NewEntityAction(this.viewer));
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