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
package org.seasar.dolteng.core.template.impl;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.seasar.dolteng.core.template.RootModel;
import org.seasar.dolteng.core.template.TemplateExecutor;
import org.seasar.dolteng.core.template.TemplateHandler;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * @author taichi
 * 
 */
public class FreeMarkerTemplateExecutor implements TemplateExecutor {

    private Configuration config;

    public FreeMarkerTemplateExecutor(Configuration config) {
        super();
        this.config = config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.Template#proceed(org.seasar.dolteng.core.template.TemplateHandler)
     */
    public void proceed(TemplateHandler handler) {
        String[] names = handler.getResourceTypes();
        for (int i = 0; i < names.length; i++) {
            execute(names[i], handler);
        }
    }

    protected void execute(String name, TemplateHandler handler) {
        OutputStream out = null;
        RootModel root = handler.getProcessModel(name);
        try {
            Template t = this.config.getTemplate(name);
            handler.begin(root);
            out = handler.open(root);
            t.process(root, new BufferedWriter(new OutputStreamWriter(out)));
            handler.done(root);
        } catch (Exception e) {
            handler.fail(root, e);
            throw new RuntimeException(e);
        } finally {
            handler.close(out);
        }
    }
}
