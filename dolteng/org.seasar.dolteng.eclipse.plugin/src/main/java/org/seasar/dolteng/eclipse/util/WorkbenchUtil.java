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
package org.seasar.dolteng.eclipse.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Labels;

/**
 * @author taichi
 * 
 */
public class WorkbenchUtil {

    public static void selectAndReveal(IResource newResource) {
        IWorkbench workbench = PlatformUI.getWorkbench();
        BasicNewResourceWizard.selectAndReveal(newResource, workbench
                .getActiveWorkbenchWindow());
    }

    public static IEditorPart openResource(final IFile resource) {
        if (resource == null) {
            return null;
        }
        IWorkbenchWindow window = getWorkbenchWindow();
        if (window == null) {
            return null;
        }
        IWorkbenchPage activePage = window.getActivePage();
        if (activePage == null) {
            return null;
        }
        try {
            return IDE.openEditor(activePage, resource, true);
        } catch (PartInitException e) {
            DoltengCore.log(e);
        }
        return null;
    }

    public static IEditorPart getActiveEditor() {
        IWorkbenchWindow window = getWorkbenchWindow();
        if (window == null) {
            return null;
        }
        final IWorkbenchPage activePage = window.getActivePage();
        if (activePage == null) {
            return null;
        }
        return activePage.getActiveEditor();
    }

    public static IWorkbenchWindow getWorkbenchWindow() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow result = workbench.getActiveWorkbenchWindow();
        if (result == null && 0 < workbench.getWorkbenchWindowCount()) {
            IWorkbenchWindow[] ws = workbench.getWorkbenchWindows();
            result = ws[0];
        }
        return result;
    }

    public static Shell getShell() {
        IWorkbenchWindow window = getWorkbenchWindow();
        return window != null ? window.getShell() : new Shell(Display
                .getDefault());
    }

    public static void showMessage(String msg) {
        showMessage(msg, MessageDialog.INFORMATION);
    }

    public static void showMessage(String msg, int msgType) {
        MessageDialog dialog = new MessageDialog(getShell(),
                Labels.PLUGIN_NAME, null, msg, msgType,
                new String[] { IDialogConstants.OK_LABEL }, 0);
        dialog.open();
    }

    public static IViewPart findView(String viewId) {
        IViewPart vp = null;
        IWorkbenchWindow window = getWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                vp = page.findView(viewId);
            }
        }
        return vp;
    }

    public static IViewPart showView(String viewId) {
        IViewPart vp = null;
        try {
            IWorkbenchWindow window = getWorkbenchWindow();
            if (window != null) {
                IWorkbenchPage page = window.getActivePage();
                if (page != null) {
                    vp = page.showView(viewId);
                }
            }
        } catch (PartInitException e) {
            DoltengCore.log(e);
        }
        return vp;
    }

    public static int startWizard(IWizard wiz) {
        WizardDialog dialog = new WizardDialog(getShell(), wiz);
        return dialog.open();
    }

}
