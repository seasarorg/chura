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
package org.seasar.dolteng.eclipse.loader.impl;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;

import jp.javelindev.mvnbeans.Artifact;
import jp.javelindev.mvnbeans.LocalRepositoryNotFoundException;
import jp.javelindev.mvnbeans.RepositoryIOException;
import jp.javelindev.mvnbeans.RepositoryManager;
import jp.javelindev.mvnbeans.Artifact.ResourceType;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengCommonPreferences;

/**
 * @author daisuke
 */
@SuppressWarnings("serial")
public class MavenResourceLoader extends CompositeResourceLoader {

    protected String remoteRepos[] = { "http://repo1.maven.org/maven2/",
            "http://maven.seasar.org/maven2/" };

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.ResouceLoader#getResouce(java.lang.String)
     */
    @Override
    public URL getResouce(String path) {
        URL result = null;

        if (path != null) {
            result = super.getResouce(path);
            
            DoltengCommonPreferences pref = DoltengCore.getPreferences();
            String localRepospath = pref.getMavenReposPath();
            
            if (result == null && pref.isDownloadOnline()
                    /* && new File(localRepospath).exists() */) {
                final String[] artifactData = path.split("[ ]*,[ ]*");
                if (artifactData.length == 4) {
                    Artifact artifact = getArtifact(artifactData[0],
                            artifactData[1], artifactData[2], localRepospath);
                    try {
                        if("src".equals(artifactData[3])) {
                            result = artifact.getFileURL(ResourceType.SRC_JAR);
                        } else {
                            result = artifact.getFileURL(ResourceType.JAR);
                        }
                    } catch (RepositoryIOException e) {
                        DoltengCore.log(e);
                    } catch (FileNotFoundException e) {
                        if("src".equals(artifactData[3]) == false) {
                            DoltengCore.log(e);
                        }
                    }
                }
            }
        }

        return result;
    }

    private Artifact getArtifact(String groupId, String artifactId,
            String version, String localReposPath) {
        final Artifact artifact;
        Properties prop = new Properties();
        prop.setProperty("repositories", StringUtils.join(remoteRepos, ","));
        prop.setProperty("localrepository", "file://" + localReposPath);

        RepositoryManager mgr = RepositoryManager.getInstance(true, prop);
        artifact = new Artifact(groupId, artifactId, version, mgr);

        if (! artifact.isInLocalRepository()) {
            try {
                downloadArtifact(artifact);
            } catch (InvocationTargetException e) {
                DoltengCore.log(e);
            } catch (InterruptedException e) {
                DoltengCore.log(e);
            }
        }
        return artifact;
    }

    private void downloadArtifact(Artifact artifact)
            throws InvocationTargetException, InterruptedException {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        dialog.run(true, false, new RunnableDownload(Messages.bind(
                Messages.DOWNLOAD_FROM_MAVEN_REPOS, new String[] {
                        artifact.getArtifactId(), artifact.getVersion() }),
                artifact));
    }

    private class RunnableDownload implements IRunnableWithProgress {

        private final String message;

        private final Artifact artifact;

        private RunnableDownload(String message, Artifact artifact) {
            this.message = message;
            this.artifact = artifact;
        }

        public void run(IProgressMonitor monitor) {
            monitor.beginTask(message, IProgressMonitor.UNKNOWN);
            try {
                artifact.download();
            } catch (LocalRepositoryNotFoundException e) {
                DoltengCore.log("local repository not found.", e);
            }
            monitor.done();
        }
    }

}
