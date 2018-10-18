package DingDongTest.pojo;

public class Card {
	/**
	 * 开发者需要平台推送到用户音箱关联的手机APP上展现的标题内容。 注：不能超过20个字
	 */
	private String title;
	/**
	 * APP展现内容类型： 1.纯文字 2.图片(暂未开放) 3.外部连接
	 */
	private String type;
	/**
	 * type为1时使用
	 */
	private String text;
	/**
	 * type为3时使用
	 * 
	 */
	private String url;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
