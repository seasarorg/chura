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
package org.seasar.dolteng.eclipse.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author taichi
 * 
 */
public class ScriptingUtilTest extends TestCase {

    /**
     * Test method for
     * {@link org.seasar.dolteng.eclipse.util.ScriptingUtil#resolveString(java.lang.String, java.util.Map)}.
     */
    public void testResolveString() {
        String before = "aaaaa${cc}bbb${ddd}aaaa${ee}";
        Map<String, String> m = new HashMap<String, String>();
        m.put("cc", "CC");
        m.put("ddd", "${XXX}");
        m.put("ee", "EE");
        String after = ScriptingUtil.resolveString(before, m);
        assertEquals("aaaaaCCbbb${XXX}aaaaEE", after);
    }

    public void testResolveString2() {
        String before = "${flexsrcroot}/${rootpackagepath}/${subapplicationrootpackagename}/${pagepackagename}";

        Map<String, String> m = new HashMap<String, String>();
        m.put("flexsrcroot", "zz01/WEB-INF/src/main/flex");
        m.put("rootpackagepath", "zz01");
        m.put("rootpackagename", "zz01");
        m.put("dtopackagename", "zz01.dto");
        m.put("subapplicationrootpackagename", "web");
        m.put("pagepackagename", "emp");
        String after = ScriptingUtil.resolveString(before, m);
        assertEquals("zz01/WEB-INF/src/main/flex/zz01/web/emp", after);
    }

}
