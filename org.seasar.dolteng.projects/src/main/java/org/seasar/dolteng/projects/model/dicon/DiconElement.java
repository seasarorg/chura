package org.seasar.dolteng.projects.model.dicon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * diconファイルで使用される全ての要素のモデル
 * 
 * @author daisuke
 */
public abstract class DiconElement implements Comparable<DiconElement> {

    public static final String NL = System.getProperties().getProperty(
            "line.separator");

    // 要定義コンポーネント
    public static final String PAGE = "pageCustomizer";

    public static final String ACTION = "actionCustomizer";

    public static final String REMOTING_SERVICE = "remotingServiceCustomizer";

    public static final String SERVICE = "serviceCustomizer";

    public static final String LOGIC = "logicCustomizer";

    public static final String LISTENER = "listenerCustomizer";

    public static final String DAO = "daoCustomizer";

    public static final String DXO = "dxoCustomizer";

    public static final String HELPER = "helperCustomizer";

    // 定義済みコンポーネント
    public static final String TRACE = "traceCustomizer";

    public static final String COMMAND_TRACE = "commandTraceCustomizer";

    public static final String REQUIRED_TX = "requiredTxCustomizer";

    public static final String S2DAO = "s2DaoCustomizer";

    public static final String KUINA_DAO = "kuinaDaoCustomizer";

    public static final String S2DXO = "s2DxoCustomizer";

    protected static List<Object> priority = new ArrayList<Object>();

    protected Collection<DiconElement> children = new TreeSet<DiconElement>();

    static {
        priority.add(Literal.class);
        priority.add(DiconModel.class);
        priority.add(IncludeModel.class);
        priority.add(ComponentModel.class);
        priority.add(InitMethodModel.class);
        priority.add(PropertyModel.class);
        priority.add(ArgModel.class);
    }

    public abstract String buildElement(int indent, IProgressMonitor monitor);

    protected void appendChild(DiconElement element) {
        children.add(element);
    }

    protected void appendIndent(StringBuilder sb, int indent) {
        sb.append(NL);
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
    }

    public int compareTo(DiconElement o) {
        int myPriority = priority.indexOf(this.getClass());
        int otherPriority = priority.indexOf(o.getClass());
        return myPriority - otherPriority;
    }

    public int size() {
        int result = children.size();
        for (DiconElement dicon : children) {
            result += dicon.size();
        }
        return result;
    }
}
