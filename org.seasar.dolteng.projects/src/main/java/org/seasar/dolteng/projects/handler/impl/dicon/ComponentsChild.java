package org.seasar.dolteng.projects.handler.impl.dicon;

/**
 * diconファイルのcomponentsタグの子要素モデル
 * 
 * @author daisuke
 */
public abstract class ComponentsChild extends DiconElement {

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    @Override
    public String toString() {
        return buildElement(0);
    }
}
