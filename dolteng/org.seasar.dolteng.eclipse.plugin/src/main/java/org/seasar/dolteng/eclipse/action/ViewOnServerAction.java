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
package org.seasar.dolteng.eclipse.action;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IActionDelegate;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.eclipse.common.util.WorkbenchUtil;

/**
 * @author taichi
 * 
 */
public class ViewOnServerAction extends AbstractWorkbenchWindowActionDelegate
        implements IActionDelegate {

    /**
     * 
     */
    public ViewOnServerAction() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.action.AbstractEditorActionDelegate#processResource(org.eclipse.core.resources.IProject,
     *      org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences,
     *      org.eclipse.core.resources.IResource)
     */
    @Override
    protected void processResource(IProject project, DoltengPreferences pref,
            IResource resource) throws Exception {
        IPath p = resource.getFullPath();
        p = p.removeFirstSegments(1);
        IPath webRoot = new Path(pref.getWebContentsRoot());
        if (webRoot.isPrefixOf(p)) {
            p = p.removeFirstSegments(webRoot.segmentCount());
            p = new Path(pref.getWebServer()).append(pref.getServletPath())
                    .append(p);
            WorkbenchUtil.openUrl(p.toString());
        }

    }

}
