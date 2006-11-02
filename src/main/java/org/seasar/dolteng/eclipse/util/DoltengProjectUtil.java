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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class DoltengProjectUtil {

    /**
     * @param pref
     * @return
     */
    public static String[] calculatePagePkg(IResource resource,
            DoltengProjectPreferences pref) {
        if (resource == null || pref == null) {
            return StringUtil.EMPTY_STRINGS;
        }
        NamingConvention nc = pref.getNamingConvention();
        IPath path = new Path(pref.getWebContentsRoot()).append(nc
                .getViewRootPath());
        IFolder rootFolder = resource.getProject().getFolder(path);
        IPath rootPath = rootFolder.getFullPath();
        IPath htmlPath = resource.getParent().getFullPath();
        String[] segroot = rootPath.segments();
        String[] seghtml = htmlPath.segments();

        String[] pkgs = nc.getRootPackageNames();
        String[] results = new String[pkgs.length];
        for (int i = 0; i < pkgs.length; i++) {
            StringBuffer stb = new StringBuffer(pkgs[i]);
            for (int j = segroot.length; j < seghtml.length; j++) {
                stb.append('.');
                stb.append(seghtml[j]);
            }
            results[i] = stb.toString();
        }

        return results;
    }

    public static boolean isInViewPkg(IFile file) {
        DoltengProjectPreferences pref = DoltengCore.getPreferences(file
                .getProject());
        if (pref == null) {
            return false;
        }
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

    public static ArrayList findDtoNames(IFile htmlfile, String pkgname) {
        ArrayList result = new ArrayList();
        IJavaProject javap = JavaCore.create(htmlfile.getProject());
        result.add("java.util.List");
        DoltengProjectPreferences pref = DoltengCore.getPreferences(javap);
        NamingConvention nc = pref.getNamingConvention();
        Pattern ptn = Pattern.compile(".*" + nc.getDtoSuffix(),
                Pattern.CASE_INSENSITIVE);
        try {
            if (pref != null) {
                String pkgName = pref.getRawPreferences().getString(
                        Constants.PREF_DEFAULT_DTO_PACKAGE);
                result.addAll(TypeUtil.getTypeNamesUnderPkg(javap, pkgName));
                List types = TypeUtil.getTypeNamesUnderPkg(javap, pkgname);
                for (Iterator i = types.iterator(); i.hasNext();) {
                    String s = (String) i.next();
                    if (ptn.matcher(s).matches()) {
                        result.add(s);
                    }
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return result;
    }

    public static IFile findHtmlByJava(IProject project,
            DoltengProjectPreferences pref, ICompilationUnit unit) {
        NamingConvention nc = pref.getNamingConvention();
        IType type = unit.findPrimaryType();
        String typeName = type.getElementName();
        String htmlName = null;
        if (typeName.endsWith(nc.getPageSuffix())) {
            htmlName = typeName.substring(0, typeName.indexOf(nc
                    .getPageSuffix()));
        } else if (typeName.endsWith(nc.getActionSuffix())) {
            htmlName = typeName.substring(0, typeName.indexOf(nc
                    .getActionSuffix()));
        }
        if (StringUtil.isEmpty(htmlName) == false) {
            htmlName = StringUtil.decapitalize(htmlName);
            htmlName = htmlName + nc.getViewExtension();
            String pkg = type.getPackageFragment().getElementName();
            String webPkg = pref.getRawPreferences().getString(
                    Constants.PREF_DEFAULT_WEB_PACKAGE);
            if (pkg.startsWith(webPkg)) {
                if (webPkg.length() < pkg.length()) {
                    pkg = pkg.substring(webPkg.length() + 1);
                } else {
                    pkg = "";
                }
                pkg = pkg.replace('.', '/');
                IPath path = new Path(pref.getWebContentsRoot()).append(
                        nc.getViewRootPath()).append(pkg);
                IFolder folder = project.getFolder(path);
                if (folder.exists()) {
                    return ResourcesUtil.findFile(htmlName, folder);
                }
            }
        }
        return null;
    }
}
