package org.seasar.dolteng.projects.handler.impl.dicon;

/**
 * diconファイルで使用されるinitMehtodタグのモデル
 * 
 * @author daisuke
 */
public class InitMethodModel extends DiconElement {

    private String name;

    public InitMethodModel(String name) {
        this.name = name;
    }

    @Override
    public String buildElement(int indent) {
        StringBuilder sb = new StringBuilder();
        appendIndent(sb, indent);
        sb.append("<initMethod name=\"").append(name).append("\">");

        for (DiconElement child : children) {
            sb.append(child.buildElement(indent + 1));
        }
        appendIndent(sb, indent);
        sb.append("</initMethod>");
        return sb.toString();
    }

    @Override
    public int compareTo(DiconElement o) {
        if (o instanceof InitMethodModel) {
            int nameResult = name.compareTo(((InitMethodModel) o).name);
            if (nameResult != 0) {
                return nameResult;
            }
            return children.iterator().next().compareTo(
                    o.children.iterator().next());
        }
        return super.compareTo(o);
    }

}
