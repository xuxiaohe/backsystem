package operation.pojo.group;

import java.util.List;
import java.util.Map;

import operation.pojo.jobs.Industryclass;

public class ResponseGroup {
	private String id;
	private String groupName;
	private String intro;
	private String logoUrl;
	private String isOpen;
	private long ctime;
	private long utime;
	private ResponseOpenFire openFireGroup;
	private Map<String, Integer> summary;
	private String isMember;
	private long groupNumber;

	private int groupMax = 50;

	private double[] position;

	/**
	 * 纬度
	 */
	public double lat;

	/**
	 * 经度
	 */
	public double lng;
	// 职能
	private Industryclass industryClass;
	// 热度
	private int temperature;
	
	private String qrCodeUrl;//二维码访问地址
	
	private double distance; //两点坐标距离
	
	private String localName;//地理位置名称
	private boolean isGeoOpen;//地理坐标是否打开
	private String tagNames;
	
	private Object categoryId;//一级分类id;
	private Object childCategoryId;//二级分类Id

	public int getTemperature() {
		return temperature;
	}

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	public Industryclass getIndustryClass() {
		return industryClass;
	}

	public void setIndustryClass(Industryclass industryClass) {
		this.industryClass = industryClass;
	}

	public ResponseGroup(XueWenGroup group) {
		super();
		this.setId(group.getId());
		this.setGroupName(group.getGroupName());
		this.setIntro(group.getIntro());
		this.setLogoUrl(group.getLogoUrl());
		this.setIsOpen(group.getIsOpen());
		this.setCtime(group.getCtime());
		this.setUtime(group.getUtime());
		OpenFireGroup openFireGroup = group.getOpenFireGroup();
		if(openFireGroup!=null){
		ResponseOpenFire resp = new ResponseOpenFire(openFireGroup);
		this.setOpenFireGroup(resp);
		}
		this.setSummary(group.getSummary());
		this.setIsMember(group.getIsMember());
		this.setGroupNumber(group.getGroupNumber());
		this.setGroupMax(group.getGroupMax());
		this.setLat(group.getLat());
		this.setLng(group.getLng());
		this.setPosition(group.getPosition());
		this.setIndustryClass(group.getIndustryClass());
		this.setTemperature(group.getTemperature());
		this.setQrCodeUrl(group.getQrCodeUrl());
		this.setLocalName(group.getLocalName());
		this.setGeoOpen(group.isGeoOpen());
		this.setTagNames(group.getTagNames());
		this.setCategoryId(group.getCategoryId());
		this.setChildCategoryId(group.getChildCategoryId());
	}

	public ResponseGroup() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getIsOpen() {
		return isOpen;
	}

	public void setIsOpen(String isOpen) {
		this.isOpen = isOpen;
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

	public ResponseOpenFire getOpenFireGroup() {
		return openFireGroup;
	}

	public void setOpenFireGroup(ResponseOpenFire openFireGroup) {
		this.openFireGroup = openFireGroup;
	}

	public Map<String, Integer> getSummary() {
		return summary;
	}

	// public OpenFireGroup getOpenFireGroup() {
	// return openFireGroup;
	// }
	//
	// public void setOpenFireGroup(OpenFireGroup openFireGroup) {
	// this.openFireGroup = openFireGroup;
	// }

	public void setSummary(Map<String, Integer> summary) {
		this.summary = summary;
	}

	public String getIsMember() {
		return isMember;
	}

	public void setIsMember(String isMember) {
		this.isMember = isMember;
	}

	public long getGroupNumber() {
		return groupNumber;
	}

	public void setGroupNumber(long groupNumber) {
		this.groupNumber = groupNumber;
	}

	public int getGroupMax() {
		return groupMax;
	}

	public void setGroupMax(int groupMax) {
		this.groupMax = groupMax;
	}

	public double[] getPosition() {
		return position;
	}

	public void setPosition(double[] position) {
		this.position = position;
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

	public String getQrCodeUrl() {
		return qrCodeUrl;
	}

	public void setQrCodeUrl(String qrCodeUrl) {
		this.qrCodeUrl = qrCodeUrl;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public boolean isGeoOpen() {
		return isGeoOpen;
	}

	public void setGeoOpen(boolean isGeoOpen) {
		this.isGeoOpen = isGeoOpen;
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
		this.categoryId = categoryId;
	}

	public Object getChildCategoryId() {
		return childCategoryId;
	}

	public void setChildCategoryId(Object childCategoryId) {
		this.childCategoryId = childCategoryId;
	}
	
	
}
