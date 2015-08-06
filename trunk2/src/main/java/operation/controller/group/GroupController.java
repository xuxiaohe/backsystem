package operation.controller.group;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import operation.BaseController;
import operation.exception.XueWenServiceException;
import operation.pojo.group.MyAllGroup;
import operation.pojo.group.ResponseGroup;
import operation.pojo.group.ResponseNearGroup;
import operation.pojo.group.XueWenGroup;
import operation.pojo.pub.QueryModel;
import operation.pojo.pub.QueryModelMul;
import operation.pojo.user.ResponsePcUser;
import operation.pojo.user.User;
import operation.pojo.user.UserMoreMessage;
import operation.service.course.CourseService;
import operation.service.course.UserGroupCourseService;
import operation.service.drycargo.DrycargoService;
import operation.service.group.GroupNumService;
import operation.service.group.GroupNumberCreateService;
import operation.service.group.GroupPcService;
import operation.service.group.GroupService;
import operation.service.group.MyGroupService;
import operation.service.queue.QueueService;
import operation.service.user.UserPcService;
import operation.service.user.UserService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tools.Config;
import tools.PageRequestTools;
import tools.ReponseDataTools;
import tools.ResponseContainer;
import tools.StringUtil;

@RestController
@RequestMapping("/group")
@Configuration
/**
 * 群组controller，用户接受客户端请求的数据，
 * 与service层交互，并将数据反馈给客户端
 * @author nes
 *
 */
public class GroupController extends BaseController {
	private static final Logger logger=Logger.getLogger(GroupController.class);
	
	@Inject Environment env;
	@Autowired
	public GroupService groupService;
	@Autowired
	public GroupPcService groupPcService;
	@Autowired
	public UserService userService;
	@Autowired
	public UserPcService userPcService;
	@Autowired
	public GroupNumberCreateService groupNumberCreateService;
	
	@Autowired
	public GroupNumService groupNumService;
	
	@Autowired
	public QueueService queueService;
	
	@Autowired
	public MyGroupService myGroupService;
	
	@Autowired
	public DrycargoService drycargoService;
	
	@Autowired
	public CourseService courseService;
	
	@Autowired
	public UserGroupCourseService userGroupCourseService;
	

	public GroupController() {
		super();
	}
    
	
	/**
	 * 创建群组表，用于保存地理坐标二维数组
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/createtable")
	public  void createTable(){
		try {
			groupService.creatTable();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * 创建群组Pc时，获取推荐的标签
	 * 
	 * @param request
	 * @return
	 * @throws
	 * @throws IOException
	 */
	@RequestMapping(value = "/groupRecTagPc", method = RequestMethod.POST)
	public @ResponseBody ResponseContainer getGroupRecTagPc(HttpServletRequest request) {
		try {
			logger.info("=============Pc开始创建群组==============");
			String groupName = request.getParameter("groupName");
			String intro = request.getParameter("intro");
			List<String> list = groupPcService.getGroupRecTagPc(groupName,intro,10,3);
			return addResponse(Config.STATUS_200, Config.MSG_200,list, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("==========业务错误，Pc创建群失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("==========未知错误，Pc创建群失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 创建群组Pc
	 * 
	 * @param request
	 * @return
	 * @throws
	 * @throws IOException
	 */
	@RequestMapping(value = "/createPc", method = RequestMethod.POST)
	public @ResponseBody ResponseContainer createPc(HttpServletRequest request,XueWenGroup group) {
		try {
			logger.info("=============Pc开始创建群组==============");
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			//String tagName = request.getParameter("tagName");
			XueWenGroup groupCreate = groupPcService.createGroupPc(group,currentUser,group.getTagNames());
			groupCreate=groupService.addResponseGroup(groupCreate);
			return addResponse(Config.STATUS_200, Config.MSG_CREATE_200,groupCreate, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("==========业务错误，Pc创建群失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("==========未知错误，Pc创建群失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 更新群组信息
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/updatePc", method = RequestMethod.POST)
	public @ResponseBody ResponseContainer updateGroup(HttpServletRequest request,XueWenGroup group) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			XueWenGroup rg = groupPcService.updateGroupPc(group,currentUser);
			return addResponse(Config.STATUS_200, Config.MSG_UPDATE_200,groupService.changGroupToResponseGroup(rg,currentUser.getId()),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，更新群组信息==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，更新群组信息==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 更新群组信息（删除干货）
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/deleteDrycargo", method = RequestMethod.POST)
	public @ResponseBody ResponseContainer deleteDry(HttpServletRequest request,String groupId,String drycargoIds) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			if(groupPcService.deleteDry(groupId, drycargoIds, currentUser)){
				return addResponse(Config.STATUS_200, Config.MSG_DELETE_200,true,Config.RESP_MODE_10, "");
			}else{
				return addResponse(Config.STATUS_201, "删除失败",false,Config.RESP_MODE_10, "");
			}
			
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，更新群组信息==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，更新群组信息==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 更新群组信息（删除话题）
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/deleteTopic", method = RequestMethod.POST)
	public @ResponseBody ResponseContainer deleteTopic(HttpServletRequest request,String groupId,String topicIds) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			if(groupPcService.deleteTopic(groupId, topicIds, currentUser)){
				return addResponse(Config.STATUS_200, Config.MSG_DELETE_200,true,Config.RESP_MODE_10, "");
			}else{
				return addResponse(Config.STATUS_201, "删除失败",false,Config.RESP_MODE_10, "");
			}
			
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，更新群组信息==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，更新群组信息==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 更新群组信息（删除分享）
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/deleteKnowledge", method = RequestMethod.POST)
	public @ResponseBody ResponseContainer deleteKnowledge(HttpServletRequest request,String groupId,String knowledgeIds) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			if(groupPcService.deleteKnowledge(groupId, knowledgeIds, currentUser)){
				return addResponse(Config.STATUS_200, Config.MSG_DELETE_200,true,Config.RESP_MODE_10, "");
			}else{
				return addResponse(Config.STATUS_201,"删除失败",false,Config.RESP_MODE_10, "");
			}
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，更新群组信息==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，更新群组信息==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 创建群组
	 * 
	 * @param request
	 * @return
	 * @throws
	 * @throws IOException
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public @ResponseBody ResponseContainer create(HttpServletRequest request,XueWenGroup group) {
		try {
			logger.info("=============开始创建群组==============");
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String lat = request.getParameter("lat"); //维度
			String lng = request.getParameter("lng");//精度
			String tagName = request.getParameter("tagName");
			logger.info("获得群组标签==================="+tagName);
			String isGeoOpen = request.getParameter("isGeoOpen");//是否显示地理坐标
			XueWenGroup groupCreate = groupService.createGroup(group,currentUser,tagName,lat,lng,isGeoOpen);
			groupCreate=groupService.addResponseGroup(groupCreate);
			return addResponse(Config.STATUS_200, Config.MSG_CREATE_200,groupCreate, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("==========业务错误，创建群失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("==========未知错误，创建群失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}

	/**
	 * 根据群组ID查询群信息,不包含成员列表
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/one/{id}")
	public @ResponseBody ResponseContainer findGroup(@PathVariable("id") String id, HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			XueWenGroup group = groupService.findGroup(id, currentUser.getId());
			return addResponse(Config.STATUS_200, Config.MSG_200, group,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("==========业务错误，根据群组ID查询群信息,不包含成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组ID查询群信息,不包含成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 根据群组ID查询群信息,不包含成员列表
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/onePc/{id}")
	public @ResponseBody ResponseContainer findGroupPc(@PathVariable("id") String id, HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			return addResponse(Config.STATUS_200, Config.MSG_200, groupPcService.findGroup(id,currentUser.getId()),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("==========业务错误，根据群组ID查询群信息,不包含成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组ID查询群信息,不包含成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 根据群组ID查询群信息,不包含成员列表
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/findByGroupNumber")
	public @ResponseBody ResponseContainer findByGroupNumber(String groupNumber, HttpServletRequest request) {
		try {
			return addResponse(Config.STATUS_200, Config.MSG_200, groupService.findByGroupNumber(Long.parseLong(groupNumber)),Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组number查询群信息,不包含成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 根据群组ID查询群成员列表
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/one/{id}/member")
	public @ResponseBody ResponseContainer findGroupMmber(@PathVariable("id") String id, HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			XueWenGroup group = groupService.findGroupMmber(id, currentUser.getId());
			return addResponse(Config.STATUS_200, Config.MSG_200, group,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 根据群组ID查询群成员列表
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/one/{id}/memberPc")
	public @ResponseBody ResponseContainer findGroupMmberPage(@PathVariable("id") String id, String userName, HttpServletRequest request) {
		try {
			List<ResponsePcUser> users = groupService.findPcGroupMmbers(id,userName);
			return addResponse(Config.STATUS_200, Config.MSG_200, users,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 根据群组ID查询群成员列表（只包含管理员：第一个是创建者）
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/one/{id}/findPcGroupMmbersOnlyAdmin")
	public @ResponseBody ResponseContainer findPcGroupMmbersOnlyAdmin(@PathVariable("id") String id, HttpServletRequest request) {
		try {
			List<ResponsePcUser> users = groupService.findPcGroupMmbersOnlyAdmin(id);
			if (users.size()==0) {
				return addResponse(Config.STATUS_200, Config.MSG_200, null,Config.RESP_MODE_10, "");
			}else{
				return addResponse(Config.STATUS_200, Config.MSG_200, users,Config.RESP_MODE_10, "");
			}
		} catch (XueWenServiceException e) {
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 
	 * @Title: findGroupMmberPage
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param id
	 * @param userName
	 * @param request
	 * @return ResponseContainer
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/one/{id}/memberPcNotSort")
	public @ResponseBody ResponseContainer findGroupMmberPage(QueryModel dm,@PathVariable("id") String id, String userName, HttpServletRequest request) {
		try {
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Map<String, Object> map = groupService.findPcGroupMmbersNotSort(id,userName,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), (Page<User>)map.get("page"));
			getReponseData().setResult(map.get("result"));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}

	/**
	 * 查询所有群组支持翻页
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/all")
	public @ResponseBody ResponseContainer findAllGroup(QueryModel dm,HttpServletRequest request) {
		try {
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			Page<XueWenGroup> group  = groupService.all(pageable,currentUser.getId());
			ReponseDataTools.getClientReponseData(getReponseData(), group);
			//this.getReponseData().setResult(groupService.changeGroupsToResponseGroup(group.getContent(),currentUser.getId()));
			this.getReponseData().setResult(groupService.toGroupResponses(group.getContent(),currentUser.getId()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，查询所有群组支持翻页==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，查询所有群组支持翻页==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}

	}
	/**
	 * 查询所有群组不支持翻页
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/all/sort")
	public @ResponseBody ResponseContainer findAllSortGroup(HttpServletRequest request) {
		try {
			String sort = request.getParameter("sort");
			String mode = request.getParameter("mode");	
			if (StringUtil.isEmpty(sort)) {
				sort = "ctime";
			}
			if (StringUtil.isEmpty(mode)) {
				mode = "DESC";
			}
			Direction d = Direction.DESC;
			if(mode.equalsIgnoreCase("ASC")){
				d = Direction.ASC;
			}
			Sort st = new Sort(d,sort);
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			List<XueWenGroup> group = groupService.all(st, currentUser.getId());
			this.getReponseData().setResult(groupService.changeGroupsToResponseGroup(group,currentUser.getId()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，查询所有群组不支持翻页==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，查询所有群组不支持翻页==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}

	/**
	 * 更新群组信息
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "{id}/update", method = RequestMethod.POST)
	public @ResponseBody ResponseContainer updateGroup(@PathVariable("id") String id, HttpServletRequest request,XueWenGroup group) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String lat=request.getParameter("lat");
			String lng=request.getParameter("lng");
			String tagName = request.getParameter("tagName");
			logger.info("更改群标签====================="+tagName);
			String isGeoOpen = request.getParameter("isGeoOpen");//是否显示地理坐标
			XueWenGroup rg = groupService.updateGroup(group,id,currentUser,tagName,lat,lng,isGeoOpen);
			return addResponse(Config.STATUS_200, Config.MSG_UPDATE_200,groupService.changGroupToResponseGroup(rg,currentUser.getId()),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，更新群组信息==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，更新群组信息==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}

	/**
	 * 解散群
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/{id}/delete")
	public @ResponseBody ResponseContainer delete(@PathVariable("id") String id, HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			boolean result = groupService.deleteById(id, currentUser.getId(),currentUser.getNickName(),currentUser.getLogoURL());
			return addResponse(Config.STATUS_200, Config.MSG_DELETE_200,result, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，解散群==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，解散群==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}

	}

	/**
	 * 加入群
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/{id}/join")
	public @ResponseBody ResponseContainer join(@PathVariable("id") String id,HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String passWord = request.getParameter("passWord");
			boolean result = groupService.join(id, currentUser.getId(),passWord);
			return addResponse(Config.STATUS_200, Config.MSG_JOIN_200, result,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，加入群==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，加入群==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}

	}

	/**
	 * 退出群
	 * 
	 * @param groupId
	 * @return
	 */
	@RequestMapping("/{id}/quit")
	public @ResponseBody ResponseContainer quit(@PathVariable("id") String id,HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			boolean result = groupService.quit(id, currentUser.getId(),currentUser.getNickName(),currentUser.getLogoURL());
			return addResponse(Config.STATUS_200, Config.MSG_OUT_200, result,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，退出群==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，退出群==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}

	}
	
	/**
	 * 管理员将成员踢出群
	 * 
	 * @param groupId
	 * @return
	 */
	@RequestMapping("/{id}/{userId}/kick")
	public @ResponseBody ResponseContainer kick(@PathVariable("id") String id,@PathVariable("userId") String kickuserId,HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			boolean result = groupService.kick(id, kickuserId,currentUser.getId(),currentUser.getNickName(),currentUser.getLogoURL());
			if(result){
				return addResponse(Config.STATUS_200, Config.KICK_OUT_200, result,Config.RESP_MODE_10, "");
			}else{
				return addResponse(Config.STATUS_201, "踢出失败", result,Config.RESP_MODE_10, "");
			}
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，管理员将成员踢出群==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，管理员将成员踢出群==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}

	}
	/**
	 * 管理员将成员踢出群（批量）
	 * 
	 * @param groupId
	 * @return
	 */
	@RequestMapping("/kickMany")
	public @ResponseBody ResponseContainer kickMany(String id, String kickuserIds,HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			boolean result = groupService.kickMany(id, kickuserIds,currentUser.getId(),currentUser.getNickName(),currentUser.getLogoURL());
			if(result){
				return addResponse(Config.STATUS_200, Config.KICK_OUT_200, result,Config.RESP_MODE_10, "");
			}else{
				return addResponse(Config.STATUS_201, "踢出失败", result,Config.RESP_MODE_10, "");
			}
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，管理员将成员踢出群==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
			}
		}

	/**
	 * 将群员提升为管理员
	 * 或将管理员降为普通成员
	 * @param id
	 * @return
	 */
	@RequestMapping("/{groupId}/{userId}/{type}/change")
	public @ResponseBody ResponseContainer changeToAdmin(@PathVariable("groupId") String groupId, @PathVariable("userId") String userId, @PathVariable("type") String type, HttpServletRequest request) {
		try{
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			boolean result = groupService.chang(groupId, userId,currentUser,type);
			return addResponse(Config.STATUS_200, Config.MSG_TOADMIN_200, result,Config.RESP_MODE_10, "");
		}
		catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，将成员提升为管理员==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，将成员提升为管理员==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}

	/**
	 * 模糊查询
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("search")
	public @ResponseBody ResponseContainer some(HttpServletRequest request,QueryModelMul dm) {
		String groupName = request.getParameter("keyword");
		try {
		if(!StringUtil.isEmpty(groupName)){
			groupName = URLDecoder.decode(groupName,"UTF-8").toString();
		}
		String token = request.getParameter("token");
		User currentUser = this.getCurrentUser(token);
		String lat = request.getParameter("lat"); //维度
		String lng = request.getParameter("lng");//精度
		Point p = null;
		Distance dis = null;
		if(null!=lat && null!= lng){
		 p = new Point(Double.parseDouble(lng),Double.parseDouble(lat));
		 dis = new Distance(1d);
		}
		List<String> sort = new ArrayList<String>();
		sort.add("memberCount");
		sort.add("ctime");
		dm.setSort(sort);
		Pageable pageable = PageRequestTools.pageRequesMake(dm);
		Page<XueWenGroup> group = groupService.findAllByGroupNameRegexOrIntroRegexAndIdNotIn(groupName,currentUser.getId(),p,dis,pageable);
		ReponseDataTools.getClientReponseData(getReponseData(), group);
		this.getReponseData().setResult(groupService.toGroupResponses(group.getContent(),currentUser.getId()));
		return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，模糊查询==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，模糊查询==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}

	}

	/**
	 * 通过用户id查询群组
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/myGroup")
	public @ResponseBody ResponseContainer getMyGroup(HttpServletRequest request) {
		String userId = request.getParameter("userId");
		try {
		//	List<XueWenGroup> group = groupService.findMyGroup(userId);
			List<XueWenGroup> group = groupService.getMyGroups(userId);
			this.getReponseData().setResult(groupService.toGroupResponses(group,userId));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，通过用户ID查找群组==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，通过用户ID查找群组==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * PC通过用户id查询群组
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/myPcGroup")
	public @ResponseBody ResponseContainer myPcGroup(HttpServletRequest request) {
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			List<XueWenGroup> group = groupService.findMyGroup(currentUser.getId());
			this.getReponseData().setResult(groupService.changeGroupsToPcResponseGroup(group,currentUser.getId()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，通过用户ID查找群组==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，通过用户ID查找群组==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 
	 * @Title: myPcGroupTop3
	 * @Description: 获取用户前3个群组
	 * @param request
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("/myPcGroupTop3")
	public @ResponseBody ResponseContainer myPcGroupTop3(HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			List<JSONObject> objs=groupService.getMyGroupTop3(currentUser.getId());
			return addResponse(Config.STATUS_200, Config.MSG_TOADMIN_200, objs,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}
		
	}
	/**
	 * pc获取所有群组
	 * @param request
	 * @return
	 */
	@RequestMapping("/myPcAllGroup")
	public @ResponseBody ResponseContainer myPcAllGroup(HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			List<JSONObject> objs=groupService.getmyPcAllGroup(currentUser.getId());
			return addResponse(Config.STATUS_200, Config.MSG_TOADMIN_200, objs,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}
		
	}
	
	/**
	 * 通过用户id查询我加入的群组
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/my/joined")
	public @ResponseBody ResponseContainer getPcJoinedGroup(HttpServletRequest request) {
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			List<XueWenGroup> group = groupService.findMyJoinedGroup(currentUser.getId());
			this.getReponseData().setResult(groupService.changeGroupsToPcResponseGroup(group,currentUser.getId()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，通过用户ID查找群组==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，通过用户ID查找群组==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	/**
	 * 通过用户id查询我加入的群组 分页
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/getMyJoinedGroup")
	public @ResponseBody ResponseContainer getMyJoinedGroup(HttpServletRequest request,QueryModel dm) {
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			List<XueWenGroup> group = groupService.findMyJoinedGroup(currentUser.getId());
			this.getReponseData().setResult(groupService.changeGroupsToPcResponseGroup(group,currentUser.getId()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，通过用户ID查找群组==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，通过用户ID查找群组==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	/**
	 * 通过用户id查询我创建的群组
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/my/created")
	public @ResponseBody ResponseContainer getPcCreatedGroup(HttpServletRequest request) {
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			List<XueWenGroup> group = groupService.findMyCreatedGroup(currentUser.getId());
			this.getReponseData().setResult(groupService.changeGroupsToPcResponseGroup(group,currentUser.getId()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，通过用户ID查找群组==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，通过用户ID查找群组==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	/**
	 * 我创建的群组带分页
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/getMyCreateGroups")
	public @ResponseBody ResponseContainer getCreatedGroup(HttpServletRequest request,QueryModel dm) {
		String token = request.getParameter("token");
		Pageable pageable=PageRequestTools.pageRequesMake(dm);
		try {
			User currentUser = this.getCurrentUser(token);
			Page<XueWenGroup> groups = groupService.findMyGreateGroup(currentUser.getId(), pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), groups);
			JSONArray groupres=groupService.shutForPc(groups);
			getReponseData().setResult(groupres);
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，通过用户ID查找群组==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 用户请求加入群组
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/apply")
	public @ResponseBody ResponseContainer apply(HttpServletRequest request) {
		String groupId = request.getParameter("groupId");
//		String password = request.getParameter("password");
//		String future=request.getParameter("future");
		String token = request.getParameter("token");
		String reason=request.getParameter("reason");
//		Map<String,String> extra=new HashMap<String, String>();
//		if(StringUtil.isBlank(future)){
//			future="1";
//		}
//		extra.put("type","1000");
//		extra.put("future", future);
		try {
			User currentUser = this.getCurrentUser(token);
			groupService.applyToGroup(groupId,currentUser,reason);
			XueWenGroup group = groupService.findGroup(groupId, currentUser.getId());
			return addResponse(Config.STATUS_200, Config.MSG_JOIN_200, group,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，用户请求加入群组==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，用户请求加入群组==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 管理员同意用户加入群组
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/agree")
	public @ResponseBody ResponseContainer agree(HttpServletRequest request) {
		try {
			String groupId = request.getParameter("groupId");//群ID
			String userId = request.getParameter("userId");//申请人
			String token = request.getParameter("token");//当前管理员
			String groupApplyId=request.getParameter("groupApplyId");//消息Id
//			String future=request.getParameter("future");
//			Map<String,String> extra=new HashMap<String, String>();
//			extra.put("type","1001");
//			extra.put("future",future);
			User currentUser = this.getCurrentUser(token);
			groupService.agree(groupId,groupApplyId,userId,currentUser);
			return addResponse(Config.STATUS_200, Config.MSG_JOIN_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，管理员同意加入群组==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，管理员同意加入群组==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 管理员拒绝用户加入群组
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/unagree")
	public @ResponseBody ResponseContainer unagree(HttpServletRequest request) {
		try {
			String groupId = request.getParameter("groupId");
			String userId = request.getParameter("userId");
			String token = request.getParameter("token");
			//String future=request.getParameter("future");
			String groupApplyId=request.getParameter("groupApplyId");
			User currentUser = this.getCurrentUser(token);
//			Map<String,String> extra=new HashMap<String, String>();
//			extra.put("type","1002");
//			extra.put("future",future);
			groupService.unagree(groupId,groupApplyId,userId,currentUser);
			return addResponse(Config.STATUS_200, Config.MSG_UNJOIN_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 查询附近的群
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("nearGroup")
	public @ResponseBody ResponseContainer findAllNearGroup(HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String lat = request.getParameter("lat"); //维度
			String lng = request.getParameter("lng");//精度
			Point p = null;
			Distance dis = null;
			if(null!=lat && null!= lng){
			 p = new Point(Double.parseDouble(lng),Double.parseDouble(lat));
			 dis = new Distance(10,Metrics.KILOMETERS);
			}
			List<ResponseGroup> group = groupService.findAllNearGroup(currentUser.getId(),p,dis);
			long groupCount = groupService.groupCount(currentUser.getId());
			ResponseNearGroup rng = new ResponseNearGroup(group,groupCount);
			this.getReponseData().setResult(rng);
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，查询所有群组不支持翻页==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，查询所有群组不支持翻页==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 查询值得加入的群
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("worthGroups")
	public @ResponseBody ResponseContainer worthGroups(HttpServletRequest request){
		try{
			String num = request.getParameter("num");
			List<JSONObject> group = groupPcService.worthGroups(Integer.valueOf(num));
			return addResponse(Config.STATUS_200, Config.MSG_200, group, Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("===========未知错误，查询值得加入的群==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false, Config.RESP_MODE_10, "");
		}
	
	}
	
	/**
	 * 获取一个人加入群的数量
	 * @param request
	 * @return
	 */
	@RequestMapping("/groupCount")
	public @ResponseBody ResponseContainer groupCount(HttpServletRequest request) {
		try {
			String userId = request.getParameter("userId");
			List<XueWenGroup> myGroup = myGroupService.myGroups(userId);
			int count = 0;
			if(myGroup != null ){
				count = myGroup.size();
			}
			long dryCount = drycargoService.getDryCount(userId,0);
			long xuanYeCount = drycargoService.getDryCount(userId,1);
			int courseCount = userGroupCourseService.getStudyedCountByUser(userId);
			UserMoreMessage umm = new UserMoreMessage(count,dryCount,courseCount,xuanYeCount);
			return addResponse(Config.STATUS_200, Config.MSG_200, umm,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 通过用户id查询群组
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/myAllGroup")
	public @ResponseBody ResponseContainer myAllGroup(HttpServletRequest request) {
		String token = request.getParameter("token");
	//	User currentUser = this.getCurrentUser(token);
		try {
//			String sort = "ctime";
//			String mode = "DESC";
//			Direction d = Direction.DESC;
//			if(mode.equalsIgnoreCase("ASC")){
//				d = Direction.ASC;
//			}
//			Sort st = new Sort(d,sort);
			User currentUser = this.getCurrentUser(token);
//			List<XueWenGroup> owner = groupService.findMyCreatedGroup(currentUser.getId(),st);
//			List<Object>  ownerResponse = new ArrayList<Object>();
//			if(owner!=null && owner.size()>0){
//		//	ownerResponse = groupService.changeGroupsToResponseGroups(owner,currentUser.getId());
//			ownerResponse = groupService.toGroupResponses(owner,currentUser.getId());
//			}
//			List<XueWenGroup> admin = groupService.findMyAdminGroup(currentUser.getId(),st);
//			List<Object>  adminResponse = new ArrayList<Object>();
//			if(admin!=null && admin.size()>0){
//			adminResponse = groupService.toGroupResponses(admin,currentUser.getId());
//			}
//			List<XueWenGroup> member = groupService.findMyMemberGroup(currentUser.getId(),st);
//			List<Object>  memberResponse = new ArrayList<Object>();
//			if(member!=null && member.size()>0){
//			memberResponse = groupService.toGroupResponses(member,currentUser.getId());
//			}
//			MyAllGroup myGroup = new MyAllGroup(ownerResponse,adminResponse,memberResponse);
//			this.getReponseData().setResult(myGroup);
//			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
			List<XueWenGroup> group = groupService.getMyGroups(currentUser.getId());
			this.getReponseData().setResult(groupService.MyGroups(group,currentUser.getId()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，通过用户ID查找群组==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，通过用户ID查找群组==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 批量添加群组用户
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value="registGroupUser", method=RequestMethod.POST)
	public @ResponseBody ResponseContainer registGroupUserPc(HttpServletRequest request,String users,String groupId,String md5) {
		try {
			groupService.registGroupUser(users, groupId, md5);
			return addResponse(Config.STATUS_200,Config.MSG_200,Config.MSG_JOIN_200,Config.RESP_MODE_10,"");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
		} catch (Exception e){
			e.printStackTrace();
			return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
		}
	}
	
	/**
	 * 根据群组ID查询群成员列表分页
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/one/{id}/memberPcPage")
	public @ResponseBody ResponseContainer findMemberPcPage(QueryModel dm,
			@PathVariable("id") String id, String userName,HttpServletRequest request) {
		try {
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<User> pageUser = groupService.memberPcPage(userName,id,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), pageUser);
			getReponseData().setResult(groupService.toResponsePcUser(pageUser.getContent()));
			return addPageResponse(Config.STATUS_200,Config.MSG_200,getReponseData(),Config.RESP_MODE_10,"");
		} catch (XueWenServiceException e) {
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 根据群组ID查询群成员列表分页(不包含管理员)
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/one/{id}/memberPcPageOnlyMember")
	public @ResponseBody ResponseContainer memberPcPageOnlyMember(QueryModel dm,@PathVariable("id") String id,HttpServletRequest request) {
		try {
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<User> pageUser = groupService.memberPcPageOnlyMember(id,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), pageUser);
			getReponseData().setResult(groupService.toResponsePcUser(pageUser.getContent()));
			if (getReponseData().getTotal_rows()==0) {
				getReponseData().setResult(null);
			}
			return addPageResponse(Config.STATUS_200,Config.MSG_200,getReponseData(),Config.RESP_MODE_10,"");
		} catch (XueWenServiceException e) {
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 查询所有群组支持翻页
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/recommend")
	public @ResponseBody ResponseContainer Recommend(QueryModelMul dm,HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			List<String> sort = new ArrayList<String>();
			sort.add("memberCount");
			sort.add("ctime");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<XueWenGroup> group = groupService.recommend(pageable,currentUser.getId());
			ReponseDataTools.getClientReponseData(getReponseData(), group);
			//this.getReponseData().setResult(groupService.changeGroupsToResponseGroups(group.getContent(),currentUser.getId()));
			this.getReponseData().setResult(groupService.toGroupResponses(group.getContent(),currentUser.getId()));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		}catch (XueWenServiceException e) {
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}
		
		catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}	
	}
	
	/**
	 * 
	 * @Title: isGroupMember
	 * @Description: 判断是否是群成员
	 * @param gId
	 * @param uId
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("isGroupMember")
	public @ResponseBody ResponseContainer isGroupMember(String gId,String token){
		try {
			User user=getCurrentUser(token);
			boolean ismember=groupPcService.isGroupMember(gId, user.getId());
			return addResponse(Config.STATUS_200, Config.MSG_200, ismember, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false, Config.RESP_MODE_10, "");
		}
		
	}
	/**
	 * @Title: 根据群组ID查询出群组的Id，群组名称，群组头像，群组成员数量，群组课程数量
	 * @author hjn
	 * @Description: 判断是否是群成员
	 * @param gId
	 * @param uId
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("groupSimpleInfo")
	public @ResponseBody ResponseContainer groupSimpleInfo(HttpServletRequest request){
		try {
			String token=request.getParameter("token");
			User user=this.getCurrentUser(token);
			String groupId=request.getParameter("groupId");
			Object obj=groupService.findByIdRspGroupNanemAndMemberCountAndCourseCount(groupId,user.getId());
			return addResponse(Config.STATUS_200,Config.MSG_200,obj,Config.RESP_MODE_10,"");
		} catch (XueWenServiceException e) {
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
		
	}
	/**
	 * 
	 * @Title: getGroupNewMembers
	 * @Description: 取群活跃成员（最新成员）
	 * @param groupId
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping(value="getGroupNewMembers")
	public @ResponseBody ResponseContainer getGroupNewMembers(String groupId,int s){
		  try {
			List<JSONObject> users=groupService.getGroupfreshMember(groupId,s);
			return addResponse(Config.STATUS_200,Config.MSG_200,users,Config.RESP_MODE_10,"");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 
	 * @Title: findMyMemberGroup
	 * @Description: 通过用户ID查询自己为成员小组（不包含创建者跟管理员）
	 * @param request
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("/findMyMemberGroup")
	public @ResponseBody ResponseContainer findMyMemberGroup(HttpServletRequest request) {
		try {
			User user = this.getCurrentUser(request.getParameter("token"));
			List<XueWenGroup> xue = groupService.findMyMemberGroup(user.getId());
			String[] includeKey = {"id","groupName","logoUrl","intro"};
			List<JSONObject> jsonObjects = groupService.toResponse(xue, includeKey);
			return addResponse(Config.STATUS_200, Config.MSG_200, jsonObjects,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 
	 * @Title: findMyMemberGroup
	 * @Description: 通过用户ID查询自己为成员小组（不包含创建者跟管理员）
	 * @param request
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("/findMyMemberGroupById")
	public @ResponseBody ResponseContainer findMyMemberGroupById(HttpServletRequest request,String userId) {
		try {
			List<XueWenGroup> xue = groupService.findMyMemberGroup(userId);
			String[] includeKey = {"id","groupName","logoUrl","intro"};
			List<JSONObject> jsonObjects = groupService.toResponse(xue, includeKey);
			return addResponse(Config.STATUS_200, Config.MSG_200, jsonObjects,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	/**
	 * 
	 * @Title: findMyMemberGroup
	 * @Description: 通过用户ID查询自己为成员小组（不包含创建者跟管理员）分页
	 * @param request
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("/findMyMemberPageGroups")
	public @ResponseBody ResponseContainer findMyMemberPageGroup(HttpServletRequest request,QueryModel dm) {
		try {
			Pageable pageable=PageRequestTools.pageRequesMake(dm);
			User user = this.getCurrentUser(request.getParameter("token"));
			Page<XueWenGroup> groups = groupService.findMyMemberGroup(user.getId(),pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), groups);
			String[] includeKey = {"id","groupName","logoUrl"};
			List<JSONObject> jsonObjects = groupService.toResponse(groups.getContent(), includeKey);
			getReponseData().setResult(jsonObjects);
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	/**
	 * 
	 * @Title: findMyMemberGroup
	 * @Description: 通过用户ID查询自己为成员小组（不包含创建者跟管理员）分页
	 * @param request
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("/findMyMemberPageGroupsById")
	public @ResponseBody ResponseContainer findMyMemberPageGroup(String userId,HttpServletRequest request,QueryModel dm) {
		try {
			Pageable pageable=PageRequestTools.pageRequesMake(dm);
			User user = userService.findOne(userId);
			Page<XueWenGroup> groups = groupService.findMyMemberGroup(user.getId(),pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), groups);
			String[] includeKey = {"id","groupName","logoUrl"};
			List<JSONObject> jsonObjects = groupService.toResponse(groups.getContent(), includeKey);
			getReponseData().setResult(jsonObjects);
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 
	 * @Title: findMyAdminGroups
	 * @Description: 通过用户ID查询自己为管理员的小组（不包含成员）
	 * @param request
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("/findMyAdminGroups")
	public @ResponseBody ResponseContainer findMyAdminGroups(HttpServletRequest request) {
		try {
			User user = this.getCurrentUser(request.getParameter("token"));
			List<XueWenGroup> xue = groupService.findMyAdminGroups(user.getId());
			String[] includeKey = {"id","groupName","logoUrl"};
			List<JSONObject> jsonObjects = groupService.toResponse(xue, includeKey);
			return addResponse(Config.STATUS_200, Config.MSG_200, jsonObjects,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 
	 * @Title: findMyAdminGroups
	 * @Description: 通过用户ID查询自己为管理员的小组（不包含成员）
	 * @param request
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("/findMyAdminGroupsById")
	public @ResponseBody ResponseContainer findMyAdminGroups(HttpServletRequest request,String userId) {
		try {
			List<XueWenGroup> xue = groupService.findMyAdminGroups(userId);
			String[] includeKey = {"id","groupName","logoUrl"};
			List<JSONObject> jsonObjects = groupService.toResponse(xue, includeKey);
			return addResponse(Config.STATUS_200, Config.MSG_200, jsonObjects,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	/**
	 * 
	 * @Title: findMyAdminGroups
	 * @Description: 通过用户ID查询自己为管理员的小组（不包含成员） 带分页
	 * @param request
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("/findMyAdminPageGroups")
	public @ResponseBody ResponseContainer findMyAdminPageGroups(HttpServletRequest request,QueryModel dm) {
		try {
			User user = this.getCurrentUser(request.getParameter("token"));
			Pageable pageable=PageRequestTools.pageRequesMake(dm);
			Page<XueWenGroup> groups = groupService.findMyAdminPageGroups(user.getId(),pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), groups);
			String[] includeKey = {"id","groupName","logoUrl","intro"};
			List<JSONObject> jsonObjects = groupService.toResponse(groups.getContent(), includeKey);
			if (jsonObjects.size()==0) {
				getReponseData().setResult(null);
			}else{
				getReponseData().setResult(jsonObjects);
			}
			
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 
	 * @Title: findMyAdminGroups
	 * @Description: 通过用户ID查询自己为管理员的小组（不包含成员） 带分页
	 * @param request
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("/findMyAdminPageGroupsById")
	public @ResponseBody ResponseContainer findMyAdminPageGroups(String userId,HttpServletRequest request,QueryModel dm) {
		try {
			User user = userService.findOne(userId);
			Pageable pageable=PageRequestTools.pageRequesMake(dm);
			Page<XueWenGroup> groups = groupService.findMyAdminPageGroups(user.getId(),pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), groups);
			String[] includeKey = {"id","groupName","logoUrl","intro"};
			List<JSONObject> jsonObjects = groupService.toResponse(groups.getContent(), includeKey);
			if (jsonObjects.size()==0) {
				getReponseData().setResult(null);
			}else{
				getReponseData().setResult(jsonObjects);
			}
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 
	 * @Title: getTagByType
	 * @Description: 查找学习小组的热门标签
	 * @param request
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("/getTagByType")
	public @ResponseBody ResponseContainer  getTagByType(HttpServletRequest request) {
		try {
			return addResponse(Config.STATUS_200, Config.MSG_200, groupPcService.getTagByType(),Config.RESP_MODE_10, "");
		}catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 申请多人入群
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/join")
	public @ResponseBody ResponseContainer joinManyUser(HttpServletRequest request) {
		try {
			String userIds = request.getParameter("userIds");
			String groupId = request.getParameter("groupId");
			boolean result = groupService.joinManyUser(userIds, groupId);
			return addResponse(Config.STATUS_200, Config.MSG_JOIN_200, result,Config.RESP_MODE_10, "");
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，加入群==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}

	}
	
	/**
	 * 判断是否是管理员
	 * @param request
	 * @return
	 */
	@RequestMapping("isAdmin")
	public @ResponseBody ResponseContainer isAdmin(HttpServletRequest request){
		try {
			String token = request.getParameter("token");
			String groupId=request.getParameter("groupId");
			User user=this.getCurrentUser(token);
			return addResponse(Config.STATUS_200, Config.MSG_200,groupService.isAdmin(groupId,user),Config.RESP_MODE_10, "");
		}catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 通过用户id查询群组
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/myGroupNotShare")
	public @ResponseBody ResponseContainer myGroupNotShare(HttpServletRequest request) {
		String token = request.getParameter("token");
		String source = request.getParameter("group");//群组id 数组json
		try {
			User user=this.getCurrentUser(token);
			List<XueWenGroup> group = groupService.findMyGroup(user.getId());
			this.getReponseData().setResult(groupService.toGroupNotActivity(group,user.getId(),source));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("===========业务错误，通过用户ID查找群组==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，通过用户ID查找群组==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
}
