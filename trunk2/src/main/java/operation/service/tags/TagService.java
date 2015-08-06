package operation.service.tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import operation.exception.XueWenServiceException;
import operation.pojo.course.Course;
import operation.pojo.course.NewCourse;
import operation.pojo.drycargo.Drycargo;
import operation.pojo.group.XueWenGroup;
import operation.pojo.tags.TagBean;
import operation.pojo.tags.UserTagBean;
import operation.pojo.topics.Topic;
import operation.pojo.user.User;
import operation.repo.course.CourseRepository;
import operation.repo.course.NewCourseRepository;
import operation.repo.drycargo.DrycargoRepository;
import operation.repo.group.GroupRepository;
import operation.repo.tags.IndustryRepostory;
import operation.repo.tags.TagRepository;
import operation.repo.tags.TagTemplate;
import operation.repo.tags.TagUserRepository;
import operation.repo.topics.TopicRepository;
import operation.repo.user.UserRepository;
import operation.service.course.KnowledgeService;
import operation.service.topics.TopicService;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import tools.Config;
import tools.HttpUtil;
import tools.ReponseData;
import tools.ResponseContainer;

import com.google.common.collect.Lists;

/**
 * 标签相关的逻辑
 * 
 * @author yangquanliang
 *
 */
@Service
@Component
public class TagService {
	private static final Logger logger = Logger.getLogger(TagService.class);

	@Autowired
	public TagRepository tagRepository;

	@Autowired
	public TagUserRepository tagUserRepository;

	@Autowired
	StringRedisTemplate stringRedisTemplate;

	@Autowired
	public IndustryRepostory industryRepostory;

	@Autowired
	public UserRepository userRepo;

	@Autowired
	public CourseRepository courseRepo;

	@Autowired
	public GroupRepository groupRepo;

	@Autowired
	public TopicRepository topicRepo;

	@Autowired
	public DrycargoRepository dryRepo;

	@Autowired
	public TagTemplate tagTemplate;
	
	@Autowired
	KnowledgeService knowledgeService;
	
	@Autowired
	private TopicService topicService;
	
	//@Autowired
	//public DrycargoBeanService drycargoBeanService;
	
	@Autowired
	private NewCourseRepository newCourseRepository;

	@Value("${tag.service.url}")
	private String tagServiceUrl;

	/**
	 * 判断是否存在该标签 如不存在 存储
	 * 
	 * @param bean
	 * @throws XueWenServiceException
	 */
	public void saveTag(TagBean bean) throws XueWenServiceException {

		TagBean tagBean = tagRepository.findOneByTagNameAndTagType(
				bean.getTagName(), bean.getTagType());

		if (tagBean == null) {
			tagRepository.save(bean);
		}

	}

	/**
	 * 用户的标签行为记录 不验证重复
	 * 
	 * @param bean
	 * @return
	 * @throws XueWenServiceException
	 */
	public UserTagBean saveUserTag(UserTagBean bean)
			throws XueWenServiceException {

		return tagUserRepository.save(bean);
	}

	public UserTagBean findUserTagById(String id) {
		return tagUserRepository.findOne(id);
	}

	/**
	 * 处理标签相关信息 保存到redis
	 * 
	 * @param userTagbean
	 * @throws XueWenServiceException
	 */
	public void saveUserTagToRedis(UserTagBean userTagbean)
			throws XueWenServiceException {

		// 处理redis逻辑

		// (1)通过用户id查询所有该用打过的标签 user:userId:tags
		String keyUserTags = "user:" + userTagbean.getUserId() + ":tags";
		stringRedisTemplate.opsForSet().add(keyUserTags,
				userTagbean.getTagName());

		// (2)通过标签查询所有打过该标签的用户
		String keyTagUsers = "tag:" + userTagbean.getTagName() + ":users";
		stringRedisTemplate.opsForSet().add(keyTagUsers,
				userTagbean.getUserId());

		// (3)通过被打标签的对象的id和类型查找所有对应标签
		String keyIITypeTags = "item:" + userTagbean.getItemId() + ":"
				+ userTagbean.getItemType() + ":" + "tags";
		if (stringRedisTemplate.opsForZSet().score(keyIITypeTags,
				userTagbean.getTagName()) == null)// 如果为空 说明该对象未被打过该标签，分值设为1
		{
			stringRedisTemplate.opsForZSet().add(keyIITypeTags,
					userTagbean.getTagName(), 1);
		} else // 如果存在该标签 又重复被打 分值加1 分值越高 说明标签热度越高
		{
			stringRedisTemplate.opsForZSet().incrementScore(keyIITypeTags,
					userTagbean.getTagName(), 1);
		}

		// (4)通过tagName查找所有的item对象
		String keytagItemType = "tag:" + userTagbean.getTagName() + ":"
				+ userTagbean.getItemType() + ":" + "itemIds";
		stringRedisTemplate.opsForZSet().add(keytagItemType,
				userTagbean.getItemId(), 1);

		// 处理mongodb基本标签库 如果该标签存在则热度值加1 如果不存在 插入基础标签库
		TagBean tagBean = tagRepository.findOneByTagNameAndTagType(
				userTagbean.getTagName(), userTagbean.getItemType());
		if (tagBean != null) {
			tagTemplate.updateTagBeanAndIncSocore(tagBean);
		} else {
			tagBean = new TagBean();
			tagBean.setTagName(userTagbean.getTagName());
			tagBean.setTagType(userTagbean.getItemType());
			tagBean.setScore(1);// 初始用户贡献的标签热度为1
			tagRepository.save(tagBean);
		}
	}

	/**
	 * 
	 * @Title: saveUserTagToRedis
	 * @Description: 新保存usertag
	 * @param userTagbean
	 * @throws XueWenServiceException
	 *             void
	 * @throws
	 */

	public void saveUserTagRedis(UserTagBean userTagbean)
			throws XueWenServiceException {
		String url = "tag/createTagBatch";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("domain", Config.YXTDOMAIN);
		params.put("itemId", userTagbean.getItemId());
		params.put("userId", userTagbean.getUserId());
		params.put("userName", userTagbean.getUserName());
		params.put("itemType", userTagbean.getItemType());
		params.put("tagNames", userTagbean.getTagName());
		HttpUtil.sendPost(tagServiceUrl + url, params);
	}

	/**
	 * 删除某个tag从redis中 删除成功返回对应删除的记录条数
	 * 
	 * @param userTagbean
	 * @return
	 * @throws XueWenServiceException
	 */
	public long delTagFromRedis(UserTagBean userTagbean)
			throws XueWenServiceException {
		// (3)通过被打标签的对象的id和类型查找所有对应标签
		String keyIITypeTags = "item:" + userTagbean.getItemId() + ":"
				+ userTagbean.getItemType() + ":" + "tags";
		long result = stringRedisTemplate.opsForZSet().removeRange(
				keyIITypeTags, 0, -1);

		// (4)通过tagName查找所有的item对象
		// String keytagItemType
		// ="tag:"+userTagbean.getTagName()+":"+userTagbean.getItemType()+":"+"itemIds";
		// long result1 =
		// stringRedisTemplate.opsForZSet().removeRange(keytagItemType, 0, -1);

		return result;
	}

	/**
	 * 查询基础标签根据tagType tag对象类型 1.用户 2.课程 3.小组 4.话题 5分享
	 * 
	 * @param itemType
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<TagBean> findByItemtype(String itemType)
			throws XueWenServiceException {

		return tagRepository.findBytagType(itemType);
	}

	/**
	 * 1.用户 2.课程 3.小组 4.话题 5分享
	 * 
	 * @param tagName
	 * @param itemType
	 * @return
	 */
	public ReponseData findItemsByTagAndItemType(String tagName,
			String itemType, Pageable pageable) throws XueWenServiceException {
		// (4)通过tagName查找所有的item对象
		String keytagItemType = "tag:" + tagName + ":" + itemType + ":"
				+ "itemIds";
		long offset = pageable.getPageNumber() * pageable.getPageSize();
		long count = pageable.getPageSize();

		// 符合条件的总记录条数
		long countTotal = stringRedisTemplate.opsForZSet()
				.zCard(keytagItemType);
		// 总页数
		long totalPages = (int) Math.ceil((double) countTotal
				/ (double) pageable.getPageSize());
		// 从后往前取ID
		Object[] itemids = stringRedisTemplate.opsForZSet()
				.rangeByScore(keytagItemType, -1, 0, offset, count).toArray();
		// (1)根据标签查找用户
		ReponseData rsData = new ReponseData();
		if (itemType == String.valueOf(1))// 用户
		{
			List<User> listUser = userRepo.findByIdIn(Lists
					.newArrayList(itemids));
			rsData.setResult(listUser);
		} else if (itemType == String.valueOf(2))// 课程
		{
			List<Course> listCourse = courseRepo.findByIdIn(Lists
					.newArrayList(itemids));
			rsData.setResult(listCourse);
		} else if (itemType == String.valueOf(3))// 小组
		{
			List<XueWenGroup> listGroup = groupRepo.findByIdIn(Lists
					.newArrayList(itemids));
			rsData.setResult(listGroup);
		} else if (itemType == String.valueOf(4))// 话题
		{
			List<Topic> listTopic = topicRepo.findByTopicIdIn(Lists
					.newArrayList(itemids));
			rsData.setResult(listTopic);
		} else if (itemType == String.valueOf(5))// 干货
		{
			List<Drycargo> listdry = dryRepo.findByIdIn(Lists
					.newArrayList(itemids));
			rsData.setResult(listdry);
		}

		if (countTotal == 0) {
			rsData.setCurr_page(pageable.getPageNumber());
			rsData.setCurr_rows(0);
		} else {
			rsData.setCurr_page(pageable.getPageNumber() + 1);
			int curr_rows = (pageable.getPageNumber() + 1)
					* pageable.getPageSize();
			if (curr_rows > countTotal) {
				curr_rows = new Long(countTotal).intValue();
			}
			rsData.setCurr_rows(curr_rows);
		}

		rsData.setTotal_rows(countTotal);
		rsData.setPage_rows(new Long(totalPages).intValue());

		return rsData;
	}

	/**
	 * 根据itemId itemType来获取所有tag
	 * 
	 * @param itemId
	 * @param itemType
	 * @return
	 * @throws XueWenServiceException
	 */
	public Set<String> getTagsByItemIdItemType(String itemId, String itemType)
			throws XueWenServiceException {
		String keyIITypeTags = "item:" + itemId + ":" + itemType + ":" + "tags";
		return stringRedisTemplate.opsForZSet().range(keyIITypeTags, 0, -1);
	}

	/**
	 * 根据itemId itemType来获取用户标签及其分值
	 * 
	 * @param itemId
	 * @param itemType
	 * @return
	 * @throws XueWenServiceException
	 */
	public Set<ZSetOperations.TypedTuple<String>> getUserTagsWithScoreByItemIdItemType(
			String itemId, String itemType) throws XueWenServiceException {
		String keyIITypeTags = "item:" + itemId + ":" + itemType + ":" + "tags";
		return stringRedisTemplate.opsForZSet().rangeWithScores(keyIITypeTags,
				0, -1);
	}

	/**
	 * @author yangquanliang
	 * @Description: 通过传进来的文本分词匹配标签库中得标签
	 * @param @param words
	 * @param @return
	 * @return List<TagBean>
	 * @throws
	 */
	public List<String> getTagsByAnalysis(String words) {
		List<Term> parse = BaseAnalysis.parse(words);
		List<String> inkeyWords = new ArrayList<String>();
		for (Term tempvalue : parse) {
			inkeyWords.add(tempvalue.getName());
		}
		List<String> list = null;
		try {
			list = tagTemplate.getTagsByKeyWords(inkeyWords);
		} catch (XueWenServiceException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * 
	 * @Title: getTagNamesByAnalysis
	 * @auther tangli
	 * @Description: 通过关键词获取标签名称 以,分割
	 * @param words
	 * @return String  java,net,php
	 * 	 * @throws
	 */
	public String getTagNamesByAnalysis(String words) {
		List<Term> parse = BaseAnalysis.parse(words);
		List<String> inkeyWords = new ArrayList<String>();
		List<String>tagsList=new ArrayList<String>();
		for (Term tempvalue : parse) {
			inkeyWords.add(tempvalue.getName());
		}
		for (String string : inkeyWords) {
			if(!tagsList.contains(string)&&string.length()>1){
				if (tagsList.size()<=5) {
					tagsList.add(string);
				}else {
					break;
				}
			}
		}		
		StringBuilder tagNames=new StringBuilder();
		for (String string : tagsList) {
			tagNames.append(string+",");
		}
		tagNames.deleteCharAt(tagNames.lastIndexOf(","));
		return tagNames.toString();
	}
	
	@Test
	public void test(){
		getTagNamesByAnalysis("java net");
	}

	/**
	 * @Description: 通过传进来的文本分词匹配标签库中得标签
	 * @param words
	 * @param n
	 *            > 0 时，限制获取标签n个
	 * @return List<TagBean>
	 * @throws
	 */
	public List<TagBean> getTagsByAnalysis(List<String> inkeyWords, int n,
			String tagType) {
		List<TagBean> list = null;
		try {
			list = tagTemplate.getTagsByKeyWords(inkeyWords, n, tagType, "1");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<String> getWordsList(String words) {
		List<Term> parse = BaseAnalysis.parse(words);
		List<String> inkeyWords = new ArrayList<String>();
		for (Term tempvalue : parse) {
			inkeyWords.add(tempvalue.getName());
		}
		return inkeyWords;
	}

	/**
	 * 根据tagName ,类型2 找出课程前n条，n<=0时找出所有
	 * 
	 * @param tagName
	 * @param itemType
	 * @return
	 */
	public Object[] findUserTagByTagNameAndItemType(String tagName,
			String itemType, int n) throws XueWenServiceException {
		// (4)通过tagName查找所有的item对象
		String keytagItemType = "tag:" + tagName + ":" + itemType + ":"
				+ "itemIds";
		// 符合条件的总记录条数
		long countTotal = stringRedisTemplate.opsForZSet()
				.zCard(keytagItemType);
		if (countTotal == 0) {
			return null;
		} else {
			// 从后往前取ID
			Object[] itemids = null;
			if (n <= countTotal && n > 0) {
				itemids = stringRedisTemplate.opsForZSet()
						.rangeByScore(keytagItemType, -1, n).toArray();
			} else {
				itemids = stringRedisTemplate.opsForZSet()
						.rangeByScore(keytagItemType, -1, countTotal).toArray();
			}
			return itemids;
		}
	}

	/**
	 * 
	 * @Title: tagForObj
	 * @Description:通用类 打标签接口
	 * @param user
	 * @param tagNames
	 * @param type
	 *            tag对象类型 1.用户 2.课程 3.小组 4.话题 5分享
	 * @param itemId
	 * @throws XueWenServiceException
	 *             void
	 * @throws
	 */
	public void tagForObj(User user, String tagNames, String type, String itemId)
			throws XueWenServiceException {
		UserTagBean tagBean = new UserTagBean();
		tagBean.setCtime(System.currentTimeMillis() + "");
		tagBean.setItemId(itemId);
		tagBean.setItemType(type);
		tagBean.setTagName(tagNames);
		tagBean.setUserId(user.getId());
		tagBean.setUserName(user.getUserName());
		
		if (Config.TAG_TYPE_KNG.equals(type)) {
//			knowledgeService.updateTagNames(tagNames);
		} else if (Config.TAG_TYPE_TOPIC.equals(type)) {
//			topicService.updateTagNames(tagNames);
		} else if (Config.TAG_TYPE_DRYCARGO.equals(type)) {
//			drycargoBeanService.updateTagNames(tagNames);
		} else if (Config.TAG_TYPE_COURSE.equals(type)) {
			NewCourse newCourse=newCourseRepository.findOne(itemId);
			if(newCourse!=null){
				newCourse.setTagNames(tagNames);
				newCourseRepository.save(newCourse);
			}
		} else if (Config.TAG_TYPE_GROUP.equals(type)) {
			XueWenGroup group=groupRepo.findOne(itemId);
			group.setTagNames(tagNames);
			groupRepo.save(group);
		}
		saveUserTagRedis(tagBean);
	}

	/**
	 * 
	 * @Title: updateItemTags
	 * @Description: 更新对象标签
	 * @param tagNames
	 * @param type
	 * @param user
	 * @param itemId
	 *            void
	 * @throws
	 */
	public void updateItemTags(String domain,String tagNames,String type,User user,String itemId) throws XueWenServiceException {

		String url = tagServiceUrl+"tag/editTagsDelAdd";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("domain", domain);
		params.put("userId", user.getId());
		params.put("userName", user.getUserName());
		params.put("itemType", type);
		params.put("tagNames", tagNames);
		params.put("itemId", itemId);
		ResponseContainer container=HttpUtil.doPost(url, params, ResponseContainer.class);
		if (container!=null&&container.getStatus()!=Config.STATUS_200) {
			throw new XueWenServiceException(Config.STATUS_201,
					Config.MSG_NODATA_201, null);
		}
	
	}


	/**
	 * 
	 * @Title: findItemIds
	 * @Description: 根据标签名，类型，查询资源id
	 * @param domain
	 * @param tagNames
	 * @param itemType
	 * @param count
	 * @return
	 * @throws XueWenServiceException List<String>
	 * @throws
	 */
	public List<String> findItemIds(String domain, String tagNames,
			String itemType, int count) throws XueWenServiceException {
		if (StringUtils.isBlank(tagNames)) {
			return new ArrayList<String>();
		}
		RestTemplate restTemplate = new RestTemplate();
		String url = tagServiceUrl + "tag/getItemsByTag?domain=" + domain
				+ "&tagName=" + tagNames + "&itemType=" + itemType + "&count="
				+ count;
		String str = restTemplate.getForObject(url, String.class);
		return getReault(str);
	}
	/**
	 * 
	 * @Title: findTagByItemIdAndType
	 * @Description: 根据对象id和对象类型取得tag（用逗号隔开的的String）
	 * @param domain
	 * @param itemId
	 * @param itemType
	 * @return
	 * @throws XueWenServiceException String
	 * @throws
	 */
	public String findTagByItemIdAndType(String domain, String itemId,
			String itemType) throws XueWenServiceException {
		Map<String,Object> map=new HashMap<String, Object>();
		//domain=yxt&itemId=552737b8d25ff8bb1630b67e&itemType=2
		map.put("domain", domain);
		map.put("itemId", itemId);
		map.put("itemType", itemType);	
		String s=HttpUtil.sendGet(tagServiceUrl+"tag/getTagsByIdAndType",map );
		System.out.println(s);
		return getReault(s, "value");
	}

	/**
	 * 
	 * @Title: getTagsByType
	 * @Description: 根据对象类型获取热门标签（用逗号隔开的的String）
	 * @param domain
	 * @param itemType
	 * @param count
	 * @return
	 * @throws XueWenServiceException String
	 * @throws
	 */
	public String getTagsByType(String domain,String itemType, int count)

			throws XueWenServiceException {
		RestTemplate restTemplate = new RestTemplate();
		String url = tagServiceUrl + "tag/getTagsByType?domain=" + domain
				+ "&count=" + 1000 + "&itemType=" + itemType;
		String str = restTemplate.getForObject(url, String.class);
		return getReault(str, "value",count);
	}
	/**
	 * 
	 * @Title: getTagsByTypeList
	 * @Description: 根据对象类型获取热门标签（返回的json包含score和value）
	 * @param domain
	 * @param itemType
	 * @param count
	 * @return
	 * @throws XueWenServiceException List<JSONObject>
	 * @throws
	 */
	public List<JSONObject> getTagsByTypeList(String domain,String itemType, int count)

			throws XueWenServiceException {
		RestTemplate restTemplate = new RestTemplate();
		String url = tagServiceUrl + "tag/getTagsByType?domain=" + domain
				+ "&count=" + count + "&itemType=" + itemType;
		String str = restTemplate.getForObject(url, String.class);
		return getReaultList(str);
	}
	/**
	 * 
	 * @Title: getReault
	 * @Description:返回逗号隔开的id
	 * @param str
	 * @return
	 * @throws XueWenServiceException List<String>
	 * @throws
	 */
	private List<String> getReault(String str) throws XueWenServiceException {
		List<String> list = new ArrayList<String>();
		JSONObject jsonObject = JSONObject.fromObject(str);
		if (!jsonObject.get("status").equals(Config.STATUS_200)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NODATA_201, null);
		}
		Object obj = jsonObject.get("data");
		JSONObject data = JSONObject.fromObject(obj);
		Object result = data.get("result");
		JSONArray array = JSONArray.fromObject(result);
		for (Object object : array) {
			list.add(object.toString());
		}
		return list;
	}
	/**
	 * 
	 * @Title: getReault
	 * @Description: 返回标签名（逗号隔开）
	 * @param str
	 * @param param
	 * @return
	 * @throws XueWenServiceException String
	 * @throws
	 */
	private String getReault(String str, String param)
			throws XueWenServiceException {
		String strs = "";
		JSONObject jsonObject = JSONObject.fromObject(str);
		if (!jsonObject.get("status").equals(Config.STATUS_200)) {
			throw new XueWenServiceException(Config.STATUS_201,
					Config.MSG_NODATA_201, null);
		}
		Object obj = jsonObject.get("data");
		JSONObject data = JSONObject.fromObject(obj);
		Object result = data.get("result");
		JSONArray array = JSONArray.fromObject(result);
		for (Object object : array) {
			JSONObject jb = JSONObject.fromObject(object);
			strs += jb.getString(param) + ",";
		}
		return strs;
	}
	/**
	 * 
	 * @Title: getReault
	 * @Description: 返回前几个热门标签
	 * @param str
	 * @param param
	 * @param count
	 * @return
	 * @throws XueWenServiceException String
	 * @throws
	 */
	private String getReault(String str, String param,int count)
			throws XueWenServiceException {
		String strs = "";
		JSONObject jsonObject = JSONObject.fromObject(str);
		if (!jsonObject.get("status").equals(Config.STATUS_200)) {
			throw new XueWenServiceException(Config.STATUS_201,
					Config.MSG_NODATA_201, null);
		}
		Object obj = jsonObject.get("data");
		JSONObject data = JSONObject.fromObject(obj);
		Object result = data.get("result");
		JSONArray array = JSONArray.fromObject(result);
		for (int i = 1; i <=count; i++) {
			if(array.size()-i>=0){
				JSONObject jb = JSONObject.fromObject(array.get(array.size()-i));
				strs += jb.getString(param) + ",";
			}
			
		}
		return strs;
	}
	/**
	 * 
	 * @Title: getReaultList
	 * @Description: 返回json(包含score和value)
	 * @param str
	 * @return
	 * @throws XueWenServiceException List<JSONObject>
	 * @throws
	 */
	@SuppressWarnings("null")
	private List<JSONObject> getReaultList(String str) throws XueWenServiceException{
		List<JSONObject> jsonObjects=new ArrayList<JSONObject>();
		JSONObject jsonObject = JSONObject.fromObject(str);
		if (!jsonObject.get("status").equals(Config.STATUS_200)) {
			throw new XueWenServiceException(Config.STATUS_201,
					Config.MSG_NODATA_201, null);
		}
		Object obj = jsonObject.get("data");
		JSONObject data = JSONObject.fromObject(obj);
		Object result = data.get("result");
		JSONArray array = JSONArray.fromObject(result);
		for (Object object : array) {
			JSONObject jb = JSONObject.fromObject(object);
			jsonObjects.add(jb);
		}
		return jsonObjects;
		
	}

}
