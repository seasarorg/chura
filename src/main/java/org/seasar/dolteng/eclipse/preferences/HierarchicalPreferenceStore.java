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

package org.seasar.dolteng.eclipse.preferences;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * @author taichi
 * 
 */
public class HierarchicalPreferenceStore extends ScopedPreferenceStore {

	private static final String KEY_CHILDREN = "prefs.children";

	protected IScopeContext context;

	protected String qualifier;

	private Map children = new HashMap();

	/**
	 * @param context
	 * @param qualifier
	 */
	public HierarchicalPreferenceStore(IScopeContext context, String qualifier) {
		super(context, qualifier);
		this.context = context;
		this.qualifier = qualifier;
		loadChild();
	}

	protected void loadChild() {
		String names = getString(KEY_CHILDREN);
		String[] ary = names.split(",");
		for (int i = 0; i < ary.length; i++) {
			addChild(ary[i], new HierarchicalPreferenceStore(this.context,
					ary[i]));
		}
	}

	public void addChild(String name, IPersistentPreferenceStore store) {
		children.put(name, store);
	}

	public IPersistentPreferenceStore[] getChildren() {
		Collection values = children.values();
		return (IPersistentPreferenceStore[]) values
				.toArray(new IPersistentPreferenceStore[values.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.preferences.ScopedPreferenceStore#save()
	 */
	public void save() throws IOException {
		StringBuffer stb = new StringBuffer();
		for (Iterator i = this.children.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			stb.append(entry.getKey());
			stb.append(',');
			IPersistentPreferenceStore store = (IPersistentPreferenceStore) entry
					.getValue();
			store.save();
		}
		setValue(KEY_CHILDREN, stb.toString());
		super.save();
	}

}
