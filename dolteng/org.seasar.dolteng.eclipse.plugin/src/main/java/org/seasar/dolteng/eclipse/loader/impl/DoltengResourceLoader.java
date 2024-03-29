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
package org.seasar.dolteng.eclipse.loader.impl;

import java.net.URL;

import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.loader.ResourceLoader;

/**
 * @author taichi
 * 
 */
@SuppressWarnings("serial")
public class DoltengResourceLoader implements ResourceLoader {

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.loader.ResourceLoader#getResouce(java.lang.String)
     */
    public URL getResouce(String path) {
        return DoltengCore.getDefault().getBundle().getEntry(path);
    }

}
