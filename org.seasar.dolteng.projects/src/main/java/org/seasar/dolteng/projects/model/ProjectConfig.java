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

import org.eclipse.core.runtime.IConfigurationElement;

public class ProjectConfig implements ProjectDisplay {

    private IConfigurationElement project;

    private String displayOrder;

    public ProjectConfig(IConfigurationElement e) {
        this.project = e;
        this.displayOrder = e.getAttribute("displayOrder");
    }

    public int compareTo(Object o) {
        if (o instanceof ProjectConfig) {
            ProjectConfig other = (ProjectConfig) o;
            return displayOrder.compareTo(other.displayOrder);
        }
        return 0;
    }

    public IConfigurationElement getConfigurationElement() {
        return this.project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.ProjectDisplay#getDescription()
     */
    public String getDescription() {
        return project.getAttribute("description"); // FIXME : イマイチ
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.ProjectDisplay#getId()
     */
    public String getId() {
        return project.getAttribute("id");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.ProjectDisplay#getName()
     */
    public String getName() {
        return project.getAttribute("name");
    }
}