package org.seasar.dolteng.eclipse.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class NewDTOActionDelegate implements IObjectActionDelegate {

    /**
     * Constructor for Action1.
     */
    public NewDTOActionDelegate() {
        super();
    }

    /**
     * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    /**
     * @see IActionDelegate#run(IAction)
     */
    public void run(IAction action) {
        Shell shell = new Shell();
        System.out.println("action isChecked : " + action.isChecked());
        System.out.println("action isEnabled : " + action.isEnabled());
        MessageDialog.openInformation(shell, "Dolteng Plug-in",
                "GenerateNewDTO was executed.");
    }

    /**
     * @see IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

}
