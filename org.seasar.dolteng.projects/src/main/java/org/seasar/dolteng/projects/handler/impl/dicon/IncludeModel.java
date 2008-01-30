package org.seasar.dolteng.projects.handler.impl.dicon;

import static org.seasar.dolteng.projects.handler.impl.dicon.DiconBuilder.NL;

import java.util.ArrayList;
import java.util.List;

/**
 * diconファイルで定義されるincludeのモデル
 * @author daisuke
 */
public class IncludeModel extends ComponentsChild {

	private String path;
	
    /** app.diconの中でincludeする優先順位 */
	protected static List<String> priority = new ArrayList<String>();

    static {
        priority.add("convention.dicon");
        priority.add("aop.dicon");
        priority.add("app_aop.dicon");
        priority.add("teedaExtension.dicon");
        priority.add("dao.dicon");
        priority.add("kuina-dao.dicon");
        priority.add("dxo.dicon");
        priority.add("javaee5.dicon");
        priority.add("jms.dicon");
        priority.add("remoting_amf3.dicon");
    }

	/**
	 * コンストラクタ。
	 * @param path
	 * @category instance creation
	 */
	public IncludeModel(String path) {
		this.path = path;
	}

	/**
	 * Override method.
	 * @see org.seasar.dolteng.projects.handler.impl.dicon.ComponentsChild#createDefinition()
	 */
	@Override
	public String createDefinition() {
		StringBuilder sb = new StringBuilder();
		sb.append("  <include path=\"")
			.append(path)
			.append("\"/>")
			.append(NL);
		return sb.toString();
	}

	@Override
	public int compareTo(ComponentsChild o) {
		if(o instanceof IncludeModel) {
			return priority.indexOf(this.path) - priority.indexOf(((IncludeModel) o).path);
		}
		return super.compareTo(o);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		final IncludeModel other = (IncludeModel) obj;
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		return true;
	}
}
