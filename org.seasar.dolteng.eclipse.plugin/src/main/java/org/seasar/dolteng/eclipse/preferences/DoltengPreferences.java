/*
 * Copyright 2004-2008 the Seasar Foundation and the Others.
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

import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.seasar.framework.convention.NamingConvention;

/**
 * @author taichi
 * 
 */
public interface DoltengPreferences {

    IPersistentPreferenceStore getRawPreferences();

    NamingConvention getNamingConvention();

    String getWebContentsRoot();

    String getServletPath();

    String getWebServer();

    void setWebServerPort(String port);

    Set<String> getNecessaryDicons();

    void setNecessaryDicons(Set dicons);

    public ConnectionConfig[] getAllOfConnectionConfig();

    public void addConnectionConfig(ConnectionConfig config);

    public ConnectionConfig getConnectionConfig(String name);

    public String getViewType();

    public void setViewType(String type);

    public String getDaoType();

    public void setDaoType(String type);

    public boolean isUsePageMarker();

    public void setUsePageMarker(boolean is);

    public boolean isUseDIMarker();

    public void setUseDIMarker(boolean is);

    public boolean isUseSqlMarker();

    public void setUseSqlMarker(boolean is);

    public void setUpValues();

    public IPath getOrmXmlOutputPath();

    public void setOrmXmlOutputPath(String path);

    public IPath getDefaultSrcPath();

    public void setDefaultSrcPath(String path);

    public IPath getDefaultResourcePath();

    public void setDefaultResourcePath(String path);

    public IPath getFlexSourceFolderPath();

    public void setFlexSourceFolderPath(String path);

    public String getDefaultRootPackageName();

    public void setDefaultRootPackageName(String name);

    public boolean isHelpRemote();

    public void setHelpRemote(boolean is);

    public boolean isUsePublicField();

    public void setUsePublicField(boolean is);

}