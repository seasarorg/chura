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
package org.seasar.dolteng.eclipse.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.framework.convention.NamingConvention;

/**
 * @author taichi
 * 
 */
public class DoltengProjectUtil {

    /**
     * @param pref
     * @return
     */
    public static String calculatePagePkg(IResource resource,
            DoltengProjectPreferences pref) {
        if (resource == null || pref == null) {
            return "";
        }
        NamingConvention nc = pref.getNamingConvention();
        IPath path = new Path(pref.getWebContentsRoot()).append(nc
                .getViewRootPath());
        IFolder rootFolder = resource.getProject().getFolder(path);
        IPath rootPath = rootFolder.getFullPath();
        IPath htmlPath = resource.getParent().getFullPath();
        String[] segroot = rootPath.segments();
        String[] seghtml = htmlPath.segments();
        StringBuffer stb = new StringBuffer(pref.getRawPreferences().getString(
                Constants.PREF_DEFAULT_WEB_PACKAGE));
        for (int i = segroot.length; i < seghtml.length; i++) {
            stb.append('.');
            stb.append(seghtml[i]);
        }
        return stb.toString();
    }

    public static boolean isInViewPkg(IFile file, DoltengProjectPreferences pref) {
        NamingConvention nc = pref.getNamingConvention();
        IPath path = new Path(pref.getWebContentsRoot()).append(nc
                .getViewRootPath());
        IFolder fol = file.getProject().getFolder(path);
        IPath rootPath = fol.getFullPath();
        IPath htmlPath = file.getParent().getFullPath();
        String[] segroot = rootPath.segments();
        String[] seghtml = htmlPath.segments();
        boolean match = segroot != null && seghtml != null
                && segroot.length < seghtml.length;
        for (int i = 0; match && i < segroot.length; i++) {
            if (segroot[i].equals(seghtml[i]) == false) {
                match = false;
                break;
            }
        }
        return match;
    }
}
