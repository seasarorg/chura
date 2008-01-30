package org.seasar.dolteng.projects.handler.impl.customizer;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO describe
 * @author daisuke
 */
public class ComponentModel {

	private String name;
	private String clazz;
	
	private List<CustomizerModel> customizers = new ArrayList<CustomizerModel>();
	
	/**
	 * コンストラクタ。
	 * @param name
	 * @param clazz
	 * @category instance creation
	 */
	public ComponentModel(String name, String clazz) {
		this.name = name;
		this.clazz = clazz == null ? "org.seasar.framework.container.customizer.CustomizerChain" : clazz;
	}

	public String getName() {
		return name;
	}

	public String getClazz() {
		return clazz;
	}

	public List<CustomizerModel> getCustomizers() {
		return customizers;
	}

	public void addCustomizer(String name, String aspect) {
		CustomizerModel customizer = new CustomizerModel(name, aspect);
		if(! customizers.contains(customizer)) {
			customizers.add(customizer);
		}
	}

	public void removeCustomizer(String customizerName) {
		// TODO なんかダサくない？
		CustomizerModel removeTarget = null;
		for(CustomizerModel customizer : customizers) {
			if(customizerName.equals(customizer.getArg())) {
				removeTarget = customizer;
				break;
			}
		}
		customizers.remove(removeTarget);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ComponentModel other = (ComponentModel) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return name + customizers;
	}
}
