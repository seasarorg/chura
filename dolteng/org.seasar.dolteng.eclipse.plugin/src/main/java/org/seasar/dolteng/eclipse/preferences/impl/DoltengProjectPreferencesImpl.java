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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.XPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.preferences.HierarchicalPreferenceStore;
import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
import org.seasar.dolteng.eclipse.util.S2ContainerUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class DoltengProjectPreferencesImpl implements DoltengProjectPreferences {

    private static final IPath TOMCAT_PLUGIN_PREF = new Path(".tomcatplugin");

    private static final Set<String> VIEW_SET = new HashSet<String>(Arrays
            .asList(Constants.VIEW_TYPES));

    private static final Set<String> DAO_SET = new HashSet<String>(Arrays
            .asList(Constants.DAO_TYPES));

    private IProject project;

    private HierarchicalPreferenceStore store;

    private Map<String, ConnectionConfig> connections = new HashMap<String, ConnectionConfig>();

    private NamingConvention namingConvention;

    public DoltengProjectPreferencesImpl(IProject project) {
        super();
        if (project == null) {
            throw new IllegalArgumentException();
        }
        this.project = project;

        this.store = new HierarchicalPreferenceStore(new ProjectScope(project),
                Constants.ID_PLUGIN);
        setUpValues();

        IPersistentPreferenceStore[] children = this.store.getChildren();
        for (int i = 0; i < children.length; i++) {
            addConnectionConfig(new ConnectionConfigImpl(children[i]));
        }
    }

    public void setUpValues() {
        loadfromOtherPlugin();

        this.namingConvention = S2ContainerUtil
                .loadNamingConvensions(this.project);
        String rootPkgName = "";
        String[] ary = this.namingConvention.getRootPackageNames();
        if (0 < ary.length) {
            rootPkgName = ary[0];
        }

        String[] keys = { Constants.PREF_DEFAULT_DTO_PACKAGE,
                Constants.PREF_DEFAULT_DAO_PACKAGE,
                Constants.PREF_DEFAULT_ENTITY_PACKAGE,
                Constants.PREF_DEFAULT_WEB_PACKAGE };
        Object[] values = { this.namingConvention.getDtoPackageName(),
                this.namingConvention.getDaoPackageName(),
                this.namingConvention.getEntityPackageName(),
                this.namingConvention.getSubApplicationRootPackageName() };
        for (int i = 0; i < keys.length; i++) {
            if (values[i] != null
                    && StringUtil.isEmpty(this.store.getString(keys[i]))) {
                this.store.setValue(keys[i], ClassUtil.concatName(rootPkgName,
                        values[i].toString()));
            }
        }

        String s = this.store.getString(Constants.PREF_DEFAULT_SRC_PATH);
        if (StringUtil.isEmpty(s)) {
            this.setDefaultSrcPath(project.getFullPath().append(
                    "/src/main/java").toString());
        }
        s = this.store.getString(Constants.PREF_DEFAULT_RESOURCE_PATH);
        if (StringUtil.isEmpty(s)) {
            this.setDefaultResourcePath(project.getFullPath().append(
                    "/src/main/resources").toString());
        }
    }

    protected void loadfromOtherPlugin() {
        try {
            IFile file = this.project.getFile(TOMCAT_PLUGIN_PREF);
            if (file.exists()) {
                readFromTomcatPlugin(file);
                // TODO WTPからも取ってくる？
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    protected void readFromTomcatPlugin(IFile file) throws CoreException {
        try {
            FuzzyXMLDocument doc = FuzzyXMLUtil.parse(file);
            String rootDir = (String) XPath.getValue(doc.getDocumentElement(),
                    "//rootDir");
            if (StringUtil.isEmpty(rootDir) == false) {
                this.store.setValue(Constants.PREF_WEBCONTENTS_ROOT, rootDir);
            }
            String path = (String) XPath.getValue(doc.getDocumentElement(),
                    "//webPath");
            if (StringUtil.isEmpty(path)) {
                this.store.setValue(Constants.PREF_SERVLET_PATH, "");
            } else {
                this.store.setValue(Constants.PREF_SERVLET_PATH, path);
            }
        } catch (IOException e) {
            DoltengCore.log(e);
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
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getNamingConvention()
     */
    public NamingConvention getNamingConvention() {
        return this.namingConvention;
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
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getServletPath()
     */
    public String getServletPath() {
        return this.store.getString(Constants.PREF_SERVLET_PATH);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getWebServerPort()
     */
    public String getWebServer() {
        return this.store.getString(Constants.PREF_WEB_SERVER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setWebServerPort(java.lang.String)
     */
    public void setWebServerPort(String port) {
        this.store.setValue(Constants.PREF_WEB_SERVER, port);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getNecessaryDicons()
     */
    public Set<String> getNecessaryDicons() {
        String dicons = this.store.getString(Constants.PREF_NECESSARYDICONS);
        return new HashSet<String>(Arrays.asList(dicons.split(",")));
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
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getViewType()
     */
    public String getViewType() {
        return this.store.getString(Constants.PREF_VIEW_TYPE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setViewType(java.lang.String)
     */
    public void setViewType(String type) {
        if (VIEW_SET.contains(type)) {
            this.store.setValue(Constants.PREF_VIEW_TYPE, type);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getDaoType()
     */
    public String getDaoType() {
        return this.store.getString(Constants.PREF_DAO_TYPE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setDaoType(java.lang.String)
     */
    public void setDaoType(String type) {
        if (DAO_SET.contains(type)) {
            this.store.setValue(Constants.PREF_DAO_TYPE, type);
        }
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
        Collection<ConnectionConfig> list = this.connections.values();
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

    public boolean isUsePageMarker() {
        return this.store.getBoolean(Constants.PREF_USE_PAGE_MARKER);
    }

    public void setUsePageMarker(boolean is) {
        this.store.setValue(Constants.PREF_USE_PAGE_MARKER, is);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#isUseDIMarker()
     */
    public boolean isUseDIMarker() {
        return this.store.getBoolean(Constants.PREF_USE_DI_MARKER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setUseDIMarker(boolean)
     */
    public void setUseDIMarker(boolean is) {
        this.store.setValue(Constants.PREF_USE_DI_MARKER, is);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getOrmXmlOutputPath()
     */
    public IPath getOrmXmlOutputPath() {
        return new Path(store.getString(Constants.PREF_ORM_XML_OUTPUT_PATH));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setOrmXmlOutputPath(java.lang.String)
     */
    public void setOrmXmlOutputPath(String path) {
        if (StringUtil.isEmpty(path) == false) {
            this.store.setValue(Constants.PREF_ORM_XML_OUTPUT_PATH, path);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getDefaultResourcePath()
     */
    public IPath getDefaultResourcePath() {
        return new Path(this.store
                .getString(Constants.PREF_DEFAULT_RESOURCE_PATH));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getDefaultSrcPath()
     */
    public IPath getDefaultSrcPath() {
        return new Path(this.store.getString(Constants.PREF_DEFAULT_SRC_PATH));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setDefaultResourcePath(java.lang.String)
     */
    public void setDefaultResourcePath(String path) {
        this.store.setValue(Constants.PREF_DEFAULT_RESOURCE_PATH, path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setDefaultSrcPath(java.lang.String)
     */
    public void setDefaultSrcPath(String path) {
        this.store.setValue(Constants.PREF_DEFAULT_SRC_PATH, path);
    }

}