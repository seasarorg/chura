package org.seasar.dolteng.projects.model;

/**
 * ファセットのカテゴリ
 * 
 * @author daisuke
 */
public class FacetCategory {

    /** カテゴリID */
    private String id;

    /** カテゴリKey */
    private String key;

    /** カテゴリ名 */
    private String name;

    /**
     * コンストラクタ。
     * 
     * @param id
     * @param key
     * @param name
     * @category instance creation
     */
    public FacetCategory(String id, String key, String name) {
        super();
        this.id = id;
        this.key = key;
        this.name = name;
    }

    public FacetCategory(String key) {
        this(null, key, null);
    }

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
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
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return key;
    }
}
