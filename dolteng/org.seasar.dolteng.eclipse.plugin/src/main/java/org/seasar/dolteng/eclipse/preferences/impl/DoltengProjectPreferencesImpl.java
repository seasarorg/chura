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

import java.io.BufferedInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.exception.XMLStreamRuntimeException;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.preferences.HierarchicalPreferenceStore;
import org.seasar.dolteng.eclipse.util.JavaProjectClassLoader;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.S2ContainerUtil;
import org.seasar.dolteng.eclipse.util.XMLStreamReaderUtil;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class DoltengProjectPreferencesImpl implements DoltengProjectPreferences {

    private static final IPath TOMCAT_PLUGIN_PREF = new Path(".tomcatplugin");

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
        setUpDefaultValues();

        IPersistentPreferenceStore[] children = this.store.getChildren();
        for (int i = 0; i < children.length; i++) {
            addConnectionConfig(new ConnectionConfigImpl(children[i]));
        }
    }

    protected void setUpDefaultValues() {
        loadfromOtherPlugin();
        this.store.setDefault(Constants.PREF_NECESSARYDICONS,
                "convention.dicon");
        this.store.setDefault(Constants.PREF_USE_S2DAO, false);

        IJavaProject javap = JavaCore.create(this.project);
        try {
            JavaProjectClassLoader pkgloader = new JavaProjectClassLoader(javap);
            DiconFinder finder = new DiconFinder(pkgloader);
            IPackageFragmentRoot[] roots = ProjectUtil
                    .findSrcFragmentRoots(javap);
            for (int i = 0; i < roots.length; i++) {
                IResource rsc = roots[i].getResource();
                if (rsc != null) {
                    rsc.accept(finder, IResource.DEPTH_ONE, false);
                }
                if (StringUtil.isEmpty(finder.rootPkgName) == false) {
                    break;
                }
            }
            if (StringUtil.isEmpty(finder.rootPkgName)) {
                return;
            }

            JavaProjectClassLoader nameloader = new JavaProjectClassLoader(
                    javap);
            Map m = S2ContainerUtil.loadNamingConvensions(nameloader);
            Object daoPkgName = m.get("DaoPackageName");
            Object entityPkgName = m.get("EntityPackageName");
            Object webPkgName = m.get("WebPackageName");
            Object viewRootPath = m.get("ViewRootPath");

            if (daoPkgName != null) {
                this.store.setDefault(Constants.PREF_DEFAULT_DAO_PACKAGE,
                        ClassUtil.concatName(finder.rootPkgName, daoPkgName
                                .toString()));
            }
            if (entityPkgName != null) {
                this.store.setDefault(Constants.PREF_DEFAULT_ENTITY_PACKAGE,
                        ClassUtil.concatName(finder.rootPkgName, entityPkgName
                                .toString()));
            }
            if (webPkgName != null) {
                this.store.setDefault(Constants.PREF_DEFAULT_WEB_PACKAGE,
                        ClassUtil.concatName(finder.rootPkgName, webPkgName
                                .toString()));
            }
            if (viewRootPath != null) {
                this.store.setDefault(Constants.PREF_DEFAULT_VIEW_ROOT_PATH,
                        viewRootPath.toString());
            }

        } catch (Exception e) {
            DoltengCore.log(e);
        } catch (Error e) {
            DoltengCore.log(e);
        }
    }

    private class DiconFinder implements IResourceVisitor {
        private final Pattern hot = Pattern.compile(".*hotdeploy.dicon");

        private final Pattern cool = Pattern.compile(".*cooldeploy.dicon");

        private JavaProjectClassLoader classLoader;

        private String rootPkgName;

        public DiconFinder(JavaProjectClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        public boolean visit(IResource resource) throws CoreException {
            if (resource instanceof IFile) {
                String name = resource.getName();
                if (hot.matcher(name).matches()) {
                    rootPkgName = S2ContainerUtil.loadHotdeployRootPkg(
                            classLoader, name);
                } else if (cool.matcher(name).matches()) {
                    rootPkgName = S2ContainerUtil.loadCooldeployRootPkg(
                            classLoader, name);
                }
            }
            return StringUtil.isEmpty(rootPkgName);
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
        XMLStreamReader reader = null;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            reader = factory.createXMLStreamReader(new BufferedInputStream(file
                    .getContents()));
            while (reader.hasNext()) {

                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT
                        && "rootDir".equals(reader.getLocalName())) {
                    this.store.setDefault(Constants.PREF_WEBCONTENTS_ROOT,
                            reader.getElementText());
                    break;
                } else {
                    reader.next();
                }
            }
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        } finally {
            XMLStreamReaderUtil.close(reader);
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
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setDefaultDaoPackage(java.lang.String)
     */
    public void setDefaultDaoPackage(String name) {
        this.store.setValue(Constants.PREF_DEFAULT_DAO_PACKAGE, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#getDefaultEntityPackage()
     */
    public String getDefaultEntityPackage() {
        return this.store.getString(Constants.PREF_DEFAULT_ENTITY_PACKAGE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences#setDefaultEntityPackage(java.lang.String)
     */
    public void setDefaultEntityPackage(String name) {
        this.store.setValue(Constants.PREF_DEFAULT_ENTITY_PACKAGE, name);
    }

}