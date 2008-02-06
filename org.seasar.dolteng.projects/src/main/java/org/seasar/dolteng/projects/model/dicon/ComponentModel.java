package org.seasar.dolteng.projects.model.dicon;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;

/**
 * diconファイルで使用されるcomponentタグのモデル
 * 
 * @author daisuke
 */
public class ComponentModel extends DiconElement {

    private static List<String> priority = new ArrayList<String>();

    private String name;

    private String clazz;

    static {
        priority.add(PAGE);
        priority.add(ACTION);
        priority.add(REMOTING_SERVICE);
        priority.add(SERVICE);
        priority.add(LOGIC);
        priority.add(LISTENER);
        priority.add(DAO);
        priority.add(DXO);
        priority.add(HELPER);
    }

    /**
     * コンストラクタ。
     * 
     * @param name
     * @param clazz
     * @category instance creation
     */
    public ComponentModel(String name, String clazz) {
        this.name = name;
        if (clazz == null) {
            this.clazz = "org.seasar.framework.container.customizer.CustomizerChain";
        } else {
            this.clazz = clazz;
        }
    }

    public String getName() {
        return name;
    }

    public String getClazz() {
        return clazz;
    }

    public void addAspectCustomizer(String componentName, String arg) {
        InitMethodModel child = new InitMethodModel("addAspectCustomizer");
        child.appendChild(new ArgModel(new Literal(arg)));
        appendChild(child);
    }

    public void addCustomizer(String name, String aspect) {
        InitMethodModel child = new InitMethodModel("addCustomizer");
        if ("AspectCustomizer".equals(name) && aspect != null) {
            DiconElement grandChild = new ArgModel();
            DiconElement greatGrandChild = new ComponentModel(null,
                    "org.seasar.framework.container.customizer.AspectCustomizer");
            DiconElement greatGreatGrandChild = new InitMethodModel(
                    "addInterceptorName");
            greatGreatGrandChild.appendChild(new ArgModel(new Literal("\""
                    + aspect + "\"")));
            greatGrandChild.appendChild(greatGreatGrandChild);
            greatGreatGrandChild = new PropertyModel("pointcut");
            greatGreatGrandChild.appendChild(new Literal(
                    "\"do.*, initialize, prerender\""));
            greatGrandChild.appendChild(greatGreatGrandChild);
            grandChild.appendChild(greatGrandChild);
            child.appendChild(grandChild);
        } else {
            child.appendChild(new ArgModel(new Literal(name)));
        }
        appendChild(child);
    }

    public void removeCustomizer(String customizerName, String aspect) {
        // TODO aspectの削除に対応していない。
        // TODO なんかダサくない？
        DiconElement removeTarget = null;
        for (DiconElement child : children) {
            if (child instanceof InitMethodModel) {
                InitMethodModel imm = (InitMethodModel) child;
                DiconElement grandchild = imm.children.iterator().next();
                if (grandchild instanceof ArgModel) {
                    ArgModel arg = (ArgModel) grandchild;
                    DiconElement grandGrandChild = arg.children.iterator()
                            .next();
                    if (grandGrandChild instanceof Literal) {
                        Literal literal = (Literal) grandGrandChild;
                        if (customizerName.equals(literal.getLiteral())) {
                            removeTarget = child;
                            break;
                        }
                    }
                }
            }
        }
        if (removeTarget != null) {
            children.remove(removeTarget);
        } else {
            DoltengCore.log("fail to remove customizer [" + customizerName
                    + ", " + aspect + "]");
        }
    }

    @Override
    public String buildElement(int indent, IProgressMonitor monitor) {
        StringBuilder sb = new StringBuilder();
        appendIndent(sb, indent);
        sb.append("<component");
        if (name != null) {
            sb.append(" name=\"").append(getName()).append("\"");
        }
        sb.append(" class=\"").append(getClazz()).append("\"");

        if (children.size() == 0) {
            sb.append("/>");
        } else {
            sb.append(">");
            for (DiconElement child : children) {
                sb.append(child.buildElement(indent + 1, monitor));
                ProgressMonitorUtil.isCanceled(monitor, 1);
            }
            appendIndent(sb, indent);
            sb.append("</component>");
        }

        return sb.toString();
    }

    @Override
    public int compareTo(DiconElement o) {
        if (o instanceof ComponentModel) {
            int myPriority = priority.indexOf(this.getName());
            int providedPriority = priority.indexOf(((ComponentModel) o)
                    .getName());
            if (myPriority == -1 && providedPriority == -1) {
                if (name != null) {
                    return this.name.compareTo(((ComponentModel) o).name);
                }
                int nameResult = this.clazz
                        .compareTo(((ComponentModel) o).clazz);
                if (nameResult != 0) {
                    return nameResult;
                }
                return children.iterator().next().compareTo(
                        o.children.iterator().next());
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
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        final ComponentModel other = (ComponentModel) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
