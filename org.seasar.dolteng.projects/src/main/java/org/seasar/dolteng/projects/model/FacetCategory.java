package org.seasar.dolteng.projects.model;

/**
 * ファセットのカテゴリ
 * @author daisuke
 */
public class FacetCategory {

	/** カテゴリID */
	private String id;
	
	/** カテゴリ名 */
	private String name;

	/**
	 * コンストラクタ。
	 * @param id
	 * @param name
	 * @category instance creation
	 */
	public FacetCategory(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public FacetCategory(String id) {
		this(id, null);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		final FacetCategory other = (FacetCategory) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
}
