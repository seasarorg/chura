package org.seasar.dolteng.eclipse.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wizard.NewQueryDtoWizard;

/**
 * @author taichi
 * 
 */
public class NewQueryDtoAction implements IActionDelegate {
    private IStructuredSelection selection;

    public NewQueryDtoAction() {
    }

    public void run(IAction action) {
        NewQueryDtoWizard wiz = new NewQueryDtoWizard();
        wiz.init(PlatformUI.getWorkbench(), selection);
        WorkbenchUtil.startWizard(wiz);
    }

    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection iss = (IStructuredSelection) selection;
            this.selection = iss;
        }

    }

}
