package org.seasar.dolteng.projects.handler.impl.dicon;

import java.util.ArrayList;
import java.util.List;

/**
 * diconファイルのcomponentsタグの子要素モデル
 * 
 * @author daisuke
 */
public abstract class ComponentsChild implements Comparable<ComponentsChild> {

    private static List<Class<? extends ComponentsChild>> priority = new ArrayList<Class<? extends ComponentsChild>>();

    static {
        priority.add(IncludeModel.class);
        priority.add(ComponentModel.class);
    }

    public int compareTo(ComponentsChild o) {
        return priority.indexOf(this.getClass())
                - priority.indexOf(o.getClass());
    }

    public abstract String createDefinition();

    @Override
    public String toString() {
        return createDefinition();
    }
}
