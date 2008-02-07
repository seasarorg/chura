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
import org.seasar.dolteng.eclipse.viewer.LabelStrategy;

/**
 * @author taichi
 * 
 */
public class NullLabelStrategy implements LabelStrategy {

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.views.LabelStrategy#convertText(java.lang.String)
     */
    public String convertText(ContentDescriptor descriptor) {
        return descriptor.getText();
    }

}
