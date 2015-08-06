package operation.pojo.box;

import org.springframework.data.annotation.Id;

public class Box {

	@Id
	private String id;
	
	private Object post;//存储boxpostId
	
	private Object sourceId;  // 存储数据Id
	
	private Object sourceType;// 类型
	
	private int weightSort;//权重
	
	private String groupid="";
	
	public int getWeightSort() {
		return weightSort;
	}
	public void setWeightSort(int weightSort) {
		this.weightSort = weightSort;
	}

	private long utime;
	
	private long ctime;

	public Box(){
		super();
	}
	public Box(String post,String sourceId,String sourceType,Long time,int weightSort){
		super();
		this.post=post;
		this.sourceId=sourceId;
		this.sourceType=sourceType;
//		long time=System.currentTimeMillis();
		this.ctime=time;
		this.utime=time;
		this.weightSort=weightSort;
	}
	
	public Box(String post,String sourceId,String sourceType,Long time,int weightSort,String groupid){
		super();
		this.post=post;
		this.sourceId=sourceId;
		this.sourceType=sourceType;
//		long time=System.currentTimeMillis();
		this.ctime=time;
		this.utime=time;
		this.weightSort=weightSort;
		this.groupid=groupid;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getPost() {
		return post;
	}

	public void setPost(Object post) {
		this.post = post;
	}

	public Object getSourceId() {
		return sourceId;
	}

	public void setSourceId(Object sourceId) {
		this.sourceId = sourceId;
	}

	public Object getSourceType() {
		return sourceType;
	}

	public void setSourceType(Object sourceType) {
		this.sourceType = sourceType;
	}

	public long getUtime() {
		return utime;
	}

	public void setUtime() {
		this.utime = System.currentTimeMillis();
	}

	public long getCtime() {
		return ctime;
	}

	public void setCtime() {
		this.ctime = System.currentTimeMillis();
	}
	public String getGroupid() {
		return groupid;
	}
	public void setGroupid(String groupid) {
		this.groupid = groupid;
	}
	
}
