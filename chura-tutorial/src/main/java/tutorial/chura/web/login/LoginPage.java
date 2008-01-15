package tutorial.chura.web.login;

import org.seasar.teeda.core.exception.AppFacesException;
import org.seasar.teeda.extension.annotation.validator.Required;

public class LoginPage {

	@Required
	public String userName;
	@Required
	public String password;

	public Class doLogin() {
		if (!userName.equals(password)) {
			throw new AppFacesException("errors.invalid.login");
		}
		return WelcomePage.class;
	}

	public Class initialize() {
		return null;
	}

	public Class prerender() {
		return null;
	}

}
