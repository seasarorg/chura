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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.seasar.framework.util.ArrayMap;

/**
 * @author daisuke
 *
 */
public class JREUtils {

    public enum VersionLength {
        FULL,
        SHORT
    }
    
    private static ArrayMap jres = null;

    private static void init() {
        if(jres == null) {
            jres = new ArrayMap();
            for (IVMInstallType type : JavaRuntime.getVMInstallTypes()) {
                for (IVMInstall install : type.getVMInstalls()) {
                    if (install instanceof IVMInstall2) {
                        IVMInstall2 vm2 = (IVMInstall2) install;
//                        StringBuffer stb = new StringBuffer();
//                        stb.append(install.getName());
//                        stb.append(" (");
//                        stb.append(vm2.getJavaVersion());
//                        stb.append(")");
//                        jres.put(stb.toString(), vm2);
                        jres.put(install.getName(), vm2);
                    }
                }
            }
        }
    }
    
    public static void clear() {
        jres = null;
    }
    
    public static ArrayMap getJREs() {
        init();
        return jres;
    }
    
    public static String[] getKeyArray() {
        init();
        String[] ary = new String[jres.size()];
        for (int i = 0; i < jres.size(); i++) {
            ary[i] = jres.getKey(i).toString();
        }
        return ary;
    }

    public static String getJREContainer(String key) {
        init();
        IPath path = new Path(JavaRuntime.JRE_CONTAINER);
        if (key != null) {
            IVMInstall vm = (IVMInstall) jres.get(key);
            path = path.append(vm.getVMInstallType().getId());
            path = path.append(vm.getName());
        }
        return path.toString();
    }
    
    public static String getDefaultkey() {
        IVMInstall vm = JavaRuntime.getDefaultVMInstall();
        return vm.getName();
    }

    public static String getDefaultJavaVersion(VersionLength size) {
        String version = JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE);
        IVMInstall vm = JavaRuntime.getDefaultVMInstall();
        if (vm instanceof IVMInstall2) {
            IVMInstall2 vm2 = (IVMInstall2) vm;
            version = vm2.getJavaVersion();
        }
        if(size == VersionLength.SHORT) {
            version = shorten(version);
        }
        return version;
    }

    public static String getJavaVersion(String key, VersionLength size) {
        init();
        if(key == null) {
            return getDefaultJavaVersion(size);
        }
        IVMInstall2 vm = (IVMInstall2) jres.get(key);
        if(vm == null) {
            return getDefaultJavaVersion(size);
        }
        String version = vm.getJavaVersion();
        if(size == VersionLength.SHORT) {
            version = shorten(version);
        }
        return version;
    }

    private static String shorten(String version) {
        // TODO イマイチ過ぎる。
        int firstDot = version.indexOf('.');
        int secondDot = version.indexOf('.', firstDot+1);
        return version.substring(0, secondDot);
    }

}
