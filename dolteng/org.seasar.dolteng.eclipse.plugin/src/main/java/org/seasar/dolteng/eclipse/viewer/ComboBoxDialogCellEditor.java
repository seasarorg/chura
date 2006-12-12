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
package org.seasar.dolteng.eclipse.viewer;

import java.text.MessageFormat;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author taichi
 * 
 */
public abstract class ComboBoxDialogCellEditor extends DialogCellEditor {

    /**
     * The list of items to present in the combo box.
     */
    protected String[] items;

    /**
     * The zero-based index of the selected item.
     */
    protected int selection = 0;

    /**
     * The custom combo box control.
     */
    protected CCombo comboBox;

    /**
     * 
     */
    public ComboBoxDialogCellEditor() {
        super();
    }

    /**
     * @param parent
     */
    public ComboBoxDialogCellEditor(Composite parent) {
        super(parent);
    }

    /**
     * @param parent
     * @param style
     */
    public ComboBoxDialogCellEditor(Composite parent, int style) {
        super(parent, style);
    }

    /**
     * Returns the list of choices for the combo box
     * 
     * @return the list of choices for the combo box
     */
    public String[] getItems() {
        return this.items;
    }

    /**
     * Sets the list of choices for the combo box
     * 
     * @param items
     *            the list of choices for the combo box
     */
    public void setItems(String[] items) {
        Assert.isNotNull(items);
        this.items = items;
        populateComboBoxItems();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.DialogCellEditor#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {

        comboBox = new CCombo(parent, getStyle());
        comboBox.setFont(parent.getFont());

        comboBox.addKeyListener(new KeyAdapter() {
            // hook key pressed - see PR 14201
            public void keyPressed(KeyEvent e) {
                keyReleaseOccured(e);
            }
        });

        comboBox.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent event) {
                applyEditorValueAndDeactivate();
            }

            public void widgetSelected(SelectionEvent event) {
                selection = comboBox.getSelectionIndex();
            }
        });

        comboBox.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE
                        || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });

        comboBox.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                ComboBoxDialogCellEditor.this.focusLost();
            }
        });
        return comboBox;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.DialogCellEditor#updateContents(java.lang.Object)
     */
    protected void updateContents(Object value) {
        // Noting to do.
    }

    /**
     * The <code>ComboBoxCellEditor</code> implementation of this
     * <code>CellEditor</code> framework method returns the zero-based index
     * of the current selection.
     * 
     * @return the zero-based index of the current selection wrapped as an
     *         <code>Integer</code>
     */
    protected Object doGetValue() {
        return new Integer(selection);
    }

    /**
     * The <code>ComboBoxCellEditor</code> implementation of this
     * <code>CellEditor</code> framework method accepts a zero-based index of
     * a selection.
     * 
     * @param value
     *            the zero-based index of the selection wrapped as an
     *            <code>Integer</code>
     */
    protected void doSetValue(Object value) {
        Assert.isTrue(comboBox != null && (value instanceof Integer));
        selection = ((Integer) value).intValue();
        comboBox.select(selection);
    }

    public LayoutData getLayoutData() {
        LayoutData layoutData = super.getLayoutData();
        if ((comboBox == null) || comboBox.isDisposed())
            layoutData.minimumWidth = 70;
        else {
            // make the comboBox 10 characters wide
            GC gc = new GC(comboBox);
            layoutData.minimumWidth = (gc.getFontMetrics()
                    .getAverageCharWidth() * 10) + 20;
            gc.dispose();
        }
        return layoutData;
    }

    /**
     * Updates the list of choices for the combo box for the current control.
     */
    private void populateComboBoxItems() {
        if (comboBox != null && items != null) {
            comboBox.removeAll();
            for (int i = 0; i < items.length; i++)
                comboBox.add(items[i], i);

            setValueValid(true);
            selection = 0;
        }
    }

    void applyEditorValueAndDeactivate() {
        // must set the selection before getting value
        selection = comboBox.getSelectionIndex();
        Object newValue = doGetValue();
        markDirty();
        boolean isValid = isCorrect(newValue);
        setValueValid(isValid);
        if (!isValid) {
            // try to insert the current value into the error message.
            setErrorMessage(MessageFormat.format(getErrorMessage(),
                    new Object[] { items[selection] }));
        }
        fireApplyEditorValue();
        deactivate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.CellEditor#focusLost()
     */
    protected void focusLost() {
        if (isActivated()) {
            applyEditorValueAndDeactivate();
        }
    }

    /**
     * @return should return Integer
     */
    protected abstract Object openDialogBox(Control cellEditorWindow);

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.CellEditor#keyReleaseOccured(org.eclipse.swt.events.KeyEvent)
     */
    protected void keyReleaseOccured(KeyEvent keyEvent) {
        if (keyEvent.character == '\u001b') { // Escape character
            fireCancelEditor();
        } else if (keyEvent.character == '\t') { // tab key
            applyEditorValueAndDeactivate();
        }
    }
}