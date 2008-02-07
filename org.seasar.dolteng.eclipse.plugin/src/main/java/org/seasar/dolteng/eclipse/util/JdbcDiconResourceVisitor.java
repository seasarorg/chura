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
package org.seasar.dolteng.eclipse.util;

import java.net.URL;
import java.util.regex.Pattern;

import javax.sql.XADataSource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.jdt.core.IJavaProject;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.dolteng.eclipse.preferences.impl.ReflectiveConnectionConfig;
import org.seasar.extension.dbcp.impl.XADataSourceImpl;

public class JdbcDiconResourceVisitor implements IResourceVisitor {

    private final Pattern pattern = Pattern.compile(".*jdbc.dicon");

    final static String DICON = "s2container.dicon";

    private IJavaProject project;

    private ConnectionConfigHandler handler;

    public JdbcDiconResourceVisitor(IJavaProject project,
            ConnectionConfigHandler handler) throws Exception {
        this.project = project;
        this.handler = handler;
    }

    public boolean visit(IResource resource) {
        if (resource instanceof IFile
                && pattern.matcher(resource.getName()).matches()) {
            String diconPath = resource.getName();
            Object container = null;
            JavaProjectClassLoader loader = null;
            try {
                // hotdeploy やcooldeployを動作させない為に、
                // 空のs2container.diconをロードする。
                final URL url = Thread.currentThread().getContextClassLoader()
                        .getResource(DICON);
                loader = new JavaProjectClassLoader(this.project) {
                    @Override
                    public URL getResource(String name) {
                        if (DICON.equals(name)) {
                            return url;
                        }
                        return super.getResource(name);
                    }
                };
                Class<?> xadsImpl = loader.loadClass(XADataSourceImpl.class
                        .getName());
                container = S2ContainerUtil
                        .createS2Container(diconPath, loader);
                XADataSource[] sources = (XADataSource[]) S2ContainerUtil
                        .loadComponents(loader, container, XADataSource.class);
                if (sources != null) {
                    for (int i = 0; i < sources.length; i++) {
                        XADataSource ds = sources[i];
                        if (xadsImpl.isAssignableFrom(ds.getClass())) {
                            ConnectionConfig cc = new ReflectiveConnectionConfig(
                                    project, ds);
                            // TODO ComponentDefを読む様にする。
                            cc.setName(resource.getName()
                                    + (i < 1 ? "" : "-" + i));
                            handler.handle(cc);
                        }
                    }
                }
            } catch (Exception e) {
                DoltengCore.log(e);
            } finally {
                S2ContainerUtil.destroyS2Container(container);
                JavaProjectClassLoader.dispose(loader);
            }
        }
        return true;
    }

    public interface ConnectionConfigHandler {
        void handle(ConnectionConfig config);
    }
}