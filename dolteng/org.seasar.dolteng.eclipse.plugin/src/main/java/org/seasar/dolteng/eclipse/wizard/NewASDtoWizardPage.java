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
package org.seasar.dolteng.eclipse.wizard;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.wigets.ContainerSelectionGroup;

/**
 * @author taichi
 * 
 */
public class NewASDtoWizardPage extends WizardPage implements Listener {

    private IContainer initialSelection;

    private ContainerSelectionGroup group;

    /**
     * @param pageName
     * @param title
     * @param titleImage
     */
    public NewASDtoWizardPage(String pageName, String title,
            ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
    }

    /**
     * @param pageName
     */
    public NewASDtoWizardPage(String pageName) {
        super(pageName);
    }

    public NewASDtoWizardPage() {
        super("NewASDtoWizardPage");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        // top level group
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));

        group = new ContainerSelectionGroup(composite, this, true, "", false);

        group.setSelectedContainer(initialSelection);

        setErrorMessage(null);
        setMessage(null);
        setControl(composite);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
        validate();
    }

    public IPath getContainerFullPath() {
        return this.group.getContainerFullPath();
    }

    public void validate() {
        setPageComplete(false);
        IPath p = getContainerFullPath();
        if (p != null) {
            IWorkspaceRoot root = ProjectUtil.getWorkspaceRoot();
            IResource r = root.findMember(p);
            if (r instanceof IContainer) {
                setErrorMessage(null);
                setPageComplete(true);
                return;
            }
        }
        setErrorMessage(Messages.SELECT_FOLDER);
    }

    public void setInitialSelection(IContainer container) {
        initialSelection = container;
    }
}
