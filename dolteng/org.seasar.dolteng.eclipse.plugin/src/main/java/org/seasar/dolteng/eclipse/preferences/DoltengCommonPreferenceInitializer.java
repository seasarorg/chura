package org.seasar.dolteng.eclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.seasar.dolteng.eclipse.Constants;

public class DoltengCommonPreferenceInitializer extends
        AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences pref = new InstanceScope()
                .getNode(Constants.ID_PLUGIN + "common");
        pref.put(Constants.PREF_MAVEN_REPOS_PATH, Constants.PREF_DEFAULT_MAVEN_REPOS_PATH);
        pref.putBoolean(Constants.PREF_DOWNLOAD_ONLINE, Constants.PREF_DEFAULT_DOWNLOAD_ONLINE);
    }

}
