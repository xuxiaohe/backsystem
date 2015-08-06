package operation.pojo.course;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection="userGroupCourse")
public class UserGroupCourse {
	@Id
	private String id;
	private String userId;
	private Object group;
	private Object course;
	private String groupCourseId;
	private List<UserChapter> userChapters;
	private int allLessonNum; //此课程下章节数量
	private int allLessonStudyedNum; //此课程下用户已经学完的章节数量
	private boolean studyed;//此课程是否已经学习完毕
	private boolean faved;//此课程是否被收藏
	private boolean AllPermissions;//是否拥有全部权限(true:能够查看全部课时，能够分享，收藏；false：只能够看第一章节,加入群组之后拥有全部权限)
	private long ctime;  //创建时间
	private long utime;//更新时间
	
	public UserGroupCourse(){
		super();
	}
	
	public UserGroupCourse(String userId,String groupId,String courseId){
		super();
		this.userId=userId;
		this.group=groupId;
		this.course=courseId;
		this.allLessonStudyedNum=0;
		this.studyed=false;
		this.faved=false;
		long time=System.currentTimeMillis();
		this.ctime=time;
		this.utime=time;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public Object getGroup() {
		return group;
	}

	public void setGroup(Object group) {
		this.group = group;
	}

	public Object getCourse() {
		return course;
	}
	public void setCourse(Object course) {
		this.course = course;
	}
	public String getGroupCourseId() {
		return groupCourseId;
	}
	public void setGroupCourseId(String groupCourseId) {
		this.groupCourseId = groupCourseId;
	}
	public List<UserChapter> getUserChapters() {
		return userChapters;
	}
	public void setUserChapters(List<UserChapter> userChapters) {
		this.userChapters = userChapters;
	}
	public int getAllLessonNum() {
		return allLessonNum;
	}

	public void setAllLessonNum(int allLessonNum) {
		this.allLessonNum = allLessonNum;
	}

	public int getAllLessonStudyedNum() {
		return allLessonStudyedNum;
	}

	public void setAllLessonStudyedNum(int allLessonStudyedNum) {
		this.allLessonStudyedNum = allLessonStudyedNum;
	}

	public boolean isStudyed() {
		return studyed;
	}
	public void setStudyed(boolean studyed) {
		this.studyed = studyed;
	}
	public boolean isFaved() {
		return faved;
	}
	public void setFaved(boolean faved) {
		this.faved = faved;
	}
	public long getCtime() {
		return ctime;
	}
	public void setCtime(long ctime) {
		this.ctime = ctime;
	}
	public long getUtime() {
		return utime;
	}
	public boolean isAllPermissions() {
		return AllPermissions;
	}
	public void setAllPermissions(boolean allPermissions) {
		AllPermissions = allPermissions;
	}
	public void setUtime(long utime) {
		this.utime = utime;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
}
