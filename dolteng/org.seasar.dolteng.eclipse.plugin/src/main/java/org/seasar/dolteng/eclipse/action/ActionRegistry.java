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
package org.seasar.dolteng.eclipse.action;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;

/**
 * @author taichi
 * 
 */
public class ActionRegistry {

    private Map<String, IAction> actions = new Hashtable<String, IAction>();

    public void register(IAction action) {
        this.actions.put(action.getId(), action);
    }

    public void run(String actionId) {
        if (this.actions.containsKey(actionId)) {
            this.find(actionId).run();
        }
    }

    public void runWithEvent(String actionId, Event event) {
        if (this.actions.containsKey(actionId)) {
            this.find(actionId).runWithEvent(event);
        }
    }

    public IAction find(String actionId) {
        return this.actions.get(actionId);
    }
}
