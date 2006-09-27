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
package org.seasar.dolteng.core.teeda;

import java.util.regex.Pattern;

import org.seasar.framework.util.StringUtil;
import org.seasar.teeda.core.JsfConstants;
import org.seasar.teeda.extension.ExtensionConstants;

/**
 * @author taichi
 * 
 */
public class TeedaEmulator {

    public static final Pattern EXIST_TO_FILE_PREFIX = Pattern.compile(
            ExtensionConstants.GO_PREFIX + ".*" + "|"
                    + ExtensionConstants.JUMP_PREFIX + ".*",
            Pattern.CASE_INSENSITIVE);

    public static String calcOutCome(String s) {
        int index = 0;
        if (StringUtil.isEmpty(s) == false) {
            if (s.startsWith(ExtensionConstants.GO_PREFIX)) {
                index = 2;
            } else if (s.startsWith(ExtensionConstants.JUMP_PREFIX)) {
                index = 4;
            }
        }
        return StringUtil.decapitalize(s.substring(index));
    }

    public static final Pattern MAPPING_SKIP_ID = Pattern.compile(
            JsfConstants.MESSAGES + "|" + ".*" + ExtensionConstants.FORM_SUFFIX
                    + "|" + ".*" + ExtensionConstants.MESSAGE_SUFFIX + "|"
                    + "|" + ExtensionConstants.GO_PREFIX + ".*" + "|"
                    + ExtensionConstants.JUMP_PREFIX + ".*" + "|"
                    + ExtensionConstants.MESSAGE_SUFFIX + ".*",
            Pattern.CASE_INSENSITIVE);

}
