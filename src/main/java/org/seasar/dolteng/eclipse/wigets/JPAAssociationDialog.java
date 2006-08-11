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
package org.seasar.dolteng.eclipse.wigets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.seasar.dolteng.eclipse.ast.JPAAssociationElements;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class JPAAssociationDialog extends TitleAreaDialog {

    private static final List optionalEnable = Arrays.asList(new String[] {
            "ManyToOne", "OneToOne" });

    private static final List mappedByEnable = Arrays.asList(new String[] {
            "OneToOne", "OneToMany", "ManyToMany" });

    private JPAAssociationElements elements;

    private Combo name;

    private Text targetEntity;

    private List cascade = new ArrayList();

    private List fetch = new ArrayList();

    private Button optional;

    private Text mappedBy; // TODO マッピング先のクラスをパースしてフィールド名をコンボで出す。

    /**
     * @param parentShell
     */
    public JPAAssociationDialog(Shell parentShell) {
        super(parentShell);
    }

    public void setElements(JPAAssociationElements elements) {
        this.elements = elements;
    }

    public JPAAssociationElements getElements() {
        return this.elements;
    }

    public JPAAssociationElements toElements() {
        JPAAssociationElements je = new JPAAssociationElements();
        je.setExists(this.elements.isExists());
        je.setName("javax.persistence." + name.getText());
        je.setTargetEntity(targetEntity.getText());
        je.getCascade().clear();
        for (Iterator i = cascade.iterator(); i.hasNext();) {
            Button b = (Button) i.next();
            if (b.getSelection()) {
                je.getCascade().add(b.getData());
            }
        }
        for (Iterator i = fetch.iterator(); i.hasNext();) {
            Button b = (Button) i.next();
            if (b.getSelection()) {
                je.setFetch(b.getData().toString());
                break;
            }
        }
        if (optional.isEnabled()) {
            je.setOptional(optional.getSelection());
        }
        if (mappedBy.isEnabled()) {
            je.setMappedBy(mappedBy.getText());
        }
        return je;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite rootComposite = (Composite) super.createDialogArea(parent);

        setTitle(Labels.JPA_ASSOCIATION_DIALOG_TITLE);

        Composite composite = createMainLayout(rootComposite);

        // アノテーション名、プルダウン
        createLabel(composite, Labels.JPA_ASSOCIATION_DIALOG_ANNOTATION_NAME);
        this.name = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        this.name.setLayoutData(createGridData());
        this.name.setItems(new String[] { "ManyToOne", "OneToOne", "OneToMany",
                "ManyToMany" });
        this.name.select(0);
        this.name.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleEnabled();
            }
        });

        // targetEntity クラス名検索ボタン+テキストエリア
        createLabel(composite, Labels.JPA_ASSOCIATION_DIALOG_TARGETENTITY);
        this.targetEntity = new Text(composite, SWT.BORDER);
        GridData data = createGridData();
        data.widthHint = 100;
        this.targetEntity.setLayoutData(data);

        // cascade チェックボックスグループ
        createLabel(composite, Labels.JPA_ASSOCIATION_DIALOG_CASCADE);
        Composite comp = createFillLayout(composite);
        String[] cascades = { "ALL", "PERSIST", "MERGE", "REMOVE", "REFRESH" };
        for (int i = 0; i < cascades.length; i++) {
            Button chk = new Button(comp, SWT.CHECK);
            chk.setText(cascades[i]);
            chk.setData(cascades[i]);
            this.cascade.add(chk);
        }

        // fetch ラジオボタングループ
        createLabel(composite, Labels.JPA_ASSOCIATION_DIALOG_FETCH);
        comp = createFillLayout(composite);

        String[] fetches = { "EAGER", "LAZY" };
        for (int i = 0; i < fetches.length; i++) {
            Button btn = new Button(comp, SWT.RADIO);
            btn.setText(fetches[i]);
            btn.setData(fetches[i]);
            this.fetch.add(btn);
        }

        // optional チェックボックス(ManyToOne,OneToOne)
        createLabel(composite, Labels.JPA_ASSOCIATION_DIALOG_OPTIONAL);
        this.optional = new Button(composite, SWT.CHECK);
        this.optional.setLayoutData(createGridData());

        // mappedBy テキスト入力エリア(OneToOne,OneToMany,ManyToMany)
        createLabel(composite, Labels.JPA_ASSOCIATION_DIALOG_MAPPEDBY);
        this.mappedBy = new Text(composite, SWT.BORDER);
        data = createGridData();
        data.widthHint = 100;
        this.mappedBy.setLayoutData(data);

        Label separator = new Label(rootComposite, SWT.HORIZONTAL
                | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        setUpDefaultValues();
        handleEnabled();

        return composite;
    }

    private void handleEnabled() {
        optional.setEnabled(optionalEnable.contains(name.getText()));
        mappedBy.setEnabled(mappedByEnable.contains(name.getText()));

    }

    private Composite createMainLayout(Composite rootComposite) {
        Composite composite = new Composite(rootComposite, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite.setLayout(layout);

        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        composite.setLayoutData(data);
        return composite;
    }

    private Composite createFillLayout(Composite parent) {
        Group composite = new Group(parent, SWT.NULL);
        FillLayout layout = new FillLayout();
        composite.setLayout(layout);

        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 2;
        composite.setLayoutData(data);

        return composite;
    }

    protected Label createLabel(Composite parent, String s) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(s);
        return label;
    }

    protected GridData createGridData() {
        GridData gd = new GridData();
        gd.verticalAlignment = GridData.FILL;
        gd.horizontalAlignment = GridData.FILL;
        gd.horizontalSpan = 2;
        return gd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        Button btn = createButton(parent, IDialogConstants.BACK_ID,
                "Restore &Defaults", false);
        btn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setUpDefaultValues();
            }
        });

        super.createButtonsForButtonBar(parent);
    }

    protected void setUpDefaultValues() {
        if (this.elements != null) {
            String name = ClassUtil.getShortClassName(this.elements.getName());
            if (StringUtil.isEmpty(name) == false) {
                this.name.setText(name);
            }
            this.targetEntity.setText(this.elements.getTargetEntity());
            for (Iterator i = this.cascade.iterator(); i.hasNext();) {
                Button b = (Button) i.next();
                b
                        .setSelection(this.elements.getCascade().contains(
                                b.getData()));
            }
            for (Iterator i = this.fetch.iterator(); i.hasNext();) {
                Button b = (Button) i.next();
                String v = b.getData().toString();
                if (v.equals("LAZY")
                        && StringUtil.isEmpty(this.elements.getFetch())) {
                    b.setSelection(true);
                } else {
                    b
                            .setSelection(v.equalsIgnoreCase(this.elements
                                    .getFetch()));
                }
            }
            if (this.optional.getEnabled()) {
                this.optional.setSelection(this.elements.isOptional());
            }
            if (this.mappedBy.getEnabled()) {
                this.mappedBy.setText(this.elements.getMappedBy());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        this.elements = toElements();
        super.okPressed();
    }

}
