package DingDongTest.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DirectiveItem {

	@SerializedName("content")
	@Expose
	private String content;
	@SerializedName("type")
	@Expose
	private String type;

	public DirectiveItem(String content, String type) {
		this.content = content;
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}