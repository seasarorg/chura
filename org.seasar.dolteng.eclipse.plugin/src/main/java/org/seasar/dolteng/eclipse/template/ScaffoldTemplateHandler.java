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
package org.seasar.dolteng.eclipse.template;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.seasar.dolteng.core.template.TemplateConfig;
import org.seasar.dolteng.core.template.TemplateHandler;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.convention.NamingConventionMirror;
import org.seasar.dolteng.eclipse.model.impl.ScaffoldModel;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.scaffold.ScaffoldConfig;
import org.seasar.dolteng.eclipse.util.NameConverter;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.framework.util.CaseInsensitiveMap;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class ScaffoldTemplateHandler extends AbstractTemplateHandler implements
        TemplateHandler {

    private ScaffoldConfig config;

    private int templateCount = 0;

    public ScaffoldTemplateHandler(ScaffoldConfig config, IProject project,
            TableNode node, IProgressMonitor monitor) {
        super(project, monitor, new ScaffoldModel(createVariables(node
                .getMetaData().getName(), project), node));
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    protected static Map<String, String> createVariables(String tableName, IProject project) {
        Map<String, String> result = new CaseInsensitiveMap();
        DoltengPreferences pref = DoltengCore.getPreferences(project);
        result.putAll(NamingConventionMirror.toMap(pref.getNamingConvention()));
        String table = NameConverter.toCamelCase(tableName);
        result.put("table", StringUtil.decapitalize(table));
        result.put("table_capitalize", table);
        result.put("table_rdb", tableName);

        result.put("javasrcroot", pref.getDefaultSrcPath().removeFirstSegments(
                1).toString());
        result.put("resourceroot", pref.getDefaultResourcePath()
                .removeFirstSegments(1).toString());
        result.put("flexsrcroot", pref.getFlexSourceFolderPath()
                .removeFirstSegments(1).toString());

        result.put("webcontentsroot", pref.getWebContentsRoot());
        String pkg = pref.getDefaultRootPackageName();
        result.put("rootpackagename", pkg);
        result.put("rootpackagepath", pkg.replace('.', '/'));
        return result;
    }

    public void setJavaSrcRoot(String path) {
        baseModel.getConfigs().put("javasrcroot", path);
    }

    public void setRootPkg(String pkg) {
        baseModel.getConfigs().put("rootpackagename", pkg);
        baseModel.getConfigs().put("rootpackagepath", pkg.replace('.', '/'));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#getTemplateConfigs(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public TemplateConfig[] getTemplateConfigs() {
        TemplateConfig[] loaded = this.config.getTemplates();
        templateCount = loaded.length;
        return loaded;
    }

    public void begin() {
        monitor.beginTask(Messages.GENERATE_CODES, templateCount);
    }

    @SuppressWarnings("unchecked")
    public OutputStream open(TemplateConfig config) {
        try {
            IPath p = new Path(config.resolveOutputPath(baseModel.getConfigs()))
                    .append(config.resolveOutputFile(baseModel.getConfigs()));
            monitor.subTask(p.toString());

            ResourcesUtil.createDir(this.project, config
                    .resolveOutputPath(baseModel.getConfigs()));

            IFile f = project.getFile(p);
            boolean is = true;
            if (f.exists()) {
                is = false;
                if (config.isOverride()) {
                    f.delete(true, null);
                    is = true;
                }
            }
            if (is) {
                f.create(new ByteArrayInputStream(new byte[0]), true, null);
                return new FileOutputStream(f.getLocation().toFile());
            }
            return null;
        } catch (Exception e) {
            DoltengCore.log(e);
            throw new RuntimeException(e);
        } finally {
            monitor.worked(1);
        }
    }
}
