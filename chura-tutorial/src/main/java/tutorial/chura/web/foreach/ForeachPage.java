package tutorial.chura.web.foreach;

public class ForeachPage {

	public int aaaIndex;
	public AaaDto[] aaaItems;
	public String id;
	public String name;

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

	/**
	 * @return
	 */
	public String getAaaRowStyle() {
		if (aaaIndex % 2 == 0) {
			return "background-color:yellow";
		}
		return null;
	}

}
