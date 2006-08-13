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
package org.seasar.dolteng.eclipse.wizard;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class ChuraProjectWizardPage extends WizardNewProjectCreationPage {

    private Text rootPkgName;

    /**
     * @param pageName
     */
    public ChuraProjectWizardPage() {
        super("ChuraProjectWizard");
        setTitle(Labels.WIZARD_CHURA_PROJECT_TITLE);
        setDescription(Messages.CHURA_PROJECT_DESCRIPTION);
    }

    public void createControl(Composite parent) {
        super.createControl(parent);
        Composite composite = (Composite) getControl();
        createRootPackage(composite);
    }

    private void createRootPackage(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(composite, SWT.NONE);
        label.setText(Labels.WIZARD_PAGE_CHURA_ROOT_PACKAGE);
        label.setFont(parent.getFont());

        this.rootPkgName = new Text(composite, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        this.rootPkgName.setLayoutData(data);
        this.rootPkgName.setFont(parent.getFont());
        this.rootPkgName.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                setPageComplete(validatePage());
            }
        });
    }

    protected boolean validatePage() {
        String name = getRootPackageName();
        if (StringUtil.isEmpty(name)) {
            setErrorMessage(Messages.PACKAGE_NAME_IS_EMPTY);
            return false;
        }
        IStatus val = JavaConventions.validatePackageName(name);
        if (val.getSeverity() == IStatus.ERROR
                || val.getSeverity() == IStatus.WARNING) {
            String msg = NLS.bind(Messages.INVALID_PACKAGE_NAME, val
                    .getMessage());
            setErrorMessage(msg);
            return false;
        }

        return super.validatePage();
    }

    public String getRootPackageName() {
        if (rootPkgName == null) {
            return "";
        }
        return rootPkgName.getText();
    }
}
