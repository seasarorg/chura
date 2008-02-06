package org.seasar.dolteng.projects.handler.impl.dicon;

/**
 * diconファイルで使用されるpropertyタグのモデル
 * 
 * @author daisuke
 */
public class PropertyModel extends DiconElement {

    private String name;

    /**
     * コンストラクタ。
     * 
     * @param name
     * @category instance creation
     */
    public PropertyModel(String name) {
        this.name = name;
    }

    @Override
    public String buildElement(int indent) {
        StringBuilder sb = new StringBuilder();
        appendIndent(sb, indent);
        sb.append("<property name=\"").append(name).append("\"");
        if (children.size() == 0) {
            sb.append("/>").append(NL);
        } else {
            sb.append(">");
            for (DiconElement child : children) {
                if (child instanceof Literal) {
                    sb.append(child.buildElement(-1));
                } else {
                    sb.append(child.buildElement(indent + 1));
                }
            }
            // appendIndent(sb, indent);
            sb.append("</property>");
        }
        return sb.toString();
    }

    @Override
    public int compareTo(DiconElement o) {
        if (o instanceof PropertyModel) {
            return name.compareTo(((PropertyModel) o).name);
        }
        return super.compareTo(o);
    }

}
