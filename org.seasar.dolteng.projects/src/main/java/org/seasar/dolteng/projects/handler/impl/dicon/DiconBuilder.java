package org.seasar.dolteng.projects.handler.impl.dicon;

/**
 * TODO describe
 * 
 * @author daisuke
 */
public class DiconBuilder {

    protected DiconModel model;

    public static final String NL = System.getProperties().getProperty(
            "line.separator");

    private static final String DICON_OPEN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + NL
            + "<!DOCTYPE components PUBLIC \"-//SEASAR//DTD S2Container 2.4//EN\" "
            + NL
            + "\t\"http://www.seasar.org/dtd/components24.dtd\">"
            + NL
            + "<components>" + NL;

    private static final String DICON_CLOSE = "</components>" + NL;

    /**
     * コンストラクタ。
     * 
     * @param model
     * @category instance creation
     */
    public DiconBuilder(DiconModel model) {
        this.model = model;
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append(DICON_OPEN);

        for (ComponentsChild component : model.getChildren()) {
            sb.append(component.createDefinition());
        }

        sb.append(DICON_CLOSE);
        return sb.toString();
    }

}