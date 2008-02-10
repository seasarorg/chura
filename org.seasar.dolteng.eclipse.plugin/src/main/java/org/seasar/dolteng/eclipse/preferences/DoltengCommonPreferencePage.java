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
package org.seasar.dolteng.eclipse.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.preferences.BooleanFieldEditor2;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;

/**
 * @author taichi
 */
public class DoltengCommonPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

    private Button btnDownload;
    private DirectoryFieldEditor fePath;
    
    public DoltengCommonPreferencePage() {
        super(GRID);
        setPreferenceStore(DoltengCore.getDefault().getPreferenceStore());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    @SuppressWarnings("restriction")
    protected void createFieldEditors() {
        BooleanFieldEditor2 feDownload = new BooleanFieldEditor2(
                Constants.PREF_DOWNLOAD_ONLINE,
                "Download resources from online", SWT.CHECK,
                getFieldEditorParent());
        addField(feDownload);

        fePath = new DirectoryFieldEditor(
                Constants.PREF_MAVEN_REPOS_PATH, "Maven Repository Path:",
                getFieldEditorParent());
        addField(fePath);

        btnDownload = feDownload.getChangeControl(getFieldEditorParent());
        btnDownload.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                updatePathFieldEnable(btnDownload.getSelection());
            }
        });
        
        if (Platform.getBundle("org.seasar.dolteng.projects.dependencies1") == null
                || Platform.getBundle("org.seasar.dolteng.projects.dependencies2") == null) {
            feDownload.setEnabled(false, getFieldEditorParent());
            btnDownload.setSelection(true);
            getPreferenceStore().setValue(Constants.PREF_DOWNLOAD_ONLINE, true);
        }
        
        updatePathFieldEnable(getPreferenceStore().getBoolean(Constants.PREF_DOWNLOAD_ONLINE));
    }

    private void updatePathFieldEnable(boolean download) {
        if(download) {
            fePath.setEnabled(true, getFieldEditorParent());
        } else {
            fePath.setEnabled(false, getFieldEditorParent());
        }
    }
}
