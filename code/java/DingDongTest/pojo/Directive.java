package DingDongTest.pojo;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Directive {

	@SerializedName("directive_items")
	@Expose
	private List<DirectiveItem> directiveItems = null;

	public List<DirectiveItem> getDirectiveItems() {
		return directiveItems;
	}

	public void setDirectiveItems(List<DirectiveItem> directiveItems) {
		this.directiveItems = directiveItems;
	}

}