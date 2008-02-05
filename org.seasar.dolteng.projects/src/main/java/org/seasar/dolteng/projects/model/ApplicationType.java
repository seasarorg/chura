package org.seasar.dolteng.projects.model;

import java.util.ArrayList;
import java.util.List;

/**
 * アプリケーションタイプ
 * @author daisuke
 */
public class ApplicationType {

	private String id;
	
	private String name;
	
	private List<String> baseFacets = new ArrayList<String>();
	
	/** このタイプで無効になるカテゴリのリスト */
	private List<String> disableCategories = new ArrayList<String>();
	
	/** このタイプで無効になるファセットのリスト */
	private List<String> disableFacets = new ArrayList<String>();
	
	public ApplicationType(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getBaseFacets() {
		return baseFacets;
	}
	
	/**
	 * 指定したカテゴリが有効になっているか調べる
	 * @param category 対象カテゴリ
	 * @return 無効な場合<code>true</code>
	 */
	public boolean isDisabled(FacetCategory category) {
		return disableCategories.contains(category.getId());
	}
	
	public boolean isDisabled(FacetConfig fc) {
		return disableFacets.contains(fc.getId());
	}

	public void addBase(String baseFacet) {
		baseFacets.add(baseFacet);
	}
	
	public void disableCategory(String category) {
		disableCategories.add(category);
	}

	public void disableFacet(String facet) {
		disableFacets.add(facet);
	}

	@Override
	public String toString() {
		return name;
	}
}
