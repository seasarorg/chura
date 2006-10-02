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

import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.Plugin;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.PropertiesUtil;

import freemarker.cache.URLTemplateLoader;

/**
 * @author taichi
 * 
 */
public class DoltengTemplateLoader extends URLTemplateLoader {

    private Properties props;

    /**
     * 
     */
    public DoltengTemplateLoader(String propertiesPath) {
        super();
        props = PropertiesUtil.load(propertiesPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see freemarker.cache.URLTemplateLoader#getURL(java.lang.String)
     */
    protected URL getURL(String name) {
        Plugin plugin = DoltengCore.getDefault();
        return plugin.getBundle().getEntry(props.getProperty(name));
    }

}
