package DingDongTest.pojo;

public class RichContent {

	/**
	 * string 类型： 1.文字 2.图片
	 **/
	private String type;
	/**
	 * 类型为1时：文字内容 类型为2时：图片连接，连接长度不可超过512个字符
	 **/
	private String content;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
