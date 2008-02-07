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
package org.seasar.dolteng.eclipse.convention;

import java.util.regex.Pattern;

/**
 * @author taichi
 * 
 */
public class NamingUtil {

    private static Pattern versionPattern = Pattern.compile("version[_]?(No)?",
            Pattern.CASE_INSENSITIVE);

    public static boolean isVersionNo(String column) {
        return versionPattern.matcher(column).matches();
    }
}
