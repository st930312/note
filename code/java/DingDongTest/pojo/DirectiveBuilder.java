package DingDongTest.pojo;

import java.util.ArrayList;
import java.util.List;

public class DirectiveBuilder {

	private final List<DirectiveItem> directiveItems;

	public DirectiveBuilder() {
		directiveItems = new ArrayList<>();
	}

	public static DirectiveBuilder newBuilder() {
		return new DirectiveBuilder();
	}

	public DirectiveBuilder addItem(String content, String type) {
		directiveItems.add(new DirectiveItem(content, type));
		return this;
	}

	public Directive build() {
		Directive res = new Directive();
		res.setDirectiveItems(directiveItems);
		return res;
	}
}
