package org.seasar.dolteng.projects.model;

import java.util.ArrayList;
import java.util.List;

/**
 * アプリケーションタイプ
 * @author daisuke
 */
public class ApplicationType {

	private String name;
	
	/** このタイプで有効になるカテゴリのリスト */
	private List<FacetCategory> enable = new ArrayList<FacetCategory>();
	
	public ApplicationType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	/**
	 * 有効カテゴリを追加
	 * @param category 追加するカテゴリ
	 */
	public void enableCategory(FacetCategory category) {
		enable.add(category);
	}
	
	/**
	 * 指定したカテゴリが有効になっているか調べる
	 * @param category 対象カテゴリ
	 * @return 有効な場合<code>true</code>
	 */
	public boolean isEnabled(FacetCategory category) {
		return enable.contains(category);
	}
}
