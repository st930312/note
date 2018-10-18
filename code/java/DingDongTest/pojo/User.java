package DingDongTest.pojo;

import java.util.Map;

public class User {

	private String user_id;
	private Map<String, String> attributes;

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public String getUser_id() {
		return user_id;
	}
}
