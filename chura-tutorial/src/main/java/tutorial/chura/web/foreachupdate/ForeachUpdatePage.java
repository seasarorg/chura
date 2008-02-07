package tutorial.chura.web.foreachupdate;

import org.seasar.teeda.extension.annotation.validator.Required;

public class ForeachUpdatePage {

	public int aaaIndex;
	public AaaDto[] aaaItems;
	@Required
	public String id;
	@Required
	public String name;

	public Class doSubmit() {
		return null;
	}

	public Class initialize() {
		aaaItems = new AaaDto[3];
		for (int i = 0; i < aaaItems.length; i++) {
			AaaDto dto = new AaaDto();
			dto.id = String.valueOf(i);
			dto.name = "name" + i;
			aaaItems[i] = dto;
		}
		return null;
	}

	public Class prerender() {
		return null;
	}

}
