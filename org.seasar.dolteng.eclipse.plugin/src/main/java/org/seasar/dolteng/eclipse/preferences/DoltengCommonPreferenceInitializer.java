package org.seasar.dolteng.eclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;

public class DoltengCommonPreferenceInitializer extends
        AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = DoltengCore.getDefault().getPreferenceStore();
        store.setDefault(Constants.PREF_MAVEN_REPOS_PATH, Constants.PREF_DEFAULT_MAVEN_REPOS_PATH);
        store.setDefault(Constants.PREF_DOWNLOAD_ONLINE, Constants.PREF_DEFAULT_DOWNLOAD_ONLINE);
    }

}
