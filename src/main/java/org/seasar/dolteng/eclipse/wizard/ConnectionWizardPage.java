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

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.dolteng.eclipse.preferences.impl.ConnectionConfigImpl;

/**
 * TODO : 未実装
 * 
 * @author taichi
 * 
 */
public class ConnectionWizardPage extends WizardPage {

    private static final String[] EXTENSIONS = new String[] { "*.jar", "*.zip" };

    private static final String[] CHARSETS = new String[] { "Shift_JIS",
            "EUC-JP", "MS932", "UTF-8" };

    private TableViewer driverPath;

    private Set driverPathList = new HashSet();

    private Combo driverClass;

    private Button driverFinder;

    private Text connectionUrl;

    private Text user;

    private Text pass;

    private Combo charset;

    public ConnectionWizardPage() {
        super("ConnectionWizardPage");
    }

    /**
     * @param pageName
     */
    public ConnectionWizardPage(String pageName) {
        super(pageName);
    }

    /**
     * @param pageName
     * @param title
     * @param titleImage
     */
    public ConnectionWizardPage(String pageName, String title,
            ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        setControl(composite);
    }

    public ConnectionConfig getConfig() {
        ConnectionConfigImpl cc = new ConnectionConfigImpl(
                new PreferenceStore());
        cc.setName(ConnectionConfig.class + "@"
                + String.valueOf(System.identityHashCode(cc)));
        cc.setDriverPaths(toDriverPathArray());
        cc.setDriverClass(this.driverClass.getText());
        cc.setConnectionUrl(this.connectionUrl.getText());
        cc.setUser(this.user.getText());
        cc.setPass(this.pass.getText());
        cc.setCharset(this.charset.getText());
        return cc;
    }

    private String[] toDriverPathArray() {
        String[] ary = (String[]) driverPathList
                .toArray(new String[driverPathList.size()]);
        for (int i = 0; i < ary.length; i++) {
            ary[i] = toEncodedPath(ary[i]);
        }
        return ary;
    }

    private String toEncodedPath(String path) {
        try {
            File f = new File(path);
            return f.toURI().toURL().getPath();
        } catch (MalformedURLException e) {
            return "";
        }
    }

}
