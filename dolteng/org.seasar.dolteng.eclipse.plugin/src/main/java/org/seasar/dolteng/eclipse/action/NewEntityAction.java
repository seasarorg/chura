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
package org.seasar.dolteng.eclipse.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.util.SelectionUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wizard.NewDaoWithEntityWizard;

/**
 * @author taichi
 * 
 */
public class NewEntityAction extends Action {

    public static final String ID = NewEntityAction.class.getName();

    private ISelectionProvider provider;

    public NewEntityAction(ISelectionProvider provider) {
        this.provider = provider;
        setId(ID);
        setText(Labels.ACTION_ENTITY_CREATION);
        setImageDescriptor(Images.GENERATE_CODE);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        try {
            Object elem = SelectionUtil.getCurrentSelection(this.provider);
            if (elem instanceof TreeContent) {
                NewDaoWithEntityWizard wiz = new NewDaoWithEntityWizard();
                wiz.init(PlatformUI.getWorkbench(),
                        (IStructuredSelection) this.provider.getSelection());
                WorkbenchUtil.startWizard(wiz);
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

}
