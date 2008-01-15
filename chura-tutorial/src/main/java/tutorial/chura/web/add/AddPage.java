package tutorial.chura.web.add;

import org.seasar.teeda.extension.annotation.validator.Required;

public class AddPage {

	@Required
	public Integer arg1;
	@Required
	public Integer arg2;
	public Integer result;

	public Class doCalculate() {
		result = arg1 + arg2;
		return null;
	}

	public Class initialize() {
		return null;
	}

	public Class prerender() {
		return null;
	}

}
