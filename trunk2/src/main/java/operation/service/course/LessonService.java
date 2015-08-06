package operation.service.course;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import operation.exception.XueWenServiceException;
import operation.pojo.cloudfile.Citem;
import operation.pojo.course.Knowledge;
import operation.pojo.course.Lesson;
import operation.pojo.course.NewChapter;
import operation.pojo.course.NewCourse;
import operation.repo.course.LessonRepository;
import operation.repo.course.LessonTemplate;
import operation.repo.course.NewCourseRepository;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import tools.Config;
import tools.DateUtil;
import tools.StringUtil;

import com.google.gson.JsonObject;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.rs.Entry;
import com.qiniu.api.rs.RSClient;

/**
 * 
 * @ClassName: LessonService
 * @Description: Lesson Service
 * @author JackTang
 * @date 2014年12月19日 上午9:14:48
 *
 */

@Service
public class LessonService {
	@Autowired
	private LessonRepository lessonRepository;
	@Autowired
	private LessonTemplate lessonTemplate;
	@Autowired
	private KnowledgeService knowledgeService;
	@Autowired
	private NewChapterService newChapterService;
	@Autowired
	private NewCourseService newCourseService;

	/**
	 * 
	 * @Title: save
	 * @Description: 插入课时
	 * @param lesson
	 *            void
	 * @throws
	 */
	public Lesson save(Lesson lesson) {
		return lessonRepository.save(lesson);
	}

	/**
	 * 
	 * @Title: save
	 * @Description: 批量插入课时
	 * @param lessons
	 *            void
	 * @throws
	 */
	public void save(List<Lesson> lessons) {
		lessonRepository.save(lessons);
	}

	/**
	 * @throws XueWenServiceException
	 * 
	 * @Title: json2Lesson
	 * @Description: Json转Lessn Bean
	 * @param obj
	 * @return Lesson
	 * @throws
	 */
	public Lesson json2Lesson(JsonObject obj) throws XueWenServiceException {
		Lesson lesson = new Lesson();
		if (!obj.get("Name").isJsonNull()) {
			lesson.setTitle(obj.get("Name").getAsString());
		}
		// 课程简介
		if (!obj.get("Summary").isJsonNull()) {
			lesson.setIntro(obj.get("Summary").getAsString());
		}
		// 课程图片
		if (!obj.get("ImageUrl").isJsonNull()) {
			lesson.setLogoUrl(obj.get("ImageUrl").getAsString());
		}
		if (!obj.get("OrderIndex").isJsonNull()) {
			// 课程序号
			lesson.setOrder(obj.get("OrderIndex").getAsInt());
		}
		// 设置服务器存储地址;未转码前地址
		if (!obj.get("KnowledgeFileUrl").isJsonNull()) {
			String url = obj.get("KnowledgeFileUrl").getAsString();
			lesson.setLocalUrl(url);

		}
		if (!obj.get("CoursewareItemType").isJsonNull()) {
			// 设置类别
			lesson.setType(obj.get("CoursewareItemType").getAsString());
			// if ("Video".equals(lesson.getType())) {
			//
			// // 设置播放地址
			// Map<String, String> payUrls = new HashMap<String, String>();
			//
			// // payUrls.put(url.substring(url.lastIndexOf(".")+1), url);
			// String url = lesson.getLocalUrl();
			// payUrls.put("mp4", url);
			// payUrls.put("flv", url.substring(0, url.lastIndexOf(".") + 1)
			// + "flv");
			// // lesson.setPlayUrls(payUrls);
			//
			// }
		}

		// 设置文件大小
		if (!obj.get("Bytes").isJsonNull()) {
			lesson.setLength(obj.get("Bytes").getAsLong());
		}
		// 设置时长
		if (!obj.get("FileSize").isJsonNull()) {
			String timeString = obj.get("FileSize").getAsString();
			if ("Video".equals(lesson.getType())) {

				lesson.setTimer(DateUtil.time2Long(timeString));
			} else {
				timeString=timeString.replaceAll("页", "").trim();
				timeString=timeString.replaceAll("码", "").trim();
				lesson.setTimer(Integer.valueOf(timeString));
			}

		}

		if (!obj.get("Bytes").isJsonNull()) {
			lesson.setLength(obj.get("Bytes").getAsLong());
		}
		
		
		
		// 设置创建时间
		if (!obj.get("CreateDate").isJsonNull()) {
			String sdate = obj.get("CreateDate").getAsString()
					.replaceAll("T", " ");
			long ctime = DateUtil.Sdate2Long(sdate);
			lesson.setCtime(ctime);
		}

		// 2014-12-23 修改knowledge
		Knowledge knowledge = new Knowledge();
		knowledge.setFurl(lesson.getLocalUrl());
		knowledge.setName(lesson.getTitle());
		knowledge.setLogoUrl(lesson.getLogoUrl());
		knowledge.setCuser("54b5b90be4b0d38094124f4d");
		knowledge.setIsPublic(true);
		List<Citem> citems = new ArrayList<Citem>();

		if ("Video".equals(lesson.getType())) {

			knowledge.setKngType(1);
			knowledge.setDuration(lesson.getTimer());
			Citem citem = new Citem();
			citem.setFormat("mp4");
			
			citem.setFurl(lesson.getLocalUrl().replace("_enc.mp4", ".mp4"));
			Citem citem1 = new Citem();
			citem1.setFormat("flv");
			citem1.setFurl(lesson.getLocalUrl().substring(0,
					lesson.getLocalUrl().lastIndexOf(".") + 1)
					+ "flv");
			citems.add(citem1);
			citems.add(citem);

		} else {
			// 文档 类 默认追加两个item
			Citem citem = new Citem();
			citem.setFormat("pdf");
			citem.setCode(0);
			citem.setFurl(lesson.getLocalUrl().substring(0,
					lesson.getLocalUrl().lastIndexOf(".") + 1)
					+ "pdf");

			Citem citem1 = new Citem();
			citem1.setFormat("swf");
			citem1.setCode(0);
			citem1.setFurl(lesson.getLocalUrl().substring(0,
					lesson.getLocalUrl().lastIndexOf(".") + 1)
					+ "swf");
			citems.add(citem);
			citems.add(citem1);

			knowledge.setKngType(2);
			knowledge.setPages(Integer.valueOf(lesson.getTimer()+""));

		}
		// 内容中心获取的知识 默认设置审核不通过
		knowledge.setStatus(Config.KNOWLEDGE_STAT_PROCESS);
		knowledge.setCcode(0);
		knowledge.setCitems(citems);
		// 添加适合pc 和app端的citems
		knowledgeService.addviewcitems(knowledge, citems);
		knowledge.setLogoUrl(lesson.getLogoUrl());
		knowledge.setCtime(lesson.getCtime());
		knowledge.setUtime(System.currentTimeMillis());
		knowledgeService.insert(knowledge);
		lesson.setKnowledge(knowledge.getId());
		return lesson;
	}
	
	/**
	 * 查询课时基本信息,包括（id,title,length,timer,logoUrl,order,localUrl,type,knowledgeId）
	 * @author hjn
	 * @param lessonId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Lesson findOneLessonBasicInfoById(String lessonId)throws XueWenServiceException{
		return lessonTemplate.findOneLessonBasicInfoById(lessonId);
	}
	/**
	 * 查询课时基本信息,包括（id,timer）
	 * @author hjn
	 * @param lessonId
	 * @return
	 * @throws XueWenServiceException
	 */
	public Lesson findOneLessonByIdRspIdAndTimer(String lessonId)throws XueWenServiceException{
		return lessonTemplate.findOneLessonByIdRspIdAndTimer(lessonId);
	}
	
	public List<Lesson> findByIdIn(List<String> ids){
		return lessonRepository.findByIdIn(ids);
		
	}
	public List<Lesson> findByIdIn(List<String> ids,Sort sort){
		return lessonRepository.findByIdIn(ids,sort);
		
	}

	public void delete(Lesson lesson) {
		lessonRepository.delete(lesson);
		
	}
	/**
	 * 根据课时的id集合删除所有课时
	 * @param lessonIds
	 * @throws XueWenServiceException
	 */
	public void deleteByIds(List<Object> lessonIds)throws XueWenServiceException{
		lessonTemplate.deleteByIds(lessonIds);
	}
	
	
	/**
	 * @throws XueWenServiceException 
	 * 
	 * @Title: checkLesson
	 * @Description: 课时审核
	 * @param status
	 * @param lessonId void
	 * @throws
	 */
	public void checkLesson(boolean status,String lessonId,String desc,String kngId) throws XueWenServiceException{
		//更新知识
		knowledgeService.checkKnowledge(status, desc, kngId);	
		//更新课时
		Lesson lesson=findOne(lessonId);
		if(lesson!=null){
			if(status){
				lesson.setStatus(Config.KNOWLEDGE_STAT_PASS);
			}
			else{
				lesson.setStatus(Config.KNOWLEDGE_STAT_FAILE);
			}
			lesson.setCheckDesc(desc);
			save(lesson);
			//更新课程
			NewChapter chapter=newChapterService.findOneByLessonIdIn(lesson.getId());
			if(chapter!=null){
				NewCourse course=newCourseService.findOneByChaptersIn(chapter.getId());
				if(course!=null){
					int count=course.getUnPassedCount();
					count--;
					count=count<0?0:count;
					course.setUnPassedCount(count);
					newCourseService.save(course);
				}
			}
		}
	}
	
	/**
	 * 
	 * @Title: findOne
	 * @auther Tangli
	 * @Description: 查找
	 * @param lessonId
	 * @return Lesson
	 * @throws
	 */
	public Lesson findOne(String lessonId) {
		return lessonRepository.findOne(lessonId);
	}
	
	/**
	 * 
	 * @Title: findOne
	 * @auther Tangli
	 * @Description: 查找
	 * @param lessonId
	 * @return Lesson
	 * @throws
	 */
	public List<Lesson> findAll() {
		return lessonRepository.findAll();
	}
	

	/**
	 * 创建章节，保存章节基本信息，此时章节中应该包含knowledge节点
	 * @param lesson
	 * @return
	 * @throws XueWenServiceException
	 */
	public Lesson createLesson(String title,String intro,String knowledgeId,String order,String userId)throws XueWenServiceException{
		if(StringUtil.isBlank(title)){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_PROPERTIESERROR_201,null);
		}
		Lesson lesson;
		if(!StringUtil.isBlank(knowledgeId)){
			Knowledge know=knowledgeService.getByIdNotByStatus(knowledgeId);
			if(know !=null){
				lesson=new Lesson(title, intro, Integer.valueOf(order), userId, know);
			}else{
				lesson=new Lesson(title, intro, Integer.valueOf(order), userId);
			}
		}else{
			lesson=new Lesson(title, intro, Integer.valueOf(order), userId);
		}
		lesson.setIsUsed(1);
		return lessonRepository.save(lesson);
	}
    
	/**
	 * 
	 * @Title: saveDurations
	 * @auther Tangli
	 * @Description: 更新lesson时长
	 * @param duration
	 * @param id void
	 * @throws
	 */
	public void saveDurations(Integer duration, String id) {
		List<Lesson> lessons=lessonRepository.findByKnowledge(id);
		for (Lesson lesson : lessons) {
			lesson.setTimer(duration);
		}
		save(lessons);	
	}
	/**
	 * @throws XueWenServiceException 
	 * 
	 * @Title: updateLesson
	 * @Description: 批量修改newlesson ，把视频的时长和文件大小批量跑到现有的数据上，课程不用重新生成
	 * @throws
	 */
	public void updateLesson() throws XueWenServiceException{
		System.out.println("开始更新");
		List<Lesson> lessons=lessonRepository.findAll();
		for (Lesson lesson : lessons) {
			Knowledge knowledge=knowledgeService.getById(lesson.getKnowledge().toString());
			if(knowledge!=null){
				if(knowledge.getDuration()!=0){
					lesson.setTimer(knowledge.getDuration()*1000);
				}
				lesson.setLength(knowledge.getFileSize());
				save(lesson);
			}
			
		}
		System.out.println("结束更新");
		
	}
	
	public void in(Pageable pageable){
		System.out.println("开始");
		Page<Lesson> list=lessonRepository.findAll(pageable);
		for (Lesson lesson : list) {
			if(lesson.getLocalUrl()!=null){
				if(lesson.getLocalUrl().indexOf("tpublic")!=-1){
					String temp=lesson.getLocalUrl().substring(lesson.getLocalUrl().indexOf("com/")+4);
					lesson.setLength(Long.parseLong(test(temp)));
					save(lesson);
				}
			}
			
			
		}
		System.out.println("结束");
		
	}
	/**
	 * 更新课时的时长和文件大小
	 * @throws Exception
	 */
	public void updateAllLessonSizeAndTime()throws Exception{
		List<Lesson> list=lessonRepository.findAll();
		for(Lesson l:list){
			if(l !=null && l.getKnowledge() !=null){
				Knowledge k=knowledgeService.getById(l.getKnowledge().toString());
				if(k !=null && !StringUtil.isBlank(k.getCacheUrl())){
					String url=k.getCacheUrl()+"?avinfo";
//					url=URLEncoder.encode(url);
//					logger.info("pwd===="+pwd+"========url:"+url);
					CloseableHttpClient httpclient = HttpClients.createDefault();
					HttpGet get=new HttpGet(url);
					ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
		                public String handleResponse(
		                        final HttpResponse response) throws ClientProtocolException, IOException {
		                    int status = response.getStatusLine().getStatusCode();
		                    if (status >= 200 && status < 300) {
		                        HttpEntity entity = response.getEntity();
		                        return entity != null ? EntityUtils.toString(entity) : null;
		                    } else {
		                        throw new ClientProtocolException("Unexpected response status: " + status);
		                    }
		                }
		            };
					String responseBody;
					try {
						responseBody = httpclient.execute(get,responseHandler);
						if(!StringUtil.isBlank(responseBody)){
							JSONObject j=JSONObject.fromObject(responseBody);
							if(j!=null){
								JSONObject f=j.getJSONObject("format");
								if(f!=null){
									Object s=f.get("size"); //视频文件大小
									Object d=f.get("duration");//视频时长
									if(s!=null){
										l.setLength(Long.valueOf(s.toString()));
									}
									if(d !=null){
										String dur=d.toString();
										int end=dur.indexOf(".");
										l.setTimer(Long.valueOf(d.toString().substring(0,end)));
									}
									lessonRepository.save(l);
								}
							}
							
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			
				}
			}
		}
	}
	
	public String test(String name){
		Map<String, String> m=new HashMap<String, String>();
		m.put("name", name);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>(); 
		map.setAll(m);
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject("http://localhost:8088/oss/knowledge/getFileSize", map, String.class,"");
//		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
//        RSClient client = new RSClient(mac);
//        Entry statRet = client.stat("tpublic", name);
//        return statRet.getFsize();
	}
	/**
	 * 修改课时创建人
	 * @param fromUserId
	 * @param toUserId
	 * @throws XueWenServiceException
	 */
	public void mergeLesson(String fromUserId,String toUserId)throws XueWenServiceException{
		lessonTemplate.mergeLesson(fromUserId, toUserId);
	}
	
	/**
	 * 
	 * @Title: findUnusedLessons
	 * @Description: 课时垃圾数据
	 * @param pageable
	 * @return Page<Lesson>
	 * @throws
	 */
	public Page<Lesson> findUnusedLessons(Pageable pageable){
		return lessonRepository.findByIsUsed(1, pageable);
	}
	/**
	 * @throws XueWenServiceException 
	 * 
	 * @Title: deleteById
	 * @Description: 删除课时以及课时下面的知识
	 * @throws
	 */
	public void deleteById(String id) throws XueWenServiceException{
		Lesson lesson=lessonRepository.findOne(id);
		if(lesson==null){
			throw new XueWenServiceException(Config.STATUS_201, "无此课时",null);
		}
		if(lesson.getKnowledge()!=null){
			List<Object> ids=new ArrayList<Object>();
			ids.add(lesson.getKnowledge());
			knowledgeService.deleteByIds(ids);
		}
	}
	public void update(Lesson lesson) throws XueWenServiceException{
		if (StringUtil.isBlank(lesson.getId())) {
			throw new  XueWenServiceException(Config.STATUS_201,"课时ID不可以空",null);
		}
		Lesson Lesson1=findOne(lesson.getId());
		Lesson1.setTitle(lesson.getTitle());
		Lesson1.setOrder(lesson.getOrder());
		Lesson1.setIsbuy(lesson.getIsbuy());
		save(Lesson1);
	}
	
}
