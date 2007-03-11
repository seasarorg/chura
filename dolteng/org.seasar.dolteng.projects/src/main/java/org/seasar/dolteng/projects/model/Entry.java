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
package org.seasar.dolteng.projects.model;

import java.util.HashMap;
import java.util.Map;

public class Entry {
    public Map<String, String> attribute = new HashMap<String, String>();

    public String getKind() {
        return this.attribute.get("kind");
    }

    public String getPath() {
        return this.attribute.get("path");
    }

    public int hashCode() {
        return getPath().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof Entry) {
            Entry e = (Entry) obj;
            return getPath().equals(e.getPath());
        }
        return false;
    }
}