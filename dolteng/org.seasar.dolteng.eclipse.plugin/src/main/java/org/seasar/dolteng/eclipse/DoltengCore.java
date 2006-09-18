package org.seasar.dolteng.eclipse;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.osgi.framework.BundleContext;
import org.seasar.dolteng.eclipse.marker.PageMapper;
import org.seasar.dolteng.eclipse.nature.DoltengNature;
import org.seasar.dolteng.eclipse.preferences.ConventionChangeListener;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.S2ContainerUtil;
import org.seasar.dolteng.eclipse.util.StatusUtil;
import org.seasar.framework.util.URLUtil;

/**
 * The main plugin class to be used in the desktop.
 */
public class DoltengCore extends Plugin {

    // The shared instance.
    private static DoltengCore plugin;

    /**
     * The constructor.
     */
    public DoltengCore() {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        URLUtil.disableURLCaches();
        S2ContainerUtil.initializeSingletonTeeda();
        listenResourceChangeEvent();
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        S2ContainerUtil.destroySingletonTeeda();
        plugin = null;
    }

    /**
     * Returns the shared instance.
     */
    public static DoltengCore getDefault() {
        return plugin;
    }

    public static void log(Throwable throwable) {
        IStatus status = null;
        if (throwable instanceof CoreException) {
            CoreException e = (CoreException) throwable;
            status = e.getStatus();
        } else {
            status = StatusUtil.createError(Status.ERROR, throwable);
        }
        getDefault().getLog().log(status);
    }

    public static void log(String msg) {
        IStatus status = new Status(IStatus.INFO, Constants.ID_PLUGIN,
                IStatus.OK, msg, null);
        getDefault().getLog().log(status);
    }

    public static DoltengProject getProject(IJavaProject project) {
        return getProject(project.getProject());
    }

    public static DoltengProject getProject(IProject project) {
        return DoltengNature.getInstance(project);
    }

    public static DoltengProjectPreferences getPreferences(IJavaProject project) {
        if (project == null) {
            return null;
        }
        return getPreferences(project.getProject());
    }

    public static DoltengProjectPreferences getPreferences(IProject project) {
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

    private static File getDialogSettingsPath() throws IOException {
        IPath path = getDefault().getStateLocation();
        path = path.append("settings.xml");
        return path.toFile();
    }

    private void listenResourceChangeEvent() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.addResourceChangeListener(new ConventionChangeListener(),
                IResourceChangeEvent.POST_BUILD);
        workspace.addResourceChangeListener(new PageMapper(),
                IResourceChangeEvent.POST_CHANGE);
    }

}
