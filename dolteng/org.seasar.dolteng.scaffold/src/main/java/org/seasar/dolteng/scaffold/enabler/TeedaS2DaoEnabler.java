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
package org.seasar.dolteng.scaffold.enabler;

import org.eclipse.core.resources.IProject;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.scaffold.ScaffoldConfigEnabler;

/**
 * @author taichi
 * 
 */
public class TeedaS2DaoEnabler implements ScaffoldConfigEnabler {
	public boolean enableFor(IProject project) {
		DoltengPreferences pref = DoltengCore.getPreferences(project);
		return pref != null
				&& Constants.VIEW_TYPE_TEEDA.equals(pref.getViewType())
				&& Constants.DAO_TYPE_S2DAO.equals(pref.getDaoType());
	}
}
