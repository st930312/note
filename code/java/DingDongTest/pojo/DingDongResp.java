package DingDongTest.pojo;

import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DingDongResp {

	/**
	 * 开发者需要音箱设备播报的内容，其中可以包含文本播报和流媒体播报。
	 */
	@SerializedName("directive")
	@Expose
	private Directive directive;
	/**
	 * 扩展字段，目前支持的扩展内容是，上一次会话拒识时，要不要让音箱响应用户的下一次拒识的输入，
	 * 如果NO_REC（需大写）取值0时，播报响应。取值1时，不播报。默认是0
	 */
	@SerializedName("extend")
	@Expose
	private Map<String, String> extend;
	/**
	 * 由开发者服务决定本次会话是否结束，如果标识为结束(true)平台会清除本次会话在平台保持的会话数据，
	 * 如果标识为不结束（false）平台继续为用户保持当前会话数据。
	 */
	@SerializedName("is_end")
	@Expose
	private Boolean isEnd;
	/**
	 * 交互流水号，回传平台调用时传递的值
	 */
	@SerializedName("sequence")
	@Expose
	private String sequence;
	@SerializedName("timestamp")
	@Expose
	private Long timestamp;
	@SerializedName("versionid")
	@Expose
	private String versionid;
	/**
	 * 需要的槽值：如不为空，开放平台会主动为开发者收集此槽值服务，如用户输入的说法不符合槽值提取规则，
	 * 则视为未识别重复收集。如为空则表明不需要平台关注槽值的识别，全部透传到第三方应用进行判断
	 */
	@SerializedName("need_slot")
	@Expose
	private String needSlot;
	/**
	 * 开发者需要平台推送到音箱设备关联的手机APP展现的内容，其中连接内可包含文本图片
	 */
	@SerializedName("push_to_app")
	@Expose
	private String pushToApp;
	/**
	 * 当音箱未能识别用户说话时，给用户的重新提示语，以引导用户进行后继的对话， 其中可以包含文本播报和流媒体播报。
	 * 如果该字段为空，且音箱发生未识别用户说话时，则音箱会重复播放directive字段的内容
	 */
	@SerializedName("repeat_directive")
	@Expose
	private Directive repeatDirective;

	public String getNeedSlot() {
		return needSlot;
	}

	public void setNeedSlot(String needSlot) {
		this.needSlot = needSlot;
	}

	public String getPushToApp() {
		return pushToApp;
	}

	public void setPushToApp(String pushToApp) {
		this.pushToApp = pushToApp;
	}

	public Directive getRepeatDirective() {
		return repeatDirective;
	}

	public void setRepeatDirective(Directive repeatDirective) {
		this.repeatDirective = repeatDirective;
	}

	public Directive getDirective() {
		return directive;
	}

	public void setDirective(Directive directive) {
		this.directive = directive;
	}

	public Map<String, String> getExtend() {
		return extend;
	}

	/**
	 * 扩展字段，目前支持的扩展内容是，上一次会话拒识时，要不要让音箱响应用户的下一次拒识的输入，
	 * 如果NO_REC（需大写）取值0时，播报响应。取值1时，不播报。默认是0
	 * 
	 * @param extend
	 */
	public void setExtend(Map<String, String> extend) {
		this.extend = extend;
	}

	public Boolean getIsEnd() {
		return isEnd;
	}

	public void setIsEnd(Boolean isEnd) {
		this.isEnd = isEnd;
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

	public String getVersionid() {
		return versionid;
	}

	public void setVersionid(String versionid) {
		this.versionid = versionid;
	}

}
