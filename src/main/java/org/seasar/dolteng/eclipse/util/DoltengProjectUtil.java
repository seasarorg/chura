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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.ClassUtil;
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
            DoltengPreferences pref) {
        if (resource == null || pref == null) {
            return StringUtil.EMPTY_STRINGS;
        }
        final NamingConvention nc = pref.getNamingConvention();
        final String[] pkgs = nc.getRootPackageNames();
        final String[] results = new String[pkgs.length];
        for (int i = 0; i < pkgs.length; i++) {
            final StringBuffer stb = new StringBuffer(pkgs[i]);
            stb.append('.');
            stb.append(nc.getSubApplicationRootPackageName());
            results[i] = calculatePagePkg(resource, pref, stb.toString());
        }

        return results;
    }

    public static String calculatePagePkg(IResource resource,
            DoltengPreferences pref, String basePkg) {
        final NamingConvention nc = pref.getNamingConvention();
        final IPath path = new Path(pref.getWebContentsRoot()).append(nc
                .getViewRootPath());
        final IFolder rootFolder = resource.getProject().getFolder(path);
        final IPath rootPath = rootFolder.getFullPath();
        final IPath htmlPath = resource.getParent().getFullPath();
        final String[] segroot = rootPath.segments();
        final String[] seghtml = htmlPath.segments();
        final StringBuffer stb = new StringBuffer(basePkg);
        for (int j = segroot.length; j < seghtml.length; j++) {
            stb.append('.');
            stb.append(seghtml[j]);
        }
        return stb.toString();
    }

    public static boolean isInViewPkg(IFile file) {
        final DoltengPreferences pref = DoltengCore.getPreferences(file.getProject());
        if (pref == null) {
            return false;
        }
        final NamingConvention viewrootPath = pref.getNamingConvention();
        final IPath filePath = file.getFullPath().removeFirstSegments(1);
        final IPath path = new Path(pref.getWebContentsRoot()).append(
                viewrootPath.getViewRootPath());
        return path.isPrefixOf(filePath);
    }

    public static ArrayList findDtoNames(IFile htmlfile, String pkgname) {
        final ArrayList result = new ArrayList();
        final IJavaProject javap = JavaCore.create(htmlfile.getProject());
        result.add("java.util.List");
        result.add("java.util.Map[]");
        final DoltengPreferences pref = DoltengCore.getPreferences(javap);
        try {
            if (pref != null) {
                final NamingConvention nc = pref.getNamingConvention();
                final String[] pkgs = nc.getRootPackageNames();
                for (int i = 0; i < pkgs.length; i++) {
                    final List l = TypeUtil.getTypeNamesUnderPkg(javap, pkgs[i] + "."
                            + nc.getDtoPackageName());
                    l.addAll(TypeUtil.getTypeNamesUnderPkg(javap, pkgs[i] + "."
                            + nc.getEntityPackageName()));
                    for (final Iterator it = l.iterator(); it.hasNext();) {
                        final String s = (String) it.next();
                        result.add(s + "[]");
                    }
                }

                final List types = TypeUtil.getTypeNamesUnderPkg(javap, pkgname);
                for (final Iterator i = types.iterator(); i.hasNext();) {
                    final String s = (String) i.next();
                    if (s.endsWith(nc.getDtoSuffix())) {
                        result.add(s + "[]");
                    }
                }
            }
        } catch (final Exception e) {
            DoltengCore.log(e);
        }
        return result;
    }

    public static IFile findHtmlByJava(IProject project,
            DoltengPreferences pref, ICompilationUnit unit) {
        final NamingConvention nc = pref.getNamingConvention();
        final IType type = unit.findPrimaryType();
        final String typeName = type.getElementName();
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
            final String webPkg = ClassUtil.concatName(pref
                    .getDefaultRootPackageName(), pref.getNamingConvention()
                    .getSubApplicationRootPackageName());
            if (pkg.startsWith(webPkg)) {
                if (webPkg.length() < pkg.length()) {
                    pkg = pkg.substring(webPkg.length() + 1);
                } else {
                    pkg = "";
                }
                pkg = pkg.replace('.', '/');
                final IPath path = new Path(pref.getWebContentsRoot()).append(
                        nc.getViewRootPath()).append(pkg);
                final IFolder folder = project.getFolder(path);
                if (folder.exists()) {
                    return ResourcesUtil.findFile(htmlName, folder);
                }
            }
        }
        return null;
    }

    public static IMethod findMethodBySql(IResource sqlfile)
            throws JavaModelException {
        final IPath sqlpath = sqlfile.getFullPath();
        final String file = sqlfile.getName();
        final String daoname = file.substring(0, file.indexOf('_'));
        final String[] ary = file.split("_");
        if (1 < ary.length) {
            final String methodname = ary[1].replaceAll("\\..*", "");
            final String path = sqlpath.toString();
            final IProject p = sqlfile.getProject();
            final IJavaProject javap = JavaCore.create(p);
            final DoltengPreferences pref = DoltengCore.getPreferences(p);
            final NamingConvention nc = pref.getNamingConvention();
            final String[] pkgs = nc.getRootPackageNames();
            for (int i = 0; i < pkgs.length; i++) {
                final String s = pkgs[i];
                final String pp = s.replace('.', '/');
                if (-1 < path.indexOf(pp)) {
                    final IJavaElement element = javap.findElement(new Path(pp));
                    if (element instanceof IPackageFragment) {
                        final IPackageFragment pf = (IPackageFragment) element;
                        final ICompilationUnit[] units = pf.getCompilationUnits();
                        for (int j = 0; j < units.length; j++) {
                            final ICompilationUnit unit = units[j];
                            final String elementName = unit.getElementName();
                            if (daoname.equalsIgnoreCase(elementName)) {
                                final IType t = unit.findPrimaryType();
                                final IMethod[] methods = t.getMethods();
                                for (int k = 0; k < methods.length; k++) {
                                    final IMethod m = methods[k];
                                    if (methodname.equalsIgnoreCase(m
                                            .getElementName())) {
                                        return m;
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }
}
