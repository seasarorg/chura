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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class ScriptingUtil {

    public static String resolveString(String string, Map<String, String> context) {
        String result = "";
        if (StringUtil.isEmpty(string) == false) {
            Pattern p = Pattern.compile("\\$\\{[^\\$\\{\\}]*\\}");
            StringBuffer stb = new StringBuffer(string);
            Matcher m = p.matcher(stb);
            int index = 0;
            while (index < stb.length() && m.find(index)) {
                String s = m.group();
                String v = toString(context.get(s.substring(2, s.length() - 1)));
                index = m.start() + v.length();
                stb.replace(m.start(), m.end(), v);
                m = p.matcher(stb);
            }
            result = stb.toString();
        }
        return result;
    }

    public static String toString(Object o) {
        return o == null ? "" : o.toString();
    }

}
