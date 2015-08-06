package operation.pojo.topics;

import java.util.List;

import org.springframework.data.annotation.Id;

public class PostResponse {

	@Id
	private String postId;  //帖子PID
	private String topicId; // 归属主题ID
	private String appKey;//
	
	private int layer;//所在楼层		
	private String authorId; // 创建人id
	private String authorName; // 创建人名字
	private String authorLogoUrl;//创建者图片
	private long ctime; // 创建时间
	private long utime; // 更新时间
	private String message;//内容
//	private boolean hasAudio =false;//是否有语音
//	private boolean hasPic=false;//是否有图片  
	private String type;//类型  0:文件 1：语音 2：图片带有图片
//	private List<Integer> file_ids;//文件id集合 云存储生成
	private String fileUrl;//文件地址地址
	
	private List<SubPost> subPosts;//存储三条
	private int subPostsSize;//子回复节点长度
	private boolean isDeleted = false; // 是否已删除
	private int displayOrder; // >0为置顶,<0不显示,==0正常 -1为回收站 -2待审核 -3为被忽略
	
	private int likesCount; // 被点赞的次数
	
	private double lat;// 纬度
	private double [] position;//位置
	private double lng;// 经度
	
	private boolean like;
	
	private List<Images> images;//回复多张图片
	

	public PostResponse(){
		super();
	}
	public PostResponse(Post post){
		this.postId=post.getPostId();
		this.topicId=post.getTopicId();
		this.appKey=post.getAppKey();
		this.layer=post.getLayer();
		this.authorId=post.getAuthorId();
		this.authorName=post.getAuthorName();
		this.authorLogoUrl=post.getAuthorLogoUrl();
		this.ctime=post.getCtime();
		this.utime=post.getUtime();
		this.message=post.getMessage();
		this.type=post.getType();
		this.fileUrl=post.getFileUrl();
		this.subPosts=post.getSubPosts();
		this.subPostsSize=post.getSubPostsSize();
		this.isDeleted=post.isDeleted();
		this.displayOrder=post.getDisplayOrder();
		this.likesCount=post.getLikesCount();
		this.lat=post.getLat();
		this.lng=post.getLng();
		this.position=post.getPosition();
		this.images=post.getImages();
		
	}
	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public String getTopicId() {
		return topicId;
	}

	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public String getAuthorId() {
		return authorId;
	}

	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getAuthorLogoUrl() {
		return authorLogoUrl;
	}

	public void setAuthorLogoUrl(String authorLogoUrl) {
		this.authorLogoUrl = authorLogoUrl;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public List<SubPost> getSubPosts() {
		return subPosts;
	}

	public void setSubPosts(List<SubPost> subPosts) {
		this.subPosts = subPosts;
	}

	public int getSubPostsSize() {
		return subPostsSize;
	}

	public void setSubPostsSize(int subPostsSize) {
		this.subPostsSize = subPostsSize;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public int getLikesCount() {
		return likesCount;
	}

	public void setLikesCount(int likesCount) {
		this.likesCount = likesCount;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double[] getPosition() {
		return position;
	}

	public void setPosition(double[] position) {
		this.position = position;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public boolean isLike() {
		return like;
	}

	public void setLike(boolean like) {
		this.like = like;
	}
	public List<Images> getImages() {
		return images;
	}
	public void setImages(List<Images> images) {
		this.images = images;
	}

	
	
	
}
