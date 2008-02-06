package org.seasar.dolteng.projects.handler.impl.dicon;

/**
 * diconファイルで使用されるincludeタグのモデル
 * 
 * @author daisuke
 */
public class IncludeModel extends ComponentsChild {

    private String path;

    static {
        priority.add("convention.dicon");
        priority.add("aop.dicon");
        priority.add("app_aop.dicon");
        priority.add("teedaExtension.dicon");
        priority.add("dao.dicon");
        priority.add("kuina-dao.dicon");
        priority.add("dxo.dicon");
        priority.add("javaee5.dicon");
        priority.add("j2ee.dicon");
        priority.add("s2jdbc.dicon");
        priority.add("jms.dicon");
        priority.add("remoting_amf3.dicon");
    }

    /**
     * コンストラクタ。
     * 
     * @param path
     * @category instance creation
     */
    public IncludeModel(String path) {
        this.path = path;
    }

    @Override
    public String buildElement(int indent) {
        StringBuilder sb = new StringBuilder();
        appendIndent(sb, indent);
        sb.append("<include path=\"").append(path).append("\"/>");
        return sb.toString();
    }

    @Override
    public int compareTo(DiconElement o) {
        if (o instanceof IncludeModel) {
            int myPriority = priority.indexOf(this.path);
            int providedPriority = priority.indexOf(((IncludeModel) o).path);
            if (myPriority == -1 && providedPriority == -1) {
                return this.path.compareTo(((IncludeModel) o).path);
            }
            if (providedPriority == -1) {
                return 1;
            }
            if (myPriority == -1) {
                return -1;
            }
            return myPriority - providedPriority;
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
