package operation.service.topics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;
import operation.controller.test.MyTestController;
import operation.exception.XueWenServiceException;
import operation.pojo.box.Box;
import operation.pojo.drycargo.Drycargo;
import operation.pojo.dynamic.GroupDynamic;
import operation.pojo.file.FileStoreInfo;
import operation.pojo.group.XueWenGroup;
import operation.pojo.praise.Praise;
import operation.pojo.praise.UserPraiseResponse;
import operation.pojo.tags.UserTagBean;
import operation.pojo.topics.Images;
import operation.pojo.topics.Post;
import operation.pojo.topics.SubPost;
import operation.pojo.topics.Topic;
import operation.pojo.topics.TopicResponse;
import operation.pojo.user.User;
import operation.repo.box.BoxTemplate;
import operation.repo.dynamic.GroupDynamicRepository;
import operation.repo.dynamic.GroupDynamicTemplate;
import operation.repo.topics.TopicRepository;
import operation.repo.topics.TopicTemplate;
import operation.service.box.BoxService;
import operation.service.category.CategoryService;
import operation.service.dynamic.GroupDynamicService;
import operation.service.fav.FavService;
import operation.service.file.MyFileService;
import operation.service.group.GroupService;
import operation.service.praise.PraiseService;
import operation.service.praise.UnPraiseService;
import operation.service.rabbitmq.RabbitmqService;
import operation.service.share.ShareService;
import operation.service.tags.LocalTagService;
import operation.service.tags.TagService;
import operation.service.user.UserService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import tools.Config;
import tools.JSON2ObjUtil;
import tools.StringUtil;
import tools.YXTJSONHelper;

@Service
@Component
public class TopicService {
	private static final Logger logger = Logger.getLogger(TopicService.class);
	@Autowired
	private TopicRepository topicRepository;
	@Autowired
	public GroupService groupService;
	@Autowired
	private MyFileService myFileService;
	@Autowired
	private PraiseService praiseService;
	@Autowired
	private UnPraiseService unPraiseService;
	@Autowired
	private PostService postService;
	@Autowired
	private ShareService shareService;
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	public GroupDynamicRepository groupDynamicRepository;

	@Autowired
	private UserService userService;
	@Autowired
	private TagService tagService;	
	@Autowired
	private TopicTemplate topicTemplate;
	@Autowired
	private BoxService boxService;
	
	@Autowired
	private RabbitmqService rabbitmqService;
	
	@Value("${tag.service.url}")
	private String tagServiceUrl;

	@Autowired
	private FavService favService;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	BoxTemplate boxTemplate;
	
	@Autowired
	private GroupDynamicService groupDynamicService;
	
	@Autowired
	private LocalTagService localTagService;
	
	@Autowired
	public GroupDynamicTemplate groupDynamicTemplate;

	public TopicService() {

	}

	/**
	 * 创建一个话题对象
	 * 
	 * @param user
	 *            用户
	 * @param sourceId
	 *            来源ID
	 * @param sourceName
	 *            来源名称
	 * @param courseId
	 *            课程ID
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 * @param lat
	 *            经度
	 * @param lng
	 *            维度
	 * @param picUrl
	 *            图片地址
	 * @return
	 * @throws XueWenServiceException
	 */
	public Topic createTopic(User user, Topic topic, String tagName,String isGeoOpen,String image) throws XueWenServiceException {
		if(StringUtil.isBlank(topic.getSourceId())){
			throw new XueWenServiceException(Config.STATUS_601, Config.MSG_NOGROUP_601,null);
		}
		topic.setAuthorId(user.getId());
		topic.setAuthorName(user.getNickName());
		topic.setAuthorLogoUrl(user.getLogoURL());
		long time = System.currentTimeMillis();
		topic.setCtime(time);
		topic.setUtime(time);
		topic.setViewCount(0);
		topic.setReplyCount(0);
		topic.setNewReplyCount(0);
		topic.setLikesCount(0);
		topic.setUnLikeCount(0);
		topic.setShareCount(0);
		topic.setShareCount(0);
		topic.setDisplayOrder(0);
		topic.setDigestLevel(1);
		topic.setDeleted(false);
		if(StringUtil.isBlank(topic.getTitle())){
		topic.setTitle(StringUtil.getStr(topic.getContent()));
		}
		double[] position = new double[] { topic.getLng(), topic.getLat() };
		topic.setPosition(position);
		//调整支持多张图片（老版本也保持支持1张图片）
		logger.info(image+"调整支持多张图片（老版本也保持支持1张图片）");
		topic = changePicUrlToImage(topic,image,topic.getPicUrl());
		if ("0".equals(isGeoOpen)) {
			topic.setGeoOpen(false);
		} else {
			topic.setGeoOpen(true);
		}
		//2015-02-05增加主题分类
		XueWenGroup group = groupService.findById(topic.getSourceId());
		topic.setCategoryId(group.getCategoryId().toString());
		topic.setChildCategoryId(group.getChildCategoryId().toString());
		
		topic.setSourceName(group.getGroupName());
		topic.setSourceLogoUrl(group.getLogoUrl());
		//2015-3-12支持多张图片
//		if(!StringUtil.isBlank(image)){
//			topic.setImages(JSON2ObjUtil.getDTOList(image, Images.class));
//		}
		topic = topicRepository.save(topic);
		try {
			if (!StringUtil.isBlank(tagName)) {
				tagName = JSON2ObjUtil.getArrayFromString(tagName);
				topic.setTagNames(tagName);
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.postForObject(tagServiceUrl
						+ "tag/createTagBatch?domain=" + Config.YXTDOMAIN + "&itemId="
						+ topic.getTopicId() + "&userId=" + user.getId()
						+ "&userName=" + user.getNickName() + "&itemType="
						+ Config.TAG_TYPE_TOPIC + "&tagNames=" + tagName,null,
						String.class);
			}else{
				tagName=localTagService.getTagNamesByAnalysis(topic.getTitle()+","+topic.getContent());
				topic.setTagNames(tagName);
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.postForObject(tagServiceUrl
						+ "tag/createTagBatch?domain=" + Config.YXTDOMAIN  + "&itemId="
						+ topic.getTopicId() + "&userId=" + user.getId()
						+ "&userName=" + user.getNickName() + "&itemType="
						+ Config.TAG_TYPE_TOPIC + "&tagNames=" + tagName,null,
						String.class);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	//	topic.setTagNames(tagName);
		topic = topicRepository.save(topic);
		try {
			// 添加消息队列
			rabbitmqService.sendRegexMessage(topic.getTopicId(),
					Config.TAG_TYPE_TOPIC);
			
			//创建群组话题动态
			createGroupDynamic(topic);
		} catch (Exception e) {
			logger.error("=============发送消息队列发送错误===" + topic.getTopicId()
					+ "====类别：" + Config.TAG_TYPE_TOPIC);
			e.printStackTrace();
		}
		
	
		
		
		return topic;
	}
	
	public Topic createTopicPc(User user, Topic topic, String tagName,
			String isGeoOpen) throws XueWenServiceException {
		topic.setTagNames(tagName);
		topic.setAuthorId(user.getId());
		topic.setAuthorName(user.getNickName());
		topic.setAuthorLogoUrl(user.getLogoURL());
		long time = System.currentTimeMillis();
		topic.setCtime(time);
		topic.setUtime(time);
		topic.setViewCount(0);
		topic.setReplyCount(0);
		topic.setNewReplyCount(0);
		topic.setLikesCount(0);
		topic.setUnLikeCount(0);
		topic.setShareCount(0);
		topic.setShareCount(0);
		topic.setDisplayOrder(0);
		topic.setDigestLevel(1);
		topic.setDeleted(false);
		double[] position = new double[] { topic.getLng(), topic.getLat() };
		topic.setPosition(position);
		if (StringUtil.isBlank(topic.getPicUrl())) {
			topic.setHasImage(false);
		} else {
			topic.setHasImage(true);
		}
		if ("0".equals(isGeoOpen)) {
			topic.setGeoOpen(false);
		} else {
			topic.setGeoOpen(true);
		}
		//2015-02-05增加主题分类
		XueWenGroup group = groupService.findById(topic.getSourceId());
		topic.setCategoryId(group.getCategoryId()==null?Config.CATEFORY_DEFAULT_PRIMARY:group.getCategoryId().toString());
		topic.setChildCategoryId(group.getChildCategoryId()==null?Config.CATEFORY_DEFAULT_SENCOND:group.getChildCategoryId().toString());
		topic.setSourceName(group.getGroupName());
		topic.setSourceLogoUrl(group.getLogoUrl());
		if(StringUtil.isBlank(topic.getTagNames())){
			tagName=localTagService.getTagNamesByAnalysis(topic.getTitle()+","+topic.getContent());
		}
		topic.setTagNames(tagName);
		topic = topicRepository.save(topic);
		
		if (!StringUtil.isBlank(tagName)) {
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.postForObject(tagServiceUrl
					+ "tag/createTagBatch?domain=" + Config.YXTDOMAIN + "&itemId="
					+ topic.getTopicId() + "&userId=" + user.getId()
					+ "&userName=" + user.getNickName() + "&itemType=" + Config.TAG_TYPE_TOPIC
					+ "&tagNames=" + tagName, null,String.class);
		}
		// 添加消息队列
		try {
			rabbitmqService.sendRegexMessage(topic.getTopicId(),
					Config.TAG_TYPE_TOPIC);
			//创建群组话题动态
			createGroupDynamic(topic);
		} catch (Exception e) {
			logger.error("=============发送消息队列发送错误===" + topic.getTopicId()
					+ "====类别：" + Config.TAG_TYPE_TOPIC);
			e.printStackTrace();
		}
		return topic;
	}

//	/**
//	 * 上传主题文件，并返回主题文件的网络访问地址
//	 * 
//	 * @param file
//	 * @return
//	 * @throws XueWenServiceException
//	 */
//	public String uploadTopicFile(MultipartFile file)throws XueWenServiceException {
//		if (null != file && !file.isEmpty()) {
//			String suffile = String.valueOf(System.currentTimeMillis()); // 文件基础路径后缀（划分多文件目录，防止一个目录下有多个目录）
//			String fileLocal = getTopicPicLocal(suffile);
//			String fileUrl = getTopicPicUrl(suffile);
//			FileStoreInfo fif = myFileService.uploadFile(file, fileLocal,
//					fileUrl);
//			if (fif != null) {
//				return fif.getFileUrl();
//			}
//		}
//		return null;
//	}

	/**
	 * 得到主题文件服务器存储地址
	 * 
	 * @return
	 * @throws XueWenServiceException
	 */
//	private String getTopicPicLocal(String suffix)throws XueWenServiceException {
//		String str = myFileService.idSpilt(suffix, 5);
//		return Config.TOPICFILELOCAL + str;
//	}
//
//	/**
//	 * 得到主题文件的网络访问URL
//	 * 
//	 * @return
//	 * @throws XueWenServiceException
//	 */
//	private String getTopicPicUrl(String suffix) throws XueWenServiceException {
//		String str = myFileService.idSpilt(suffix, 5);
//		return Config.TOPICFILEURL + str;
//	}

	/**
	 * 主题点赞
	 * 
	 * @param userId
	 * @param appKey
	 * @param sourceId
	 * @param type
	 * @throws XueWenServiceException
	 */
	public Topic topicAddParise(User user, String topicId)throws XueWenServiceException {
		Topic topic = findOneById(topicId);
		if (topic == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,	null);
		}
		praiseService.addPraise(user,Config.YXTDOMAIN,topic.getAppKey(), topic.getTopicId(),Config.TYPE_TOPIC_GROUP);
		topic.setLikesCount(topic.getLikesCount()+1);
		return topicRepository.save(topic);
	}

	/**
	 * 主题点赞
	 * 
	 * @param userId
	 * @param appKey
	 * @param sourceId
	 * @param type
	 * @throws XueWenServiceException
	 */
	public Topic topicAddParisePc(User user, String topicId)throws XueWenServiceException {
		Topic topic = findOneById(topicId);
		if (topic == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		praiseService.addPraiseTip(Config.YXTDOMAIN, Config.APPKEY_PC, topicId,Config.TYPE_TOPIC_GROUP, user.getId());
		topic.setLikesCount(topic.getLikesCount() + 1);
		return topicRepository.save(topic);
	}

	/**
	 * 主题点不赞
	 * 
	 * @param userId
	 * @param appKey
	 * @param sourceId
	 * @param type
	 * @throws XueWenServiceException
	 */
	public Topic topicAddUnParise(User user, String topicId)throws XueWenServiceException {
		Topic topic = findOneById(topicId);
		if (topic == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,
					null);
		}
		unPraiseService.addUnPraise(user,Config.YXTDOMAIN,topic.getAppKey(), topic.getTopicId(),Config.TYPE_TOPIC_GROUP);
		topic.setUnLikeCount(topic.getUnLikeCount() + 1);
		return topicRepository.save(topic);
	}

	/**
	 * 主题分享
	 * 
	 * @param user
	 * @param topicId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Topic topicAddShare(User user, String topicId, String toAddr,String appKey, String toType) throws XueWenServiceException {
		Topic topic = findOneById(topicId);
		if (topic == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		shareService.addShare(user, Config.YXTDOMAIN, appKey,topic.getTopicId(), Config.TYPE_TOPIC_GROUP, toType, toAddr);
		topic.setShareCount(topic.getShareCount() + 1);
		return topicRepository.save(topic);
	}

	/**
	 * 查询一个topic
	 * 
	 * @param topicId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Topic findOneById(String topicId) throws XueWenServiceException {
		if (StringUtil.isBlank(topicId)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		return topicRepository.findByTopicIdAndIsDeleted(topicId, false);
	}
	
	/**
	 * @throws XueWenServiceException 
	 * 
	 * @auther tangli
	 * @Description: 用户手机微信推广页面获取话题详情
	 * @param userId 
	 * @param topicId
	 * @param sourceId
	 * @return JSONObject
	 * @Date:2015年4月28日
	 * @throws
	 */
	public JSONObject mobileTopicDetail(String userId,String topicId) throws XueWenServiceException{
		Topic topic=findOneById(topicId);
		// 计算该主题查看数
		
		Map<String, Object> res=new HashMap<String,Object>();
		if(topic !=null){
			topic.setViewCount(topic.getViewCount() + 1);
			topicRepository.save(topic);
			String authorId=topic.getAuthorId();
			User author=userService.findOne(authorId);
			XueWenGroup group=groupService.findById(topic.getSourceId());
			
			//封装创建者信息 id 头像 昵称 岗位
			if(author!=null){
				res.put("isHavaAuthor", true);
				res.put("authorId", author.getId());
				res.put("authorLogoUrl", author.getLogoURL());
				res.put("authorNick", author.getNickName());
				res.put("authorStation", author.getStation());
			}
			else {
				res.put("isHavaAuthor", false);
			}
			//封装群信息 ID 名称 头像
			if(group!=null){
				res.put("groupId", group.getId());
				res.put("groupName", group.getGroupName());
				res.put("groupLogoUrl", group.getLogoUrl());
			}else{
				res.put("isHavaGroup", false);
			}
			//封装用户是否已点赞
			if(!StringUtil.isBlank(userId)){
				res.put("isHavaPraise",praiseService.isUserPraise(userId,Config.YXTDOMAIN, topic.getTopicId(), Config.TYPE_TOPIC_GROUP) );
			}else {
				res.put("isHavaPraise",false);
			}
			//封装最赞回复
			List<Post>posts=postService.findPosts(topicId,3);
			res.put("posts", posts);
			return YXTJSONHelper.getInObjectAttrJsonObject(topic, res,"topicId","title","content","ctime","utime","viewCount","type","images");
			
		}
		return null;
	}

	/**
	 * 查询话题详情
	 * 
	 * @param userId
	 * @param topicId
	 * @return
	 * @throws XueWenServiceException
	 */
	public TopicResponse topicDetails(String userId,String topicId,String sourceId)throws XueWenServiceException{
		Topic topic=findOneById(topicId);
		if(topic !=null){
			TopicResponse topicRsp=new TopicResponse(topic);
			//该主题的赞用户列表
			Pageable pageable1 = new PageRequest(0,7);
			Page<Praise> praises=praiseService.findOnePraiseByDomainAndSourceId(Config.YXTDOMAIN, topic.getTopicId(), pageable1);
			Praise praise = null;
			List<UserPraiseResponse> list = new ArrayList<UserPraiseResponse>();
			if (praises != null) {
				if (praises.getContent().size() > 0) {
					List<String> userIds = new ArrayList<String>();
					for (int i = 0; i < praises.getContent().size(); i++) {
						praise = praises.getContent().get(i);
						userIds.add(praise.getUserId());
					}
					List<User> users = userService.findByIdIn(userIds);
					for (User user : users) {
						UserPraiseResponse upr = new UserPraiseResponse(praise.getUserId(),user.getUserName(),user.getLogoURL(),user.getNickName());
						list.add(upr);
					}
					topicRsp.setPraiseResponse(list);
				}
			}
			// 用户对此话题的态度 赞
			topicRsp.setLike(praiseService.isUserPraise(userId,Config.YXTDOMAIN, topic.getTopicId(), Config.TYPE_TOPIC_GROUP) ? true: false);
			// 不攒
			topicRsp.setUnlike(unPraiseService.isUserUnPraise(userId,Config.YXTDOMAIN, topic.getTopicId(), Config.TYPE_TOPIC_GROUP) ? true: false);
			// 判断该用户是否有回复权限（此人是否为该群）
			topicRsp.setAuthority(groupService.findMember(topic.getSourceId(),userId) ? true : false);
			// 获得该话题标签
			RestTemplate restTemplate = new RestTemplate();
			String tag = restTemplate.getForObject(tagServiceUrl
					+ "tag/getTagsByIdAndType?domain=" + Config.YXTDOMAIN + "&itemId="
					+ topic.getTopicId() + "&itemType=" + Config.TAG_TYPE_TOPIC, String.class);
			JSONObject objj = JSONObject.fromObject(tag);
			JSONObject obss = objj.getJSONObject("data");
			net.sf.json.JSONArray childs = obss.getJSONArray("result");
			topicRsp.setTagName(childs);
			//XueWenGroup group = groupService.findGroup(sourceId, userId);
			Object obj = groupService.toGroupHelp(sourceId, userId);
			topicRsp.setGroup(obj);
			//获得一级分类对象
//			if(topic.getCategoryId()!=null){
//			topicRsp.setCategoryId(categoryService.formateCategory(categoryService.findOneCategoryById(topic.getCategoryId())));
//			}
			//获得二级分类对象
			if(topic.getChildCategoryId()!=null){
			topicRsp.setChildCategoryId(categoryService.formateCategory(categoryService.findOneCategoryById(topic.getChildCategoryId())));
			}
			// 计算该主题查看数
			topic.setViewCount(topic.getViewCount() + 1);
			topicRepository.save(topic);

			return topicRsp;
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NODATA_201, null);
		}

	}
	
	/**
	 * 查询话题详情
	 * 
	 * @param userId
	 * @param topicId
	 * @return
	 * @throws XueWenServiceException
	 */
	public JSONObject topicDetailPc(String userId,String topicId)throws XueWenServiceException{
		Topic topic=findOneById(topicId);
		if(topic !=null){
			Map<String, Object> map = new HashMap<String, Object>();
			//该主题的赞用户列表
			Pageable pageable1 = new PageRequest(0,7);
			Page<Praise> praises=praiseService.findOnePraiseByDomainAndSourceId(Config.YXTDOMAIN, topic.getTopicId(), pageable1);
			Praise praise = null;
			List<UserPraiseResponse> list = new ArrayList<UserPraiseResponse>();
			if (praises != null) {
				if (praises.getContent().size() > 0) {
					List<String> userIds = new ArrayList<String>();
					for (int i = 0; i < praises.getContent().size(); i++) {
						praise = praises.getContent().get(i);
						userIds.add(praise.getUserId());
					}
					List<User> users = userService.findByIdIn(userIds);
					for (User user : users) {
						UserPraiseResponse upr = new UserPraiseResponse(praise.getUserId(),user.getUserName(),user.getLogoURL(),user.getNickName());
						list.add(upr);
					}
					map.put("praiseResponse", list);
				}
			}
			if(userId!=null){
				// 用户对此话题的态度 赞
				map.put("like", praiseService.isUserPraise(userId, topic.getAppKey(), topic.getTopicId(), Config.TYPE_TOPIC_GROUP) ? true : false);
				// 不攒
				map.put("unlike", unPraiseService.isUserUnPraise(userId, topic.getAppKey(), topic.getTopicId(), Config.TYPE_TOPIC_GROUP) ? true : false);
				map.put("isHavaPraise", praiseService.existByUserIdAndSourceId(topic.getTopicId(), userId));
				// 判断该用户是否有回复权限（此人是否为该群）
				map.put("authority", groupService.findMember(topic.getSourceId(), userId) ? true : false);
				// 计算该主题查看数
				topic.setViewCount(topic.getViewCount() + 1);
				topicRepository.save(topic);
			} else {
				map.put("isHavaPraise", false);

			}
			return YXTJSONHelper.addAndModifyAttrJsonObject(topic, map);

		}
			else {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NODATA_201, null);
		}
	}

	/**
	 * 查询话题详情
	 * 
	 * @param userId
	 * @param topicId
	 * @return
	 * @throws XueWenServiceException
	 */
	public TopicResponse topicDetail(String userId,String topicId)throws XueWenServiceException{
		Topic topic=findOneById(topicId);
		if(topic !=null){
			TopicResponse topicRsp=new TopicResponse(topic);
			//该主题的赞用户列表
			Pageable pageable1 = new PageRequest(0,7);
			Page<Praise> praises=praiseService.findOnePraiseByDomainAndSourceId(Config.YXTDOMAIN, topic.getTopicId(), pageable1);
			Praise praise = null;
			List<UserPraiseResponse> list = new ArrayList<UserPraiseResponse>();
			if (praises != null) {
				if (praises.getContent().size() > 0) {
					List<String> userIds = new ArrayList<String>();
					for (int i = 0; i < praises.getContent().size(); i++) {
						praise = praises.getContent().get(i);
						userIds.add(praise.getUserId());
					}
					List<User> users = userService.findByIdIn(userIds);
					for (User user : users) {

						UserPraiseResponse upr = new UserPraiseResponse(praise.getUserId(),user.getUserName(),user.getLogoURL(),user.getNickName());
						list.add(upr);
					}
					topicRsp.setPraiseResponse(list);
				}
			}

			// 用户对此话题的态度 赞
			topicRsp.setLike(praiseService.isUserPraise(userId,topic.getAppKey(), topic.getTopicId(), Config.TYPE_TOPIC_GROUP) ? true: false);
			// 不攒
			topicRsp.setUnlike(unPraiseService.isUserUnPraise(userId,topic.getAppKey(), topic.getTopicId(), Config.TYPE_TOPIC_GROUP) ? true: false);
			// 判断该用户是否有回复权限（此人是否为该群）
			topicRsp.setAuthority(groupService.findMember(topic.getSourceId(),userId) ? true : false);
			// 获得该话题标签
			RestTemplate restTemplate = new RestTemplate();
			String tag = restTemplate.getForObject(tagServiceUrl
					+ "tag/getTagsByIdAndType?domain=" + Config.YXTDOMAIN + "&itemId="
					+ topic.getTopicId() + "&itemType=" + Config.TAG_TYPE_TOPIC, String.class);
			JSONObject objj = JSONObject.fromObject(tag);
			JSONObject obss = objj.getJSONObject("data");
			net.sf.json.JSONArray childs = obss.getJSONArray("result");
			topicRsp.setTagName(childs);
			//获得一级分类对象
//			if(topic.getCategoryId()!=null){
//			topicRsp.setCategoryId(categoryService.formateCategory(categoryService.findOneCategoryById(topic.getCategoryId())));
//			}
			//获得二级分类对象
			if(topic.getChildCategoryId()!=null){
			topicRsp.setChildCategoryId(categoryService.formateCategory(categoryService.findOneCategoryById(topic.getChildCategoryId())));
			}
			// 计算该主题查看数
			topic.setViewCount(topic.getViewCount() + 1);
			topicRepository.save(topic);
			return topicRsp;
		} else {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NODATA_201, null);
		}

	}

	/**
	 * 根据群组或者课程来查询主题列表
	 * 
	 * @param groupId
	 * @param courseId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<Topic> findTopicByGroupIdOrCourseId(String groupId,String appKey, String courseId, Pageable pageable)throws XueWenServiceException {
		if(StringUtil.isBlank(groupId)){
			throw new XueWenServiceException(Config.STATUS_601, Config.MSG_NOGROUP_601,null);
		}
		// 如果课程ID为空，则查询的为群话题
		if (StringUtil.isBlank(courseId)) {
			// 过滤掉需要申请加入群的话题
			return topicRepository.findBySourceIdAndIsDeleted(groupId, false, pageable);
		} else { // 如果课程ID不为空,则查询该课程的话题
			return topicRepository.findByCourseIdAndIsDeleted(courseId, false,pageable);
		}
	}

	/**
	 * 
	 * @Title: findByGroupId
	 * @Description: 查询群top10话题
	 * @param groupId
	 * @param pageable
	 * @param appKey
	 * @return
	 * @throws XueWenServiceException
	 *             Page<Topic>
	 * @throws
	 */
	public Page<Topic> findByGroupId(String groupId, Pageable pageable,String appKey) throws XueWenServiceException {
		if (StringUtil.isBlank(groupId)) {
			throw new XueWenServiceException(Config.STATUS_201, "群id不能为空", null);
		}
		if (StringUtil.isBlank(appKey)) {
			throw new XueWenServiceException(Config.STATUS_201, "appKey不能为空",null);
		}
		Page<Topic> topics = topicRepository.findBySourceIdAndIsDeleted(groupId, false,pageable);
		return topics;
	}
	
	
	
	/**
	 * 
	 * @Title: findByGroupId
	 * @Description: 查询话题
	 * @param groupId
	 * @param pageable
	 * @param appKey
	 * @return
	 * @throws XueWenServiceException
	 *             Page<Topic>
	 * @throws
	 */
	public Topic findByGroupId(String groupId) throws XueWenServiceException {
		if (StringUtil.isBlank(groupId)) {
			throw new XueWenServiceException(Config.STATUS_201, "群id不能为空", null);
		}
		 
		Topic topics = topicRepository.findByTopicIdAndIsDeleted(groupId, false);
		return topics;
	}
	
	
	/**
	 * 
	 * @Title: findByGroupId
	 * @Description: 查询话题
	 * @param groupId
	 * @param pageable
	 * @param appKey
	 * @return
	 * @throws XueWenServiceException
	 *             Page<Topic>
	 * @throws
	 */
	public List<Topic> findBychildCategoryId(String groupId) throws XueWenServiceException {
		 
		List<Topic> topics = topicRepository.findByChildCategoryIdAndIsDeleted(groupId, false);
		return topics;
	}
	

	/**
	 * 
	 * @Title: shoutPageTopicForGroupSpace
	 * @Description: 群空间top10话题瘦身
	 * @param topics
	 * @return List<JSONObject>
	 * @throws
	 */
	public List<JSONObject> shoutPageTopicForGroupSpace(Page<Topic> topics) {
		List<Topic> topbuffer = topics.getContent();
		List<JSONObject> objs = new ArrayList<JSONObject>();
		for (Topic topic : topbuffer) {
			objs.add(YXTJSONHelper.getInObjectAttrJsonObject(topic,new HashMap<String, Object>(), new String[] { "topicId","title", "authorName","content" }));
		}
		return objs;
	}


	/**
	 * 将topic转成topicResponse
	 * 
	 * @param subs
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<TopicResponse> toResponses(List<Topic> topic)throws XueWenServiceException {
		List<TopicResponse> topicRes = new ArrayList<TopicResponse>();
		if (topic == null || topic.size() <= 0) {
			// logger.info("=====无返回符合条件话题");
		} else {
			for (int i = 0; i < topic.size(); i++) {
				topicRes.add(toResponse(topic.get(i)));
			}
		}
		return topicRes;
	}
	
	/**
	 * 根据位置中的话题列表返回话题列表
	 * @param boxs
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<Object> toBoxResponses(List<Box> boxs)throws XueWenServiceException {
		List<Object> topicRes = new ArrayList<Object>();
		List<Object> topicId = new ArrayList<Object>();
		if(boxs !=null && boxs.size()>0){
			for(Box box:boxs){
				topicId.add(box.getSourceId().toString());
			}
				List<Topic> topics = topicRepository.findByTopicIdIn(topicId);
				Topic topic = null;
				for(int i = 0 ; i < topics.size();i++){
					topic = topics.get(i);
					if(topic !=null){
						topicRes.add(formateTopic(topic));
					}
				}
				//Topic topic=findOneById(box.getSourceId().toString());
				
		}
		return topicRes;
	}

	/**
	 * 格式化topic
	 * @param topic
	 * @return
	 * @throws XueWenServiceException
	 */
	public Object formateTopic(Topic topic)throws XueWenServiceException{
		String[] exclude = {"post","praiseResponse","position","tagName","group","categoryId","childCategoryId"};
		return YXTJSONHelper.excludeAttrJsonObject(toResponse(topic), exclude);
	}
	
	/**
	 * 单个对象转换成前端对象
	 * 
	 * @param topic
	 * @return
	 * @throws XueWenServiceException
	 */
	public TopicResponse toResponse(Topic topic) throws XueWenServiceException {
		if (topic == null) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NODATA_201, null);
		}
		RestTemplate restTemplate = new RestTemplate();
		String tag = restTemplate.getForObject(tagServiceUrl
				+ "tag/getTagsByIdAndType?domain=" + Config.YXTDOMAIN + "&itemId="
				+ topic.getTopicId() + "&itemType=" + Config.TAG_TYPE_TOPIC, String.class);
		JSONObject objj = JSONObject.fromObject(tag);
		JSONObject obss = objj.getJSONObject("data");
		net.sf.json.JSONArray childs = obss.getJSONArray("result");
		TopicResponse topicRespone = new TopicResponse(topic);
		topicRespone.setTagName(childs);
		return topicRespone;
	}

	/**
	 * 获得该群组下的主题的数量
	 * 
	 * @param sourceId
	 * @param appKey
	 * @return
	 */
	public int getTopicCount(String sourceId) {
		return topicRepository.countBySourceIdAndIsDeleted(sourceId, false);
	}

	/**
	 * 发现话题（只查询6个）
	 * 
	 * @param groupId
	 * @param courseId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<Topic> findAll(String userId, Pageable pageable)throws XueWenServiceException {
		List<Object> groupIsOpen = groupService.findGroupByIsOpen(userId);
		Page<Topic> topicSub = topicRepository.findBySourceIdInAndIsDeleted(groupIsOpen, false, pageable);
		return topicSub;
	}
	/**
	 * 查询所有话题
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<Topic> findAll()throws XueWenServiceException{
		return topicRepository.findAll();
	}

	/**
	 * 发现话题（只查询6个）
	 * 
	 * @param groupId
	 * @param courseId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<Topic> findAllByUserID(String userId, Pageable pageable)throws XueWenServiceException {
		Page<Topic> topicSub = topicRepository.findByAuthorIdAndIsDeleted(userId, false, pageable);
		return topicSub;
	}

	/**
	 * 发现话题（只查询6个，根据我所在的群组ID集合，过滤出本人不在的群组）
	 * 
	 * @param userId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<Topic> findAll(Pageable pageable) throws XueWenServiceException {
		Page<Topic> topicSub = topicRepository.findByIsDeleted( false, pageable);
		return topicSub;
	}

	/**
	 * 查询所有话题列表
	 * 
	 * @param p
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<Topic> all(Pageable pageable) throws XueWenServiceException {
		List<Object> groupIsOpen = groupService.findGroupByIsOpen();
		Page<Topic> topic = topicRepository.findBySourceIdInAndIsDeleted(groupIsOpen, false, pageable);
		return topic;
	}

	/**
	 * 根据标题或者内容搜索匹配话题列表
	 * 
	 * @param p
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<Topic> search(String titleOrContent, Pageable pageable)throws XueWenServiceException {
	//	List<Object> groupIsOpen = groupService.findGroupByIsOpen();
		Page<Topic> topic = null;

		if (StringUtil.isBlank(titleOrContent)) {
			// 如果titleOrContent为空，则搜索全部
			//topic = topicRepository.findBySourceIdInAndIsDeleted(groupIsOpen,false, pageable);
			topic = topicRepository.findByIsDeleted(  false, pageable);
		} else {
			// 按条件搜索
			titleOrContent = ".*?(?i)" + titleOrContent + ".*";
			//topic = topicRepository.findBySourceIdInAndIsDeletedAndTitleRegexOrSourceIdInAndIsDeletedAndContentRegex(groupIsOpen, false, titleOrContent, groupIsOpen,false, titleOrContent, pageable);
			topic = topicRepository.findByIsDeletedAndTitleRegexOrIsDeletedAndContentRegex(false, titleOrContent, false, titleOrContent, pageable);
		}
		return topic;
	}
	/**
	 * 根据标题或者内容搜索匹配话题列表
	 * 
	 * @param p
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<Topic> searchByGroupId(String groupId,String titleOrContent, Pageable pageable)throws XueWenServiceException {
//		List<Object> groupIsOpen = groupService.findGroupByIsOpen();
		Page<Topic> topic = null;

		if (StringUtil.isBlank(titleOrContent)) {
			// 如果titleOrContent为空，则搜索全部
			topic = topicRepository.findBySourceIdAndIsDeleted(groupId,false, pageable);
		} else {
			// 按条件搜索
			titleOrContent = ".*?(?i)" + titleOrContent + ".*";
			topic = topicRepository.findBySourceIdAndIsDeletedAndTitleRegexOrSourceIdAndIsDeletedAndContentRegex(groupId, false, titleOrContent, groupId,false, titleOrContent, pageable);
		}
		return topic;
	}
	/**
	 * 根据标题或者内容搜索匹配话题列表(把没有关联群组的话题也全部查询出来）
	 * 
	 * @param p
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<Topic> searchnochecktopic(String titleOrContent, Pageable pageable)throws XueWenServiceException {
		Page<Topic> topic = null;
		if (StringUtil.isBlank(titleOrContent)) {
			// 如果titleOrContent为空，则搜索全部
			topic = topicRepository.findByIsDeletedAndReview(false,false,pageable);
		} else {
			// 按条件搜索
			titleOrContent = ".*?(?i)" + titleOrContent + ".*";
			topic = topicRepository.findByTitleRegexAndIsDeletedAndReviewOrContentRegexAndIsDeletedAndReview(titleOrContent,false,false, titleOrContent, false,false,pageable);
		}
		return topic;
	}
	
	
	
	/**
	 * 根据标题或者内容搜索匹配话题列表(把没有关联群组的话题也全部查询出来）
	 * 
	 * @param p
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<Topic> searchAll(String titleOrContent, Pageable pageable)throws XueWenServiceException {
		Page<Topic> topic = null;
		if (StringUtil.isBlank(titleOrContent)) {
			// 如果titleOrContent为空，则搜索全部
			topic = topicRepository.findByIsDeleted(false,pageable);
		} else {
			// 按条件搜索
			titleOrContent = ".*?(?i)" + titleOrContent + ".*";
			topic = topicRepository.findByTitleRegexAndIsDeletedOrContentRegexAndIsDeleted(titleOrContent,false, titleOrContent, false,pageable);
		}
		return topic;
	}
	
	
	

	/**
	 * 初始化 建立2d位置索引
	 * 
	 * @param racBeinfo
	 */
	public void creatTable() {
		mongoTemplate.indexOps(Topic.class).ensureIndex(new GeospatialIndex("position"));
	}

//	/**
//	 * 上传主题文件，并返回主题文件的网络访问地址
//	 * 
//	 * @param file
//	 * @return
//	 * @throws XueWenServiceException
//	 */
//	public FileStoreInfo uploadFile(MultipartFile file)throws XueWenServiceException {
//		if (null != file && !file.isEmpty()) {
//			String suffile = String.valueOf(System.currentTimeMillis()); // 文件基础路径后缀（划分多文件目录，防止一个目录下有多个目录）
//			String fileLocal = getTopicPicLocal(suffile);
//			String fileUrl = getTopicPicUrl(suffile);
//			FileStoreInfo fif = myFileService.uploadFile(file, fileLocal,
//					fileUrl);
//			return fif;
//		}
//		return null;
//	}

	/**
	 * 查询所有话题列表
	 * 
	 * @param p
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<Topic> findAlTopic(Pageable pageable)throws XueWenServiceException {
		Page<Topic> topic = topicRepository.findByIsDeletedAndReview(false,true, pageable);
		return topic;
	}

	/**
	 * 创建话题标签
	 * 
	 * @param tagName
	 * @return
	 */
	public UserTagBean createUserTag(User user, Topic topic, String tagName) {
		UserTagBean utb = new UserTagBean();
		utb.setUserId(user.getId());
		utb.setUserName(user.getUserName());
		utb.setItemId(topic.getTopicId());
		utb.setItemType(Config.TAG_TYPE_TOPIC);
		utb.setCtime(String.valueOf(System.currentTimeMillis()));
		utb.setTagName(tagName);
		return utb;

	}

	public long getCountsByGroupId(String groupId) throws  XueWenServiceException{
		return topicTemplate.getCountsByGroupId(groupId);
	}
	/**
	 * 
	 * @Title: getCountsByUserId
	 * @Description: 根据用户ID统计群组下话题数量
	 * @param userId
	 * @return
	 * @throws XueWenServiceException long
	 * @throws
	 */
	public long getCountsByUserId(String userId) throws  XueWenServiceException{
		return topicTemplate.getCountsByUserId(userId);
	}

	/**
	 * 查询附近的话题
	 * 
	 * @param p
	 * @return
	 */
	public List<TopicResponse> findAllNearTopic(String userId, Point p,Distance dis) throws XueWenServiceException {
		if (null == p) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_POSITION_201, null);
		}
		List<Topic> topics = topicRepository.findByPositionNearAndIsDeletedAndIsGeoOpen(p, false, true, dis);
		List<TopicResponse> topicRes = new ArrayList<TopicResponse>();
		for (int i = 0; i < topics.size(); i++) {
			Topic topic = topics.get(i);
			String topicId = topic.getTopicId();
			Set<String> tagName = tagService.getTagsByItemIdItemType(topicId,Config.TAG_TYPE_TOPIC);
			TopicResponse topicRespone = new TopicResponse(topic);
			topicRespone.setTagName(tagName);
			topicRespone.setDistance(StringUtil.Distance(p.getX(), p.getY(),topic.getLng(), topic.getLat()));
			topicRes.add(toResponse(topics.get(i)));
		}
		return topicRes;

	}

	/**
	 * 话题数量
	 * 
	 * @return
	 */
	public long topicCount() {
		long topicCount = topicRepository.countByIsDeleted(false);
		return topicCount;
	}

	/**
	 * 小组解散后将小组下的话题调整成删除
	 * 
	 * @param groupId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public void changeTopicByGroupId(String groupId)throws XueWenServiceException {
		topicTemplate.updatePostIsDeleteBySourceId(groupId);
	}

	/**
	 * 
	 * @Title: setTopicBarCode
	 * @Description: 给话题添加二维码地址
	 * @param topicId
	 * @param url
	 * @throws XueWenServiceException
	 *             void
	 * @throws
	 */
	public void setTopicBarCode(String topicId, String url)throws XueWenServiceException {
		if (StringUtil.isBlank(url)) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_KNGMODIFY_201, null);
		}
		Topic topic = topicRepository.findOne(topicId);
		topic.setBarCode(url);
		topicRepository.save(topic);
	}

	/**
	 * 
	 * @Title: setTopicBarCode
	 * @Description: 给话题添加二维码地址
	 * @param topicId
	 * @param url
	 * @throws XueWenServiceException
	 *             void
	 * @throws
	 */
	public Topic updateTopic(Topic topic) throws XueWenServiceException {
		String tagName=topic.getTagNames();
//		if (!StringUtil.isBlank(tagName)) {
			//tagName = JSON2ObjUtil.getArrayFromString(tagName);
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.postForObject(tagServiceUrl
					+ "tag/editTagsDelAdd?domain=" + "yxt" + "&itemId="
					+ topic.getTopicId() + "&itemType=" + Config.TAG_TYPE_TOPIC + "&tagNames="
					+ tagName + "&userId=" + topic.getAuthorId() + "&userName="
					+ topic.getAuthorName(), null, String.class);
			
			if(topic.getSourceId()!=null&&!("".equals(topic.getSourceId()))){
				GroupDynamic g=groupDynamicRepository.findByGroupIdAndSourceId(topic.getSourceId().toString(),topic.getTopicId());
				if(g!=null)
				{
					List<Images> l=new ArrayList<Images>();
					g.setTitle(topic.getTitle());
					g.setContent(topic.getContent());
					if(topic.getImages()!=null){
						if(topic.getImages().size()!=0){
							l=topic.getImages();
						}
						
					}
//					if(!("".equals(topic.getPicUrl()))||topic.getPicUrl()!=null){
//						Images images=new Images();
//						images.setPicHeight(topic.getPicHeight());
//						images.setPicUrl(topic.getPicUrl());
//						images.setPicWidth(topic.getPicWidth());
//						l.add(images);
//					}
					g.setImages(l);
					groupDynamicRepository.save(g);
				}
			}
//}
		return topicRepository.save(topic);
	}

	/**
	 * 添加用户收藏话题记录
	 * 
	 * @param courseId
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Topic favTopic(String topicId, String userId)throws XueWenServiceException {
		Topic topic = topicRepository.findOne(topicId);
		if (topic == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		favService.addFavPc(Config.YXTDOMAIN, Config.APPKEY_PC, topicId,Config.TYPE_TOPIC_GROUP, userId);
		topic.setFavoritesCount(topic.getFavoritesCount() + 1);
		return topicRepository.save(topic);
	}

	/**
	 * @throws XueWenServiceException 
	 * 
	 * @Title: myCreatedTopic
	 * @Description: 我创建的话题
	 * @param id
	 * @param pageable void
	 * @throws
	 */
	public Page<Topic> myCreatedTopic(String userId, Pageable pageable) throws XueWenServiceException {
		if(StringUtils.isBlank(userId)){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		Page<Topic> topics = topicRepository.findByAuthorIdAndIsDeleted(userId,false,pageable);
		return topics;
	}
	
	/**
	 * 
	 * @Title: getGroupTopPc
	 * @Description: 取群话题列表
	 * @param groupId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 *             Map<String,Object>
	 * @throws
	 */
	public Map<String, Object> getGroupTopPc(String groupId,String keyWords, Pageable pageable)throws XueWenServiceException {
		if (StringUtil.isBlank(groupId)) {
			throw new XueWenServiceException(Config.STATUS_201, "群Id不能为空", null);
		}
		//Page<Topic> topics = topicRepository.findBySourceIdAndIsDeleted(groupId, false, pageable);
		//支持搜索
		Page<Topic> topics = topicRepository.searchBySourceId(false, keyWords, groupId, pageable);
		List<String> topids = new ArrayList<String>();
		for (Topic topic : topics) {
			topids.add(topic.getTopicId());
		}
		List<JSONObject> objs=shoutlistforpc(topics.getContent());
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("topics", topics);
		result.put("objs", objs);
		return result;

	}

	/**
	 * 
	 * @Title: shoutlistforpc
	 * @Description: 话题列表瘦身
	 * @param resref
	 *            void
	 * @throws
	 */
	public  List<JSONObject> shoutlistforpc(List<Topic> topics) {
		List<JSONObject> res = new ArrayList<JSONObject>();
		for (Topic topic : topics) {
			//Post post = postService.findOneByTopicId(topic.getTopicId());
			Post post = postService.findOneByTopicId(topic.getTopicId());
			XueWenGroup group=groupService.findById(topic.getSourceId());
			Map<String, Object> postM = new HashMap<String, Object>();
			if (post!= null) {
				postM.put("ishavepost", true);
				postM.put("postId", post.getPostId());
				postM.put("postauthorName", post.getAuthorName());
				postM.put("postauthorId", post.getAuthorId());
				postM.put("postauthorLogoUrl", post.getAuthorLogoUrl());
				postM.put("postmessage", post.getMessage());
				postM.put("postctime", post.getCtime());
				postM.put("postutime", post.getUtime());
				postM.put("posttype", post.getType());
				postM.put("postfileurl", post.getFileUrl());
			}
			else{
				postM.put("ishavepost", false);
			}
			JSONObject obj = YXTJSONHelper.getInObjectAttrJsonObject(topic,
					postM, new String[] {"sourceName","picUrl", "topicId","title", "content",
							"ctime", "utime", "viewCount", "replyCount" ,"likesCount","sourceId","authorId","authorName","authorLogoUrl","likesCount","unLikeCount","shareCount","displayOrder"});
			res.add(obj);
		}
		return res;
	}

	/**
	 * 
	 * @Title: getSearchGroupTopPc
	 * @Description: 搜索话题列表
	 * @param groupId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException Map<String,Object>
	 * @throws
	 */
	public Map<String, Object> getSearchGroupTopPc(String keyWords, Pageable pageable)
			throws XueWenServiceException {
		if (StringUtil.isBlank(keyWords)) {
			keyWords = "";
		}
		//Page<Topic> topics = topicRepository.findByIsDeletedAndTitleLikeOrContentLikeOrTagNamesLike(false,keyWords,keyWords,keyWords,pageable);
		Page<Topic> topics = topicRepository.search(false,keyWords,pageable);

		List<String> topids = new ArrayList<String>();
		for (Topic topic : topics) {
			topids.add(topic.getTopicId());
		}
		List<JSONObject> objs=shoutlistforpc(topics.getContent());
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("topics", topics);
		result.put("objs", objs);
		return result;
	}
	
	/**
	 * 根据话题列表，返回话题Id列表
	 * @param topics
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<Object> findTopicIdsByTopics(List<Topic> topics)throws XueWenServiceException{
		if(topics != null){
			List<Object> topicIds=new ArrayList<Object>();
			for(Topic topic:topics){
				topicIds.add(topic.getTopicId());
			}
			return topicIds;
		}else{
			return null;
		}
	}
	
	/**
	 * 根据话题ID删除所有的话题
	 * @param topicId
	 * @throws XueWenServiceException
	 */
	public void deleteById(String topicId)throws XueWenServiceException{
		//删除所有的赞记录
		praiseService.deleteBySourceId(topicId);
		//删除所有的不攒接口
		unPraiseService.deleteBySourceId(topicId);
		//删除所有的分享记录
		shareService.deleteBySourceId(topicId);
		//删除所有的评论(包括主楼评论，副楼评论)
		postService.deleteByTopicId(topicId);
		//删除话题记录
		Topic t=findOneById(topicId);
		t.setDeleted(true);
		savetopic(t);
		//topicTemplate.deleteById(topicId);
		//删除排行榜记录

		boxTemplate.deleteBysourceId(topicId);
		
		//删除话题动态
		groupDynamicTemplate.deleteBySourceIdId(topicId);


		
	}
	
	/**
	 * 根据话题ID删除所有的话题
	 * @param topicId
	 * @throws XueWenServiceException
	 */
	public boolean deleteByIds(String topicIds)throws XueWenServiceException{
		String[] ids;
		if(!StringUtil.isBlank(topicIds)){
			ids=topicIds.split(",");
		}else{
			throw new XueWenServiceException(Config.STATUS_201, "请选择需要删除的话题", null);
		}
		try {
			List<Object> idList=new ArrayList<Object>();
			for (String id : ids) {
				idList.add(id);
				//删除所有的评论(包括主楼评论，副楼评论)
				postService.deleteByTopicId(id);
			}
			//删除所有的赞记录
			praiseService.deleteBySourceIds(idList);
			//删除所有的不攒接口
			unPraiseService.deleteBySourceIds(idList);
			//删除所有的分享记录
			shareService.deleteBySourceIds(idList);
			//收藏话题删除记录
			favService.deleteBySourceIds(idList);
			//删除话题记录
			topicTemplate.deleteByIds(idList);
			//删除排行榜
			boxTemplate.deleteBySourceIds(idList);
			//删除动态
			groupDynamicService.deleteGroupDynamics(idList);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 根据topicId将主楼回复的统计数量递增相应的数值 话题
	 * @param topicId
	 * @throws XueWenServiceException
	 */
	public void increasingPostCount(String topicId,int inc)throws XueWenServiceException{
		topicTemplate.increasingPostCountByTopicId(topicId, inc);
	}
	/**
	 * 根据topicId将主楼回复的统计数量递增相应的数值 干货
	 * @param topicId
	 * @throws XueWenServiceException
	 */
	public void increasingPostCountByDry(String topicId,int inc)throws XueWenServiceException{
		topicTemplate.increasingPostCountByDryId(topicId, inc);
	}
	/**
	 * 通过群ID查询List<Topic>
	 * @param groupId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<Topic> getTopics(String groupId)throws  XueWenServiceException{
		return topicRepository.findBySourceIdAndIsDeleted(groupId,false);
	}
	/**
	 * 测试方法，将title填充值
	 */
	public void getTopics(){
		List<Topic> topics = topicRepository.findAll();
		String content = "";
		String title = "";
		Topic one = null;
		for(int i = 0; i < topics.size(); i++){
			one = topics.get(i);
			content = one.getContent();
			title =one.getTitle();
			if(StringUtil.isBlank(title)){
				one.setTitle(StringUtil.getStr(content));
				topicRepository.save(one);
			}
		}
	}
	
	/**
	 * 通过分类查询话题列表
	 * 
	 * @param p
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<Topic> findTopicsByCategoryId(String categoryId,String childCategoryId,Pageable pageable)throws XueWenServiceException {
		Page<Topic> topic = null;
		if(!StringUtil.isBlank(childCategoryId)){
			topic = topicRepository.findByIsDeletedAndChildCategoryId(false,childCategoryId, pageable);
		}else{
			topic = topicRepository.findByIsDeletedAndCategoryId(false,categoryId, pageable);
		}
		return topic;
	}
	
	/**
	 * 通过一级分类Id计算该分类下的主题数量
	 * 
	 * @param p
	 * @return
	 * @throws XueWenServiceException
	 */
	public int countByCategoryId(String categoryId)throws XueWenServiceException {
		return topicRepository.countByIsDeletedAndCategoryId(false, categoryId);
	}
	
	/**
	 * 通过二级分类Id计算该分类下的主题数量
	 * 
	 * @param p
	 * @return
	 * @throws XueWenServiceException
	 */
	public int countByChildCategoryId(String childCategoryId)throws XueWenServiceException {
		return topicRepository.countByIsDeletedAndChildCategoryId(false, childCategoryId);
	}
	/**
	 * 给主题添加该群的分类
	 * @throws XueWenServiceException
	 */
	public void addCategoryForTopic()throws XueWenServiceException {
		List<Topic> topicList = topicRepository.findAll();
		Topic topic = null;
		XueWenGroup group = null;
		if(topicList!=null && topicList.size() > 0){
			for(int i = 0 ; i < topicList.size(); i++){
				topic = topicList.get(i);
				group =  groupService.findById(topic.getSourceId());
				if(group==null){
					continue;
				}
				//if(StringUtil.isBlank(topic.getCategoryId())){
				topic.setCategoryId(group.getCategoryId().toString());
				topic.setChildCategoryId(group.getChildCategoryId().toString());
				topicRepository.save(topic);
				//}
			}
		}
	}
	
	
	/**
	 * 分页获取不在此位置的话题列表
	 * @param boxPostId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<Topic> findByBoxPostIdNotInBox(String boxPostId,Pageable pageable)throws XueWenServiceException{
		List<Object> ids=boxService.getSourceIdsByBoxPostId(boxPostId);
		return topicRepository.findByIsDeletedAndTopicIdNotIn(false,ids, pageable);
	}
	
	
	/**
	 * 分页获取不在此位置的话题列表
	 * @param boxPostId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<Topic> findByBoxPostIdNotInBoxForSearch(String boxPostId,String keyword)throws XueWenServiceException{
		List<Object> ids=boxService.getSourceIdsByBoxPostId(boxPostId);
		return topicRepository.findByTitleRegexAndIsDeletedAndTopicIdNotInOrContentRegexAndIsDeletedAndTopicIdNotIn(keyword,false,ids,keyword,false,ids);
	}
	
	/**
	 * 
	 * @Title: findByIdIn
	 * @auther Tangli
	 * @Description: 通过ids取
	 * @param ids
	 * @return List<Topic>
	 * @throws
	 */
	public List<Topic> findByIdIn(List<Object> ids) {
		return topicRepository.findByTopicIdIn(ids);
	}
	/**
	 * 支持话题创建多图片（调整图片）
	 * @throws XueWenServiceException
	 */
	public void updateImage() throws XueWenServiceException{
		List<Topic> topicList = topicRepository.findByIsDeletedAndPicUrlNotNullAndImagesIsNull(false);
		Topic topic = null;
		
		if(topicList!=null && topicList.size()>0){
	
			for(int i =0 ; i < topicList.size(); i++){
				topic = topicList.get(i);
				Images images = new Images();
				List<Images> imagesList = new ArrayList<Images>();
				images.setPicUrl(topic.getPicUrl());
				images.setPicWidth(topic.getPicWidth());
				images.setPicHeight(topic.getPicHeight());
				imagesList.add(images);
				topic.setImages(imagesList);
				topicRepository.save(topic);
			}
		}
	}
	
	/**
	 * 处理话题创建图片（为了支持低版本）
	 * @param image（1.2新版本创建）
	 * @param picUrl（1.1旧版本创建）
	 * @return
	 * @throws XueWenServiceException
	 */
	public Topic changePicUrlToImage(Topic topic,String image,String picUrl)  throws XueWenServiceException{
		//如果image不为空，代表新版本创建
		if(!StringUtil.isBlank(image)){
			List<Images> imageList = JSON2ObjUtil.getDTOList(image, Images.class);
			Images images = null;
			if(imageList!=null && imageList.size() > 0){
				topic.setImages(imageList);
					images = imageList.get(0);
					topic.setPicUrl(images.getPicUrl());
					topic.setPicWidth(images.getPicWidth());
					topic.setPicHeight(images.getPicHeight());
			}
			topic.setHasImage(true);
			return topic;
		}
		//如果picUrl不是空 则代表从旧版本创建
		if(!StringUtil.isBlank(picUrl)){
			Images images = new Images();
			images.setPicUrl(picUrl);
			images.setPicHeight(topic.getPicHeight());
			images.setPicWidth(topic.getPicWidth());
			List<Images> imageList  = new ArrayList<Images>();
			imageList.add(images);
			topic.setImages(imageList);
			topic.setHasImage(true);
			return topic;
		}
		topic.setHasImage(false);
		return topic;
		
	}
	/**
	 * 排行榜-精彩话题（按照话题创建时间倒序）
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<Topic> findWonderfulTopic(Pageable pageable) throws XueWenServiceException{
		return topicRepository.findByIsDeletedAndReview(false, true,pageable);
		
	}
	/**
	 * 将topic转成JSON
	 * 
	 * @param subs
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<Object> toJSONHelper(List<Topic> topic,User user)throws XueWenServiceException {
		List<Object> topicRes = new ArrayList<Object>();
		if (topic == null || topic.size() <= 0) {
		} else {
			for (int i = 0; i < topic.size(); i++) {
				Map<String,Object> addAndModifyMap=new HashMap<String, Object>();
				String[] exclude = {"post","praiseResponse","position","group","categoryId","childCategoryId"};
				topicRes.add(YXTJSONHelper.getExObjectAttrJsonObject(toHelp(topic.get(i),user), addAndModifyMap, exclude));
			}
		}
		return topicRes;
	}
	
	
	public Page<Topic> findByIdIn(List<Object> ids, Pageable pageable) {
		return topicRepository.findByTopicIdIn(ids,pageable);
	}
	
	/**
	 * 话题存储
	 * 
	 * @param userId
	 * @param appKey
	 * @param sourceId
	 * @param type
	 * @throws XueWenServiceException
	 */
	public Topic savetopic(Topic topic)throws XueWenServiceException {
		 
		return topicRepository.save(topic);
	}
	/**
	 * 将话题的创建者由fromUser 改为toUser
	 * @param fromUserId
	 * @param toUserId
	 * @param toUserNickName
	 * @param toUserLogoUrl
	 * @throws XueWenServiceException
	 */
	public void mergeTopic(String fromUserId,String toUserId,String toUserNickName,String toUserLogoUrl)throws XueWenServiceException{
		topicTemplate.mergeTopic(fromUserId, toUserId, toUserNickName, toUserLogoUrl);
	}
	
	/**
	 * 
	 * @Title: countByCuser
	 * @auther Tangli
	 * @Description: 获取用户话题数量
	 * @param userId
	 * @return int
	 * @throws
	 */
	public int countByCuser(String userId) {
		return topicRepository.countByAuthorId(userId);
	}
	
	/**
	 * 创建群组话题动态
	 * @param topic
	 * @throws XueWenServiceException
	 */
	public void createGroupDynamic(Topic topic)throws XueWenServiceException{
		if(topic !=null && !StringUtil.isBlank(topic.getSourceId()) ){
			groupDynamicService.addGroupDynamic(topic.getSourceId(), "","",topic.getTopicId(), topic.getTitle(), topic.getContent(),
					"", topic.getImages(),topic.getAuthorId(), topic.getAuthorName(), topic.getAuthorLogoUrl(), Config.TYPE_TOPIC_GROUP,
					topic.getCtime());
		}
		
	}
	
	/**
	 * 批量生成已有动态,OSS使用
	 * @throws XueWenServiceException
	 */
	public void createOldGroupTopicDynamic()throws XueWenServiceException{
		List<Topic> topics=findAll();
		List<Object> ids=new ArrayList<Object>();
		for(Topic topic:topics){
			if(topic.isDeleted()){
				ids.add(topic.getTopicId());
			}
		}
		groupDynamicService.deleteGroupDynamics(ids);
	}

	public void increasingFavCount(String sourceId, int i) {
		topicTemplate.increasingFavCountByTopicId(sourceId, i);
		
		
	}
	/**
	 * 话题置顶
	 * @param topicId
	 * @param currentUser
	 * @return
	 */
	public TopicResponse display(String topicId, User currentUser) throws XueWenServiceException{
		Topic topic=findOneById(topicId);//获得话题对象
		//判断该群已经置顶的话题数量如果大于3则不允许置顶
		int displayCount = this.doDisplayCount(topic);
		if(displayCount>=Config.DISPLAYCOUNT){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_DISPALYCOUNT_201,null);
		}
		boolean admin = groupService.isGroupAdmin(topic.getSourceId(), currentUser.getId());//判断该用户是否为该群的管理员
		boolean owner = groupService.isGroupOwner(currentUser.getId(), topic.getSourceId());//判断该用户是否为该群的创建员
		if(admin==false && owner==false){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_DISPALY_201,null);
		}
		topic.setDisplayOrder(Config.DISPALY);
		topic.setDisplayTime(System.currentTimeMillis());
		topicRepository.save(topic);
		TopicResponse tr = new TopicResponse(topic);
		return tr;
	}
	
	/**
	 * 话题取消置顶
	 * @param topicId
	 * @param currentUser
	 * @return
	 */
	public TopicResponse nodisplay(String topicId, User currentUser) throws XueWenServiceException{
		Topic topic=findOneById(topicId);//获得话题对象
		boolean admin = groupService.isGroupAdmin(topic.getSourceId(), currentUser.getId());//判断该用户是否为该群的管理员
		boolean owner = groupService.isGroupOwner(currentUser.getId(), topic.getSourceId());//判断该用户是否为该群的创建员
		if(admin==false && owner==false){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NODISPALY_201,null);
		}
		topic.setDisplayOrder(Config.NODISPLAY);
		topic.setDisplayTime(0);
		topicRepository.save(topic);
		TopicResponse tr = new TopicResponse(topic);
		return tr;
	}
	
	/**
	 * 单个对象转换成前端对象
	 * 
	 * @param topic
	 * @return
	 * @throws XueWenServiceException
	 */
	public TopicResponse toHelp(Topic topic,User user) throws XueWenServiceException {
		if (topic == null) {
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_NODATA_201, null);
		}
		RestTemplate restTemplate = new RestTemplate();
		String tag = restTemplate.getForObject(tagServiceUrl
				+ "tag/getTagsByIdAndType?domain=" + Config.YXTDOMAIN + "&itemId="
				+ topic.getTopicId() + "&itemType=" + Config.TAG_TYPE_TOPIC, String.class);
		JSONObject objj = JSONObject.fromObject(tag);
		JSONObject obss = objj.getJSONObject("data");
		net.sf.json.JSONArray childs = obss.getJSONArray("result");
		TopicResponse topicRespone = new TopicResponse(topic);
		topicRespone.setTagName(childs);
		boolean admin = groupService.isGroupAdmin(topic.getSourceId(), user.getId());//判断该用户是否为该群的管理员
		boolean owner = groupService.isGroupOwner(user.getId(), topic.getSourceId());//判断该用户是否为该群的创建员
		if(admin==false && owner==false){
			topicRespone.setAuthorGroup(false); //无权限置顶
		}else{
			topicRespone.setAuthorGroup(true);//有权限置顶
		}
		return topicRespone;
	}
	/**
	 * 计算某一群下的话题置顶数量
	 * @param topic
	 * @return
	 * @throws XueWenServiceException
	 */
	public int doDisplayCount(Topic topic)throws XueWenServiceException {
		return topicRepository.countByDisplayOrderAndIsDeletedAndSourceId(Config.DISPALY, false, topic.getSourceId());
	}
	/**
	 * 话题增加默认置顶时间字段
	 * @throws XueWenServiceException
	 */
	public void updateDisplayTime()throws XueWenServiceException {
		List<Topic> topics = topicRepository.findAll();
		Topic topic = null;
		for(int i = 0 ;i < topics.size();i++){
			topic = topics.get(i);
			if(topic.getDisplayTime()==0){
			topic.setDisplayTime(0);
			topicRepository.save(topic);
			}
		}
	}
	/**
	 * 
	 * @Title: modifyTopic
	 * @Description: pc修改话题shenb
	 * @param topic
	 * @param user
	 * @param image
	 * @throws XueWenServiceException void
	 * @throws
	 */
	/**
	 * 
	 * @auther tangli
	 * @Description: 话题修改
	 * @param topic
	 * @param user
	 * @param image
	 * @throws XueWenServiceException void
	 * @Date:2015年4月23日
	 * @throws
	 */
	public void modifyTopic(Topic topic,User user,String image) throws XueWenServiceException{
		Topic oldTopic=findOneById(topic.getTopicId());
		if(oldTopic==null){
			throw new XueWenServiceException(Config.STATUS_201, "此话题不存在或已被删除",null);
			}
		if (!topicTemplate.isExist(topic.getTopicId(), user.getId(), false)) {
			throw new XueWenServiceException(Config.STATUS_201, "无修改权限",null);
		}
		oldTopic.setTitle(topic.getTitle());
		oldTopic.setContent(topic.getContent());
		oldTopic.setType(topic.getType());
		String tagName=topic.getTagNames();
		oldTopic.setTagNames(tagName);
		try {
			if (!StringUtil.isBlank(tagName)) {
				tagService.updateItemTags(Config.YXTDOMAIN, tagName, Config.TAG_TYPE_TOPIC, user, topic.getTopicId());
			}
		} catch (Exception e) {
		}
		oldTopic=changePicUrlToImage(oldTopic,image,topic.getPicUrl());
		savetopic(oldTopic);
		
	}
	/**
	 * 分页获取不在此位置的话题列表及不在该分类下
	 * @param boxPostId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<Topic> findByBoxPostIdNotInBoxAndNotInCategory(String boxPostId,String type,String categoryId,Pageable pageable)throws XueWenServiceException{
		List<Object> ids=boxService.getSourceIdsByBoxPostIdAndNotInCagetory(boxPostId,type);
		return topicRepository.findByIsDeletedAndTopicIdNotIn(false,ids, pageable);
	}

}
