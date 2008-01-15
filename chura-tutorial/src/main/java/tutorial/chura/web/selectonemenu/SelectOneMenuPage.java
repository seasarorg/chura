package tutorial.chura.web.selectonemenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectOneMenuPage {

	public Integer aaa;
	public List aaaItems;

	public Class doAction() {
		return null;
	}

	public Class initialize() {
		aaaItems = new ArrayList();
		for (int i = 0; i < 3; i++) {
			Map item = new HashMap();
			item.put("label", "label" + i);
			item.put("value", i);
			aaaItems.add(item);
		}
		return null;
	}

	public Class prerender() {
		return null;
	}

}
