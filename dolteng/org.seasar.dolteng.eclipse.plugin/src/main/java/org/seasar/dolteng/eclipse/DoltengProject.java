package org.seasar.dolteng.eclipse;

import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.framework.container.S2Container;

public interface DoltengProject {

	S2Container getContainer();

	DoltengProjectPreferences getProjectPreferences();

}
