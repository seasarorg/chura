package examples.chura.web.add;

import org.seasar.teeda.extension.annotation.validator.Required;

public class AddInputPage {

	@Required
	private Integer arg1;

	@Required
	private Integer arg2;

	public Integer getArg1() {
		return arg1;
	}

	public void setArg1(Integer arg1) {
		this.arg1 = arg1;
	}

	public Integer getArg2() {
		return arg2;
	}

	public void setArg2(Integer arg2) {
		this.arg2 = arg2;
	}
}