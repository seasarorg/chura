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
package org.seasar.dolteng.eclipse.wigets;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.seasar.dolteng.eclipse.action.ActionRegistry;
import org.seasar.dolteng.eclipse.action.FindChildrenAction;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.dolteng.eclipse.viewer.TableTreeContentProvider;
import org.seasar.dolteng.eclipse.viewer.TableTreeViewer;
import org.seasar.dolteng.eclipse.viewer.TreeContentUtil;

/**
 * @author taichi
 * 
 */
public class TableDialog extends Dialog {

    private IJavaProject project;

    private TableTreeViewer viewer;

    private TableNode tableNode;

    private ActionRegistry registry;

    public TableDialog(Shell parentShell, IJavaProject project) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.project = project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite rootcomposite = (Composite) super.createDialogArea(parent);

        Composite composite = new Composite(rootcomposite, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(data);

        TableTreeContentProvider provider = new TableTreeContentProvider();
        viewer = new TableTreeViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.BORDER, provider);
        provider.initialize(this.project);

        this.registry = new ActionRegistry();
        this.registry.register(new FindChildrenAction(this.viewer));

        TreeContentUtil.hookDoubleClickAction(this.viewer, this.registry);
        TreeContentUtil.hookTreeEvent(this.viewer, this.registry);

        Tree tree = viewer.getTree();
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 400;
        gd.heightHint = 250;

        tree.setLayoutData(gd);
        tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button b = getButton(IDialogConstants.OK_ID);
                b.setEnabled(e.item.getData() instanceof TableNode);
            }
        });

        viewer.setInput(getShell());
        viewer.expandToLevel(2);
        viewer.refresh(true);

        return rootcomposite;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        IStructuredSelection selection = (IStructuredSelection) viewer
                .getSelection();
        Object obj = selection.getFirstElement();
        if (obj instanceof TableNode) {
            tableNode = (TableNode) obj;
            tableNode.findChildren();
            super.okPressed();
        }
    }

    /**
     * @return Returns the tableNode.
     */
    public TableNode getTableNode() {
        return tableNode;
    }

}