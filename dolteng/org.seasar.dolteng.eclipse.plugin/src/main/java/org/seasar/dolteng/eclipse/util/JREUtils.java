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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jface.util.Policy;
import org.seasar.framework.util.ArrayMap;

/**
 * @author daisuke
 */
public class JREUtils {

    public enum VersionLength {
        FULL, SHORT
    }

    private static ArrayMap/*<String, IVMInstall2>*/ jres = null;

    private static ArrayMap/*<String, IExecutionEnvironment>*/ eeJres = null;
    
    private static void init() {
        if (jres == null) {
            jres = new ArrayMap();
            for (IVMInstallType type : JavaRuntime.getVMInstallTypes()) {
                for (IVMInstall install : type.getVMInstalls()) {
                    if (install instanceof IVMInstall2) {
                        IVMInstall2 vm2 = (IVMInstall2) install;
                        jres.put(install.getName(), vm2);
                    }
                }
            }
        }
        if (eeJres == null) {
            eeJres = new ArrayMap();
            IExecutionEnvironment[] installedEEs = JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments();
            for (IExecutionEnvironment ee : installedEEs) {
                eeJres.put(ee.getId(), ee);
            }
        }
    }

    public static void clear() {
        jres = null;
        eeJres = null;
    }

    public static ArrayMap getJREs() {
        init();
        return jres;
    }

    public static String[] getInstalledVmNames() {
        init();
        String[] ary = new String[jres.size()];
        for (int i = 0; i < jres.size(); i++) {
            ary[i] = jres.getKey(i).toString();
        }
        return ary;
    }

    public static String[] getExecutionEnvironmentNames() {
        IExecutionEnvironment[] installedEEs = JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments();
        Arrays.sort(installedEEs, new Comparator<IExecutionEnvironment>() {
            public int compare(IExecutionEnvironment arg0, IExecutionEnvironment arg1) {
                return Policy.getComparator().compare(arg0.getId(), arg1.getId());
            }
        });
        String[] eeLabels= new String[installedEEs.length];
        String[] eeCompliance= new String[installedEEs.length];
        for (int i= 0; i < installedEEs.length; i++) {
            eeLabels[i]= installedEEs[i].getId();
            eeCompliance[i]= JavaModelUtil.getExecutionEnvironmentCompliance(installedEEs[i]);
        }
        return eeLabels;
    }
    
    public static String getDefaultEEName() {
        IVMInstall defaultVM = JavaRuntime.getDefaultVMInstall();
        
        IExecutionEnvironment[] installedEEs = JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments();
        if (defaultVM != null) {
            for (IExecutionEnvironment installedEE : installedEEs) {
                IVMInstall eeDefaultVM = installedEE.getDefaultVM();
                if (eeDefaultVM != null && defaultVM.getId().equals(eeDefaultVM.getId())) {
                    return installedEE.getId();
                }
            }
        }
        
        String defaultCC;
        if (defaultVM instanceof IVMInstall2) {
            defaultCC= JavaModelUtil.getCompilerCompliance((IVMInstall2)defaultVM, JavaCore.VERSION_1_4);
        } else {
            defaultCC= JavaCore.VERSION_1_4;
        }
        
        for (int i= 0; i < installedEEs.length; i++) {
            String eeCompliance= JavaModelUtil.getExecutionEnvironmentCompliance(installedEEs[i]);
            if (defaultCC.endsWith(eeCompliance)) {
                return installedEEs[i].getId();
            }
        }
        
        return "J2SE-1.5";
    }

    public static String getJREContainer(String name) {
        init();
        IPath path = new Path(JavaRuntime.JRE_CONTAINER);
        if (name != null) {
            IVMInstall vm = (IVMInstall) jres.get(name);
            if(vm == null) {
                IExecutionEnvironment ee = (IExecutionEnvironment) eeJres.get(name);
                path = JavaRuntime.newJREContainerPath(ee);
            } else {
                path = path.append(vm.getVMInstallType().getId());
                path = path.append(vm.getName());
            }
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
        IVMInstall2 vm = (IVMInstall2) jres.get(name);
        if (vm == null) {
            IExecutionEnvironment ee = (IExecutionEnvironment) eeJres.get(name);
            return JavaModelUtil.getExecutionEnvironmentCompliance(ee);
        }
        String version = vm.getJavaVersion();
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
