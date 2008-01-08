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

import java.net.URL;

import org.seasar.dolteng.eclipse.loader.ResourceLoader;

import freemarker.cache.URLTemplateLoader;

/**
 * @author taichi
 * 
 */
public class TemplateLoaderAdaptor extends URLTemplateLoader {

    protected ResourceLoader adaptee;

    public TemplateLoaderAdaptor(ResourceLoader adaptee) {
        super();
        this.adaptee = adaptee;
    }

    /*
     * (non-Javadoc)
     * 
     * @see freemarker.cache.URLTemplateLoader#getURL(java.lang.String)
     */
    @Override
    protected URL getURL(String name) {
        return this.adaptee.getResouce(name);
    }
}
