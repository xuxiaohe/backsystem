package operation.pojo.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.ZSetOperations;

import operation.pojo.jobs.Jobs;

public class ResponseUser {
	private String id;

	private String userName;

	private String sex;

	private String email;

	private String phoneNumber;

	private String intro;

	private String tag;

	private long ctime;

	private long logintime;
	
	private String nickName;
	
	private double [] location;
	
	 /**
    * 纬度
    */
   public double lat;
   
   /**
    * 经度
    */
   public double lng;
   
 //20140911云学堂增加字段
 	private Jobs job; //我的职位
 	private Jobs interestJob;//感兴趣职位
 	private long userNumber;//用户号
 	private long birthday;//生日
 	private int contactStatus;
 	private int robot;
 	private int followerCount; //粉丝数量
 	private int attentionCount;//关注数量
 	//20150416添加
 	private long  dryCargoCount;//干货数量
 	private long topicCount;
 	private String registType;

 	

 	public String getRegistType() {
		return registType;
	}

	public void setRegistType(String registType) {
		this.registType = registType;
	}

	public long getBirthday() {
		return birthday;
	}

	public void setBirthday(long birthday) {
		this.birthday = birthday;
	}


 	public Jobs getJob() {
		return job;
	}

	public void setJob(Jobs job) {
		this.job = job;
	}

	public Jobs getInterestJob() {
		return interestJob;
	}

	public void setInterestJob(Jobs interestJob) {
		this.interestJob = interestJob;
	}

	public long getUserNumber() {
 		return userNumber;
 	}

 	public void setUserNumber(long userNumber) {
 		this.userNumber = userNumber;
 	}
 	//20140911云学堂增加字段


	private ResponseOpenFireUser openFireUser;
	private String logoURL;
	private String constelLation;//星座
	
	private Set<String> industry;//行业
	private String direction;//方向
	
	private String industryName;//行业名称
	
	private String station;//岗位
	
	private String education;//学历
	
	private String school;//学校
	
	private String area;//地区
	
	private String company;//公司
	
	private Set<String> interest;//感兴趣得事情
	
	private Set<String> special;//擅长
	
	private int age;//年纪
	
//	private Set<ZSetOperations.TypedTuple<String>> userTageName;//用户标签
	private Object userTageName;//用户标签
	private int scoreSum;//标签数量
	
	public ResponseUser(User user) {
		super();
		this.setId(user.getId());
		this.setUserName(user.getUserName());
		this.setSex(user.getSex());
		this.setEmail(user.getEmail());
		this.setPhoneNumber(user.getPhoneNumber());
		this.setIntro(user.getIntro());
		this.setTag(user.getTag());
		this.setCtime(user.getCtime());
		this.setLogintime(user.getLogintime());
		//openFireUser1 = user.getOpenFireUser();
		if(user.getOpenFireUser()!=null){
		ResponseOpenFireUser resp = new ResponseOpenFireUser(user.getOpenFireUser());
		this.setOpenFireUser(resp);
		}
		this.setLogoURL(user.getLogoURL());
		this.setNickName(user.getNickName());
		this.setLat(user.getLat());
		this.setLng(user.getLng());
		this.setLocation(user.getLocation());
		//20140911新增职位字段
		this.setJob(user.getJob());
		this.setInterestJob(user.getInterestJob());
		this.setUserNumber(user.getUserNumber());
		this.setBirthday(user.getBirthday());
		this.setConstelLation(user.getConstelLation());
		this.setIndustry(user.getIndustry());
		this.setStation(user.getStation());
		this.setEducation(user.getEducation());
		this.setSchool(user.getSchool());
		this.setArea(user.getArea());
		this.setCompany(user.getCompany());
		this.setInterest(user.getInterest());
		this.setSpecial(user.getSpecial());
		this.setAge(user.getAge());
		this.setUserTageName(user.getUserTageName());
		this.setScoreSum(user.getScoreSum());
		this.setDirection(user.getDirection());
		this.setIndustryName(user.getIndustryName());
		this.setRobot(user.getRobot());
		this.setRegistType(user.getRegistType());
	}
	/**
	 * 将user集合转换成
	 * @param user
	 * @return
	 */
	public List<ResponseUser> toResponseUser(List<User> user){
		List<ResponseUser> respList = new ArrayList<ResponseUser>();
		if(null != user){
			for (int i = 0; i < user.size(); i++) {
				ResponseUser respUser = new ResponseUser(user.get(i));
				 respList.add(respUser);
			}
		}
		return respList;
		
	}

	public ResponseUser() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
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

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public long getCtime() {
		return ctime;
	}

	public void setCtime(long ctime) {
		this.ctime = ctime;
	}

	public long getLogintime() {
		return logintime;
	}

	public void setLogintime(long logintime) {
		this.logintime = logintime;
	}

	

	public String getLogoURL() {
		return logoURL;
	}

	public void setLogoURL(String logoURL) {
		this.logoURL = logoURL;
	}

	@Override
	public String toString() {
		return "ResponseUser [id=" + id + ", userName=" + userName + ", sex="
				+ sex + ", email=" + email + ", phoneNumber=" + phoneNumber
				+ ", intro=" + intro + ", tag=" + tag + ", ctime=" + ctime
				+ ", logintime=" + logintime + ", openFireUser=" + openFireUser
				+ ", logoURL=" + logoURL + "]";
	}
	
	public ResponseOpenFireUser getOpenFireUser() {
		return openFireUser;
	}
	public void setOpenFireUser(ResponseOpenFireUser openFireUser) {
		this.openFireUser = openFireUser;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public double[] getLocation() {
		return location;
	}

	public void setLocation(double[] location) {
		this.location = location;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}

	public String getConstelLation() {
		return constelLation;
	}

	public void setConstelLation(String constelLation) {
		this.constelLation = constelLation;
	}

	

	public String getStation() {
		return station;
	}

	public void setStation(String station) {
		this.station = station;
	}

	public String getEducation() {
		return education;
	}

	public void setEducation(String education) {
		this.education = education;
	}

	public String getSchool() {
		return school;
	}

	public void setSchool(String school) {
		this.school = school;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public Set<String> getInterest() {
		return interest;
	}

	public void setInterest(Set<String> interest) {
		this.interest = interest;
	}

	public Set<String> getSpecial() {
		return special;
	}

	public void setSpecial(Set<String> special) {
		this.special = special;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}



	public int getScoreSum() {
		return scoreSum;
	}

	public void setScoreSum(int scoreSum) {
		this.scoreSum = scoreSum;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getIndustryName() {
		return industryName;
	}

	public void setIndustryName(String industryName) {
		this.industryName = industryName;
	}

	public int getContactStatus() {
		return contactStatus;
	}

	public void setContactStatus(int contactStatus) {
		this.contactStatus = contactStatus;
	}

	public Set<String> getIndustry() {
		return industry;
	}

	public void setIndustry(Set<String> industry) {
		this.industry = industry;
	}

	public Object getUserTageName() {
		return userTageName;
	}

	public void setUserTageName(Object userTageName) {
		this.userTageName = userTageName;
	}

	public int getRobot() {
		return robot;
	}

	public void setRobot(int robot) {
		this.robot = robot;
	}

	public int getFollowerCount() {
		return followerCount;
	}

	public void setFollowerCount(int followerCount) {
		this.followerCount = followerCount;
	}

	public int getAttentionCount() {
		return attentionCount;
	}

	public void setAttentionCount(int attentionCount) {
		this.attentionCount = attentionCount;
	}

	public long getDryCargoCount() {
		return dryCargoCount;
	}

	public void setDryCargoCount(long dryCargoCount) {
		this.dryCargoCount = dryCargoCount;
	}

	public long getTopicCount() {
		return topicCount;
	}

	public void setTopicCount(long topicCount) {
		this.topicCount = topicCount;
	}


	
	
}
