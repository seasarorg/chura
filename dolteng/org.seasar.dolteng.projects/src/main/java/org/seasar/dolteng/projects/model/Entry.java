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
package org.seasar.dolteng.projects.model;

import java.io.Serializable;
import java.util.HashMap;

import org.seasar.dolteng.eclipse.loader.ResourceLoader;
import org.seasar.framework.util.StringUtil;

@SuppressWarnings("serial")
public class Entry implements Serializable {

    public HashMap<String, String> attribute = new HashMap<String, String>();
    public String value;

    private ResourceLoader loader;

    public Entry(ResourceLoader loader) {
        this.loader = loader;
    }

    public ResourceLoader getLoader() {
        return loader;
    }

    public String getKind() {
        String s = this.attribute.get("kind");
        return StringUtil.isEmpty(s) ? "path" : s;
    }

    public String getPath() {
        return this.attribute.get("path");
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Entry) {
            Entry e = (Entry) obj;
            return getPath().equals(e.getPath());
        }
        return false;
    }
}