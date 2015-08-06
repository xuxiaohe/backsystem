//package operation.pojo.drycargo;
//
//import java.util.List;
//import java.util.Set;
//
//import operation.pojo.praise.UserPraiseResponse;
//import operation.pojo.user.UserShort;
//
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.index.Indexed;
//
//public class DrycargoBeanResponse {
//	@Id
//	private String id;
//	private String url; //干货的源地址
//	private String fileUrl; //干货的关键图片或路径
//	private String message;//干货的内容
//	private String description;//干货信息
//	private long ctime;//创建时间
//	private long utime;//修改时间
//
//	private int viewCount; //浏览量
//	private List<UserShort> sharePerList;// 分享人列表
//	public List<Object> shareids;// 分享人id列表查询使用
//	
//	private Set<String> drycargoTagName;
//	
//	//以下为调整干货回复的字段
//	//private int viewCount; // 查看数
//	private int replyCount; // 回复数
//	private int newReplyCount;//主题的新回复数
//	private int likesCount; // 被点赞的次数
//	
//	private int unLikeCount;//不赞数量
//	
//	private int shareCount;
//	
//	private int favoritesCount; // 被收藏的次数 TODO 是否保留
//
//	private int displayOrder; // >0为置顶,<0不显示,==0正常 -1为回收站 -2待审核 -3为被忽略
//
//	private int digestLevel; // 精华级别,1~3
//	private boolean isDeleted = false; //是否删除
//	
//	private double lat;// 纬度
//	private double [] position;//位置
//	private double lng;// 经度
//	
//	private String localName;//地理位置名称
//	
//	private boolean like;//态度 0无态度 1赞
//	private boolean unlike; //态度 0无态度 1踩
//	
//	private List<UserPraiseResponse> praiseResponse; //赞列表
//	
//	private Set<String> tagName;//话题的标签
//	
//	private int weightSort;//权重
//	
//	@Indexed
//	private int dryFlag;//干货0炫页1
//	
//	public DrycargoBeanResponse(DrycargoBean db){
//		super();
//		this.setId(db.getId());
//		this.setUrl(db.getUrl());
//		this.setFileUrl(db.getFileUrl());
//		this.setMessage(db.getMessage());
//		this.setCtime(db.getCtime());
//		this.setUtime(db.getUtime());
//		this.setViewCount(db.getViewCount());
//		this.setReplyCount(db.getReplyCount());
//		this.setLikesCount(db.getLikesCount());
//		this.setUnLikeCount(db.getUnLikeCount());
//		this.setShareCount(db.getShareCount());
//		this.setDisplayOrder(db.getDisplayOrder());
//		this.setDeleted(db.isDeleted());
//		this.setPosition(db.getPosition());
//		this.setLocalName(db.getLocalName());
//		this.setWeightSort(db.getWeightSort());
//		this.setDescription(db.getDescription());
//		this.setDryFlag(db.getDryFlag());
//		
//	}
//	
//
//	
//	
//
//	public String getId() {
//		return id;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}
//
//
//
//
//	public long getCtime() {
//		return ctime;
//	}
//
//	public void setCtime(long ctime) {
//		this.ctime = ctime;
//	}
//
//	public long getUtime() {
//		return utime;
//	}
//
//	public void setUtime(long utime) {
//		this.utime = utime;
//	}
//
//	public String getUrl() {
//		return url;
//	}
//
//	public void setUrl(String url) {
//		this.url = url;
//	}
//
//	public String getFileUrl() {
//		return fileUrl;
//	}
//
//	public void setFileUrl(String fileUrl) {
//		this.fileUrl = fileUrl;
//	}
//
//	public String getMessage() {
//		return message;
//	}
//
//	public void setMessage(String message) {
//		this.message = message;
//	}
//
//	public int getViewCount() {
//		return viewCount;
//	}
//
//	public void setViewCount(int viewCount) {
//		this.viewCount = viewCount;
//	}
//
//	
//
//	public Set<String> getDrycargoTagName() {
//		return drycargoTagName;
//	}
//
//	public void setDrycargoTagName(Set<String> drycargoTagName) {
//		this.drycargoTagName = drycargoTagName;
//	}
//
//
//
//	public int getReplyCount() {
//		return replyCount;
//	}
//
//	public void setReplyCount(int replyCount) {
//		this.replyCount = replyCount;
//	}
//
//	public int getNewReplyCount() {
//		return newReplyCount;
//	}
//
//	public void setNewReplyCount(int newReplyCount) {
//		this.newReplyCount = newReplyCount;
//	}
//
//	
//
//	public int getLikesCount() {
//		return likesCount;
//	}
//
//	public void setLikesCount(int likesCount) {
//		this.likesCount = likesCount;
//	}
//
//
//
//	
//
//	public int getUnLikeCount() {
//		return unLikeCount;
//	}
//
//	public void setUnLikeCount(int unLikeCount) {
//		this.unLikeCount = unLikeCount;
//	}
//
//
//
//	public int getShareCount() {
//		return shareCount;
//	}
//
//	public void setShareCount(int shareCount) {
//		this.shareCount = shareCount;
//	}
//
//	public int getFavoritesCount() {
//		return favoritesCount;
//	}
//
//	public void setFavoritesCount(int favoritesCount) {
//		this.favoritesCount = favoritesCount;
//	}
//
//	public int getDisplayOrder() {
//		return displayOrder;
//	}
//
//	public void setDisplayOrder(int displayOrder) {
//		this.displayOrder = displayOrder;
//	}
//
//	public int getDigestLevel() {
//		return digestLevel;
//	}
//
//	public void setDigestLevel(int digestLevel) {
//		this.digestLevel = digestLevel;
//	}
//
//	public boolean isDeleted() {
//		return isDeleted;
//	}
//
//	public void setDeleted(boolean isDeleted) {
//		this.isDeleted = isDeleted;
//	}
//
//	public double getLat() {
//		return lat;
//	}
//
//	public void setLat(double lat) {
//		this.lat = lat;
//	}
//
//	public double[] getPosition() {
//		return position;
//	}
//
//	public void setPosition(double[] position) {
//		this.position = position;
//	}
//
//	public double getLng() {
//		return lng;
//	}
//
//	public void setLng(double lng) {
//		this.lng = lng;
//	}
//
//	public String getLocalName() {
//		return localName;
//	}
//
//	public void setLocalName(String localName) {
//		this.localName = localName;
//	}
//
//	public boolean isLike() {
//		return like;
//	}
//
//	public void setLike(boolean like) {
//		this.like = like;
//	}
//
//	public boolean isUnlike() {
//		return unlike;
//	}
//
//	public void setUnlike(boolean unlike) {
//		this.unlike = unlike;
//	}
//
//	public List<UserPraiseResponse> getPraiseResponse() {
//		return praiseResponse;
//	}
//
//	public void setPraiseResponse(List<UserPraiseResponse> praiseResponse) {
//		this.praiseResponse = praiseResponse;
//	}
//
//	public Set<String> getTagName() {
//		return tagName;
//	}
//
//	public void setTagName(Set<String> tagName) {
//		this.tagName = tagName;
//	}
//
//	public List<UserShort> getSharePerList() {
//		return sharePerList;
//	}
//
//	public void setSharePerList(List<UserShort> sharePerList) {
//		this.sharePerList = sharePerList;
//	}
//
//	public List<Object> getShareids() {
//		return shareids;
//	}
//
//	public void setShareids(List<Object> shareids) {
//		this.shareids = shareids;
//	}
//
//
//
//
//
//	public int getWeightSort() {
//		return weightSort;
//	}
//
//
//
//
//
//	public void setWeightSort(int weightSort) {
//		this.weightSort = weightSort;
//	}
//
//
//
//
//
//	public String getDescription() {
//		return description;
//	}
//
//
//
//
//
//	public void setDescription(String description) {
//		this.description = description;
//	}
//
//
//
//
//
//	public int getDryFlag() {
//		return dryFlag;
//	}
//
//
//
//
//
//	public void setDryFlag(int dryFlag) {
//		this.dryFlag = dryFlag;
//	}
//
//	
//
//
//}
