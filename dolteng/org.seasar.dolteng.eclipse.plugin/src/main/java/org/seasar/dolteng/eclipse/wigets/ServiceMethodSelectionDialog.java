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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.util.ActionScriptUtil;
import org.seasar.dolteng.eclipse.util.TreeContentAcceptor.TreeContentVisitor;
import org.seasar.dolteng.eclipse.viewer.AsServiceMethodTreeContentProvider;
import org.seasar.dolteng.eclipse.viewer.AsServiceMethodTreeViewer;

import uk.co.badgersinfoil.metaas.dom.ASCompilationUnit;
import uk.co.badgersinfoil.metaas.dom.ASMethod;
import uk.co.badgersinfoil.metaas.dom.ASType;

/**
 * @author taichi
 * 
 */
public class ServiceMethodSelectionDialog extends TitleAreaDialog {

    // FIXME : 未完成

    private CheckboxTreeViewer viewer;

    private IFile as;

    private IType service;

    private List<String> selectedMethods = new ArrayList<String>();

    public ServiceMethodSelectionDialog(Shell parent, IFile as) {
        super(parent);
        this.as = as;
        this.service = ActionScriptUtil.findAsPairType(as);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = createMainLayout((Composite) super
                .createDialogArea(parent));
        setTitle(Labels.ADD_DYNAMIC_PROPERTY_SELECT_ATTRIBUTE); // FIXME

        AsServiceMethodTreeContentProvider provider = new AsServiceMethodTreeContentProvider(
                service);
        ASCompilationUnit unit = ActionScriptUtil.parse(as);
        if (unit != null) {
            final ASType astype = unit.getType();
            provider.walk(new TreeContentVisitor() {
                public void visit(TreeContent content) {
                    ASMethod m = astype.getMethod(content.getText());
                    if (m != null) {
                        content.getParent().removeChild(content);
                    }
                }
            });
        }

        this.viewer = new AsServiceMethodTreeViewer(composite, provider);
        this.viewer.setInput(this.service);

        return composite;
    }

    private Composite createMainLayout(Composite rootComposite) {
        Composite composite = new Composite(rootComposite, SWT.NONE);
        composite.setLayout(new FillLayout());

        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = 300;
        composite.setLayoutData(data);
        return composite;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        for (Object checkedElement : viewer.getCheckedElements()) {
            TreeContent tc = (TreeContent) checkedElement;
            selectedMethods.add(tc.getText());
        }
        super.okPressed();
    }

    public String[] getSelectedMethods() {
        return selectedMethods.toArray(new String[selectedMethods
                .size()]);
    }
}
