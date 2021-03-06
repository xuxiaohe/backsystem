package operation.controller.ring;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import operation.BaseController;
import operation.exception.XueWenServiceException;
import operation.pojo.course.NewCourse;
import operation.pojo.course.NewGroupCourse;
import operation.pojo.drycargo.Drycargo;
import operation.pojo.pub.QueryModel;
import operation.pojo.pub.QueryModelMul;
import operation.pojo.topics.Topic;
import operation.pojo.topics.TopicTopResult;
import operation.pojo.topics.WeeksTopicTopResult;
import operation.pojo.user.User;
import operation.service.course.NewGroupCourseService;
import operation.service.drycargo.DrycargoService;
import operation.service.topics.TopicRingService;
import operation.service.topics.TopicService;

import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tools.Config;
import tools.PageRequestTools;
import tools.ReponseDataTools;
import tools.ResponseContainer;

@RestController
@RequestMapping("/ring")
@Configuration
public class RingController extends BaseController {
	@Autowired
	private TopicService topicService;
	@Autowired
	private DrycargoService drycargoService;
	@Autowired
	private NewGroupCourseService newGroupCourseService;
	
	@Autowired
	private TopicRingService topicRingService;
	
	private static final Logger logger=Logger.getLogger(RingController.class);
	public RingController() {
		super();
	}
	
	/**
	 * 排行榜-精彩话题（按照话题创建时间倒序）
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("wonderfulTopic")
	public @ResponseBody ResponseContainer wonderfulTopic(HttpServletRequest request,QueryModel dm) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<Topic> topics = topicService.findWonderfulTopic(pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), topics);
			this.getReponseData().setResult((topicService.toJSONHelper(topics.getContent(),currentUser)));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		}
		catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
		
	}
	
	/**
	 * 排行榜-精彩干货或炫页（按照干货的创建时间倒序）
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("wonderfulDryCargo")
	public @ResponseBody ResponseContainer wonderfulDryCargo(HttpServletRequest request,QueryModel dm) {
		try {
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			String dryFlag = request.getParameter("dryFlag");//0代表干货1代表炫页
			if(StringUtil.isBlank(dryFlag)){
				dryFlag = "0";
			}
			Page<Drycargo> drycargo = drycargoService.ringDryCargo(Integer.parseInt(dryFlag),pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), drycargo);
			this.getReponseData().setResult((drycargoService.toJSONHelper(drycargo.getContent())));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} 
		catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
		
	}
	
	/**
	 * 排行榜-穿越干货或炫页（按照干货的浏览量）
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("throughDryCargo")
	public @ResponseBody ResponseContainer throughDryCargo(HttpServletRequest request,QueryModelMul dm) {
		try {
			List<String> sort = new ArrayList<String>();
			sort.add("replyCount");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			String dryFlag = request.getParameter("dryFlag");//0代表干货1代表炫页
			if(StringUtil.isBlank(dryFlag)){
				dryFlag = "0";
			}
			Page<Drycargo> drycargo = drycargoService.ringDryCargo(Integer.parseInt(dryFlag),pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), drycargo);
			this.getReponseData().setResult((drycargoService.toJSONHelper(drycargo.getContent())));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} 
		catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
		
	}
	
	/**
	 * 排行榜-精彩课程（按照课程创建时间倒序）
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("wonderfulCourse")
	public @ResponseBody ResponseContainer wonderfulCourse(HttpServletRequest request,QueryModelMul dm) {
//		try {
//			Pageable pageable = PageRequestTools.pageRequesMake(dm);
//			Page<NewGroupCourse> newGroupCourses=newGroupCourseService.findCourseByReview(pageable);
//			ReponseDataTools.getClientReponseData(getReponseData(), newGroupCourses);
//			this.getReponseData().setResult(newGroupCourseService.formateGroupCourseList(newGroupCourses.getContent()));
//			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
//		}
//		catch (XueWenServiceException e) {
//			e.printStackTrace();
//			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
//		}
		try {
			List<String> sort = new ArrayList<String>();
			sort.add("shareCount");
			//sort.add("studyCount");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<NewCourse> newCourses=newGroupCourseService.findCourse(pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), newCourses);
			this.getReponseData().setResult(newGroupCourseService.courseToGroupCourse(newCourses.getContent()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		}
		catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 排行榜-穿越课程（按照课程的学习人数）
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("throughCourse")
	public @ResponseBody ResponseContainer throughCourse(HttpServletRequest request,QueryModelMul dm) {
		try {
			List<String> sort = new ArrayList<String>();
			sort.add("studyCount");
			//sort.add("ctime");
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<NewCourse> newCourses=newGroupCourseService.findCourse(pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), newCourses);
			this.getReponseData().setResult(newGroupCourseService.courseToGroupCourse(newCourses.getContent()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} 
		catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
		
	}
	
	/**
	 * 排行榜-穿越话题（当前时间往前推7天内得回复数最高的话题）
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("throughTopic")
	public @ResponseBody ResponseContainer throughTopic(HttpServletRequest request,QueryModel dm) {
		try {
			dm.setMode("ASC");
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<WeeksTopicTopResult> weeksTopicTopResult = topicRingService.findAllWeeksTopic(pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), weeksTopicTopResult);
			this.getReponseData().setResult((topicRingService.toJSONHelperForWeeks(weeksTopicTopResult.getContent())));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		}
		catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
		
	}
	
	/**
	 * 排行榜-神帖话题（当前时间往前推24时内得回复数最高的话题）
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("godTopic")
	public @ResponseBody ResponseContainer godTopic(HttpServletRequest request,QueryModel dm) {
		try {
			dm.setMode("ASC");
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<TopicTopResult> topicTopResult = topicRingService.findAll(pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), topicTopResult);
			this.getReponseData().setResult((topicRingService.toJSONHelper(topicTopResult.getContent())));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		}
		catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
		
	}
	
	/**
	 * 从排行榜数据结构的list对象分页读取数据
	 * @param request
	 * @param dm
	 * @return
	 */
	 public @ResponseBody ResponseContainer getRinkDataFromArray(HttpServletRequest request,String n,String s,String keyFile,String keyValue) {
		 
			try {
				if(!StringUtil.isBlank(n) && Integer.parseInt(n)>=0 && !StringUtil.isBlank(s) && Integer.parseInt(s)>=0)
				 {
					//第几页 从1开始
				    int number =Integer.parseInt(n);
				    //每页记录条数
				    int pageSize=Integer.parseInt(s);
				    //主记录的条件 这个在数据里定义好 比如keyFile xxx_code=dayRank
					Query query=new Query(Criteria.where(keyFile).is(keyValue));
					//排除主记录中的字段
					query.fields().exclude("topic").exclude("drycargo").exclude("course");
					//list的属性名 后面俩参数相当于skip 和limit 参数
					query.fields().slice("group", (number-1)*pageSize, pageSize);
					//IndexBean indexbb =mongoTemplate.findOne(query, IndexBean.class, "indexBean");
					
				 }
				//this.getReponseData().setResult((topicRingService.toJSONHelper(topicTopResult.getContent())));
				return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
			}
			 catch (Exception e) {
				e.printStackTrace();
				return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
			}
		 
		 
	 }

}
