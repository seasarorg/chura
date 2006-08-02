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

import java.util.Set;

import org.eclipse.jface.preference.IPersistentPreferenceStore;

/**
 * @author taichi
 * 
 */
public interface DoltengProjectPreferences {

    IPersistentPreferenceStore getRawPreferences();

    String getWebContentsRoot();

    void setWebContentsRoot(String path);

    Set getNecessaryDicons();

    void setNecessaryDicons(Set dicons);

    public ConnectionConfig[] getAllOfConnectionConfig();

    public void addConnectionConfig(ConnectionConfig config);

    public ConnectionConfig getConnectionConfig(String name);

    public boolean isUseS2Dao();

    public void setUseS2Dao(boolean is);

    public String getDefaultEntityPackage();

    public void setDefaultEntityPackage(String name);

    public String getDefaultDaoPackage();

    public void setDefaultDaoPackage(String name);

}
