package org.seasar.dolteng.projects.handler.impl.dicon;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * diconファイルのモデル
 * 
 * @author daisuke
 */
public class DiconModel extends DiconElement {

    private static final String DICON_OPEN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + NL
            + "<!DOCTYPE components PUBLIC \"-//SEASAR//DTD S2Container 2.4//EN\" "
            + NL
            + "\t\"http://www.seasar.org/dtd/components24.dtd\">"
            + NL
            + "<components>";

    private static final String DICON_CLOSE = NL + "</components>" + NL;

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

    public void addAspectCustomizerTo(String componentName, String arg) {
        ComponentModel component = getComponent(componentName);
        if (component == null) {
            throw new IllegalStateException();
        }
        component.addAspectCustomizer(componentName, arg);
    }

    public void addCustomizerTo(String componentName, String customizerName,
            String aspect) {
        ComponentModel component = getComponent(componentName);
        if (component == null) {
            throw new IllegalStateException();
        }
        component.addCustomizer(customizerName, aspect);
    }

    public void removeCustomizerFrom(String componentName,
            String customizerName, String aspect) {
        ComponentModel component = getComponent(componentName);
        if (component != null) {
            component.removeCustomizer(customizerName, aspect);
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
    public String buildElement(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(DICON_OPEN);

        for (ComponentsChild component : children) {
            sb.append(component.buildElement(indent + 1));
        }

        sb.append(DICON_CLOSE);
        return sb.toString();
    }

    @Override
    public int compareTo(DiconElement o) {
        if (o instanceof DiconModel) {
            throw new RuntimeException();
        }
        return super.compareTo(o);
    }

    @Override
    public String toString() {
        return children.toString();
    }
}
