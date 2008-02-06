package org.seasar.dolteng.projects.handler.impl.dicon;

/**
 * diconファイルで使用されるcomponentタグのモデル
 * 
 * @author daisuke
 */
public class ArgModel extends DiconElement {

    /**
     * コンストラクタ。
     * 
     * @param child
     * @category instance creation
     */
    public ArgModel(DiconElement child) {
        appendChild(child);
    }

    /**
     * コンストラクタ。
     * 
     * @category instance creation
     */
    public ArgModel() {
    }

    @Override
    public String buildElement(int indent) {
        StringBuilder sb = new StringBuilder();
        appendIndent(sb, indent);
        sb.append("<arg>");
        int nextIndent = -1;
        for (DiconElement child : children) {
            if (child instanceof Literal) {
                nextIndent = -1;
            } else {
                nextIndent = indent + 1;
            }
            sb.append(child.buildElement(nextIndent));
        }
        if (nextIndent != -1) {
            appendIndent(sb, indent);
        }
        sb.append("</arg>");
        return sb.toString();
    }

    @Override
    public int compareTo(DiconElement o) {
        if (o instanceof ArgModel) {
            return children.iterator().next().compareTo(
                    o.children.iterator().next());
        }
        return super.compareTo(o);
    }

}
