package org.seasar.dolteng.projects.handler.impl.dicon;


/**
 * TODO describe
 * @author daisuke
 */
public class AspectCustomizerModel {

	private String arg;
	
	/**
	 * コンストラクタ。
	 * @param arg
	 * @category instance creation
	 */
	public AspectCustomizerModel(String arg) {
		this.arg = arg;
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
		final AspectCustomizerModel other = (AspectCustomizerModel) obj;
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
