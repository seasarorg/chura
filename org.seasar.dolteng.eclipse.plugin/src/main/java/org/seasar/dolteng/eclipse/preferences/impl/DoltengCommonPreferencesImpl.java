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
package org.seasar.dolteng.eclipse.preferences.impl;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.preferences.DoltengCommonPreferences;
import org.seasar.dolteng.eclipse.preferences.HierarchicalPreferenceStore;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class DoltengCommonPreferencesImpl implements DoltengCommonPreferences {

    private IPreferenceStore store;

    public DoltengCommonPreferencesImpl() {
        super();

        store = new HierarchicalPreferenceStore(new InstanceScope(),
                Constants.ID_PLUGIN + "common");
        setUpValues();
    }

    public void setUpValues() {
        String s = store.getString(Constants.PREF_MAVEN_REPOS_PATH);
        if (StringUtil.isEmpty(s)) {
            this.setMavenReposPath(Constants.PREF_DEFAULT_MAVEN_REPOS_PATH);
        }
        
        setDownloadOnline(isDownloadOnline());
        setMavenReposPath(getMavenReposPath());
    }

    public IPreferenceStore getRawPreferences() {
        return store;
    }

    /* (non-Javadoc)
     * @see org.seasar.dolteng.eclipse.preferences.DoltengCommonPreferences#isDownloadOnline()
     */
    public boolean isDownloadOnline() {
        if (Platform.getBundle("org.seasar.dolteng.projects.dependencies1") == null
                || Platform.getBundle("org.seasar.dolteng.projects.dependencies2") == null) {
            return true;
        }
        return store.getBoolean(Constants.PREF_DOWNLOAD_ONLINE);
    }

    /* (non-Javadoc)
     * @see org.seasar.dolteng.eclipse.preferences.DoltengCommonPreferences#setDownloadOnline()
     */
    public void setDownloadOnline(boolean value) {
        store.setValue(Constants.PREF_DOWNLOAD_ONLINE, value);
    }

    public String getMavenReposPath() {
        return store.getString(Constants.PREF_MAVEN_REPOS_PATH);
    }

    public void setMavenReposPath(String path) {
        if(StringUtil.isEmpty(path) == false) {
            store.setValue(Constants.PREF_MAVEN_REPOS_PATH, path);
        }
    }


}