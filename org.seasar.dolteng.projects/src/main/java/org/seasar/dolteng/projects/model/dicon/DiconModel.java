package org.seasar.dolteng.projects.model.dicon;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;

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

    protected SortedSet<DiconElement> children = new TreeSet<DiconElement>();

    @SuppressWarnings("unused")
    private String diconName;

    public DiconModel(String diconName) {
        this.diconName = diconName;
    }

    public SortedSet<DiconElement> getChildren() {
        return children;
    }

    public void addChild(DiconElement child) {
        if (child == null) {
            throw new IllegalArgumentException();
        }
        children.add(child);
    }

    public void addProperty(String componentName, String name, String value) {
        ComponentModel component = getComponent(componentName);
        if (component == null) {
            throw new IllegalStateException();
        }
        component.addProperty(componentName, name, value);
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
        for (DiconElement component : children) {
            if (component instanceof ComponentModel
                    && componentName.equals(((ComponentModel) component)
                            .getName())) {
                return (ComponentModel) component;
            }
        }
        return null;
    }

    @Override
    public String buildElement(int indent, IProgressMonitor monitor) {
        StringBuilder sb = new StringBuilder();
        sb.append(DICON_OPEN);

        for (DiconElement component : children) {
            sb.append(component.buildElement(indent + 1, monitor));
            ProgressMonitorUtil.isCanceled(monitor, 1);
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
