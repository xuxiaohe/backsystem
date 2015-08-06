package operation.pojo.course;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
/**
* 
* @ClassName: Lesson
* @Description: 课时
* @author Jack Tang
* @date 2014年12月19日 上午8:36:27
*
*/
@Document(collection="newLesson")
public class Lesson {
	@Id
	private String id;
	private int isUsed;//0未被课程引用1被引用
	private String title;//课程名称
	private String intro;//课程描述	
	private long length;//课程文件大小
	private long timer;//课程时长
	private String md5;//课程MD5
	private String logoUrl;//课程图片
	private int order;//序号
	private String localUrl;//服务器存储URL
	//private Map<String, String> playUrls;//课程播放地址 ex:Map<mp4,http...>
	private List<Object> tags;//课程标签（预留）
	private long ctime;//创建时间
	private long utime;//更新时间
	private Object createUser;
    private String type ;//Video,PPT PDF Excel Word 

    private String isbuy;//1 可看  0 不可看
    @Indexed
    private Object  knowledge;//课时知识
    /**
     * 课时审核状态  同步于所对应的知识审核状态
     */
    private int status;
    /**
     * 课时审核描述
     */
    private String checkDesc;
    
    public Lesson(){
    	super();
    }
    
    public Lesson(String title,String intro,int order,String createUser,Knowledge knowledge){
    	this.title=title;
    	this.intro=intro;
//    	this.length=knowledge.
//    	this.type=knowledge.getKngType();
//    	this.timer=knowledge.get
//    	this.md5=knowledge.
    	this.type=knowledge.getKngType()+"";
    	this.length=knowledge.getFileSize();
    	this.knowledge=knowledge.getId();
    	this.status=knowledge.getStatus();
    	this.order=order;
    	this.localUrl=knowledge.getFurl();
    	this.createUser=createUser;
    	long time=System.currentTimeMillis();
    	this.ctime=time;
    	this.utime=time;
    }
    
    public Lesson(String title,String intro,int order,String createUser){
    	this.title=title;
    	this.intro=intro;
    	this.order=order;
    	this.createUser=createUser;
    	long time=System.currentTimeMillis();
    	this.ctime=time;
    	this.utime=time;
    }
	
	public Object getKnowledge() {
		return knowledge;
	}

	public void setKnowledge(Object knowledge) {
		this.knowledge = knowledge;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getLocalUrl() {
		return localUrl;
	}

	public void setLocalUrl(String localUrl) {
		this.localUrl = localUrl;
	}

	



	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public long getTimer() {
		return timer;
	}

	public void setTimer(long timer) {
		this.timer = timer;
	}

	public List<Object> getTags() {
		return tags;
	}

	public void setTags(List<Object> tags) {
		this.tags = tags;
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

	
	public Object getCreateUser() {
		return createUser;
	}

	public void setCreateUser(Object createUser) {
		this.createUser = createUser;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCheckDesc() {
		return checkDesc;
	}

	public void setCheckDesc(String checkDesc) {
		this.checkDesc = checkDesc;
	}

	public int getIsUsed() {
		return isUsed;
	}

	public void setIsUsed(int isUsed) {
		this.isUsed = isUsed;
	}

	public String getIsbuy() {
		return isbuy;
	}

	public void setIsbuy(String isbuy) {
		this.isbuy = isbuy;
	}

	
	
	
}
