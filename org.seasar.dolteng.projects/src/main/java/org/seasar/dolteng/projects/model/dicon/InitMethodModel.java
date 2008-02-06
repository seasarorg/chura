package org.seasar.dolteng.projects.model.dicon;

import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;

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
    public String buildElement(int indent, IProgressMonitor monitor) {
        StringBuilder sb = new StringBuilder();
        appendIndent(sb, indent);
        sb.append("<initMethod name=\"").append(name).append("\">");

        for (DiconElement child : children) {
            sb.append(child.buildElement(indent + 1, monitor));
            ProgressMonitorUtil.isCanceled(monitor, 1);
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
