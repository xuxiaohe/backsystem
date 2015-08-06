package operation.controller.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;
import operation.BaseController;
import operation.exception.XueWenServiceException;
import operation.pojo.fav.Fav;
import operation.pojo.industry.IndustryBean;
import operation.pojo.pub.QueryModel;
import operation.pojo.user.ContactAdress;
import operation.pojo.user.ResponsePcUser;
import operation.pojo.user.ResponseUser;
import operation.pojo.user.ResponseUserFriendShip;
import operation.pojo.user.User;
import operation.pojo.user.UserMessage;
import operation.pojo.user.UserStudyResult;
import operation.service.course.UserCourseService;
import operation.service.drycargo.DrycargoService;
import operation.service.drycargo.UserDrycargoService;
import operation.service.fav.FavService;
import operation.service.file.MyFileService;
import operation.service.group.GroupService;
import operation.service.jobs.IndustryService;
import operation.service.log.UserRegistLogService;
import operation.service.queue.QueueService;
import operation.service.rabbitmq.RabbitmqService;
import operation.service.sms.SmsService;
import operation.service.tags.TagService;
import operation.service.topics.TopicService;
import operation.service.user.ContactUserService;
import operation.service.user.UserContactListService;
import operation.service.user.UserFriendShipService;
import operation.service.user.UserInviteService;
import operation.service.user.UserNameService;
import operation.service.user.UserNumService;
import operation.service.user.UserPcService;
import operation.service.user.UserService;
import operation.service.user.UserStudyResultService;
import operation.service.util.ObjCopyPropsService;
import operation.service.version.VersionService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import tools.Config;
import tools.JSON2ObjUtil;
import tools.PageRequestTools;
import tools.ReponseDataTools;
import tools.ResponseContainer;
import tools.StringUtil;

@RestController
@RequestMapping("/user")
@Configuration
/**
 * 用户信息Controller,用于接受客户端传递
 * 的数据，进行用户相关操作
 * @author nes
 *
 */
public class UserController extends BaseController {
	private static final Logger logger=Logger.getLogger(UserController.class);
	
	@Inject Environment env;
	
	@Autowired
	public UserService userService;
	
	@Autowired
	public UserPcService userPcService;

	
	@Autowired
	public ObjCopyPropsService objCopyPropsService;
	
	@Autowired
	public QueueService queueService;
	
	@Autowired
	public UserNumService userNumService;   //20140911 新增创建用户时，创建用户号
	@Autowired
	public MyFileService myFileService;
	
	@Autowired
	public UserContactListService userContactListService;
	
	@Autowired
	public UserFriendShipService  userFriendShipService;
	
	@Autowired
	public IndustryService industryService;
	
	@Autowired
	public UserCourseService userCourseService;
	
	@Autowired
	public GroupService groupService;
	
	@Autowired
	public VersionService versionService;
	
	@Autowired
	public UserInviteService userInviteService;
	
	@Autowired
	public UserStudyResultService userStudyResultService;
	
	@Autowired
	public SmsService smsService;
	
	@Autowired
	private ContactUserService contactUserService;
	
	@Autowired
	private UserDrycargoService userDrycargoService;
	
	@Autowired
	private UserNameService userNameService;
	
	@Autowired
	private TagService tagService;
	
	@Autowired
	private UserRegistLogService userRegistLogService;
	
	@Autowired
	private DrycargoService drycargoService;
	@Autowired
	private FavService favcargoService;
	
	@Autowired
	private TopicService topicService;
	
	@Autowired
	private RabbitmqService  rabbitmqService;
	
	public UserController() {
		super();
	}
	@Value("${tag.service.url}")
	private String tagServiceUrl;
	/**
	 * 创建用户表，用于保存地理坐标二维数组
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/createtable")
	public  void createTable(){
		try {
			userService.creatTable();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	/**
	 * 用户注册
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value="regist", method=RequestMethod.POST)
	public @ResponseBody ResponseContainer regist(HttpServletRequest request,User user,String a) {
		try {
			//20140811,增加地理坐标
			String lat = request.getParameter("lat"); //维度
			String lng = request.getParameter("lng");//精度
			String robot = request.getParameter("robot");//机器人标志
			String emailUrl = request.getParameter("emailUrl");//邮箱注册激活url
			String appKey=request.getParameter("appKey");
			user=userService.regist(user,lat,lng,robot,emailUrl,appKey);// 调用service进行用户注册，并返回注册结果
			userRegistLogService.add(a,user);
			return addResponse(Config.STATUS_200,Config.MSG_REGIST_200,user,Config.RESP_MODE_10,"");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("=========创建用户业务错误："+e);
			return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
		} catch (Exception e){
			e.printStackTrace();
			logger.error("=========创建用户系统错误："+e);
			return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
		}
	}
	
	/**
	 * 与群组关联注册用户(激活)
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value="registByGroup", method=RequestMethod.POST)
	public @ResponseBody ResponseContainer registByGroup(HttpServletRequest request,User user,String groupNumber,String inviteUserID) {
		try {
			//user=userPcService.registByGroupId(user,groupNumber);// 调用service进行用户注册，并返回注册结果
			String appKey=request.getParameter("appKey");
			userService.regist(user, "0", "0", null, null,appKey);
			userService.joinGroup(groupNumber,user);
			// 调用登录
			user=userService.login(user,appKey);
			userRegistLogService.addByUserName(inviteUserID, user);
			return addResponse(Config.STATUS_200,Config.MSG_LOGIN_200,user,Config.RESP_MODE_10,"");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
		} catch (Exception e){
			e.printStackTrace();
			return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
		}
	}
	
	/**
	 * 与群组关联注册用户
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value="registByGroupUncheck", method=RequestMethod.POST)
	public @ResponseBody ResponseContainer registByGroupUncheck(String serverName,HttpServletRequest request,User user,String groupNumber,String inviteUserID) {
		try {
			//user=userPcService.registByGroupId(user,groupNumber);// 调用service进行用户注册，并返回注册结果
			String appKey=request.getParameter("appKey");
			userService.registUncheck(serverName,user, "0", "0", null, serverName,appKey);
			try {
				userService.joinGroup(groupNumber,user);
			} catch (Exception e) {
				
			}
			// 调用登录
			user=userService.login(user,appKey);
//			Map<String, Object>map=new HashMap<>();
//			map.put("groupId", groupNumber);
//			//JSONObject object=YXTJSONHelper.addAndModifyAttrJsonObject(user, map,);
//			JSONObject object=YXTJSONHelper.getExObjectAttrJsonObject(user, map,"job","userTageName","interestJob","openFireUser");
			JSONObject object=userService.shutDown(user, groupNumber);
			userRegistLogService.addByUserName(inviteUserID, user);
			return addResponse(Config.STATUS_200,Config.MSG_LOGIN_200,object,Config.RESP_MODE_10,"");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
		} catch (Exception e){
			e.printStackTrace();
			return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
		}
	}
	/**
	 * 获取所有行业名称新版
	 * @param request
	 * @return
	 */
	@RequestMapping("industry")
	public  ResponseContainer getAllIndustry(HttpServletRequest request) {
			try {
				List<IndustryBean> list =industryService.getAllNewIndustry();
				return addResponse(Config.STATUS_200,Config.MSG_200,list,Config.RESP_MODE_10,"");
			} catch (XueWenServiceException ex) {
				ex.printStackTrace();
				return addResponse(ex.getCode(),ex.getMessage(),false,Config.RESP_MODE_10,"");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
			}		
	}
	
	/**
	 * 获取某个行业对应的行业方向新版
	 * @param request
	 * @return
	 */
	@RequestMapping("indirect")
	public  ResponseContainer getAllIndustDirectById(String directId,HttpServletRequest request) {
			try {
				if(!StringUtil.isBlank(directId))
				{
					IndustryBean bean =industryService.getInduDirect(directId);
					return addResponse(Config.STATUS_200,Config.MSG_200,bean.indDirectList,Config.RESP_MODE_10,"");
				}
				else
				{
					return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
				}
			} catch (XueWenServiceException ex) {
				ex.printStackTrace();
				return addResponse(ex.getCode(),ex.getMessage(),false,Config.RESP_MODE_10,"");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
			}		
	}
	
	
	/**
	 * 用户登录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("login")
	public @ResponseBody ResponseContainer login(HttpServletRequest request,User user) {
		try {
			String appKey=request.getParameter("appKey");
			//20140811,增加地理坐标
			String lat = request.getParameter("lat"); //维度
			String lng = request.getParameter("lng");//精度
			if(null !=lat && null != lng){
				double [] position = new double[]{Double.parseDouble(lng),Double.parseDouble(lat)};
				user.setLng(Double.parseDouble(lng));
				user.setLat(Double.parseDouble(lat));
				user.setLocation(position);
			}
			long nowTime = System.currentTimeMillis();// 获取系统时间戳
			user.setLogintime(nowTime);
			User loginUser = userService.login(user,appKey);
			return addResponse(Config.STATUS_200,Config.MSG_LOGIN_200,loginUser,Config.RESP_MODE_10,"");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
		} catch (Exception e){
			e.printStackTrace();
			return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
		}
	}
	
	/**
	 * 第三方登陆
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("loginThrid")
	public @ResponseBody ResponseContainer loginThrid(HttpServletRequest request,User user,String a) {
		try {
			String appKey=request.getParameter("appKey");
			long nowTime = System.currentTimeMillis();// 获取系统时间戳
			user.setLogintime(nowTime);
			User loginUser = userService.loginByThird(user,appKey,a);			
			return addResponse(Config.STATUS_200,Config.MSG_LOGIN_200,loginUser,Config.RESP_MODE_10,"");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
		} catch (Exception e){
			e.printStackTrace();
			return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
		}
	}
	/**
	 * 用户退出
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("loginOut")
	public @ResponseBody ResponseContainer loginOut(HttpServletRequest request) {
		try {
			String appKey=request.getParameter("appKey");
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String lat = request.getParameter("lat"); //维度
			String lng = request.getParameter("lng");//精度
			if(null !=lat && null != lng){
				double [] position = new double[]{Double.parseDouble(lng),Double.parseDouble(lat)};
				currentUser.setLng(Double.parseDouble(lng));
				currentUser.setLat(Double.parseDouble(lat));
				currentUser.setLocation(position);
				}
			userService.loginOut(currentUser,token,appKey);
			return addResponse(Config.STATUS_200,Config.MSG_OUT_200,true,Config.RESP_MODE_10,"");
		} 
		catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
		} catch (Exception e){
			e.printStackTrace();
			return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
		}
	}
	/**
	 * 获得当前登录用户
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("checkLogin")
	public @ResponseBody ResponseContainer checkLogin(HttpServletRequest request, User user) {
		try{
			User loginUser = userService.checkLogin(user);
			return addResponse(Config.STATUS_200,Config.MSG_CHECK_200,loginUser,Config.RESP_MODE_10,"");
		}catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
		} catch (Exception e){
			e.printStackTrace();
			return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
		}
	}
	
	
	/**
	 * 
	 * @Title: loginFromGroup
	 * @auther Tangli
	 * @Description: 用于群邀请
	 * @param request
	 * @param user
	 * @param groupNumber
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("loginFromGroup")
	public @ResponseBody ResponseContainer loginFromGroup(HttpServletRequest request, User user,String groupNumber,String inviteUserID ) {
		logger.info("=============群邀请登陆=======");
		//TODO 等业务出需求 做统计
		try{
			String appKey=request.getParameter("appKey");
			// 调用登录
			user=userService.login(user,appKey);	
			try {
				userService.joinGroup(groupNumber,user);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			return addResponse(Config.STATUS_200,Config.MSG_CHECK_200,user,Config.RESP_MODE_10,"");
		}catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(),e.getMessage(),null,Config.RESP_MODE_10,"");
		} catch (Exception e){
			e.printStackTrace();
			return addResponse(Config.STATUS_505,Config.MSG_505,null,Config.RESP_MODE_10,"");
		}
	}
	

	/**
	 * 根据用户ID 查询用户信息
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/one/{id}")
	public @ResponseBody ResponseContainer findUser(HttpServletRequest request,@PathVariable("id") String id) {
		try{
			String token=request.getParameter("token");
			User currentUser=this.getCurrentUser(token);
			ResponseUser respUser;
			if(id.equals(currentUser.getId())){
				User user = userService.findUser(id);
				respUser = new ResponseUser(user);
				respUser.setContactStatus(10);
			}else{
				User user = userService.findUser(id);
				respUser = new ResponseUser(user);
				respUser.setContactStatus(contactUserService.contact(currentUser.getId(), id));
			}
			//获得该用户粉丝与关注人的数量
			respUser.setAttentionCount(contactUserService.getUserContact(id, "1"));
			respUser.setFollowerCount(contactUserService.getUserContact(id, "0"));
			respUser.setDryCargoCount(drycargoService.getDryCount(id, 0));
			respUser.setTopicCount(topicService.getCountsByUserId(id));
			return addResponse(Config.STATUS_200,Config.MSG_200,respUser,Config.RESP_MODE_10,"");
		}catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
		} catch (Exception e){
			e.printStackTrace();
			return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
		}
		
	}
	/**
	 * 根据用户ID 查询用户信息pc
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/onePc/{id}")
	public @ResponseBody ResponseContainer findUserPc(@PathVariable("id") String id) {
		try{
			ResponsePcUser respUser = userPcService.findUserPc(id);
			return addResponse(Config.STATUS_200,Config.MSG_200,respUser,Config.RESP_MODE_10,"");
		}catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
		} catch (Exception e){
			e.printStackTrace();
			return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
		}
		
	}
	/**
	 * 用户更新
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/update/{id}",method=RequestMethod.POST)
	public @ResponseBody ResponseContainer updateUser(@PathVariable("id") String id, HttpServletRequest request,User user) {
		User oldUser = null;
		try{
			String token =  request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			if(!currentUser.getId().equals(id)){
				throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
			}
			boolean isRabb=false;
			oldUser = userService.findUser(id);
			user.setId(id);
			if(user.getBirthday() == 0){
				user.setBirthday(oldUser.getBirthday());
			}
			if(user !=null && !StringUtil.isBlank(user.getNickName()) && !user.getNickName().equals(oldUser.getNickName())){
				isRabb=true;
			}
			if(user !=null && !StringUtil.isBlank(user.getLogoURL()) && !user.getLogoURL().equals(oldUser.getLogoURL())){
				isRabb=true;
			}
			//将请求参数中USER的属性拷贝入OLDUSER
			oldUser=(User)objCopyPropsService.copyPropertiesExclude(user, oldUser, new String[]{"username","ctime","expiretime","logintime","usernumber","password","deleFlag","age","phonenumber","email"});
			int age = user.getAge();
			if(age!=0 && age!=oldUser.getAge()){
				oldUser.setAge(age);
			}
			String lat = request.getParameter("lat"); //维度
			String lng = request.getParameter("lng");//精度
			if(null !=lat && null != lng){
				double [] position = new double[]{Double.parseDouble(lng),Double.parseDouble(lat)};
				oldUser.setLng(Double.parseDouble(lng));
				oldUser.setLat(Double.parseDouble(lat));
				oldUser.setLocation(position);
			}
			//处理感兴趣字段
			String interest = request.getParameter("interest");
			//处理擅长字段
			String special = request.getParameter("special");
			//处理方向字段
			String industry = request.getParameter("industry");
			logger.info("获取个人昵称=============================="+oldUser.getNickName());
			logger.info("获取个人头像=============================="+oldUser.getLogoURL());
			user = userService.updateUser(oldUser,interest,special,industry);
			logger.info("用户更新成功==============================");
			if(isRabb){
				rabbitmqService.sendUpdateInfo(id, Config.UPDATE_TYPE_USER);
			}
			ResponseUser ru=new ResponseUser(user);
			return addResponse(Config.STATUS_200,Config.MSG_UPDATE_200,ru,Config.RESP_MODE_10,"");
		}catch(XueWenServiceException e){
			e.printStackTrace();
			return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
		}catch(Exception e){
			e.printStackTrace();
			return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
		}
	}
	
	/**
	 * 根据用户ID 查询用户推送列表信息
	 * 支持分页及排序
	 * @param id
	 * @return
	 */
	@RequestMapping("message")
	public @ResponseBody ResponseContainer findUserMessage(HttpServletRequest request,QueryModel dm) {
		try{
            String token = request.getParameter("token");
            User currentUser = this.getCurrentUser(token);
			//根据请求参数封装一个分页信息对象
            dm.setSort("stime");
            Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<UserMessage> userMessagePage = userService.findUserMessage(currentUser, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), userMessagePage);
			//将消息全部改为已读
			userService.updateAllRead(userMessagePage.getContent());
		    return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		}catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
		} catch (Exception e){
			e.printStackTrace();
			return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
		}
		
	}
	
	/**
	 * 根据用户消息ID删除用户此条消息
	 * @param request
	 * @return
	 */
	@RequestMapping("/message/delete")
	public @ResponseBody ResponseContainer deleteUserMessage(HttpServletRequest request) {
		String userMessageId = request.getParameter("id");
		try {
			userService.deleteOneUserMessage(userMessageId);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	 
	
	/**
	 * 根据用户消息ID 查询用户推送信息详情
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/message/{id}")
	public @ResponseBody ResponseContainer findOneUserMessage(HttpServletRequest request, @PathVariable("id") String id) {
		try {
			UserMessage userMessage = userService.findOneUserMessage(id);
			return addResponse(Config.STATUS_200, Config.MSG_200, userMessage,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 发送短信验证码
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("sendSms")
	public @ResponseBody ResponseContainer sendSms(HttpServletRequest request){
		try {
			String phoneNumber = request.getParameter("phoneNumber");
			String type=request.getParameter("type");
			smsService.sendSms(phoneNumber, type);
			User user=userService.getOneByPhoneOrEmail(phoneNumber);
			return addResponse(Config.STATUS_200,Config.MSG_SENDSMSSUCCESS_200, user,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("=========== 业务错误，发送短信验证码==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}catch (Exception e) {
			logger.error("===========未知错误，发送短信验证码==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 验证短信验证码
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("checkSms")
	public @ResponseBody ResponseContainer checkSms(HttpServletRequest request){
		String phoneNumber = request.getParameter("phoneNumber");
		String smsCode = request.getParameter("smsCode");
		String type=request.getParameter("type");
		try {
			if(StringUtil.isEmpty(phoneNumber) || !StringUtil.isMobileNO(phoneNumber)){
				throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOTMOBILE_201,null);
			}
			if(StringUtil.isEmpty(smsCode) || !StringUtil.isMsgCode(smsCode)){
				throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOMSGCODE_201,null);
			}
			smsService.checkSms(phoneNumber, type, smsCode);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("===========业务错误，验证短信验证码失败==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("===========未知错误，验证短信验证码失败==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 重置密码（手机）
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("resetPassword")
	public @ResponseBody ResponseContainer resetPassword(HttpServletRequest request){
		String passWord = request.getParameter("passWord");
		String phoneNumber = request.getParameter("phoneNumber");
		try {
			if(StringUtil.isEmpty(phoneNumber) || !StringUtil.isMobileNO(phoneNumber)){
				throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOTMOBILE_201,null);
			}
			boolean result = userService.resetPassword(passWord,phoneNumber);
			return addResponse(Config.STATUS_200, Config.MSG_RESTPASSWORD_200, result,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("===========业务错误，找回密码失败==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("===========未知错误，找回密码失败==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 用户通讯录上传
	 * @param request
	 * @return
	 */
	@RequestMapping("uploadContacts")
	public @ResponseBody ResponseContainer uploadContacts(HttpServletRequest request){
		String contacts=request.getParameter("contacts");
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			logger.info("通讯录上传串=========================="+contacts);
			userContactListService.getContacts(contacts,currentUser.getPhoneNumber(),currentUser.getId());
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("===========未知错误，用户通讯录上传失败==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，用户通讯录上传失败==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 获取用户联系人列表
	 * @param request
	 * @return
	 */
	@RequestMapping("friendShip")
	public @ResponseBody ResponseContainer friendShip(HttpServletRequest request){
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			ResponseUserFriendShip rspUserFriendShip=userFriendShipService.getResponseUserFriendShip(currentUser);
			return addResponse(Config.STATUS_200, Config.MSG_200, rspUserFriendShip,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("===========业务错误，获取用户联系人失败==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，获取用户联系人失败==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 校验旧密码是否正确
	 * @param request
	 * @return
	 */
	@RequestMapping("changePass")
	public @ResponseBody ResponseContainer changPass(HttpServletRequest request){
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String passWord = request.getParameter("passWord");
			String phoneNumber = request.getParameter("phoneNumber");
			userService.changePass(passWord, phoneNumber);
			return addResponse(Config.STATUS_200, Config.MSG_TOADMIN_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("=========== 修改失败==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 删除我的收藏
	 * @param request
	 * @return
	 */
	@RequestMapping("deleCollect")
	public @ResponseBody ResponseContainer deleCollect(HttpServletRequest request){
		try{
		String token = request.getParameter("token");
		User currentUser = this.getCurrentUser(token);
		String courseId = request.getParameter("courseId");
		userCourseService.deleFav(courseId,currentUser.getId());
		return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
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
	 * 获取用户邀请列表
	 * @param request
	 * @return
	 */
	@RequestMapping("friend")
	public @ResponseBody ResponseContainer friend(HttpServletRequest request){
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String groupId=request.getParameter("groupId");
			List<ContactAdress> ucs=userContactListService.getUserContact(currentUser.getId(),groupId);
			return addResponse(Config.STATUS_200, Config.MSG_200, ucs,Config.RESP_MODE_10, "");
			}catch (XueWenServiceException e) {
				e.printStackTrace();
				return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
			} catch (Exception e){
				e.printStackTrace();
				return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
			}
	}
	
	/**
	 * 邀请社交圈好友加入某一群
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/invite")
	public @ResponseBody ResponseContainer invite(HttpServletRequest request) {
		try {
			String groupId = request.getParameter("groupId");
			String invitedUserId=request.getParameter("invitedUserId");
			String token = request.getParameter("token");
			Map<String,String> extra=new HashMap<String, String>();
			extra.put("type","1001");
			User currentUser = this.getCurrentUser(token);
			//获得被邀请的用户
			groupService.invite(groupId,invitedUserId,currentUser,extra);
			return addResponse(Config.STATUS_200, Config.MSG_INVITEJOINGROUP_200, true,Config.RESP_MODE_10, "");
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
	 * 邀请通讯录已注册好友加入群组
	 * @author hjn
	 * 
	 */
	@RequestMapping("/inviteUserJoinGroup")
	public @ResponseBody ResponseContainer inviteUserJoinGroup(HttpServletRequest request) {
		try {
			String groupId = request.getParameter("groupId");
			String phoneNumber=request.getParameter("phoneNumber");
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			groupService.inviteUserJoinGroup(groupId, phoneNumber, currentUser);
			return addResponse(Config.STATUS_200, Config.MSG_INVITEJOINGROUP_200, true,Config.RESP_MODE_10, "");
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
	 * 邀请通讯录好友加入某一群/或注册使用APP(非注册用户)
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/inviteFriend")
	public @ResponseBody ResponseContainer inviteFriend(HttpServletRequest request) {
		try {
			String phoneNumber = request.getParameter("phoneNumber");
			String name = request.getParameter("name");
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String groupId = request.getParameter("groupId");
			//将该联系写入到邀请对象中
			userInviteService.saveUserInvite(currentUser,phoneNumber,name,groupId);
			return addResponse(Config.STATUS_200, Config.MSG_INVITE_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("=========== 业务错误，发送短信验证码==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，发送短信验证码==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	
	/**
	 * 
	 * @Title: inviteFriend
	 * @auther Tangli
	 * @Description:发送激活邮件
	 * @param request
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("/sendJHemail")
	public @ResponseBody ResponseContainer sendJHemail(String groupNumber,HttpServletRequest request,String email,String serverName) {
		try {
			userService.sendJHemail(email, serverName,groupNumber);
			//将该联系写入到邀请对象中
			return addResponse(Config.STATUS_200, Config.MSG_INVITE_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("=========== 发送邮件失败==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
			}
	}
	
	
	/**
	 * 学习成果
	 * @param request
	 * @return
	 */
	@RequestMapping("/studyResult")
	public @ResponseBody ResponseContainer studyResult(HttpServletRequest request) {
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			UserStudyResult userStudyResult=userStudyResultService.findByUserId(currentUser.getId());
			return addResponse(Config.STATUS_200, Config.MSG_200, userStudyResult,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("=========== 业务错误，发送短信验证码==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("===========未知错误，发送短信验证码==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 更新用户通讯录
	 * @param request
	 * @return
	 */
	@RequestMapping("updateContacts")
	public @ResponseBody ResponseContainer updateContacts(HttpServletRequest request){
		try {
			String contacts=request.getParameter("contacts");
			String token = request.getParameter("token");
			String groupId = request.getParameter("groupId");
			User currentUser = this.getCurrentUser(token);
			List<ContactAdress> ucs = userContactListService.updateUserContact(contacts,currentUser,groupId);
			return addResponse(Config.STATUS_200, Config.MSG_200, ucs,Config.RESP_MODE_10, "");
			}catch (XueWenServiceException e) {
				e.printStackTrace();
				return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
			} catch (Exception e){
				e.printStackTrace();
				return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
			}
	}
	
	/**
	 * 给用户打标签
	 * @param request
	 * @return
	 */
	@RequestMapping("tagUser")
	public @ResponseBody ResponseContainer tagUser(HttpServletRequest request){
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String userId = request.getParameter("userId");
			String tagName = request.getParameter("tagName");
			logger.info("用户id=============="+userId+"标签为=================="+tagName);
			if(!StringUtil.isBlank(tagName)){
				tagName = JSON2ObjUtil.getArrayFromString(tagName);
				RestTemplate restTemplate=new RestTemplate();
				restTemplate.postForObject(tagServiceUrl+"tag/createTagBatch?domain="+Config.YXTDOMAIN+"&itemId="+userId+"&userId="+currentUser.getId()+"&userName="+currentUser.getNickName()+"&itemType="+1+"&tagNames="+tagName,null, String.class);
			}
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (RestClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return addResponse(Config.STATUS_500, e.getLocalizedMessage(), true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return addResponse(Config.STATUS_500, e.getLocalizedMessage(), true,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 查询我的干货收藏
	 * @param request
	 * @return
	 */
	/**
	 * 查询我的干货收藏
	 * @param request
	 * @return
	 */
//	@RequestMapping("findDryCollect")
//	public @ResponseBody ResponseContainer findDryCollect(HttpServletRequest request,QueryModel dm){
//		try {
//            String token = request.getParameter("token");
//            User currentUser = this.getCurrentUser(token);
//            String dryFlag = request.getParameter("dryFlag");//0干货1炫页
//			if(StringUtil.isBlank(dryFlag)){
//				dryFlag = "0";
//			}
//			//根据请求参数封装一个分页信息对象
//			Pageable pageable = PageRequestTools.pageRequesMake(dm);
//			Page<UserDrycargoBean> udb = userDrycargoService.findDryCollect(currentUser.getId(),Integer.parseInt(dryFlag),pageable);
//			ReponseDataTools.getClientReponseData(getReponseData(), udb);
//			this.getReponseData().setResult(udb.getContent());
//			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),
//					Config.RESP_MODE_10, "");
//		} catch (XueWenServiceException e) {
//			e.printStackTrace();
//			return addResponse(e.getCode(), e.getMessage(), false,
//					Config.RESP_MODE_10, "");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return addResponse(Config.STATUS_505, Config.MSG_505, false,
//					Config.RESP_MODE_10, "");
//		}
//	}
	/**
	 * 查询我的干货收藏
	 * @param request
	 * @return
	 */
	@RequestMapping("findDryCollect")
	public @ResponseBody ResponseContainer findDryCollect(HttpServletRequest request,QueryModel dm){
		try {
            String token = request.getParameter("token");
            User currentUser = this.getCurrentUser(token);
            String dryFlag = request.getParameter("dryFlag");//0干货1炫页
			if(StringUtil.isBlank(dryFlag)){
				dryFlag = "0";
			}
			//根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<Fav> udb = drycargoService.getMyCollect(currentUser.getId(),Integer.parseInt(dryFlag),pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), udb);
			this.getReponseData().setResult((drycargoService.toDrycargoForFav(udb.getContent(),currentUser.getId())));
			//this.getReponseData().setResult(udb.getContent());
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),
					Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 查询我的干货收藏
	 * @param request
	 * @return
	 */
	@RequestMapping("findNewDryCollect")
	public @ResponseBody ResponseContainer findNewDryCollect(HttpServletRequest request,QueryModel dm){
		try {
            String token = request.getParameter("token");
            User currentUser = this.getCurrentUser(token);
            String dryFlag = request.getParameter("dryFlag");//0干货1炫页
			if(StringUtil.isBlank(dryFlag)){
				dryFlag = "0";
			}
			//根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<Fav> udb = drycargoService.getMyCollect(currentUser.getId(),Integer.parseInt(dryFlag),pageable);
			
			Map<String, Object> res = (drycargoService.toNewDrycargoForFav(udb.getContent(),currentUser.getId()));
			ReponseDataTools.getClientReponseData(getReponseData(), udb);
			getReponseData().setResult(res.get("items"));
			if(getReponseData().getTotal_rows()==0){
				getReponseData().setResult(null);
			}
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
			//ReponseDataTools.getClientReponseData(getReponseData(), udb);
			//this.getReponseData().setResult((drycargoService.toNewDrycargoForFav(udb.getContent(),currentUser.getId())));
			//this.getReponseData().setResult(udb.getContent());
			//return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 搜索用户
	 * @param request
	 * @return
	 */
	
	@RequestMapping("/search")
	public @ResponseBody ResponseContainer search(HttpServletRequest request,QueryModel dm) {
		
		try {
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			String keywords = request.getParameter("keywords");
			Page<User> users = userService.search(keywords,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), users);
			this.getReponseData().setResult((userService.toResponseUser(users.getContent())));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
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
	 * 
	 * @Title: findDryCollect
	 * @Description: 分页查询所有用户
	 * @param request
	 * @param dm
	 * @return ResponseContainer
	 * @throws 
	 */
	@RequestMapping("findUserPage")
	public @ResponseBody ResponseContainer findUserPage(HttpServletRequest request,QueryModel dm){
		try {
			//根据请求参数封装一个分页信息对象
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<User> users = userService.findUserPage(pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), users);
			//减少返回值
			List<User> userList = users.getContent();
			List<ResponsePcUser> responsePcUserList = new ArrayList<ResponsePcUser>();
			for (User user : userList) {
				ResponsePcUser responsePcUser = new ResponsePcUser(user.getId(), user.getUserName(), user.getToken(), user.getUdid(), user.getNickName(), user.getLogoURL());
				responsePcUserList.add(responsePcUser);
			}
			this.getReponseData().setResult(responsePcUserList);
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false, Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false, Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 删除我的干货收藏
	 * @param request
	 * @return
	 */
	@RequestMapping("deleDryCollect")
	public @ResponseBody ResponseContainer deleDryCollect(HttpServletRequest request){
		try{
		String token = request.getParameter("token");
		User currentUser = this.getCurrentUser(token);
		String drycargoId = request.getParameter("drycargoId");
		userDrycargoService.deleFav(drycargoId,currentUser.getId());
		return addResponse(Config.STATUS_200, Config.MSG_DELETE_200, true,Config.RESP_MODE_10, "");
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
	 * 获得感兴趣标签
	 * @param request
	 * @return
	 */
	@RequestMapping("interestTag")
	public @ResponseBody ResponseContainer interestTag(HttpServletRequest request){
		RestTemplate restTemplate=new RestTemplate();
		String tag = restTemplate.getForObject(tagServiceUrl+"tag/getHotBaseTags?count="+6, String.class); 
		JSONObject objj=JSONObject.fromObject(tag);
		JSONObject obss=objj.getJSONObject("data");
		net.sf.json.JSONArray childs= obss.getJSONArray("result"); 
		return addResponse(Config.STATUS_200, Config.MSG_200, childs,Config.RESP_MODE_10, "");
	}
	/**
	 * 
	 * @Title: delMyFav
	 * @auther tangli
	 * @Description:删除用户收藏
	 * @param token 登录票据
	 * @param id    收藏id
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("delMyFav")
	public  @ResponseBody ResponseContainer  delMyFav(String token,String id){
		try {
			User user=getCurrentUser(token);
			userService.delUserFav(user,id);
			return addResponse(Config.STATUS_200, Config.MSG_200, true, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("==========未知错误，删除收藏失败============");
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * @throws XueWenServiceException 
	 * 
	 * @Title: getMyFavs
	 * @Description: 获取收藏
	 * @param request
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("getMyFavs")
	public @ResponseBody ResponseContainer getMyFavs(HttpServletRequest request,String token,QueryModel dm) throws XueWenServiceException{
		User user=getCurrentUser(token);
		Pageable pageable=PageRequestTools.pageRequesMake(dm);
		Map<String, Object> res=userService.getMyFav(user.getId(),pageable);
		@SuppressWarnings("unchecked")
		Page<Fav>favs=(Page<Fav>) res.get("page");
		ReponseDataTools.getClientReponseData(getReponseData(), favs);
		getReponseData().setResult(res.get("items"));
		if(getReponseData().getTotal_rows()==0){
			getReponseData().setResult(null);
		}
		return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
	}
	

	/**
	 * @throws XueWenServiceException 
	 * 
	 * @Title: getMyFavs
	 * @Description: 获取收藏
	 * @param request
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("getMyFavsById")
	public @ResponseBody ResponseContainer getMyFavsById(String userId,HttpServletRequest request,String token,QueryModel dm) throws XueWenServiceException{
		User user=userService.findOne(userId);
		Pageable pageable=PageRequestTools.pageRequesMake(dm);
		Map<String, Object> res=userService.getMyFav(user.getId(),pageable);
		@SuppressWarnings("unchecked")
		Page<Fav>favs=(Page<Fav>) res.get("page");
		ReponseDataTools.getClientReponseData(getReponseData(), favs);
		getReponseData().setResult(res.get("items"));
		if(getReponseData().getTotal_rows()==0){
			getReponseData().setResult(null);
		}
		return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
	}
	
	/**
	 * 
	 * @Title: getUserRecTagPc
	 * @Description: 用户推荐标签
	 * @param request
	 * @return
	 * @throws XueWenServiceException ResponseContainer
	 * @throws
	 */
	@RequestMapping("getUserRecTagPc")
	public @ResponseBody ResponseContainer getUserRecTagPc(HttpServletRequest request) throws XueWenServiceException{
		try {
			String instrests = request.getParameter("instrests");
			List<String> list = userService.getUserRecTagPc(instrests, 10);
			return addResponse(Config.STATUS_200, Config.MSG_200,list, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("==========业务错误，用户推荐标签============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("==========未知错误，用户推荐标签============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 
	 * @Title: modifyPc
	 * @Description: 用户修改
	 * @param request
	 * @param mUser
	 * @param token
	 * @return
	 * @throws XueWenServiceException ResponseContainer
	 * @throws
	 */
	@RequestMapping("modify")
	public @ResponseBody ResponseContainer modifyPc(HttpServletRequest request,User mUser,String token,String birthDate ) throws XueWenServiceException{
		 User cUser=getCurrentUser(token);
		 cUser=userService.updateUserPc(mUser, cUser);
		 return addResponse(Config.STATUS_200, Config.MSG_200,cUser, Config.RESP_MODE_10, "");
	}
	
	/**
	 * 验证token有效性
	 * 
	 * @param request
	 * @param user
	 * @return
	 */
	@RequestMapping("checkToken")
	public @ResponseBody ResponseContainer checkToken(HttpServletRequest request, User user) {
		try {
			String email = request.getParameter("email");
			String token = request.getParameter("otoken");
			userService.checkToken(email, token);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("用户注册业务错误：" + e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("用户注册系统错误：" + e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * email找回密码发送激活邮箱
	 * @param request
	 * @return
	 */
	@RequestMapping("sendEmailPassWord")
	public @ResponseBody ResponseContainer sendEmailPassWord(HttpServletRequest request) {
		try {
			String email = request.getParameter("email");
			String registMailUrl = request.getParameter("registMailUrl"); //注册激活url
			String passWordMailUrl = request.getParameter("passWordMailUrl"); //找回密码激活url
			userService.sendEmailForPassWord(registMailUrl,passWordMailUrl,email);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("找回密码发送错误：" + e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("用户注册sendEmailForPassWord统错误：" + e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 重置密码(email)
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("resetPasswordForEmail")
	public @ResponseBody ResponseContainer resetPasswordForEmail(HttpServletRequest request){
		String passWord = request.getParameter("passWord");
		String email = request.getParameter("email");
		try {
			if(StringUtil.isEmpty(email) || !StringUtil.isEmailAddr(email)){
				throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOTEMAIL_201,null);
			}
			boolean result = userService.resetPasswordForEmail(passWord,email);
			return addResponse(Config.STATUS_200, Config.MSG_RESTPASSWORD_200, result,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("===========业务错误，找回密码失败==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("===========未知错误，找回密码失败==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	@RequestMapping("checkEmailOrPhone")
	public @ResponseBody ResponseContainer checkEmailOrPhone(String mp){
		boolean isHave;
		try {
			isHave = userService.findOneByPhoneOrEmail(mp);
			return addResponse(Config.STATUS_200, Config.MSG_200, isHave, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("===========参数校验失败==============");
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}

		
	}
	
	/**
	 * 
	 * @Title: bindEamil
	 * @auther Tangli
	 * @Description: 用户中心邮件绑定
	 * @param userId
	 * @param email
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("bindEamil")
	public @ResponseBody ResponseContainer bindEamil(String userId, String email,String code) {
		try {
			userService.bindEmail(userId, email,code);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,
					Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("===========绑定邮箱错误===========" );
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		}
	}
	
	
	/**
	 * 
	 * @Title: bindphone
	 * @auther Tangli
	 * @Description: 用户中心手机绑定
	 * @param userId
	 * @param email
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("bindPhone")
	public @ResponseBody ResponseContainer bindPhone(String userId, String phone,String code) {
		try {
			userService.bindPhone(userId, phone,code);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,
					Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("===========绑定邮箱错误===========" );
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 
	 * @Title: modifiPwdByPhoneOrEmail
	 * @auther Tangli
	 * @Description:通过邮箱或手机验证码修改密码
	 * @param userId
	 * @param email
	 * @param phone
	 * @param code
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("modifiPwdByPhoneOrEmail")
	public @ResponseBody ResponseContainer modifiPwdByPhoneOrEmail(String ep,String pwd) {
		try {
			userService.modifiPwdByPhoneOrEmail(ep,pwd);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,
					Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("===========修改密码发送错误===========" );
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		}
	}
	
	@RequestMapping("checkPhoneOrEmailCode")
	public @ResponseBody ResponseContainer checkPhoneOrEmailCode(String ep,String code,int type) {
		try {
			boolean res=userService.checkPhoneOrEmailCode(ep,code,type);
			return addResponse(Config.STATUS_200, Config.MSG_200, res,
					Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		}
	}
	
	
	
	/**
	 * 将email注册校验调整为通过
	 * @param request
	 * @return
	 */
	@RequestMapping("changeEmailChecked")
	public @ResponseBody ResponseContainer changeEmailChecked(HttpServletRequest request,String groupNumber){
		try {
			String email = request.getParameter("email");
			String appKey=request.getParameter("appKey");
			if(StringUtil.isEmpty(email) || !StringUtil.isEmailAddr(email)){
				throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOTEMAIL_201,null);
			}
			User user = userService.changeEmailChecked(email,appKey);
			if(!StringUtil.isEmpty(groupNumber)){
				try{
				userService.joinGroup(groupNumber, user);
				}catch(Exception e){
					
				}
			}
			return addResponse(Config.STATUS_200, Config.MSG_CHECKED_200, user,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("===========业务错误，email激活失败==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("===========未知错误，email激活失败==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	@RequestMapping("getUserByToken")
	public ResponseContainer getUserByToken(String token){
		try {
			User user=getCurrentUser(token);
			user.setToken(token);
			return addResponse(Config.STATUS_200, Config.MSG_200, user, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {		
			return addResponse(Config.STATUS_200, e.getMessage(), null,Config.RESP_MODE_10, "");
		} catch (Exception e){
			return addResponse(Config.STATUS_200, Config.MSG_505, null,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 检验是否设置密码
	 * @param request
	 * @return
	 */
	@RequestMapping("checkIsSetPassword")
	public @ResponseBody ResponseContainer checkIsSetPassword(HttpServletRequest request){
		try {
			String token = request.getParameter("token");
			User currentUser=this.getCurrentUser(token);
			return addResponse(Config.STATUS_200, Config.MSG_200, userService.checkIsSetPassWord(currentUser.getId()),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("===========业务错误，检验是否设置密码失败==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("===========未知错误，检验是否设置密码失败==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 *  判断是否有其他用户绑定过此手机号，如果有则返回此用户，如果没有则返回空
	 * @param request
	 * @return
	 */
	@RequestMapping("isHasUserSamplePhone")
	public @ResponseBody ResponseContainer isHasUserSamplePhone(HttpServletRequest request){
		try {
			String token = request.getParameter("token");
			String phoneNumber = request.getParameter("phoneNumber");
			User currentUser=this.getCurrentUser(token);
			return addResponse(Config.STATUS_200, Config.MSG_200, userService.isHasUserSamplePhone(phoneNumber, currentUser.getId()),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("===========业务错误，判断是否有其他用户绑定过此手机号，如果有则返回此用户，如果没有则返回空失败==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("===========未知错误，判断是否有其他用户绑定过此手机号，如果有则返回此用户，如果没有则返回空败==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 合并账户
	 * @param request
	 * @return
	 */
	@RequestMapping("mergeUserAccount")
	public @ResponseBody ResponseContainer mergeUserAccount(HttpServletRequest request){
		try {
			String token = request.getParameter("token");
			String fromUserId=request.getParameter("fromUserId");
			User currentUser=this.getCurrentUser(token);
			userService.mergeUserAccount(fromUserId, currentUser);
			return addResponse(Config.STATUS_200, Config.MSG_TOADMIN_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("===========业务错误，合并账户失败==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("===========未知错误，合并账户失败==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 为当前用户绑定手机号
	 * @param request
	 * @return
	 */
	@RequestMapping("boundPhoneNumber")
	public @ResponseBody ResponseContainer boundPhoneNumber(HttpServletRequest request){
		try {
			String token = request.getParameter("token");
			String phoneNumber = request.getParameter("phoneNumber");
			String passWord = request.getParameter("passWord");
			if(StringUtil.isEmpty(phoneNumber) || !StringUtil.isMobileNO(phoneNumber)){
				throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOTMOBILE_201,null);
			}
			User user=this.getCurrentUser(token);
			User boundUser=userService.boundPhoneNumber(user, phoneNumber,passWord);
			return addResponse(Config.STATUS_200, Config.MSG_TOADMIN_200, boundUser,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("===========业务错误，为当前用户绑定手机号失败==========="+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("===========未知错误，为当前用户绑定手机号失败==========="+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 获取昵称
	 * @param request
	 * @return
	 */
	@RequestMapping("getNickName")
	public @ResponseBody ResponseContainer getNickName(HttpServletRequest request){
		try {
			return addResponse(Config.STATUS_200, Config.MSG_200,userService.getNickName(),Config.RESP_MODE_10, "");
		}catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}

	@RequestMapping("getMyCreated")
	public @ResponseBody ResponseContainer getMyCreated(HttpServletRequest request,String token,QueryModel dm) throws XueWenServiceException{
		User user=getCurrentUser(token);
		Map<String, Object> res=userService.getMyCreated(user.getId(), dm);
		return addResponse(Config.STATUS_200, Config.MSG_200,res,Config.RESP_MODE_10, "");
	}
	
	
}
