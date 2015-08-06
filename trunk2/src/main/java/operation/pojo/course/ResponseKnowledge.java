//package operation.pojo.course;
//
//import java.util.List;
//
//import operation.pojo.cloudfile.AttachFile;
//import operation.pojo.cloudfile.Citem;
//
//public class ResponseKnowledge {
//	private String id;
//	private String name;// 知识名字
//	private String logoUrl;// 知识预览图
//	private int kngType;// 知识类型： 1-视频 2-文档
//	private int pages;// 文档页数
//	private int duration;// 视频时长，单位：秒
//	private String furl;// 原知识全地址
//	private String fid;// 文件id
//	private String cid;// 持久化云处理的进程ID
//	private String chash;// 云处理结果保存在服务端的
//	private String ctime;//创建时间
//	private long utime;//更新时间
//    private int ccode;//0（成功），1（等待处理），2（正在处理），3（处理失败），4（通知提交失败）。
//    private String desc;//简介
//    private String content;//详细内容
//	private int arc;// 引用计数
//	private int styhour;//学时
//	private int styscore;//学分
//	private int stypoint;//
//	private  String author;//作者
//	private Object cuser;//创建者
//	private int status;//1 审核中，2审核通过 3审核失败
//	private String checkdesc;//审核描述
//	private boolean isdelete;//是否已删除
//	private boolean isopen;//是否开放
//	private List<Citem> citems;//
//	private AttachFile fileInfo;//
//	public String getId() {
//		return id;
//	}
//	public void setId(String id) {
//		this.id = id;
//	}
//	public String getName() {
//		return name;
//	}
//	public void setName(String name) {
//		this.name = name;
//	}
//	public String getLogoUrl() {
//		return logoUrl;
//	}
//	public void setLogoUrl(String logoUrl) {
//		this.logoUrl = logoUrl;
//	}
//	public int getKngType() {
//		return kngType;
//	}
//	public void setKngType(int kngType) {
//		this.kngType = kngType;
//	}
//	public int getPages() {
//		return pages;
//	}
//	public void setPages(int pages) {
//		this.pages = pages;
//	}
//	public int getDuration() {
//		return duration;
//	}
//	public void setDuration(int duration) {
//		this.duration = duration;
//	}
//	public String getFurl() {
//		return furl;
//	}
//	public void setFurl(String furl) {
//		this.furl = furl;
//	}
//	public String getFid() {
//		return fid;
//	}
//	public void setFid(String fid) {
//		this.fid = fid;
//	}
//	public String getCid() {
//		return cid;
//	}
//	public void setCid(String cid) {
//		this.cid = cid;
//	}
//	public String getChash() {
//		return chash;
//	}
//	public void setChash(String chash) {
//		this.chash = chash;
//	}
//	public String getCtime() {
//		return ctime;
//	}
//	public void setCtime(String ctime) {
//		this.ctime = ctime;
//	}
//	public long getUtime() {
//		return utime;
//	}
//	public void setUtime(long utime) {
//		this.utime = utime;
//	}
//	public int getCcode() {
//		return ccode;
//	}
//	public void setCcode(int ccode) {
//		this.ccode = ccode;
//	}
//	public String getDesc() {
//		return desc;
//	}
//	public void setDesc(String desc) {
//		this.desc = desc;
//	}
//	public String getContent() {
//		return content;
//	}
//	public void setContent(String content) {
//		this.content = content;
//	}
//	public int getArc() {
//		return arc;
//	}
//	public void setArc(int arc) {
//		this.arc = arc;
//	}
//	public int getStyhour() {
//		return styhour;
//	}
//	public void setStyhour(int styhour) {
//		this.styhour = styhour;
//	}
//	public int getStyscore() {
//		return styscore;
//	}
//	public void setStyscore(int styscore) {
//		this.styscore = styscore;
//	}
//	public int getStypoint() {
//		return stypoint;
//	}
//	public void setStypoint(int stypoint) {
//		this.stypoint = stypoint;
//	}
//	public String getAuthor() {
//		return author;
//	}
//	public void setAuthor(String author) {
//		this.author = author;
//	}
//	public Object getCuser() {
//		return cuser;
//	}
//	public void setCuser(Object cuser) {
//		this.cuser = cuser;
//	}
//	public int getStatus() {
//		return status;
//	}
//	public void setStatus(int status) {
//		this.status = status;
//	}
//	public String getCheckdesc() {
//		return checkdesc;
//	}
//	public void setCheckdesc(String checkdesc) {
//		this.checkdesc = checkdesc;
//	}
//	public boolean isIsdelete() {
//		return isdelete;
//	}
//	public void setIsdelete(boolean isdelete) {
//		this.isdelete = isdelete;
//	}
//	public boolean isIsopen() {
//		return isopen;
//	}
//	public void setIsopen(boolean isopen) {
//		this.isopen = isopen;
//	}
//	public List<Citem> getCitems() {
//		return citems;
//	}
//	public void setCitems(List<Citem> citems) {
//		this.citems = citems;
//	}
//	public AttachFile getFileInfo() {
//		return fileInfo;
//	}
//	public void setFileInfo(AttachFile fileInfo) {
//		this.fileInfo = fileInfo;
//	}
//	
//	
//
//}
