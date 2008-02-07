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

import org.seasar.dolteng.core.template.impl.FreeMarkerTemplateExecutor;
import org.seasar.dolteng.eclipse.loader.ResourceLoader;
import org.seasar.dolteng.eclipse.loader.impl.DoltengResourceLoader;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;

/**
 * @author taichi
 * 
 */
public class DoltengTemplateExecutor extends FreeMarkerTemplateExecutor {

    public DoltengTemplateExecutor() {
        super(createConfig(new DoltengResourceLoader()));
    }

    public DoltengTemplateExecutor(ResourceLoader loader) {
        super(createConfig(loader));
    }

    private static Configuration createConfig(ResourceLoader loader) {
        Configuration config = new Configuration();
        config.setLocalizedLookup(false);
        config.setTemplateLoader(new TemplateLoaderAdaptor(loader));
        config.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
        return config;
    }
}