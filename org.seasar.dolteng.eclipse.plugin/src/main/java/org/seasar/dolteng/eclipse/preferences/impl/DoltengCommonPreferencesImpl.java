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

import org.eclipse.jface.preference.IPreferenceStore;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengCommonPreferences;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class DoltengCommonPreferencesImpl implements DoltengCommonPreferences {

    private IPreferenceStore store;

    public DoltengCommonPreferencesImpl() {
        super();

        store = DoltengCore.getDefault().getPreferenceStore();
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
        store.setValue(Constants.PREF_MAVEN_REPOS_PATH, path);
    }


}