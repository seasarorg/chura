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
package org.seasar.dolteng.projects.wizard;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class KickstartProjectWizardPage extends WizardNewProjectCreationPage {

    private Text kickstartFile;

    private Listener validateListener = new Listener() {
        public void handleEvent(Event event) {
            boolean valid = validatePage();
            setPageComplete(valid);
        }
    };

    public KickstartProjectWizardPage() {
        super("[kickstart] ChuraProjectWizard");
        setTitle("[kickstart] " + Labels.WIZARD_CHURA_PROJECT_TITLE);
        setDescription(Messages.CHURA_PROJECT_DESCRIPTION);
        setImageDescriptor(Images.SEASAR);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        Composite composite = (Composite) getControl();

        Label label = new Label(composite, SWT.NONE);
        label.setText(Labels.WIZARD_PAGE_KICKSTART_FILE_PATH);
        label.setFont(parent.getFont());

        kickstartFile = new Text(composite, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 250;
        kickstartFile.setLayoutData(gd);
        kickstartFile.setFont(parent.getFont());
        kickstartFile.addListener(SWT.Modify, validateListener);
    }

    @Override
    protected boolean validatePage() {
        if(super.validatePage() == false) {
            return false;
        }
        
        String filename = getKickstartFileName();
        if (StringUtil.isEmpty(filename) || new File(filename).exists() == false) {
            setErrorMessage(Messages.FILE_NOT_FOUND);
            setPageComplete(false);
            return false;
        }
        
        setErrorMessage(null);
        setMessage(null);
        return true;
    }

    public String getKickstartFileName() {
        if (kickstartFile == null) {
            return "";
        }
        return kickstartFile.getText();
    }
}
