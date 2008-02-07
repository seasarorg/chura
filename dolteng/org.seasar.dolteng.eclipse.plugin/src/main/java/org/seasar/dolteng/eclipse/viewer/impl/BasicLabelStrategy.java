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
package org.seasar.dolteng.eclipse.viewer.impl;

import org.seasar.dolteng.eclipse.model.ContentDescriptor;
import org.seasar.dolteng.eclipse.model.impl.ColumnNode;
import org.seasar.dolteng.eclipse.viewer.LabelStrategy;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class BasicLabelStrategy implements LabelStrategy {

    public String convertText(ContentDescriptor descriptor) {
        String result = descriptor.getText();
        if (StringUtil.isEmpty(result) == false) {
            result = result.toLowerCase();
            String[] ary = result.split("_");
            StringBuffer stb = new StringBuffer();
            for (String element : ary) {
                stb.append(StringUtil.capitalize(element));
            }
            result = stb.toString();
        }
        if (descriptor instanceof ColumnNode) {
            char chars[] = result.toCharArray();
            chars[0] = Character.toLowerCase(chars[0]);
            result = new String(chars);
        }
        return result;
    }

}
