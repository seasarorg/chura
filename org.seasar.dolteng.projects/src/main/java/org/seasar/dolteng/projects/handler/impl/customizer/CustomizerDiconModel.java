package org.seasar.dolteng.projects.handler.impl.customizer;

import java.util.Map;

import org.seasar.framework.util.ArrayMap;

/**
 * TODO describe
 * @author daisuke
 */
public class CustomizerDiconModel {
	
	private static CustomizerDiconModel singleton;
	
	@SuppressWarnings("unchecked")
	private Map<String, ComponentModel> components = new ArrayMap/*<String, ComponentModel>*/();
	
	/**
	 * privateコンストラクタ。(singleton)
	 * @category instance creation
	 */
	private CustomizerDiconModel() {
	}

	public static void init() {
		singleton = null;
	}
	
	public static CustomizerDiconModel getInstance() {
		if(singleton == null) {
			singleton = new CustomizerDiconModel();
		}
		return singleton;
	}
	
	public Map<String, ComponentModel> getComponents() {
		return components;
	}
	
	public void createComponent(String name, String clazz) {
		if(name == null) {
			throw new IllegalArgumentException();
		}
		if(components.get(name) == null) {
			components.put(name, new ComponentModel(name, clazz));
		}
	}
	
	public void addCustomizerTo(String componentName, String customizerName, String aspect) {
		ComponentModel component = components.get(componentName);
		if(component == null) {
			throw new IllegalStateException();
		}
		component.addCustomizer(customizerName, aspect);
	}
	
	public void removeCustomizerFrom(String componentName, String customizerName) {
		ComponentModel component = components.get(componentName);
		if(component != null) {
			component.removeCustomizer(customizerName);
		}
	}

	@Override
	public String toString() {
		return components.values().toString();
	}
}
