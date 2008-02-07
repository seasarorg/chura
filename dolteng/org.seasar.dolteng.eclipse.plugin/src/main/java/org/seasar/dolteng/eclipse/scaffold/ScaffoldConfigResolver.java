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
package org.seasar.dolteng.eclipse.scaffold;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.ScaffoldDisplay;
import org.seasar.eclipse.common.util.ExtensionAcceptor;

/**
 * @author taichi
 * 
 */
public class ScaffoldConfigResolver {

    protected IProject project;

    protected Map<String, ScaffoldConfig> configs = new HashMap<String, ScaffoldConfig>();

    public ScaffoldConfigResolver(IProject project) {
        this.project = project;
    }

    public void initialize() {
        ExtensionAcceptor.accept(Constants.ID_PLUGIN, "scaffold",
                new ExtensionAcceptor.ExtensionVisitor() {
                    public void visit(IConfigurationElement e) {
                        try {
                            if ("templates".equals(e.getName())) {
                                ScaffoldConfigEnabler enabler = (ScaffoldConfigEnabler) e
                                        .createExecutableExtension("enablement");
                                if (enabler.enableFor(project)) {
                                    configs.put(e.getAttribute("id"),
                                            new ScaffoldConfig(e));
                                }
                            }
                        } catch (Exception ex) {
                            DoltengCore.log(ex);
                        }
                    }
                });
    }

    public ScaffoldConfig getConfig(String id) {
        return this.configs.get(id);
    }

    public ScaffoldDisplay[] getScaffolds() {
        Collection<ScaffoldConfig> configs = this.configs.values();
        return configs.toArray(new ScaffoldDisplay[configs.size()]);
    }
}
