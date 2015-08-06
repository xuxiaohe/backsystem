package operation.controller.test;

import javax.servlet.http.HttpServletRequest;

import operation.BaseController;
import operation.exception.XueWenServiceException;
import operation.pojo.course.Chapter;
import operation.pojo.user.User;
import operation.repo.course.KnowledgeRepository;
import operation.service.course.ChapterService;
import operation.service.group.GroupNumService;
import operation.service.group.GroupService;
import operation.service.qrcode.QRCodeService;
import operation.service.rabbitmq.RabbitmqService;
import operation.service.user.UserCourseChapterService;
import operation.service.user.UserService;
import operation.service.user.UserSkillsService;
import operation.service.user.UserStudyResultService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tools.Config;
import tools.ResponseContainer;


@RestController
@RequestMapping("/mytest")
@Configuration
public class MyTestController extends BaseController{

	private static final Logger logger=Logger.getLogger(MyTestController.class);
	@Autowired
	public GroupService groupService;
	@Autowired
	public GroupNumService groupNumService;
	
	@Autowired
	public RabbitmqService rabbitservice;
	
	
	@Autowired
	public ChapterService chapterService;
	
	@Autowired
	public UserService userService;
	
	@Autowired
	public UserCourseChapterService userCourseChapterService;
	
	@Autowired
	public UserStudyResultService userStudyResultService;
	
	@Autowired
	public UserSkillsService userSkillsService;
	
	@Autowired
	private QRCodeService qRCodeService;
	
	@Autowired
	private KnowledgeRepository knowledgeRes;
	
	@Autowired
	private RabbitmqService rabb;
	
	
	/**
	 * 创建多个群组
	 * @param request
	 * @return
	 */
//	@RequestMapping("/createManyGroups")
//	public @ResponseBody ResponseContainer createManyGroups(HttpServletRequest request) {
//		try {
//			String max=request.getParameter("number");
//			int m=Integer.valueOf(max);
//			groupService.createManyGroups(m);;
//			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
//		} catch (XueWenServiceException e) {
//			e.printStackTrace();
//			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
//		}
//	}
	
//	@RequestMapping("/updateGroups")
//	public @ResponseBody ResponseContainer updateGroups(HttpServletRequest request) {
//		try {
//			groupService.updateGroups();
//			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
//		} catch (XueWenServiceException e) {
//			e.printStackTrace();
//			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
//		}
//	}
	
	/**
	 * 批量修改用户默认头像
	 * @param request
	 * @return
	 */
//	@RequestMapping("/updateUsers")
//	public @ResponseBody ResponseContainer updateUsers(HttpServletRequest request) {
//		try {
//			groupService.updateUser();
//			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
//		} catch (XueWenServiceException e) {
//			e.printStackTrace();
//			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
//		}
//	}
	
	/**
	 * 一次创建多个话题
	 * @param request
	 * @return
	 */
	@RequestMapping("/createManySubjects")
	public @ResponseBody ResponseContainer createManySubjects(HttpServletRequest request) {
		try {
			String max=request.getParameter("number");
			int m=Integer.valueOf(max);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} 
//		catch (XueWenServiceException e) {
//			e.printStackTrace();
//			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
//		} 
		catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	@RequestMapping("/cptest")
	public @ResponseBody ResponseContainer test(HttpServletRequest request) {

		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String chapterId=request.getParameter("chapterId");
			Chapter ct=chapterService.getChapter(chapterId);
			return addResponse(Config.STATUS_200, Config.MSG_200, ct,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 测试使用，创建章节序号
	 * @param request
	 * @return
	 */
//	@RequestMapping("/createOrder")
//	public @ResponseBody ResponseContainer createOrder(HttpServletRequest request) {
//
//		try {
//			String token = request.getParameter("token");
//			User currentUser = this.getCurrentUser(token);
//			String chapterId=request.getParameter("chapterId");
//			chapterService.setChapterOrder();
//			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
//		} catch (XueWenServiceException e) {
//			e.printStackTrace();
//			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
//			return addResponse(e.getCode(), e.getMessage(), false,
//					Config.RESP_MODE_10, "");
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
//			return addResponse(Config.STATUS_505, Config.MSG_505, false,
//					Config.RESP_MODE_10, "");
//		}
//	}
	
	/**
	 * 用户数量
	 * @param request
	 * @return
	 */
	@RequestMapping("/countUser")
	public @ResponseBody ResponseContainer countUser(HttpServletRequest request) {

		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			int i=userService.countUsers();
			return addResponse(Config.STATUS_200, Config.MSG_200, i,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	
//	@RequestMapping("/allUserId")
//	public @ResponseBody ResponseContainer allUserId(HttpServletRequest request) {
//
//		try {
//			String token = request.getParameter("token");
//			User currentUser = this.getCurrentUser(token);
//			List<User> users=userService.findAllUserOnlyId();
//			return addResponse(Config.STATUS_200, Config.MSG_200, users,Config.RESP_MODE_10, "");
//		} catch (XueWenServiceException e) {
//			e.printStackTrace();
//			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
//			return addResponse(e.getCode(), e.getMessage(), false,
//					Config.RESP_MODE_10, "");
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
//			return addResponse(Config.STATUS_505, Config.MSG_505, false,
//					Config.RESP_MODE_10, "");
//		}
//	}
	
	/**
	 * 计算单个用户的学习时间
	 * @param request
	 * @return
	 */
	@RequestMapping("/userStudyTimer")
	public @ResponseBody ResponseContainer userStudyTimer(HttpServletRequest request) {

		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
		    long timer=userCourseChapterService.countUserStudyTime(currentUser.getId());
			return addResponse(Config.STATUS_200, Config.MSG_200, timer,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	
	@RequestMapping("/saveAllSutdyResult")
	public @ResponseBody ResponseContainer saveAllSutdyResult(HttpServletRequest request) {

		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			userStudyResultService.saveAllUserStudyResult();
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	
	
	@RequestMapping("/updateSkilltreeCourseNum")
	public @ResponseBody ResponseContainer updateSkilltreeCourseNum(HttpServletRequest request) {

		try {
			userSkillsService.addCourseNumForUserSKillForTest();
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	@RequestMapping("/qrCode")
	public @ResponseBody ResponseContainer qrCode(HttpServletRequest request) {
		
		try {
			String content=request.getParameter("content");
			String qrurl=request.getParameter("local");
			String name=request.getParameter("name");
			String high=request.getParameter("high");
			String weight=request.getParameter("weight");
			qRCodeService.creadQRCode(content, qrurl,name, Integer.valueOf(high),Integer.valueOf(weight));
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	
	@RequestMapping("/createQRCodeForEveryGroup")
	public @ResponseBody ResponseContainer createQRCodeForEveryGroup(HttpServletRequest request) {
		
		try {
			groupService.createQRCodeForEveryGroup();
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	
	@RequestMapping("t7")
	public @ResponseBody Object test7(){
		try {
			rabb.sendMessage("hello", "test11111");
		} catch (XueWenServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return null;
		
	}
	
	
	@RequestMapping("/megergroup")
	public @ResponseBody ResponseContainer megergroup(HttpServletRequest request) {
		
		try {
			groupService.mergeUserGroup("54b85fe3e4b0078fad6d8db8", "54f158a2e4b0dafb081de342");
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	

}
