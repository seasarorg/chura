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
package org.seasar.dolteng.core.template;

import java.util.Map;

import junit.framework.TestCase;

import org.seasar.framework.util.CaseInsensitiveMap;

/**
 * @author taichi
 * 
 */
public class TemplateConfigTest extends TestCase {

    private TemplateConfig config;

    private Map<String, String> values;

    /*
     * @see TestCase#setUp()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        values = new CaseInsensitiveMap();
        values.put("fugafuga", "OK");
        values.put("Piro", "OKOK");
        values.put("bbb", "BBB");
        this.config = new TemplateConfig();
        this.config.setOutputPath("hoge/fuga/${fugafuga}/moge/${piro}");
        this.config.setOutputFile("aaa${bbb}Ccc.java");
    }

    public void testResolveOutputFile() throws Exception {
        assertEquals("aaaBBBCcc.java", config.resolveOutputFile(values));
    }

    public void testResolveOutputPath() throws Exception {
        assertEquals("hoge/fuga/OK/moge/OKOK", config.resolveOutputPath(values));
    }
}
