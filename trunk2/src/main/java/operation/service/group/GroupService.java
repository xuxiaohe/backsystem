package operation.service.group;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import operation.controller.test.Title;
import operation.exception.XueWenServiceException;
import operation.pojo.box.Box;
import operation.pojo.category.Category;
import operation.pojo.course.GroupShareKnowledge;
import operation.pojo.course.NewCourse;
import operation.pojo.drycargo.Drycargo;
import operation.pojo.group.MyAllGroup;
import operation.pojo.group.OpenFireGroup;
import operation.pojo.group.ResponseGroup;
import operation.pojo.group.ResponseOpenFire;
import operation.pojo.group.XueWenGroup;
import operation.pojo.jobs.Industryclass;
import operation.pojo.pub.QueryModel;
import operation.pojo.push.ZtiaoPush;
import operation.pojo.remmond.Remmond;
import operation.pojo.tags.UserTagBean;
import operation.pojo.topics.Topic;
import operation.pojo.user.MessageContext;
import operation.pojo.user.ResponsePcUser;
import operation.pojo.user.ResponseUser;
import operation.pojo.user.User;
import operation.pojo.user.UserMessage;
import operation.repo.ad.ZtiaoAdMongoTemplate;
import operation.repo.group.GroupMongoTemplate;
import operation.repo.group.GroupRepository;
import operation.repo.group.GroupTemplate;
import operation.repo.user.UserRepository;
import operation.service.ad.ZtiaoAdService;
import operation.service.box.BoxService;
import operation.service.category.CategoryService;
import operation.service.course.GroupShareKnowledgeService;
import operation.service.course.KnowledgeService;
import operation.service.course.NewCourseService;
import operation.service.course.NewGroupCourseService;
import operation.service.drycargo.DrycargoService;
import operation.service.dynamic.GroupDynamicService;
import operation.service.file.MyFileService;
import operation.service.jobs.IndustryService;
import operation.service.push.ZtiaoPushService;
import operation.service.qrcode.QRCodeService;
import operation.service.queue.QueueService;
import operation.service.rabbitmq.RabbitmqService;
import operation.service.remmond.RemmondService;
import operation.service.tags.TagService;
import operation.service.topics.TopicService;
import operation.service.user.ContactUserService;
import operation.service.user.UserNumService;
import operation.service.user.UserService;
import operation.service.util.ObjCopyPropsService;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.util.StringUtils;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import tools.Config;
import tools.JSON2ObjUtil;
import tools.ListComparator;
import tools.MD5Util;
import tools.PageRequestTools;
import tools.RestfulTemplateUtil;
import tools.StringUtil;
import tools.YXTJSONHelper;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

@Service
@Component
@Configuration
/**
 * 群组service层，用于接受controller传递的参数
 * 与repo进行数据库操作
 * @author nes
 *
 */
public class GroupService {
	private static final Logger logger = Logger.getLogger(GroupService.class);
	@Autowired
	private GroupRepository groupRepo;
	@Autowired
	private BoxService boxService;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private MyGroupService myGroupService;
	@Autowired
	private UserService userService;
	@Autowired
	private RemmondService remmondService;
	@Autowired
	private QueueService queueService;
	@Autowired
	private GroupNumService groupNumService;
	@Autowired
	private IndustryService industryService;
	@Autowired
	private MyFileService myFileService;
	@Autowired
	private TopicService topicService;
	@Autowired
	private QRCodeService qRCodeService;
	@Autowired
	private ObjCopyPropsService objCopyPropsService;
	@Autowired
	private UserNumService userNumService;
	@Autowired
	private TagService tagService;
	@Autowired
	private DrycargoService drycargoService;
	@Autowired
	private GroupMongoTemplate groupMongoTemplate;
	@Autowired
	private GroupTemplate groupTemplate;

	@Autowired
	private ContactUserService contactUserService;

	@Autowired
	private NewGroupCourseService newGroupCourseService;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private RabbitmqService rabbitmqService;
	
	@Autowired
	private GroupDynamicService groupDynamicService;
	
	@Autowired
	private GroupShareKnowledgeService groupShareKnowledgeService; 
	
	@Autowired
	private KnowledgeService knowledgeService;
	
	@Autowired
	private ZtiaoPushService ztiaoPushService;
	@Autowired
	private  NewCourseService newCourseService;
	@Autowired
	private DrycargoService drycarGoService;
	
	@Autowired
	private ZtiaoAdMongoTemplate ztiaoAdMongoTemplate;
	
	@Autowired
	private ZtiaoAdService ztiaoAdService;
	

	// group service中用到的系统参数
	@Value("${openfire.createGroup.url}")
	private String openfireCreateGroupUrl;
	@Value("${openfire.destroyGroup.url}")
	private String openfireDestroyGroupUrl;
	@Value("${app.download.url}")
	private String appDownLoadUrl;
	@Value("${tag.service.url}")
	private String tagServiceUrl;
	@Value("${group.qrcode.url}")
	private String groupQrCodeUrl;
	@Value("${group.qrcode.local}")
	private String groupQrCodeLocal;
	

	/**
	 * 创建群组（该方法废弃）
	 * 
	 * @param group
	 * @return 
	 */
	public XueWenGroup create(XueWenGroup group, User user, String tagName)
			throws XueWenServiceException {
		logger.info("====开始创建群组=====userId:" + user.getId());
		if (null == group.getGroupName() || "".equals(group.getGroupName())) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_GROUPNAME_201, null);
		}
		XueWenGroup one = groupRepo.findOneByGroupName(group.getGroupName());
		if (null == one) {
			logger.info("=============创建群组的======");
			String isOpen = group.getIsOpen();
			if (isOpen == null || isOpen.equals("") || isOpen.equals("0")) {
				group.setIsOpen("0");
			} else if (isOpen.equals("2")) {
				if (group.getPassWord() == null) {
					throw new XueWenServiceException(Config.STATUS_201,Config.MSG_GROUPNOPWD_201, null);
				}
			} else if (!isOpen.equals("1")) {
				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_ISOPENERR_201, null);
			}
			one = groupRepo.save(group);
			if (tagName != null) {
				JSONArray ja = (JSONArray) JSONValue.parse(tagName);
				if (ja != null) {
					if (ja.size() > 0) {
						for (int i = 0; i < ja.size(); i++) {
							tagService.saveUserTag(createUserTag(user, one, ja.get(i).toString()));
							tagService.saveUserTagToRedis(createUserTag(user,one, ja.get(i).toString()));
						}
					}
				}
			}
			myGroupService.addMyGroup(user.getId(), group.getId());
			//添加群组创建动态
			creatGroupCreatDynamic(one.getId(),user.getId(),user.getNickName(),user.getLogoURL(),one.getCtime());
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_CREATE_201, null);
		}
		return one;
	}

	/**
	 * 创建群组
	 * 
	 * @author hjn
	 * @param group
	 * @return
	 */
	public XueWenGroup createGroup(XueWenGroup group, User user,String tagName, String lat, String lng, String isGeoOpen)throws XueWenServiceException {
		logger.info("====开始创建群组=====userId:" + user.getId());
		if (StringUtil.isBlank(group.getGroupName())) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_GROUPNAME_201, null);
		}
		if (null != group.getGroupName()) {
			if (!StringUtil.isEmpty(group.getGroupName().toString())) {
				try {
					group.setGroupName(URLDecoder.decode(group.getGroupName(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		if (null != group.getIntro()) {
			if (!StringUtil.isEmpty(group.getIntro())) {
				try {
					group.setIntro(URLDecoder.decode(group.getIntro(),"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (!groupMongoTemplate.isGroupExi(group.getGroupName())) {
			// 群组名称未被使用，进行新群组的创建流程
			// 1.生成群号
			String groupNum = groupNumService.getGroupNum();
			logger.info("生成群号===================:" + groupNum);
			// 2.先创建openfire群组,以便于进行群组的聊天等
//			OpenFireGroup openFireGroup = new OpenFireGroup();
//			openFireGroup.setGroupService(openfireGroupService);
//			List<String> list = new ArrayList<String>();
//			list.add(openfireService);
//			openFireGroup.setServerList(list);
//			openFireGroup.setGroupName(groupNum);
//			openFireGroup.setGroupDesc(group.getGroupName());
//			OpenFireUser ofu = new OpenFireUser();
//			ofu.setOpenFireUserName(String.valueOf(user.getUserNumber()));
//			ofu.setOpenFirePassWord(user.getPassWord());
//			openFireGroup.setGroupCreater(ofu);
//			openFireGroupService.create(openFireGroup);
			group.setOpenFireGroup(creatOpenFireGroup(groupNum,group.getGroupName(),
					String.valueOf(user.getUserNumber()), user.getPassWord()));
			logger.info("创建openfire成功=====================");
			// 3.完善群组信息
			group.setGroupNumber(Long.parseLong(groupNum));
			// 组装群组成员，管理员，创建者等信息
			List<Object> owner = new ArrayList<Object>();
			owner.add(user.getId());
			group.setOwner(owner);
			group.setAdmin(owner);
			group.setMember(owner);
			// 组装创建时间和更新时间
			long loginTime = System.currentTimeMillis();
			group.setCtime(loginTime);
			group.setUtime(loginTime);
			// 规划地理位置信息
			if (null != lat && null != lng) {
				double[] position = new double[] { Double.parseDouble(lng),Double.parseDouble(lat) };
				group.setLng(Double.parseDouble(lng));
				group.setLat(Double.parseDouble(lat));
				group.setPosition(position);
			}
			if ("0".equals(isGeoOpen)) {
				group.setGeoOpen(false);
			} else {
				group.setGeoOpen(true);
			}
			String isOpen = group.getIsOpen();
			// 群组开关控制
			if (isOpen == null || isOpen.equals("") || isOpen.equals("0")) {
				group.setIsOpen("0");
			} else if (isOpen.equals("2")) {
				if (group.getPassWord() == null) {
					throw new XueWenServiceException(Config.STATUS_201,
							Config.MSG_GROUPNOPWD_201, null);
				}
			} else if (!isOpen.equals("1")) {
				throw new XueWenServiceException(Config.STATUS_201,
						Config.MSG_ISOPENERR_201, null);
			}
			//如果群图片为空则随机取一张图片
			if(StringUtil.isBlank(group.getLogoUrl())){
				//随机获取用户头像
				String logoUrl = userService.userLogoUrl();
				group.setLogoUrl(logoUrl);
			}
			// 生成群组二维码并保存群信息入库
			group = groupRepo.save(group);
			logger.info("创建群成功=====================");
			group.setQrCodeUrl(createGroupQRCode(group));
			logger.info("生成二维码成功=====================");
			group = groupRepo.save(group);
			logger.info("保存二维码成功=====================");
			// 将此群加入我的群组列表
			myGroupService.addMyGroup(user.getId(), group.getId());
			logger.info("将群写入我的群列表==================");
			if (!StringUtil.isBlank(tagName)) {
				tagName = JSON2ObjUtil.getArrayFromString(tagName);
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.postForObject(tagServiceUrl
						+ "tag/createTagBatch?domain=" + Config.YXTDOMAIN + "&itemId="
						+ group.getId() + "&userId=" + user.getId()
						+ "&userName=" + user.getNickName() + "&itemType=" + Config.TAG_TYPE_GROUP
						+ "&tagNames=" + tagName, null,String.class);
			}
			//获得一级分类对象
			if(group.getCategoryId()!=null){
				group.setCategoryId(categoryService.formateCategory(categoryService.findOneCategoryById(group.getCategoryId().toString())));
			}
			//获得二级分类对象
			if(group.getChildCategoryId()!=null){
				group.setChildCategoryId(categoryService.formateCategory(categoryService.findOneCategoryById(group.getChildCategoryId().toString())));
			}
			//添加群组创建动态
			creatGroupCreatDynamic(group.getId(),user.getId(),user.getNickName(),user.getLogoURL(),group.getCtime());
			
			try {
				rabbitmqService.sendRegexMessage(group.getId(), Config.TAG_TYPE_GROUP);
			} catch (Exception e) {
				logger.error("=============发送群组过滤消息队列发送错误================");
			}
			return group;
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_CREATE_201, null);
		}
	}
	
	/**
	 * 创建openfire房间
	 * @param groupNum
	 * @param groupDesc
	 * @param userNumber
	 * @param password
	 * @return
	 * @throws XueWenServiceException
	 */
	public OpenFireGroup creatOpenFireGroup(String groupNum,String groupDesc, String userNumber, String password )throws XueWenServiceException{
		Map<String,String> map=new HashMap<String, String>();
		map.put("groupNum", groupNum);
		map.put("groupDesc", groupDesc);
		map.put("userNumber", userNumber);
		map.put("password", password);
		RestfulTemplateUtil restfulTemplateUtil=new RestfulTemplateUtil();
		JSONObject obj=restfulTemplateUtil.getRestApiData(openfireCreateGroupUrl, map);
		JSONObject data=obj.getJSONObject("data");
		Object status=obj.get("status");
		Object msg=obj.get("msg");
//		ResponseContainer rc=(ResponseContainer)JSONObject.toBean(obj, ResponseContainer.class);
		if("200".equals(status.toString())){
			JSONObject rs=data.getJSONObject("result");
			return (OpenFireGroup)JSONObject.toBean(rs, OpenFireGroup.class);
		}else{
			throw new XueWenServiceException(Integer.valueOf(status.toString()),msg.toString() ,null);
		}
	}

	/**
	 * 根据群组ID 获得群组信息
	 * 
	 * @return
	 */
	public XueWenGroup findGroup(String id, String userId)throws XueWenServiceException {
		XueWenGroup one = groupRepo.findOneById(id);
		if (null == one) {
			throw new XueWenServiceException(Config.STATUS_601, Config.MSG_NOGROUP_601,null);
		} else {
			XueWenGroup respGroup = changGroupToResponseGroups(one, userId);
			return respGroup;
		}

	}

	/**
	 * 根据群组ID 获得群成员列表
	 * 
	 * @return
	 */
	public XueWenGroup findGroupMmber(String id, String userId)throws XueWenServiceException {
		XueWenGroup one = groupRepo.findOneById(id);
		if (null == one) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		} else {
			// 判断当前用户身份
			if (one.getOwner().contains(userId)) {
				one.setIsMember("3"); // 创建者
			} else {
				if (one.getAdmin().contains(userId)) {
					one.setIsMember("2"); // 管理员
				} else if (one.getMember().contains(userId))
					one.setIsMember("1"); // 成员
				else {
					one.setIsMember("0"); // 未加入群
				}
			}
			one = rspGroupNoUsersRepeat(one, userId);
			one.getOpenFireGroup().setGroupCreater(null);
			return one;
		}

	}

	/**
	 * 查询话题列表，支持分页功能
	 * 结果不包含自己参与的群
	 * @param p
	 * @return
	 */
	public Page<XueWenGroup> all(Pageable pageable, String userId)
			throws XueWenServiceException {
//		List<String> myGrouplist = myGroupService.myGroupIds(userId);
//		if (null == myGrouplist) {
//			myGrouplist = new ArrayList<String>();
//		}
//		Page<XueWenGroup> group = groupRepo.findByIdNotIn(myGrouplist, pageable);
		Page<XueWenGroup> group = groupRepo.findAll(pageable);
		return group;
	}
	/**
	 * 将群组转成前端需要的属性
	 * @param groups
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<ResponseGroup> changeGroupsToResponseGroup(List<XueWenGroup> groups, String userId)throws XueWenServiceException {
		if (groups.size() <= 0) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		List<ResponseGroup> rps = new ArrayList<ResponseGroup>();
		for (XueWenGroup group : groups) {
			rps.add(changGroupToResponseGroup(group, userId));
		}
		return rps;
	}
	/**
	 * 将群组转成前端需要的属性(包括群组中得更多数据值)
	 * @param groups
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<ResponseGroup> changeGroupsToResponseGroups(List<XueWenGroup> groups, String userId)throws XueWenServiceException {
		if (groups == null || groups.size() <= 0) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		List<ResponseGroup> rps = new ArrayList<ResponseGroup>();
		for (XueWenGroup group : groups) {
			if (group != null) {
				rps.add(new ResponseGroup(changGroupToResponseGroups(group,userId)));
			}
		}
		return rps;
	}
	/**
	 * 将群组转成前端需要的属性(PC)
	 * @param groups
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<ResponseGroup> changeGroupsToPcResponseGroup(List<XueWenGroup> groups, String userId)throws XueWenServiceException {
		List<ResponseGroup> rps = new ArrayList<ResponseGroup>();
		for (XueWenGroup group : groups) {
			if(group!=null){
				rps.add(changGroupToResponseGroup(group, userId));
			}
			
		}
		return rps;
	}
	/**
	 * 获得群组成员数量及当前身份
	 * @param group
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public ResponseGroup changGroupToResponseGroup(XueWenGroup group,String userId) throws XueWenServiceException {
		if (group == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		Map<String, Integer> summary = new HashMap<String, Integer>();
		summary.put("memberCount", group.getMember().size());
		summary.put("adminCount", group.getAdmin().size());
		group.setSummary(summary);
		if (null != group.getOwner() && group.getOwner().contains(userId)) {
			group.setIsMember("3"); // 创建者
		} else if (null != group.getAdmin()&& group.getAdmin().contains(userId)) {
			group.setIsMember("2"); // 管理员
		} else if (null != group.getMember()&& group.getMember().contains(userId)) {
			group.setIsMember("1"); // 成员
		} else {
			group.setIsMember("0"); // 未加入群
		}
		return new ResponseGroup(group);
	}
	/**
	 * 将小组转换成前端需要的参数形式
	 * @param group
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public XueWenGroup changGroupToResponseGroups(XueWenGroup group,String userId) throws XueWenServiceException {
		if (group == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		if(userId!=null){
			if (null != group.getOwner() && group.getOwner().contains(userId)) {
				group.setIsMember("3"); // 创建者
			} else if (null != group.getAdmin()
					&& group.getAdmin().contains(userId)) {
				group.setIsMember("2"); // 管理员
			} else if (null != group.getMember() && group.getMember().contains(userId)) {
				group.setIsMember("1"); // 成员
			} else {
				group.setIsMember("0"); // 未加入群
			}
		}
		else{
			group.setIsMember("0"); // 未加入群
		}
		return this.addResponseGroups(group);
	}
	/**
	 * 将group转换成ResponseGroup
	 * @param group
	 * @return
	 */
	public ResponseGroup changeGroupToResponseGroup(XueWenGroup group) {
		ResponseGroup rp = new ResponseGroup(group);
		return rp;

	}

	/**
	 * 查询群组列表，不支持分页功能
	 * 不包含自己参与的群组
	 * @param p
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<XueWenGroup> all(Sort sort, String userId)throws XueWenServiceException {
		List<String> myGrouplist = myGroupService.myGroupIds(userId);
		if (null == myGrouplist) {
			myGrouplist = new ArrayList<String>();
		}
		List<XueWenGroup> xue = groupRepo.findByIdNotIn(myGrouplist, sort);
		if (xue.size() <= 0) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		return xue;
	}

	/**
	 * 解散群（解散话题课程干货openfire信息）
	 * 
	 * @param id
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean deleteById(String id, String userId,String userNickName,String userLogoUrl)throws XueWenServiceException {
		logger.info("删除群ID================"+id);
		XueWenGroup group = groupRepo.findOneById(id);
		if (null != group) {
			List<Object> owner = group.getOwner();
			List<Object> member = group.getMember();
			if (owner.contains(userId)) {
				logger.info("该用户为创建者========="+userId);
				groupRepo.delete(group);
				myGroupService.removeAllMemberFromGroup(member, id);
				logger.info("清除群包括我的群===============");
				// 给所有群成员推送一条群解散的消息推送
				String notice = "群组创建者解散该群";
				member.remove(userId);
				Map<String, String> extras = new HashMap<String, String>();
				extras.put("type", "1003");
				// 生成所有用户的消息
				saveListUserMessage(member, notice, group, "1003");
				destroyOpenFireGroup(String.valueOf(group.getGroupNumber()));
				logger.info("清除openfire群===============");
				// 解散群后，话题要解散
				List<Topic> topics = topicService.getTopics(id);
				if(topics!=null && topics.size() > 0){
					for(int i=0; i <  topics.size(); i++){
						topicService.deleteById(topics.get(i).getTopicId());
					}
				}
				logger.info("清除话题===============");
				// 解散群的话，群组课程相关的数据全部删除
				newGroupCourseService.deleteGroupCourseList(id, userId);

				logger.info("清除课程推荐===============");
				// 解散群的话，群组课程相关的数据全部删除
				boxService.deleteBysourseId(id);
				
				logger.info("清除课程===============");
				// 解散群后删除群下的干货
				List<Drycargo> drycargos = drycargoService.getDryCargos(id);
				if(drycargos!=null && drycargos.size() > 0){
					for(int i=0;i < drycargos.size();i++){
						drycargoService.deleteById(drycargos.get(i).getId());
					}
				}
				logger.info("清除干货===============");
				//群组下分享删除
				List<GroupShareKnowledge> list=groupShareKnowledgeService.getByGroupId(group.getId());
				for (GroupShareKnowledge groupShareKnowledge : list) {
					groupShareKnowledgeService.del(groupShareKnowledge);
				}
				//删除该群下的广告
				List<String> ztiaoAd = ztiaoAdService.findAllZtiaoAdByGroup(id);
				if(ztiaoAd!=null && ztiaoAd.size() >0){
					for(int i=0;i < ztiaoAd.size();i++){
						ztiaoAdMongoTemplate.deleteByGroupId(ztiaoAd.get(i));
					}
				}
				try {
					//删除动态
					groupDynamicService.deleteByGroupId(id);
					//解散通知
					ztiaoPushService.sendDeleteGroups(userId, userNickName, userLogoUrl, 
							member, id, group.getGroupName(), group.getLogoUrl());
				} catch (Exception e) {
					logger.error("解散群组通知失败==============="+e);
				}
				return true;
			} else {
				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_DELETE_201, null);
			}
		} else {
			throw new XueWenServiceException(Config.STATUS_404, Config.MSG_404,null);
		}
	}
	/**
	 * openfire 摧毁房间
	 * @param groupNumber
	 */
	public void destroyOpenFireGroup(String groupNumber){
		Map<String,String> map=new HashMap<String, String>();
		map.put("groupNum", groupNumber);
		map.put("reason", "创建者解散群组");
		RestfulTemplateUtil restfulTemplateUtil=new RestfulTemplateUtil();
		JSONObject obj=restfulTemplateUtil.getRestApiData(openfireDestroyGroupUrl, map);
		Object status=obj.get("status");
//		ResponseContainer rc=(ResponseContainer)JSONObject.toBean(obj, ResponseContainer.class);
		if(!"200".equals(status.toString())){
			logger.error("======openFire 解散群组失败========");
		}
	}

	/**
	 * 加入群
	 * 
	 * @param id
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean join(String id, String userId, String passWord)throws XueWenServiceException {
		List<Object> listMember = new ArrayList<Object>();
		XueWenGroup group = groupRepo.findOneById(id);
		logger.info("用户==============="+userId +"申请加入群=============="+id);
		if (null != group) {
			listMember = group.getMember();
			if (!listMember.contains(userId)) {
			listMember.add(userId);
			group.setMember(listMember);
			group.setUtime(System.currentTimeMillis());
			groupRepo.save(group);
			logger.info("加入成功=====================");
			myGroupService.addMyGroup(userId, id);
			logger.info("加入我的群成功=====================");
			createGroupJoinDynamic(group.getId(),userId);
			}
			return true;
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_UPDATENOUSER_201, null);
		}
	}

	/**
	 * 退出群
	 * 
	 * @param id
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean quit(String id, String userId,String userNickName,String userLogoUrl) throws XueWenServiceException {XueWenGroup group = groupRepo.findOneById(id);
		if (null != group) {
			List<Object> owner = group.getOwner();// 获得创建者ID列表
			List<Object> admin = group.getAdmin();// 获得管理员ID列表
			List<Object> member = group.getMember();// 获得成员ID列表
			// 如果退出群的用户为创建者，则解散群
			if (owner.contains(userId)) {
				this.deleteById(id, userId,userNickName,userLogoUrl);
				logger.info("解散群============="+userId);
				return true;
			}
			// 如果退出群的用户为管理员，则需要将管理员列表及成员列表删除该用户
			if (admin.contains(userId)) {
				admin.remove(userId);
				member.remove(userId);
				group.setAdmin(admin);
				group.setMember(member);
				group.setUtime(System.currentTimeMillis());
				groupRepo.save(group);
				logger.info("将该用户清除该群============="+userId);
				myGroupService.removeMyGroup(userId, id);
				logger.info("从我的群中将该群清除============="+userId);
				return true;
			}
			if (member.contains(userId)) {
				member.remove(userId);
				group.setMember(member);
				group.setUtime(System.currentTimeMillis());
				groupRepo.save(group);
				logger.info("该用户为成员将该用户清除该群============="+userId);
				myGroupService.removeMyGroup(userId, id);
				logger.info("该用户为成员从我的群中将该群清除============="+userId);
				return true;
			}
			throw new XueWenServiceException(Config.STATUS_406, Config.MSG_406,null);
		
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_UPDATENOUSER_201, null);
		}

	}
	/**
	 * 
	 * @Title: kickMany
	 * @Description: 批量踢出
	 * @param id
	 * @param kickuserIds
	 * @param curryUser
	 * @return boolean
	 * @throws
	 */
	public boolean kickMany(String id, String kickuserIds, String curryUser,String curryUserNickName,String curryUserLogoUrl){
		try {
			if(StringUtil.isBlank(kickuserIds)){
				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_UPDATENOUSER_201, null);
			}
			String[] userIds=kickuserIds.split(",");
			for (String kickuserId : userIds) {
				this.kick(id, kickuserId, curryUser,curryUserNickName,curryUserLogoUrl);
			}
			return true;
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	/**
	 * 踢出群
	 * 
	 * @param id
	 * @param userId
	 * @param id2
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean kick(String id, String kickuserId, String curryUser,String curryUserNickName,String curryUserLogoUrl)throws XueWenServiceException {
		XueWenGroup group = groupRepo.findOneById(id);
		if (null != group) {
			List<Object> owner = group.getOwner();// 获得创建者ID列表
			List<Object> admin = group.getAdmin();// 获得管理员ID列表
			List<Object> member = group.getMember();// 获得成员ID列表
			// 如果当前用户为创建者
			if (owner.contains(curryUser)) {
				// 如果被踢不是创建者
				if (!owner.contains(kickuserId)) {
					// 如果被踢着是管理员
					if (admin.contains(kickuserId)) {
						admin.remove(kickuserId);
						member.remove(kickuserId);
						group.setMember(member);
						groupRepo.save(group);
						logger.info("将管理员提出该群============="+kickuserId);
						myGroupService.removeMyGroup(kickuserId, id);
						
						try {
							//将管理员移出群组发送
							ztiaoPushService.sendRemoveGroup(curryUser, curryUserNickName, curryUserLogoUrl,
									kickuserId, id, group.getGroupName(), group.getLogoUrl());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return true;
					}
					// 如果被踢者为成员
					else {
						member.remove(kickuserId);
						//确保memberCount数量同步
						group.setMember(member);
						groupRepo.save(group);
						logger.info("将成员提出该群============="+kickuserId);
						myGroupService.removeMyGroup(kickuserId, id);
						try {
							//将成员移出群组发送
							ztiaoPushService.sendRemoveGroup(curryUser, curryUserNickName, curryUserLogoUrl,
									kickuserId, id, group.getGroupName(), group.getLogoUrl());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return true;
					}
				}
				// 如果被踢人是创建者，则权限不够
				else {
					throw new XueWenServiceException(Config.STATUS_201,Config.MSG_DELETE_201, null);
				}
			}
			// 如果当前用户为管理员
			if (admin.contains(curryUser)) {
				// 如果被踢者为管理员或是创建者则权限不够
				if (admin.contains(kickuserId) || owner.contains(kickuserId)) {
					throw new XueWenServiceException(Config.STATUS_201,Config.MSG_DELETE_201, null);
				} else {
					member.remove(kickuserId);
					group.setMember(member);
					groupRepo.save(group);
					myGroupService.removeMyGroup(kickuserId, id);
					try {
						//将成员移出群组发送
						ztiaoPushService.sendRemoveGroup(curryUser, curryUserNickName, curryUserLogoUrl,
								kickuserId, id, group.getGroupName(), group.getLogoUrl());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return true;
				}
			}
			// 如果为普通用户，则权限不够
			if (member.contains(curryUser)) {
				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_DELETE_201, null);
			}
			return false;
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_UPDATENOUSER_201, null);
		}

	}

	/**
	 * 将成员提升为管理员
	 * 
	 * @param id
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean chang(String groupId, String userId, User curryUser,String type) throws XueWenServiceException {
		XueWenGroup group = groupRepo.findOneById(groupId);
		if (null != group) {
			List<Object> admin = group.getAdmin();// 获得管理员ID列表
			List<Object> member = group.getMember();// 获得成员ID列表
			List<Object> owner = group.getOwner();// 获得管理员列表
			// 如果该用户存在成员类别中
			if (member.contains(userId)) {
				// 如果type为0代表提升管理员
				if ("0".equals(type)) {
					// 如果该用户已经存为管理员，直接返回
					if (admin.contains(userId)) {
						return true;
					}
					// 只有当前用户为管理员才可以将用户提升为管理员
					if (owner.contains(curryUser.getId())) {
						admin.add(userId);
						group.setAdmin(admin);
						group.setUtime(System.currentTimeMillis());
						groupRepo.save(group);
						try {
							//提升管理员错误
							ztiaoPushService.sendSetUpAdmin(curryUser.getId(), curryUser.getNickName(), curryUser.getLogoURL(),
									userId, groupId, group.getGroupName(), group.getLogoUrl());
						} catch (Exception e) {
							logger.error("提升管理员通知错误:"+e);
						}
						return true;
					} else {
						throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NOACESS_201, null);
					}
				}
				// 如果type为1代表为降低管理员
				else {
					// 如果当前用户为不创建者则无权限
					if (!owner.contains(curryUser.getId())) {
						throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NOACESS_201, null);
					}
					//防止群创建者的管理权限被干掉
					if(owner.contains(userId)){
						throw new XueWenServiceException(Config.STATUS_201,"不可以删除创建者的管理权限", null);
					}
					if (admin.contains(userId)) {
						admin.remove(userId);
						group.setUtime(System.currentTimeMillis());
						groupRepo.save(group);
						try {
							//去掉管理员错误
							ztiaoPushService.sendCancelAdmin(curryUser.getId(), curryUser.getNickName(), curryUser.getLogoURL(),
									userId, groupId, group.getGroupName(), group.getLogoUrl());
						} catch (Exception e) {
							logger.error("去掉管理员通知错误:"+e);
						}
						return true;
					}
				}
			}
			throw new XueWenServiceException(Config.STATUS_404, Config.MSG_404,null);
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NODATA_201, null);
		}

	}

	/**
	 * 模糊查询，通过群组或者介绍关键字
	 * 
	 * @param groupName
	 * @param userId
	 * @param page
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<XueWenGroup> findAllByGroupNameRegexOrIntroRegexAndIdNotIn(String groupName, String userId, Point p, Distance d,Pageable pageable) throws XueWenServiceException {
		if (StringUtil.isBlank(groupName)) {
			return groupRepo.findAll(pageable);
		} else {
			groupName = ".*?(?i)" + groupName + ".*";
			return groupRepo.findByGroupNameRegex(groupName, pageable);
		}
	}

	/**
	 * 模糊查询，通过群组或者介绍关键字
	 * 
	 * @param groupName
	 * @param userId
	 * @param page
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<XueWenGroup> findAllByGroupNameRegexOrIntroRegex(String groupName, String info, Pageable pageable)throws XueWenServiceException {
		List<Object> myGrouplist = new ArrayList<Object>();
		if (StringUtil.isBlank(groupName) && StringUtil.isBlank(info)) {
			return groupRepo.findByIdNotIn(myGrouplist, pageable);
		} else {
			groupName = ".*?(?i)" + groupName + ".*";
			info = ".*?(?i)" + info + ".*";
			return groupRepo.findAllByGroupNameRegexOrIntroRegex(groupName,info, pageable);
		}
	}
	
	
	
	/**
	 * 模糊查询，通过群组或者介绍关键字
	 * 
	 * @param groupName
	 * @param userId
	 * @param page
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<XueWenGroup> findAllBychildCategoryId(String groupName)throws XueWenServiceException {
		 
			return groupRepo.findByChildCategoryId(groupName);
		
	}

	/**
	 * 通过用户ID查询自己的群组
	 * 
	 * @param userId
	 * @param page
	 * @return
	 */
	public List<XueWenGroup> findMyGroup(String userId) {
		List<XueWenGroup> xue = myGroupService.myGroups(userId);
		return xue;

	}

	/**
	 * 更新群组信息
	 * 
	 * @return
	 */
	public XueWenGroup updateGroup(XueWenGroup group, String id, User user,String tagName, String lat, String lng, String isGeoOpen)throws XueWenServiceException {
		// 判断当前用户是否有权限修改群组
		this.isPermission(user, id);
		logger.info("该用户为创建者或管理员===========");
		// 根据ID查找出数据库中得群组
		XueWenGroup oldGroup = groupRepo.findOneById(id);
		boolean isRabb=false;
		if (null == oldGroup) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_UPDATENOUSER_201, null);
		} else {
			if (null != group.getGroupName()) {
				if (!StringUtil.isEmpty(group.getGroupName().toString())) {
					try {
						group.setGroupName(URLDecoder.decode(group.getGroupName(), "UTF-8"));
						if(!group.getGroupName().equals(oldGroup.getGroupName())){
							//如果群组名称或者群组logo有修改，则发送队列消息，修改相关的
							isRabb=true;
						}
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
			if (null != group.getIntro()) {
				if (!StringUtil.isEmpty(group.getIntro())) {
					try {
						group.setIntro(URLDecoder.decode(group.getIntro(),"UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
			if (null != lat && null != lng) {
				double[] position = new double[] { Double.parseDouble(lng),Double.parseDouble(lat) };
				oldGroup.setLng(Double.parseDouble(lng));
				oldGroup.setLat(Double.parseDouble(lat));
				oldGroup.setPosition(position);
			}
			// 将修改的参数，复制到原群组对象
			if (group != null) {
				if(!oldGroup.getLogoUrl().equals(group.getLogoUrl())){
					//如果群组名称或者群组logo有修改，则发送队列消息，修改相关的
					isRabb=true;
				}
				objCopyPropsService.copyPropertiesInclude(group, oldGroup,new String[] { "tag", "passWord", "logoUrl", "intro","groupName", "industryClass", "isOpen","localName" ,"isGeoOpen","categoryId","childCategoryId"});
			}
			oldGroup.setUtime(System.currentTimeMillis());
			logger.info("群名称==========="+oldGroup.getGroupName());
			// 判断群组的isopen
			String isOpen = oldGroup.getIsOpen();
			if (isOpen == null || isOpen.equals("") || isOpen.equals("0")) {
				oldGroup.setIsOpen("0");
			} else if (isOpen.equals("2")) {
				if (oldGroup.getPassWord() == null) {
					throw new XueWenServiceException(Config.STATUS_201,Config.MSG_GROUPNOPWD_201, null);
				}
			} else if (!isOpen.equals("1")) {
				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_ISOPENERR_201, null);
			}
			if ("0".equals(isGeoOpen)) {
				oldGroup.setGeoOpen(false);
			} else {
				oldGroup.setGeoOpen(true);
			}
			logger.info("群图片==========="+oldGroup.getLogoUrl());
			if (!StringUtil.isBlank(tagName)) {
				tagName = JSON2ObjUtil.getArrayFromString(tagName);
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.postForObject(tagServiceUrl
						+ "tag/editTagsDelAdd?domain=" + Config.YXTDOMAIN + "&itemId="
						+ oldGroup.getId() + "&itemType=" + Config.TAG_TYPE_GROUP + "&tagNames="
						+ tagName + "&userId=" + user.getId() + "&userName="
						+ user.getNickName(), null, String.class);
			}
			logger.info("群号码==========="+oldGroup.getGroupNumber());
			oldGroup = groupRepo.save(oldGroup);
			logger.info("修改群成功===========");
		}
		try {
			rabbitmqService.sendRegexMessage(oldGroup.getId(), Config.TAG_TYPE_GROUP);
			if(isRabb){
				rabbitmqService.sendUpdateInfo(id, Config.UPDATE_TYPE_GROUP);
			}
		} catch (Exception e) {
			logger.error("===============修改小组发送过滤消息队列发送错误=============");
		}
		return oldGroup;
	}
	
	
	
	/**
	 * 更新群组信息
	 * 
	 * @return
	 */
	public XueWenGroup updateGroupByOss(XueWenGroup group, String id, User user,String tagName, String lat, String lng, String isGeoOpen)throws XueWenServiceException {
		// 判断当前用户是否有权限修改群组
		this.isPermission(user, id);
		logger.info("该用户为创建者或管理员===========");
		// 根据ID查找出数据库中得群组
		XueWenGroup oldGroup = groupRepo.findOneById(id);
		if (null == oldGroup) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_UPDATENOUSER_201, null);
		} else {
			if (null != group.getGroupName()) {
				if (!StringUtil.isEmpty(group.getGroupName().toString())) {
					try {
						group.setGroupName(URLDecoder.decode(group.getGroupName(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
			if (null != group.getIntro()) {
				if (!StringUtil.isEmpty(group.getIntro())) {
					try {
						group.setIntro(URLDecoder.decode(group.getIntro(),"UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
			if (null != lat && null != lng) {
				double[] position = new double[] { Double.parseDouble(lng),Double.parseDouble(lat) };
				oldGroup.setLng(Double.parseDouble(lng));
				oldGroup.setLat(Double.parseDouble(lat));
				oldGroup.setPosition(position);
			}
			// 将修改的参数，复制到原群组对象
			if (group != null) {
				objCopyPropsService.copyPropertiesInclude(group, oldGroup,new String[] { "tag", "passWord", "logoUrl", "intro","groupName", "industryClass", "isOpen","localName" ,"isGeoOpen","categoryId","childCategoryId"});
			}
			oldGroup.setUtime(System.currentTimeMillis());
			logger.info("群名称==========="+oldGroup.getGroupName());
			// 判断群组的isopen
			String isOpen = oldGroup.getIsOpen();
			if (isOpen == null || isOpen.equals("") || isOpen.equals("0")) {
				oldGroup.setIsOpen("0");
			} else if (isOpen.equals("2")) {
				if (oldGroup.getPassWord() == null) {
					throw new XueWenServiceException(Config.STATUS_201,Config.MSG_GROUPNOPWD_201, null);
				}
			} else if (!isOpen.equals("1")) {
				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_ISOPENERR_201, null);
			}
			if ("0".equals(isGeoOpen)) {
				oldGroup.setGeoOpen(false);
			} else {
				oldGroup.setGeoOpen(true);
			}
			logger.info("群图片==========="+oldGroup.getLogoUrl());
			if (!StringUtil.isBlank(tagName)) {
				//tagName = JSON2ObjUtil.getArrayFromString(tagName);
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.postForObject(tagServiceUrl
						+ "tag/editTagsDelAdd?domain=" + Config.YXTDOMAIN + "&itemId="
						+ oldGroup.getId() + "&itemType=" + Config.TAG_TYPE_GROUP + "&tagNames="
						+ tagName + "&userId=" + user.getId() + "&userName="
						+ user.getNickName(), null, String.class);
			}
			logger.info("群号码==========="+oldGroup.getGroupNumber());
			oldGroup = groupRepo.save(oldGroup);
			logger.info("修改群成功===========");
		}
		return oldGroup;
	}

	/**
	 * 更新群组的分类
	 * 
	 * @return
	 */
	public XueWenGroup insertIndustryClass(XueWenGroup group, String industryId)
			throws XueWenServiceException {
		Industryclass ic = industryService.findIndustryClass(industryId);
		group.setIndustryClass(ic);
		return group;
	}

	/**
	 * 更新群组的分类
	 * 
	 * @return
	 */
	public XueWenGroup findRoleGroup(String id) throws XueWenServiceException {
		XueWenGroup one = groupRepo.findOneById(id);
		if (one == null) {
			throw new XueWenServiceException(Config.STATUS_201,
					Config.MSG_UPDATENOUSER_201, null);
		}
		return one;
	}

	/**
	 * 生成群组二维码
	 * 
	 * @param group
	 * @return
	 * @throws XueWenServiceException
	 */
	public String createGroupQRCode(XueWenGroup group)
			throws XueWenServiceException {
		if (group == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		// 二维码路径后缀
		String suffix = myFileService.idSpilt(String.valueOf(group.getId()), 4);
		String qrCodeDir = groupQrCodeLocal + suffix;
		String qrCodeName = System.currentTimeMillis() + ".jpg";
		String context = appDownLoadUrl + "?type=1000&groupId=" + group.getId();
		qRCodeService.creadQRCode(context, qrCodeDir, qrCodeName,Config.QRCODE_HIGH, Config.QRCODE_WEIGHT);
		return groupQrCodeUrl + suffix + "/" + qrCodeName;
	}

	/**
	 * 返回GROUP所带的USER对象不带相应的关键信息
	 * 
	 * @return
	 */
	public XueWenGroup addResponseGroup(XueWenGroup group)throws XueWenServiceException {
		try {
			if (null != group) {
				List<Object> ownerList = group.getOwner();
				List<Object> adminList = group.getAdmin();
				List<Object> memberList = group.getMember();
				Map<String, Integer> summary = new HashMap<String, Integer>();
				if (null != ownerList) {
					List<User> users = userRepo.findByIdIn(ownerList);
					List<ResponseUser> rss = new ResponseUser().toResponseUser(users);
					List<Object> owners = new ArrayList<Object>();
					owners.addAll(rss);
					group.setOwner(owners);
				}
				if (null != adminList) {
					List<User> users = userRepo.findByIdIn(adminList);
					List<ResponseUser> rss = new ResponseUser().toResponseUser(users);
					List<Object> admins = new ArrayList<Object>();
					admins.addAll(rss);
					group.setAdmin(admins);
					summary.put("adminCount", group.getAdmin().size());
				}
				if (null != memberList) {
					List<User> users = userRepo.findByIdIn(memberList);
					List<ResponseUser> rss = new ResponseUser().toResponseUser(users);
					List<Object> members = new ArrayList<Object>();
					members.addAll(rss);
					group.setMember(members);
					summary.put("memberCount", group.getMember().size());
				}
				group.setSummary(summary);
			} else {
				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_201, null);
			}
		} catch (Exception e) {
			logger.error("返回前端需求的group对象错误：" + e);
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		return group;
	}
	/**
	 * 获得群组相关属性值
	 * @param group
	 * @return
	 * @throws XueWenServiceException
	 */
	public XueWenGroup addResponseGroups(XueWenGroup group)throws XueWenServiceException {
		Map<String, Integer> summary = new HashMap<String, Integer>();
		try {
			List<Object> owners = new ArrayList<Object>();
			if (null != group) {
				List<Object> ownerList = group.getOwner();
				List<Object> adminList = group.getAdmin();
				List<Object> memberList = group.getMember();
				if (null != ownerList) {
					List<User> users = userRepo.findByIdIn(ownerList);
					List<ResponseUser> rss = new ResponseUser().toResponseUser(users);
					owners.addAll(rss);
					group.setOwner(owners);
				}
				List<Object> admins = new ArrayList<Object>();
				if (null != adminList) {
					adminList.removeAll(ownerList);
					if (null != adminList) {
						List<User> users = userRepo.findByIdIn(adminList);
						List<ResponseUser> rss = new ResponseUser().toResponseUser(users);
						admins.addAll(rss);
						group.setAdmin(admins);
					}
				}
				List<Object> members = new ArrayList<Object>();
				if (null != memberList) {
					memberList.removeAll(ownerList);
					memberList.removeAll(adminList);
					if (null != memberList) {
						List<User> users = userRepo.findByIdIn(memberList);
						List<ResponseUser> rss = new ResponseUser().toResponseUser(users);
						members.addAll(rss);
						group.setMember(members);
					}
				}
				summary.put("memberCount", members.size());//成员数量
				summary.put("adminCount", admins.size());//管理员数量
				summary.put("ownerCount", owners.size());//创建人管理
				int subjectCount = topicService.getTopicCount(group.getId());
				summary.put("subjectCount", subjectCount);//话题数量
				int courseCount = newGroupCourseService.getCourseCountByGroup(group.getId());
				summary.put("courseCount", courseCount);//课程数量
				int dryCount = drycargoService.getDryCountByGroup(group.getId(),0);
				summary.put("dryCount", dryCount);//干货数量
				//int xuanYeCount = drycargoService.getDryCountByGroup(group.getId(),1);
				//summary.put("xuanYeCount", xuanYeCount);//炫页数量
				// 增加标签查询
				RestTemplate restTemplate = new RestTemplate();
				String tag = restTemplate.getForObject(tagServiceUrl
						+ "tag/getTagsByIdAndType?domain=" + Config.YXTDOMAIN
						+ "&itemId=" + group.getId() + "&itemType=" + Config.TAG_TYPE_GROUP,
						String.class);
				JSONObject objj = JSONObject.fromObject(tag);
				String fatherName = objj.getString("data");
				JSONObject ss = JSONObject.fromObject(fatherName);
				net.sf.json.JSONArray childs = ss.getJSONArray("result");
				logger.info("查询群组信息标签=============="+childs);
				group.setGroupTagName(childs);
				logger.info("查询群组信息标签数量=============="+childs.size());
				group.setScoreSum(childs.size());
				group.setSummary(summary);
				//获得一级分类对象
//				if(group.getCategoryId()!=null){
//					group.setCategoryId(categoryService.formateCategory(categoryService.findOneCategoryById(group.getCategoryId().toString())));
//				}
				//if(group.getCategoryId()!=null){
				group.setCategoryId(null);
				//}
				//获得二级分类对象
				if(group.getChildCategoryId()!=null){
					group.setChildCategoryId(categoryService.formateCategory(categoryService.findOneCategoryById(group.getChildCategoryId().toString())));
				}
			} else {
				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_201, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		return group;
	}

	/**
	 * 
	 * @Title: findPcGroupMmbers
	 * @Description: 查询组用户list
	 * @param id
	 * @param userName
	 * @return
	 * @throws XueWenServiceException
	 *             List<ResponsePcUser>
	 * @throws
	 */
	public List<ResponsePcUser> findPcGroupMmbers(String id, String userName)throws XueWenServiceException {
		if (org.apache.commons.lang.StringUtils.isBlank(id) || userName == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		List<ResponsePcUser> pcUsers = new ArrayList<ResponsePcUser>();
		XueWenGroup group = groupRepo.findOneById(id);
		if (null != group) {
			List<Object> ownerList = group.getOwner();
			List<Object> adminList = group.getAdmin();
			List<Object> memberList = group.getMember();
			if (null != memberList) {
				List<User> users = userRepo.findByIdInAndUserNameLike(memberList, userName);
				if (users != null) {
					for (User user : users) {
						ResponsePcUser pcUser;
						if (ownerList != null&& ownerList.contains(user.getId())) {
							pcUser = toResponsePcUser(user, "0");
						} else if (adminList != null&& adminList.contains(user.getId())) {
							pcUser = toResponsePcUser(user, "1");
						} else {
							pcUser = toResponsePcUser(user, "2");
						}
						pcUsers.add(pcUser);
					}
				}
			}
		}
		return pcUsers;
	}
	
	/**
	 * 
	 * @Title: findPcGroupMmbers
	 * @Description: 查询组用户list
	 * @param id
	 * @param userName
	 * @return
	 * @throws XueWenServiceException
	 *             List<ResponsePcUser>
	 * @throws
	 */
	public List<ResponsePcUser> findPcGroupMmbersOnlyAdmin(String id)throws XueWenServiceException {
		if (org.apache.commons.lang.StringUtils.isBlank(id)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		List<ResponsePcUser> pcUsers = new ArrayList<ResponsePcUser>();
		XueWenGroup group = groupRepo.findOneById(id);
		if (null != group) {
			List<Object> ownerList = group.getOwner();
			List<Object> adminList = group.getAdmin();
			if (null != adminList) {
				List<User> users = userRepo.findByIdIn(adminList);
				if (users != null) {
					for (User user : users) {
						ResponsePcUser pcUser;
						if (ownerList != null&& ownerList.contains(user.getId())) {
							pcUser = toResponsePcUser(user, "0");
							pcUsers.add(0,pcUser);
						} else if (adminList != null&& adminList.contains(user.getId())) {
							pcUser = toResponsePcUser(user, "1");
							pcUsers.add(pcUser);
						} 
						
					}
				}
			}
		}
		return pcUsers;
	}
	/**
	 * 查询群组成员（PC）
	 * @param id
	 * @param userName
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Map<String, Object> findPcGroupMmbersNotSort(String id, String userName, Pageable pageable)throws XueWenServiceException {
		if (org.apache.commons.lang.StringUtils.isBlank(id)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
		}
		if (userName == null) {
			userName = "";
		}
		List<ResponsePcUser> pcUsers = new ArrayList<ResponsePcUser>();
		XueWenGroup group = groupRepo.findOneById(id);
		if (group == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
		}
		if (null != group) {
			List<Object> ownerList = group.getOwner();
			List<Object> adminList = group.getAdmin();
			List<Object> memberList = group.getMember();
			if (null != memberList) {
				Page<User> users = userRepo.findByIdIn(memberList,pageable);
				if (users.getContent() != null) {
					for (User user : users.getContent()) {
						ResponsePcUser pcUser;
						if (ownerList != null && ownerList.contains(user.getId())) {
							pcUser = toResponsePcUser(user, "0");
						} else if (adminList != null && adminList.contains(user.getId())) {
							pcUser = toResponsePcUser(user, "1");
						} else {
							pcUser = toResponsePcUser(user, "2");
						}
						pcUsers.add(pcUser);
					}
				}
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("page", users);
				map.put("result", pcUsers);
				return map;
			}
		}
		return null;
	}
	
	/**
	 * 查询群组成员（PC）
	 * @param id
	 * @param userName
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Map<String, Object> findPcGroupApplyNotSort(String id, String userName, Pageable pageable)throws XueWenServiceException {
		if (org.apache.commons.lang.StringUtils.isBlank(id)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
		}
		List<ResponsePcUser> pcUsers = new ArrayList<ResponsePcUser>();
		XueWenGroup group = groupRepo.findOneById(id);
		if (group == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
		}
		if (null != group) {
			List<Object> applyList = group.getApplyer();
			if(applyList!=null){
				if(applyList.size()!=0){
					Page<User> users = userRepo.findByIdIn(applyList,pageable);
					if (users.getContent() != null) {
						for (User user : users.getContent()) {
							ResponsePcUser pcUser;
								pcUser = toResponsePcUser(user, "2");
							pcUsers.add(pcUser);
						}
					}
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("page", users);
					map.put("result", pcUsers);
					return map;
				}
				
			}
		}
		return null;
	}

	
	/**
	 * 
	 * @Title: findPcGroupMmbers
	 * @Description: 查询组用户list
	 * @param id
	 * @param userName
	 * @return
	 * @throws XueWenServiceException
	 *             List<ResponsePcUser>
	 * @throws
	 */
	public Map<String, List<User>> findPcGroupMmbers(String id) throws XueWenServiceException {
		Map<String, List<User>> map = new HashMap<String, List<User>>();
		if (org.apache.commons.lang.StringUtils.isBlank(id)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		XueWenGroup group = groupRepo.findOneById(id);
		if (null != group) {
			List<Object> ownerList = group.getOwner();
			List<Object> adminList = group.getAdmin();
			List<Object> memberList = group.getMember();
			if (null != memberList) {
				List<User> memberListusers = userRepo.findByIdIn(memberList);
				map.put("memberListusers", memberListusers);
			}
			if (null != ownerList) {
				List<User> ownerListusers = userRepo.findByIdIn(ownerList);
				map.put("ownerListusers", ownerListusers);
			}
			if (null != adminList) {
				List<User> adminListusers = userRepo.findByIdIn(adminList);
				map.put("adminListusers", adminListusers);
			}

		}
		return map;
	}

	/**
	 * 
	 * @Title: toResponsePcUser
	 * @Description: 减肥
	 * @param users
	 * @return List<ResponsePcUser>
	 * @throws
	 */
	public ResponsePcUser toResponsePcUser(User user, String type) {
		ResponsePcUser pcUser = new ResponsePcUser();
		pcUser.setId(user.getId());
		pcUser.setLogoURL(user.getLogoURL());
		pcUser.setNickName(user.getNickName());
		pcUser.setType(type);
		pcUser.setUserName(user.getUserName());
		pcUser.setIntro(user.getIntro());
		return pcUser;
	}

	/**
	 * 返回GROUP所带的USER对象不带相应的关键信息，而且三个权限群组不重复
	 * 
	 * @return
	 */
	public XueWenGroup rspGroupNoUsersRepeat(XueWenGroup group, String userId)throws XueWenServiceException {
		try {
			if (null != group) {
				List<Object> ownerList = group.getOwner();
				List<Object> adminList = group.getAdmin();
				List<Object> memberList = group.getMember();
				Map<String, Integer> summary = new HashMap<String, Integer>();
				if (null != ownerList) {
					List<User> users = userRepo.findByIdIn(ownerList);
					List<ResponseUser> rss = this.toResponseUser(users, userId);
					List<Object> owners = new ArrayList<Object>();
					owners.addAll(rss);
					group.setOwner(owners);
				}
				if (null != adminList) {
					adminList.removeAll(ownerList);
					if (null != adminList) {
						List<User> users = userRepo.findByIdIn(adminList);
						List<ResponseUser> rss = this.toResponseUser(users,userId);
						List<Object> admins = new ArrayList<Object>();
						admins.addAll(rss);
						group.setAdmin(admins);
						summary.put("adminCount", group.getAdmin().size());
					}
				}
				if (null != memberList) {
					memberList.removeAll(ownerList);
					memberList.removeAll(adminList);
					if (null != memberList) {
						List<User> users = userRepo.findByIdIn(memberList);
						List<ResponseUser> rss = this.toResponseUser(users,userId);
						List<Object> members = new ArrayList<Object>();
						members.addAll(rss);
						group.setMember(members);
						summary.put("memberCount", group.getMember().size());
					}
				}
				group.setSummary(summary);
			} else {
				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_201, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		return group;
	}

	/**
	 * 用户申请加入群组
	 * 
	 * @return
	 */
//	public void applyToGroup(String groupId, String password, User user,Map<String, String> extras, String future)throws XueWenServiceException {
//		XueWenGroup group = groupRepo.findOneById(groupId);
//		String isOpen = "";
//		if (null != group) {
//			isOpen = group.getIsOpen();
//			if (group.getMember().contains(user.getId())) {
//				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_ISMEMBER_201, null);
//			}
//			if ((Config.ISOPEN_ADMIN_AGREE).equals(isOpen)) {
//				// 如果为1，需要管理员或者创建者同意
//				String notice = user.getNickName() + "申请加入"+ group.getGroupName() + "群组";
//				// 通知管理员入群信息
//				List<String> alias = new ArrayList<String>();
//				if (group.getAdmin() != null && group.getAdmin().size() > 0) {
//					String messageGroupId = MD5Util.MD5(String.valueOf(System.currentTimeMillis() + groupId + user.getId()));
//					MessageContext context = new MessageContext();
//					context.setUserId(user.getId());
//					context.setNikeName(user.getNickName());
//					context.setLogoURL(user.getLogoURL());
//					context.setGroupId(groupId);
//					context.setGroupName(group.getGroupName());
//					context.setContext(notice);
//					for (Object admin : group.getAdmin()) {
//						alias.add(String.valueOf(admin));
//						logger.info("================alias:"+ String.valueOf(admin));
//						UserMessage userMessage = new UserMessage();
//						userMessage.setUserId(String.valueOf(admin));
//						userMessage.setMessageGroupId(messageGroupId);
//						userMessage.setContext(context);
//						userMessage.setIsOpertison("0");
//						userMessage.setIsRead("0"); // 是否可读 0 未读 1 已读
//						userMessage.setStime(System.currentTimeMillis());
//						userMessage.setType("1000"); // 推送消息类型
//						userMessage.setFuture(future);
//						saveUserMessage(userMessage);
//					}
//					context.setContext(notice);
//				}
//				throw new XueWenServiceException(Config.STATUS_204,Config.MSG_204, null);
//			} else if ((Config.ISOPEN_PASSWORD).equals(isOpen)) {
//				// 如果为2，则需要密码验证
//				if (!StringUtil.isBlank(password)&& password.equals(group.getPassWord())) {
//					// 调用join方法
//					join(group, user.getId());
//				} else {
//					throw new XueWenServiceException(Config.STATUS_201,Config.MSG_JOINFALSEPWD_201, null);
//				}
//			} else {
//				// 其他情况默认加入群组
//				// 调用join方法
//				join(group, user.getId());
//				 
//			}
//		} else {
//			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NODATA_201, null);
//		}
//	}
	public void applyToGroup(String groupId, User user,String reason)throws XueWenServiceException {
		XueWenGroup group = groupRepo.findOneById(groupId);
		String isOpen = "";
		if (null != group) {
			isOpen = group.getIsOpen();
			if (group.getMember().contains(user.getId())) {
				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_ISMEMBER_201, null);
			}
			if ((Config.ISOPEN_ADMIN_AGREE).equals(isOpen)) {
				// 如果为1，需要管理员或者创建者同意
				if (group.getAdmin() != null && group.getAdmin().size() > 0) {
					//用户申请群组唯一ID
					String groupApplyId = MD5Util.MD5(String.valueOf(groupId + user.getId()));
					//给所有管理员推送消息
					try {
						//添加到申请列表
						List<Object> applyList=new ArrayList<Object>();
						if(group.getApplyer()!=null){
							applyList=group.getApplyer();
							if(!applyList.contains(user.getId())){
								applyList.add(user.getId());
							}else{
								throw new XueWenServiceException(Config.STATUS_204,"已经申请过了，无需重复申请", null);
							}
						}else{
							applyList.add(user.getId());
						}
						group.setApplyer(applyList);
						savegroup(group);
						this.sendApplyToGroup(user.getId(), user.getNickName(), user.getLogoURL(), group.getId(), group.getGroupName(), group.getLogoUrl(),
								group.getOpenFireGroup(), group.getAdmin(), reason, groupApplyId);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				throw new XueWenServiceException(Config.STATUS_204,Config.MSG_204, null);
			}  else {
				// 其他情况默认加入群组
				// 调用join方法
				join(group, user.getId());
				
			}
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NODATA_201, null);
		}
	}
	
	/**
	 * 发送入群申请推送给管理员
	 * @param fromUserId
	 * @param fromUserNickName
	 * @param fromUserLogoUrl
	 * @param groupId
	 * @param groupName
	 * @param groupLogoUrl
	 * @param openFireGroup
	 * @param admins
	 * @param reason
	 * @param applyGroupId
	 * @throws Exception
	 */
	public void sendApplyToGroup(String fromUserId,String fromUserNickName,String fromUserLogoUrl,
			String groupId,String groupName,String groupLogoUrl,OpenFireGroup openFireGroup,List<Object> admins,
			String reason,String applyGroupId)throws Exception{
		for(Object obj:admins){
//			String adminNickName=userService.getUserNickNameByUserId(obj.toString()); //此处不提供管理员的nickName也可以
			ZtiaoPush zp=new ZtiaoPush(Config.PUSH_APPTYTOGROUP_TYPE,fromUserId,fromUserNickName,fromUserLogoUrl,
					obj.toString(),groupId,groupName,groupLogoUrl,openFireGroup,reason,obj.toString(),"",applyGroupId);
			rabbitmqService.sendPushMessage(zp);
		}
	}
	
	


	/**
	 * 加入群
	 * 
	 * @param id
	 * @return
	 * @throws XueWenServiceException
	 */
	public void join(XueWenGroup group, String userId)throws XueWenServiceException {
		List<Object> listMember = new ArrayList<Object>();
		if (null != group) {
			listMember = group.getMember();
			if (listMember.contains(userId)) {
				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_USERISLIVE_201, null);
			}
			listMember.add(userId);
			group.setMember(listMember);
			group.setUtime(System.currentTimeMillis());
			myGroupService.addMyGroup(userId, group.getId());
			group.setTemperature(this.getMemberCount(group));
			groupRepo.save(group);
			createGroupJoinDynamic(group.getId(),userId);
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_UPDATENOUSER_201, null);
		}
	}
	/**
	 * 计算群组热度
	 * @param mg
	 * @return
	 */
	public int getMemberCount(XueWenGroup mg) {
		List<Object> groups = mg.getMember();
		if (null == groups) {
			return 0;
		} else {
			if (groups.size() > 0) {
				if (groups.size() >= 10) {
					return 3;
				}
				if (groups.size() >= 5 && groups.size() < 10) {
					return 2;
				}
				return 1;
			}
		}
		return 1;
	}

	/**
	 * 同意入群
	 * @param groupId（群ID）
	 * @param messageGroupId（消息ID）
	 * @param userId（申请人）
	 * @param user（当前管理里员）
	 * @param extra
	 * @param future
	 * @throws XueWenServiceException
	 */
//	public void agree(String groupId, String messageGroupId, String userId,User user, Map<String, String> extra, String future)throws XueWenServiceException {
//		XueWenGroup group = groupRepo.findOneById(groupId);
//		if (null != group) {
//			List<Object> admin = group.getAdmin();
//			if (admin.size() > 0 && admin.contains(user.getId()) && group.getMember().contains(userId)) {
//				updateUserMessages(messageGroupId, "1");
//			} else if (admin.size() > 0 && admin.contains(user.getId()) && !group.getMember().contains(userId)) {
//				// 如此用户为管理员则调用join方法，将申请者加入群
//				join(group, userId);
//				String notice = "您已经加入" + group.getGroupName() + "群组";
//				extra.put("context", notice);
//				List<String> alias = new ArrayList<String>();
//				alias.add(userId);
//				UserMessage userMessage = new UserMessage();
//				userMessage.setUserId(userId);
//				MessageContext context = new MessageContext();
//				context.setGroupId(groupId);
//				context.setGroupName(group.getGroupName());
//				context.setContext(notice);
//				userMessage.setContext(context);
//				userMessage.setIsRead("0"); // 是否可读 0 未读 1 已读
//				userMessage.setStime(System.currentTimeMillis());
//				userMessage.setType("1001"); // 推送消息类型
//				userMessage.setIsOpertison("0");
//				userMessage.setFuture(future);
//				saveUserMessage(userMessage);
//				updateUserMessages(messageGroupId, "1");
//				//xmpp进行推送
//				try {
//					//解散通知
//					ztiaoPushService.sendDeleteGroups(userId, userNickName, userLogoUrl, 
//							admin, id, group.getGroupName(), group.getLogoUrl());
//				} catch (Exception e) {
//					logger.error("解散群组通知失败==============="+e);
//				}
//				
//			} else {
//				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_CANNOTAGREE_201, null);
//			}
//		} else {
//			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_UPDATENOUSER_201, null);
//		}
//	}
	
	/**
	 * 同意入群
	 * @param groupId（群ID）
	 * @param messageGroupId（消息ID）
	 * @param userId（申请人）
	 * @param user（当前管理里员）
	 * @param extra
	 * @param future
	 * @throws XueWenServiceException
	 */
	public void agree(String groupId, String messageGroupId, String userId,User user)throws XueWenServiceException {
		XueWenGroup group = groupRepo.findOneById(groupId);
		User applyUser = userService.findOne(userId);
		if (null != group) {
			List<Object> admin = group.getAdmin();
			if (admin.size() > 0 && admin.contains(user.getId()) && group.getMember().contains(userId)) {
				//updateUserMessages(messageGroupId, "1");
			} else if (admin.size() > 0 && admin.contains(user.getId()) && !group.getMember().contains(userId)) {
				// 如此用户为管理员则调用join方法，将申请者加入群
				join(group, userId);
				//xmpp进行推送
				try {
					//删除申请的记录
					List<Object> applyList=group.getApplyer();
					applyList.remove(userId);
					group.setApplyer(applyList);
					savegroup(group);
					this.sendAgreeToGroupToApply(user.getId(), user.getNickName(), user.getLogoURL(), userId,
							groupId, group.getGroupName(), group.getLogoUrl(), group.getOpenFireGroup(), messageGroupId);
					this.sendAgreeToGroup(applyUser.getId(), applyUser.getNickName(), applyUser.getLogoURL(), 
							group.getId(), group.getGroupName(), group.getLogoUrl(), group.getOpenFireGroup(), 
							group.getAdmin(), messageGroupId, user.getId(), user.getNickName());
				} catch (Exception e) {
					logger.error("解散群组通知失败==============="+e);
				}
				
			} else {
				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_CANNOTAGREE_201, null);
			}
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_UPDATENOUSER_201, null);
		}
	}
	
	/**
	 * 管理员同意加入该群,推送给管理员
	 * @param fromUserId
	 * @param fromUserNickName
	 * @param fromUserLogoUrl
	 * @param groupId
	 * @param groupName
	 * @param groupLogoUrl
	 * @param openFireGroup
	 * @param admins
	 * @param reason
	 * @param applyGroupId
	 * @throws Exception
	 */
	public void sendAgreeToGroup(String fromUserId,String fromUserNickName,String fromUserLogoUrl,
			String groupId,String groupName,String groupLogoUrl,OpenFireGroup openFireGroup,List<Object> admins,
			String applyGroupId,String adminId,String adminNickName)throws Exception{
		for(Object obj:admins){
//			String adminNickName=userService.getUserNickNameByUserId(obj.toString()); //此处不提供管理员的nickName也可以
			if(!obj.equals(adminId)){
			ZtiaoPush zp=new ZtiaoPush(Config.PUSH_AGREEINGROUP_TYPE,fromUserId,fromUserNickName,fromUserLogoUrl,
					obj.toString(),groupId,groupName,groupLogoUrl,openFireGroup,"",adminId,adminNickName,applyGroupId);
			rabbitmqService.sendPushMessage(zp);
		}
		}
	}
	/**
	 * 管理员同意入群，推送给申请人
	 * @param fromUserId
	 * @param fromUserNickName
	 * @param fromUserLogoUrl
	 * @param groupId
	 * @param groupName
	 * @param groupLogoUrl
	 * @param openFireGroup
	 * @param admins
	 * @param applyGroupId
	 * @param adminId
	 * @param adminNickName
	 * @throws Exception
	 */
	public void sendAgreeToGroupToApply(String fromUserId,String fromUserNickName,String fromUserLogoUrl,String applyUserId,
			String groupId,String groupName,String groupLogoUrl,OpenFireGroup openFireGroup,
			String applyGroupId)throws Exception{
				ZtiaoPush zp=new ZtiaoPush(Config.PUSH_AGREEINGROUPTOAPPLY_TYPE,fromUserId,fromUserNickName,fromUserLogoUrl,
						applyUserId,groupId,groupName,groupLogoUrl,openFireGroup,"",fromUserId,fromUserNickName,applyGroupId);
				rabbitmqService.sendPushMessage(zp);
	}
	

	/**
	 * 不同意加入群
	 * 
	 * @param id
	 * @return
	 * @throws XueWenServiceException
	 */
//	public void unagree(String groupId, String messageGroupId, String userId,User user, Map<String, String> extra, String future)throws XueWenServiceException {
//		XueWenGroup group = groupRepo.findOneById(groupId);
//		if (null != group) {
//			List<Object> admin = group.getAdmin();
//			if (StringUtil.isBlank(messageGroupId)) {
//				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_201, null);
//			}
//			List<UserMessage> userMessages = userService.findUserMessagesByMessageGroupId(messageGroupId);
//			if (admin.size() > 0 && admin.contains(user.getId())&& group.getMember().contains(userId)) {
//				for (UserMessage um : userMessages) {
//					um.setIsRead("1");
//					saveUserMessage(um);
//				}
//			} else if (admin.size() > 0 && admin.contains(user.getId())&& !group.getMember().contains(userId)) {
//				// 管理员拒绝用户入群，如搜索得知所有管理员都不同意则通知用户，并用户加入消息队列
//				int i = 1;
//				for (UserMessage um : userMessages) {
//					if (!um.getUserId().equals(user.getId()) && um.getIsOpertison().equals("2")) {
//						i = i + 1;
//					} else if (um.getUserId().equals(user.getId())) {
//						um.setIsOpertison("2");
//						um.setIsRead("1");
//						saveUserMessage(um);
//					}
//				}
//				if (i >= userMessages.size()) {
//					// 所有管理员都拒绝申请者入群
//					String notice = "群组管理员拒绝您加入" + group.getGroupName() + "群组";
//					List<String> alias = new ArrayList<String>();
//					alias.add(userId);
//					UserMessage userMessage = new UserMessage();
//					userMessage.setIsRead("0"); // 是否可读 0 未读 1 已读
//					userMessage.setIsOpertison("0");
//					userMessage.setUserId(userId);
//					userMessage.setFuture(future);
//					MessageContext context = new MessageContext();
//					context.setContext(notice);
//					context.setGroupId(groupId);
//					context.setGroupName(group.getGroupName());
//					userMessage.setContext(context);
//					userMessage.setStime(System.currentTimeMillis());
//					userMessage.setType("1002"); // 推送消息类型
//					saveUserMessage(userMessage);
//				}
//			} else {
//				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NOSECNOA_201, null);
//			}
//
//		} else {
//			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_UPDATENOUSER_201, null);
//		}
//	}
	
	/**
	 * 不同意加入群
	 * 
	 * @param id
	 * @return
	 * @throws XueWenServiceException
	 */
	public void unagree(String groupId, String messageGroupId, String userId,User user)throws XueWenServiceException {
		XueWenGroup group = groupRepo.findOneById(groupId);
		User applyUser = userService.findOne(userId);
		if (null != group) {
			List<Object> admin = group.getAdmin();
			if (StringUtil.isBlank(messageGroupId)) {
				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_201, null);
			}
			try {
				//删除申请记录
				List<Object> applyList=group.getApplyer();
				applyList.remove(userId);
				group.setApplyer(applyList);
				savegroup(group);
				this.sendUnAgreeToGroup(applyUser.getId(), applyUser.getNickName(), applyUser.getLogoURL(), 
						group.getId(), group.getGroupName(), group.getLogoUrl(), group.getOpenFireGroup(), 
						admin, messageGroupId, user.getId(), user.getNickName());
				this.sendUnAgreeToGroupToApply(user.getId(),user.getNickName(),user.getLogoURL(),userId,
						groupId,group.getGroupName(),group.getLogoUrl(),group.getOpenFireGroup(),
						messageGroupId);
			} catch (Exception e) {
				logger.error("解散群组通知失败==============="+e);
			}
			} 

		 else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_UPDATENOUSER_201, null);
		}
	}
	/**
	 * 管理员不同意加入该群
	 * @param fromUserId
	 * @param fromUserNickName
	 * @param fromUserLogoUrl
	 * @param groupId
	 * @param groupName
	 * @param groupLogoUrl
	 * @param openFireGroup
	 * @param admins
	 * @param reason
	 * @param applyGroupId
	 * @throws Exception
	 */
	public void sendUnAgreeToGroup(String fromUserId,String fromUserNickName,String fromUserLogoUrl,
			String groupId,String groupName,String groupLogoUrl,OpenFireGroup openFireGroup,List<Object> admins,
			String applyGroupId,String adminId,String adminNickName)throws Exception{
		for(Object obj:admins){
//			String adminNickName=userService.getUserNickNameByUserId(obj.toString()); //此处不提供管理员的nickName也可以
			if(!obj.equals(adminId)){
			ZtiaoPush zp=new ZtiaoPush(Config.PUSH_UNAGREEINGROUP_TYPE,fromUserId,fromUserNickName,fromUserLogoUrl,
					obj.toString(),groupId,groupName,groupLogoUrl,openFireGroup,"",adminId,adminNickName,applyGroupId);
			rabbitmqService.sendPushMessage(zp);
		}
		}
	}
	
	/**
	 * 发送通知给申请人
	 * @param fromUserId
	 * @param fromUserNickName
	 * @param fromUserLogoUrl
	 * @param applyUserId
	 * @param groupId
	 * @param groupName
	 * @param groupLogoUrl
	 * @param openFireGroup
	 * @param applyGroupId
	 * @throws Exception
	 */
	public void sendUnAgreeToGroupToApply(String fromUserId,String fromUserNickName,String fromUserLogoUrl,String applyUserId,
			String groupId,String groupName,String groupLogoUrl,OpenFireGroup openFireGroup,
			String applyGroupId)throws Exception{
			ZtiaoPush zp=new ZtiaoPush(Config.PUSH_UNAGREEINGROUPTOAPPLY_TYPE,fromUserId,fromUserNickName,fromUserLogoUrl,
					applyUserId,groupId,groupName,groupLogoUrl,openFireGroup,"",fromUserId,fromUserNickName,applyGroupId);
			rabbitmqService.sendPushMessage(zp);
	}

	/**
	 * 批量生成群通知消息
	 * 
	 * @param users
	 * @param context
	 * @param group
	 * @throws XueWenServiceException
	 */
	public void saveListUserMessage(List<Object> users, String context,XueWenGroup group, String type) throws XueWenServiceException {
		if (users == null || StringUtil.isBlank(context)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		for (Object obj : users) {
			UserMessage userMessage = new UserMessage();
			userMessage.setIsRead("0"); // 是否可读 0 未读 1 已读
			userMessage.setIsOpertison("0");
			userMessage.setUserId(obj.toString());
			MessageContext msgContext = new MessageContext();
			msgContext.setContext(context);
			if (group != null) {
				msgContext.setGroupId(group.getId());
				msgContext.setGroupName(group.getGroupName());
			}
			userMessage.setContext(msgContext);
			userMessage.setStime(System.currentTimeMillis());
			userMessage.setType(type); // 推送消息类型
			saveUserMessage(userMessage);
		}
	}

	/**
	 * 保存消息
	 * 
	 * @param userMessage
	 * @throws XueWenServiceException
	 */
	public void saveUserMessage(UserMessage userMessage)throws XueWenServiceException {
		UserMessage userMessageResult = userService.saveUserMessage(userMessage);
		if (userMessageResult == null) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_USERMESSAGE_201, null);
		}
	}
	/**
	 * 修改消息
	 * @param messageGroupId
	 * @param isOpertion
	 * @throws XueWenServiceException
	 */
	public void updateUserMessages(String messageGroupId, String isOpertion)throws XueWenServiceException {
		if (StringUtil.isBlank(messageGroupId)) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_USERMESSAGE_201, null);
		} else {
			List<UserMessage> userMessages = userService.findUserMessagesByMessageGroupId(messageGroupId);
			for (UserMessage um : userMessages) {
				um.setIsOpertison(isOpertion);
				um.setIsRead("1");
				userService.saveUserMessage(um);
			}
		}
	}

	/**
	 * 修改群组信息时需要判断是否有权限修改
	 * 
	 * @param user
	 * @param groupId
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean isPermission(User user, String groupId)throws XueWenServiceException {
		XueWenGroup group = groupRepo.findOne(groupId);
		if (group == null) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NODATA_201, null);
		}
		List<Object> ownerList = group.getOwner();
		if (ownerList.contains(user.getId())) {
			return true;
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NOPERMISSION_201, null);
		}
	}
	
	/**
	 * 删除群组下的干货或者话题时需要判断是否有权限修改（管理员）
	 * 
	 * @param user
	 * @param groupId
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean isAdmin(User user, String groupId)throws XueWenServiceException {
		XueWenGroup group = groupRepo.findOne(groupId);
		if (group == null) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NODATA_201, null);
		}
		List<Object> adminList = group.getAdmin();
		if (adminList.contains(user.getId())) {
			return true;
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NOPERMISSION_201, null);
		}
	}

	/**
	 * 查询附近的群
	 * 
	 * @param p
	 * @return
	 */
	public List<ResponseGroup> findAllNearGroup(String userId, Point p,Distance dis) throws XueWenServiceException {
		if (null == p) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_POSITION_201, null);
		}
		List<XueWenGroup> groups = groupRepo.findByPositionNearAndIsGeoOpen(p,true, dis);
		List<ResponseGroup> rps = new ArrayList<ResponseGroup>();
		for (XueWenGroup group : groups) {
			Map<String, Integer> summary = new HashMap<String, Integer>();
			summary.put("memberCount", group.getMember().size());
			summary.put("adminCount", group.getAdmin().size());
			group.setSummary(summary);
			if (null != group.getOwner() && group.getOwner().contains(userId)) {
				group.setIsMember("3"); // 创建者
			} else if (null != group.getAdmin()
					&& group.getAdmin().contains(userId)) {
				group.setIsMember("2"); // 管理员
			} else if (null != group.getMember()
					&& group.getMember().contains(userId)) {
				group.setIsMember("1"); // 成员
			} else {
				group.setIsMember("0"); // 未加入群
			}
			ResponseGroup rg = new ResponseGroup(group);
			rg.setDistance(StringUtil.Distance(p.getX(), p.getY(),group.getLng(), group.getLat()));
			rps.add(rg);
		}
		return rps;
	}

	/**
	 * 查找推荐的群
	 * 
	 * @param currentUser
	 * @param sort
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<XueWenGroup> findAllGroupRecommend(User currentUser,String pageNumber, String pageSize, Sort sort)throws XueWenServiceException {

		Pageable pageable = null;
		if (!StringUtils.isNullOrEmpty(pageNumber)&& !StringUtils.isNullOrEmpty(pageSize)) {
			pageable = new PageRequest(Integer.parseInt(pageNumber),Integer.parseInt(pageSize), sort);
		} else {
			pageable = new PageRequest(Integer.parseInt("0"),Integer.parseInt("10"), sort);
		}
		Remmond remmond = remmondService.findOneByUserId(currentUser.getId());
		if (remmond != null) {
			Map<String, String> map = remmond.getGroupId();
			Set<Map.Entry<String, String>> set = map.entrySet();
			List<Object> groupIds = new ArrayList<Object>();
			for (Iterator<Map.Entry<String, String>> it = set.iterator(); it.hasNext();) {
				Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
				groupIds.add(entry.getKey());
			}
			Page<XueWenGroup> group = groupRepo.findByIdIn(groupIds, pageable);
			if (group.getTotalElements() < 0) {
				throw new XueWenServiceException(Config.STATUS_201,
						Config.MSG_201, null);
			}
			return group;
		} else {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
	}

	/**
	 * 初始化 建立2d位置索引
	 * 
	 * @param racBeinfo
	 */
	public void creatTable() {
		mongoTemplate.indexOps(XueWenGroup.class).ensureIndex(new GeospatialIndex("position"));
	}

	/**
	 * 判断用户是否在该群中
	 * 
	 * @param groupId
	 * @param user
	 * @return
	 */
	public boolean doGroupContansUser(String groupId, User user)throws XueWenServiceException {
		XueWenGroup group = groupRepo.findOneById(groupId);
		if (null == group) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NODATA_201, null);
		}
		List<Object> members = group.getMember();
		if (members.contains(user.getId())) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 查询所有群
	 * 
	 * @param groupId
	 * @param user
	 * @return
	 */
	public List<Object> findAllGroups() throws XueWenServiceException {
		List<XueWenGroup> group = groupRepo.findAll();
		if (null == group) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NODATA_201, null);
		}
		List<Object> groupIds = new ArrayList<Object>();
		for (int i = 0; i < group.size(); i++) {
			groupIds.add(group.get(i).getId());
		}
		return groupIds;
	}
	
	/**
	 * 查询所有群
	 * 
	 * @param groupId
	 * @param user
	 * @return
	 */
	public List<XueWenGroup> findAllGroupstest() throws XueWenServiceException {
		List<XueWenGroup> group = groupRepo.findAll();
		return group;
	}
	/**
	 * 通过群组ID查询小组
	 * @param groupId
	 * @return
	 */
	public XueWenGroup findGroup(String groupId) {
		return groupRepo.findOne(groupId);
	}
	
	
	
	/**
	 * 根据位置中的群组列表返回群组列表
	 * @param boxs
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<Object> toBoxResponses(List<Box> boxs)throws XueWenServiceException {
		List<Object> groupRes = new ArrayList<Object>();
		if(boxs !=null && boxs.size()>0){
			for(Box box:boxs){
				XueWenGroup group=findGroup(box.getSourceId().toString());
				if(group !=null){
//					Map<String,Object> addAndModifyMap=new HashMap<String, Object>();
//					addAndModifyMap.put("boxId", box.getId());
//					String[] exclude = {"post","praiseResponse","position","tagName","group","categoryId","childCategoryId"};
					groupRes.add(group);
				}
			}
		}
		return groupRes;
	}
	
	
	/**
	 * 分页获取不在此位置的话题列表
	 * @param boxPostId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<XueWenGroup> findByBoxPostIdNotInBox(String boxPostId,Pageable pageable)throws XueWenServiceException{
		List<Object> ids=boxService.getSourceIdsByBoxPostId(boxPostId);
		return groupRepo.findByIsOpenAndIdNotIn("0",ids, pageable);
	}
	
	
	/**
	 * 获得群名称（暂时不用）
	 * @return
	 */
	private String groupName() {
		try {
			Mongo m = new Mongo("chat.yunxuetang.com");
			mongoTemplate = new MongoTemplate(new SimpleMongoDbFactory(m,"solr"));
			Query query = new Query();
			Title test = mongoTemplate.findAndRemove(query, Title.class);
			logger.info("获取群组名：" + test.getName());
			m.close();
			return test.getName();
		} catch (MongoException e) {
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 批量创建群组（暂时不用）
	 * @param max
	 * @throws XueWenServiceException
	 */
//	public void createManyGroups(int max) throws XueWenServiceException {
//		List<User> users = userRepo.findAll();
//		for (int i = 0; i < max; i++) {
//			// 获取一个用户
//			User user = users.get(StringUtil.getOneInt(users.size()));
//			String groupName = groupName();
//			XueWenGroup group = new XueWenGroup();
//			String groupNum = groupNumService.getGroupNum();
//			group.setGroupNumber(Long.parseLong(groupNum));
//			group.setGroupName(groupName);
//			List<Object> owner = new ArrayList<Object>();
//			owner.add(user.getId());
//			group.setOwner(owner);
//			group.setAdmin(owner);
//			group.setMember(owner);
//			long loginTime = System.currentTimeMillis();
//			group.setCtime(loginTime);
//			group.setUtime(loginTime);
//			List<Object> tags = new ArrayList<Object>();
//			tags.add(groupName);
//		//	group.setTag(tags);
//			group.setLogoUrl("http://s1.xuewen.yunxuetang.com/images/group/540e/c0d8/e4b0/fa91/04d6/6e11/icon/1410253015.807833.png");
//			group.setIntro("这是" + groupName + "的学习群组");
//			group.setIsOpen("0");
//			OpenFireGroup openFireGroup = new OpenFireGroup();
//			openFireGroup.setGroupService(openfireGroupService);
//			List<String> list = new ArrayList<String>();
//			list.add(openfireService);
//			openFireGroup.setServerList(list);
//			openFireGroup.setGroupName(groupNum);
//			openFireGroup.setGroupDesc(group.getGroupName());
//			OpenFireUser ofu = new OpenFireUser();
//			ofu.setOpenFireUserName(user.getUserName());
//			ofu.setOpenFirePassWord(user.getPassWord());
//			openFireGroup.setGroupCreater(ofu);
//			openFireGroup.setGroupName(groupNum);
//			group.setOpenFireGroup(openFireGroup);
//			try {
//				create(group, user, "tag");
//				openFireGroupService.create(openFireGroup);
//			} catch (Exception e) {
//				e.printStackTrace();
//				logger.error("==========生成群组失败：" + e);
//			}
//		}
//	}

	/**
	 * 修改群组默认图片
	 * 
	 * @throws XueWenServiceException
	 */
//	public void updateGroups() throws XueWenServiceException {
//		List<String> isOperations = new ArrayList<String>();
//		isOperations.add("0");
//		isOperations.add("1");
//		List<String> fileNames = getLogo("/var/www/html/default/gqrwtxb");
//		String url = "http://s1.xuewen.yunxuetang.com/default/gqrwtxb/";
//		List<XueWenGroup> groups = groupRepo.findAll();
//		for (XueWenGroup group : groups) {
//			String logo = url + fileNames.get(StringUtil.getOneInt(fileNames.size()));
//			String operation = isOperations.get(StringUtil.getOneInt(isOperations.size()));
//			logger.info("群组修改，图片地址：" + logo + "====加入限制：" + operation);
//			group.setLogoUrl(logo);
//			group.setIsOpen(operation);
//			groupRepo.save(group);
//		}
//	}

	/**
	 * 批量修改用户默认图片
	 * 
	 * @throws XueWenServiceException
	 */
//	public void updateUser() throws XueWenServiceException {
//		List<String> fileNames = getLogo("/var/www/html/default/gqrwtxb");
//		String url = "http://s1.xuewen.yunxuetang.com/default/gqrwtxb/";
//		List<User> users = userRepo.findAll();
//		for (User user : users) {
//			String logo = url+ fileNames.get(StringUtil.getOneInt(fileNames.size()));
//			logger.info("群组修改，图片地址：" + logo);
//			user.setLogoURL(logo);
//			userRepo.save(user);
//		}
//	}
	/**
	 * 获得某一路径下的文件
	 * @param path
	 * @return
	 */
	public List<String> getLogo(String path) {
		File dir = new File(path);
		File[] files = dir.listFiles();
		List<String> file_names = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile() && (!files[i].isHidden())) {// 判断是否是文件并不能是隐藏文件
				file_names.add(files[i].getName());
			}
		}
		return file_names;
	}

	/**
	 * 发现推荐群组
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param st
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<XueWenGroup> findGroup(Pageable pageable, String userId)throws XueWenServiceException {
		List<String> myGrouplist = myGroupService.myGroupIds(userId);
		if (null == myGrouplist) {
			myGrouplist = new ArrayList<String>();
		}
		Page<XueWenGroup> group = groupRepo.findByIdNotInAndIsOpen(myGrouplist,"0", pageable);
		if (group.getTotalElements() < 0) {
			logger.info("=====无返回符合条件群组");
		}
		return group;
	}

	/**
	 * 发现推荐群组(我没有加入的)
	 * 
	 * @param myGrouplist
	 * @param pageable
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<XueWenGroup> findGroup(List<String> myGrouplist,Pageable pageable, String userId) throws XueWenServiceException {
		Page<XueWenGroup> group = groupRepo.findByIdNotInAndIsOpen(myGrouplist,"0", pageable);
		if (group.getTotalElements() < 0) {
			logger.info("=====无返回符合条件群组");
		}
		return group;
	}

	/**
	 * 我加入的群组
	 * 
	 * @param myGrouplist
	 * @param pageable
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<XueWenGroup> findMyGroupByMember(String userId)throws XueWenServiceException {
		List<XueWenGroup> group = groupRepo.findByMemberAndOwnerIsNotAndAdminIsNot(userId, userId, userId);
		return group;
	}

	/**
	 * 我加入的群组
	 * 
	 * @param myGrouplist
	 * @param pageable
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<XueWenGroup> findMyGroupByOwner(String userId)throws XueWenServiceException {
		List<XueWenGroup> group = groupRepo.findByOwner(userId);
		return group;
	}

	/**
	 * 我加入的群组
	 * 
	 * @param myGrouplist
	 * @param pageable
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<XueWenGroup> findMyGroupByAdmin(String userId)throws XueWenServiceException {
		List<XueWenGroup> group = groupRepo.findByAdminAndOwnerIsNot(userId,userId);
		return group;
	}

	/**
	 * 查询所有群
	 * 
	 * @param groupId
	 * @param user
	 * @return
	 */
	public List<Object> findGroupByIsOpen(String userId)throws XueWenServiceException {
		List<String> myGrouplist = myGroupService.myGroupIds(userId);
		if (null == myGrouplist) {
			myGrouplist = new ArrayList<String>();
		}
		List<XueWenGroup> group = groupRepo.findByIdNotInAndIsOpen(myGrouplist,"0");
		if (null != group) {
			logger.info("=====无返回符合条件群");
			List<Object> groupIds = new ArrayList<Object>();
			for (int i = 0; i < group.size(); i++) {
				groupIds.add(group.get(i).getId());
			}
			return groupIds;
		}
		return null;
	}

	/**
	 * 根据我所在的群组ID集合，过滤出本人不是成员的群组Id集合
	 * 
	 * @param myGrouplist
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<Object> findGroupByIsOpen(List<String> myGrouplist)throws XueWenServiceException {
		List<XueWenGroup> group = groupRepo.findByIdNotInAndIsOpen(myGrouplist,"0");
		if (null != group) {
			logger.info("=====无返回符合条件群");
			List<Object> groupIds = new ArrayList<Object>();
			for (int i = 0; i < group.size(); i++) {
				groupIds.add(group.get(i).getId());
			}
			return groupIds;
		}
		return null;
	}

	/**
	 * 查询所有群
	 * 
	 * @param groupId
	 * @param user
	 * @return
	 */
	public List<Object> findGroupByIsOpen() throws XueWenServiceException {
		List<XueWenGroup> group = groupRepo.findAllAndIsOpen("0");
		if (null == group) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NODATA_201, null);
		}
		List<Object> groupIds = new ArrayList<Object>();
		for (int i = 0; i < group.size(); i++) {
			groupIds.add(group.get(i).getId());
		}
		return groupIds;
	}

	/**
	 * 邀请社交圈好友加入某一群
	 * 
	 * @param id
	 * @return
	 * @throws XueWenServiceException
	 */
	public void invite(String groupId, String userId, User user,Map<String, String> extra) throws XueWenServiceException {
		XueWenGroup group = groupRepo.findOneById(groupId);
		if (null != group) {
			// 如此用户为管理员则调用join方法，将申请者加入群
			join(group, userId);
			try {
				ztiaoPushService.sendInviteInGroup(user.getId(), user.getNickName(), user.getLogoURL(), 
						userId, groupId, group.getGroupName(), group.getLogoUrl(),group.getOpenFireGroup());
			} catch (Exception e) {
				logger.error("邀请好友入群推送消息："+e);
			}
			String notice = "您已经被邀请加入" + group.getGroupName() + "群组";
			extra.put("context", notice);
			List<String> alias = new ArrayList<String>();
			alias.add(userId);
			UserMessage userMessage = new UserMessage();
			userMessage.setUserId(userId);
			MessageContext context = new MessageContext();
			context.setGroupId(groupId);
			context.setGroupName(group.getGroupName());
			context.setContext(notice);
			userMessage.setContext(context);
			userMessage.setIsRead("0"); // 是否可读 0 未读 1 已读
			userMessage.setStime(System.currentTimeMillis());
			userMessage.setType("1001"); // 推送消息类型
			userMessage.setIsOpertison("0");
			saveUserMessage(userMessage);
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NOTHISGROUP_201, null);
		}
	}

	/**
	 * 通过通讯录邀请已注册用户加入群组
	 * 
	 * @author hjn
	 * @param groupId
	 * @param phoneNum
	 * @param user
	 * @throws XueWenServiceException
	 */
	public void inviteUserJoinGroup(String groupId, String phoneNum, User user)throws XueWenServiceException {
		XueWenGroup group = groupRepo.findOneById(groupId);
		if (null != group) {
			User inviteUser = userService.findByUserNameRspOnlyId(phoneNum);
			if (inviteUser != null) {
				// 如此用户为管理员则调用join方法，将申请者加入群
				Map<String, String> extra = new HashMap<String, String>();
				extra.put("type", "1001");
				join(group, inviteUser.getId());
				
				try {
					ztiaoPushService.sendInviteInGroup(user.getId(), user.getNickName(), user.getLogoURL(), 
							inviteUser.getId(), groupId, group.getGroupName(), group.getLogoUrl(),group.getOpenFireGroup());
				} catch (Exception e) {
					logger.error("邀请好友入群推送消息："+e);
				}
				
				String notice = "您已经被邀请加入" + group.getGroupName() + "群组";
				extra.put("context", notice);
				List<String> alias = new ArrayList<String>();
				alias.add(inviteUser.getId());
				UserMessage userMessage = new UserMessage();
				userMessage.setUserId(inviteUser.getId());
				MessageContext context = new MessageContext();
				context.setGroupId(groupId);
				context.setGroupName(group.getGroupName());
				context.setContext(notice);
				userMessage.setContext(context);
				userMessage.setIsRead("0"); // 是否可读 0 未读 1 已读
				userMessage.setStime(System.currentTimeMillis());
				userMessage.setType("1001"); // 推送消息类型
				userMessage.setIsOpertison("0");
				saveUserMessage(userMessage);
			} else {
				throw new XueWenServiceException(Config.STATUS_201,Config.MSG_USERNOTFIND_201, null);
			}
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NOTHISGROUP_201, null);
		}
	}

	/**
	 * 判断该用户是否在该群中
	 * 
	 * @param groupId
	 * @param userId
	 * @return
	 */
	public boolean findMember(String groupId, String userId) {
		XueWenGroup group = groupRepo.findByIdAndMember(groupId, userId);
		if (group == null) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 根据id查找群
	 * 
	 * @param groupId
	 * @param userId
	 * @return
	 */
	public XueWenGroup findByid(String groupId) {
		XueWenGroup group = groupRepo.findOne(groupId);
		return group;
	}

	/**
	 * 批量为所有群组生成二维码
	 * 
	 * @throws XueWenServiceException
	 */
	public void createQRCodeForEveryGroup() throws XueWenServiceException {
		List<XueWenGroup> groups = groupRepo.findAll();
		for (XueWenGroup group : groups) {
			group.setQrCodeUrl(createGroupQRCode(group));
			groupRepo.save(group);
		}
	}

	/**
	 * 创建话题标签
	 * 
	 * @param tagName
	 * @return
	 */
	public UserTagBean createUserTag(User user, XueWenGroup group,String tagName) {
		UserTagBean utb = new UserTagBean();
		utb.setUserId(user.getId());
		utb.setUserName(user.getUserName());
		utb.setItemId(group.getId());
		utb.setItemType(Config.TAG_TYPE_GROUP);
		utb.setCtime(String.valueOf(System.currentTimeMillis()));
		utb.setTagName(tagName);
		return utb;

	}

	/**
	 * 通过用户ID查询自己的群组
	 * 
	 * @param userId
	 * @param page
	 * @return
	 */
	public List<XueWenGroup> findMyJoinedGroup(String userId) {
		List<XueWenGroup> xue = groupRepo.findByMemberAndOwnerIsNot(userId,userId);
		return xue;

	}

	/**
	 * 
	 * @Title: findMyCreatedGroup
	 * @Description: 通过用户ID查询自己创建的群组
	 * @param id
	 * @return List<XueWenGroup>
	 * @throws
	 */
	public List<XueWenGroup> findMyCreatedGroup(String userId) {
		List<XueWenGroup> xue = groupRepo.findByOwner(userId);
		return xue;
	}
	
	public Page<XueWenGroup> findMyGreateGroup(String userId,Pageable pageable){
		Page<XueWenGroup> groups = groupRepo.findByOwner(userId,pageable);
	    return groups;
	}

	/**
	 * 
	 * @Title: findMyCreatedGroup
	 * @Description: 通过用户ID查询自己为管理员的小组（不包含创建者跟成员）
	 * @param id
	 * @return List<XueWenGroup>
	 * @throws
	 */
	public List<XueWenGroup> findMyAdminGroup(String userId) {
		List<XueWenGroup> xue = groupRepo.findByAdminAndOwnerIsNot(userId,userId);
		return xue;
	}
	
	/**
	 * 
	 * @Title: findMyAdminGroups
	 * @Description: 通过用户ID查询自己为管理员的小组（不包含成员）
	 * @param userId
	 * @return
	 * @throws XueWenServiceException List<XueWenGroup>
	 * @throws
	 */
	public List<XueWenGroup> findMyAdminGroups(String userId) throws XueWenServiceException {
		List<XueWenGroup> group = groupRepo.findByAdmin(userId);
		return group;
	}
	
	/**
	 * 
	 * @Title: findMyAdminGroups
	 * @Description: 通过用户ID查询自己为管理员的小组（不包含成员）带分页
	 * @param userId
	 * @return
	 * @throws XueWenServiceException List<XueWenGroup>
	 * @throws
	 */
	public Page<XueWenGroup> findMyAdminPageGroups(String userId,Pageable pageable) throws XueWenServiceException {
		Page<XueWenGroup> groups = groupRepo.findByAdmin(userId,pageable);
		return groups;
	}

	/**
	 * 
	 * @Title: findMyCreatedGroup
	 * @Description: 通过用户ID查询自己为成员小组（不包含创建者跟管理员）
	 * @param id
	 * @return List<XueWenGroup>
	 * @throws
	 */
	public List<XueWenGroup> findMyMemberGroup(String userId) {
		List<XueWenGroup> xue = groupRepo.findByMemberAndOwnerIsNotAndAdminIsNot(userId, userId, userId);
		return xue;
	}
	
	/**
	 * 
	 * @Title: 
	 * @Description: 通过用户ID查询自己为成员小组（不包含创建者跟管理员）
	 * @param id
	 * @return List<XueWenGroup>
	 * @throws
	 */
	public Page<XueWenGroup> findMyMemberGroup(String userId,Pageable pageable) {
		Page<XueWenGroup> xue = groupRepo.findByMemberAndOwnerIsNotAndAdminIsNot(userId, userId, userId,pageable);
		return xue;
	}

	/**
	 * 没有参与小组的总数量
	 * 
	 * @return
	 */
	public long groupCount(String userId) {
		long groupCount = groupRepo.count();
		List<String> myGroup = myGroupService.myGroupIds(userId);
		if (myGroup == null) {
			return groupCount;
		} else {
			return groupCount - myGroup.size();
		}

	}

	/**
	 * 
	 * @Title: registGroupUser
	 * @Description: 群组批量添加用户（shenb）
	 * @param users
	 * @param groupId
	 * @param md5
	 * @throws XueWenServiceException
	 *             void
	 * @throws
	 */
	public void registGroupUser(String users, String groupId, String md5)throws XueWenServiceException {
		if (StringUtil.isBlank(groupId) || StringUtil.isBlank(md5)|| StringUtil.isBlank(users)) {
			throw new XueWenServiceException(Config.STATUS_201, "参数有误", null);
		}
		XueWenGroup group = findGroup(groupId);
		if (group.getMember().size() > group.getGroupMax()) {
			throw new XueWenServiceException(Config.STATUS_201, "群组成员数量达到上限",null);
		}
		List<User> userList = change(users);
		if (userList.size() == 0) {
			throw new XueWenServiceException(Config.STATUS_201, "用户列表有误", null);
		}
		for (User user : userList) {
			User userBean = null;
			List<User> userList1;
			List<User> userList2;
			if (!StringUtil.isBlank(user.getPhoneNumber())) {
				userList1 = userRepo.findByphoneNumber(user.getPhoneNumber());
			} else {
				userList1 = new ArrayList<User>();
			}
			if (!StringUtil.isBlank(user.getEmail())) {
				userList2 = userRepo.findByEmail(user.getEmail());
			} else {
				userList2 = new ArrayList<User>();
			}

			// 该用户不存在
			if (userList1.size() == 0&& userList2.size() == 0&& !StringUtil.isBlank(user.getEmail()+ user.getPhoneNumber())) {
				long time = System.currentTimeMillis();
				user.setCtime(time);// 获取系统时间戳
				user.setUtime(time);
				// 20140911增加创建用户时，增加用户号
				String userNumber = userNumService.getGroupNum();
				user.setUserNumber(Long.parseLong(userNumber));
				user.setNickName(user.getNickName());
				if (user.getPhoneNumber() != null) {
					user.setUserName(user.getPhoneNumber());
				} else {
					user.setUserName(user.getEmail());
				}
				user = userRepo.save(user);
				join(group, user.getId());
			} else {
				if (userList1.size() != 0) {
					userBean = userList1.get(0);
				} else if (userList2.size() != 0) {
					userBean = userList2.get(0);
				}
				if (userBean != null) {
					List<Object> listMember = new ArrayList<Object>();
					if (null != group) {
						listMember = group.getMember();
						if (!listMember.contains(userBean.getId())) {
							join(group, userBean.getId());
						}
					}
				}
			}
		}
	}

	private List<User> change(String usersStr) {
		List<User> list = new ArrayList<User>();
		String[] userList = usersStr.split("\\|");
		for (String string : userList) {
			String[] user = string.split(",");
			User userBean = new User();
			userBean.setNickName(user[0].toString());
			userBean.setPhoneNumber(user[1].toString());
			userBean.setEmail(user[2].toString());
			list.add(userBean);
		}
		return list;

	}

	/**
	 * 
	 * @Title: memberPcPage
	 * @Description: 分页显示群组成员列表
	 * @param groupId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 *             Page<User>
	 * @throws
	 */
	public Page<User> memberPcPage(String userName, String groupId,Pageable pageable) throws XueWenServiceException {
		if (org.apache.commons.lang.StringUtils.isBlank(groupId)|| userName == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		XueWenGroup group = groupRepo.findOneById(groupId);
		Page<User> users = null;
		if (null != group) {
			List<Object> ownerList = group.getOwner();
			List<Object> adminList = group.getAdmin();
			List<Object> memberList = group.getMember();
			if (null != memberList) {
				users = userRepo.findByIdInAndUserNameLike(memberList,userName, pageable);
				List<User> list = users.getContent();
				if (list != null) {
					for (User user : list) {
						if (ownerList != null&& ownerList.contains(user.getId())) {
							user.setIntro("0");
						} else if (adminList != null&& adminList.contains(user.getId())) {
							user.setIntro("1");
						} else {
							user.setIntro("2");
						}
					}
				}
			}
		}
		return users;
	}
	
	/**
	 * 
	 * @Title: memberPcPage
	 * @Description: 分页显示群组成员列表
	 * @param groupId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 *             Page<User>
	 * @throws
	 */
	public Page<User> memberPcPageOnlyMember(String groupId,Pageable pageable) throws XueWenServiceException {
		if (org.apache.commons.lang.StringUtils.isBlank(groupId)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		XueWenGroup group = groupRepo.findOneById(groupId);
		Page<User> users = null;
		if (null != group) {
			List<Object> adminList = group.getAdmin();
			List<Object> memberList = group.getMember();
			if (null != memberList) {
				memberList.removeAll(adminList);
				users = userRepo.findByIdIn(memberList, pageable);
			}
		}
		return users;
	}

	/**
	 * 
	 * @Title: toResponsePcUser
	 * @Description: 分页显示群组成员列表减肥
	 * @param content
	 * @return Object
	 * @throws
	 */
	public List<ResponsePcUser> toResponsePcUser(List<User> users) {
		List<ResponsePcUser> pcUsers = new ArrayList<ResponsePcUser>();
		for (User user : users) {
			ResponsePcUser pcUser = toResponsePcUser(user, user.getIntro());
			pcUsers.add(pcUser);
		}
		return pcUsers;
	}

	/**
	 * 根据ID查找群组只返回member数据
	 * 
	 * @author hjn
	 * @param groupId
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public XueWenGroup findOneXuewenGroupOnlyMember(String groupId)throws XueWenServiceException {
		return groupTemplate.findOneXuewenGroupOnlyMember(groupId);
	}

	/**
	 * 推荐小组（按成员数）
	 * 
	 * @param pageable
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<XueWenGroup> recommend(Pageable pageable, String userId)throws XueWenServiceException {
//		List<String> myGrouplist = myGroupService.myGroupIds(userId);
//		if (null == myGrouplist) {
//			myGrouplist = new ArrayList<String>();
//		}
//		Page<XueWenGroup> group = groupRepo.findByIdNotIn(myGrouplist, pageable);
		Page<XueWenGroup> group = groupRepo.findAll(pageable);
		return group;
	}

	/**
	 * 根据ID查找群组，只返回群名和群号
	 * 
	 * @author hjn
	 * @param groupId
	 * @return
	 * @throws XueWenServiceException
	 */
	public XueWenGroup findOneXuewenGroupOnlyGroupNameAndGroupNum(String groupId)throws XueWenServiceException {
		return groupTemplate.findOneXuewenGroupOnlyGroupNameAndGroupNum(groupId);
	}

	/**
	 * 根据群组Id和用户Id判断用户是否在群组中
	 * 
	 * @author hjn
	 * @param groupId
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean isUserInGroupByGroupIdAndUserId(String groupId, String userId)throws XueWenServiceException {
		return groupTemplate.isUserInGroupByUserIdAndGroupId(groupId, userId);
	}

	/**
	 * 
	 * @Title: isGroupAdmin
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param gid
	 * @param userId
	 * @return boolean
	 * @throws
	 */
	public boolean isGroupAdmin(String gid, String userId) {
		if (groupRepo.findOneByIdAndAdminIn(gid, userId) == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 将user对象转换成前端对象，并获得该用户与小组成员的关系
	 * 
	 * @param user
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<ResponseUser> toResponseUser(List<User> user, String userId)throws XueWenServiceException {
		List<ResponseUser> respList = new ArrayList<ResponseUser>();
		if (null != user) {
			for (int i = 0; i < user.size(); i++) {
				ResponseUser respUser = new ResponseUser(user.get(i));
				if (user.get(i).getId().equals(userId)) {
					respUser.setContactStatus(10);
				} else {
					respUser.setContactStatus(contactUserService.contact(userId, user.get(i).getId()));
				}
				respList.add(respUser);
			}
		}
		return respList;

	}

	/**
	 * 根据用户Id和群组Id判断是否为群管理员
	 * 
	 * @param userId
	 * @param groupId
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean isGroupOwner(String userId, String groupId)throws XueWenServiceException {
		return groupTemplate.isGroupOwnerByUserIdAndGroupId(groupId, userId);
	}

	/**
	 * 根据群组Id返回群组Id，群组名称，群组头像，群组人员数量，群组课程数量
	 * 
	 * @param groupId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Object findByIdRspGroupNanemAndMemberCountAndCourseCount(String groupId, String userId) throws XueWenServiceException {
		XueWenGroup group = groupTemplate.findOneXuewenGroupRspGroupNameAndMemberAndLogoUrl(groupId);
		int memberCount = group.getMember().size();
		int courseCount = newGroupCourseService.getCourseCountByGroup(groupId);
		// 此处有群组成员列表，不需要重新查库
		boolean isMember = group.getMember().contains(userId);
		// 去掉无需返回前端的属性,只包含以下属性
		String[] include = { "id", "groupName", "logoUrl","isOpen"};
		// 添加的属性
		Map<String, Object> addAndModifyMap = new HashMap<String, Object>();
		if(group.getOpenFireGroup() !=null){
			ResponseOpenFire resp = new ResponseOpenFire(group.getOpenFireGroup());
			addAndModifyMap.put("openFireGroup", resp);
		}
		addAndModifyMap.put("memberCount", memberCount);
		addAndModifyMap.put("courseCount", courseCount);
		addAndModifyMap.put("isMember", isMember ? 1 : 0);
		
		return YXTJSONHelper.getInObjectAttrJsonObject(group, addAndModifyMap,include);
	}

	/**
	 * @throws XueWenServiceException
	 * 
	 * @Title: getGroupfreshMember
	 * @Description: 取 群活跃成员 按时间排序  10 个
	 * @param groupId
	 * @return List<User>
	 * @throws
	 */
	public List<JSONObject> getGroupfreshMember(String groupId,int s)throws XueWenServiceException {

		if (StringUtil.isBlank(groupId)) {
			throw new XueWenServiceException(Config.STATUS_201, "群组Id不能为空",null);
		}
		XueWenGroup group = groupRepo.findOne(groupId);
		List<Object> userIds = group.getMember();
		List<JSONObject> objs=new ArrayList<JSONObject>();
        Map<String, Object> addmap=new HashMap<String, Object>();
        addmap.put("total", group.getMember().size());
		for (int i =userIds.size() - 1 ; i >= Math.max(userIds.size()-s,0); i--) {
			User user=userService.findOne((String)userIds.get(i));
			if(user!=null){
				objs.add(YXTJSONHelper.getInObjectAttrJsonObject(user, addmap, new String[]{"id","nickName","intro","logoURL"}));	
			}
		}
		if(objs.size()==0){
			return null;
		}else{
			return objs;
		}

	}
	
	/**
	 * 
	 * @Title: toResponse
	 * @Description: 瘦身
	 * @param xue
	 * @return List<JSONObject>
	 * @throws
	 */
	public List<JSONObject> toResponse(List<XueWenGroup> xue,String... includeKey){
		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
		Map<String, Object> map = null;
		for (XueWenGroup xueWenGroup : xue) {
			map = new HashMap<String, Object>();
			map.put("memberSum", xueWenGroup.getMember().size());
			map.put("intro", xueWenGroup.getIntro());
			jsonObjects.add(YXTJSONHelper.getInObjectAttrJsonObject(xueWenGroup, map, includeKey));
		}
		return jsonObjects;
	}
	
	/**
	 * 
	 * @Title: getMyGroupTop3
	 * @Description: 已时间排序 获取top3
	 * @param userId
	 * @return List<JSONObject>
	 * @throws
	 */
	public List<JSONObject> getmyPcAllGroup(String userId){
//		QueryModel queryModel= new QueryModel();
//		queryModel.setS(4);
//		Pageable pageable=PageRequestTools.pageRequesMake(queryModel);
		List<XueWenGroup> groups= groupRepo.findByMemberIn(userId);
		List<JSONObject> objs=new ArrayList<JSONObject>();
		for(XueWenGroup group:groups){
			Map<String, Object> oMap=new HashMap<String, Object>();
			oMap.put("memberCount", group.getMember().size());
			JSONObject object=YXTJSONHelper.getInObjectAttrJsonObject(group,oMap , new String[]{"id","groupName","intro","logoUrl","ctime"});
			objs.add(object);
		}
		return objs;
		
	}
	
	/**
	 * 获取top3
	 * @param userId
	 * @return
	 */
	public List<JSONObject> getMyGroupTop3(String userId){
		QueryModel queryModel= new QueryModel();
		queryModel.setS(4);
		Pageable pageable=PageRequestTools.pageRequesMake(queryModel);
		Page<XueWenGroup> groups= groupRepo.findByMemberIn(userId,pageable);
		List<JSONObject> objs=new ArrayList<JSONObject>();
		for(XueWenGroup group:groups){
			Map<String, Object> oMap=new HashMap<String, Object>();
			oMap.put("memberCount", group.getMember().size());
			JSONObject object=YXTJSONHelper.getInObjectAttrJsonObject(group,oMap , new String[]{"id","groupName","intro","logoUrl","ctime"});
			objs.add(object);
		}
		return objs;
		
	}
	/**
	 * 
	 * @Title: shutForPc
	 * @Description:瘦身
	 * @param groups
	 * @return net.sf.json.JSONArray
	 * @throws
	 */
	public  net.sf.json.JSONArray shutForPc(Page<XueWenGroup> groups) {
		List<XueWenGroup>groupres=new ArrayList<XueWenGroup>();
		net.sf.json.JSONArray ayys= YXTJSONHelper.getINListObjectAttrJsonArray(groups.getContent(), new String[]{"id","groupName","logoUrl","intro","ctime"});
		return ayys;
	}
    /**
     * 
     * @Title: findById
     * @Description:通过iD取群
     * @param groupId
     * @return XueWenGroup
     * @throws
     */
	public XueWenGroup findById(String groupId) {
		return groupRepo.findOne(groupId);
		
	}
	/**
	 * 
	 * @param groupId
	 * 保存群组
	 * @return
	 */
	public XueWenGroup savegroup(XueWenGroup xuewen) {
		return groupRepo.save(xuewen);
		
	}
	public XueWenGroup findByGroupNumber(long groupNumber) {
		return groupRepo.findOneByGroupNumber(groupNumber);
	}
	/**
	 * 多人入群
	 * @param userIds
	 * @param groupId
	 * @return
	 */
	public boolean joinManyUser(String userIds, String groupId) throws XueWenServiceException{
		if(userIds!=null){
			userIds = JSON2ObjUtil.getArrayFromString(userIds);
			String[] sourceStrArray = userIds.split(",");
	        for (int i = 0; i < sourceStrArray.length; i++) {
	            this.join(groupId, sourceStrArray[i], "");
	        }
		}else{
			
		}
		
		return true;
	}
	/**
	 * 通过一级分类Id计算该分类下的小组数量
	 * 
	 * @param p
	 * @return
	 * @throws XueWenServiceException
	 */
	public int countByCategoryId(String categoryId)throws XueWenServiceException {
		return groupRepo.countByCategoryId(categoryId);
	}
	
	/**
	 * 通过二级分类Id计算该分类下的小组数量
	 * 
	 * @param p
	 * @return
	 * @throws XueWenServiceException
	 */
	public int countByChildCategoryId(String childCategoryId)throws XueWenServiceException {
		return groupRepo.countByChildCategoryId(childCategoryId);
	}
	/**
	 * 将没有分类的群组随机添加分类
	 * @param categoryId
	 * @param childCategoryId
	 */
	public void addGroupCategory()throws XueWenServiceException{
		List<Category> category = categoryService.findAllPrimary();
		Category cate = null;
		Category childCategory = null;
		String categoryId = "";
		String childId = "";
		if(category!=null && category.size() > 0){
			cate = category.get(StringUtil.getOneInt(category.size()));
			categoryId = cate.getId();
			List<Category> child =  categoryService.findSecondByPrimaryId(categoryId);
				List<XueWenGroup> groupList = groupRepo.findAll();
				XueWenGroup one = null;
				for(int  i= 0 ; i < groupList.size(); i++){
					if(child!=null && child.size() > 0){
						childCategory = child.get(StringUtil.getOneInt(child.size()));
						childId = childCategory.getId();
					}
					one = groupList.get(i);
				//	if(one.getCategoryId()==null){
					one.setCategoryId(categoryId);
					one.setChildCategoryId(childId);
					groupRepo.save(one);
				//	}
				}
				
			}
    }
	
	/**
	 * 批量修改群组的群号和openfire节点信息，重新在openfire中注册相关的
	 * @throws XueWenServiceException
	 */
	public void updateAllGroupNumAndOpenfireGroupInfo()throws XueWenServiceException{
		List<XueWenGroup> groups=groupRepo.findAll();
		for(XueWenGroup group:groups){
			// 1.生成群号
			String groupNum = groupNumService.getGroupNum();
			String userId=group.getOwner().get(0).toString();
			//获取创建者
			User user=userService.findOne(userId);
			logger.info("生成群号===================:" + groupNum);
			// 2.先创建openfire群组,以便于进行群组的聊天等
			group.setOpenFireGroup(creatOpenFireGroup(groupNum,group.getGroupName(),
					String.valueOf(user.getUserNumber()), user.getPassWord()));
			logger.info("创建openfire成功=====================");
			// 3.完善群组信息
			group.setGroupNumber(Long.parseLong(groupNum));
			groupRepo.save(group);
		}
	}
	
	/**
	 * 
	 * @Title: findByIdIn
	 * @auther Tangli
	 * @Description: 通过ids取
	 * @param ids
	 * @return List<XueWenGroup>
	 * @throws
	 */
	public List<XueWenGroup> findByIdIn(List<Object> ids) {
		return groupRepo.findByIdIn(ids);
	}
	
	
	/**
	 * 
	 * @Title: findByIdIn
	 * @auther Tangli
	 * @Description: 通过ids取
	 * @param ids
	 * @return List<XueWenGroup>
	 * @throws
	 */
	public Page<XueWenGroup> findByIdIn(List<Object> ids,Pageable pageable) {
		return groupRepo.findByIdIn(ids,pageable);
	}
	
	
	/**
	 * 分页获取不在此位置的话题列表
	 * @param boxPostId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<XueWenGroup> findByBoxPostIdNotInBoxForSearch(String boxPostId,String keyword)throws XueWenServiceException{
		List<Object> ids=boxService.getSourceIdsByBoxPostId(boxPostId);
		return groupRepo.findByGroupNameRegexAndIsOpenAndIdNotInOrIntroRegexAndIsOpenAndIdNotIn(keyword,"0",ids,keyword,"0",ids);
	}
	
	/**
	 * 合并账户，将fromUser 的群组合并到toUser
	 * @param fromUserId
	 * @param toUserId
	 * @throws XueWenServiceException
	 */
	public void mergeUserGroup(String fromUserId,String toUserId)throws XueWenServiceException{
		//将fromUser创建的群组，合并至toUser
		mergeUserOwnerGroup(fromUserId,toUserId);
		//将fromUser管理的群组，合并至toUser
		mergeUserAdminGroup(fromUserId,toUserId);
		//将fromUser参与的群组，合并至toUser
		mergeUserMemberGroup(fromUserId,toUserId);
	}
	
	/**
	 * 合并账户，将fromUser创建的群组，合并至toUser
	 * @param fromUserId
	 * @param toUserId
	 * @throws XueWenServiceException
	 */
	public void mergeUserOwnerGroup(String fromUserId,String toUserId)throws XueWenServiceException{
		//获取fromuser创建的群组
		List<XueWenGroup> ownerGroups=findMyCreatedGroup(fromUserId);
		if(ownerGroups !=null && ownerGroups.size()>0){
			for(XueWenGroup group:ownerGroups){
				List<Object> owners=group.getOwner();
				owners.remove(fromUserId);
				if(!owners.contains(toUserId)){
					owners.add(toUserId);
				}
				List<Object> admins=group.getAdmin();
				admins.remove(fromUserId);
				if(!admins.contains(toUserId)){
					admins.add(toUserId);
				}
				List<Object> members=group.getMember();
				members.remove(fromUserId);
				if(!members.contains(toUserId)){
					members.add(toUserId);
				}
				//将群组从fromUser的群组列表中移除
				myGroupService.removeMyGroup(fromUserId,group.getId());
				//添加群组到toUser的群组列表中
				myGroupService.addMyGroup(toUserId, group.getId());
			}
			groupRepo.save(ownerGroups);
		}
	}
	/**
	 * 
	 * 合并账户，将fromUser管理的群组，合并至toUser
	 * @param fromUserId
	 * @param toUserId
	 * @throws XueWenServiceException
	 */
	public void mergeUserAdminGroup(String fromUserId,String toUserId)throws XueWenServiceException{
		//获取fromUser管理的群组
		List<XueWenGroup> adminGroups=findMyAdminGroup(fromUserId);
		if(adminGroups !=null && adminGroups.size()>0){
			for(XueWenGroup group:adminGroups){
				List<Object> admins=group.getAdmin();
				admins.remove(fromUserId);
				if(!admins.contains(toUserId)){
					admins.add(toUserId);
				}
				List<Object> members=group.getMember();
				members.remove(fromUserId);
				if(!members.contains(toUserId)){
					members.add(toUserId);
				}
				//将群组从fromUser的群组列表中移除
				myGroupService.removeMyGroup(fromUserId,group.getId());
				//添加群组到我的群组
				myGroupService.addMyGroup(toUserId, group.getId());
			}
			groupRepo.save(adminGroups);
		}
	}
	/**
	 * 合并账户，将fromUser参与的群组，合并至toUser
	 * @param fromUserId
	 * @param toUserId
	 * @throws XueWenServiceException
	 */
	public void mergeUserMemberGroup(String fromUserId,String toUserId)throws XueWenServiceException{
		//获取fromUser参加的群组
		List<XueWenGroup> memberGroups=findMyMemberGroup(fromUserId);
		if(memberGroups !=null && memberGroups.size()>0){
			for(XueWenGroup group:memberGroups){
				List<Object> members=group.getMember();
				members.remove(fromUserId);
				if(!members.contains(toUserId)){
					members.add(toUserId);
				}
				//将群组从fromUser的群组列表中移除
				myGroupService.removeMyGroup(fromUserId,group.getId());
				//添加群组到我的群组
				myGroupService.addMyGroup(toUserId, group.getId());
			}
			groupRepo.save(memberGroups);
		}
	}
	/**
	 * 群推荐、群搜索列表
	 * @param groups
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<Object> toGroupResponses(List<XueWenGroup> groups,String userId)throws XueWenServiceException {
		List<Object> groupRes = new ArrayList<Object>();
		if(groups !=null && groups.size()>0){
			for(int i=0 ;i < groups.size(); i++){
					if(groups.get(i)==null){
						continue;
					}
					System.out.println("groupid======="+groups.get(i).getId());
					Map<String,Object> addAndModifyMap=new HashMap<String, Object>();
					int courseCount = newGroupCourseService.getCourseCountByGroup(groups.get(i).getId());
//					map.put("courseCount",courseCount);
//					addAndModifyMap.put("summary",map);
					OpenFireGroup openFireGroup = groups.get(i).getOpenFireGroup();
					if(openFireGroup!=null){
					ResponseOpenFire resp = new ResponseOpenFire(openFireGroup);
					addAndModifyMap.put("openFireGroup",resp);
					}
					addAndModifyMap.put("memberCount",groups.get(i).getMember().size());
					//Map summary = new HashMap();
					Map<String, Integer> summary = new HashMap<String, Integer>();
					// 此处有群组成员列表，不需要重新查库
					boolean isMember = groups.get(i).getMember().contains(userId);
					
					summary.put("memberCount", groups.get(i).getMember().size());
					summary.put("adminCount", 0);
					summary.put("ownerCount", 0);
					summary.put("courseCount", courseCount);
					addAndModifyMap.put("summary", summary);
					addAndModifyMap.put("isMember", isMember ? 1 : 0);
					String[] include = {"id", "groupName", "intro","logoUrl","ctime","isOpen"};
					groupRes.add(YXTJSONHelper.getInObjectAttrJsonObject(groups.get(i), addAndModifyMap, include));
				}
			}
		return groupRes;
	}
	
	/**
	 * 我的群组（创建、管理、加入）
	 * @param groups
	 * @return
	 * @throws XueWenServiceException
	 */
	public MyAllGroup MyGroups(List<XueWenGroup> groups,String userId)throws XueWenServiceException {
		List<Object> ownerResponse = new ArrayList<Object>();
		List<Object> adminResponse = new ArrayList<Object>();
		List<Object> memberResponse = new ArrayList<Object>();
		MyAllGroup myGroup = null;
		if(groups !=null && groups.size()>0){
			for(int i=0 ;i < groups.size(); i++){
				if(groups.get(i)==null){
					continue;
				}
				Map<String,Object> addAndModifyMap=new HashMap<String, Object>();
				int courseCount = newGroupCourseService.getCourseCountByGroup(groups.get(i).getId());
				OpenFireGroup openFireGroup = groups.get(i).getOpenFireGroup();
				if(openFireGroup!=null){
				ResponseOpenFire resp = new ResponseOpenFire(openFireGroup);
				addAndModifyMap.put("openFireGroup",resp);
				}
				addAndModifyMap.put("memberCount",groups.get(i).getMember().size());
				//Map summary = new HashMap();
				Map<String, Integer> summary = new HashMap<String, Integer>();
				// 此处有群组成员列表，不需要重新查库
			//	boolean isMember = groups.get(i).getMember().contains(userId);
				
				summary.put("memberCount", groups.get(i).getMember().size());
				summary.put("adminCount", 0);
				summary.put("ownerCount", 0);
				summary.put("courseCount", courseCount);
				addAndModifyMap.put("summary", summary);
			//	addAndModifyMap.put("isMember", isMember ? 1 : 0);
				String[] include = {"id", "groupName", "intro","logoUrl","ctime","isOpen"};
				if(groups.get(i).getOwner().contains(userId)){
					ownerResponse.add(YXTJSONHelper.getInObjectAttrJsonObject(groups.get(i), addAndModifyMap, include));
				}if(groups.get(i).getAdmin().contains(userId) && !groups.get(i).getOwner().contains(userId)){
					adminResponse.add(YXTJSONHelper.getInObjectAttrJsonObject(groups.get(i), addAndModifyMap, include));
				}
				if(groups.get(i).getMember().contains(userId) && !groups.get(i).getOwner().contains(userId) && !groups.get(i).getAdmin().contains(userId)){
					memberResponse.add(YXTJSONHelper.getInObjectAttrJsonObject(groups.get(i), addAndModifyMap, include));
				}
				}
			 myGroup = new MyAllGroup(ownerResponse,adminResponse,memberResponse);
			}
		return myGroup;
	}
	
	/**
	 * 创建加入群组动态
	 * @param groupId
	 * @throws XueWenServiceException
	 */
	public void createGroupJoinDynamic(String groupId,String userId)throws XueWenServiceException{
		User user=userService.findOne(userId);
		groupDynamicService.addGroupDynamic(groupId,"","", "","", "", "",null, user.getId(), user.getNickName(), 
				user.getLogoURL(), Config.TYPE_JOINGROUP, System.currentTimeMillis());
	}
	/**
	 * 创建群组时的动态
	 * @param groupId
	 * @param userId
	 * @param autherNickName
	 * @param autherLogoUrl
	 * @param time
	 * @throws XueWenServiceException
	 */
	public void creatGroupCreatDynamic(String groupId,String  userId,String autherNickName,String autherLogoUrl,long time)throws XueWenServiceException{
		groupDynamicService.addGroupDynamic(groupId,"","", "", "", "", "", null,userId,
				autherNickName, autherLogoUrl, Config.TYPE_GROUP, time);
	}
	
	/**
	 * 为已经创建的群组添加
	 * @throws XueWenServiceException
	 */
	public void createOldGroupCreateDynamic()throws XueWenServiceException{
		List<XueWenGroup> groups=groupRepo.findAll();
		for(XueWenGroup group:groups){
			User user=userService.findOne(group.getOwner().get(0).toString());
			creatGroupCreatDynamic(group.getId(),user.getId(),user.getNickName(),user.getLogoURL(),group.getCtime());
		}
	}
	
	
	public long count(){
		return groupRepo.count();
	}
	
	public long countByChildCategory(String childCategoryId){
		return groupRepo.countByChildCategoryId(childCategoryId);
	}
	
	/**
	 * 根据群组Id返回群组Id，群组名称，群组头像，群组人员数量，群组课程数量
	 * 
	 * @param groupId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Object toGroupHelp(String groupId, String userId) throws XueWenServiceException {
		XueWenGroup group = groupTemplate.findOneXuewenGroupRspGroupNameAndMemberAndLogoUrl(groupId);
		int memberCount = group.getMember().size();
		int courseCount = newGroupCourseService.getCourseCountByGroup(groupId);
		// 此处有群组成员列表，不需要重新查库
		boolean isMember = group.getMember().contains(userId);
		// 去掉无需返回前端的属性,只包含以下属性
		String[] include = { "id", "groupName", "logoUrl","ctime","isOpen"};
		// 添加的属性
		Map<String, Object> addAndModifyMap = new HashMap<String, Object>();
		if(group.getOpenFireGroup() !=null){
			ResponseOpenFire resp = new ResponseOpenFire(group.getOpenFireGroup());
			addAndModifyMap.put("openFireGroup", resp);
		}
		Map summary = new HashMap();
		summary.put("memberCount", memberCount);
		summary.put("adminCount", 0);
		summary.put("ownerCount", 0);
		summary.put("courseCount", courseCount);
		addAndModifyMap.put("summary", summary);
		//addAndModifyMap.put("courseCount", courseCount);
		addAndModifyMap.put("isMember", isMember ? 1 : 0);
		
		return YXTJSONHelper.getInObjectAttrJsonObject(group, addAndModifyMap,include);
	}
	/**
	 * 通过分类查询群组
	 * @param categoryId
	 * @param childCategoryId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<XueWenGroup> findGroupByCategoryId(String categoryId,String childCategoryId,Pageable pageable) throws XueWenServiceException {
		Page<XueWenGroup> group = null;
		if(!StringUtil.isBlank(childCategoryId)){
			group = groupRepo.findByChildCategoryId(childCategoryId, pageable);
		}else{
			group = groupRepo.findByCategoryId(categoryId, pageable);
		}
		return group;
	}
	
	/**
	 * 
	 * @Title: 获得我参与的群组app
	 * @Description: 已时间排序 获取top3
	 * @param userId
	 * @return List<JSONObject>
	 * @throws
	 */
	public List<XueWenGroup> getMyGroups(String userId){
		
		String sort = "ctime";
		String mode = "DESC";
		Direction d = Direction.DESC;
		if(mode.equalsIgnoreCase("ASC")){
			d = Direction.ASC;
		}
		Sort st = new Sort(d,sort);
		List<XueWenGroup> groups= groupRepo.findByMemberIn(userId,st);
		return groups;
		
	}
	public List<XueWenGroup> findMyCreatedGroup(String userId,Sort st) {
		List<XueWenGroup> xue = groupRepo.findByOwner(userId,st);
		return xue;
	}
	public List<XueWenGroup> findMyAdminGroup(String userId,Sort st) {
		List<XueWenGroup> xue = groupRepo.findByAdminAndOwnerIsNot(userId,userId,st);
		return xue;
	}
	public List<XueWenGroup> findMyMemberGroup(String userId,Sort st) {
		List<XueWenGroup> xue = groupRepo.findByMemberAndOwnerIsNotAndAdminIsNot(userId, userId, userId,st);
		return xue;
	}
	
	/**
	 * 
	 * @auther tangli
	 * @Description: 分页获取我的群组
	 * @param id
	 * @param pageable
	 * @return Page<XueWenGroup>
	 * @Date:2015年4月20日
	 * @throws
	 */
	public Page<XueWenGroup> findMyGroups(String id, Pageable pageable) {
		return groupRepo.findByMemberIn(id, pageable);
	}
	public boolean isOwner(String groupId, User user) {
		XueWenGroup xueWenGroup=findById(groupId);
		
		return xueWenGroup.getOwner().contains(user.getId());
	}
	/**
	 * 
	 * @Title: isAdmin
	 * @Description: 
	 * @param groupId
	 * @param user
	 * @return boolean
	 * @throws
	 */
	public boolean isAdmin(String groupId, User user) {
		XueWenGroup xueWenGroup=findById(groupId);
		
		return xueWenGroup.getAdmin().contains(user.getId());
	}
	
	
	
	public boolean isMember(String groupId, User user) {
		XueWenGroup xueWenGroup=findById(groupId);
		
		return xueWenGroup.getMember().contains(user.getId());
	}
	/**
	 * 
	 * @Title: getJsonByGroupId
	 * @Description: 小组下的内容，根据浏览数量排序（取九个）
	 * @param userId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException Map<String,Object>
	 * @throws
	 */
	public Map<String, Object> getJsonByGroupId(String groupId, QueryModel dm,User cuser) throws XueWenServiceException {
		Map<String, Object> res = new HashMap<String, Object>();
		List<JSONObject> jObjects = new ArrayList<JSONObject>();
		dm.setSort("viewCount");
		dm.setN(0);
		dm.setS(9);
		Pageable pageable=PageRequestTools.pageRequesMake(dm);
		Page<NewCourse> courses=newCourseService.getGroupCoursesPcNew(pageable, groupId, "",true);
		if(courses.getContent().size()!=0){
			for (NewCourse newCourse : courses) {
				if( newCourse.getId()!=null){
					JSONObject map=new JSONObject();
					map.put("itemId", newCourse.getId());
					map.put("title", newCourse.getTitle());
					map.put("intro", newCourse.getIntro());
					JSONObject obj = newCourseService.getCourseJson(newCourse, "");
					map.put("groupId", (String) obj.get("groupId"));
					map.put("groupName", (String) obj.get("groupName"));
					map.put("praiseCount", newCourse.getPraiseCount());
					if(newCourse.getCreateUser()!=null){
						User user=userService.findOne(newCourse.getCreateUser().toString());
						if (user!=null) {
							map.put("logoURL",user.getLogoURL() );
							map.put("authorName",user.getNickName() );
							map.put("station",user.getStation() );
							map.put("uId",user.getId() );
						}
					}
					map.put("coverLogoUrl", newCourse.getLogoUrl());
					map.put("searchType", Config.TYPE_COURSE_GROUP);
					map.put("viewCount", newCourse.getViewCount());
					jObjects.add(map);
				}
				
			}
		}
//		dm.setSort("likesCount");
//		Pageable pageable1=PageRequestTools.pageRequesMake(dm);
		Page<Topic> topics=topicService.findByGroupId(groupId, pageable, Config.YXTDOMAIN);
		if (topics.getContent().size()!=0) {
			for (Topic topic : topics.getContent()) {
				if(topic.getTopicId()!=null){
					JSONObject map=new JSONObject();
					map.put("itemId", topic.getTopicId());
					map.put("title", topic.getTitle());
					map.put("intro", topic.getContent());
					map.put("groupId", topic.getSourceId());
					map.put("groupName", topic.getSourceName());
					map.put("praiseCount", topic.getLikesCount());
					if(topic.getAuthorId()!=null){
						User user=userService.findOne(topic.getAuthorId().toString());
						if (user!=null) {
							map.put("logoURL",user.getLogoURL() );
							map.put("authorName",user.getNickName() );
							map.put("station",user.getStation() );
							map.put("uId",user.getId() );
						}
					}
					map.put("searchType", Config.TYPE_TOPIC_GROUP);
					map.put("images", topic.getImages());
					map.put("viewCount", topic.getViewCount());
					jObjects.add(map);
				}
				
			}
		}
//		dm.setSort("likesCount");
//		Pageable pageable2=PageRequestTools.pageRequesMake(dm);
		Page<Drycargo> drys=drycarGoService.allPc(groupId, "", pageable);
		if(drys.getContent().size()!=0){
			for (Drycargo drycargo : drys) {
				JSONObject map=new JSONObject();
				if(drycargo.getId()!=null){
					map.put("itemId", drycargo.getId());
					map.put("title", drycargo.getMessage());
					map.put("intro", drycargo.getDescription());
					map.put("groupId",drycargo.getGroup());
					map.put("groupName", drycargo.getGroupName());
					map.put("praiseCount", drycargo.getLikesCount());
					if(drycargo.getAuthorId()!=null){
						User user=userService.findOne(drycargo.getAuthorId().toString());
						if (user!=null) {
							map.put("logoURL",user.getLogoURL() );
							map.put("authorName",user.getNickName() );
							map.put("station",user.getStation() );
							map.put("uId",user.getId() );
						}
					}
					map.put("coverLogoUrl", drycargo.getFileUrl());
					map.put("searchType",Config.TYPE_DRYCARGO_GROUP);
					map.put("viewCount", drycargo.getViewCount());
					jObjects.add(map);
				}
			}
		}
	//排序
		ListComparator comparator =new ListComparator("viewCount");
		Collections.sort(jObjects,comparator);
		int s=jObjects.size()>27?27:jObjects.size();
		//排序
		res.put("items", jObjects.subList(0, s));
		//小组基本信息（包含3个粉丝数量最多的成员）
		res.put("group", getBaeInfoByGroupId(groupId,cuser));
		return res;
	}
	/**
	 * 
	 * @Title: getBaeInfoByGroupId
	 * @Description: 小组基本信息（包含3个粉丝数量最多的成员）
	 * @param userId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException Map<String,Object>
	 * @throws
	 */
	public Map<String, Object> getBaeInfoByGroupId(String groupId,User cuser) throws XueWenServiceException {
		Map<String, Object> res = new HashMap<String, Object>();
		List<JSONObject> jObjects = new ArrayList<JSONObject>();
		XueWenGroup group=findById(groupId);
		if(group.getMember().size()!=0){
			List<String> ids=new ArrayList<String>();
			for (Object userId : group.getMember()) {
				ids.add(userId.toString());
			}
			List<User> users=userService.findByIdIn(ids);
			if(users!=null){
				for (User user : users) {
					JSONObject jsonObject=new JSONObject();
					jsonObject.put("contactCount", contactUserService.getUserContact(user.getId(), "0"));
					jsonObject.put("nickName", user.getNickName());
					jsonObject.put("logoURL", user.getLogoURL());
					jsonObject.put("station", user.getStation());
					jsonObject.put("uId", user.getId());
					jObjects.add(jsonObject);
				}
				
			}
		}
	//排序
		ListComparator comparator =new ListComparator("contactCount");
		Collections.sort(jObjects,comparator);
		int s=jObjects.size()>3?3:jObjects.size();
		//排序
		res.put("contacts", jObjects.subList(0, s));
		res.put("groupName", group.getGroupName());
		res.put("memberCount", group.getMemberCount());
		res.put("logoUrl", group.getLogoUrl());
		res.put("groupId", group.getId());
		if(cuser!=null){
			res.put("isMember", isMember(groupId, cuser));
		}else{
			res.put("isMember", false);
		}
		
		return res;
	}
	
	/**
	 * 根据位置中的话题列表返回话题列表
	 * @param boxs
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<Object> toBox(List<Box> boxs)throws XueWenServiceException {
		List<Object> groupRes = new ArrayList<Object>();
		List<Object> groupIds = new ArrayList<Object>();
		if(boxs !=null && boxs.size()>0){
			for(Box box:boxs){
				groupIds.add(box.getSourceId().toString());
			}
			QueryModel mm = new QueryModel();
//			List<String> sort = new ArrayList<String>();
//			sort.add("weightSort");
//			sort.add("ctime");
			mm.setSort("ctime");
			//String sort = "weightSort";
			String mode = "DESC";
			Direction d = Direction.DESC;
			if(mode.equalsIgnoreCase("ASC")){
				d = Direction.ASC;
			}
			Sort st = new Sort(d,mm.getSort());
			List<XueWenGroup> groups =  groupRepo.findByIdIn(groupIds,st);
			for(int i=0 ;i < groups.size(); i++){
				if(groups.get(i)==null){
					continue;
				}
					Map<String,Object> addAndModifyMap=new HashMap<String, Object>();
					//int subjectCount = topicService.getTopicCount(groups.get(i).getId());
					//int courseCount = newGroupCourseService.getCourseCountByGroup(groups.get(i).getId());
					//addAndModifyMap.put("subjectCount",subjectCount);
					//addAndModifyMap.put("courseCount",courseCount);
					addAndModifyMap.put("memberCount",groups.get(i).getMember().size());
					String[] include = {"id", "groupName", "intro","logoUrl","isMember"};
					groupRes.add(YXTJSONHelper.getInObjectAttrJsonObject(groups.get(i), addAndModifyMap, include));
				}
			}
		return groupRes;
	}

	public Page<XueWenGroup> findByBoxPostIdNotInBoxAndNotInCategory(String boxPostId, String type, String category, Pageable pageable) throws XueWenServiceException{
		List<Object> ids=boxService.getSourceIdsByBoxPostIdAndNotInCagetory(boxPostId,type);
		return groupRepo.findByIdNotIn(ids, pageable);
	}
	
	
	public List<Object> toGroupNotActivity(List<XueWenGroup> groups,String userId,String source)throws XueWenServiceException {
		List<Object> groupRes = new ArrayList<Object>();
		JSONArray array = JSONArray.fromObject(source);
		if(groups !=null && groups.size()>0){
			for(int i=0 ;i < groups.size(); i++){
					Map<String,Object> addAndModifyMap=new HashMap<String, Object>();
					if(groups.get(i)==null){
						continue;
					}
					for(int k = 0 ; k < array.size() ; k++){
						if(groups.get(i).getId().equals(array.get(k))){
							addAndModifyMap.put("flag","1");
						}
					}
					
					String[] include = {"id", "groupName", "intro","logoUrl","ctime","isOpen"};
					JSONObject data = YXTJSONHelper.getInObjectAttrJsonObject(groups.get(i), addAndModifyMap, include);
					data=YXTJSONHelper.addAndModifyAttrJsonObject(data, new HashMap<String, Object>());
					groupRes.add(data);
				}
			}
		return groupRes;
	}
	
}
