package org.seasar.dolteng.projects.handler.impl.dicon;

/**
 * diconファイルで使用されるリテラルのモデル
 * 
 * @author daisuke
 */
public class Literal extends DiconElement {

    private String literal;

    static {
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

    /**
     * コンストラクタ。
     * 
     * @param literal
     * @category instance creation
     */
    public Literal(String literal) {
        this.literal = literal;
    }

    @Override
    public String buildElement(int indent) {
        if (indent != -1) {
            StringBuilder sb = new StringBuilder();
            appendIndent(sb, indent);
            sb.append(literal);
            return sb.toString();
        }
        return literal;
    }

    @Override
    public int compareTo(DiconElement o) {
        if (o instanceof Literal) {
            int myPriority = priority.indexOf(this.literal);
            int otherPriority = priority.indexOf(((Literal) o).literal);

            if (myPriority == -1 && otherPriority == -1) {
                return literal.compareTo(((Literal) o).literal);
            }
            if (otherPriority == -1) {
                return 1;
            }
            if (myPriority == -1) {
                return -1;
            }
            return myPriority - otherPriority;
        }
        return super.compareTo(o);
    }

    String getLiteral() {
        return literal;
    }
}
