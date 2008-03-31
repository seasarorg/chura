package org.seasar.dolteng.projects.model.dicon;

import static org.seasar.dolteng.projects.Constants.ATTR_COMPONENT_CLASS;
import static org.seasar.dolteng.projects.Constants.ATTR_COMPONENT_NAME;
import static org.seasar.dolteng.projects.Constants.ATTR_INCLUDE_PATH;
import static org.seasar.dolteng.projects.Constants.ATTR_INIT_NAME;
import static org.seasar.dolteng.projects.Constants.ATTR_PROPERTY_NAME;
import static org.seasar.dolteng.projects.Constants.TAG_ARG;
import static org.seasar.dolteng.projects.Constants.TAG_COMPONENT;
import static org.seasar.dolteng.projects.Constants.TAG_INCLUDE;
import static org.seasar.dolteng.projects.Constants.TAG_INIT_METHOD;
import static org.seasar.dolteng.projects.Constants.TAG_INIT_METHOD_X;
import static org.seasar.dolteng.projects.Constants.TAG_PROPERTY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.framework.util.ArrayMap;

/**
 * diconファイルで使用される全ての要素のモデル
 * 
 * @author daisuke
 */
@SuppressWarnings("serial")
public class DiconElement implements Serializable, Comparable<DiconElement> {

    public final String NL = System.getProperties().getProperty(
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

    private String tag;

    private ArrayMap/*<String, String>*/ attributeMap;

    private String value;

    private ArrayList<String> priority = new ArrayList<String>();

    protected Set<DiconElement> children = new TreeSet<DiconElement>();

    private boolean counteract = false;

    public DiconElement(String tag, ArrayMap/*<String, String>*/ attributeMap,
            String value) {
        if (tag == null) {
            throw new IllegalArgumentException("tag is null.");
        }
        if (attributeMap == null) {
            attributeMap = new ArrayMap();
        }
        this.tag = tag;
        this.attributeMap = attributeMap;
        this.value = value;

        if (TAG_COMPONENT.equals(tag)
                && attributeMap.get(ATTR_COMPONENT_CLASS) == null) {
            this.attributeMap
                    .put(ATTR_COMPONENT_CLASS,
                            "org.seasar.framework.container.customizer.CustomizerChain");
        }

        priority.add("");
        priority.add("components");
        priority.add("include");
        priority.add("component");
        priority.add("initMethod");
        priority.add("property");
        priority.add("arg");

        // component
        priority.add(PAGE);
        priority.add(ACTION);
        priority.add(REMOTING_SERVICE);
        priority.add(SERVICE);
        priority.add(LOGIC);
        priority.add(LISTENER);
        priority.add(DAO);
        priority.add(DXO);
        priority.add(HELPER);

        // include
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

        // value
        priority.add(TRACE);
        priority.add(COMMAND_TRACE);
        priority.add(REQUIRED_TX);
        priority.add(S2DAO);
        priority.add(KUINA_DAO);
        priority.add(S2DXO);
        priority.add("\"aop.traceInterceptor\"");
        priority.add("\"app_aop.appFacesExceptionThrowsInterceptor\"");
        priority.add("\"app_aop.actionSupportInterceptor\"");
        priority.add("\"j2ee.requiredTx\"");
        priority.add("\"actionMessagesThrowsInterceptor\"");
}

    public DiconElement(String tag, ArrayMap/*<String, String>*/ attributeMap) {
        this(tag, attributeMap, null);
    }

    @SuppressWarnings("unchecked")
    public DiconElement(String tag) {
        this(tag, null, null);
    }

    public String buildElement(int indent, IProgressMonitor monitor) {
        StringBuilder sb = new StringBuilder();
        if (indent != -1) {
            appendIndent(sb, indent);
        }
        if ("".equals(tag)) {
            sb.append(value);
            ProgressMonitorUtil.isCanceled(monitor, 1);
        } else {
            sb.append("<").append(tag);
            for (Object/*Map.Entry<String, String>*/ o : attributeMap.entrySet()) {
                Map.Entry<String, String> e = (Entry<String, String>) o;
                sb.append(" ").append(e.getKey()).append("=\"").append(
                        e.getValue()).append("\"");
            }

            if (children.size() == 0) {
                sb.append("/>");
            } else {
                sb.append(">");
                int nextIndent = -1;
                for (DiconElement child : children) {
                    nextIndent = (indent == -1 || "".equals(child.getTag())) ? -1
                            : indent + 1;
                    sb.append(child.buildElement(nextIndent, monitor));
                    ProgressMonitorUtil.isCanceled(monitor, 1);
                }
                if (nextIndent != -1) {
                    appendIndent(sb, indent);
                }
                sb.append("</").append(tag).append(">");
            }
        }

        return sb.toString();
    }

    public void appendChild(DiconElement child) {
        if (child == null) {
            return;
        }
        if ("".equals(child.tag) && child.value == null) {
            return;
        }
        if (child.isCounteract()) {
            children.remove(child);
        } else {
            children.add(child);
        }
    }

    protected void appendIndent(StringBuilder sb, int indent) {
        sb.append(NL);
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((attributeMap == null) ? 0 : attributeMap.hashCode());
        result = prime * result
                + ((children == null) ? 0 : children.hashCode());
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        final DiconElement other = (DiconElement) obj;
        if (attributeMap == null) {
            if (other.attributeMap != null) {
                return false;
            }
        } else if (!attributeMap.equals(other.attributeMap)) {
            return false;
        }
        if (children == null) {
            if (other.children != null) {
                return false;
            }
        } else if (!children.equals(other.children)) {
            return false;
        }
        if (tag == null) {
            if (other.tag != null) {
                return false;
            }
        } else if (!tag.equals(other.tag)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    public int compareTo(DiconElement o) {
        if (this.equals(o) || o == null) {
            return 0;
        }

        if (!this.tag.equals(o.tag)) {
            int myPriority = priority.indexOf(this.tag);
            int otherPriority = priority.indexOf(o.tag);
            if (myPriority == -1) {
                return 1;
            }
            if (otherPriority == -1) {
                return -1;
            }
            return myPriority - otherPriority;
        }

        if (tag.equals("")) {
            int myPriority = priority.indexOf(this.value);
            int otherPriority = priority.indexOf(o.value);

            if (myPriority == -1 && otherPriority == -1) {
                return this.value.compareTo(o.value);
            }
            if (otherPriority == -1) {
                return 1;
            }
            if (myPriority == -1) {
                return -1;
            }
            return myPriority - otherPriority;
        } else if (tag.equals(TAG_ARG)) {
            return compareChildren(o);
        } else if (tag.equals(TAG_INCLUDE)) {
            return compareTo(o, ATTR_INCLUDE_PATH);
        } else if (tag.equals(TAG_COMPONENT)) {
            int res = compareTo(o, ATTR_COMPONENT_NAME);
            if (res != 0) {
                return res;
            }

            res = compareTo(o, ATTR_COMPONENT_CLASS);
            if (res != 0) {
                return res;
            }
            return compareChildren(o);
        } else if (tag.equals(TAG_INIT_METHOD)) {
            int res = compareTo(o, ATTR_INIT_NAME);
            if (res != 0) {
                return res;
            }

            return compareChildren(o);
        } else if (tag.equals(TAG_INIT_METHOD_X)) {
            return 0;
        } else if (tag.equals(TAG_PROPERTY)) {
            int res = compareTo(o, ATTR_PROPERTY_NAME);
            if (res != 0) {
                return res;
            }

            return compareChildren(o);
        } else {
            return this.tag.compareTo(o.tag);
        }
    }

    private int compareChildren(DiconElement o) {
        for (DiconElement child : children) {
            int res = child.compareTo(o.children.iterator().next());
            if (res != 0) {
                return res;
            }
        }
        return 0;
    }

    private int compareTo(DiconElement o, String targetAttr) {
        String myTarget = (String) this.attributeMap.get(targetAttr);
        String otherTarget = (String) o.attributeMap.get(targetAttr);
        int myPriority = priority.indexOf(myTarget);
        int providedPriority = priority.indexOf(otherTarget);
        if (myPriority == -1 && providedPriority == -1) {
            if (myTarget != null) {
                return myTarget.compareTo(otherTarget);
            }
        }
        if (providedPriority == -1) {
            return 1;
        }
        if (myPriority == -1) {
            return -1;
        }
        return myPriority - providedPriority;
    }

    public int size() {
        int result = children.size();
        for (DiconElement dicon : children) {
            result += dicon.size();
        }
        return result;
    }

    public Collection<DiconElement> getChildren() {
        return children;
    }

    public String getTag() {
        return tag;
    }

    public ArrayMap/*<String, String>*/ getAttributeMap() {
        return attributeMap;
    }

    public boolean isCounteract() {
        return counteract;
    }

    public void setCounteract(boolean counteract) {
        this.counteract = counteract;
    }

}
