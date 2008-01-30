package org.seasar.dolteng.projects.handler.impl.customizer;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * customizer.diconのモデル
 * @author daisuke
 */
public class CustomizerDiconModel {
	
	private static CustomizerDiconModel singleton;
	
	@SuppressWarnings("unchecked")
	private SortedSet<ComponentModel> components = new TreeSet<ComponentModel>();
	
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
	
	public Collection<ComponentModel> getComponents() {
		return components;
	}
	
	public void createComponent(String name, String clazz) {
		if(name == null) {
			throw new IllegalArgumentException();
		}
		if(getComponent(name) == null) {
			components.add(new ComponentModel(name, clazz));
		}
	}
	
	public void addCustomizerTo(String componentName, String customizerName, String aspect) {
		ComponentModel component = getComponent(componentName);
		if(component == null) {
			throw new IllegalStateException();
		}
		component.addCustomizer(customizerName, aspect);
	}
	
	public void removeCustomizerFrom(String componentName, String customizerName) {
		ComponentModel component = getComponent(componentName);
		if(component != null) {
			component.removeCustomizer(customizerName);
		}
	}
	
	public ComponentModel getComponent(String componentName) {
		for(ComponentModel component : components) {
			if(componentName.equals(component.getName())) {
				return component;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return components.toString();
	}
}
