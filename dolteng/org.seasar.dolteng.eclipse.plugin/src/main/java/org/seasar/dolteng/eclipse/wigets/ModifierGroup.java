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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.seasar.dolteng.eclipse.nls.Labels;

/**
 * @author taichi
 * 
 */
public class ModifierGroup {

    protected Group group;

    protected List<ModifierSelectionListener> listeners = new ArrayList<ModifierSelectionListener>();

    public ModifierGroup(Composite parent, int style, boolean usePublicField) {
        this.group = new Group(parent, style);
        this.group.setLayout(new FillLayout(SWT.HORIZONTAL));
        createComposite(usePublicField);
    }

    protected void createComposite(boolean usePublicField) {
        this.group.setText(Labels.WIZARD_PAGE_FIELD_TYPE);
        Button privateRadio = new Button(this.group, SWT.RADIO);
        privateRadio.setText(Labels.WIZARD_PAGE_FIELD_PRIVATE);
        privateRadio.setSelection(!usePublicField);
        Button publicRadio = new Button(this.group, SWT.RADIO);
        publicRadio.setText(Labels.WIZARD_PAGE_FIELD_PUBLIC);
        publicRadio.setSelection(usePublicField);
        privateRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (ModifierSelectionListener listener : listeners) {
                    listener.privateSelected();
                }
            }
        });
        publicRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (ModifierSelectionListener listener : listeners) {
                    listener.publicSelected();
                }
            }
        });

    }

    public void setLayoutData(Object layoutData) {
        this.group.setLayoutData(layoutData);
    }

    public void add(ModifierSelectionListener listener) {
        this.listeners.add(listener);
    }

    public void remove(ModifierSelectionListener listener) {
        this.listeners.remove(listener);
    }

    public interface ModifierSelectionListener {

        void publicSelected();

        void privateSelected();
    }
}
