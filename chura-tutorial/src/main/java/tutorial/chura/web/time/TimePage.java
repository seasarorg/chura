package tutorial.chura.web.time;

import java.util.Date;

import org.seasar.teeda.extension.annotation.convert.DateTimeConverter;

public class TimePage {

	@DateTimeConverter(type = "time")
	public Date initializedTime;
	@DateTimeConverter(type = "time")
	public Date prerenderedTime;

	public Class doSubmit() {
		return null;
	}

	public Class initialize() {
		initializedTime = new Date();
		return null;
	}

	public Class prerender() {
		prerenderedTime = new Date();
		return null;
	}

}
