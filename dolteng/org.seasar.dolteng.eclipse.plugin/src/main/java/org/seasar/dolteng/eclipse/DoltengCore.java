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
package org.seasar.dolteng.eclipse;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.osgi.framework.BundleContext;
import org.seasar.dolteng.core.template.TemplateExecutor;
import org.seasar.dolteng.core.types.AsTypeResolver;
import org.seasar.dolteng.core.types.MxComponentValueResolver;
import org.seasar.dolteng.core.types.TypeMappingRegistry;
import org.seasar.dolteng.eclipse.nature.DoltengNature;
import org.seasar.dolteng.eclipse.preferences.DoltengCommonPreferences;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.preferences.impl.DoltengCommonPreferencesImpl;
import org.seasar.dolteng.eclipse.template.DoltengTemplateExecutor;
import org.seasar.dolteng.eclipse.util.LogUtil;
import org.seasar.framework.util.URLUtil;

/**
 * The main plugin class to be used in the desktop.
 */
public class DoltengCore extends Plugin {

    // The shared instance.
    private static DoltengCore plugin;

    private static DoltengCommonPreferences preferences;
    
    /**
     * The constructor.
     */
    public DoltengCore() {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        URLUtil.disableURLCaches();
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
    }

    /**
     * Returns the shared instance.
     * 
     * @return singleton instance
     */
    public static DoltengCore getDefault() {
        return plugin;
    }

    public static void log(Throwable throwable) {
        LogUtil.log(getDefault(), throwable);
    }

    public static void log(String msg) {
        LogUtil.log(getDefault(), msg);
    }

    public static void log(String msg, Throwable throwable) {
        LogUtil.log(getDefault(), msg, throwable);
    }

    public static DoltengProject getProject(IJavaProject project) {
        return getProject(project.getProject());
    }

    public static DoltengProject getProject(IProject project) {
        return DoltengNature.getInstance(project);
    }

    public static DoltengCommonPreferences getPreferences() {
        if(preferences == null) {
            preferences = new DoltengCommonPreferencesImpl();
        }
        return preferences;
    }

    public static DoltengPreferences getPreferences(IJavaProject project) {
        if (project == null) {
            return null;
        }
        return getPreferences(project.getProject());
    }

    public static DoltengPreferences getPreferences(IProject project) {
        DoltengProject dp = getProject(project);
        if (dp != null) {
            return dp.getProjectPreferences();
        }
        return null;
    }

    public static IDialogSettings getDialogSettings() {
        IDialogSettings settings = new DialogSettings("Dolteng");
        try {
            File f = getDialogSettingsPath();
            if (f.exists()) {
                settings.load(f.getCanonicalPath());
            }
        } catch (Exception e) {
            log(e);
        }
        return settings;
    }

    public static void saveDialogSettings(IDialogSettings settings) {
        try {
            if (settings == null) {
                return;
            }
            File f = getDialogSettingsPath();
            if (f.exists()) {
                f.delete();
            }
            settings.save(f.getCanonicalPath());
        } catch (Exception e) {
            log(e);
        }
    }

    private static File getDialogSettingsPath() {
        IPath path = getDefault().getStateLocation();
        path = path.append("settings.xml");
        return path.toFile();
    }

    public static TemplateExecutor getTemplateExecutor() {
        return new DoltengTemplateExecutor();
    }

    public static TypeMappingRegistry getTypeMappingRegistry(
            IJavaProject project) {
        return getTypeMappingRegistry(project.getProject());
    }

    public static TypeMappingRegistry getTypeMappingRegistry(IProject project) {
        if (project != null) {
            DoltengProject p = getProject(project);
            if (p != null) {
                return p.getTypeMappingRegistry();
            }
        }
        return null;
    }

    public static AsTypeResolver getAsTypeResolver(IJavaProject project) {
        return getAsTypeResolver(project.getProject());
    }

    public static AsTypeResolver getAsTypeResolver(IProject project) {
        if (project != null) {
            DoltengProject p = getProject(project);
            if (p != null) {
                return p.getAsTypeResolver();
            }
        }
        return null;
    }

    public static MxComponentValueResolver getMxResolver(IJavaProject project) {
        return getMxResolver(project.getProject());
    }

    public static MxComponentValueResolver getMxResolver(IProject project) {
        if (project != null) {
            DoltengProject p = getProject(project);
            if (p != null) {
                return p.getMxComponentValueResolver();
            }
        }
        return null;
    }
}