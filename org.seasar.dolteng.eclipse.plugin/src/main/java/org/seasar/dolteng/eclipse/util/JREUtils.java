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

import java.util.Comparator;
import java.util.TreeMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * @author daisuke
 */
public class JREUtils {

    public enum VersionLength {
        FULL, SHORT
    }

    private static TreeMap<String, IVMInstall2> jres = null;

    private static void init() {
        if (jres == null) {
            jres = new TreeMap<String, IVMInstall2>(new Comparator<String>() {

                public int compare(String o1, String o2) {
                    String v1 = getJavaVersionNumber(o1, VersionLength.FULL);
                    String v2 = getJavaVersionNumber(o2, VersionLength.FULL);
                    return v1.compareTo(v2);
                }

            });
            for (IVMInstallType type : JavaRuntime.getVMInstallTypes()) {
                for (IVMInstall vm : type.getVMInstalls()) {
                    if (vm instanceof IVMInstall2) {
                        IVMInstall2 vm2 = (IVMInstall2) vm;
                        jres.put(vm.getName(), vm2);
                    }
                }
            }
        }
    }

    public static void clear() {
        jres = null;
    }

    public static TreeMap getJREs() {
        init();
        return jres;
    }

    @SuppressWarnings("unchecked")
    public static String[] getInstalledVmNames() {
        init();
        return jres.keySet().toArray(new String[jres.size()]);
    }

    public static String getJREContainer(String name) {
        init();
        IPath path = new Path(JavaRuntime.JRE_CONTAINER);
        if (name != null) {
            IVMInstall vm = (IVMInstall) jres.get(name);
            path = path.append(vm.getVMInstallType().getId());
            path = path.append(vm.getName());
        }
        return path.toString();
    }

    public static String getDefaultJavaVmName() {
        IVMInstall vm = JavaRuntime.getDefaultVMInstall();
        return vm.getName();
    }

    public static String getDefaultJavaVersionNumber(VersionLength size) {
        String version = JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE);
        IVMInstall vm = JavaRuntime.getDefaultVMInstall();
        if (vm instanceof IVMInstall2) {
            IVMInstall2 vm2 = (IVMInstall2) vm;
            version = vm2.getJavaVersion();
        }
        if (size == VersionLength.SHORT) {
            version = shorten(version);
        }
        return version;
    }

    public static String getJavaVersionNumber(String name, VersionLength size) {
        init();
        if (name == null) {
            return getDefaultJavaVersionNumber(size);
        }
        IVMInstall2 vm2 = jres.get(name);
        if (vm2 == null) {
            return getDefaultJavaVersionNumber(size);
        }
        String version = vm2.getJavaVersion();
        if (size == VersionLength.SHORT) {
            version = shorten(version);
        }
        return version;
    }

    private static String shorten(String version) {
        // TODO イマイチ過ぎる。
        int firstDot = version.indexOf('.');
        int secondDot = version.indexOf('.', firstDot + 1);
        return version.substring(0, secondDot);
    }

}
