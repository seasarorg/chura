package org.seasar.dolteng.projects.handler.impl.dicon;

import static org.seasar.dolteng.projects.handler.impl.dicon.DiconBuilder.NL;

import java.util.ArrayList;
import java.util.List;

/**
 * diconファイルで定義されるcomponentのモデル
 * @author daisuke
 */
public class ComponentModel extends ComponentsChild implements Comparable<ComponentsChild>, CustomizerConstant {

	private static List<String> priority = new ArrayList<String>();
	
	private String name;
	private String clazz;
	
	private List<CustomizerModel> customizers = new ArrayList<CustomizerModel>();
	
	static {
		priority.add(PAGE);
		priority.add(ACTION);
		priority.add(REMOTING_SERVICE);
		priority.add(SERVICE);
		priority.add(LOGIC);
		priority.add(LISTENER);
		priority.add(DAO);
		priority.add(DXO);
		priority.add(HELPER);
	}
	
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
	public String createDefinition() {
		StringBuilder sb = new StringBuilder();
		sb.append("  <component name=\"").append(getName())
			.append("\" class=\"").append(getClazz()).append("\">" + NL);

		for(CustomizerModel customizer : getCustomizers()) {
			sb.append("    <initMethod name=\"addCustomizer\">" + NL);
			sb.append("      <arg>").append(customizer.getArg()).append("</arg>" + NL);
			sb.append("    </initMethod>" + NL);
		}
		sb.append("  </component>" + NL);

		return sb.toString();
	}
	
	@Override
	public int compareTo(ComponentsChild o) {
		if(o instanceof ComponentModel) {
			return priority.indexOf(this.getName()) - priority.indexOf(((ComponentModel) o).getName());
		}
		return super.compareTo(o);
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
}
