package operation.pojo.activity;

import org.springframework.data.annotation.Id;

/**
 * 
* @ClassName: ActivityUser
* @Description: 活动参与&&活动报名
* @author tangli
* @date 2015年3月23日 下午2:30:48
*
 */
public class ActivityUser {
	@Id
	private String id;
	private String activityId;//活动Id
	private String name;//姓名
	private String phone;//电话
	private String email;//邮件
	private String company;//公司
	private String job;//职务
	private long ctime;//创建时间
	private String address;//地址
	
	
	public ActivityUser() {
		this.ctime = System.currentTimeMillis();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}
	public long getCtime() {
		return ctime;
	}
	public void setCtime(long ctime) {
		this.ctime = ctime;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	
	
}
