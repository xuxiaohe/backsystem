 package operation.service.course;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import operation.exception.XueWenServiceException;
import operation.pojo.box.Box;
import operation.pojo.category.Category;
import operation.pojo.cloudfile.Citem;
import operation.pojo.course.GroupCourse;
import operation.pojo.course.Knowledge;
import operation.pojo.course.Lesson;
import operation.pojo.course.NewChapter;
import operation.pojo.course.NewCourse;
import operation.pojo.course.NewGroupCourse;
import operation.pojo.dynamic.GroupDynamic;
import operation.pojo.group.XueWenGroup;
import operation.pojo.tags.UserTagBean;
import operation.pojo.topics.Images;
import operation.pojo.user.User;
import operation.repo.box.BoxTemplate;
import operation.repo.course.NewCourseRepository;
import operation.repo.course.NewCourseTemplate;
import operation.repo.dynamic.GroupDynamicRepository;
import operation.service.box.BoxService;
import operation.service.category.CategoryService;
import operation.service.fav.FavService;
import operation.service.group.GroupService;
import operation.service.praise.PraiseService;
import operation.service.share.ShareService;
import operation.service.study.StudyService;
import operation.service.tags.LocalTagService;
import operation.service.tags.TagService;
import operation.service.user.UserService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import tools.Config;
import tools.DateUtil;
import tools.HttpUtil;
import tools.ObjectComparator;
import tools.RestfulTemplateUtil;
import tools.StringUtil;
import tools.YXTJSONHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * @ClassName: CourseService
 * @Description: 课时Service
 * @author JackTang
 * @date 2014年12月19日 上午9:05:02
 *
 */
@Component
@EnableScheduling
@Service
public class NewCourseService {
	@Autowired
	public BoxService boxService;
	
	@Autowired
	public GroupDynamicRepository groupDynamicRepository;
	
	
	@Autowired
	private NewCourseRepository courseRepository;
	
	@Autowired
	private KnowledgeService knowledgeService;

	@Autowired
	private NewChapterService chapterService;
	
	@Autowired
	private LessonService lessonService;
	
	@Autowired
	private TagService tagService;
	@Autowired
	public PraiseService  praiseService;
	@Autowired
	private NewCourseTemplate newCourseTemplate;
	@Autowired
	private BoxTemplate boxTemplate;
	
	@Autowired
	private NewChapterService newChapterService;
	@Autowired
	private FavService favService;
	@Autowired
	private ShareService shareService;
	@Autowired
	private StudyService studyService;
	
	@Autowired
	private GroupCourseService groupCourseService;
	@Autowired
	private NewGroupCourseService newGroupCourseService;
	@Autowired
	private UserService userService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private LocalTagService localTagService;
	@Autowired
	private GroupService groupService;
	
	@Autowired
	private UserBuyCourseService userBuyCourseService;
	
	@Value("${coupon.service.url}")
	private String couponServiceUrl;
	
	
	private static final Logger logger = Logger.getLogger(NewCourse.class);

	/**
	 * 
	 * @Title: save
	 * @Description:插入一个课程
	 * @param course
	 *            void
	 * @throws
	 */
	public void save(NewCourse course) {
		courseRepository.save(course);
	}

	/**
	 * 
	 * @Title: save
	 * @Description: 批量插入课程
	 * @param courses
	 *            void
	 * @throws
	 */
	public void save(List<NewCourse> courses) {
		courseRepository.save(courses);

	}
	
	public NewCourse getById(String id) throws XueWenServiceException{
		NewCourse course = courseRepository.findOne(id);
		if (course == null) {
			return null;
		}
		List<Object> chapterIds = course.getChapters();
		List<Object> chapters=null;
		//根据章节id（list）获取全部章节
		if(chapterIds!=null){
			 chapters = newChapterService.findChapter(chapterIds);
		}
		
		List<Object> reschapters =new ArrayList<Object>();
		
		if(chapters!=null){
			ObjectComparator coc=new ObjectComparator("order");
			Collections.sort(chapters, coc);
		
		//搜索分享并放入章节中
		for (Object obj : chapters) {
			NewChapter chapter = (NewChapter)obj;
			//TODO 修改 lesson 需要对null属性做处理 
			//List<Lesson> lessons = chapter.getLessons();
			List<Lesson> lessons=lessonService.findByIdIn(chapter.getLessonIds());
			ObjectComparator lc=new ObjectComparator("order");
			Collections.sort(lessons, lc);
			List<JSONObject>lessonJson=new ArrayList<JSONObject>();
			for (Lesson lesson : lessons) {
				if (lesson.getKnowledge() != null) {
					Knowledge knowledge = knowledgeService.getByIdNotByStatus(lesson.getKnowledge().toString());
					
					if (("null").equals(knowledge.getTagNames())) {
						knowledge.setTagNames(null);
					}
					lesson.setKnowledge(knowledge);	
				}
				else{
					lesson.setKnowledge("");
				}
				lessonJson.add(YXTJSONHelper.excludeAttrJsonObject(lesson, new String[]{"createUser","tags"}));
			}
			Map<String, Object>map=new HashMap<String, Object>();
			map.put("lessons", lessonJson);
			JSONObject object=YXTJSONHelper.addAndModifyAttrJsonObject(chapter, map);
			reschapters.add(object);
			//chapter.setLessons(lessons);
		}
		
		course.setChapters(reschapters);
		}
		return course;
	}
	
	
	/**
	 * 订单页或者我购买的课程列表获取课程详情
	 * @param id
	 * @return
	 * @throws XueWenServiceException
	 */
	public Object getCourseById(String id,String userId) throws XueWenServiceException{
		NewCourse course = courseRepository.findOne(id);
		if (course == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_COURSEDELETE_201, null);
		}
		List<Object> chapterIds = course.getChapters();
		//根据章节id（list）获取全部章节
		List<Object> chapters = newChapterService.findChapter(chapterIds);
		List<Object> reschapters =new ArrayList<Object>();
		
		ObjectComparator coc=new ObjectComparator("order");
		Collections.sort(chapters, coc);
		//搜索分享并放入章节中
		for (Object obj : chapters) {
			
			NewChapter chapter = (NewChapter)obj;
			//TODO 修改 lesson 需要对null属性做处理 
			//List<Lesson> lessons = chapter.getLessons();
			List<Lesson> lessons=lessonService.findByIdIn(chapter.getLessonIds());
			ObjectComparator lc=new ObjectComparator("order");
			Collections.sort(lessons, lc);
			List<JSONObject>lessonJson=new ArrayList<JSONObject>();
			for (Lesson lesson : lessons) {
				String localUrl="";
				String downloadUrl="";
				if (lesson.getKnowledge() != null) {
					Knowledge k=knowledgeService.getAppItemsByKnowledgeId(lesson.getKnowledge().toString());
					if(k != null){
						downloadUrl=k.getCacheUrl();
						List<Citem>  citems=k.getAppItems();
						if(citems != null ){
							for(Citem ci:citems){
									localUrl= ci.getFurl();
							}
						}
					}
					lesson.setLocalUrl(localUrl);
				}else{
					lesson.setKnowledge("");
				}
				Map<String,Object> map=new HashMap<String, Object>();
				map.put("downloadUrl",downloadUrl);
				lessonJson.add(YXTJSONHelper.getExObjectAttrJsonObject(lesson,map, new String[]{"createUser","tags"}));
			}
			Map<String, Object> map=new HashMap<String, Object>();
			map.put("lessons", lessonJson);
			JSONObject object=YXTJSONHelper.getExObjectAttrJsonObject(chapter, map,new String[]{"lessonIds"});
			reschapters.add(object);
		}
		course.setChapters(reschapters);
		//获取购买人头像列表
		List<Object> objs=userBuyCourseService.courseDetailBuyCourseUserList(id);
		Map<String, Object> courseMap=new HashMap<String, Object>();
		courseMap.put("isBuy", userBuyCourseService.findOneByUserIdAndCourseId(userId, id));
		courseMap.put("isOwner", course.getCreateUser().toString().equals(userId)?true:false);
		courseMap.put("buyers", objs);
		//获取课程活动
		JSONArray coupons=getCanUserCouponsByCourseIdAndUserId(id,userId);
		if(coupons !=null){
			courseMap.put("coupons",coupons);
		}
		return YXTJSONHelper.addAndModifyAttrJsonObject(course, courseMap);
	}
	
	public JSONObject getByIdForPc(String id) throws XueWenServiceException{
		NewCourse course = courseRepository.findOne(id);
		if (course == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		List<Object> chapterIds = course.getChapters();
		List<Object> chapters=null;
		if(chapterIds!=null){
			//根据章节id（list）获取全部章节
			chapters = newChapterService.findChapter(chapterIds);
		}
		
		List<JSONObject>newchapter=new ArrayList<JSONObject>();
		if(chapters!=null){
			//搜索分享并放入章节中
			for (Object obj : chapters) {
				NewChapter chapter = (NewChapter)obj;
				Sort sort=new Sort(Direction.ASC,"order");
				List<Lesson> lessons =lessonService.findByIdIn(chapter.getLessonIds(),sort);
				
				List<Map<String, String>> newlessons=new ArrayList<Map<String,String>>();
				for (Lesson lesson : lessons) {
					
					if (lesson.getKnowledge() != null) {
						Knowledge knowledge=null;
						if(course.isChecked()){
							knowledge = knowledgeService.getById(lesson.getKnowledge().toString());
						}else{
							knowledge = knowledgeService.getByIdAll(lesson.getKnowledge().toString());
						}
						Map<String, String>kMap=new HashMap<String, String>();
						if(knowledge!=null){
							kMap.put("kName", knowledge.getName());
							kMap.put("kId", knowledge.getId());
							kMap.put("ktype", knowledge.getKngType()+"");
							kMap.put("lId", lesson.getId());
							kMap.put("ltitle", lesson.getTitle());
							kMap.put("IsBuy", lesson.getIsbuy());
							newlessons.add(kMap);
						}
						
						
						//lesson.setKnowledge(knowledge);
					}
				}
				
				//chapter.setLessons(lessons);
				//chapter.setNewLessons(newlessons);
				Map<String, Object>map=new HashMap<String, Object>();
				//map.put("lessons", lessons);
				map.put("newLessons", newlessons);
				JSONObject chapJson=YXTJSONHelper.addAndModifyAttrJsonObject(chapter, map);
				String[] chapterincludeKey=new String[]{"id","title","newLessons"};
				chapJson=YXTJSONHelper.includeAttrJsonObject(chapJson, chapterincludeKey);

//				//对chapter做瘦身
//				
//				JSONObject chapJson=YXTJSONHelper.includeAttrJsonObject(chapter, chapterincludeKey);
				newchapter.add(chapJson);
			}
		}
		
		 
		//course.setChapters(chapters);
		course.setNewchapter(newchapter);
		String[] includeKey=new String[]{"id","title","sourceType","newchapter","logoUrl","intro","tagNames","price"};
		JSONObject  res=YXTJSONHelper.includeAttrJsonObject(course, includeKey);
		if(res.size()==0){
			return null;
		}
		return res;
	}
	
	@Test
	public void test(){
		updateCourse();
	}
	/**
	 * 
	 * @Title: updateCourse
	 * @Description: 每天凌晨00:00:00定时采集数据 
	 * @throws
	 */
	//@Scheduled(cron="0 0 0 ? * *")
	public void updateCourse(){
		DateFormat format =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		//format.format(date)
		Calendar c = Calendar.getInstance(); 
		c.setTime(new Date());
		c.add(Calendar.DATE, -60);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		Date date=c.getTime();
		String time=format.format(date);
		logger.info("--------开启从内容纵向添加数据任务：时间，"+time);
		try {
			getCoursesFromCenter(time);
			logger.info("--------从内容纵向添加数据任务顺利结束");
		} catch (XueWenServiceException e) {
			logger.error("------从内容纵向添加数据任务失败--------00");
			e.printStackTrace();
		}
		
	}
	public void initCourse(){
		DateFormat format =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		//format.format(date)
		Calendar c = Calendar.getInstance(); 
		c.setTime(new Date());
		c.add(Calendar.DATE, -36500);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		Date date=c.getTime();
		String time=format.format(date);
		logger.info("--------开启从内容纵向添加数据任务：时间，"+time);
		try {
			getCoursesFromCenter("2013-01-01 00:00:00");
			logger.info("--------从内容纵向添加数据任务顺利结束");
		} catch (XueWenServiceException e) {
			logger.error("------从内容纵向添加数据任务失败--------00");
			e.printStackTrace();
		}
		
	}
	

	/**
	 * @throws XueWenServiceException
	 * 
	 * @Title: getCoursesFromCenter
	 * @Description: 从课程中心获取课程
	 * @return List<Course>
	 * @throws
	 */
	
	public List<NewCourse> getCoursesFromCenter(String time)
			throws XueWenServiceException {
		// 1从内容中心获取数据
		String reString=getDataFromCenter(time);
		
		logger.info("----------------开始解析课程数据-------------------");
		long t1=System.currentTimeMillis();
		// 2数据解析
		List<NewCourse> courses = parseCourseJsonString(reString);
		long t2=System.currentTimeMillis();
		logger.info("----------------解析课程数据结束，用时："+(t2-t1)+"ms");
		
		//3剔除已经插入过的课程
		deleteReCourse(courses);
		logger.info("--------课程数据插入数据库--------"+courses.size());
		long t3=System.currentTimeMillis();
		//4保存数据
		for(NewCourse course:courses){
					List<Object> objs=course.getChapters();
					List<NewChapter>chapters=new ArrayList<NewChapter>();
				    List<Object>chapterIds=new ArrayList<Object>();
					for(Object obj:objs){
						NewChapter chapter=new NewChapter();
						chapter=(NewChapter)obj;
//						
//						List<Lesson>lessons=chapter.getLessons();
//						
//						//4.1存lesson
//						lessonService.save(lessons);
//						//List<String>lessonIds=new ArrayList<String>();
//						//取lessonIds
//						chapter.setLessonIds(getLessonIds(lessons));
//						chapters.add(chapter);	
				}
					//4.2 存chapter
					chapterService.save(chapters);
					//重新组装Chapter Course中只存id
					for(NewChapter newchapterbuffer:chapters){
						chapterIds.add(newchapterbuffer.getId());
					}
					course.setChapters(chapterIds);
					
				}
		//4.3存courses
		save(courses);
		long t4=System.currentTimeMillis();
		logger.info("----------------课程数据插入数据库结束,用时:"+(t4-t3)+"ms");
		return courses;
	}
    
	/**
	 * 
	 * @Title: getLessonIds
	 * @Description: 获取ids
	 * @param lessons
	 * @return List<String>
	 * @throws
	 */
	private List<String> getLessonIds(List<Lesson> lessons) {
		List<String> idsList=new ArrayList<String>();
		for (Lesson lesson : lessons) {
			 idsList.add(lesson.getId());
		}
		return idsList;
	}

	private String getDataFromCenter(String time) throws XueWenServiceException{
		long t1 = System.currentTimeMillis();
		String url = Config.CENTER_BASEURL + "course/list";
		Map<String, Object> p = new HashMap<String, Object>();
		p.put("apikey", Config.CENTER_APIKEY);
		p.put("salt", Config.CENTER_SALT);
		p.put("signature", Config.CENTER_SIGNATURE);
		p.put("PublishDate", time);
		String resObj = HttpUtil.sendPost(url, p);
		System.out.println(resObj);
		long t = System.currentTimeMillis() - t1;
		logger.info("-------课程中心响应结束，耗时：" + t + " ms");
		if (StringUtil.isEmpty(resObj)) {
			throw new XueWenServiceException(Config.STATUS_504, Config.MSG_504,
					null);
			
		}
		return resObj;
		
	}
	
	/**
	 * 
	 * @Title: parseCourseJsonString
	 * @Description: 解析课程Json
	 * @param jsonString
	 * @return
	 * @throws XueWenServiceException List<NewCourse>
	 * @throws
	 */
	@SuppressWarnings("rawtypes")
	private List<NewCourse> parseCourseJsonString(String jsonString) throws XueWenServiceException{
		
		JsonParser parser = new JsonParser();
		JsonElement jsonEl = parser.parse(jsonString);
		JsonObject jsonObj = jsonEl.getAsJsonObject();// 转换成Json对象
		JsonArray carrys = jsonObj.get("data").getAsJsonArray();
		List<NewCourse> courses = new ArrayList<NewCourse>();
		for (Iterator iter = carrys.iterator(); iter.hasNext();) {
			JsonObject obj = (JsonObject) iter.next();
		   String id=obj.get("ID").getAsString();
			
			if(!StringUtil.isBlank(id)&&id=="a8c41d04-8a30-4ff1-955b-ecc80a9efe56"){
				NewCourse course = json2Course(obj);
				courses.add(course);
			}
		}
		return courses;
	}
	/**
	 * 
	 * @Title: json2Course
	 * @Description: 将Json转为Course Bean
	 * @param obj
	 * @return
	 * @throws XueWenServiceException
	 *             Course
	 * @throws
	 */
	@SuppressWarnings("rawtypes")
	private NewCourse json2Course(JsonObject obj) throws XueWenServiceException {
		NewCourse course = new NewCourse();

		if (!obj.get("ID").isJsonNull()) {
			course.setSourceId(obj.get("ID").getAsString());
		}
		if (!obj.get("Name").isJsonNull()) {
			course.setTitle(obj.get("Name").getAsString());
		}
		if (!obj.get("Summary").isJsonNull()) {
			course.setIntro(obj.get("Summary").getAsString());
		}

		if (!obj.get("ImageUrl").isJsonNull()) {
			String logourl = obj.get("ImageUrl").getAsString();
			course.setLogoUrl(logourl);
		}

		// 设置创建用户id
		course.setCreateUser("");
		if (!obj.get("CreateDate").isJsonNull()) {
			String sdate = obj.get("CreateDate").getAsString()
					.replaceAll("T", " ");
			long ctime = DateUtil.Sdate2Long(sdate);
			course.setCtime(ctime);
		}
		// 设置 来源类别      1 为内容中心
		course.setSourceType(1);
		if(!obj.get("HotIndex").isJsonNull()){
			course.setHotIndex(obj.get("HotIndex").getAsString());
		}
		if(!obj.get("RecommendIndex").isJsonNull()){
		   course.setRecommendIndex(obj.get("RecommendIndex").getAsString());
		}
		if(!obj.get("BestIndex").isJsonNull()){
			course.setBestIndex(obj.get("BestIndex").getAsString());
		}
		
		//设置课程类别
		if(!obj.get("CourseType").isJsonNull()){
			course.setCourseType(obj.get("CourseType").getAsString());
	
		}
		// 创建chapter
		List<Object> chapters = new ArrayList<Object>();
		
		if (!obj.get("Chapters").isJsonNull()) {
			JsonArray carrys = obj.get("Chapters").getAsJsonArray();

			for (Iterator iter = carrys.iterator(); iter.hasNext();) {
				JsonObject chapterobj = (JsonObject) iter.next();
				NewChapter chapter = chapterService.json2Chapter(chapterobj);
				chapters.add(chapter);
				
			}
		}
	

		course.setChapters(chapters);

		return course;
	}
	/**
	 * 
	 * @Title: deleteReCourse
	 * @Description: 剔除重复的课程
	 * @param courses void
	 * @throws
	 */
	private void deleteReCourse(List<NewCourse> courses){
		List<NewCourse>delCourses=new ArrayList<NewCourse>();
		for(int i=0;i<courses.size();i++){
			NewCourse course=courses.get(i);
		
			if(courseRepository.findBySourceId(course.getSourceId()).size()>0){
				delCourses.add(course);
			}
		}
		courses.removeAll(delCourses);

		
	}
	/**
	 * @throws XueWenServiceException 
	 * 
	 * @Title: tagsForCourses
	 * @Description: 给课程批量打标签
	 * @param tagNames  格式如：name;name
	 * @param CourseId
	 * @param user void
	 * @throws
	 */
	public void tagsForCourses(String tagNames,String CourseId,User user) throws XueWenServiceException{
		  if(StringUtil.isBlank(tagNames)){
			  logger.error("-----------标签名称不能为空-----------");
			  throw new XueWenServiceException(Config.STATUS_201,"标签名不能为空",null);
		  }
		  else{
			  tagForCourse(tagNames,CourseId,user);
		  }
	}

	/**
	 * 
	 * @Title: tagForCourse
	 * @Description: 给课程添加标签
	 * @throws XueWenServiceException 
	 */
	public void tagForCourse(String tagName,String CourseId,User user) throws XueWenServiceException{
	 	
		UserTagBean userTagBean=new UserTagBean();
		userTagBean.setCtime(System.currentTimeMillis()+"");
		userTagBean.setItemType("2");
		userTagBean.setTagName(tagName);
		userTagBean.setUserId(user.getId());
		userTagBean.setUserName(user.getUserName());
		userTagBean.setItemId(CourseId);		
		//tagService.saveUserTag(userTagBean);
		tagService.saveUserTagRedis(userTagBean);
		
	}
	/**
	 * 
	 * @Title: getRecommendTagsBySelf
	 * @Description: 根据课程自身的信息获取推荐的标签 6个
	 * @param cousrId
	 * @return List<TagBean>
	 * @throws
	 */
	public List<String>getRecommendTagsBySelf(String cousrId){
		NewCourse nCourse=courseRepository.findOne(cousrId);
		String KeyWords=nCourse.getTitle()+nCourse.getIntro();
		return tagService.getTagsByAnalysis(KeyWords);
	}
	
	/**
	 * 
	 * @Title: getCourses
	 * @Description: 分页获取课程
	 * @param sort
	 * @param pageable
	 * @return Page<NewCourse>
	 * @throws
	 */
	public Page<NewCourse> getCourses(Pageable pageable) {
		Page<NewCourse> newCourses = courseRepository.findAll(pageable);
		return newCourses;
	}
	
	/**
	 * 
	 * @Title: getCourses
	 * @Description: 获取课程 用户创建
	 * @param sort
	 * @param pageable
	 * @return Page<NewCourse>
	 * @throws
	 */
	public List<NewCourse> getMyCourses(String uid) {
		List<NewCourse> newCourses = courseRepository.findByCreateUser(uid);
		return newCourses;
	}
	/**
	 * 
	 * @Title: getMyCourses
	 * @Description: 带分页的获取用户的课程
	 * @param uid
	 * @param pageable
	 * @return Page<NewCourse>
	 * @throws
	 */
	public Page<NewCourse> getMyCourses(String uid,Pageable pageable) {
		Page<NewCourse> newCourses = courseRepository.findByCreateUser(uid,pageable);
		return newCourses;
	}
	
	
	
	/**
	 * 分页获取所有已经通过审核的课程
	 * @param pageable
	 * @return
	 */
	public Page<NewCourse> getCheckedCourses(Pageable pageable) {
		Page<NewCourse> newCourses = courseRepository.findByChecked(true, pageable);
		return newCourses;
	}
	
	
	
	/**
	 * @Title: getCourses
	 * @Description: 分页获取不再群组小组课堂内的其他课程
	 * @param sort
	 * @param pageable
	 * @return Page<NewCourse>
	 * @throws
	 */
	public Page<NewCourse> getCoursesNotInGroup(String groupId,Pageable pageable) throws XueWenServiceException{
		//获取此群下已经有的群组课程ID集合
		List<Object> groupCourseIds=newGroupCourseService.findGroupCourseCourseIdsByGroupId(groupId);
		Page<NewCourse> newCourses = courseRepository.findByIdNotInAndChecked(groupCourseIds, true,pageable);
		return newCourses;
	}
	
	/**
	 * 
	 * @Title: getCourses
	 * @Description: 分页获取课程
	 * @param sort
	 * @param pageable
	 * @return Page<NewCourse>
	 * @throws
	 */
	public Page<NewCourse> getCourseNoShare(Pageable pageable,List l) {
		Page<NewCourse> newCourses = courseRepository.findByIdNotInAndChecked(l,true,pageable);
		return newCourses;
	}

	/**
	 * 课程的收藏，学习，分享记录
	 * 
	 * @param userId
	 * @param courseId
	 * @param operation
	 * @throws XueWenServiceException
	 */
	public boolean countOperationPc(String userId, String courseId, String operation)
			throws XueWenServiceException {

		NewCourse course = getById(courseId);
		if (course != null) {
			if (operation.equals("share")) {
				Map<String, Long> whoShare = course.getWhoShare();
				if (whoShare == null) {
					whoShare = new LinkedHashMap<String, Long>();
				}

				if (!whoShare.containsKey(userId)) {
					long time = System.currentTimeMillis();
					whoShare.put(userId, time);
					course.setWhoShare(whoShare);
					course.setShareCount(course.getShareCount() + 1);
				} else {
					// 不增加分享人d，只增加分享数量
					course.setShareCount(course.getShareCount() + 1);
				}
				courseRepository.save(course);
			} else if (operation.equals("fav")) {
				Map<String, Long> whoFav = course.getWhoFav();
				if (whoFav == null) {
					whoFav = new LinkedHashMap<String, Long>();
				}

				if (!whoFav.containsKey(userId)) {
					long time = System.currentTimeMillis();
					whoFav.put(userId, time);
					course.setWhoFav(whoFav);
					course.setFavCount(course.getFavCount() + 1);
					courseRepository.save(course);
				}
//				else{
//					throw new XueWenServiceException(Config.STATUS_201,
//							Config.MSG_FAV_201, null);
//				}
			} else if (operation.equals("study")) {
				Map<String, Long> whoStudy = course.getWhoStudy();
				if (whoStudy == null) {
					whoStudy = new LinkedHashMap<String, Long>();
				}

				if (!whoStudy.containsKey(userId)) {
					long time = System.currentTimeMillis();
					whoStudy.put(userId, time);
					course.setWhoStudy(whoStudy);
					course.setStudyCount(course.getStudyCount() + 1);
					courseRepository.save(course);
				}
			} else {
				throw new XueWenServiceException(Config.STATUS_201,
						Config.MSG_NODATA_201, null);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 根据课程ID返回课程基本信息（只有，ID，title，intro,tags，logoUrl);
	 * @author hjn
	 * @param courseId
	 * @return
	 * @throws XueWenServiceException
	 */
	public NewCourse findOneNewCourseByIdBasicInfo(String courseId)throws XueWenServiceException{
		return newCourseTemplate.findOneCourseBasicInfo(courseId);
	}
	
	/**
	 * 根据课程ID返回课程基本信息包括chapter（只有，ID，title，intro,tags，logoUrl,chapter）
	 * @author hjn
	 * @param courseId
	 * @return
	 * @throws XueWenServiceException
	 */
	public NewCourse findOneNewCourseByIdBasicInfoIncludeChapter(String courseId)throws XueWenServiceException{
		return newCourseTemplate.findOneCourseBasicInfoIncludeChapter(courseId);
	}
	/**
	 * 根据课程ID返回课程ID 和chapters节点（只有，ID，chapters）
	 * @author hjn
	 * @param courseId
	 * @return
	 * @throws XueWenServiceException
	 */
	public NewCourse findOneNewCourseByIdRspOnlyIdAndChapters(String courseId)throws XueWenServiceException{
		return newCourseTemplate.findOneNewCourseByIdRspOnlyIdAndChapters(courseId);
	}
	
	/**
	 * 判断课程是否被用户收藏
	 * @param courseId
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean isCourseFavByUser(String courseId,String userId)throws XueWenServiceException{
		return favService.isUserFav(userId, Config.YXTDOMAIN, courseId, Config.TYPE_COURSE);
	}
	
	/**
	 * 添加用户收藏课程记录
	 * @param courseId
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public void courseFav(String courseId,String userId,String appkey)throws XueWenServiceException{
		favService.addFavNotCheck(Config.YXTDOMAIN, appkey, courseId, Config.TYPE_COURSE, userId);
	}
	
	/**
	 * 增加课程收藏数量
	 * @throws XueWenServiceException
	 */
	public void increaseFavCount(String courseId,int increaseNum)throws XueWenServiceException{
		newCourseTemplate.increaseFavCount(courseId, increaseNum);
	}
	/**
	 * 判断课程是否被用户学习
	 * @param courseId
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean isCourseStudyByUser(String courseId,String userId)throws XueWenServiceException{
		return studyService.isUserFav(userId, Config.YXTDOMAIN, courseId, Config.TYPE_COURSE);
	}
	/**
	 * 添加用户学习课程记录
	 * @param courseId
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public void courseStudy(String courseId,String userId,String appkey)throws XueWenServiceException{
		studyService.addStudyNotCheck(Config.YXTDOMAIN, appkey, courseId,  Config.TYPE_COURSE, userId);
	}
	
	/**
	 * 增加课程学习数量
	 * @throws XueWenServiceException
	 */
	public void increaseStudyCount(String courseId,int increaseNum)throws XueWenServiceException{
		newCourseTemplate.increaseStudyCount(courseId, increaseNum);
	}
	/**
	 * 添加用户分享课程记录
	 * @param courseId
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public void courseShare(String courseId,String userId,String appkey,String toType,String toAddr)throws XueWenServiceException{
		shareService.addShare(userId, Config.YXTDOMAIN, appkey, courseId, Config.TYPE_COURSE,toType, toAddr);
	}
	
	/**
	 * 增加课程分享数量
	 * @throws XueWenServiceException
	 */
	public void increaseShareCount(String courseId,int increaseNum)throws XueWenServiceException{
		newCourseTemplate.increaseShareCount(courseId, increaseNum);
	}
	/**
	 * 
	 * @Title: addFav
	 * @Description: 收藏数量加1
	 * @param id void
	 * @throws
	 */
	public void addFav(String id){
		NewCourse newCourse=courseRepository.findOne(id);
		newCourse.setFavCount(newCourse.getFavCount()+1);
		courseRepository.save(newCourse);
	}

	public Page<NewCourse> search(String keywords, Pageable pageable) throws XueWenServiceException{
		if(StringUtil.isBlank(keywords)){
			return courseRepository.findAll(pageable);
		}else{
			keywords = ".*?(?i)"+keywords+".*";
			Page<NewCourse> newCourses = courseRepository.findByTitleRegexAndCheckedOrIntroRegexAndChecked(keywords,true,keywords,true,pageable);
			return newCourses;
		}
	}
	/**
	 * 根据关键字搜索课程，不分页
	 * @param keywords
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<NewCourse> searchRspIds(String keywords) throws XueWenServiceException{
		keywords = ".*?(?i)"+keywords+".*";
		return newCourseTemplate.searchByTitelAndIntro(keywords);
	}
	
	
	public List<Object> searchByKeywordsRspIdsList(String keywords)throws XueWenServiceException{
		List<NewCourse> newCourses=searchRspIds(keywords);
		if(newCourses == null){
			return null;
		}else{
			List<Object> newCoursesIdList=new ArrayList<Object>(newCourses.size());
			for(NewCourse newCourse:newCourses){
				newCoursesIdList.add(newCourse.getId());
			}
			return newCoursesIdList;
		}
	}
	
	/**
	 * @throws XueWenServiceException 
	 * @return 
	 * 
	 * @Title: praiseCourse
	 * @Description: 课程点赞
	 * @param courseId
	 * @param currentUser void
	 * @throws
	 */
	public NewCourse praiseCoursePc(String courseId, User currentUser) throws XueWenServiceException {
		if(StringUtil.isBlank(courseId)){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		NewCourse course = courseRepository.findOne(courseId);
		if(course == null){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		praiseService.addPraiseTip(Config.YXTDOMAIN, Config.APPKEY_PC,courseId,Config.TYPE_COURSE_GROUP,currentUser.getId());
		course.setPraiseCount(course.getPraiseCount()+1);
		return courseRepository.save(course);
	}
	/**
	 * @throws XueWenServiceException 
	 * @return 
	 * 
	 * @Title: praiseCourse
	 * @Description: 课程收藏
	 * @param courseId
	 * @param currentUser void
	 * @throws
	 */
	public NewCourse favCoursePc(String courseId, String userId) throws XueWenServiceException {
		if(StringUtil.isBlank(courseId)){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		NewCourse course = courseRepository.findOne(courseId);
		if(course == null){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		favService.addFavPc(Config.YXTDOMAIN, Config.APPKEY_PC,courseId,Config.TYPE_COURSE_GROUP,userId);
		course.setFavCount(course.getFavCount()+1);
		return courseRepository.save(course);
	}

//	/**
//	 * @throws XueWenServiceException 
//	 * 
//	 * @Title: getCourseByTagPc
//	 * @Description: 按标签查找newCourse
//	 * @param tagName
//	 * @param pageable
//	 * @return List<JSONObject>
//	 * @throws
//	 */
//	public Map<String,Object> getCourseByTagPc(String tagName, Pageable pageable) throws XueWenServiceException {
//		//获取该标签下的所有课程id集合
//		List<String> courseIds = tagService.findItemIds(Config.YXTDOMAIN, tagName, Config.TAG_TYPE_COURSE, 1000);
//		//根据所有课程id集合 查出所有关联群组的 课程群组关系
//		Page<GroupCourse> groupCourses = groupCourseService.findByCourseIn(courseIds,pageable);
//		//关联群组的全部课程id的集合
//		List<Object> realCourseIds = new ArrayList<Object>();
//		for (GroupCourse groupCourse : groupCourses) {
//			realCourseIds.add(groupCourse.getCourse());
//		}
//		//关联群组的全部课程
//		List<NewCourse> courseReturns = courseRepository.findByIdIn(realCourseIds);
//		//将课程放入map
//		Map<Object,NewCourse> map = new HashMap<Object, NewCourse>();
//		for (NewCourse newCourse : courseReturns) {
//			map.put(newCourse.getId(), newCourse);
//		}
//		//返回时，拼装组id
//		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
//		String[] includeKey = {"id","title","studyCount","utime","logoUrl","recommendIndex"};
//		Map<String, Object> addGroupIdMap = null;
//		for (GroupCourse groupCourse : groupCourses) {
//			NewCourse course = map.get(groupCourse.getCourse());
//			addGroupIdMap = new HashMap<String, Object>();
//			addGroupIdMap.put("groupId", groupCourse.getGroup());
//			jsonObjects.add(YXTJSONHelper.getInObjectAttrJsonObject(course, addGroupIdMap, includeKey));
//		}
//		
//		Map<String,Object> mapReturn = new HashMap<String, Object>();
//		mapReturn.put("page", groupCourses);
//		mapReturn.put("result", jsonObjects);
//		return mapReturn;
//	}

	/**
	 * @throws XueWenServiceException 
	 * 
	 * @Title: getCourseByKeyWordsPc
	 * @Description: 按条件查找课程（在群组中的课程）
	 * @param tagName
	 * @param pageable
	 * @return List<JSONObject>
	 * @throws
	 */
	public Map<String,Object> getCourseByKeyWordsPc(String keyWords, Pageable pageable) throws XueWenServiceException {
		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
		
		Page<NewCourse> courses = null;
		if(StringUtil.isBlank(keyWords)){
			courses = courseRepository.findByShareCountGreaterThanAndChecked(0,true, pageable);
		}else{
//			keyWords = ".*?(?i)"+keyWords+".*";
			courses = courseRepository.findByShareCountGreaterThanAndTitleLikeAndCheckedOrIntroLikeAndCheckedOrTagNamesLikeAndChecked(0, keyWords, true,pageable);
		}
//		//搜索到的全部课程的id
//		List<String> courseIds = new ArrayList<String>();
//		Map<Object,NewCourse> map = new HashMap<Object, NewCourse>();
//		for (NewCourse newCourse : courses) {
//			courseIds.add(newCourse.getId());
//			map.put(newCourse.getId(), newCourse);
//		}
		
//		List<GroupCourse> groupCourses = groupCourseService.findByCourseIn(courseIds);
		String[] includeKey = {"id","title","studyCount","utime","logoUrl","recommendIndex","price","pricemodel"};
		
		
	
//		Map<String, Object> addGroupIdMap = null;
//		for (GroupCourse groupCourse : groupCourses) {
//			NewCourse course = map.get(groupCourse.getCourse());
//			addGroupIdMap = new HashMap<String, Object>();
//			addGroupIdMap.put("groupId", groupCourse.getGroup());
//			jsonObjects.add(YXTJSONHelper.getInObjectAttrJsonObject(course, addGroupIdMap, includeKey));
//		}
		for (NewCourse course : courses.getContent()) {
			jsonObjects.add(getCourseJson(course, includeKey));
		}
		Map<String,Object> mapReturn = new HashMap<String, Object>();
		mapReturn.put("page",courses );
		mapReturn.put("result", jsonObjects);
		return mapReturn;
	}
	//...
	public JSONObject getCourseJson(NewCourse course ,String...includeKey) throws XueWenServiceException{
		Map<String, Object> map=new HashMap<String, Object>();
		JSONObject newGroupCourse = newGroupCourseService.findOneByCourseId(course.getId());
		NewGroupCourse groupCourse = newGroupCourseService.findNewGroupCourseByCourseId(course.getId());
		if(newGroupCourse!=null){
			map.put("groupId", newGroupCourse.get("id"));
			map.put("groupName", newGroupCourse.get("groupName"));
			map.put("groupLogoUrl", newGroupCourse.get("logoUrl"));
			
			if(groupCourse!=null){
				map.put("groupCourseId", groupCourse.getId());
			}
		}else{
			map.put("groupId", "");
			map.put("groupName", "");
			map.put("groupLogoUrl", "");
			if(groupCourse==null){
				map.put("groupCourseId","");
			}
		}
		return YXTJSONHelper.getInObjectAttrJsonObject(course, map, includeKey);
	}
	
	/**
	 * 删除
	 * @Title: deletec
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @throws XueWenServiceException void
	 * @throws
	 */
	public void deletec() throws XueWenServiceException{
		 List<NewCourse>courses=courseRepository.findAll();
		 for(NewCourse course:courses){
			 List<Object>chapters=course.getChapters();
			 List<NewChapter> chs=chapterService.findChapterList(chapters);
			 for(NewChapter chapter:chs){
				 // List<Lesson> lessons=chapter.getLessons();
				 List<Lesson>lessons=lessonService.findByIdIn(chapter.getLessonIds());
				  for(Lesson lesson:lessons){
					   if(lesson.getLength()==0){
						   lessonService.delete(lesson);
						   chapterService.delete(chapter);
						   courseRepository.delete(course);
					   }
				  }
				  
				  
			 }
		 }
		 
	}

	/**
	 * @throws XueWenServiceException 
	 * 
	 * @Title: getGroupCoursesPc
	 * @Description: 群组下的课程
	 * @param pageable
	 * @param id
	 * @return Page<GroupCourse>
	 * @throws
	 */
	public Page<NewCourse> getGroupCoursesPc(Pageable pageable, String groupId) throws XueWenServiceException {
		if (StringUtils.isBlank(groupId)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		List<GroupCourse> groupCourses=groupCourseService.findByGroupIn(groupId);
		List<Object> courseIds = new ArrayList<Object>();
		for (GroupCourse groupCourse : groupCourses) {
			courseIds.add(groupCourse.getCourse());
		}
		Page<NewCourse> page = courseRepository.findByIdInAndChecked(courseIds,true,pageable);
		return page;
	}
	
	/**
	 * @throws XueWenServiceException 
	 * 
	 * @Title: getGroupCoursesPcNew
	 * @Description: 群组下的课程
	 * @param pageable
	 * @param id
	 * @return Page<GroupCourse>
	 * @throws
	 */
	public Page<NewCourse> getGroupCoursesPcNew(Pageable pageable, String groupId,String keyWords,Boolean checked) throws XueWenServiceException {
		if (StringUtils.isBlank(groupId)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		
		List<NewGroupCourse>newGroupCourses=newGroupCourseService.findByGroupIdPC(groupId);
		List<Object> courseIds = new ArrayList<Object>();
		for (NewGroupCourse groupCourse : newGroupCourses) {
			courseIds.add(groupCourse.getCourse());
		}
		Page<NewCourse> page=null;
		if(checked!=null){
			page = courseRepository.search(checked, keyWords, courseIds, pageable);
		}else{
			page = courseRepository.searchAll(keyWords, courseIds, pageable);
		}
		return page;
	}
	

	public List<JSONObject> toResp(List<NewCourse> content) {
		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
		if (content != null && content.size() != 0) {
			String[] includeKey = {"id","logoUrl","title","utime","ctime","studyCount","recommendIndex",
					"studyCount","favCount","shareCount","tags","tagNames","praiseCount","price","pricemodel"};
			for (NewCourse newCourse : content) {
				Map<String, Object> map=new HashMap<String, Object>();
				map.put("isChecked", newCourse.isChecked());
				jsonObjects.add(YXTJSONHelper.getInObjectAttrJsonObject(newCourse,map, includeKey));
			}
		}
		return jsonObjects; 
	}
    
	/**
	 * @throws XueWenServiceException 
	 * 
	 * @Title: getRelCourse
	 * @Description: 根据课程获取相关课程 （标签）
	 * @param courseId
	 * @return List<NewCourse>
	 * @throws
	 */
	public JSONArray getRelCourse(String courseId,int count) throws XueWenServiceException {
		if(StringUtil.isBlank(courseId)){
			return null;
		}
		String tags=tagService.findTagByItemIdAndType(Config.YXTDOMAIN, courseId, Config.TAG_TYPE_COURSE);
		if(StringUtil.isBlank(tags)){
			return null;
		}
		List<String>coueseIds=tagService.findItemIds(Config.YXTDOMAIN, tags, Config.TAG_TYPE_COURSE, count);
		if(coueseIds==null){
			return null;
		}
		List<NewCourse>courses= courseRepository.findByIdInAndChecked(coueseIds,true);
		
		String[]includeKey=new String[]{"id","title","intro","utime","logoUrl","studyCount","shareCount"};
		if(courses!=null){
			 return YXTJSONHelper.getINListObjectAttrJsonArray(courses, includeKey);
		}
		else{
			return null;
		}
	}
	
	/**
	 * 根据课程Id删除课程
	 * @param courseId
	 * @throws XueWenServiceException
	 */
	public void deleteById(String courseId)throws XueWenServiceException{
		NewCourse newCourse=findOneNewCourseByIdRspOnlyIdAndChapters(courseId);
		if(newCourse != null){
			//删除收藏记录
			favService.deleteBySourceId(courseId);
			//删除分享记录
			shareService.deleteBySourceId(courseId);
			//删除学习记录
			studyService.deleteBySourceId(courseId);
			//删除群组课程相关(包括群组课程收藏/学习/分享,用户群组课程记录等)
			newGroupCourseService.deleteGroupCourseListByCourseId(courseId);
			//删除章节以及章节下所有课时，课时下所有知识
			if(newCourse.getChapters() !=null && newCourse.getChapters().size()>0){
				newChapterService.deleteByChapterIds(newCourse.getChapters());
			}
			//删除课程记录
			newCourseTemplate.deleteById(courseId);
			//删除排行榜记录
			boxTemplate.deleteBysourceId(courseId);
		}
		
	}
    /**
     * @throws XueWenServiceException 
     * 
     * @Title: findOneById
     * @Description: 获取课信息
     * @param courseId
     * @return JSONObject
     * @throws
     */
	public JSONObject findOneById(String courseId,User vuser,String groupId) throws XueWenServiceException {
		if(StringUtil.isBlank(courseId)){
			throw new XueWenServiceException(Config.STATUS_201, "参数不能为空", null);
		}
		NewCourse course=courseRepository.findOne(courseId);
		if(course==null){
			throw new XueWenServiceException(Config.STATUS_201, "参数Id错误 未找到相关课程", null);
		}
		User user=userService.getOneByUserId((String)course.getCreateUser());
		if(user!=null){
			course.setCreateUser(user);
			
		}
		//判断当前用户是否赞过
		boolean isHavaPraise=false;
		if(vuser!=null){
			isHavaPraise=praiseService.existByUserIdAndSourceId(courseId,vuser.getId());
		}else {
			isHavaPraise=false;
		}
		if(StringUtil.isBlank(groupId)){
			//推荐一个群组
		    JSONObject res = newGroupCourseService.findOneByCourseId(courseId);
		    
		    if(res!=null){
		    	 groupId=res.get("id").toString();  
		    }
		}
		
	    Map<String, Object> addAndModifyMap=new HashMap<String, Object>();
	    JSONObject jsonObject=YXTJSONHelper.excludeAttrJsonObject(user, new String[]{"interestJob","job","openFireUser"});
	    addAndModifyMap.put("createUser", jsonObject);
	    if(vuser!=null){
	    	addAndModifyMap.put("isBuy", userBuyCourseService.findOneByUserIdAndCourseId(vuser.getId(), courseId));
	    }else{
	    	addAndModifyMap.put("isBuy", false);
	    }
	    String[] includeKey=new String[]{"id","title","intro","favCount","shareCount","praiseCount","studyCount","logoUrl","viewCount","tagNames","ctime","pricemodel","price","original"};
	    JSONObject object= YXTJSONHelper.getInObjectAttrJsonObject(course, addAndModifyMap, includeKey);
	    XueWenGroup group=null;
	    if(groupId!=null){
	    	group=groupService.findById(groupId);
	    }
	    if(group!=null){
	    	object.put("groupName", group.getGroupName());
	    	object.put("logoUrl", group.getLogoUrl());
	    	}
	    if(course.getCategoryId()!=null){
	    	Category c=categoryService.findById(course.getCategoryId());
	    	if(c!=null){
	    		object.put("categoryId", c.getId());
	    		object.put("categoryName", c.getCategoryName());
	    	}
	    }
	    if(course.getChildCategoryId()!=null){
	    	Category c=categoryService.findById(course.getChildCategoryId());
	    	if(c!=null){
	    		object.put("childCategoryId", c.getId());
	    		object.put("childCategoryName", c.getCategoryName());
	    	}
	    }
	    
	    object.put("groupId", groupId);
	    object.put("isHavaPraise", isHavaPraise);
	    if(object.size()==0){
	    	return null;
	    }
	    return object;
	}
	
	/**
	 * 增加课程购买次数
	 * @param courseId
	 * @throws XueWenServiceException
	 */
	public void addBuyCount(String courseId) throws XueWenServiceException{
		if(StringUtil.isBlank(courseId)){
			throw new XueWenServiceException(Config.STATUS_201, "参数不能为空", null);
		}
		newCourseTemplate.addBuyCount(courseId);
	}
	/**
	 * 
	 * @Title: addViewCount
	 * @Description:课程浏览次数加1 
	 * @param courseId
	 * @throws XueWenServiceException void
	 * @throws
	 */
	public void addViewCount(String courseId) throws XueWenServiceException{
		if(StringUtil.isBlank(courseId)){
			throw new XueWenServiceException(Config.STATUS_201, "参数不能为空", null);
		}
		newCourseTemplate.addViewCount(courseId);
	}
	
	public NewCourse findOne(String id){
		return  courseRepository.findOne(id);
	}
	
	/**
	 * 分页获取未审核的课程
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<NewCourse> getNotCheckedCourse(Pageable pageable )throws XueWenServiceException{
		return courseRepository.findByChecked(false, pageable);
	}
	
	/**
	 * 课程审核通过，并添加分类
	 * @param courseId
	 * @param categoryId
	 * @param childCategoryId
	 * @throws XueWenServiceException
	 */
	public NewCourse addCheckedAndCategory(String courseId,String categoryId,String childCategoryId)throws XueWenServiceException{
		NewCourse course=findOne(courseId);
		
		List<NewGroupCourse> l= newGroupCourseService.findBycourseId(course.getId());
		
		for(NewGroupCourse n:l){
			n.setReview(true);
			newGroupCourseService.saveGroupCourse(n);
		}
		
		if(course !=null ){
			course.setChecked(true);
//			course.setCategoryId(categoryId);
//			course.setChildCategoryId(childCategoryId);
			return courseRepository.save(course);
		}else{
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_PROPERTIESERROR_201, null);
		}
	}
	/**
	 * 根据课程Id查询课程，只返回课程的分类信息
	 * @param courseId
	 * @return
	 * @throws XueWenServiceException
	 */
	public NewCourse findOneNewCourseByIdRspCategoryInfo(String courseId)throws XueWenServiceException{
		return newCourseTemplate.findOneNewCourseByIdRspCategoryInfo(courseId);
	}
	
	/**
	 * 根据一级分类查询课程
	 * @param categoryId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<NewCourse> findByCategoryId(String categoryId,Pageable pageable)throws XueWenServiceException{
		return courseRepository.findByCheckedAndCategoryId(true,categoryId, pageable);
	}
	
	/**
	 * 根据一级分类，且Id集合，查询出不在此Id集合内，且属于此分类下的课程
	 * @param categoryId
	 * @param ids
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<NewCourse> findByCategoryIdNotInList(String categoryId,List<Object> ids,Pageable pageable)throws XueWenServiceException{
		return courseRepository.findByCheckedAndCategoryIdAndIdNotIn(true, categoryId, ids, pageable);
	}
	/**
	 * 根据一级分类ID统计此一级分类下面的课程数量
	 * @param categoryId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public int countCheckedCourseByCategoryId(String categoryId)throws XueWenServiceException{
		return courseRepository.countByCheckedAndCategoryId(true,categoryId);
	}
	/**
	 * 根据二级分类查询课程
	 * @param childCategoryId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<NewCourse> findByChildCategoryId(String childCategoryId,Pageable pageable)throws XueWenServiceException{
		return courseRepository.findByCheckedAndChildCategoryId(true,childCategoryId, pageable);
	}
	
	/**
	 * 根据二级分类查询不再此Id集合的课程
	 * @param childCategoryId
	 * @param ids
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<NewCourse> findByChildCategoryIdNotInList(String childCategoryId,List<Object> ids,Pageable pageable)throws XueWenServiceException{
		return courseRepository.findByCheckedAndChildCategoryIdAndIdNotIn(true,childCategoryId, ids,pageable);
	}
	/**
	 * 根据分类查询课程，优先级  二级分类---》一级分类----》所有
	 * @param categoryId
	 * @param childCategoryId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<NewCourse> findByCategory(String categoryId,String childCategoryId,Pageable pageable)throws XueWenServiceException{
		if(!StringUtil.isBlank(childCategoryId)){
			return findByChildCategoryId(childCategoryId,pageable);
		}else if(!StringUtil.isBlank(categoryId)){
			return findByCategoryId(categoryId,pageable);
		}else{
			return getCheckedCourses(pageable);
		}
	}
	
	/**
	 * 根据分类查询课程，课程不再群组的小组课堂内，前提：不在群组的小组课堂，然后 优先级  二级分类---》一级分类----》所有
	 * @param categoryId
	 * @param childCategoryId
	 * @param groupId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<NewCourse> findByCategoryNotInGroup(String categoryId,String childCategoryId,String groupId,Pageable pageable)throws XueWenServiceException{
		//获取此群下已经有的群组课程ID集合
		List<Object> groupCourseIds=newGroupCourseService.findGroupCourseCourseIdsByGroupId(groupId);
		if(!StringUtil.isBlank(childCategoryId)){
			return findByChildCategoryIdNotInList(childCategoryId,groupCourseIds,pageable);
		}else if(!StringUtil.isBlank(categoryId)){
			return findByCategoryIdNotInList(categoryId,groupCourseIds,pageable);
		}else{
			return getCoursesNotInGroup(groupId,pageable);
		}
	}
	
	/**
	 * 统计某二级分类下面课程的数量
	 * @param childCategoryId
	 * @return
	 * @throws XueWenServiceException
	 */
	public int countCheckedCourseByChildCategoryId(String childCategoryId,String groupId)throws XueWenServiceException{
		if(StringUtil.isBlank(groupId)){
			return courseRepository.countByCheckedAndChildCategoryId(true, childCategoryId);
		}else{
		//获取此群下已经有的群组课程ID集合
		List<Object> groupCourseIds=newGroupCourseService.findGroupCourseCourseIdsByGroupId(groupId);
		return courseRepository.countByCheckedAndChildCategoryIdAndIdNotIn(true, childCategoryId,groupCourseIds);
		}
	}
	
	/**
	 * 为所有老课程数据添加未审核字段
	 * @throws XueWenServiceException
	 */
	public void addNotCheckedForAllCourse()throws XueWenServiceException{
		newCourseTemplate.addNotCheckedForAllCourse();
	}
	
	/**
	 * 为所有课程添加分类
	 * @throws XueWenServiceException
	 */
	public void addCourseCategory()throws XueWenServiceException{
		List<Category> category = categoryService.findAllPrimary();
		Category cate = null;
		Category childCategory = null;
		String categoryId = "";
		String childId = "";
		if(category!=null && category.size() > 0){
			cate = category.get(StringUtil.getOneInt(category.size()));
			categoryId = cate.getId();
			List<Category> child =  categoryService.findSecondByPrimaryId(categoryId);
				List<NewCourse> nc = courseRepository.findAll();
				NewCourse one = null;
				for(int  i= 0 ; i < nc.size(); i++){
					if(child!=null && child.size() > 0){
						childCategory = child.get(StringUtil.getOneInt(child.size()));
						childId = childCategory.getId();
					}
					one = nc.get(i);
					one.setCategoryId(categoryId);
					one.setChildCategoryId(childId);
					one.setChecked(true);
					courseRepository.save(one);
				}
				
			}
	}
	/**
	 * 课程创建，此时创建课程，最好带有章节Id信息
	 * @param newCourse
	 * @return
	 * @throws XueWenServiceException
	 */
	public NewCourse createNewCourse(NewCourse newCourse,User user)throws XueWenServiceException{
		if(newCourse == null || StringUtil.isBlank(newCourse.getTitle())  || StringUtil.isBlank(newCourse.getIntro())){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_PROPERTIESERROR_201,null);
		}
		newCourse.setRecommendIndex("0");
		if(user!=null){
			newCourse.setCreateUser(user.getId());
			newCourse.setCreateUserName(user.getNickName());
		}
		newCourse.setCtime(System.currentTimeMillis());
		newCourse.setUtime(System.currentTimeMillis());
		return courseRepository.save(newCourse);
	}
	
	/**
	 * 
	 * @Title: findOneByChaptersIn
	 * @auther Tangli
	 * @Description: 通过章节取课程
	 * @param id
	 * @return NewCourse
	 * @throws
	 */
	public NewCourse findOneByChaptersIn(String id) {
		return courseRepository.findOneByChaptersIn(id);
	}
	
	public NewCourse modifyCourse(NewCourse newCourse,String chapterIds) throws XueWenServiceException{
		List<Object> idList=new ArrayList<Object>();
		User user;
		if (StringUtil.isBlank(newCourse.getId())) {
			throw new XueWenServiceException(Config.STATUS_201, "参数不能为空", null);
		}
		if(chapterIds==null||"".equals(chapterIds)){
			chapterIds="";
		}else{
			String[] ids=chapterIds.split(",");
			String isbuy="1";
			if(newCourse.getPricemodel()==1){
				isbuy="0";
			}
			for (String string : ids) {
				
				NewChapter chapter=chapterService.findOneChapterByIdRspExcLessons(string);
				chapter.setIsUsed(2);
				chapterService.save(chapter);
				List<String> l=chapter.getLessonIds();
				List<Lesson> ll=lessonService.findByIdIn(l);
				if(ll!=null||ll.size()!=0){
				for(Lesson l1:ll){
					l1.setIsbuy(isbuy);
					lessonService.save(l1);
				}
				}
				idList.add(string);
			}
		}
		
		newCourse.setRecommendIndex("0");
		newCourse.setCtime(System.currentTimeMillis());
		newCourse.setUtime(System.currentTimeMillis());
		newCourse.setChapters(idList);
		//打标签
		if(newCourse.getCreateUser()==null||"".equals(newCourse.getCreateUser())){
			 user=null;
		}
		else{
			 user=userService.findOne(newCourse.getCreateUser().toString());
		}
		
		
		String tag="";
		if(StringUtil.isBlank(newCourse.getTagNames())){
			tag=localTagService.getTagNamesByAnalysis(newCourse.getTitle());
			newCourse.setTagNames(tag);
		}else{
			tag=newCourse.getTagNames();
		}
		if(user!=null){
			tagService.tagForObj(user, tag, Config.TAG_TYPE_COURSE, newCourse.getId());
		}
		return courseRepository.save(newCourse);
	}
	public NewCourse modifyBaseInfo(NewCourse newCourse) throws XueWenServiceException{
		User user;
		if (StringUtil.isBlank(newCourse.getId())) {
			throw new XueWenServiceException(Config.STATUS_201, "参数不能为空", null);
		}
		newCourse.setUtime(System.currentTimeMillis());
		//打标签
		if(newCourse.getCreateUser()==null||"".equals(newCourse.getCreateUser())){
			 user=null;
		}
		else{
			 user=userService.findOne(newCourse.getCreateUser().toString());
		}
		String tag="";
		if(StringUtil.isBlank(newCourse.getTagNames())){
			tag=localTagService.getTagNamesByAnalysis(newCourse.getTitle());
			newCourse.setTagNames(tag);
		}else{
			tag=newCourse.getTagNames();
		}
		if(user!=null){
			tagService.tagForObj(user, tag, Config.TAG_TYPE_COURSE, newCourse.getId());
		}
		//修改包含该课程的群动态资料
		
		List<GroupDynamic>  g=groupDynamicRepository.findByCourseId(newCourse.getId());
			if(g!=null)
			{
				for(GroupDynamic gg:g){
					gg.setTitle(newCourse.getTitle());
					gg.setContent(newCourse.getIntro());
					Images images=new Images();
					images.setPicUrl(newCourse.getLogoUrl());
					List<Images> l=new ArrayList<Images>();
					l.add(images);
					gg.setImages(l);
					groupDynamicRepository.save(gg);
				}
			}
		
		return courseRepository.save(newCourse);
	}
	
	
	/**
	 * 
	 * 根据位置中的群组列表返回群组列表
	 * 
	 * @param boxs
	 * 
	 * @return
	 * 
	 * @throws XueWenServiceException
	 */

	public List<Object> toBoxResponses(List<Box> boxs) throws XueWenServiceException {

		List<Object> courseRes = new ArrayList<Object>();
		if (boxs != null && boxs.size() > 0) {
			for (Box box : boxs) {
				NewCourse newcourse = courseRepository.findOne(box.getSourceId().toString());
				if (newcourse != null) {
					// Map<String,Object> addAndModifyMap=new HashMap<String,
					// Object>();
					// addAndModifyMap.put("boxId", box.getId());
					// String[] exclude =
					// {"post","praiseResponse","position","tagName","group","categoryId","childCategoryId"};
					courseRes.add(newcourse);
				}
			}
		}
		return courseRes;

	}
	
	
	/**
	 * 分页获取不在此位置的话题列表
	 * @param boxPostId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<NewCourse> findByBoxPostIdInBox(List<Object> ids,Pageable pageable)throws XueWenServiceException{
		return courseRepository.findByCheckedAndIdIn(true,ids, pageable);
}
	
	
	/**
	 * 分页获取不在此位置的话题列表
	 * @param boxPostId
	 * @return
	 * @throws XueWenServiceException
	 */
	public NewCourse findById(String id)throws XueWenServiceException{
		return courseRepository.findOne(id);
}
	
	
	/**
	 * 分页获取不在此位置的话题列表
	 * @param boxPostId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<NewCourse> findByBoxPostIdNotInBoxForSearch(String boxPostId,String keyword)throws XueWenServiceException{
		List<Object> ids=boxService.getSourceIdsByBoxPostId(boxPostId);
		return courseRepository.findByTitleRegexAndCheckedAndIdNotInOrIntroRegexAndCheckedAndIdNotIn(keyword,true,ids,keyword,true,ids);
	}
	/**
	 * 合并用户，将fromUser 创建的课程合并到toUserId
	 * @param fromUserId
	 * @param toUserId
	 * @throws XueWenServiceException
	 */
	public void mergeNewCourse(String fromUserId,String toUserId)throws XueWenServiceException{
		newCourseTemplate.mergeNewCourse(fromUserId, toUserId);
	}
	
	/**
	 * 
	 * @Title: addFavCount
	 * @auther tangli
	 * @Description:收藏数量变更
	 * @param sourceId
	 * @param i void
	 * @throws
	 */
	public void addFavCount(String sourceId, int i) {
		newCourseTemplate.addFavCount(sourceId,i);
		
	}
	
	
	/**
	 * @throws XueWenServiceException 
	 * 
	 * @auther tangli
	 * @Description: 用于手机端推广课程详情
	 * @param courseId
	 * @param user
	 * @return JSONObject
	 * @Date:2015年4月28日
	 * @throws
	 */
	public JSONObject findOneByIdForMobile(String courseId, User user) throws XueWenServiceException {
		NewCourse course = findOne(courseId);
		if (course != null) {
			Map<String, Object> map = new HashMap<String, Object>();
			NewGroupCourse groupCourse = newGroupCourseService.findNewGroupCourseByCourseId(courseId);
			XueWenGroup group = groupService.findById(groupCourse.getGroup()
					.toString());
			if (group != null) {
				map.put("ishavaGroup", true);
				map.put("groupId", group.getId());
				map.put("groupLogoUrl", group.getLogoUrl());
				map.put("groupName", group.getGroupName());
			} else {
				map.put("ishavaGroup", false);
			}
			if(user!=null){
				map.put("ishavaGroup",
						praiseService.existByUserIdAndSourceId(courseId,
								user.getId()));
				
				map.put("isBuy", userBuyCourseService.findOneByUserIdAndCourseId(user.getId(), course.getId()));
			}else{
				map.put("isHavaParise", false);
				map.put("isBuy",false);
			}
			List<Object> chapters=course.getChapters();
			if(chapters!=null&&chapters.size()>0){
				NewChapter chapter= newChapterService.findOne(chapters.get(0).toString());	
				if(chapter!=null){
					List<String>lessonIds=chapter.getLessonIds();	
					if(lessonIds!=null&&lessonIds.size()>0){
						Lesson lesson=lessonService.findOne(lessonIds.get(0));
						if(lesson!=null){
							String kngId=lesson.getKnowledge().toString();
							Knowledge kngKnowledge=knowledgeService.getById(kngId);
							if(kngKnowledge!=null){
								map.put("isHavaKng", true);
								map.put("kng", YXTJSONHelper.getInObjectAttrJsonObject(kngKnowledge, new HashMap<String,Object>(),"pcItems","appItems","kngType","name","logoUrl","id"));
								Map m=(Map) map.get("kng");
								m.put("isBuy", lesson.getIsbuy());
								map.put("kng", m);
							}else{
								map.put("isHavaKng", false);
								map.put("kng", null);
							}
						}else{
							map.put("isHavaKng", false);
							map.put("kng", null);
						}
					}
					else{
						map.put("isHavaKng", false);
						map.put("kng", null);
					}
				}else{
					map.put("isHavaKng", false);
					map.put("kng", null);
				}
			}else{
				map.put("isHavaKng", false);
				map.put("kng", null);
			}
			return YXTJSONHelper.getInObjectAttrJsonObject(course, map, "id",
					"title", "intro", "logoUrl", "viewCount","price","pricemodel","createUserName","createUser");
		}
		return null;
		
		
		
	}
	
	/**
	 * 
	 * @Title: updateChapter
	 * @Description: 更新课程下的章节（先删后创建）
	 * @param json
	 * @param courseId
	 * @return
	 * @throws XueWenServiceException NewCourse
	 * @throws
	 */
	public void updateChapter(String json,String courseId) throws XueWenServiceException{
		json=json.replaceAll("''", "\"");
		NewCourse course=findById(courseId);
		List<Object> chapterIds = new ArrayList<Object>();
		if (course!=null) {
			chapterIds=course.getChapters();
			if (chapterIds!=null) {
				//删除课程下所有章节
				chapterService.deleteByChapterIdsNoKng(chapterIds);
			}
		}
		JSONArray chapterArray=JSONArray.fromObject(json);
		if (chapterArray.size()!=0) {
			chapterIds=new ArrayList<Object>();
			for (Object object : chapterArray) {
				JSONObject chapterJson=JSONObject.fromObject(object);
				NewChapter chapter=new NewChapter();
				chapter.setIsUsed(2);
				chapter.setTitle(chapterJson.getString("title"));
				//课时
				JSONArray lessonArray=chapterJson.getJSONArray("lessons");
				List<String> lessonIds=new ArrayList<String>();
				if(lessonArray.size()!=0){
					for (Object object2 : lessonArray) {
						JSONObject lessonJson=JSONObject.fromObject(object2);
						Lesson lesson=new Lesson();
						lesson.setKnowledge(lessonJson.getString("knowledgeId"));
						lesson.setTitle(lessonJson.getString("title"));
						lesson.setCtime(System.currentTimeMillis());
						lesson.setUtime(System.currentTimeMillis());
						lesson=lessonService.save(lesson);
						lessonIds.add(lesson.getId());
					}
				}
				chapter.setLessonIds(lessonIds);
				chapter=newChapterService.save(chapter);
				
				chapterIds.add(chapter.getId()); 
			}
		}
		course.setChapters(chapterIds);
		save(course);
	}
	
	/**
	 * 重置所有课程的购买数，回复数，和好评度
	 * @throws XueWenServiceException
	 */
	public void allBuyCountAndPostCountAndFavProp()throws XueWenServiceException{
		List<NewCourse> nes=courseRepository.findAll();
		for(NewCourse ne:nes){
			ne.setBuyCount(0);
			ne.setPostCount(0);
			ne.setFavPostCount(0);
			ne.setFavProp("1");
			courseRepository.save(ne);
		}
	}
	
	/**
	 * 根据课程Id取课程的活动
	 * @param courseId
	 * @throws XueWenServiceException
	 */
	public JSONArray getCanUserCouponsByCourseIdAndUserId(String courseId,String userId)throws XueWenServiceException{
		String url=this.couponServiceUrl+Config.COUPON_COURSEACOUPONS_URL;
		Map<String,String> map=new HashMap<String, String>();
		map.put("courseid", courseId);
		map.put("userid", userId);
		RestfulTemplateUtil restfulTemplateUtil=new RestfulTemplateUtil();
		JSONObject obj=restfulTemplateUtil.getRestApiData(url, map);
		Object status=obj.get("status");
		if("200".equals(status.toString())){
			JSONObject data=obj.getJSONObject("data");
			JSONArray result=data.getJSONArray("result");
			String rs=result.toString();
			rs=rs.replaceAll("null", "\"\"");
			result=JSONArray.fromObject(rs);
			if(result != null && result.size()>0){
				return result;
			}
		}
		return null;
	}

}
