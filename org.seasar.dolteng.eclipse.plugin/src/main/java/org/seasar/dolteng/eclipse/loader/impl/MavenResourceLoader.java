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

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import jp.javelindev.mvnbeans.Artifact;
import jp.javelindev.mvnbeans.LocalRepositoryNotFoundException;
import jp.javelindev.mvnbeans.RepositoryManager;

import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengCommonPreferences;

/**
 * @author taichi
 * 
 */
public class MavenResourceLoader extends CompositeResourceLoader {

    protected final Properties prop = new Properties();

    public MavenResourceLoader() {
        prop.setProperty("repositories", "http://repo1.maven.org/maven2/,http://maven.seasar.org/maven2/");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.ResouceLoader#getResouce(java.lang.String)
     */
    @Override
    public URL getResouce(String path) {
        URL result = null;
        
        if(path != null) {
            DoltengCommonPreferences pref = DoltengCore.getPreferences();
            if(pref.isDownloadOnline()) {
                prop.setProperty("localrepository", "file://" + pref.getMavenReposPath());
                String[] artifactData = path.split("[ ]*,[ ]*", 3);
                
                if(artifactData.length == 3) {
                    RepositoryManager mgr = RepositoryManager.getInstance(true, prop);
                    Artifact artifact = new Artifact(artifactData[0], artifactData[1], artifactData[2], mgr);
                    
                    try {
                        System.out.print("MavenResource [" + path + "]");
                        if(! artifact.getFileURL().getProtocol().equals("file")) {
                            System.out.print(" Downloading...");
                            artifact.download();
                            System.out.println(" finished.");
                        } else {
                            System.out.println(" Found in Local.");
                        }
                        result = artifact.getFileURL();
                    } catch (LocalRepositoryNotFoundException e) {
                        DoltengCore.log("local repository not found.", e);
                    } catch (IOException e) {
                        DoltengCore.log(path, e);
                    }
                }
            }
        }
        
        if(result == null && path != null) {
            result = super.getResouce(path);
        }
        
        return result;
    }

}
