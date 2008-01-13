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
package org.seasar.dolteng.eclipse.scaffold;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.seasar.dolteng.core.template.TemplateConfig;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.loader.ResourceLoader;
import org.seasar.dolteng.eclipse.loader.impl.DoltengResourceLoader;
import org.seasar.dolteng.eclipse.model.ScaffoldDisplay;
import org.seasar.framework.util.BooleanConversionUtil;

/**
 * @author taichi
 * 
 */
public class ScaffoldConfig implements ScaffoldDisplay {

    private IConfigurationElement templates;

    protected ScaffoldConfig(IConfigurationElement templates) {
        this.templates = templates;
    }

    public TemplateConfig[] getTemplates() {
        IConfigurationElement[] kids = this.templates.getChildren("template");
        List<TemplateConfig> configs = new ArrayList<TemplateConfig>(
                kids.length);

        for (IConfigurationElement kid : kids) {
            TemplateConfig tc = new TemplateConfig();
            tc.setTemplatePath(kid.getAttribute("path"));
            tc.setOverride(BooleanConversionUtil.toPrimitiveBoolean(kid
                    .getAttribute("override")));
            tc.setOutputPath(kid.getAttribute("outputpath"));
            tc.setOutputFile(kid.getAttribute("outputname"));
            configs.add(tc);
        }

        return configs.toArray(new TemplateConfig[configs.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.ProjectDisplay#getDescription()
     */
    public String getDescription() {
        return templates.getAttribute("description");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.ProjectDisplay#getId()
     */
    public String getId() {
        return templates.getAttribute("id");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.ProjectDisplay#getName()
     */
    public String getName() {
        return templates.getAttribute("name");
    }

    public ResourceLoader getResourceLoader() {
        try {
            Object loader = templates
                    .createExecutableExtension("resourceLoader");
            if (loader instanceof ResourceLoader) {
                return (ResourceLoader) loader;

            }
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
        return new DoltengResourceLoader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ScaffoldDisplay o) {
        return o == null ? 0 : this.getName().compareTo(o.getName());
    }

}
