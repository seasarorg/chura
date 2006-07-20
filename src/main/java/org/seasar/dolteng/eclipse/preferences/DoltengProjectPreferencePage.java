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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.util.ProjectUtil;

/**
 * @author taichi
 * 
 */
public class DoltengProjectPreferencePage extends PropertyPage {

	private Button useDolteng;

	private Button useS2Dao;

	public DoltengProjectPreferencePage() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		this.useDolteng = new Button(createDefaultComposite(composite),
				SWT.CHECK);
		this.useDolteng.setText(Labels.PREFERENCE_USE_DOLTENG);

		this.useS2Dao = new Button(createDefaultComposite(composite), SWT.CHECK);
		this.useS2Dao.setText(Labels.PREFERENCE_USE_S2DAO);

		setUpStoredValue();

		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		composite.setLayoutData(data);

		return composite;
	}

	private void setUpStoredValue() {
		IProject project = getSelectedProject();
		if (project != null) {
			this.useDolteng.setSelection(ProjectUtil.hasNature(project,
					Constants.ID_NATURE));
		}
		DoltengProjectPreferences pref = DoltengCore.getPreferences(project);
		if (pref != null) {
			this.useS2Dao.setSelection(pref.isUseS2Dao());
		}
	}

	private IProject getSelectedProject() {
		IAdaptable adaptor = getElement();
		IProject project = null;
		if (adaptor instanceof IJavaProject) {
			IJavaProject javap = (IJavaProject) adaptor;
			project = javap.getProject();
		} else if (adaptor instanceof IProject) {
			IProject p = (IProject) adaptor;
			IJavaProject javap = JavaCore.create(p);
			if (javap.exists()) {
				project = p;
			}
		}
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		setUpStoredValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		try {
			IProject project = getSelectedProject();
			if (project != null) {
				if (this.useDolteng.getSelection()) {
					ProjectUtil.addNature(project, Constants.ID_NATURE);
					DoltengProjectPreferences pref = DoltengCore
							.getPreferences(project);
					if (pref != null) {
						pref.setUseS2Dao(this.useS2Dao.getSelection());
					}
				} else {
					ProjectUtil.removeNature(project, Constants.ID_NATURE);
				}
			}

			return true;
		} catch (CoreException e) {
			DoltengCore.log(e);
			return false;
		}
	}

}
