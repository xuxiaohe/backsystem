package operation.pojo.course;

import java.util.List;

import operation.pojo.cloudfile.Citem;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
/**
 * 
 * @ClassName: Knowledge
 * @Description: 知识
 * @author JackTang
 * @date 2014年12月23日 下午12:50:22
 */

public class Knowledge {
	@Id
	private String id;
	@Indexed
	private String name;// 知识名字
	private String logoUrl;// 知识预览图
	private int kngType;// 知识类型： 1-视频 2-文档
	private int pages;// 文档页数
	private long duration;// 视频时长，单位：秒
	private String furl;// 原知识全地址
	private String fid;// 文件id
	//知识大小 bit
	private long fileSize;
	@Indexed
	private String cid;// 持久化云处理的进程ID
	private String chash;// 云处理结果保存在服务端的
	private long ctime;//创建时间
	private long utime;//更新时间
    private int ccode;//0（成功），1（等待处理），2（正在处理），3（处理失败），4（通知提交失败）。
    private String desc;//简介
    private String content;//详细内容
	private int arc;// 引用计数
	private int styhour;//学时
	private int styscore;//学分
	private int stypoint;//
	private  String author;//作者
	private Object cuser;//创建者
	private int status;//1 审核中，2审核通过 3审核失败
	private String checkdesc;//审核描述
	private boolean isdelete;//是否已删除
	private boolean isopen;//是否开放
	private List<Citem> citems;//解码后的citems
	private List<Citem> pcItems;//解码后的适用于Pc的item
	private List<Citem> appItems;//解码后的适用于移动端的item
	private int words;//文档字数
	
    //1.9添加两个字段
	private int praiseCount;//被赞数量
	private int favCount;//被收藏数量
	
	private String tagNames;//标签
	
	private long viewCount;//浏览次数
	
	private Object categoryId="";
	
	private Object childCategoryId="";
	// 是否公开 
	private boolean isPublic;
	
	private boolean realTime;//是否实时
	
	private String cacheUrl;//app 本地缓存地址
	
	public int getPraiseCount() {
		return praiseCount;
	}

	public void setPraiseCount(int praiseCount) {
		this.praiseCount = praiseCount;
	}

	public int getFavCount() {
		return favCount;
	}
	
	public long getViewCount() {
		return viewCount;
	}

	public void setViewCount(long viewCount) {
		this.viewCount = viewCount;
	}

	public void setFavCount(int favCount) {
		this.favCount = favCount;
	}

	public int getWords() {
		return words;
	}

	public void setWords(int words) {
		this.words = words;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Citem> getPcItems() {
		return pcItems;
	}

	public void setPcItems(List<Citem> pcItems) {
		this.pcItems = pcItems;
	}

	public List<Citem> getAppItems() {
		return appItems;
	}

	public void setAppItems(List<Citem> appItems) {
		this.appItems = appItems;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public int getKngType() {
		return kngType;
	}

	public void setKngType(int kngType) {
		this.kngType = kngType;
	}

	

	
	public int getPages() {
		return pages;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getCtime() {
		return ctime;
	}

	public void setCtime(long ctime) {
		this.ctime = ctime;
	}

	public String getFurl() {
		return furl;
	}

	public void setFurl(String furl) {
		this.furl = furl;
	}

	public String getFid() {
		return fid;
	}

	public void setFid(String fid) {
		this.fid = fid;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getChash() {
		return chash;
	}

	public void setChash(String chash) {
		this.chash = chash;
	}

	public int getArc() {
		return arc;
	}

	public void setArc(int arc) {
		this.arc = arc;
	}

	public List<Citem> getCitems() {
		return citems;
	}

	public void setCitems(List<Citem> citems) {
		this.citems = citems;
	}
     
	public int getCcode() {
		return ccode;
	}

	public void setCcode(int ccode) {
		this.ccode = ccode;
	}

	public int getStyhour() {
		return styhour;
	}

	public void setStyhour(int styhour) {
		this.styhour = styhour;
	}

	public int getStyscore() {
		return styscore;
	}

	public void setStyscore(int styscore) {
		this.styscore = styscore;
	}

	public int getStypoint() {
		return stypoint;
	}

	public void setStypoint(int stypoint) {
		this.stypoint = stypoint;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Object getCuser() {
		return cuser;
	}

	public void setCuser(Object cuser) {
		this.cuser = cuser;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCheckdesc() {
		return checkdesc;
	}

	public void setCheckdesc(String checkdesc) {
		this.checkdesc = checkdesc;
	}

	public boolean isIsdelete() {
		return isdelete;
	}

	public void setIsdelete(boolean isdelete) {
		this.isdelete = isdelete;
	}

	public boolean isIsopen() {
		return isopen;
	}

	public void setIsopen(boolean isopen) {
		this.isopen = isopen;
	}

	public Knowledge() {
		setIsopen(true);
	}

	public long getUtime() {
		return utime;
	}

	public void setUtime(long utime) {
		this.utime = utime;
		
	}

	public String getTagNames() {
		return tagNames;
	}

	public void setTagNames(String tagNames) {
		this.tagNames = tagNames;
	}

	public Object getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Object categoryId) {
		
		this.categoryId = categoryId==null?"":categoryId;
	}

	public Object getChildCategoryId() {
		return childCategoryId;
	}

	public void setChildCategoryId(Object childCategoryId) {
		this.childCategoryId = childCategoryId==null?"":childCategoryId;
	}

	public boolean getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public boolean isRealTime() {
		return realTime;
	}

	public void setRealTime(boolean realTime) {
		this.realTime = realTime;
	}

	public String getCacheUrl() {
		return cacheUrl;
	}

	public void setCacheUrl(String cacheUrl) {
		this.cacheUrl = cacheUrl;
	}
	
}
