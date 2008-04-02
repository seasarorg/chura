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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.seasar.framework.util.StringUtil;

public class FacetConfig implements FacetDisplay {

    private IConfigurationElement project;

    private String displayOrder;

    public FacetConfig(IConfigurationElement e) {
        project = e;
        displayOrder = e.getAttribute("displayOrder");
    }

    public int compareTo(Object o) {
        if (o instanceof FacetConfig) {
            FacetConfig other = (FacetConfig) o;
            return displayOrder.compareTo(other.displayOrder);
        }
        return 0;
    }

    public IConfigurationElement getConfigurationElement() {
        return project;
    }

    public boolean isSelectableFacet() {
        return !StringUtil.isEmpty(displayOrder);
    }

    public Set<String> getJres() {
        Set<String> jres = new HashSet<String>();
        for (IConfigurationElement e : project.getChildren()) {
            if ("if".equals(e.getName())) {
                String jreStr = e.getAttribute("jre");
                if (jres != null) {
                    for (String jre : jreStr.split("[ ]*,[ ]*")) {
                        jres.add(jre);
                    }
                }
            }
        }
        return jres;
    }

    public String getCategory() {
        String[] cat = displayOrder.split("\\d*$");
        if (cat.length == 0 || cat[0].length() != 2) {
            return null;
        }
        return cat[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.projects.model.FacetDisplay#getDescription()
     */
    public String getDescription() {
        return project.getAttribute("description"); // FIXME : イマイチ
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.projects.model.FacetDisplay#getId()
     */
    public String getId() {
        return project.getAttribute("id");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.projects.model.FacetDisplay#getName()
     */
    public String getName() {
        return project.getAttribute("name");
    }

    @Override
    public String toString() {
        return getName();
    }
}