package org.seasar.dolteng.projects.handler.impl.dicon;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * diconファイルのモデル
 * 
 * @author daisuke
 */
public class DiconModel {

    // protected static Map<String, DiconModel> singletons = new HashMap<String,
    // DiconModel>();

    protected SortedSet<ComponentsChild> children = new TreeSet<ComponentsChild>();

    @SuppressWarnings("unused")
    private String diconName;

    public DiconModel(String diconName) {
        this.diconName = diconName;
    }

    public SortedSet<ComponentsChild> getChildren() {
        return children;
    }

    public void addChild(ComponentsChild child) {
        if (child == null) {
            throw new IllegalArgumentException();
        }
        children.add(child);
    }

    public void addCustomizerTo(String componentName, String customizerName,
            String aspect) {
        ComponentModel component = getComponent(componentName);
        if (component == null) {
            throw new IllegalStateException();
        }
        component.addCustomizer(customizerName, aspect);
    }

    public void removeCustomizerFrom(String componentName, String customizerName) {
        ComponentModel component = getComponent(componentName);
        if (component != null) {
            component.removeCustomizer(customizerName);
        }
    }

    public ComponentModel getComponent(String componentName) {
        for (ComponentsChild component : children) {
            if (component instanceof ComponentModel
                    && componentName.equals(((ComponentModel) component)
                            .getName())) {
                return (ComponentModel) component;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return children.toString();
    }
}
