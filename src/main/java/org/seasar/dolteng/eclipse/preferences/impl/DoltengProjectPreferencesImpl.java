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

package org.seasar.dolteng.eclipse.preferences.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.preferences.HierarchicalPreferenceStore;

/**
 * @author taichi
 * 
 */
public class DoltengProjectPreferencesImpl implements DoltengProjectPreferences {

	private HierarchicalPreferenceStore store;

	private Map connections = new HashMap();

	public DoltengProjectPreferencesImpl(IProject project) {
		super();
		if (project == null) {
			throw new IllegalArgumentException();
		}

		this.store = new HierarchicalPreferenceStore(new ProjectScope(project),
				Constants.ID_PLUGIN);
		IPersistentPreferenceStore[] children = this.store.getChildren();
		for (int i = 0; i < children.length; i++) {
			addConnectionConfig(new ConnectionConfigImpl(children[i]));
		}
	}

	protected void setUpDefaultValues() {
		this.store.setDefault(Constants.PREF_WEBCONTENTS_ROOT, ".");
		this.store.setDefault(Constants.PREF_NECESSARYDICONS,
				"convention.dicon,hotdeploy.dicon");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getRawPreferences()
	 */
	public IPreferenceStore getRawPreferences() {
		return this.store;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getWebContentsRoot()
	 */
	public String getWebContentsRoot() {
		return this.store.getString(Constants.PREF_WEBCONTENTS_ROOT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setWebContentsRoot(java.lang.String)
	 */
	public void setWebContentsRoot(String path) {
		this.store.setValue(Constants.PREF_WEBCONTENTS_ROOT, path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getNecessaryDicons()
	 */
	public Set getNecessaryDicons() {
		String dicons = this.store.getString(Constants.PREF_NECESSARYDICONS);
		return new HashSet(Arrays.asList(dicons.split(",")));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setNecessaryDicons(java.util.Set)
	 */
	public void setNecessaryDicons(Set dicons) {
		if (dicons != null && 0 < dicons.size()) {
			StringBuffer stb = new StringBuffer();
			for (Iterator i = dicons.iterator(); i.hasNext();) {
				stb.append(i.next());
				stb.append(',');

			}
			stb.setLength(stb.length() - 1);
			this.store.setValue(Constants.PREF_NECESSARYDICONS, stb.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#addConnectionConfig(org.seasar.dolteng.eclipse.preferences.ConnectionConfig)
	 */
	public void addConnectionConfig(ConnectionConfig config) {
		this.connections.put(config.getName(), config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getAllOfConnectionConfig()
	 */
	public ConnectionConfig[] getAllOfConnectionConfig() {
		Collection list = this.connections.values();
		return (ConnectionConfig[]) list.toArray(new ConnectionConfig[list
				.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getConnectionConfig(java.lang.String)
	 */
	public ConnectionConfig getConnectionConfig(String name) {
		return (ConnectionConfig) this.connections.get(name);
	}

}