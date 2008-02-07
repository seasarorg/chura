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
package org.seasar.dolteng.core.kuina;

import junit.framework.TestCase;

/**
 * @author taichi
 * 
 */
public class KuinaEmulatorTest extends TestCase {

    /**
     * Test method for
     * {@link org.seasar.dolteng.core.kuina.KuinaEmulator#isQueryPatterns(java.lang.String)}.
     */
    public void testIsQueryPatterns() {
        assertTrue(KuinaEmulator.isQueryPatterns("orderBy"));
        assertTrue(KuinaEmulator.isQueryPatterns("orderby"));
        assertTrue(KuinaEmulator.isQueryPatterns("firstResult"));
        assertTrue(KuinaEmulator.isQueryPatterns("maxResults"));
    }

    /**
     * Test method for
     * {@link org.seasar.dolteng.core.kuina.KuinaEmulator#splitPropertyName(java.lang.String)}.
     */
    public void testSplitPropertyName() {
        String paramName = "hoge$fuga$name_LT";
        String[] result = KuinaEmulator.splitPropertyName(paramName);
        assertEquals("hoge", result[0]);
        assertEquals("fuga", result[1]);
        assertEquals("name", result[2]);
    }

}
