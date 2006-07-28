/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.seasar.dolteng.eclipse.preferences;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.PreferenceConstants;

public class JavaPreferencesSettings {

    public static int getImportNumberThreshold(IJavaProject project) {
        String thresholdStr = PreferenceConstants.getPreference(
                PreferenceConstants.ORGIMPORTS_ONDEMANDTHRESHOLD, project);
        try {
            int threshold = Integer.parseInt(thresholdStr);
            if (threshold < 0) {
                threshold = Integer.MAX_VALUE;
            }
            return threshold;
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    public static String[] getImportOrderPreference(IJavaProject project) {
        String str = PreferenceConstants.getPreference(
                PreferenceConstants.ORGIMPORTS_IMPORTORDER, project);
        if (str != null && 0 < str.length()) {
            return str.split(";");
        }
        return new String[0];
    }

}
