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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class TypeUtil {

    public static String getResolvedTypeName(String typeSignature, IType type) {
        int count = Signature.getArrayCount(typeSignature);
        if (Signature.C_UNRESOLVED == typeSignature.charAt(count)) {
            String name = null;
            int generics = typeSignature.indexOf('<');
            if (0 < generics) {
                name = typeSignature.substring(count + 1, generics);
            } else {
                if (0 < count) {
                    name = typeSignature.substring(count + 1, typeSignature
                            .indexOf(';'));
                } else {
                    name = Signature.toString(typeSignature);
                }
            }
            return resolveType(type, count, name);
        } else {
            return Signature.toString(typeSignature);
        }
    }

    /**
     * @param type
     * @param count
     * @param shortname
     * @return
     * @throws JavaModelException
     */
    private static String resolveType(IType type, int count, String shortname) {
        try {
            String[][] resolvedNames = type.resolveType(shortname);
            if (resolvedNames != null && resolvedNames.length > 0) {
                StringBuffer stb = new StringBuffer();
                String pkg = resolvedNames[0][0];
                if (pkg != null && 0 < pkg.length()) {
                    stb.append(resolvedNames[0][0]);
                    stb.append('.');
                }
                stb.append(resolvedNames[0][1].replace('.', '$'));
                for (int i = 0; i < count; i++) {
                    stb.append("[]");
                }
                return stb.toString();
            }
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
        return "";
    }

    public static String resolveType(String shortname, IType type) {
        return resolveType(type, 0, shortname);
    }

    public static List getTypeNamesUnderPkg(IJavaProject project, String pkgName)
            throws CoreException {
        List result = new ArrayList();
        IPackageFragmentRoot root = ProjectUtil
                .getFirstSrcPackageFragmentRoot(project);
        if (root != null && root.exists()) {
            IPackageFragment fragment = root.getPackageFragment(pkgName);
            if (fragment != null && fragment.exists()) {
                ICompilationUnit[] classes = fragment.getCompilationUnits();
                for (int i = 0; i < classes.length; i++) {
                    IType type = classes[i].findPrimaryType();
                    if (type != null) {
                        result.add(type.getFullyQualifiedName());
                    }
                }
            }
        }
        return result;
    }

    public static IMember getMember(IType type, String name)
            throws JavaModelException {
        IMember result = null;
        if (StringUtil.isEmpty(name) == false) {
            result = matchMember(type.getMethods(), name);
            if (result == null) {
                result = matchMember(type.getFields(), name);
            }
        }
        return result;
    }

    private static IMember matchMember(IMember[] members, String name) {
        for (int i = 0; i < members.length; i++) {
            if (name.equalsIgnoreCase(members[i].getElementName())) {
                return members[i];
            }
        }
        return null;
    }
}
