package operation.service.course;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import operation.exception.XueWenServiceException;
import operation.pojo.cloudfile.AttachFile;
import operation.pojo.cloudfile.Citem;
import operation.pojo.common.ColudConfig;
import operation.pojo.course.GroupShareKnowledge;
import operation.pojo.course.Knowledge;
import operation.pojo.group.XueWenGroup;
import operation.pojo.pub.QueryModel;
import operation.pojo.user.User;
import operation.repo.box.BoxTemplate;
import operation.repo.course.KnowledgeRepository;
import operation.repo.course.KnowledgeTemplate;
import operation.service.cloudfile.AttachFileService;
import operation.service.common.ColudConfigService;
import operation.service.fav.FavService;
import operation.service.group.GroupService;
import operation.service.praise.PraiseService;
import operation.service.praise.UnPraiseService;
import operation.service.share.ShareService;
import operation.service.tags.LocalTagService;
import operation.service.tags.TagService;
import operation.service.topics.PostService;
import operation.service.user.UserService;
import operation.service.util.ObjCopyPropsService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import tools.Config;
import tools.PageRequestTools;
import tools.StringToList;
import tools.StringUtil;
import tools.YXTJSONHelper;

import com.google.gson.JsonObject;

/**
 * 
 * @ClassName: KnowledgeService
 * @Description: 知识Service
 * @author
 * @date 2014年12月23日 下午3:45:19
 *
 */
@Service
public class KnowledgeService {

	private static final Logger logger = Logger
			.getLogger(KnowledgeService.class);

	@Autowired
	private KnowledgeRepository knowledgeRepository;

	@Autowired
	private GroupShareKnowledgeService groupShareKnowledgeService;

	@Autowired
	private AttachFileService attachFileService;

	@Autowired
	private ColudConfigService coludConfigService;

	@Autowired
	private ObjCopyPropsService objCopyPropsService;

	@Autowired
	private GroupService groupService;
	
	@Autowired
	public PraiseService  praiseService;
	
	@Autowired
	private FavService favService;
	
	@Autowired
	private KnowledgeTemplate knowledgeTemplate;
	
	@Autowired
	private TagService tagService;
	
	@Autowired
	private UserService userService;
	@Autowired
	private LessonService lessonService;
	
	@Autowired
	private LocalTagService localTagService;
	
	@Autowired
	private UnPraiseService unPraiseService;
	@Autowired
	private ShareService shareService;
	@Autowired
	private PostService postService;
	@Autowired
	BoxTemplate boxTemplate;
	
	/**
	 * 
	 * @Title: insert
	 * @Description: 插入
	 * @param knowledge
	 * @throws
	 */
	public void insert(Knowledge knowledge) {

		knowledgeRepository.save(knowledge);

	}

	/**
	 * 
	 * @Title: addKnowledge
	 * @Description: 添加知识
	 * @param knowledge
	 * @throws XueWenServiceException
	 *             void
	 * @throws
	 */
	public void addKnowledge(Knowledge knowledge, String groupId, String userId)
			throws XueWenServiceException {

		if (!checkKnoeledge(knowledge)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,
					null);
		} else {
			AttachFile attachFile = attachFileService
					.viewAttachFileById(knowledge.getFid());
			if (attachFile == null) {
				throw new XueWenServiceException(Config.STATUS_201,
						Config.MSG_201, null);
			}
			knowledge.setFurl(attachFile.getFurl());
			knowledge.setFileSize(attachFile.getFsize());
			knowledge.setUtime(System.currentTimeMillis());
			knowledge.setCtime(System.currentTimeMillis());
			knowledge.setCcode(Config.KNOWLEDGE_CCODE_WAIT);
			knowledge.setCuser(userId);
			String tag="";
			if(StringUtil.isBlank(knowledge.getTagNames())){
				tag=localTagService.getTagNamesByAnalysis(knowledge.getName()+","+knowledge.getDesc());
				knowledge.setTagNames(tag);
			}
			attachFileService.addAttachCountById(knowledge.getFid());
			// TODO 文档知识 是否需要审核
//			if (knowledge.getKngType() == Config.KNOWLEDGE_KNGTYPE_VIDE0) {
//				knowledge.setStatus(Config.KNOWLEDGE_STAT_PROCESS);
//			} else {
				knowledge.setStatus(Config.KNOWLEDGE_STAT_PROCESS);
//			}
			insert(knowledge);
			if(knowledge.getTagNames()!=null){
				User user=userService.findOne(userId);
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            				tagService.tagForObj(user, knowledge.getTagNames(), Config.TAG_TYPE_KNG, knowledge.getId());
			}
			// 关联群组
			if (!StringUtil.isEmpty(groupId)) {
				// TODO 是否要验证成员和群组的关系
				groupShareKnowledgeService.add(knowledge.getId(), groupId,
						userId);
				XueWenGroup xueWenGroup=groupService.findById(groupId);
//				xueWenGroup.setCategoryId(xueWenGroup.getCategoryId());
//				xueWenGroup.setChildCategoryId(xueWenGroup.getChildCategoryId());
				knowledge.setCategoryId(xueWenGroup.getCategoryId()==null?Config.CATEFORY_DEFAULT_PRIMARY:xueWenGroup.getCategoryId().toString());
				knowledge.setChildCategoryId(xueWenGroup.getChildCategoryId()==null?Config.CATEFORY_DEFAULT_SENCOND:xueWenGroup.getChildCategoryId().toString());
				knowledge.setArc(knowledge.getArc() + 1);
				insert(knowledge);
			}
			
			//更新vo
//			searchGroupVoService.updateByKng(knowledge, groupId);

		}

	}

	/**
	 * 
	 * @Title: checkKnoeledge
	 * @Description:后台验证知识必要参数是否合法
	 * @param knowledge
	 * @return boolean
	 * @throws
	 */
	private boolean checkKnoeledge(Knowledge knowledge) {
		String fid = knowledge.getFid();
		if (StringUtil.isEmpty(fid)) {
			logger.error("----------出错啦！知识没有文件Id-----");
			return false;
		}
		if (knowledge.getKngType() == 0) {
			logger.error("----------出错啦！缺失知识类型-----");
			return false;
		}

		// TODO CID是否要验证
		if (StringUtil.isBlank(knowledge.getName())) {
			logger.error("----------出错啦！缺失知识name-----");
			return false;
		}
		return true;
	}

	/**
	 * @throws XueWenServiceException
	 * 
	 * @Title: addCitems
	 * @Description: 云存储转码后回调
	 * @param itemstring
	 * @param cid
	 *            void
	 * @throws
	 */
	public void addCitems(String itemstring, String cid, int code,
			String logourl, Integer words, Integer pages, Integer duration)
			throws XueWenServiceException {

		if (StringUtil.isBlank(itemstring) || StringUtil.isBlank(cid)) {
			logger.error("------调用转码回调接口时 参数非法------");
			throw new XueWenServiceException(Config.STATUS_201,
					Config.MSG_ERROP_ADDCITEMS_201, null);
		}
		Knowledge knowledge = knowledgeRepository.findOneByCid(cid);
		if (knowledge == null) {
			logger.error("-------调用转码回调接口时 通过cid没有找到知识-----");
			throw new XueWenServiceException(Config.STATUS_201,
					Config.MSG_ERROP_ADDCITEMFINDK_201, null);
		}
		AttachFile attachFile = attachFileService.viewAttachFileById(knowledge
				.getFid());
		String ckey = attachFile.getCkey();
		List<Citem> citems = parseCitems(itemstring, ckey);
		sortCitems(knowledge, citems);
		knowledge.setCcode(code);
		knowledge.setCitems(citems);
		knowledge.setUtime(System.currentTimeMillis());
		if (!StringUtil.isBlank(logourl)) {
			knowledge.setLogoUrl(logourl);
		}
		if (words != null) {
			knowledge.setWords(words);
			
			
		}
		if (pages != null) {
			knowledge.setPages(pages);
		}
		if (duration != null) {
			knowledge.setDuration(duration*1000);
			//设置lesson 时长
			lessonService.saveDurations(duration,knowledge.getId());
			
		}
		knowledgeRepository.save(knowledge);

	}
	
	/**
	 * 
	 * @Title: addviewcitems
	 * @Description: 添加citem
	 * @param knowledge
	 * @param citems void
	 * @throws
	 */
	public void addviewcitems(Knowledge knowledge, List<Citem> citems) {
		List<Citem> pcCitems = new ArrayList<Citem>();
		List<Citem> appCitems = new ArrayList<Citem>();
		for (Citem citem : citems) {

			if ("flv".equals(citem.getFormat())) {
				pcCitems.add(citem);
			}
			if ("mp4".equals(citem.getFormat())) {
				appCitems.add(citem);
			}
			if ("html4".equals(citem.getFormat())) {
				appCitems.add(citem);
				pcCitems.add(citem);
			}
			if ("pdf".equals(citem.getFormat())) {
				appCitems.add(citem);
			}
			if ("swf".equals(citem.getFormat())) {
				pcCitems.add(citem);
			}

		}

		knowledge.setPcItems(pcCitems);
		knowledge.setAppItems(appCitems);

	}

	/**
	 * 
	 * @Title: sortCitems
	 * @Description: 按视频优先级组装对象
	 * @param knowledge
	 * @param citems
	 *            void
	 * @throws
	 */
	public void sortCitems(Knowledge knowledge, List<Citem> citems) {
		List<Citem> pcCitems = new ArrayList<Citem>();
		List<Citem> appCitems = new ArrayList<Citem>();
		for (String format : Config.KNOW_TYPES) {

			for (Citem citem : citems) {
				if (format.equals(citem.getFormat())) {
					if ("flv".equals(citem.getFormat())) {
						pcCitems.add(citem);
					}else if ("m3u8".equals(citem.getFormat())) {
						appCitems.add(citem);
						pcCitems.add(citem);
					}
					
					else if ("mp4".equals(citem.getFormat())) {
						appCitems.add(citem);
					} else if ("html4".equals(citem.getFormat())) {
						appCitems.add(citem);
						pcCitems.add(citem);
					} else if ("pdf".equals(citem.getFormat())) {
						appCitems.add(citem);
					} else if ("swf".equals(citem.getFormat())) {
						pcCitems.add(citem);
					}
				}

			}
		}
		knowledge.setPcItems(pcCitems);
		knowledge.setAppItems(appCitems);

	}
	
	/**
	 * 
	 * @Title: parseCitems
	 * @Description: 解析Citem
	 * @param itemstring
	 * @param ckey
	 * @return
	 * @throws XueWenServiceException List<Citem>
	 * @throws
	 */
	private List<Citem> parseCitems(String itemstring, String ckey)
			throws XueWenServiceException {
		JSONObject obj2 = JSONObject.fromObject(itemstring);
		JSONArray arr2 = JSONArray.fromObject(obj2.get("items"));
		List<Citem> citems = new ArrayList<Citem>();

		for (int i = 0; i < arr2.size(); i++) {

			JSONObject object = JSONObject.fromObject(arr2.get(i));

			ColudConfig coludConfig = coludConfigService.getColudConfig(ckey);
			Citem citem = (Citem) JSONObject.toBean(object, Citem.class);

			if (coludConfig == null) {
				throw new XueWenServiceException(Config.STATUS_201,
						Config.MSG_ERROCKEY_201, null);
			}

			// 获取List中的随机一个数
			List<String> baseurls = coludConfig.getBaseUrls();
			String baseurl = baseurls
					.get(new Random().nextInt(baseurls.size()));
			citem.setFurl(baseurl + citem.getKey());
			citems.add(citem);

		}

		// 拼装全路径

		// List<Citem> citems = new ArrayList<Citem>();
		// JsonParser parser = new JsonParser();
		// JsonElement jsonEl = parser.parse(itemstring);
		// JsonObject jsonObj = jsonEl.getAsJsonObject();// 转换成Json对象
		// JsonArray carrys = jsonObj.get("items").getAsJsonArray();
		// for (Iterator iter = carrys.iterator(); iter.hasNext();) {
		// JsonObject obj = (JsonObject) iter.next();
		// Citem citem = json2Citem(obj, ckey);
		// citems.add(citem);
		// }
		return citems;

	}

	/**
	 * @throws XueWenServiceException
	 * 
	 * @Title: json2Citem
	 * @Description: json转Citem
	 * @param obj
	 * @return Citem
	 * @throws
	 */
	private Citem json2Citem(JsonObject obj, String ckey)
			throws XueWenServiceException {
		Citem citem = new Citem();

		if (!obj.get("cmd").isJsonNull()) {
			citem.setCmd(obj.get("cmd").getAsString());
		}
		if (!obj.get("code").isJsonNull()) {
			citem.setCode(obj.get("code").getAsInt());
		}

		if (!obj.get("error").isJsonNull()) {
			citem.setError(obj.get("error").getAsString());
		}
		if (!obj.get("hash").isJsonNull()) {
			citem.setHash(obj.get("hash").getAsString());
		}

		// 拼装全路径
		ColudConfig coludConfig = coludConfigService.getColudConfig(ckey);
		if (coludConfig == null) {
			throw new XueWenServiceException(Config.STATUS_201,
					Config.MSG_ERROCKEY_201, null);
		}
		// 获取List中的随机一个数
		List<String> baseurls = coludConfig.getBaseUrls();
		String baseurl = baseurls.get(new Random().nextInt(baseurls.size()));
		citem.setFurl(baseurl + obj.get("key").getAsString());
		if (!obj.get("format").isJsonNull()) {
			citem.setFormat(obj.get("format").getAsString());
		}
		return citem;

	}
	
	
	public Knowledge getById(String id) throws XueWenServiceException {
		if (StringUtil.isBlank(id)) {
			logger.info("---------获取知识是 参数非法---------");
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_ERROP_GETKNGP_201, null);
		}
		Knowledge knowledge = knowledgeRepository.findOneByIdAndStatus(id, Config.KNOWLEDGE_STAT_PASS);
		return knowledge;
	}
	
	public Knowledge getByIdAll(String id) throws XueWenServiceException {
		if (StringUtil.isBlank(id)) {
			logger.info("---------获取知识是 参数非法---------");
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_ERROP_GETKNGP_201, null);
		}
		Knowledge knowledge = knowledgeRepository.findOne(id);
		return knowledge;
	}
	
	/**
	 * 根据ID获取知识信息，不关注其审核状态
	 * @param id
	 * @return
	 * @throws XueWenServiceException
	 */
	public Knowledge getByIdNotByStatus(String id) throws XueWenServiceException {
		if (StringUtil.isBlank(id)) {
			logger.info("---------获取知识是 参数非法---------");
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_ERROP_GETKNGP_201, null);
		}
		Knowledge knowledge = knowledgeRepository.findOne(id);
		return knowledge;
	}
	
	
	
	
	public JSONObject getKnowledgeAndUser(String id,User vuser) throws XueWenServiceException {
		if (StringUtil.isBlank(id)) {
			logger.info("---------获取知识是 参数非法---------");
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_ERROP_GETKNGP_201, null);
		}
		Knowledge knowledge = knowledgeRepository.findOneByIdAndStatus(id,
				Config.KNOWLEDGE_STAT_PASS);
		// 判断当前用户是否赞过
		boolean isHavaPraise = false;
		if (vuser != null) {
			isHavaPraise = praiseService.existByUserIdAndSourceId(
					knowledge.getId(), vuser.getId());
		} else {
			isHavaPraise = false;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		if (knowledge == null) {
			map.put("userLogoUrl", "");
			map.put("isHavaPraise", isHavaPraise);
		} else {
			User user = userService.findOne(knowledge.getCuser().toString());
			map.put("userLogoUrl", user.getLogoURL());
			map.put("cuserName", user.getNickName());
			map.put("isHavaPraise", isHavaPraise);
		}
		return YXTJSONHelper.addAndModifyAttrJsonObject(knowledge, map);
	}
	/**
	 * 查看我的知识
	 * @Title: getMyKnowledgeAndUser
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param id
	 * @return
	 * @throws XueWenServiceException JSONObject
	 * @throws
	 */
	public JSONObject getMyKnowledgeAndUser(String id) throws XueWenServiceException {
		if (StringUtil.isBlank(id)) {
			logger.info("---------获取知识是 参数非法---------");
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_ERROP_GETKNGP_201, null);
		}
		Knowledge knowledge = knowledgeRepository.findOne(id);
		
		Map<String, Object> map = new HashMap<String, Object>();
		if (knowledge == null) {
			map.put("userLogoUrl", "");
		} else {
			User vuser = userService.findOne(knowledge.getCuser().toString());
			boolean isHavaPraise = false;
			if (vuser != null) {
				isHavaPraise = praiseService.existByUserIdAndSourceId(
						knowledge.getId(), vuser.getId());
			} else {
				isHavaPraise = false;
			}
			map.put("userLogoUrl",  vuser.getLogoURL());
			map.put("isHavaPraise", isHavaPraise);

		}
		return YXTJSONHelper.addAndModifyAttrJsonObject(knowledge, map);
	}

	/**
	 * 
	 * @Title: getKnowledgesByGroupId
	 * @Description:通过群Id获取知识
	 * @param groupId
	 * @return
	 * @throws XueWenServiceException
	 *             List<Knowledge>
	 * @throws
	 */
	public Page<GroupShareKnowledge> getKnowledgesByGroupId(String groupId,Pageable pageable)
			throws XueWenServiceException {

		Page<GroupShareKnowledge> gs = groupShareKnowledgeService
				.getByGroupId(groupId,pageable);

		return gs;
	}
	
	
	public Page<Knowledge> getKnowledgeList(List<GroupShareKnowledge> ks,Pageable pageable) {
		List<String> kids = new ArrayList<String>();
		for (GroupShareKnowledge gsk : ks) {
			kids.add(gsk.getKnowledge());
		}
		return knowledgeRepository.findByIdInAndStatusAndCcodeAndRealTime(kids,
				Config.KNOWLEDGE_STAT_PASS, Config.KNOWLEDGE_CCODE_OK,false,pageable);
	}
	
	public Page<Knowledge> getKnowledgeListOnlyVideo(List<GroupShareKnowledge> ks,Pageable pageable) {
		List<String> kids = new ArrayList<String>();
		for (GroupShareKnowledge gsk : ks) {
			kids.add(gsk.getKnowledge());
		}
		return knowledgeRepository.findByIdInAndStatusAndCcodeAndKngType(kids,
				Config.KNOWLEDGE_STAT_PASS, Config.KNOWLEDGE_CCODE_OK,pageable,1);
	}
	
	public Map<String, Object>  getKnowledgeListAll(List<GroupShareKnowledge> ks,Pageable pageable) {
		List<String> kids = new ArrayList<String>();
		for (GroupShareKnowledge gsk : ks) {
			kids.add(gsk.getKnowledge());
		}

		Page<Knowledge>kngs= knowledgeRepository.findByIdInAndStatusAndCcode(kids,
				Config.KNOWLEDGE_STAT_PASS, Config.KNOWLEDGE_CCODE_OK,pageable);
		kids.removeAll(kids);
		for (Knowledge knowledge : kngs) {
			kids.add(knowledge.getId());
		}
		
		List<GroupShareKnowledge>  groupShareKnowledges=groupShareKnowledgeService.findByKnowledgeIn(kids);
		Map<String, Object> res=new HashMap<String, Object>();
		res.put("buffer", groupShareKnowledges);
		res.put("res", kngs);
		return res;
	}

	/**
	 * @throws XueWenServiceException
	 * 
	 * @Title: getPreTransKnowledges
	 * @Description: 获取等待转码的一条知识信息，ccode=1, 或 ccode=2
	 * @return Map<String,Object>
	 * @throws
	 */

	public synchronized JSONObject  getPreTransKnowledge()
			throws XueWenServiceException {
			Knowledge knowledge = null;
			if (getKnow(1) != null) {
				knowledge = getKnow(1);
			} else if (getKnow(2) != null) {
				knowledge = getKnow(2);

			}
			if (knowledge != null) {
				knowledge.setUtime(new Date().getTime());
				knowledge.setCcode(2);
				insert(knowledge);
				AttachFile attachFile = null;
				if (!StringUtil.isBlank(knowledge.getFid())) {
					attachFile = attachFileService.viewAttachFileById(knowledge
							.getFid());
				}
				Map<String, Object> map=new HashMap<String, Object>();
				map.put("fileInfo", attachFile);
				return YXTJSONHelper.addAndModifyAttrJsonObject(knowledge, map);
			}else{
				return null;
			}
		
	}

	public Knowledge getKnow(int ccode) throws XueWenServiceException {
		Order order = new Order("utime");
		Sort sort = new Sort(order);
		List<Knowledge> listCode=new ArrayList<Knowledge>();
		if(ccode==1){
			listCode = knowledgeRepository.findByCcode(ccode, sort);
		}else if(ccode==2){
			List<Knowledge> list=new ArrayList<Knowledge>();
			list = knowledgeRepository.findByCcode(ccode, sort);
			for (Knowledge knowledge : list) {
				if(knowledge.getKngType()==2){
					if(System.currentTimeMillis()-knowledge.getUtime()>3600*1000){
						listCode.add(knowledge);
					}
				}else{
					listCode.add(knowledge);
				}
				
			}
		}
		if (listCode.size() != 0) {
			return listCode.get(0);
		}
		return null;
	}

	/**
	 * 
	 * @Title: getTransData
	 * @Description: 获取等待转码的数据
	 * @return Map<String,Object>
	 * @throws
	 */
	public Map<String, Object> getTransData() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("t1c1", knowledgeRepository.findByKngTypeAndCcode(1, 1).size());
		map.put("t2c1", knowledgeRepository.findByKngTypeAndCcode(2, 1).size());
		map.put("t1c2", getKnows(1, 2, 7200 * 1000).size());
		map.put("t2c2", getKnows(2, 2, 3600 * 1000).size());
		map.put("t1c3", knowledgeRepository.findByKngTypeAndCcode(1, 3).size());
		map.put("t2c3", knowledgeRepository.findByKngTypeAndCcode(2, 3).size());
		return map;
	}

	public List<Knowledge> getKnows(int KngType, int ccode, long time) {
		List<Knowledge> listFind = knowledgeRepository.findByKngTypeAndCcode(
				KngType, ccode);
		List<Knowledge> list = new ArrayList<Knowledge>();
		long nowtime = new Date().getTime();
		if (listFind.size() != 0) {
			for (Knowledge knowledge : listFind) {
				if (nowtime - knowledge.getUtime() > time) {
					list.add(knowledge);
				}
			}
		}
		return list;
	}

	/**
	 * 
	 * @Title: getUserKnowledge
	 * @Description: 取用户上传的知识
	 * @param userId
	 * @param pageable
	 * @return Page<Knowledge>
	 * @throws
	 */
	public Page<Knowledge> getUserKnowledge(String userId, Pageable pageable,
			String name, Long ctime, Long ltime) {
		if (name == null && ctime == null) {
			return knowledgeRepository.findByCuser(userId, pageable);
		}

		if (name == null && ctime != null) {
			return knowledgeRepository.findByCuserAndCtimeBetween(userId,
					ctime, ltime, pageable);

		}
		if (name != null && ctime == null) {
			return knowledgeRepository.findByCuserAndNameLike(userId, name,
					pageable);

		}
		if (name != null && ctime != null) {
			return knowledgeRepository.findByCuserAndNameLikeAndCtimeBetween(
					userId, name, ctime, ltime, pageable);
		}

		return knowledgeRepository.findByCuser(userId, pageable);

	}

	/**
	 * @throws XueWenServiceException
	 * @Title: updateKnowledge
	 * @Description: 更新知识
	 * @return Knowledge
	 * @throws
	 */
	public Knowledge modifyKnowledge(Knowledge knowledge, int mtype, User user)
			throws XueWenServiceException {
		Knowledge oKnowledge = knowledgeRepository.findOne(knowledge.getId());
		if (!user.getId().equals((String) oKnowledge.getCuser())) {
			throw new XueWenServiceException(Config.STATUS_201,
					Config.MSG_KNGMODIFY_201, null);
		}
		// 判断是否重新上传附件
		if (mtype == Config.KNOWLEDGE_MODIFY_REFILE) {
			
			AttachFile attachFile=attachFileService.viewAttachFileById(knowledge.getFid());
			oKnowledge.setChash(knowledge.getChash());
			oKnowledge.setStatus(Config.KNOWLEDGE_STAT_PROCESS);
			oKnowledge.setCcode(Config.KNOWLEDGE_CCODE_WAIT);
			oKnowledge.setFid(knowledge.getFid());
			oKnowledge.setCid(knowledge.getCid());
			oKnowledge.setCheckdesc(null);
			oKnowledge.setCitems(null);
			oKnowledge.setFurl(attachFile.getFurl());
			// 老文件引用次数减一
			attachFileService.subAttachCountById(oKnowledge.getFid());

		}
		
		oKnowledge.setAuthor(knowledge.getAuthor());
		oKnowledge.setName(knowledge.getName());
		oKnowledge.setDesc(knowledge.getDesc());
		oKnowledge.setContent(knowledge.getContent());
		oKnowledge.setLogoUrl(knowledge.getLogoUrl());
		oKnowledge.setDuration(knowledge.getDuration());
		oKnowledge.setPages(knowledge.getPages());
		oKnowledge.setUtime(System.currentTimeMillis());
		insert(oKnowledge);
		return oKnowledge;
	}
	
	/**
	 * @throws XueWenServiceException
	 * @Title: updateKnowledge
	 * @Description: 更新知识
	 * @return Knowledge
	 * @throws
	 */
	public Knowledge verifyKnowledge(String id , String status)
			throws XueWenServiceException {
		if(StringUtil.isBlank(id)||StringUtil.isBlank(status)){
			throw new XueWenServiceException(Config.STATUS_201,
					"参数错误", null);
		}
		Knowledge oKnowledge = knowledgeRepository.findOne(id);
		
		
		oKnowledge.setStatus(Integer.parseInt(status));
		
		insert(oKnowledge);
		return oKnowledge;
	}

	/**
	 * @throws XueWenServiceException
	 * 
	 * @Title: delGroupKng
	 * @Description: 删除群知识
	 * @param gId
	 * @param kId
	 * @param user
	 *            void
	 * @throws
	 */
	public void delGroupKng(String gId, String kId, User user)
			throws XueWenServiceException {

		boolean s = groupService.isGroupAdmin(gId, kId);
		Knowledge knowledge = knowledgeRepository.findOne(kId);
		if (!s) {
			if (!user.getId().equals((String) knowledge.getCuser())) {
				throw new XueWenServiceException(Config.STATUS_201,
						Config.MSG_KNGMODIFY_201, null);
			}
		}

		GroupShareKnowledge groupShareKnowledge = groupShareKnowledgeService
				.findByGidAndKid(gId, kId);
		groupShareKnowledgeService.del(groupShareKnowledge);

	}
	/**
	 * 
	 * @Title: addFav
	 * @Description: 收藏数量加1
	 * @param id void
	 * @throws
	 */
	public void addFav(String id){
		Knowledge knowledge = knowledgeRepository.findOne(id);
		knowledge.setFavCount(knowledge.getFavCount());
		knowledgeRepository.save(knowledge);
	}

	/**
	 * @throws XueWenServiceException 
	 * @return 
	 * 
	 * @Title: praiseKnowledge
	 * @Description: 分享点赞
	 * @param id
	 * @param currentUser void
	 * @throws
	 */
	public Knowledge praiseKnowledgePc(String id, User currentUser) throws XueWenServiceException {
		if(StringUtil.isBlank(id)){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		Knowledge knowledge = knowledgeRepository.findOne(id);
		if(knowledge == null){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		praiseService.addPraiseTip(Config.YXTDOMAIN, Config.APPKEY_PC,id,Config.TYPE_KNOWLEDGE_GROUP,currentUser.getId());
		knowledge.setPraiseCount(knowledge.getPraiseCount()+1);
		return knowledgeRepository.save(knowledge);
	}
	/**
	 * @throws XueWenServiceException 
	 * @return 
	 * 
	 * @Title: praiseKnowledge
	 * @Description: 分享收藏
	 * @param id
	 * @param currentUser void
	 * @throws
	 */
	public Knowledge favKnowledgePc(String id, String userId) throws XueWenServiceException {
		if(StringUtil.isBlank(id)){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		Knowledge knowledge = knowledgeRepository.findOne(id);
		if(knowledge == null){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		favService.addFavPc(Config.YXTDOMAIN, Config.APPKEY_PC,id,Config.TYPE_KNOWLEDGE_GROUP,userId);
		knowledge.setFavCount(knowledge.getFavCount()+1);
		return knowledgeRepository.save(knowledge);
	}
	
	/**
	 * 根据knowledgeId返回app播放地址
	 * @param knowledgeId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Knowledge getAppItemsByKnowledgeId(String knowledgeId)throws XueWenServiceException{
		return knowledgeTemplate.findByIdRspIdAndappItems(knowledgeId);
//		List<Citem>  citems=knowledge.getAppItems();
//		
//		if(citems != null ){
//			for(Citem ci:citems){
//					return ci.getFurl();
//			}
//		}
//		
//		return null;
	}

//	/**
//	 * @throws XueWenServiceException 
//	 * 
//	 * @Title: getKnowledgeByTagPc
//	 * @Description: 按标签查找分享（在群组中的分享）
//	 * @param tagName
//	 * @param pageable
//	 * @return List<JSONObject>
//	 * @throws
//	 */
//	public Map<String,Object> getKnowledgeByTagPc(String tagName, Pageable pageable) throws XueWenServiceException {
//		//获取该标签下的所有分享id集合
//		List<String> knowledgeIds = tagService.findItemIds(Config.YXTDOMAIN, tagName, Config.TAG_TYPE_KNG, 1000);
//		//所有分享关系表集合
//		Page<GroupShareKnowledge> groupShareKnowledges = groupShareKnowledgeService.findByKnowledgeIn(knowledgeIds, pageable);
//		//关联群组的全部分享id的集合
//		List<String> ids = new ArrayList<String>();
//		for (GroupShareKnowledge groupShareKnowledge : groupShareKnowledges) {
//			ids.add(groupShareKnowledge.getKnowledge());
//		}
//		//关联群组的全部分享
//		List<Knowledge> knowledgeReturns = knowledgeRepository.findByIdInAndStatusAndCcode(ids,Config.KNOWLEDGE_STAT_PASS,Config.KNOWLEDGE_CCODE_OK);
//		//将课程放入map
//		Map<String,Knowledge> map = new HashMap<String, Knowledge>();
//		for (Knowledge knowledge : knowledgeReturns) {
//			map.put(knowledge.getId(), knowledge);
//		}
//		//拼装返回值
//		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
//		String[] includeKey = {"id","name","logoUrl","kngType","pages","duration","pcItems","appItems","words"};
//		Map<String, Object> addGroupIdMap = null;
//		for (GroupShareKnowledge groupShareKnowledge : groupShareKnowledges) {
//			Knowledge knowledge = map.get(groupShareKnowledge.getKnowledge());
//			addGroupIdMap = new HashMap<String, Object>();
//			addGroupIdMap.put("groupId", groupShareKnowledge.getGroupId());
//			jsonObjects.add(YXTJSONHelper.getInObjectAttrJsonObject(knowledge, addGroupIdMap, includeKey));
//		}
//		
//		Map<String,Object> mapReturn = new HashMap<String, Object>();
//		mapReturn.put("page", groupShareKnowledges);
//		mapReturn.put("result", jsonObjects);
//		return mapReturn;
//	}

	/**
	 * 
	 * @Title: getKnowledgeByKeyWordsPc
	 * @Description: 按条件查找分享（在群组中的分享）
	 * @param keyWords
	 * @param pageable
	 * @return List<Knowledge>
	 * @throws
	 */
	public Map<String,Object> getKnowledgeByKeyWordsPc(String keyWords, Pageable pageable) {
		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
		Page<Knowledge> knowledges =null;
		System.out.println(System.currentTimeMillis());
		if(StringUtil.isBlank(keyWords)){
			knowledges = knowledgeRepository.findByArcGreaterThanAndStatusAndCcodeAndRealTime(0, Config.KNOWLEDGE_STAT_PASS,Config.KNOWLEDGE_CCODE_OK,false,pageable);
		}else{
			//keyWords = ".*?(?i)"+keyWords+".*";
			knowledges = knowledgeRepository.findByArcGreaterThanAndStatusAndCcodeAndNameRegexOrDescRegexOrTagNamesRegexAndRealTime(0, Config.KNOWLEDGE_STAT_PASS,Config.KNOWLEDGE_CCODE_OK,keyWords,keyWords,keyWords,false,pageable);
		}
		//搜索到的全部分享的id
//		List<String> knowledgeIds = new ArrayList<String>();
//		Map<String,Knowledge> map = new HashMap<String, Knowledge>();
//		for (Knowledge knowledge : knowledges) {
//			knowledgeIds.add(knowledge.getId());
//			map.put(knowledge.getId(), knowledge);
//		}
		//搜索关系表
		//Page<GroupShareKnowledge> groupShareKnowledges = groupShareKnowledgeService.findByKnowledgeIn(knowledgeIds,pageable);
		//组装返回值
		String[] includeKey = {"id","name","logoUrl","kngType","pages","duration","pcItems","appItems","words","arc"};
//		for (GroupShareKnowledge groupShareKnowledge : groupShareKnowledges) {
//			Knowledge knowledge = map.get(groupShareKnowledge.getKnowledge());
//			addGroupIdMap = new HashMap<String, Object>();
//			addGroupIdMap.put("groupId", groupShareKnowledge.getGroupId());
//			jsonObjects.add(YXTJSONHelper.getInObjectAttrJsonObject(knowledge, addGroupIdMap, includeKey));
//		}
		for (Knowledge knowledge: knowledges.getContent()) {
			jsonObjects.add(getKnowledgeJson(knowledge, includeKey));
		}
		
		Map<String,Object> mapReturn = new HashMap<String, Object>();
		mapReturn.put("page", knowledges);
		mapReturn.put("result", jsonObjects);
		return mapReturn;
	}
	//...
	public JSONObject getKnowledgeJson(Knowledge knowledge,String...includeKey){
		Map<String, Object> map=new HashMap<String, Object>();
		if(groupShareKnowledgeService.findOneByKnowledgeId(knowledge.getId())!=null){
			map.put("groupId", groupShareKnowledgeService.findOneByKnowledgeId(knowledge.getId()).getGroupId());
			map.put("groupName", groupShareKnowledgeService.findOneByKnowledgeId(knowledge.getId()).getGroupName());
		}else{
			map.put("groupId", "");
			map.put("groupName", "");
		}
		return YXTJSONHelper.getInObjectAttrJsonObject(knowledge, map, includeKey);
	}
	

    /**
     * Jack Tang
     * @Title: gettop10
     * @Description: 根据热度获取top10
     * @return List<Knowledge>
     * @throws
     */
	public List<JSONObject> gettop10() {
 		QueryModel dmModel=new QueryModel();
 		dmModel.setN(0);
 		dmModel.setS(10);
 		dmModel.setSort("praiseCount");
 		dmModel.setMode("DESC");
 		Pageable pageable=PageRequestTools.pageRequesMake(dmModel);
 		Page<Knowledge>kngs=knowledgeRepository.findByArcGreaterThanAndStatusAndCcodeAndRealTime(0, Config.KNOWLEDGE_STAT_PASS, Config.KNOWLEDGE_CCODE_OK,false, pageable);
		String[] includeKey = {"id","name","logoUrl","kngType","pages","duration","pcItems","appItems","words","arc"};
 		List<JSONObject> objects=new ArrayList<JSONObject>();
		for (Knowledge kng : kngs) {
 			JSONObject jsonObject=getKnowledgeJson(kng, includeKey);
 			objects.add(jsonObject);

		}
   
		return objects;
	}

	/**
	 * 根据知识的id集合删除所有知识
	 * @param knowledgeIds
	 * @throws XueWenServiceException
	 */
	public void deleteByIds(List<Object> knowledgeIds)throws XueWenServiceException{
		knowledgeTemplate.deleteByIds(knowledgeIds);
	}
	
	/**
	 * 
	 * @Title: addViewCount
	 * @Description: 更新分享浏览次数
	 * @param id
	 * @throws XueWenServiceException void
	 * @throws
	 */
	public void  addViewCount(String id) throws XueWenServiceException {
        if(StringUtil.isBlank(id)){
        	throw new XueWenServiceException(Config.STATUS_201, "参数不能为空", null);
        }		
		knowledgeTemplate.addViewCount(id);
	}

	public Page<Knowledge> findKngList(Pageable pageable,String name) {
		if(name.equals("null")){
			name="";
		}
		return knowledgeRepository.findByStatusAndCcodeAndNameLike(1,0,name,pageable);
	}
	/**
	 * 
	 * @Title: searchAllPublicKnowledge
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param keyword
	 * @param pageable
	 * @return Page<Knowledge>
	 * @throws
	 */
	public Page<Knowledge> searchAllPublicKnowledge(String keyword,
			Pageable pageable) {
		if(StringUtil.isBlank(keyword)){
			//knowledgeRepository.findByCcodeAndStatusAndis
			return knowledgeRepository.findAllPublicKngs(Config.KNOWLEDGE_STAT_PASS,
					Config.KNOWLEDGE_CCODE_OK, true, pageable);
		}else {
			return knowledgeRepository.searchAllPublicKngs(Config.KNOWLEDGE_STAT_PASS,
					Config.KNOWLEDGE_CCODE_OK, keyword, true, pageable);
		}
	}
	
	/**
	 * 
	 * @Title: searchUserKnowledge
	 * @Description: 搜索用户知识
	 * @param keyword
	 * @param pageable
	 * @param userId
	 * @return Page<Knowledge>
	 * @throws
	 */
	public Page<Knowledge> searchUserKnowledge(String keyword, Pageable pageable,String userId) {
		if(StringUtil.isBlank(keyword)){
			return knowledgeRepository.findAllUserKngs(Config.KNOWLEDGE_STAT_PASS,
					Config.KNOWLEDGE_CCODE_OK, userId, pageable);
		}else {
			return knowledgeRepository.searchUserKngs(Config.KNOWLEDGE_STAT_PASS,
					Config.KNOWLEDGE_CCODE_OK,keyword, userId, pageable);	
		}
	}
	
	/**
	 * @throws XueWenServiceException 
	 * 
	 * @Title: checkKnowledge
	 * @auther Tangli
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param status
	 * @param desc 审核描述
	 * @throws
	 */
	public void checkKnowledge(boolean status, String desc,String kngId) throws XueWenServiceException {
		Knowledge kng=getByIdNotByStatus(kngId);
		if(status){
			kng.setStatus(Config.KNOWLEDGE_STAT_PASS);
		}else {
			kng.setStatus(Config.KNOWLEDGE_STAT_FAILE);
		}
		kng.setCheckdesc(desc);
		save(kng);
	}
	
	/**
	 * 
	 * @Title: save
	 * @auther Tangli
	 * @Description: 更新或保存
	 * @param kng void
	 * @throws
	 */
	public void save(Knowledge kng) {
		knowledgeRepository.save(kng);		
	}
	
	/**
	 * @throws XueWenServiceException 
	 * 
	 * @Title: findByUserIdAndCcodeAndStatus
	 * @auther Tangli
	 * @Description: 获取用户正常状态的分享文库
	 * @param userId
	 * @param knowledgeCcodeOk
	 * @param knowledgeStatPass
	 * @param pageable
	 * @return Page<Knowledge>
	 * @throws
	 */
	public Page<Knowledge> findByUserIdAndCcodeAndStatus(String userId,
			int knowledgeCcodeOk, int knowledgeStatPass, Pageable pageable) throws XueWenServiceException {
		if(StringUtil.isBlank(userId)){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201_PAGRM_ERRO, null);
		}
		return knowledgeRepository.findByCuserAndCcodeAndStatus(userId,
				knowledgeCcodeOk, knowledgeStatPass, pageable);
	}
	
	/**
	 * 
	 * @param fromUserId
	 * @param toUserId
	 * @throws XueWenServiceException
	 */
	public void mergeKnowledge(String fromUserId,String toUserId)throws XueWenServiceException{
		knowledgeTemplate.mergeKnowledge(fromUserId, toUserId);
	}
	//引用次数减一
	public void removeArc(String id) throws XueWenServiceException {
		Knowledge k=getById(id);
		k.setArc(k.getArc()>0?k.getArc()-1:0);
		save(k);
	}
	/**
	 * 
	 * @Title: deleteByIds
	 * @Description: 删除分享
	 * @param knowledgeIds
	 * @return
	 * @throws XueWenServiceException boolean
	 * @throws
	 */
	public boolean deleteByIds(String knowledgeIds)throws XueWenServiceException{
		List<Object> idList=new ArrayList<Object>();
		if(!StringUtil.isBlank(knowledgeIds)){
			idList=StringToList.tranfer(knowledgeIds);
		}else{
			throw new XueWenServiceException(Config.STATUS_201, "请选择需要删除的分享", null);
		}
		try {
			//删除所有的赞记录
			praiseService.deleteBySourceIds(idList);
			//删除所有的不攒接口
			unPraiseService.deleteBySourceIds(idList);
			//删除所有的分享记录
			shareService.deleteBySourceIds(idList);
			//删除收藏记录
			favService.deleteBySourceIds(idList);
			//删除分享记录
			deleteByIds(idList);
			//删除排行榜
			boxTemplate.deleteBySourceIds(idList);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 
	 * @Title: addfavCount
	 * @auther tangli
	 * @Description: 收藏数量变更
	 * @param sourceId
	 * @param i void
	 * @throws
	 */
	public void addfavCount(String sourceId, int i) {
		knowledgeTemplate.addfavCount(sourceId, i);	
	}
	/**
	 * 修改所有的老的知识实时上传为false
	 */
	public void updateAllKnowledgeRealTime(){
		knowledgeTemplate.updateAllRealTime();
	}
	/**
	 * 
	 * @param kids
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<Knowledge> findByIdRspAppItemsAndDownloadUlr(List<Object> kids)throws XueWenServiceException{
		return knowledgeTemplate.findByIdRspAppItemsAndDownloadUlr(kids);
	}

}
