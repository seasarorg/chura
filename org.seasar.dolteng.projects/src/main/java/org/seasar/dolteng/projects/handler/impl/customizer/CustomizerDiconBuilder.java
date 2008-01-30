package org.seasar.dolteng.projects.handler.impl.customizer;

/**
 * customizer.diconのビルダ
 * @author daisuke
 */
public class CustomizerDiconBuilder {

	private CustomizerDiconModel model;
	
	public static final String NL = System.getProperties().getProperty("line.separator");

	private static final String DICON_OPEN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
		+ "<!DOCTYPE components PUBLIC \"-//SEASAR//DTD S2Container 2.4//EN\" " + NL
		+ "\t\"http://www.seasar.org/dtd/components24.dtd\">" + NL
		+ "<components>" + NL
		+ "  <include path=\"default-customizer.dicon\"/>" + NL + NL;
	
	private static final String DICON_CLOSE = //"  <!--" + NL
//		+ "    その他のカスタマイズは以下を参考にしてください。" + NL
//		+ "    コンポーネント名はカスタマイズ対象に合わせて変更してください。" + NL
//		+ "    app.dicon で aop.dicon 等必要な dicon をインクルードしてください。" + NL
//		+ "  -->" + NL
//		+ "  <!--" + NL
//		+ "  <component name=\"xxxCustomizer\" class=\"org.seasar.framework.container.customizer.CustomizerChain\">" + NL
//		+ "    <initMethod name=\"addCustomizer\">" + NL
//		+ "      <arg>traceCustomizer</arg>" + NL
//		+ "    </initMethod>" + NL
//		+ "  </component>" + NL
//		+ "  -->" + NL
		/*+*/ "</components>" + NL;
	
	public CustomizerDiconBuilder(CustomizerDiconModel model) {
		this.model = model;
	}
	
	public String build() {
		System.out.println(model.getComponents());
		StringBuilder sb = new StringBuilder();
		sb.append(DICON_OPEN);
		
		for(ComponentModel component : model.getComponents()) {
			sb.append("  <component name=\"").append(component.getName())
					.append("\" class=\"").append(component.getClazz()).append("\">" + NL);
			
			for(CustomizerModel customizer : component.getCustomizers()) {
				sb.append("    <initMethod name=\"addCustomizer\">" + NL);
				sb.append("      <arg>").append(customizer.getArg()).append("</arg>" + NL);
				sb.append("    </initMethod>" + NL);
			}
			sb.append("  </component>" + NL);
		}
		
		sb.append(DICON_CLOSE);
		return sb.toString();
	}
}
