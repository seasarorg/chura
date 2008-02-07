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
package org.seasar.dolteng.core.types.impl;

import java.util.HashMap;
import java.util.Map;

import jp.aonir.fuzzyxml.FuzzyXMLElement;

import org.seasar.dolteng.core.types.MxComponentValueResolver;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class MxComponentValueResolverImpl implements MxComponentValueResolver {

    private Map<String, String> mapping = new HashMap<String, String>();

    public void initialize() {
        mapping.put("mx:CheckBox", "selected");
        mapping.put("mx:ColorPicker", "value");
        mapping.put("mx:ComboBox", "selectedItem.data");
        mapping.put("mx:DataGrid", "selectedItem.data");
        mapping.put("mx:DateChooser", "selectedDate");
        mapping.put("mx:DateField", "selectedDate");
        mapping.put("mx:HSlider", "value");
        mapping.put("mx:HorizontalList", "selectedItem.data");
        mapping.put("mx:Label", "text");
        mapping.put("mx:List", "selectedItem.data");
        mapping.put("mx:NumericStepper", "value");
        mapping.put("mx:RadioButton", "value");
        mapping.put("mx:RadioButtonGroup", "selectedValue");
        mapping.put("mx:Text", "text");
        mapping.put("mx:TextArea", "text");
        mapping.put("mx:TextInput", "text");
        mapping.put("mx:TileList", "selectedItem.data");
        mapping.put("mx:VSlider", "value");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.types.MxComponentValueResolver#resolveValueAttribute(jp.aonir.fuzzyxml.FuzzyXMLElement)
     */
    public String resolveValueAttribute(FuzzyXMLElement element) {
        String result = "value";
        if (element != null) {
            String s = mapping.get(element.getName());
            if (StringUtil.isEmpty(s) == false) {
                result = s;
            }
        }
        return result;
    }

}
