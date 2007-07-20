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

package org.seasar.dolteng.docs.action;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;
import org.seasar.dolteng.docs.Activator;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.eclipse.common.util.ResouceUtil;
import org.seasar.eclipse.common.util.WorkbenchUtil;

/**
 * @author taichi
 * 
 */
public abstract class AbstractHelpAction implements IActionDelegate {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {
			IResource r = ResouceUtil.getCurrentSelectedResouce();
			boolean isRemote = false;

			if (r != null) {
				IProject p = r.getProject();
				DoltengPreferences pref = DoltengCore.getPreferences(p);
				if (pref != null) {
					isRemote = pref.isHelpRemote();
				}
			}

			URL url = null;
			if (isRemote) {
				url = getRemoteHelp();
			} else {
				url = getLocalHelp();
			}
			if (url != null) {
				WorkbenchUtil.openUrl(url, true);
			}
		} catch (Exception e) {
			DoltengCore.log(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}

	protected URL getLocalHelp() throws IOException {
		URL url = Activator.getDefault().getBundle().getEntry(
				getLocalHelpPath());
		return FileLocator.toFileURL(url);
	}

	protected abstract String getLocalHelpPath();

	protected URL getRemoteHelp() throws MalformedURLException {
		return new URL(getRemoteHelpURL());
	}

	protected abstract String getRemoteHelpURL();

}
