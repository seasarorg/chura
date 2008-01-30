package org.seasar.dolteng.projects.handler.impl.customizer;

import static org.seasar.dolteng.projects.handler.impl.customizer.CustomizerDiconBuilder.NL;

/**
 * TODO describe
 * @author daisuke
 */
public class CustomizerModel {

	private String arg;
	
	public static final String ASPECT_OPEN = NL
		+ "        <component class=\"org.seasar.framework.container.customizer.AspectCustomizer\">" + NL
		+ "          <initMethod name=\"addInterceptorName\">" + NL
		+ "            <arg>\"";
	
	public static final String ASPECT_CLOSE = "\"</arg>" + NL
		+ "          </initMethod>" + NL
		+ "          <property name=\"pointcut\">\"do.*, initialize, prerender\"</property>" + NL
		+ "        </component>" + NL
		+ "      ";
	
	/**
	 * コンストラクタ。
	 * @param name
	 * @param aspect
	 * @category instance creation
	 */
	public CustomizerModel(String name, String aspect) {
		if("AspectCustomizer".equals(name) && aspect != null) {
			this.arg = new StringBuilder()
				.append(ASPECT_OPEN).append(aspect).append(ASPECT_CLOSE).toString();
		} else {
			this.arg = name;
		}
	}

	public String getArg() {
		return arg;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arg == null) ? 0 : arg.hashCode());
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
		final CustomizerModel other = (CustomizerModel) obj;
		if (arg == null) {
			if (other.arg != null) {
				return false;
			}
		} else if (!arg.equals(other.arg)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return arg;
	}
}
