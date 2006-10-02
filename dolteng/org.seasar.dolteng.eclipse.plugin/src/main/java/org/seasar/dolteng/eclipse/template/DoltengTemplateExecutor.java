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

import org.seasar.dolteng.core.template.impl.FreeMarkerTemplateExecutor;

import freemarker.template.Configuration;

/**
 * @author taichi
 * 
 */
public class DoltengTemplateExecutor extends FreeMarkerTemplateExecutor {

    /**
     * @param config
     */
    public DoltengTemplateExecutor(String templateType) {
        super(createConfig(templateType));
    }

    private static Configuration createConfig(String type) {
        Configuration config = new Configuration();
        config.setTemplateLoader(new DoltengTemplateLoader(type));
        return config;
    }
}
