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
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.preferences.HierarchicalPreferenceStore;
import org.seasar.dolteng.eclipse.util.S2ContainerUtil;
import org.seasar.framework.container.autoregister.AutoRegisterProject;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.ClassUtil;

/**
 * @author taichi
 * 
 */
public class DoltengProjectPreferencesImpl implements DoltengProjectPreferences {

	private IProject project;

	private HierarchicalPreferenceStore store;

	private Map connections = new HashMap();

	public DoltengProjectPreferencesImpl(IProject project) {
		super();
		if (project == null) {
			throw new IllegalArgumentException();
		}
		this.project = project;

		this.store = new HierarchicalPreferenceStore(new ProjectScope(project),
				Constants.ID_PLUGIN);
		// setUpDefaultValues();

		IPersistentPreferenceStore[] children = this.store.getChildren();
		for (int i = 0; i < children.length; i++) {
			addConnectionConfig(new ConnectionConfigImpl(children[i]));
		}
	}

	protected void setUpDefaultValues() {
		this.store.setDefault(Constants.PREF_WEBCONTENTS_ROOT, ".");
		this.store.setDefault(Constants.PREF_NECESSARYDICONS,
				"convention.dicon,hotdeploy.dicon");
		this.store.setDefault(Constants.PREF_USE_S2DAO, false);

		IJavaProject javap = JavaCore.create(this.project);
		NamingConvention nc = (NamingConvention) S2ContainerUtil.loadComponent(
				javap, "convention.dicon", NamingConvention.class);
		if (nc == null) {
			return;
		}
		DiconFinder finder = new DiconFinder();
		try {
			this.project.accept(finder, IResource.DEPTH_ONE, false);
			AutoRegisterProject arp = (AutoRegisterProject) S2ContainerUtil
					.loadComponent(javap, finder.name,
							AutoRegisterProject.class);
			String rootPkg = arp.getRootPackageName();

			this.store.setDefault(Constants.PREF_DEFAULT_DAO_PACKAGE, ClassUtil
					.concatName(rootPkg, nc.getDaoPackageName()));
			this.store.setDefault(Constants.PREF_DEFAULT_ENTITY_PACKAGE,
					ClassUtil.concatName(rootPkg, nc.getEntityPackageName()));

		} catch (Exception e) {
			DoltengCore.log(e);
		}
	}

	private class DiconFinder implements IResourceVisitor {
		Pattern ptn = Pattern.compile(".*hotdeploy.dicon");

		String name;

		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IFile
					&& ptn.matcher(resource.getName()).matches()) {
				IFile f = (IFile) resource;
				name = f.getName();
				return false;
			}
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getRawPreferences()
	 */
	public IPersistentPreferenceStore getRawPreferences() {
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
	 * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#isUseS2Dao()
	 */
	public boolean isUseS2Dao() {
		return this.store.getBoolean(Constants.PREF_USE_S2DAO);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setUseS2Dao(boolean)
	 */
	public void setUseS2Dao(boolean is) {
		this.store.setValue(Constants.PREF_USE_S2DAO, is);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#addConnectionConfig(org.seasar.dolteng.eclipse.preferences.ConnectionConfig)
	 */
	public void addConnectionConfig(ConnectionConfig config) {
		this.connections.put(config.getName(), config);
		this.store.addChild(config.getName(), config.toPreferenceStore());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getDefaultDaoPackage()
	 */
	public String getDefaultDaoPackage() {
		return this.store.getString(Constants.PREF_DEFAULT_DAO_PACKAGE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getDefaultEntityPackage()
	 */
	public String getDefaultEntityPackage() {
		return this.store.getString(Constants.PREF_DEFAULT_ENTITY_PACKAGE);
	}

}