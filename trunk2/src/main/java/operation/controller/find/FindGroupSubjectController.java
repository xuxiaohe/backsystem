package operation.controller.find;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import operation.BaseController;
import operation.exception.XueWenServiceException;
import operation.pojo.box.Box;
import operation.pojo.course.GroupCourse;
import operation.pojo.drycargo.Drycargo;
import operation.pojo.dynamic.GroupDynamic;
import operation.pojo.group.XueWenGroup;
import operation.pojo.index.IndexBean;
import operation.pojo.index.NewIndexBean;
import operation.pojo.pub.QueryModel;
//import operation.pojo.drycargo.DrycargoBean;
import operation.pojo.pub.QueryModelMul;
import operation.pojo.topics.Topic;
import operation.pojo.user.User;
import operation.service.box.BoxService;
import operation.service.course.CourseService;
import operation.service.course.GroupCourseService;
//import operation.service.drycargo.DrycargoBeanService;
import operation.service.drycargo.DrycargoService;
import operation.service.dynamic.GroupDynamicService;
import operation.service.group.GroupService;
import operation.service.group.MyGroupService;
import operation.service.index.IndexService;
import operation.service.topics.TopicService;

import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tools.Config;
import tools.PageRequestTools;
import tools.ReponseDataTools;
import tools.ResponseContainer;
import tools.YXTJSONHelper;


@RestController
@RequestMapping("/find")
@Configuration
public class FindGroupSubjectController extends BaseController{
	
	private static final Logger logger=Logger.getLogger(FindGroupSubjectController.class);
	public FindGroupSubjectController() {
		super();
	}
	@Autowired
	public GroupService groupService;
	
	@Autowired
	public MyGroupService myGroupService;
	@Autowired
	public TopicService topicService;
	@Autowired
	public DrycargoService drycargoService;
	@Autowired
	public GroupCourseService groupCourseService;
	
	@Autowired
	public CourseService courseService;
	@Autowired
	public BoxService boxService;
	@Autowired
	public IndexService indexService;
	@Autowired
	public GroupDynamicService groupDynamicService;
	
	/**
	 * 系统参数
	 */
	@Value("${appindex.topic.boxId}")
	private String indexTopicBoxId;
	@Value("${appindex.dry.boxId}")
	private String indexDryBoxId;
	@Value("${appindex.xuanye.boxId}")
	private String indexXuanYeBoxId;

	
	/**
	 * 查询发现话题
	 * 
	 * @param request
	 * @return
	 */
	
	@RequestMapping("findTopic")
	public @ResponseBody ResponseContainer findTopic(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			List<String> sort = new ArrayList<String>();
			sort.add("ctime");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
//			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			Page<Box> boxs = boxService.findByBoxPostId(indexTopicBoxId, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), boxs);
			this.getReponseData().setResult((topicService.toBoxResponses(boxs.getContent())));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	
	
	
	/**
	 * 发现干货
	 * @param request
	 * @return
	 */
	@RequestMapping("findDrycargo")
	public @ResponseBody ResponseContainer findDrycargo(HttpServletRequest request,QueryModelMul dm) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			List<String> sort = new ArrayList<String>();
			//sort.add("weightSort");
			sort.add("ctime");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			String dryFlag = request.getParameter("dryFlag");//0代表干货1代表炫页
			if(StringUtil.isBlank(dryFlag)){
				dryFlag = "0";
			}
			Page<Drycargo> drycargo = drycargoService.findAll(Integer.parseInt(dryFlag),pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), drycargo);
			this.getReponseData().setResult((drycargoService.toResponeses(drycargo.getContent(),currentUser.getId())));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} 
		catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
//		try {
//			String token = request.getParameter("token");
//			User currentUser = this.getCurrentUser(token);
//			List<String> sort = new ArrayList<String>();
//			sort.add("weightSort");
//			sort.add("ctime");
//			dm.setSort(sort);
//			Pageable pageable = PageRequestTools.pageRequesMake(dm);
//			String dryFlag = request.getParameter("dryFlag");//0代表干货1代表炫页
//			if(StringUtil.isBlank(dryFlag) || "0".equals(dryFlag)){
//				dryFlag = "0";
//				Page<Box> boxs = boxService.findByBoxPostId(indexDryBoxId, pageable);
//				ReponseDataTools.getClientReponseData(getReponseData(), boxs);
//				this.getReponseData().setResult((drycargoService.toBoxResponses(boxs.getContent())));
//				return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
//			}else{
//				Page<Box> boxs = boxService.findByBoxPostId(indexXuanYeBoxId, pageable);
//				ReponseDataTools.getClientReponseData(getReponseData(), boxs);
//				this.getReponseData().setResult((drycargoService.toBoxResponses(boxs.getContent())));
//				return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
//			}
			
			
			
//			Page<Drycargo> drycargo = drycargoService.findAll(Integer.parseInt(dryFlag),pageable);
//			ReponseDataTools.getClientReponseData(getReponseData(), drycargo);
//			this.getReponseData().setResult((drycargoService.toResponeses(drycargo.getContent(),currentUser.getId())));
//			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
	//	} 
//		catch (Exception e) {
//			e.printStackTrace();
//			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
//		}
	}
	
	/**
	 * 发现小组课程
	 * @param request
	 * @return
	 */
	@RequestMapping("findGroupCourse")
	public @ResponseBody ResponseContainer findGroupCourse(HttpServletRequest request,QueryModelMul dm) {
		try {
			List<String> sort = new ArrayList<String>();
			sort.add("ctime");
			sort.add("studyCount");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<GroupCourse> groupCourse = groupCourseService.findAll(pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), groupCourse);
			this.getReponseData().setResult(groupCourseService.toResponseGroupsCourseList(groupCourse.getContent()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} 
		
		catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}

	}
	/**
	 * 查询发现更多话题
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("findAllTopic")
	public @ResponseBody ResponseContainer findAlTopic(HttpServletRequest request,Topic topic,QueryModelMul dm) {
		try {
			//根据请求参数封装一个分页信息对象
			List<String> sort = new ArrayList<String>();
			sort.add("displayOrder");
			sort.add("replyCount");
			sort.add("ctime");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<Topic> topicResult = topicService.findAlTopic(pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), topicResult);
			this.getReponseData().setResult(topicService.toResponses(topicResult.getContent()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		}catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getLocalizedMessage(), false,Config.RESP_MODE_10, "");
		}catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 发现更多干货
	 * @param request
	 * @return
	 */
	@RequestMapping("findAllDrycargo")
	public @ResponseBody ResponseContainer findAllDrycargo(
			HttpServletRequest request,Drycargo drycargo,QueryModelMul dm) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			List<String> sort = new ArrayList<String>();
			//sort.add("weightSort");
			sort.add("replyCount");
			sort.add("ctime");
			dm.setSort(sort);
			String dryFlag = request.getParameter("dryFlag");//0代表干货1代表炫页
			if(StringUtil.isBlank(dryFlag)){
				dryFlag = "0";
			}
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<Drycargo> drycargoSub = drycargoService.findAll(Integer.parseInt(dryFlag),pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), drycargoSub);
			this.getReponseData().setResult((drycargoService.toResponeses(drycargoSub.getContent(),currentUser.getId())));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		}catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}

	}
	
	/**
	 * 发现更多干货
	 * @param request
	 * @return
	 */
	@RequestMapping("findAllGroupCourse")
	public @ResponseBody ResponseContainer findAllGroupCourse(HttpServletRequest request,GroupCourse groupCourse,QueryModelMul dm) {
		try {
			List<String> sort = new ArrayList<String>();
			sort.add("studyCount");
			sort.add("ctime");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<GroupCourse> gc = groupCourseService.findAll(pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), gc);
			this.getReponseData().setResult(groupCourseService.toResponseGroupsCourseList(gc.getContent()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} 
		
		catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}

	}
	
	/**
	 * 根据分类查询发现话题
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("findTopicByCategory")
	public @ResponseBody ResponseContainer findTopicByCategory(HttpServletRequest request,QueryModelMul dm) {
		try {
			List<String> sort = new ArrayList<String>();
			sort.add("weightSort");
			sort.add("ctime");
			dm.setSort(sort);
			String categoryId = request.getParameter("categoryId");
			String childCategoryId = request.getParameter("childCategoryId");
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<Topic> topicResult = topicService.findTopicsByCategoryId(categoryId,childCategoryId,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), topicResult);
			this.getReponseData().setResult(topicService.toResponses(topicResult.getContent()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} 
		catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}

	}
	
	/**
	 * 发现干货
	 * @param request
	 * @return
	 */
	@RequestMapping("findDrycargoByCategory")
	public @ResponseBody ResponseContainer findDrycargoByCategory(HttpServletRequest request,QueryModelMul dm) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			List<String> sort = new ArrayList<String>();
			sort.add("weightSort");
			sort.add("ctime");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			String dryFlag = request.getParameter("dryFlag");//0代表干货1代表炫页
			if(StringUtil.isBlank(dryFlag)){
				dryFlag = "0";
			}
			String categoryId = request.getParameter("categoryId");
			String childCategoryId = request.getParameter("childCategoryId");
			Page<Drycargo> drycargo = drycargoService.findAllByCategory(Integer.parseInt(dryFlag),categoryId,childCategoryId,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), drycargo);
			this.getReponseData().setResult((drycargoService.toResponeses(drycargo.getContent(),currentUser.getId())));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} 
		catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}

	}
	
	/**
	 * 首页
	 * @param request
	 * @return
	 */
	@RequestMapping("findIndex")
	public @ResponseBody ResponseContainer findIndex(HttpServletRequest request,QueryModel dm) {
		try {
			IndexBean index = indexService.findIndex();
			this.getReponseData().setResult(index);
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} 
		catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}

	}
	/**
	 * 首页新版
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("findNewIndex")
	public @ResponseBody ResponseContainer findNewIndex(HttpServletRequest request,QueryModel dm) {
		try {
			String nGroup = request.getParameter("nGroup");
			String nCourse = request.getParameter("nCourse");
			String token=request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String ctime=request.getParameter("ctime");
			String dynamic=request.getParameter("dynamic");
			NewIndexBean index =null;
			JSONObject obj=groupDynamicService.getAllGroupDynamics(token, ctime, dynamic);
			if(obj != null ){
				Object status=obj.get("status");
				if("200".equals(status.toString())){
					JSONObject data=obj.getJSONObject("data");
					//System.out.println(data.toString());
					data=YXTJSONHelper.addAndModifyAttrJsonObject(data, new HashMap<String, Object>());
					//System.out.println(data.toString());
					JSONArray result=data.getJSONArray("result");
					if(result != null){
						result=result.fromObject(result,YXTJSONHelper.initJSONObjectConfig());
						//System.out.println(result.toString());
						result=indexService.formateGroupDynamic(currentUser.getId(), result);
						index = indexService.findNewIndex(result,dynamic,nGroup,nCourse,currentUser.getId());
						this.getReponseData().setCurr_page(Integer.valueOf(data.get("curr_page").toString()));
						this.getReponseData().setTotal_rows(Long.valueOf(data.get("total_rows").toString()));
						this.getReponseData().setCurr_rows(Integer.valueOf(data.get("curr_rows").toString()));
						this.getReponseData().setPage_rows(Integer.valueOf(data.get("page_rows").toString()));
					}else{
						index = indexService.findNewIndex(null,dynamic,nGroup,nCourse,currentUser.getId());
					}
				}else{
					index = indexService.findNewIndex(null,dynamic,nGroup,nCourse,currentUser.getId());
				}
			}else{
				index = indexService.findNewIndex(null,dynamic,nGroup,nCourse,currentUser.getId());
			}
			this.getReponseData().setResult(index);			
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} 
		catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}

	}
	
	@RequestMapping("findNewIndex/v1.3.7")
	public @ResponseBody ResponseContainer findNewIndex137(HttpServletRequest request,QueryModel dm) {
		try {
			String nGroup = request.getParameter("nGroup");
			String nCourse = request.getParameter("nCourse");
			String token=request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String ctime=request.getParameter("ctime");
			String dynamic=request.getParameter("dynamic");
			NewIndexBean index =null;
			JSONObject obj=groupDynamicService.getAllGroupDynamics137(token, ctime, dynamic);
			if(obj != null ){
				Object status=obj.get("status");
				if("200".equals(status.toString())){
					JSONObject data=obj.getJSONObject("data");
					//System.out.println(data.toString());
					data=YXTJSONHelper.addAndModifyAttrJsonObject(data, new HashMap<String, Object>());
					//System.out.println(data.toString());
					JSONArray result=data.getJSONArray("result");
					if(result != null){
						result=result.fromObject(result,YXTJSONHelper.initJSONObjectConfig());
						//System.out.println(result.toString());
						result=indexService.formateGroupDynamic(currentUser.getId(), result);
						index = indexService.findNewIndex(result,dynamic,nGroup,nCourse,currentUser.getId());
						this.getReponseData().setCurr_page(Integer.valueOf(data.get("curr_page").toString()));
						this.getReponseData().setTotal_rows(Long.valueOf(data.get("total_rows").toString()));
						this.getReponseData().setCurr_rows(Integer.valueOf(data.get("curr_rows").toString()));
						this.getReponseData().setPage_rows(Integer.valueOf(data.get("page_rows").toString()));
					}else{
						index = indexService.findNewIndex(null,dynamic,nGroup,nCourse,currentUser.getId());
					}
				}else{
					index = indexService.findNewIndex(null,dynamic,nGroup,nCourse,currentUser.getId());
				}
			}else{
				index = indexService.findNewIndex(null,dynamic,nGroup,nCourse,currentUser.getId());
			}
			this.getReponseData().setResult(index);			
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} 
		catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
		
	}
	
	/**
	 * 根据分类查询群组
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("findGroupByCategory")
	public @ResponseBody ResponseContainer findGroupByCategory(HttpServletRequest request,QueryModelMul dm) {
		try {
			List<String> sort = new ArrayList<String>();
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			//sort.add("weightSort");
			sort.add("ctime");
			dm.setSort(sort);
			String categoryId = request.getParameter("categoryId");
			String childCategoryId = request.getParameter("childCategoryId");
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<XueWenGroup> group = groupService.findGroupByCategoryId(categoryId,childCategoryId,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), group);
			//this.getReponseData().setResult(groupService.changeGroupsToResponseGroup(group.getContent(),currentUser.getId()));
			this.getReponseData().setResult(groupService.toGroupResponses(group.getContent(),currentUser.getId()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} 
		catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}

	}

}
