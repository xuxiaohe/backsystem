package operation.pojo.study;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection="study")
public class Study {

	@Id
	private String id;
	@Indexed
	private Object userId;  //用户Id
	@Indexed
	private String domain;//域即产品线  如：yxt
	@Indexed
	private String appKey;  //来源 ，如 ios，android，pc,oss
	@Indexed
	private String sourceId; //类型ID(话题Id，课程Id等)
	@Indexed
	private String studyType; //类型，0-9代表话题相关（如：0：话题 1：回复（打个比方）） 10-19 代表课程相关  （10:基础课程 11：群组课程 ）
	@Indexed
	private long ctime;
	private long utime;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Object getUserId() {
		return userId;
	}
	public void setUserId(Object userId) {
		this.userId = userId;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getAppKey() {
		return appKey;
	}
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public String getStudyType() {
		return studyType;
	}
	public void setStudyType(String studyType) {
		this.studyType = studyType;
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
	public void setUtime(long utime) {
		this.utime = utime;
	}
}
