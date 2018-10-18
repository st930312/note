package DingDongTest.pojo;

import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DingDongReq {

	@SerializedName("versionid")
	@Expose
	private String versionid;
	@SerializedName("status")
	@Expose
	private Status status;
	@SerializedName("sequence")
	@Expose
	private String sequence;
	@SerializedName("timestamp")
	@Expose
	private Long timestamp;
	@SerializedName("application_info")
	@Expose
	private ApplicationInfo applicationInfo;
	@SerializedName("session")
	@Expose
	private Session session;
	@SerializedName("user")
	@Expose
	private User user;
	@SerializedName("input_text")
	@Expose
	private String inputText;
	@SerializedName("slots")
	@Expose
	private Map<String, String> slots;
	@SerializedName("extend")
	@Expose
	private Map<String, String> extend;

	public String getVersionid() {
		return versionid;
	}

	public void setVersionid(String versionid) {
		this.versionid = versionid;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public ApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}

	public void setApplicationInfo(ApplicationInfo applicationInfo) {
		this.applicationInfo = applicationInfo;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getInputText() {
		return inputText;
	}

	public void setInputText(String inputText) {
		this.inputText = inputText;
	}

	public Map<String, String> getSlots() {
		return slots;
	}

	public void setSlots(Map<String, String> slots) {
		this.slots = slots;
	}

	public Map<String, String> getExtend() {
		return extend;
	}

	public void setExtend(Map<String, String> extend) {
		this.extend = extend;
	}

	public enum Status {
		/**
		 * 用户只输入了应用名称无具体意图时的状态,可以理解为当前应用的会话启动
		 */
		@SerializedName("LAUNCH")
		LAUNCH,

		/**
		 * 用户输入的内容具有某个应用的具体意图时的状态，会话开始时可以理解为带有意图的启动，在会话中是可以理解为正在交互的内容。
		 */
		@SerializedName("INTENT")
		INTENT,

		/**
		 * 在与用户交互的过程中，用户的输入不能识别出具体的意图时，音箱会开启重复询问，在询问的过程中也会异步地将用户的输入发送给开发者应用。注：这种状态下的报文，不是用来交互的，而且是异步延迟发送的，目的只是把数据日志给到应用，供应用端进行用户话术的分析，改进VUI的交互设计。
		 */
		@SerializedName("NOTICE")
		NOTICE,

		/**
		 * 用户退出应用，或会话总次数超出平台次数限制。
		 * 
		 */
		@SerializedName("END")
		END
	}
}
