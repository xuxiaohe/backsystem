package operation.pojo.log;

import operation.pojo.ad.Ad;
import operation.pojo.user.User;

import org.springframework.data.mongodb.core.index.Indexed;

import tools.Config;

/**
 * 
 * @ClassName: UserRegistLog
 * @Description: 用户注册日志
 * @author tangli
 * @date 2015年3月17日 下午2:19:33
 *
 */
public class UserRegistLog {
	private String id;
	@Indexed
	private String userNick;// 用户名
	@Indexed
	private String userId;//用户Id
	private String email;// email
	private String phoneNumber;// 手机号
	private String type;// 注册类别
	
	private String ip;
	private String refType; // 0 渠道商  1 用户邀请  2渠道商浏览
	@Indexed
	private String adId;//广告位Id
	@Indexed
	private String name;// 广告位名称
	@Indexed
	private String adSid;// 渠道商Id
	@Indexed
	private String adSellerId;// 渠道商Id号
	private String adSellerName;//渠道商名称
	@Indexed
	private String vUserId;// 邀请人ID
	private String vUserEmail;// 邀请人账号
	private String vUserPhone;// 邀请人手机
	@Indexed
	private String vUserNick;// 邀请人昵称
	private long ctime;
	private long logintime;//最后登录时间
	private int ctn;//渠道注册成功为1
	private String creater;//渠道商创建者
	private long adTime;//广告位创建时间
	
	

	public UserRegistLog() {
		ctime=System.currentTimeMillis();
		ctn=0;
	}
	//渠道注册
	public UserRegistLog(Ad ad, User user) {
		adTime=ad.getCtime();
		adId=ad.getId();
		name=ad.getName();
		adSid=ad.getAdSid();
		adSellerId=ad.getAdSellerId();
		adSellerName=ad.getAdSellerName();
		ctime=System.currentTimeMillis();
		creater=ad.getCreater();
		userId=user.getId();
		userNick=user.getNickName();
		email=user.getEmail();
		phoneNumber=user.getPhoneNumber();
		ctn=1;
		refType=Config.LOG_REG_TYPE_QD;
		logintime=System.currentTimeMillis();
		}
	//邀请注册
	public UserRegistLog(User vUser, User user) {
		userId=user.getId();
		userNick=user.getNickName();
		email=user.getEmail();
		phoneNumber=user.getPhoneNumber();
		vUserId=vUser.getId();
		vUserEmail=vUser.getEmail();
		vUserNick=vUser.getNickName();
		vUserPhone=vUser.getPhoneNumber();
		refType=Config.LOG_REG_TYPE_YQ;
		logintime=System.currentTimeMillis();
		ctime=System.currentTimeMillis();
		ctn=1;
		}
	
	
	//渠道访问
	public UserRegistLog(Ad ad) {
		ctime=System.currentTimeMillis();
		adId=ad.getId();
		name=ad.getName();
		adSid=ad.getAdSid();
		adSellerId=ad.getAdSellerId();
		adSellerName=ad.getAdSellerName();
		this.setAdTime(ad.getCtime());
		creater=ad.getCreater();
		refType=Config.LOG_VIST_TYPE_QD;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserNick() {
		return userNick;
	}

	public void setUserName(String userName) {
		this.userNick = userName;
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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public long getLogintime() {
		return logintime;
	}

	public void setLogintime(long logintime) {
		this.logintime = logintime;
	}

	public int getCtn() {
		return ctn;
	}

	public void setCtn(int ctn) {
		this.ctn = ctn;
	}

	public String getCreater() {
		return creater;
	}

	public void setCreater(String creater) {
		this.creater = creater;
	}

	public String getAdSellerName() {
		return adSellerName;
	}

	public void setAdSellerName(String adSellerName) {
		this.adSellerName = adSellerName;
	}

	public long getAdTime() {
		return adTime;
	}

	public void setAdTime(long adTime) {
		this.adTime = adTime;
	}



	

}
