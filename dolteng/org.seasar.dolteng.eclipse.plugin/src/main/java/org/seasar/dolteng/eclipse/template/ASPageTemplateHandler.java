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
package org.seasar.dolteng.eclipse.template;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.seasar.dolteng.core.template.TemplateConfig;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.impl.AsModel;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.framework.convention.NamingConvention;

/**
 * @author taichi
 * 
 */
public class ASPageTemplateHandler extends AbstractTemplateHandler {

    private int templateCount = 0;

    private IFile generated;

    public ASPageTemplateHandler(IFile mxml, IFile asdto,
            IProgressMonitor monitor) {
        super(mxml.getProject(), monitor, new AsModel(createVariables(mxml,
                asdto)));
    }

    private static Map<String, String> createVariables(IFile mxml, IFile asdto) {
        Map<String, String> var = new HashMap<String, String>();
        var.put("mxml", mxml.getFullPath().removeFileExtension().lastSegment());
        var.put("dtoname", asdto.getFullPath().removeFileExtension()
                .lastSegment());

        DoltengPreferences pref = DoltengCore.getPreferences(mxml.getProject());
        IPath src = pref.getFlexSourceFolderPath();
        IPath dtopath = asdto.getFullPath();
        String dtopkgname = "";
        if (src.isPrefixOf(dtopath)) {
            IPath p = dtopath.removeFirstSegments(src.segmentCount());
            dtopkgname = p.toString().replace('/', '.');
        }
        var.put("dtopackagename", dtopkgname);

        src = src.removeFirstSegments(1);
        var.put("flexsrcroot", src.toString());

        NamingConvention nc = pref.getNamingConvention();
        String[] names = nc.getRootPackageNames();
        if (names != null && 0 < names.length) {
            var.put("rootpackagename", names[0]);
            var.put("rootpackagepath", names[0].replace('.', '/'));
        }

        var.put("subapplicationrootpackagename", nc
                .getSubApplicationRootPackageName());
        var.put("pagepackagename", mxml.getParent().getName());
        return var;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#begin()
     */
    public void begin() {
        monitor.beginTask(Messages.GENERATE_CODES, templateCount);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#getTemplateConfigs()
     */
    public TemplateConfig[] getTemplateConfigs() {
        URL url = DoltengCore.getDefault().getBundle().getEntry(
                "template/fm/flex2_page.xml");
        TemplateConfig[] loaded = TemplateConfig.loadConfigs(url);
        templateCount = loaded.length;
        return loaded;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#open(org.seasar.dolteng.core.template.TemplateConfig)
     */
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
                this.generated = f;
                return new FileOutputStream(f.getLocation().toFile());
            } else {
                return null;
            }
        } catch (Exception e) {
            DoltengCore.log(e);
            throw new RuntimeException(e);
        } finally {
            monitor.worked(1);
        }
    }

    public IFile getGenarated() {
        return generated;
    }

}
