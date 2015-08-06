package operation.pojo.log;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * 
 * @ClassName: UserRegistLog
 * @Description: 用户注册访问日志
 * @author tangli
 * @date 2015年3月17日 下午2:19:33
 *
 */
public class UserVisitLog {
	@Id
	private String id;
	private String email;// email
	private String phoneNumber;// 手机号
	private String type;// 注册类别
	private String ip;
	private String refType; // 0 渠道商 1 用户邀请
	@Indexed
	private String adId;//广告位Id
	@Indexed
	private String name;// 广告位名称
	@Indexed
	private String adSid;// 渠道商Id
	@Indexed
	private String adSellerId;// 渠道商Id号
	@Indexed
	private String vUserId;// 邀请人ID
	private String vUserEmail;// 邀请人账号
	private String vUserPhone;// 邀请人手机
	@Indexed
	private String vUserNick;// 邀请人昵称
	private long ctime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getAdId() {
		return adId;
	}

	public void setAdId(String adId) {
		this.adId = adId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAdSid() {
		return adSid;
	}

	public void setAdSid(String adSid) {
		this.adSid = adSid;
	}

	public String getAdSellerId() {
		return adSellerId;
	}

	public void setAdSellerId(String adSellerId) {
		this.adSellerId = adSellerId;
	}

	public long getCtime() {
		return ctime;
	}

	public void setCtime(long ctime) {
		this.ctime = ctime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRefType() {
		return refType;
	}

	public void setRefType(String refType) {
		this.refType = refType;
	}

	public String getvUserId() {
		return vUserId;
	}

	public void setvUserId(String vUserId) {
		this.vUserId = vUserId;
	}

	public String getvUserEmail() {
		return vUserEmail;
	}

	public void setvUserEmail(String vUserEmail) {
		this.vUserEmail = vUserEmail;
	}

	public String getvUserPhone() {
		return vUserPhone;
	}

	public void setvUserPhone(String vUserPhone) {
		this.vUserPhone = vUserPhone;
	}

	public String getvUserNick() {
		return vUserNick;
	}

	public void setvUserNick(String vUserNick) {
		this.vUserNick = vUserNick;
	}

	

}
