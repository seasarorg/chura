package org.seasar.dolteng.eclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.osgi.framework.BundleContext;
import org.seasar.dolteng.eclipse.nature.DoltengNature;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
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
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        try {
            IProject[] projects = ProjectUtil.getAllProjects();
            for (int i = 0; i < projects.length; i++) {
                IProject p = projects[i];
                DoltengNature nature = DoltengNature.getInstance(p);
                if (nature != null) {
                    nature.destroy();
                }
            }
        } finally {
            plugin = null;
        }
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
        return getPreferences(project.getProject());
    }

    public static DoltengProjectPreferences getPreferences(IProject project) {
        DoltengProject dp = getProject(project);
        if (dp != null) {
            return dp.getProjectPreferences();
        }
        return null;
    }

}
