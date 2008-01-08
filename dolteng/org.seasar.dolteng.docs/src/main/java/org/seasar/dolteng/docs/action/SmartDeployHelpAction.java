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
package org.seasar.dolteng.docs.action;

/**
 * @author taichi
 */
public class SmartDeployHelpAction extends AbstractHelpAction {

    // plugin.xmlだけで何とかしてぇなぁ…
    private static final String PATH = "/ja/DIContainer.html#SMARTdeploy";

    @Override
    protected String getLocalHelpPath() {
        return "/docs/s2container" + PATH;
    }

    @Override
    protected String getRemoteHelpURL() {
        return "http://s2container.seasar.org/2.4" + PATH;
    }
}