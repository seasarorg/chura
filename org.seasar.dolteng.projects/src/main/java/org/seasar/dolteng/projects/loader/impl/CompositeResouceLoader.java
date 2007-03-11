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
package org.seasar.dolteng.projects.loader.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.seasar.dolteng.projects.Constants;
import org.seasar.dolteng.projects.loader.ResouceLoader;

/**
 * @author taichi
 * 
 */
public class CompositeResouceLoader implements ResouceLoader {

    protected final List<Bundle> bundles = new ArrayList<Bundle>();

    public CompositeResouceLoader() {
        bundles.add(Platform.getBundle(Constants.ID_PLUGIN));
        bundles.add(Platform
                .getBundle(org.seasar.dolteng.eclipse.Constants.ID_PLUGIN));
        bundles.add(Platform
                .getBundle("org.seasar.dolteng.projects.dependencies"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.ResouceLoader#getResouce(java.lang.String)
     */
    public URL getResouce(String path) {
        URL result = null;
        for (Bundle b : bundles) {
            result = b.getEntry(path);
            if (result != null) {
                break;
            }
        }
        return result;
    }

}
