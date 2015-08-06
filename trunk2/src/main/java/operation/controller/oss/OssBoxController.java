package operation.controller.oss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;
import operation.OssController;
import operation.exception.XueWenServiceException;
import operation.pojo.activity.Activity;
import operation.pojo.box.Box;
import operation.pojo.category.Category;
import operation.pojo.course.NewCourse;
import operation.pojo.course.NewGroupCourse;
import operation.pojo.drycargo.Drycargo;
import operation.pojo.group.XueWenGroup;
import operation.pojo.pub.QueryModelMul;
import operation.pojo.topics.Topic;
import operation.service.activity.ActivityService;
import operation.service.box.BoxPostService;
import operation.service.box.BoxService;
import operation.service.category.CategoryService;
import operation.service.course.NewCourseService;
import operation.service.course.NewGroupCourseService;
import operation.service.drycargo.DrycargoService;
import operation.service.group.GroupService;
import operation.service.topics.TopicService;

import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;













import tools.Config;
import tools.PageRequestTools;
import tools.ReponseData;
import tools.ReponseDataTools;
import tools.ResponseContainer;


@RestController
@RequestMapping("/oss/box")
@Configuration
public class OssBoxController extends OssController{
	@Autowired
	private NewCourseService newcourseservice;
	@Autowired
	private NewGroupCourseService newgroupcourseservice;
	@Autowired
	private BoxService boxService;
	@Autowired
	private BoxPostService boxPostService;
	@Autowired
	private DrycargoService drycargoService;
	@Autowired
	private TopicService topicService;
	@Autowired
	public GroupService groupService;
	@Autowired
	private ActivityService activityService;
	@Autowired
	private CategoryService categoryService;
	
	public OssBoxController(){
		super();
	}
	
	/**
	 * 创建位置
	 * @param request
	 * @return
	 * @throws XueWenServiceException
	 */
	@RequestMapping("/addBoxPost")
	public @ResponseBody ResponseContainer addBoxPost(HttpServletRequest request) throws XueWenServiceException {
		try {
			String chinaName=request.getParameter("chinaName");
			String englishName=request.getParameter("englishName");
			String local=request.getParameter("local");
			String type=request.getParameter("type");
			String size=request.getParameter("size");
			return addResponse(Config.STATUS_200, Config.MSG_200, boxPostService.create(chinaName, englishName, local, type,Integer.valueOf(size)),Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 根据类型获取位置信息
	 * @param request
	 * @return
	 * @throws XueWenServiceException
	 */
	@RequestMapping("/getBoxPostByType")
	public @ResponseBody ResponseContainer getBoxPostByType(HttpServletRequest request) throws XueWenServiceException {
		try {
			String type=request.getParameter("type");
			return addResponse(Config.STATUS_200, Config.MSG_200, boxPostService.findByType(type),Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	
	
	/**
	 * 根据类型获取位置信息
	 * @param request
	 * @return
	 * @throws XueWenServiceException
	 */
	@RequestMapping("/getBoxPostById")
	public @ResponseBody ResponseContainer getBoxPostById(HttpServletRequest request) throws XueWenServiceException {
		try {
			String id=request.getParameter("id");
			return addResponse(Config.STATUS_200, Config.MSG_200, boxPostService.findById(id),Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	
	
	
	/**
	 * 将相应的数据添加到相应位置
	 * @param request
	 * @return
	 * @throws XueWenServiceException
	 */
	@RequestMapping("/addBoxInBoxPost")
	public @ResponseBody ResponseContainer addBoxInBoxPost(HttpServletRequest request) throws XueWenServiceException {
		try {
			String boxPostId=request.getParameter("boxPostId");
			String sourceType=request.getParameter("sourceType");
			String sourceId=request.getParameter("sourceId");
			String ctime=request.getParameter("ctime");
			
			
			String  groupid=request.getParameter("groupid");
			if(groupid==null||"null".equals(groupid)){
				groupid="";
			}
			// 是否添加各个模块的权重
			Drycargo d = drycargoService.findOneById(sourceId);
			Topic t = topicService.findOneById(sourceId);
			XueWenGroup x = groupService.findById(sourceId);
			NewGroupCourse ngc = newgroupcourseservice.findOneByid(sourceId);
			if (d != null) {
				d.setReview(true);
				drycargoService.saveDrycargo(d);
			}
			if (t != null) {
				t.setReview(true);
				topicService.savetopic(t);
			}
			if (x != null) {
				x.setReview(true);
				groupService.savegroup(x);
			}
			if (ngc != null) {
				ngc.setReview(true);
				newgroupcourseservice.saveGroupCourse(ngc);
			}
			 
				return addResponse(Config.STATUS_200, Config.MSG_200, boxService.addInBoxPost(boxPostId, sourceType, sourceId,ctime,groupid),Config.RESP_MODE_10, "");
		 
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 删除位置中的对象
	 * @param request
	 * @return
	 * @throws XueWenServiceException
	 */
	@RequestMapping("/deleteBox")
	public @ResponseBody ResponseContainer deleteBox(HttpServletRequest request) throws XueWenServiceException {
		try {
			String boxId=request.getParameter("boxId");
			boxService.deleteByBoxId(boxId);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	/**
	 * 删除位置中的对象
	 * @param request
	 * @return
	 * @throws XueWenServiceException
	 */
	@RequestMapping("/deleteBoxByGroupId")
	public @ResponseBody ResponseContainer deleteBoxByGroupId(HttpServletRequest request) throws XueWenServiceException {
		try {
			String groupid=request.getParameter("groupid");
			List<Box> l=boxService.findByGroupid(groupid);
			for(Box b:l){
				boxService.deleteByGroupId(b);
			}
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	
	
	/**
	 * 删除位置中的对象
	 * @param request
	 * @return
	 * @throws XueWenServiceException
	 */
	@RequestMapping("/deleteBoxBypostAndsource")
	public @ResponseBody ResponseContainer deleteBoxBypostAndsource(HttpServletRequest request) throws XueWenServiceException {
		try {
			String postId=request.getParameter("postId");
			String sourceId=request.getParameter("sourceId");
			Box box=boxService.findByBoxPostIdAndSourceId(postId, sourceId);
			boxService.deleteByBoxId(box.getId());
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	/**
	 * 删除位置
	 * @param request
	 * @return
	 * @throws XueWenServiceException
	 */
	@RequestMapping("/deleteBoxPost")
	public @ResponseBody ResponseContainer deleteBoxPost(HttpServletRequest request) throws XueWenServiceException {
		try {
			String id=request.getParameter("id");
			boxService.deleteById(id);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	
	/**
	 * 根据位置Id查询不在此位置的干货列表
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("drycargoListNotInBoxPost")
	public @ResponseBody ResponseContainer drycargoListNotInBoxPost(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<Drycargo> dryCargoResult;
			String dryFlag = request.getParameter("dryFlag");//0干货1炫页
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			if(StringUtil.isBlank(dryFlag)){
				dryFlag = "0";
			}
			dryCargoResult = drycargoService.findByBoxPostId(Integer.parseInt(dryFlag),boxPostId,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), dryCargoResult);
			this.getReponseData().setResult((drycargoService.toResponeses(dryCargoResult.getContent(),"")));
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
	 * 根据位置Id查询不在此位置的干货列表
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("searchDrycargoNotInBoxPost")
	public @ResponseBody ResponseContainer searchDrycargoNotInBoxPost(HttpServletRequest request) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			 
			List<Drycargo> dryCargoResult;
			String dryFlag = request.getParameter("dryFlag");//0干货1炫页
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			String keyword = request.getParameter("keyword");//0干货1炫页
			if(StringUtil.isBlank(dryFlag)){
				dryFlag = "0";
			}
			dryCargoResult = drycargoService.findByNotInBoxPostId(Integer.parseInt(dryFlag),boxPostId,keyword);
			 
			this.getReponseData().setResult(dryCargoResult);
			return addPageResponse(Config.STATUS_200, Config.MSG_200,getReponseData() ,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 根据位置Id，获取此位置下的干活列表
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("drycargoInBox")
	public @ResponseBody ResponseContainer drycargoInBox(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			List<String> sort = new ArrayList<String>();
			sort.add("weightSort");
			sort.add("ctime");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			Page<Box> boxs = boxService.findByBoxPostId(boxPostId, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), boxs);
			this.getReponseData().setResult((drycargoService.formateDrycatgoList((boxs.getContent()))));
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
	 * 根据位置Id查询不在此位置的话题列表
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("topicListNotInBoxPost")
	public @ResponseBody ResponseContainer topicListNotInBoxPost(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			Page<Topic> topics = topicService.findByBoxPostIdNotInBox(boxPostId,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), topics);
			this.getReponseData().setResult(topicService.toResponses(topics.getContent()));
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
	 * 根据位置Id查询不在此位置的话题列表
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("searchtopicListNotInBoxPost")
	public @ResponseBody ResponseContainer searchtopicListNotInBoxPost(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			String keyword = request.getParameter("keyword");
			List<Topic> topics = topicService.findByBoxPostIdNotInBoxForSearch(boxPostId,keyword);
			this.getReponseData().setResult(topics);
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
	 * 根据位置Id，获取此位置下的话题列表
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("topicInBox")
	public @ResponseBody ResponseContainer topicInBox(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			List<String> sort = new ArrayList<String>();
			sort.add("weightSort");
			sort.add("ctime");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			Page<Box> boxs = boxService.findByBoxPostId(boxPostId, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), boxs);
			this.getReponseData().setResult(topicService.toBoxResponses(boxs.getContent()));
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
	 * 根据位置Id，获取此位置下的群组列表
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("groupInBox")
	public @ResponseBody ResponseContainer groupInBox(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			List<String> sort = new ArrayList<String>();
			sort.add("weightSort");
			sort.add("ctime");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			Page<Box> boxs = boxService.findByBoxPostId(boxPostId, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), boxs);
			this.getReponseData().setResult(groupService.toBoxResponses(boxs.getContent()));
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
	 * 根据位置Id查询不在此位置的话题列表
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("groupListNotInBoxPost")
	public @ResponseBody ResponseContainer groupListNotInBoxPost(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			//Page<Activity>  activitys = activityService.findByBoxPostIdNotInBox(boxPostId,pageable);
			Page<XueWenGroup>  activitys = groupService.findByBoxPostIdNotInBox(boxPostId,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), activitys);
			this.getReponseData().setResult(activitys.getContent());
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
	 * 根据位置Id查询不在此位置的群组列表
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("searchgroupListNotInBoxPost")
	public @ResponseBody ResponseContainer searchgroupListNotInBoxPost(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			String keyword = request.getParameter("keyword");
			//List<Topic> topics = topicService.findByBoxPostIdNotInBoxForSearch(boxPostId,keyword);
			List<Activity> activitys = activityService.findByBoxPostIdNotInBoxForSearch(boxPostId,keyword);
			this.getReponseData().setResult(activitys);
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
	 * 根据位置Id，获取此位置下的课程列表
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("courseInBox")
	public @ResponseBody ResponseContainer courseInBox(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			List<String> sort = new ArrayList<String>();
			sort.add("weightSort");
			sort.add("ctime");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			Page<Box> boxs = boxService.findByBoxPostId(boxPostId, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), boxs);
			List<NewGroupCourse> newGroupCourses =newgroupcourseservice.toBoxResponsesTonewgroupcourse(boxs.getContent());
			List<Object> l=new ArrayList<Object>();
			if(newGroupCourses.size()>0){
			for(NewGroupCourse n:newGroupCourses){
				Map m=new HashMap();
				m.put("course", newcourseservice.findById(n.getCourse().toString()));
				m.put("groupcourseid", n.getId());
				m.put("weightSort", n.getWeightSort());
				l.add(m);
			}
			}
			//this.getReponseData().setResult(newcourseservice.findByBoxPostIdInBox(l,pageable));
			//Page<NewCourse> p=newcourseservice.findByBoxPostIdInBox(l,pageable);
			return addResponse(Config.STATUS_200, Config.MSG_200, l,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 根据位置Id查询不在此位置的课程列表(已经分享到群组的课程)
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("courseListNotInBoxPost")
	public @ResponseBody ResponseContainer courseListNotInBoxPost(HttpServletRequest request,QueryModelMul dm) {
		
		List<Object> ll=new ArrayList<Object>();
		try {
			// 根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			Page<NewGroupCourse> l=newgroupcourseservice.findGroupCourseNotInBox(boxPostId,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), l);
			if(l.getTotalElements()!=0){
				for(NewGroupCourse n:l){
					Map m=new HashMap();
					NewCourse nn=newcourseservice.findById(n.getCourse().toString());
					nn.getTitle();
					m.put("newgroupcourse", n);
					m.put("title", nn.getTitle());
					m.put("logourl", nn.getLogoUrl());
					m.put("ctime", nn.getCtime());
					ll.add(m);
					
				}
			}
			Map m1=new HashMap();
			m1.put("result", ll);
			this.getReponseData().setResult(ll);
			//Page<NewCourse>  newcourse = newcourseservice.findByBoxPostIdInBox(l,pageable);
			
			
//			ReponseDataTools.getClientReponseData(getReponseData(), newcourse);
			this.getReponseData().setResult(ll);
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
	 * 根据位置Id查询不在此位置的课程列表
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("searchcourseListNotInBoxPost")
	public @ResponseBody ResponseContainer searchcourseListNotInBoxPost(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			String keyword = request.getParameter("keyword");
			//List<Topic> topics = topicService.findByBoxPostIdNotInBoxForSearch(boxPostId,keyword);
			List<NewCourse> NewCourse = newcourseservice.findByBoxPostIdNotInBoxForSearch(boxPostId,keyword);
			this.getReponseData().setResult(NewCourse);
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
	 * 根据位置Id，获取此位置下的群组列表
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("activityInBox")
	public @ResponseBody ResponseContainer activityInBox(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			List<String> sort = new ArrayList<String>();
			sort.add("weightSort");
			sort.add("ctime");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			Page<Box> boxs = boxService.findByBoxPostId(boxPostId, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), boxs);
			this.getReponseData().setResult(activityService.toBoxResponses(boxs.getContent()));
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
	 * 根据位置Id查询不在此位置的话题列表
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("activityListNotInBoxPost")
	public @ResponseBody ResponseContainer activityListNotInBoxPost(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			Page<Activity>  topics = activityService.findByBoxPostIdNotInBox(boxPostId,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), topics);
			this.getReponseData().setResult(topics.getContent());
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
	 * 根据位置Id查询不在此位置的群组列表
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("searchactivityListNotInBoxPost")
	public @ResponseBody ResponseContainer searchactivityListNotInBoxPost(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			String keyword = request.getParameter("keyword");
			//List<Topic> topics = topicService.findByBoxPostIdNotInBoxForSearch(boxPostId,keyword);
			List<Activity> XueWenGroup = activityService.findByBoxPostIdNotInBoxForSearch(boxPostId,keyword);
			this.getReponseData().setResult(XueWenGroup);
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
//分类
	/**
	 * 根据位置Id，获取此位置下的分类
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("categoryInBox")
	public @ResponseBody ResponseContainer categoryInBox(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			Page<Box> boxs = boxService.findByBoxPostId(boxPostId, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), boxs);
			this.getReponseData().setResult(categoryService.toBoxResponses(boxs.getContent()));
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
	 * 根据位置Id，获取此位置下的分类
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("subjectInBox")
	public @ResponseBody ResponseContainer subjectInBox(HttpServletRequest request) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			List<Box> boxs = boxService.findAllByBoxPostId(boxPostId);
			return addResponse(Config.STATUS_200, Config.MSG_200, boxs,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	
	/**
	 * 根据位置Id查询不在此位置的分类列表
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("categoryListNotInBoxPost")
	public @ResponseBody ResponseContainer categoryListNotInBoxPost(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页findByBoxPostIdNotInBox
			Page<Category>  topics = categoryService.findByBoxPostIdNotInBox(boxPostId,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), topics);
			this.getReponseData().setResult(topics.getContent());
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
	 * 根据位置Id查询不在此位置的分类列表
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("searchcategoryListNotInBoxPost")
	public @ResponseBody ResponseContainer searchcategoryListNotInBoxPost(HttpServletRequest request,QueryModelMul dm) {
		
		try {
			// 根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			
			String boxPostId = request.getParameter("boxPostId");//0干货1炫页
			String keyword = request.getParameter("keyword");
			List<Category> categorys = categoryService.findByBoxPostIdNotInBoxForSearch(boxPostId,keyword);
			this.getReponseData().setResult(categorys);
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
	 * 推荐排序修改权重
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("updateweightSort")
	public @ResponseBody ResponseContainer updateweightSort(HttpServletRequest request) {
		
		try {
			String weightSort = request.getParameter("weightSort");
			int i=Integer.parseInt(weightSort);
			String postId=request.getParameter("postId");
			String sourceId=request.getParameter("sourceId");
			Box box=boxService.findByBoxPostIdAndSourceId(postId, sourceId);
			box.setWeightSort(i);
			//是否添加各个模块的权重
			Drycargo d=drycargoService.findOneById(sourceId);
			Topic t=topicService.findOneById(sourceId);
			XueWenGroup x=groupService.findById(sourceId);
			NewGroupCourse ngc= newgroupcourseservice.findOneByid(sourceId);
			if (d != null) {
				d.setWeightSort(i);
				drycargoService.saveDrycargo(d);
			}
			if (t != null) {
				t.setWeightSort(i);
				topicService.savetopic(t);
			}
			if (x != null) {
				x.setWeightSort(i);
				groupService.savegroup(x);
			}
			if (ngc != null) {
				ngc.setWeightSort(i);
				newgroupcourseservice.saveGroupCourse(ngc);
			}
			
			boxService.saveBox(box);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
//	@RequestMapping("recommend")
//	public @ResponseBody ResponseContainer recommend(HttpServletRequest request,QueryModelMul dm) {
//		
//		try {
//			// 根据请求参数封装一个分页信息对象
//			//Pageable pageable = PageRequestTools.pageRequesMake(dm);
//			
//			String n = request.getParameter("n");
//			JSONObject categorys = boxService.findRecommended(n);
//			this.getReponseData().setResult(categorys);
//			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
//		} catch (XueWenServiceException e) {
//			e.printStackTrace();
//			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
//		}
//	}
	
	/**
	 * 根据位置Id及分类查询不在此位置的话题列表
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("notInBoxPostAndNotInCategory")
	public @ResponseBody ResponseContainer topicNotInBoxPostAndNotInCategory(HttpServletRequest request,QueryModelMul dm) {
			try {
				// 根据请求参数封装一个分页信息对象
				Pageable pageable = PageRequestTools.pageRequesMake(dm);
				String boxPostId = request.getParameter("boxPostId");
				String type = request.getParameter("dataType");//对象类型
				String category = request.getParameter("childCategoryId");//二级分类ID
				if("topic".equals(type)){
					Page<Topic> topics = topicService.findByBoxPostIdNotInBoxAndNotInCategory(boxPostId,type,category,pageable);
					ReponseDataTools.getClientReponseData(getReponseData(), topics);
					this.getReponseData().setResult(topicService.toResponses(topics.getContent()));
					return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
				}
				if("dry".equals(type)){
					Page<Drycargo> topics = drycargoService.findByBoxPostIdNotInBoxAndNotInCategory(boxPostId,type,category,pageable);
					ReponseDataTools.getClientReponseData(getReponseData(), topics);
					this.getReponseData().setResult(drycargoService.toResponeses(topics.getContent(),""));
					return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
				}
				if("group".equals(type)){
					Page<XueWenGroup>  activitys = groupService.findByBoxPostIdNotInBoxAndNotInCategory(boxPostId,type,category,pageable);
					ReponseDataTools.getClientReponseData(getReponseData(), activitys);
					this.getReponseData().setResult(activitys.getContent());
					return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
				}
				if("course".equals(type)){
					List<Object> ll=new ArrayList<Object>();
					Page<NewGroupCourse> l=newgroupcourseservice.findByBoxPostIdNotInBoxAndNotInCategory(boxPostId,type,category,pageable);
					ReponseDataTools.getClientReponseData(getReponseData(), l);
					if(l.getTotalElements()!=0){
						for(NewGroupCourse n:l){
							Map m=new HashMap();
							NewCourse nn=newcourseservice.findById(n.getCourse().toString());
							nn.getTitle();
							m.put("newgroupcourse", n);
							m.put("title", nn.getTitle());
							m.put("logourl", nn.getLogoUrl());
							m.put("ctime", nn.getCtime());
							ll.add(m);
							
						}
					}
					Map m1=new HashMap();
					m1.put("result", ll);
					this.getReponseData().setResult(ll);
					//Page<NewCourse>  newcourse = newcourseservice.findByBoxPostIdInBox(l,pageable);
					
					
//					ReponseDataTools.getClientReponseData(getReponseData(), newcourse);
					this.getReponseData().setResult(ll);
					return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
				}
				return  addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
			} catch (XueWenServiceException e) {
				e.printStackTrace();
				return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
			} catch (Exception e) {
				e.printStackTrace();
				return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
			}
		}
	
	
	
	 
}
