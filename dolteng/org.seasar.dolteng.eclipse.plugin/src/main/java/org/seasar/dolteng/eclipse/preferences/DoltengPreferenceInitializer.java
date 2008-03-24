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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.seasar.dolteng.eclipse.Constants;

/**
 * @author taichi
 * 
 */
public class DoltengPreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences pref = new DefaultScope()
                .getNode(Constants.ID_PLUGIN);
        pref.put(Constants.PREF_VIEW_TYPE, Constants.VIEW_TYPE_TEEDA);
        pref.put(Constants.PREF_DAO_TYPE, Constants.DAO_TYPE_KUINADAO);
        pref.putBoolean(Constants.PREF_USE_PAGE_MARKER, true);
        pref.putBoolean(Constants.PREF_USE_DI_MARKER, true);
        pref.put(Constants.PREF_ORM_XML_OUTPUT_PATH, "/");
        pref.put(Constants.PREF_WEB_SERVER, "http://localhost:8080");
        pref.putBoolean(Constants.PREF_IS_HELP_REMOTE, false);
    }

}
