package operation.service.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.sf.json.JSONObject;
import operation.exception.XueWenServiceException;
import operation.pojo.black.Black;
import operation.pojo.course.Knowledge;
import operation.pojo.course.NewCourse;
import operation.pojo.course.NewGroupCourse;
import operation.pojo.drycargo.Drycargo;
import operation.pojo.email.MailCode;
import operation.pojo.fav.Fav;
import operation.pojo.group.XueWenGroup;
import operation.pojo.log.UserLoginLog;
import operation.pojo.pub.QueryModel;
import operation.pojo.tags.TagBean;
import operation.pojo.tags.UserTagBean;
import operation.pojo.topics.Topic;
import operation.pojo.user.MsgCode;
import operation.pojo.user.NewUserNickName;
import operation.pojo.user.OpenFireUser;
import operation.pojo.user.ResponseUser;
import operation.pojo.user.User;
import operation.pojo.user.UserHead;
import operation.pojo.user.UserMessage;
import operation.repo.group.GroupTemplate;
import operation.repo.user.ContactAdressRepository;
import operation.repo.user.MsgCodeRepository;
import operation.repo.user.NewUserNameRepository;
import operation.repo.user.NewUserNickNameRepository;
import operation.repo.user.UserHeadRepository;
import operation.repo.user.UserMessageRepository;
import operation.repo.user.UserRepository;
import operation.repo.user.UserTemplate;
import operation.service.black.BlackService;
import operation.service.course.KnowledgeService;
import operation.service.course.LessonService;
import operation.service.course.NewCourseService;
import operation.service.course.NewGroupCourseService;
import operation.service.course.UserGroupCourseService;
import operation.service.drycargo.DrycargoService;
import operation.service.email.EmailService;
import operation.service.email.MailCodeService;
import operation.service.fav.FavService;
import operation.service.file.MyFileService;
import operation.service.group.GroupService;
import operation.service.jobs.IndustryService;
import operation.service.log.UserLoginLogService;
import operation.service.log.UserRegistLogService;
import operation.service.queue.QueueService;
import operation.service.redis.OnlineUserRedisService;
import operation.service.sms.SmsService;
import operation.service.tags.TagService;
import operation.service.topics.PostService;
import operation.service.topics.TopicService;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import tools.Config;
import tools.ListComparator;
import tools.MD5Util;
import tools.PageRequestTools;
import tools.RestfulTemplateUtil;
import tools.StringUtil;
import tools.YXTJSONHelper;

@Service
@Component
@EnableScheduling
/**
 * 用户service层，用于接受controller传递的参数
 * 与repo进行数据库操作
 * @author nes
 *
 */
public class UserService {

	private static final Logger logger = Logger.getLogger(UserService.class);
	@Autowired
	public UserRepository userRepo;
	
	@Autowired
	public NewUserNameRepository newUserNameRepository;
	
	@Autowired
	public NewUserNickNameRepository newUserNickNameRepository;

	@Autowired
	public UserMessageRepository userMessageRepository;

	@Autowired
	MongoTemplate template;

	@Autowired
	public QueueService queueService;

	@Autowired
	public UserLoginLogService userLoginLogService;

	@Autowired
	public MsgCodeRepository msgCodeRepository;

	@Autowired
	public IndustryService industryService;

	@Autowired
	public UserJobsService userJobsService;

	@Autowired
	public MyFileService myFileService;

	@Autowired
	public UserInviteService userInviteService;

	@Autowired
	public GroupService groupService;

	@Autowired
	public ContactAdressRepository contactAdressRepository;

	@Autowired
	private TagService tagService;

	@Autowired
	private UserTemplate userTemplate;

	@Autowired
	private UserNumService userNumService;

	@Autowired
	private UserNameService userNameService;
	@Autowired
	private NewUserNameService newUserNameService;

	@Autowired
	private UserContactListService userContactListService;
	@Autowired
	private ContactUserService contactUserService;
	@Autowired
	private FavService favService;
	@Autowired
	private NewCourseService newCourseService;
	@Autowired
	private TopicService topservice;
	@Autowired
	private KnowledgeService knoeledgeService;
	@Autowired
	private DrycargoService drycarGoService;
	@Autowired
	private UserHeadRepository userHeadRepository;
	@Autowired
	private BlackService blackService;

	@Autowired
	private EmailService emailService;
	@Autowired
	private MailCodeService mailCodeService;
	@Autowired
	private SmsService smsService;
	@Autowired
	private OnlineUserRedisService onlineUserRedisService;
	@Autowired
	private UserRegistLogService userRegistLogService;
	@Autowired
	private LessonService lessonService;
	@Autowired
	private PostService postService;
	@Autowired
	private TopicService topicService;
	@Autowired
	private NewGroupCourseService newGroupCourseService;
	@Autowired
	private UserGroupCourseService userGroupCourseService;

	
	@Autowired
	private GroupTemplate groupTemplate;

	@Value("${tag.service.url}")
	private String tagServiceUrl;
	
	@Value("${openfire.lockUrl.url}")
	private String openfireLockUrlUrl;
	
	@Value("${openfire.createUser.url}")
	private String openfireCreateUserUrl;
	
	@Value("${ztiao.pay.url}")
	private String ztiaoPayUrl;

	// @Value("${user.regist.email.url}")
	// private String emailUrl;
	// @Value("${image.default.url}")
	// private String imagDefaultUrl;

	// /**
	// * 注册用户
	// *
	// * @param subject
	// * @return
	// */
	// public User regist(User user,String lat,String lng,String robot) throws
	// XueWenServiceException {
	// //增加黑名单认证
	// Black black = blackService.getBlackByUser(user.getUserName());
	// if(black!=null){
	// throw new XueWenServiceException(Config.STATUS_201,
	// Config.MSG_REGISTFAIL_201,null);
	// }
	// if (!userTemplate.isExiseByUserName(user.getUserName())) {
	// //地理位置
	// if(null !=lat && null != lng){
	// double [] position = new
	// double[]{Double.parseDouble(lng),Double.parseDouble(lat)};
	// user.setLng(Double.parseDouble(lng));
	// user.setLat(Double.parseDouble(lat));
	// user.setLocation(position);
	// }
	// if(StringUtil.isBlank(robot)){
	// user.setRobot(0);
	// }
	// long time=System.currentTimeMillis();
	// user.setCtime(time);// 获取系统时间戳
	// user.setUtime(time);
	// //20140911增加创建用户时，增加用户号
	// String userNumber = userNumService.getGroupNum();
	// logger.info("获取用户号码成功====================="+userNumber);
	// user.setUserNumber(Long.parseLong(userNumber));
	// user.setNickName(getNickName());
	// //随机获取用户头像
	// String userLogoUrl = this.userLogoUrl();
	// logger.info("获取用户logourl成功====================="+userLogoUrl);
	// user.setLogoURL(userLogoUrl);
	// user.setPhoneNumber(user.getUserName());
	// // 注册成功后，将注册用户信息同步到openFire服务端
	// OpenFireUser ofu = this.getOpenFireUser(user);
	// user.setOpenFireUser(ofu);
	// //创建openfire用户
	// OpenFireUser openFireUser = openFireUserService.create(ofu);
	// logger.info("创建openfire用户成功====================="+openFireUser.getOpenFireUserName());
	// openFireUser.setServerList(ofu.getServerList());
	// user.setOpenFireUser(openFireUser);
	// user=userRepo.save(user);
	// logger.info("用户注册成功=============================="+user.getId());
	// //用户注册后修改通讯录列表信息
	// try {
	// userContactListService.updateContactAdress(user.getUserName(),
	// user.getId());
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return user;
	// } else {
	// throw new XueWenServiceException(Config.STATUS_400, Config.MSG_400,null);
	// }
	// }
	/**
	 * 注册用户
	 * 
	 * @param subject
	 * @return
	 */
	public User regist(User user, String lat, String lng, String robot, String url, String appkey) throws XueWenServiceException {
		// 增加黑名单认证
		Black black = blackService.getBlackByUser(user.getUserName());
		if (black != null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_REGISTFAIL_BLACK_201, null);
		}
		// 如果注册类型为空则默认为手机号注册
		if (StringUtil.isBlank(user.getRegistType())) {
			user.setRegistType(Config.USER_REGIST_PHONE);
		}
		if (!StringUtil.verdictRegistUserName(user.getUserName()).equals(user.getRegistType())) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_REGISTTYPE_201, null);
		}

		// 获得基础用户数据
		user = this.getBaseUser(user, lat, lng, robot);
		// 如果注册类型为手机号
		if (Config.USER_REGIST_PHONE.equals(user.getRegistType())) {
			user = this.registByPhone(user, lat, lng, robot);
		}// 如果注册类型为email
		else if (Config.USER_REGIST_EMAIL.equals(user.getRegistType())) {
			user = this.registByEmail(user, lat, lng, robot, url);
		}

		// 创建openfire用户
		OpenFireUser ofu = creatOpenFireUser(String.valueOf(user.getUserNumber()),
				user.getPassWord(),user.getOpenId(),user.getEmail());
		user.setOpenFireUser(ofu);
		user = userRepo.save(user);
		// 如果注册类型为手机号
		if (!Config.USER_REGIST_EMAIL.equals(user.getRegistType())) {
			try {
				userContactListService.updateContactAdress(user.getUserName(), user.getId());
				user.setLogintime(System.currentTimeMillis());
				user = this.login(user, appkey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.info("用户注册成功==============================" + user.getId());
		return user;
	}
	/**
	 * 创建openfire用户
	 * @param userNumber
	 * @param passWord
	 * @param openId
	 * @param email
	 * @return
	 * @throws XueWenServiceException
	 */
	public OpenFireUser creatOpenFireUser(String userNumber,String passWord, String openId, String email )throws XueWenServiceException{
		Map<String,String> map=new HashMap<String, String>();
		map.put("userNumber", userNumber);
		map.put("passWord", passWord);
		map.put("openId", openId);
		map.put("email", email);
		RestfulTemplateUtil restfulTemplateUtil=new RestfulTemplateUtil();
		JSONObject obj=restfulTemplateUtil.getRestApiData(openfireCreateUserUrl, map);
		JSONObject data=obj.getJSONObject("data");
		Object status=obj.get("status");
		Object msg=obj.get("msg");
//		ResponseContainer rc=(ResponseContainer)JSONObject.toBean(obj, ResponseContainer.class);
		if("200".equals(status.toString())){
			JSONObject rs=data.getJSONObject("result");
			return (OpenFireUser)JSONObject.toBean(rs, OpenFireUser.class);
		}else{
			throw new XueWenServiceException(Integer.valueOf(status.toString()),msg.toString() ,null);
		}
	}

	/**
	 * 注册用户
	 * 
	 * @param subject
	 * @return
	 */
	public User ossregist(User user) throws XueWenServiceException {
		// 20140911增加创建用户时，增加用户号
		String userNumber = userNumService.getGroupNum();
		logger.info("获取用户号码成功=====================" + userNumber);
		user.setUserNumber(Long.parseLong(userNumber));
		if (StringUtil.isBlank(user.getNickName())) {
			user.setNickName(getNickName());
		}
		if (StringUtil.isBlank(user.getLogoURL())) {
			// 随机获取用户头像
			String userLogoUrl = this.userLogoUrl();
			logger.info("获取用户logourl成功=====================" + userLogoUrl);
			user.setLogoURL(userLogoUrl);
		}
		long l=System.currentTimeMillis();
		user.setCtime(l);
		user = userRepo.save(user);

		logger.info("用户注册成功==============================" + user.getId());
		return user;
	}

	/**
	 * 注册用户未激活
	 * 
	 * @param subject
	 * @return
	 */
	public User registUncheck(String serverName, User user, String lat, String lng, String robot, String url, String appkey)
			throws XueWenServiceException {
		User usertemp = regist(user, lat, lng, robot, url, appkey);
		usertemp.setEmailChecked(false);
		save(usertemp);
		if(Config.USER_REGIST_EMAIL.equals(usertemp.getRegistType())){
			sendJHemail(user.getEmail(), serverName,"");
		}
		return usertemp;
	}

	/**
	 * 获得用户的登录信息
	 * 
	 * @return
	 */
	public User checkLogin(User user) throws XueWenServiceException {
		User userResult = userRepo.findOneByUdidAndToken(user.getUdid(), user.getToken());
		if (null != userResult) {
			return userResult;
		} else {
			throw new XueWenServiceException(Config.STATUS_203, Config.MSG_203, null);
		}

	}

	/**
	 * 根据用户ID查询用户信息
	 * 
	 * @return
	 */
	public User findUser(String id) throws XueWenServiceException {
		User userResult = userRepo.findOneById(id);
		if (null != userResult) {
			// 获得用户标签
			RestTemplate restTemplate = new RestTemplate();
			String tag = restTemplate.getForObject(tagServiceUrl + "tag/getTagsByIdAndType?domain=" + Config.YXTDOMAIN + "&itemId=" + id + "&itemType="
					+ Config.TAG_TYPE_USER, String.class);
			JSONObject objj = JSONObject.fromObject(tag);
			JSONObject obss = objj.getJSONObject("data");
			net.sf.json.JSONArray childs = obss.getJSONArray("result");
			logger.info("获取个人标签信息==============================" + childs);
			userResult.setScoreSum(childs.size());
			logger.info("获取个人标签数量==============================" + childs.size());
			userResult.setUserTageName(childs);
			return userResult;
		} else {
			throw new XueWenServiceException(Config.STATUS_404, Config.MSG_USERNOTFIND_201, null);
		}
	}

	/**
	 * 用户登录
	 * 
	 * @param subject
	 * @return
	 */
	// @SuppressWarnings("unchecked")
	// public User login(User user) throws XueWenServiceException {
	// // 通过用户名查询该用户
	// logger.info("======================查询用户");
	// if (!userTemplate.isExiseByUserName(user.getUserName())) {
	// throw new XueWenServiceException(Config.STATUS_201,
	// Config.MSG_NOREGIST_201,null);
	// }
	// User one =
	// userRepo.findOneByUserNameAndPassWord(user.getUserName(),user.getPassWord());
	// if (one != null) {
	// //该用户已经被删除
	// if(one.getDeleFlag()==1){
	// throw new XueWenServiceException(Config.STATUS_201,
	// Config.MSG_NOLOGIN_201,null);
	// }
	// logger.info("==================用户查询成功=========");
	// if(one.getToken()!=null){
	// Config.map.remove(one.getToken());
	// }
	// long time = System.currentTimeMillis();
	// one.setLogintime(time);
	// one.setExpireTime(time + 86400000 * 365);
	// one.setToken(MD5Util.MD5(one.getUdid() + one.getId()+
	// String.valueOf(one.getLogintime())));
	// if (user.getUdid()==null) {
	// } else {
	// one.setUdid(user.getUdid());
	// }
	// logger.info("==================更新用户信息========="+one.getToken()+"===="+one.getUdid());
	// one=userRepo.save(one);
	// logger.info("==================更新用户信息成功,放入数据后=========token："+one.getToken()+"-===udid:"+one.getUdid());
	// Config.map.put(one.getToken(), one);
	// logger.info("==================登陆成功后，将登陆用户写入登陆日志表");
	// UserLoginLog ull = new UserLoginLog();
	// ull.setLoginTime(time);
	// ull.setUserId(one.getId());
	// userLoginLogService.saveUserLoginLog(ull);
	// } else {
	// throw new XueWenServiceException(Config.STATUS_404, Config.MSG_404,null);
	// }
	// return one;
	// }
	/**
	 * 用户登录
	 * 
	 * @param subject
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public User login(User user, String appkey) throws XueWenServiceException {
		// 判断登陆用户是否合法
		if (StringUtil.isBlank(user.getRegistType())) {
			user.setRegistType("01");
		}
		if (!StringUtil.verdictRegistUserName(user.getUserName()).equals(user.getRegistType())) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_REGISTFAIL_201, null);
		}
		// 如果手机号登陆
		if (Config.USER_REGIST_PHONE.equals(user.getRegistType())) {
			user = this.loginByPhone(user, appkey);
		}// 如果email登陆
		else if (Config.USER_REGIST_EMAIL.equals(user.getRegistType())) {
			user = this.loginByEmail(user, appkey);
		}
		return user;
	}

	/**
	 * 用户退出
	 * 
	 * @param subject
	 * @return
	 */
	public boolean loginOut(User user, String token, String appKey) throws XueWenServiceException {
		if (Config.APPKEY_PC.equals(appKey)) {
			onlineUserRedisService.removeOnlineUser(token);
			return true;
		} else {
			// 通过用户名查询该用户
			User one = userRepo.findOneById(user.getId());
			if (one != null) {
				onlineUserRedisService.removeOnlineUser(one.getToken());
				one.setToken("");
				if (userRepo.save(one) != null) {
					return true;
				} else {
					throw new XueWenServiceException(Config.STATUS_202, Config.MSG_202, null);
				}
			} else {
				throw new XueWenServiceException(Config.STATUS_404, Config.MSG_404, null);
			}
		}
	}

	/**
	 * 更新用户信息
	 * 
	 * @return
	 */
	public User updateUser(User user, String interest, String special, String industry) throws XueWenServiceException {
		User findUser = userRepo.findOneById(user.getId());
		if (null == findUser) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_UPDATENOUSER_201, null);
		} else {
			Set<String> interSet = new HashSet<String>();
			if (interest != null) {
				JSONArray ja = (JSONArray) JSONValue.parse(interest);
				if (ja != null) {
					if (ja.size() > 0) {
						for (int i = 0; i < ja.size(); i++) {
							interSet.add(ja.get(i).toString());
						}
					}
				}
				user.setInterest(interSet);
			}
			Set<String> specialSet = new HashSet<String>();
			if (special != null) {
				JSONArray ja = (JSONArray) JSONValue.parse(special);
				if (ja != null) {
					if (ja.size() > 0) {
						for (int i = 0; i < ja.size(); i++) {
							specialSet.add(ja.get(i).toString());
						}
					}
				}
				user.setSpecial(specialSet);
			}
			Set<String> industrySet = new HashSet<String>();
			if (industry != null) {
				JSONArray ja = (JSONArray) JSONValue.parse(industry);
				if (ja != null) {
					if (ja.size() > 0) {
						for (int i = 0; i < ja.size(); i++) {
							industrySet.add(ja.get(i).toString());
						}
					}
				}
				user.setIndustry(industrySet);
			}
			user.setUtime(System.currentTimeMillis());
			user = userRepo.save(user);
			// 更新在线用户信息
			onlineUserRedisService.addOrUpdateOnlineUser(user.getId(), user);
		}
		return user;
	}

	/**
	 * 根据UDID、TOKEN获得当前用户
	 * 
	 * @param token
	 * @param udid
	 * @return
	 */
	public User getCurrentUser(String token, String udid) {
		return userRepo.findOneByUdidAndToken(udid, token);
	}
	
	
	/**
	 * 根据PhoneNumber获得当前用户
	 * 
	 * @param token
	 * @param udid
	 * @return
	 */
	public User findOneByPhoneNumber(String udid) {
		return userRepo.findOneByPhoneNumber(udid);
	}

	/**
	 * 保存用户推送消息列表
	 * 
	 * @param userMessage
	 * @return
	 * @throws XueWenServiceException
	 */
	public UserMessage saveUserMessage(UserMessage userMessage) throws XueWenServiceException {
		UserMessage userMessageResult = userMessageRepository.save(userMessage);
		if (userMessageResult == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_UPDATE_201, null);
		}
		return userMessageResult;
	}

	/**
	 * 通过群消息ID查询用户消息
	 * 
	 * @param messageGroupId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<UserMessage> findUserMessagesByMessageGroupId(String messageGroupId) throws XueWenServiceException {
		List<UserMessage> userMessages = userMessageRepository.findByMessageGroupId(messageGroupId);
		if (userMessages == null || userMessages.size() <= 0) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_UPDATE_201, null);
		}
		return userMessages;
	}

	/**
	 * 根据用户ID 查询用户推送列表信息
	 * 
	 * @param user
	 * @param page
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<UserMessage> findUserMessage(User user, Pageable page) throws XueWenServiceException {
		if (user == null) {
			throw new XueWenServiceException(Config.STATUS_404, Config.MSG_404, null);
		}
		Page<UserMessage> userMessageList = userMessageRepository.findAllByUserId(user.getId(), page);
		if (userMessageList.getTotalElements() <= 0) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
		}
		return userMessageList;
	}

	/**
	 * 将用户消息id组装成List
	 * 
	 * @param messages
	 * @return
	 */
	public List<Object> getUserMessageIdList(List<UserMessage> messages) {
		List<Object> ids = new ArrayList<Object>();
		if (messages != null) {
			for (UserMessage um : messages) {
				ids.add(um.getId());
			}
		}
		return ids;
	}

	/**
	 * 将所有消息置为已读
	 * 
	 * @param messages
	 */
	public void updateAllRead(List<UserMessage> messages) {
		List<Object> ids = getUserMessageIdList(messages);
		if (ids != null) {
			List<UserMessage> ums = userMessageRepository.findByIdIn(ids);
			for (UserMessage um : ums) {
				if (um.getIsRead().equals("0")) {
					um.setIsRead("1");
					userMessageRepository.save(um);
				}
			}
		}
	}

	/**
	 * 根据用户消息ID 查询用户推送信息详情
	 * 
	 * @param userMessageId
	 * @return
	 * @throws XueWenServiceException
	 */
	public UserMessage findOneUserMessage(String userMessageId) throws XueWenServiceException {
		UserMessage userMessage = userMessageRepository.findOneById(userMessageId);
		if (userMessage == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
		}
		userMessage.setIsRead("1");
		userMessageRepository.save(userMessage);
		return userMessage;
	}

	/**
	 * 删除用户消息
	 * 
	 * @param userMessageId
	 * @throws XueWenServiceException
	 */
	public void deleteOneUserMessage(String userMessageId) throws XueWenServiceException {
		if (StringUtil.isBlank(userMessageId)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
		}
		userMessageRepository.delete(userMessageId);
	}

	/**
	 * 初始化 建立2d位置索引
	 * 
	 * @param racBeinfo
	 */
	public void creatTable() {
		template.indexOps(User.class).ensureIndex(new GeospatialIndex("location"));
	}

	/**
	 * 将用户转换成前端Response
	 * 
	 * @param user
	 * @return
	 */
	public List<ResponseUser> changeUserToResponseUser(List<User> user) {
		List<ResponseUser> rps = new ArrayList<ResponseUser>();
		for (int i = 0; i < user.size(); i++) {
			ResponseUser rp = new ResponseUser(user.get(i));
			rps.add(rp);
		}
		return rps;

	}

	/**
	 * 发送短信验证码
	 * 
	 * @param phoneNumber
	 */
	public void sendSms(String phoneNumber) throws XueWenServiceException {
		long time = System.currentTimeMillis();
		if (StringUtil.isBlank(phoneNumber)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOTMOBILE_201, null);
		}
		// 验证此用户发送短信验证码是否超过一分钟
		MsgCode msg = msgCodeRepository.findOneByPhoneNumAndUtimeGreaterThan(phoneNumber, time - (1000 * 60));
		if (msg == null) {
			// 生成短信验证码
			int code = nextInt(100000, 999999);
			msg = msgCodeRepository.findOneByPhoneNum(phoneNumber);
			if (msg == null) {
				msg = new MsgCode();
				msg.setPhoneNum(phoneNumber);
				msg.setCode(String.valueOf(code));
				msg.setCheckTime(0);
			} else {
				msg.setUtime();
				msg.setCheckTime(0);
				msg.setCode(String.valueOf(code));
			}
			// 调用短信接口
			toSend(phoneNumber, String.valueOf(code), "1");
			msgCodeRepository.save(msg);
		} else {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_FREQUENT_201, null);
		}
		// 发送短信验证码
	}

	/**
	 * 获取固定区间的随机数
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	private int nextInt(final int min, final int max) {

		int tmp = Math.abs(new Random().nextInt());
		return tmp % (max - min + 1) + min;
	}

	/**
	 * 发送短信
	 * 
	 * @param phoneNum
	 * @param code
	 * @param type
	 * @throws XueWenServiceException
	 */
	public void toSend(String phoneNum, String code, String type) throws XueWenServiceException {
		try {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String dateStr = df.format(new Date());
			logger.info(dateStr);
			long time = (df.parse(dateStr).getTime()) / 1000;
			String dt = String.valueOf(time);
			String user = "yunxuetang";
			String pwd = MD5Util.MD5(user + "suzhouyxt" + dt).toLowerCase();
			String params = "";
			if ("1".equals(type)) {
				code = "【云学堂】校验码" + code + ",有学有问,祝您云学堂学习之旅愉快！";
				params = "username=" + user + "&pwd=" + pwd + "&dt=" + dt + "&msg=" + code + "&mobiles=" + phoneNum + "&code=4110";
			} else {
				code = "【云学堂】" + code;
				params = "username=" + user + "&pwd=" + pwd + "&dt=" + dt + "&mobiles=" + phoneNum + "&code=999" + "&msg="
						+ URLDecoder.decode(code, "UTF-8");
			}
			String url = "http://sms.ensms.com:8080/sendsms/?" + params;
			logger.info("pwd====" + pwd + "========url:" + url);
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			String str = "";
			if (entity != null) {
				InputStream instreams = entity.getContent();
				str = convertStreamToString(instreams);
				System.out.println("Do something");
				System.out.println(str);
			}
			if (!str.replaceAll("\r|\n", "").equals("0")) {
				throw new XueWenServiceException(Config.STATUS_201, Config.MSG_SENDMSGERROR_201, null);
			}
		} catch (Exception e) {
			logger.error(e);
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_SENDMSGERROR_201, null);
		}
	}

	/**
	 * 将流转换成字符串
	 * 
	 * @param is
	 * @return
	 */
	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * 验证短息码
	 * 
	 * @param phoneNumber
	 * @param smsCode
	 */
	public boolean checkSms(String phoneNumber, String smsCode) throws XueWenServiceException {
		MsgCode msgCode = msgCodeRepository.findOneByPhoneNum(phoneNumber);
		if (msgCode == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_ERRORCODE_201, null);
		}
		if (smsCode.equals(msgCode.getCode())) {
			msgCodeRepository.delete(msgCode);
			return true;
		} else {
			if ((msgCode.getCheckTime() + 1) == 5) {
				msgCodeRepository.delete(msgCode.getId());
				throw new XueWenServiceException(Config.STATUS_201, Config.MSG_ERRORTOMUCH_201, null);
			} else {
				msgCode.setCheckTime(msgCode.getCheckTime() + 1);
				msgCodeRepository.save(msgCode);
			}
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_ERRORCODE_201, null);
		}
	}

	/**
	 * 重置密码
	 * 
	 * @param passWrod
	 * @param confirmPassWrod
	 * @param udid
	 * @param phoneNumber
	 */
	public boolean resetPassword(String passWord, String phoneNumber) throws XueWenServiceException {
		logger.info("重置密码的手机号为==============" + phoneNumber);
		User user = userRepo.findOneByPhoneNumberAndIsPhoneChecked(phoneNumber, true);
		if (null == user) {
			List<User> users = userRepo.findByphoneNumber(phoneNumber);
			if(users == null || users.size()==0){
				throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOUSER_201, null);
			}else{
				user=users.get(0);
			}
		}
		user.setPassWord(passWord);
		user.setPhoneChecked(true);
		userRepo.save(user);
		return true;
	}

	/**
	 * 重置密码 运维系统调用
	 * 
	 * @param passWrod
	 * @param confirmPassWrod
	 * @param udid
	 * @param phoneNumber
	 */
	public boolean resetPasswordbyBackSystem(String passWord, String id) throws XueWenServiceException {
		User user = userRepo.findOneById(id);
		if (user == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOUSER_201, null);
		}
		user.setPassWord(passWord);
		userRepo.save(user);
		return true;
	}

	/**
	 * 根据用户ID返回响应用户数据
	 * 
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public ResponseUser toResponseUser(String userId) throws XueWenServiceException {
		if (userId == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
		} else {
			User user = userRepo.findOneById(userId.toString());
			if (user == null) {
				throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
			} else {
				return new ResponseUser(user);
			}
		}
	}

	/**
	 * 根据用户ID的List返回响应用户数据的List列表
	 * 
	 * @param userIds
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<ResponseUser> toResponseUserList(List<Object> userIds) throws XueWenServiceException {
		if (userIds == null || userIds.size() == 0) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
		} else {
			List<User> users = userRepo.findByIdIn(userIds);
			List<ResponseUser> rspUsers = new ArrayList<ResponseUser>();
			for (User user : users) {
				rspUsers.add(new ResponseUser(user));
			}
			return rspUsers;
		}
	}

	/**
	 * 修改密码
	 * 
	 * @param passWrod
	 * @param confirmPassWrod
	 * @param udid
	 * @param phoneNumber
	 */
	public boolean changePass(String passWord, String phoneNumber) throws XueWenServiceException {
		//User user = userRepo.findOneByUdidAndPassWord(udid, passWord);
		boolean result = userTemplate.isExitByPhoneNumberForPassword(phoneNumber,passWord);
		if (result == false) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_PASSWORDERROR_201, null);
		} else {
			return true;
		}
	}

	/**
	 * 通过用户名查询用户
	 * 
	 * @param userName
	 * @return
	 */
	public User getUser(String userName) throws XueWenServiceException {
		return userRepo.findOneByUserName(userName);
	}

	/**
	 * 通过用户ID查找用户，获取用户数据库存储的基础信息
	 * 
	 * @author hjn
	 * @param id
	 * @return
	 * @throws XueWenServiceException
	 */
	public User getOneNomailUserById(String id) throws XueWenServiceException {
		return userRepo.findOne(id);
	}

	/**
	 * 获取所有用户
	 * 
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<User> findAllUsers() throws XueWenServiceException {
		return userRepo.findAll();
	}

	/**
	 * 得到用户总数量
	 * 
	 * @return
	 * @throws XueWenServiceException
	 */
	public int countUsers() throws XueWenServiceException {
		return (int) userRepo.count();
	}

	/**
	 * 根据用户名判断是否为注册用户
	 */
	public boolean isRegistUserByPhoneNumber(String phoneNumber) throws XueWenServiceException {
		return userTemplate.isExiseByPhoneNumber(phoneNumber);
	}

	public User findShareUser(String id) throws XueWenServiceException {
		User userResult = userRepo.findOneById(id);
		return userResult;
	}

	/**
	 * 给用户打标签
	 * 
	 * @param user
	 * @param userId
	 * @param tagName
	 */
	public void tagUser(User user, String userId, String tagName) throws XueWenServiceException {
		if (tagName != null) {
			JSONArray ja = (JSONArray) JSONValue.parse(tagName);
			if (ja != null) {
				if (ja.size() > 0) {
					for (int i = 0; i < ja.size(); i++) {
						tagService.saveUserTag(createUserTag(user, userId, ja.get(i).toString()));
						tagService.saveUserTagToRedis(createUserTag(user, userId, ja.get(i).toString()));
					}
				}
			}
		}

	}

	/**
	 * 创建话题标签
	 * 
	 * @param tagName
	 * @return
	 */
	public UserTagBean createUserTag(User user, String userId, String tagName) {
		UserTagBean utb = new UserTagBean();
		utb.setUserId(user.getId());
		utb.setUserName(user.getUserName());
		utb.setItemId(userId);
		utb.setItemType(Config.TAG_TYPE_USER);
		utb.setCtime(String.valueOf(System.currentTimeMillis()));
		utb.setTagName(tagName);
		return utb;

	}

	/**
	 * 根据用户ID返回联系人列表所需的用户信息，包括ID，userName,nickName,sex,phoneNumber,email,intro等，
	 * 其他字段为空
	 * 
	 * @author hjn
	 * @return
	 * @throws XueWenServiceException
	 */
	public User getContactOfRspUserById(String id) throws XueWenServiceException {
		return userTemplate.getContactOfRspUserById(id);
	}

	/**
	 * 删除用户
	 */
	public boolean deleteUserById(String id) throws XueWenServiceException {
		return userTemplate.deleUserById(id);
	}

	/**
	 * 通过用户名查询用户
	 * 
	 * @param userName
	 * @return
	 */
	public User getOneByUserId(String userId) throws XueWenServiceException {
		return userRepo.findOneById(userId);
	}

	/**
	 * 
	 * @author yangquanliang
	 * @Description: 修改原方法 改为从数据库随机读取头像
	 * @param @return
	 * @return String
	 * @throws
	 */
	public String userLogoUrl() {
		List<UserHead> listHead = userHeadRepository.findAll();
		String userLogoUrl = listHead.get(StringUtil.getOneInt(listHead.size())).getUrl();
		return userLogoUrl;
	}

	/**
	 * 模糊查找用户
	 * 
	 * @param keywords
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<User> search(String keywords, Pageable pageable) throws XueWenServiceException {
		if (StringUtil.isBlank(keywords)) {
			return userRepo.findAllByRobotAndDeleFlag(0,0, pageable);
		} else {
			keywords = ".*?(?i)" + keywords + ".*";
			return userRepo.findByRobotAndUserNameRegexOrRobotAndNickNameRegex(0, keywords, 0, keywords, pageable);
		}
	}

	/**
	 * 模糊查找用户，返回用户Id集合
	 * 
	 * @param keywords
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<String> searchRspOnlyId(String keywords) throws XueWenServiceException {
		keywords = ".*?(?i)" + keywords + ".*";
		List<User> users = userTemplate.searchByNickNameAndIntro(keywords);
		if (users != null && users.size() > 0) {
			List<String> ids = new ArrayList<String>(users.size());
			for (User user : users) {
				ids.add(user.getId());
			}
			return ids;
		} else {
			return null;
		}
	}

	/**
	 * 模糊查找用户
	 * 
	 * @param keywords
	 * @param pageable
	 * @author xurui
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<User> searchbyInfo(String keyword, String robot, String registType, Pageable pageable) {
		Page<User> u=null;
		if (StringUtil.isBlank(keyword) && robot.equals("0")&&StringUtil.isBlank(registType)) {
			u= userRepo.findAllByRobotAndDeleFlag(0, 0,pageable);
		}
		
		if (StringUtil.isBlank(keyword) && robot.equals("0")&&!(StringUtil.isBlank(registType))) {
			u= userRepo.findAllByRobotAndDeleFlagAndRegistType(0, 0,registType,pageable);
		}

		if (StringUtil.isBlank(keyword) && robot.equals("1")) {
			u= userRepo.findAllByRobotAndDeleFlag(1,0, pageable);
		}

		if (robot.equals("0")&&StringUtil.isBlank(registType)&&!(StringUtil.isBlank(keyword))) {
			keyword = ".*?(?i)" + keyword + ".*";
			u= userRepo.findByRobotAndUserNameRegexAndDeleFlagOrRobotAndEmailRegexAndDeleFlagOrRobotAndPhoneNumberRegexAndDeleFlagOrRobotAndNickNameRegexAndDeleFlag(0,keyword, 0, 0, keyword, 0, 0, keyword, 0, 0, keyword, 0, pageable);
		}
		
		if (robot.equals("1")&&StringUtil.isBlank(registType)&&!(StringUtil.isBlank(keyword))){
			keyword = ".*?(?i)" + keyword + ".*";
			Page<User> pageable2 = userRepo.findByRobotAndUserNameRegexAndDeleFlagOrRobotAndEmailRegexAndDeleFlagOrRobotAndPhoneNumberRegexAndDeleFlagOrRobotAndNickNameRegexAndDeleFlag(1,
					keyword, 0, 1, keyword, 0, 1, keyword, 0, 1, keyword, 0, pageable);
			u= pageable2;
		}
		 return u;

	}

	/**
	 * 根据用户ID的List返回响应用户数据的List列表
	 * 
	 * @param userIds
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<ResponseUser> toResponseUser(List<User> users) throws XueWenServiceException {
		if (users == null || users.size() == 0) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
		} else {
			List<ResponseUser> rspUsers = new ArrayList<ResponseUser>();
			for (User user : users) {
				rspUsers.add(new ResponseUser(user));
			}
			return rspUsers;
		}
	}

	/**
	 * 
	 * @Title: findUserPage
	 * @Description: 分页查询所有用户
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException
	 *             Page<User>
	 * @throws
	 */
	public Page<User> findUserPage(Pageable pageable) throws XueWenServiceException {
		Page<User> users = userRepo.findAll(pageable);
		if (users.getTotalElements() < 0) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
		}
		return users;
	}

	/**
	 * 根据用户名返回用户，只返回用户ID
	 * 
	 * @author hjn
	 * @return
	 * @throws XueWenServiceException
	 */
	public User findByUserNameRspOnlyId(String userName) throws XueWenServiceException {
		return userTemplate.findByUserNameRspOnlyId(userName);
	}

	/**
	 * 根据已经校验的手机号查询注册用户的Id
	 * 
	 * @param userName
	 * @return
	 * @throws XueWenServiceException
	 */
	public User findByPhoneAndPhoneCheckedRspOnlyId(String phoneNumber) throws XueWenServiceException {
		return userTemplate.findByPhoneAndPhoneCheckedRspOnlyId(phoneNumber);
	}

	/**
	 * 根据用户ID的List返回响应用户数据的List列表(并得到该人与用户列表的关系)
	 * 
	 * @param userIds
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<ResponseUser> toResponseUserList(List<Object> userIds, String userId) throws XueWenServiceException {
		if (userIds == null || userIds.size() == 0) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
		} else {
			List<User> users = userRepo.findByIdIn(userIds);
			List<ResponseUser> rspUsers = new ArrayList<ResponseUser>();
			for (User user : users) {
				ResponseUser ru = new ResponseUser(user);
				if (user.getId().equals(userId)) {
					ru.setContactStatus(10);
				} else {
					ru.setContactStatus(contactUserService.contact(userId, user.getId()));
				}
				rspUsers.add(ru);
			}
			return rspUsers;
		}
	}

	/**
	 * 根据用户ID的List返回响应用户数据的List列表(并得到该人与用户列表的关系)
	 * 
	 * @param userIds
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<JSONObject> toJsonHelperUserList(List<Object> userIds, String userId) throws XueWenServiceException {
		if (userIds == null || userIds.size() == 0) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
		} else {
			List<User> users = userRepo.findByIdIn(userIds);
			List<JSONObject> rspUsers = new ArrayList<JSONObject>();
			for (User user : users) {
				ResponseUser ru = new ResponseUser(user);
				if (user.getId().equals(userId)) {
					ru.setContactStatus(10);
				} else {
					ru.setContactStatus(contactUserService.contact(userId, user.getId()));
				}
				JSONObject jsonObeject = formateResponseUser(ru);
				rspUsers.add(jsonObeject);
			}
			return rspUsers;
		}
	}

	/**
	 * 格式化ResponseUser对象，只包含返回前端需要属性
	 * 
	 * @param ru
	 * @return
	 * @throws XueWenServiceException
	 */
	public JSONObject formateResponseUser(ResponseUser ru) throws XueWenServiceException {
		// 参数校验
		if (ru == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_PROPERTIESERROR_201, null);
		}
		// 去掉无需返回前端的属性,只包含以下属性
		String[] include = { "id", "nickName", "logoURL", "intro", "contactStatus" };
		return YXTJSONHelper.includeAttrJsonObject(ru, include);

	}

	/**
	 * 
	 * @Title: findByIdIn
	 * @Description: 根据ids找用户list
	 * @param userIds
	 * @return List<User>
	 * @throws
	 */
	public List<User> findByIdIn(List<String> userIds) {
		return userTemplate.findByIdIn(userIds);
	}

	/**
	 * 获得默认昵称
	 * 
	 * @return
	 */
	public String getNickName() {
		long countNickName = newUserNameService.countNickName();
		NewUserNickName  nickName =  newUserNameService.getNickName(StringUtil.getOneInt(new Long(countNickName).intValue()));
		logger.info("获得默认昵称==========================" + nickName.getName());
		return nickName.getName();
	}

	/**
	 * @throws XueWenServiceException
	 * 
	 * @Title: getMyFav
	 * @Description: 获取用户收藏
	 * @param userId
	 * @param pageable
	 * @return Object
	 * @throws
	 */
	public Map<String, Object> getMyFav(String userId, Pageable pageable) throws XueWenServiceException {
		Map<String, Object> res = new HashMap<String, Object>();
		Page<Fav> favs = favService.findByUserId(userId, pageable);
		List<JSONObject> jObjects = new ArrayList<JSONObject>();
		for (Fav fav : favs.getContent()) {
			Map<String, Object> map = new HashMap<String, Object>();
			User user=findOne(fav.getUserId().toString());
			if (Config.TYPE_COURSE_GROUP.equals(fav.getFavType())) {
				NewCourse newCourse = newCourseService.findOne(fav.getSourceId());
				if (newCourse != null) {
					map.put("itemId", newCourse.getId());
					map.put("title", newCourse.getTitle());
					map.put("intro", newCourse.getIntro());
					JSONObject obj = newCourseService.getCourseJson(newCourse, "");
					map.put("groupId", (String) obj.get("groupId"));
					map.put("groupName", (String) obj.get("groupName"));
					map.put("praiseCount", newCourse.getPraiseCount());
					if (user!=null) {
						map.put("logoURL",user.getLogoURL() );
						map.put("uId",user.getId() );
					}
					map.put("coverLogoUrl", newCourse.getLogoUrl());
					map.put("searchType", Config.TYPE_COURSE_GROUP);
					map.put("groupLogoUrl", (String)obj.get("groupLogoUrl"));//增加群图标
					map.put("ctime", fav.getCtime());//增加收藏时间
					map.put("groupCourseId", obj.get("groupLogoUrl"));//群组课程id
					map.put("favCount", newCourse.getFavCount());//收藏数量
					
				}
				else{
					NewGroupCourse newGroupCourse = newGroupCourseService.findOneByid(fav.getSourceId());
					if(newGroupCourse!=null){
						NewCourse course = newCourseService.findOne(newGroupCourse.getCourse().toString());
						map.put("itemId", newGroupCourse.getCourse().toString());
						map.put("title", course.getTitle());
						map.put("intro", course.getIntro());
						//JSONObject obj = newCourseService.getCourseJson(newCourse, "");
						map.put("groupId", newGroupCourse.getGroup());
						map.put("groupName",newGroupCourse.getGroupName());
						map.put("praiseCount", course.getPraiseCount());
						if (user!=null) {
							map.put("logoURL",user.getLogoURL() );
							map.put("uId",user.getId() );
						}
						map.put("coverLogoUrl", course.getLogoUrl());
						map.put("searchType", Config.TYPE_COURSE_GROUP);
						map.put("groupLogoUrl", newGroupCourse.getLogoUrl());//增加群图标
						map.put("ctime", fav.getCtime());//增加收藏时间
						map.put("groupCourseId", newGroupCourse.getId());//群组课程id
						map.put("favCount", newGroupCourse.getFavCount());//收藏数量
					}
				}
			} else if (Config.TYPE_TOPIC_GROUP.equals(fav.getFavType())) {
				Topic topic = topservice.findOneById(fav.getSourceId());
				if (topic != null) {
					map.put("itemId", topic.getTopicId());
					map.put("title", topic.getTitle());
					map.put("intro", topic.getContent());
					map.put("groupId", topic.getSourceId());
					map.put("groupName", topic.getSourceName());
					map.put("praiseCount", topic.getLikesCount());
					map.put("logoURL",topic.getAuthorLogoUrl());
					map.put("uId",topic.getAuthorId());
					map.put("searchType", Config.TYPE_TOPIC_GROUP);
					map.put("images", topic.getImages());
					
				}
			} else if (Config.TYPE_DRYCARGO_GROUP.equals(fav.getFavType())) {
				Drycargo drycargo = drycarGoService.findOneById(fav.getSourceId());
				if (drycargo != null) {
					map.put("itemId", drycargo.getId());
					map.put("title", drycargo.getMessage());
					map.put("intro", drycargo.getDescription());
					map.put("groupId",drycargo.getGroup());
					map.put("groupName", drycargo.getGroupName());
					map.put("praiseCount", drycargo.getLikesCount());
					map.put("logoURL",drycargo.getAuthorLogoUrl());
					map.put("uId",drycargo.getAuthorId());
					map.put("coverLogoUrl", drycargo.getFileUrl());
					map.put("searchType",Config.TYPE_DRYCARGO_GROUP);
					XueWenGroup group = groupTemplate.findOneXuewenGroupRspGroupNameAndMemberAndLogoUrl(drycargo.getGroup().toString());
					if(group!=null){
						map.put("groupLogoUrl", group.getLogoUrl());
					}else{
						map.put("groupLogoUrl", "");
					}
					map.put("ctime", fav.getCtime());//增加收藏时间
					map.put("favCount", drycargo.getFavCount());//增加收藏数量
					map.put("url", drycargo.getUrl());//增加干货外链地址
					
				}
			} else if (Config.TYPE_KNOWLEDGE_GROUP.equals(fav.getFavType())) {
				Knowledge kng = knoeledgeService.getById(fav.getSourceId());
				if (kng != null) {
					JSONObject object = knoeledgeService.getKnowledgeJson(kng, "");
					map.put("itemId", kng.getId());
					map.put("title", kng.getName());
					map.put("intro", kng.getDesc());
					map.put("groupId", (String) object.get("groupId"));
					map.put("groupName", (String) object.get("groupName"));
					map.put("praiseCount", kng.getPraiseCount());
					if (user!=null) {
						map.put("logoURL",user.getLogoURL() );
						map.put("uId",user.getId());
					}
					map.put("coverLogoUrl", kng.getLogoUrl());
					map.put("searchType",Config.TYPE_KNOWLEDGE_GROUP);
				}
			}
			jObjects.add(YXTJSONHelper.addAndModifyAttrJsonObject(fav, map));
		}
		res.put("page", favs);
		res.put("items", jObjects);
		return res;
	}

	public User findOne(String id) {
		return userRepo.findOne(id);
	}

	/**
	 * @return
	 * 
	 * @Title: updateUserPc
	 * @Description: pc端用户资料修改
	 * @param oldUser
	 * @param cUser
	 *            void
	 * @throws
	 */
	public User  updateUserPc(User mUser, User cUser) {
		// 取最新的用户
		cUser = findOne(cUser.getId());
		// 修改昵称
		if (mUser.getNickName() != null) {
			cUser.setNickName(mUser.getNickName());
		}
		// 修改性别
		if (mUser.getSex() != null) {
			cUser.setSex(mUser.getSex());
		}
		// 修改地区
		if (mUser.getArea() != null) {
			cUser.setArea(mUser.getArea());
		}
		// 修改简介
		if (mUser.getIntro() != null) {
			cUser.setIntro(mUser.getIntro());
		}
		// 修改业职

		if (mUser.getStation() != null) {
			cUser.setStation(mUser.getStation());
		}
		// 修改生日
		if (mUser.getBirthday() != 0) {
			cUser.setBirthday(mUser.getBirthday());
		}
		// 修改标签
		if (mUser.getTag() != null) {
			cUser.setTag(mUser.getTag());
			try {
				tagService.updateItemTags(Config.YXTDOMAIN, mUser.getTag(), Config.TAG_TYPE_USER, cUser, cUser.getId());
			} catch (XueWenServiceException e) {
				logger.error("===================用户修改时修改标签库发生未知错误====================");
				e.printStackTrace();
			}
		}
		// 修改头像
		if (mUser.getLogoURL() != null) {
			cUser.setLogoURL(mUser.getLogoURL());
		}
		if (mUser.getInterest() != null) {
			cUser.setInterest(mUser.getInterest());
		}
		// 保存用户
		cUser = userRepo.save(cUser);
		// 在线用户信息修改
		try {
			onlineUserRedisService.addOrUpdateOnlineUser(cUser.getId(), cUser);
		} catch (XueWenServiceException e) {
			// TODO Auto-generated catch block
			logger.error("===================用户修改时修改在线用户信息发生未知错误====================");
		}
		return cUser;
	}

	/**
	 * 
	 * @author yangquanliang
	 * @Description: 读取七牛 存储的用户头像地址信息存入数据库
	 * @param @param userHeadList
	 * @return void
	 * @throws
	 */
	public void saveUserHeadTodb(List<UserHead> userHeadList) {
		userHeadRepository.save(userHeadList);
	}

	/**
	 * 
	 * @Title: getUserRecTagPc
	 * @Description: 用户推荐标签
	 * @param instrests
	 * @param total
	 * @return
	 * @throws XueWenServiceException
	 *             List<String>
	 * @throws
	 */
	public List<String> getUserRecTagPc(String instrests, int total) throws XueWenServiceException {
		List<String> userTagWords = tagService.getWordsList(instrests);
		List<TagBean> tagBeans = tagService.getTagsByAnalysis(userTagWords, total, Config.TAG_TYPE_USER);

		List<String> list = new ArrayList<String>();
		for (TagBean tagBean : tagBeans) {
			list.add(tagBean.getTagName());
		}
		if (tagBeans.size() < total) {
			String tagNames = tagService.getTagsByType(Config.YXTDOMAIN, Config.TAG_TYPE_USER, total);
			if (StringUtils.isNotBlank(tagNames)) {
				String[] tagNamesAdd = tagNames.split(",");
				for (int i = 0; i < tagNamesAdd.length && list.size() < total; i++) {
					if (!list.contains(tagNamesAdd[i])) {
						list.add(tagNamesAdd[i]);
					}
				}
			}
		}
		return list;
	}

	/**
	 * 将之前apache下的图片转成七牛得图片
	 */
	public void changeLogoUrl() {
		List<User> users = userRepo.findAll();
		User one = null;
		String logoUrl = "";
		for (int i = 0; i < users.size(); i++) {
			one = users.get(i);
			logoUrl = one.getLogoURL();
			if (!StringUtil.isBlank(logoUrl)) {
				if (logoUrl.startsWith("http://s1.xuewen")) {
					one.setLogoURL(this.userLogoUrl());
					userRepo.save(one);
				}
			}
		}
	}

	/**
	 * 删除用户，将用户标示deleFlag修改为1
	 * 
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean deleUser(String userId) throws XueWenServiceException {
		if (StringUtil.isBlank(userId)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NODATA_201, null);
		}
		User user = userRepo.findOne(userId);
		if (user == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NODATA_201, null);
		}
		user.setDeleFlag(1);
		userRepo.save(user);
		if (user.getToken() != null) {
			this.loginOut(user, user.getToken(), Config.APPKEY_IOS);
		}
		blackService.saveBlack(user, "非法干预");
		lockoutUser(String.valueOf(user.getUserNumber()));
		return true;
	}
	
	/**
	 * openfire 锁定用户
	 * @param groupNumber
	 */
	public void lockoutUser(String userNumber){
		Map<String,String> map=new HashMap<String, String>();
		map.put("userNum", userNumber);
		RestfulTemplateUtil restfulTemplateUtil=new RestfulTemplateUtil();
		JSONObject obj=restfulTemplateUtil.getRestApiData(openfireLockUrlUrl, map);
		Object status=obj.get("status");
//		ResponseContainer rc=(ResponseContainer)JSONObject.toBean(obj, ResponseContainer.class);
		if(!"200".equals(status.toString())){
			logger.error("======openFire 解散群组失败========");
		}
	}

	/**
	 * 修改用户openfire用户数据
	 * 
	 * @throws XueWenServiceException
	 */
	public void updateUserOpenFire() throws XueWenServiceException {
		List<User> user = userRepo.findAll();
		User one = null;
		OpenFireUser ofu = null;
		if (user != null) {
			for (int i = 0; i < user.size(); i++) {
				one = user.get(i);
				String userNumber = userNumService.getGroupNum();
				one.setUserNumber(Long.parseLong(userNumber));
				ofu = creatOpenFireUser(String.valueOf(one.getUserNumber()),
						one.getPassWord(),one.getOpenId(),one.getEmail());
				one.setOpenFireUser(ofu);
				// 创建openfire用户
				logger.info("创建openfire用户成功=====================" + ofu.getOpenFireUserName());
				one = userRepo.save(one);
			}
		}

	}

	/**
	 * 第三方登录
	 * 
	 * @param user
	 * @return
	 * @throws XueWenServiceException
	 */
	public User loginByThird(User user, String appkey, String a) throws XueWenServiceException {
		if (StringUtil.isBlank(user.getOpenId()) || StringUtil.isBlank(user.getRegistType())) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_THRIDFAILURE_201, null);
		}
		// 查该第三方账号是否存在
		User one = userRepo.findByOpenIdAndRegistTypeAndDeleFlag(user.getOpenId(), user.getRegistType(), 0);
		// 如果该账号不存在
		if (one == null) {
			// 获得基础用户数据
			user = this.getBaseUser(user, String.valueOf(user.getLat()), String.valueOf(user.getLng()), "0");
			// 创建openfire用户

			OpenFireUser ofu = creatOpenFireUser(String.valueOf(user.getUserNumber()),
					user.getPassWord(),user.getOpenId(),user.getEmail());
			user.setOpenFireUser(ofu);
			user = userRepo.save(user);
			// userRegistLogService.addByUserName(a, user);
			userRegistLogService.add(a, user);
			return this.getBaseLoginUser(user, appkey);

		} else {
			return this.getBaseLoginUser(one, appkey);
		}
	}

	/**
	 * 获得登陆基本用户
	 * 
	 * @param one
	 * @return
	 * @throws XueWenServiceException
	 */
	public User getBaseLoginUser(User one, String appkey) throws XueWenServiceException {
		long time = System.currentTimeMillis();
		// 生成登录日志
		UserLoginLog ull = new UserLoginLog();
		ull.setLoginTime(time);
		ull.setUserId(one.getId());
		if (Config.APPKEY_PC.equals(appkey)) {
			one.setToken("PC" + MD5Util.MD5("PC" + one.getUdid() + one.getId() + String.valueOf(time)));
			ull.setLoginType(appkey);
		} else {
			if (!StringUtil.isBlank(one.getToken())) {
				onlineUserRedisService.removeOnlineUser(one.getToken());
			}
			one.setLogintime(time);
			one.setExpireTime(time + 86400000 * 365);
			one.setToken(MD5Util.MD5(one.getUdid() + one.getId() + String.valueOf(time)));
			if (one.getUdid() == null) {
			} else {
				one.setUdid(one.getUdid());
			}
			logger.info("==================更新用户信息=========" + one.getToken() + "====" + one.getUdid());
			one = userRepo.save(one);
			ull.setLoginType(appkey);
		}
		logger.info("==================更新用户信息成功,放入数据后=========token：" + one.getToken() + "-===udid:" + one.getUdid());
		onlineUserRedisService.addOnlineUser(one.getToken(), one);
		logger.info("==================登陆成功后，将登陆用户写入登陆日志表");
		userLoginLogService.saveUserLoginLog(ull);
		return one;
	}

	/**
	 * 处理基础用户信息
	 * 
	 * @param user
	 * @return
	 */
	public User getBaseUser(User user, String lat, String lng, String robot) throws XueWenServiceException {
		if (null != lat && null != lng) {
			double[] position = new double[] { Double.parseDouble(lng), Double.parseDouble(lat) };
			user.setLng(Double.parseDouble(lng));
			user.setLat(Double.parseDouble(lat));
			user.setLocation(position);
		}
		if (StringUtil.isBlank(robot)) {
			user.setRobot(0);
		}
		long time = System.currentTimeMillis();
		user.setCtime(time);// 获取系统时间戳
		user.setUtime(time);
		// 20140911增加创建用户时，增加用户号
		String userNumber = userNumService.getGroupNum();
		logger.info("获取用户号码成功=====================" + userNumber);
		user.setUserNumber(Long.parseLong(userNumber));
		if (StringUtil.isBlank(user.getNickName())) {
			user.setNickName(getNickName());
		}
		if (StringUtil.isBlank(user.getLogoURL())) {
			// 随机获取用户头像
			String userLogoUrl = this.userLogoUrl();
			logger.info("获取用户logourl成功=====================" + userLogoUrl);
			user.setLogoURL(userLogoUrl);
		}
		return user;
	}

	/**
	 * 注册手机用户
	 * 
	 * @param user
	 * @param lat
	 * @param lng
	 * @param robot
	 * @return
	 * @throws XueWenServiceException
	 */
	public User registByPhone(User user, String lat, String lng, String robot) throws XueWenServiceException {
		// 判断该手机号是否存在
		if (!userTemplate.isExiseByPhoneNumber(user.getUserName())) {
			// 将登录名写入到phoneNumber字段
			user.setPhoneNumber(user.getUserName());
			// 将手机验证成功
			user.setPhoneChecked(true);
			// 设置未删除状态
			user.setDeleFlag(0);
			logger.info("用户注册成功==============================" + user.getId());
			return user;
		} else {// 该手机号已经注册
			throw new XueWenServiceException(Config.STATUS_400, Config.MSG_400, null);
		}
	}

	/**
	 * 注册邮箱用户
	 * 
	 * @param user
	 * @param lat
	 * @param lng
	 * @param robot
	 * @return
	 * @throws XueWenServiceException
	 */
	public User registByEmail(User user, String lat, String lng, String robot, String emailUrl) throws XueWenServiceException {
		// 判断email是否存在并且是否检验通过
		if (checkEmail(user.getUserName(), user)) {
			// 将登录名写入到email字段中
			user.setEmail(user.getUserName());
			// 设置未删除状态
			user.setDeleFlag(0);

			if (emailUrl != null) {
				// 生成激活token
				user.setToken(MD5Util.MD5(user.getUdid() + user.getId() + String.valueOf(System.currentTimeMillis())));
				user.setExpireTime(System.currentTimeMillis() + 24 * 60 * 60 * 60 * 1000);
				// 调用task服务进行email验证
				String params = "token=" + user.getToken() + "&email=" + user.getUserName();
				String url="";
				if(emailUrl.indexOf("?")==-1){
					url = emailUrl + "?" + params;
				}else{
					url = emailUrl + "&" + params;
				}
				logger.info(url);
				emailService.sendRegMail(user.getUserName(), url, user.getNickName());
				// 调用task服务结束
				// 将emal未验证
				user.setEmailChecked(false);
			} else {
				user.setEmailChecked(true);
			}
			return user;
		}
		return user;
	}

	/**
	 * 
	 * @Title: sendJHemail
	 * @auther Tangli
	 * @Description: 发送激活邮件
	 * @param email
	 * @param serverName
	 * @throws XueWenServiceException
	 *             void
	 * @throws
	 */
	public void sendJHemail(String email, String serverName,String groupNumber) throws XueWenServiceException {
		// 生成激活token
		if (StringUtil.isBlank(email) || StringUtil.isBlank(serverName)) {
			throw new XueWenServiceException(Config.STATUS_201, "发送邮件参数不能为空", null);
		}
		User user = findOneByEmail(email);
		if (user == null) {
			throw new XueWenServiceException(Config.STATUS_201, "邮件未注册", null);
		}
		user.setToken(MD5Util.MD5(user.getUdid() + user.getId() + String.valueOf(System.currentTimeMillis())));
		user.setExpireTime(System.currentTimeMillis() + 24 * 60 * 60 * 60 * 1000);
		save(user);
		// 调用task服务进行email验证
		String params = "token=" + user.getToken() + "&email=" + user.getUserName();
		if(!StringUtil.isBlank(groupNumber)){
			params+="&gnum="+groupNumber;
		}
		String url = serverName + "&" + params;
		emailService.sendRegMail(user.getEmail(), url, user.getEmail());
	}

	/**
	 * 判断该email是否存在，是否激活（返回true代表该email未注册过）
	 * 
	 * @param email
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean checkEmail(String email, User user) throws XueWenServiceException {
		if (userTemplate.isExiseByEmail(email)) {
			if (userTemplate.isEmailCheckedByEmail(email)) {
				throw new XueWenServiceException(Config.STATUS_201, Config.MSG_EMAILUSER_201, null);
			} else {
				// 调用task服务进行激活邮件发送
				// emailService.sendRegMail(user.getUserName(), "",
				// user.getNickName());
				// 调用task服务结束
				throw new XueWenServiceException(Config.STATUS_201, Config.MSG_EMAILUSERNOCHECK_201, null);
			}
		} else {
			return true;
		}
	}

	/**
	 * 以手机号登陆
	 * 
	 * @param user
	 * @return
	 * @throws XueWenServiceException
	 */
	public User loginByPhone(User user, String appkey) throws XueWenServiceException {
		// 判断手机号是否注册过
		if (!userTemplate.isExiseByPhoneNumber(user.getUserName())) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOREGIST_201, null);
		}
		// deleFlag 为1代表删除
		if (userTemplate.isExitByPhoneNumberAndDeleFlag(user.getUserName(), 1)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOLOGIN_201, null);
		}
		// isPhoneChecked 为false代表没有被激活
		User userInfo = userRepo.findOneByPhoneNumberAndIsPhoneChecked(user.getUserName(), true);
		if (userInfo == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_REGISTNOCHECK_201, null);
		}
		// 判断手机号用户密码是否正确
		if (StringUtil.isBlank(userInfo.getPassWord()) || !userInfo.getPassWord().equals(user.getPassWord())) {
			throw new XueWenServiceException(Config.STATUS_404, Config.MSG_404, null);
		}
		return this.getBaseLoginUser(userInfo, appkey);
	}

	/**
	 * 以email登陆
	 * 
	 * @param user
	 * @return
	 * @throws XueWenServiceException
	 */
	public User loginByEmail(User user, String appkey) throws XueWenServiceException {
		// 判断手机号是否注册过
		if (!userTemplate.isExiseByEmail(user.getUserName())) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOREGIST_201, null);
		}
		// deleFlag 为1代表删除
		if (userTemplate.isExitByEmailAndDeleFlag(user.getUserName(), 1)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOLOGIN_201, null);
		}
		User userInfo = userRepo.findOneByEmailAndIsEmailChecked(user.getUserName(), true);
		// isPhoneChecked 为false代表没有被激活
		if (userInfo == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_REGISTNOCHECK_201, null);
		}
		// 判断邮箱用户密码是否正确
		if (StringUtil.isBlank(userInfo.getPassWord()) || !userInfo.getPassWord().equals(user.getPassWord())) {
			throw new XueWenServiceException(Config.STATUS_404, Config.MSG_404, null);
		}
		return this.getBaseLoginUser(userInfo, appkey);
	}

	/**
	 * 调整数据，登陆调整为通过phoneNumber 与email登陆
	 */
	public void updateUserCheckFlag() throws XueWenServiceException {
		List<User> userList = userRepo.findAll();
		User one = null;
		for (int i = 0; i < userList.size(); i++) {
			one = userList.get(i);
			if (StringUtil.isBlank(one.getUserName())) {
				continue;
			}
			if (StringUtil.isMobileNO(one.getUserName())) {
				one.setPhoneNumber(one.getUserName());
				one.setPhoneChecked(true);
				one.setEmailChecked(false);
				one.setRegistType(Config.USER_REGIST_PHONE);
			} else {
				one.setEmail(one.getUserName());
				one.setEmailChecked(false);
				one.setPhoneChecked(false);
				one.setRegistType(Config.USER_REGIST_EMAIL);
			}
			if (one.getDeleFlag() == 0) {
				one.setDeleFlag(0);
			}
			userRepo.save(one);
		}
	}

	/**
	 * 检测用户token有效
	 * 
	 * @param user
	 * @throws XueWenServiceException
	 */
	public boolean checkToken(String email, String token) throws XueWenServiceException {
		if (StringUtil.isBlank(email) || StringUtil.isBlank(token)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201, null);
		}
		User userToken = userTemplate.getOneByEmailAndToken(email, token);
		if (userToken == null) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_ACTIVELINKERROR_201, null);
		}
		if (userToken.isEmailChecked()) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_ACTIVED_201, null);
		}
		if (userToken.getExpireTime() < System.currentTimeMillis()) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_ACTIVELINKERROR_201, null);
		}
		return true;
	}

	/**
	 * 发送找回密码邮箱用户连接
	 * 
	 * @param email
	 * @param token
	 * @throws XueWenServiceException
	 */
	public void sendEmailForPassWord(String registMailUrl, String passWordMailUrl, String email) throws XueWenServiceException {
		// 判断是否有此用户
		if (!userTemplate.isExiseByEmail(email)) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOREGIST_201, null);
		}
		User user = userRepo.findOneByEmail(email);
		user.setToken(MD5Util.MD5(user.getUdid() + user.getId() + String.valueOf(System.currentTimeMillis())));
		// 调用task服务进行email验证
		String params = "udid=" + user.getUdid() + "&token=" + user.getToken() + "&email=" + email;
		String resUrl = registMailUrl + "?" + params;
		userRepo.save(user);
		// 判断该email是否被激活
		if (!userTemplate.isEmailCheckedByEmail(email)) {

			emailService.sendRegMail(email, resUrl, user.getNickName());
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_REGISTNOCHECK_201, null);
		}
		// 发送找回密码发送邮箱
		String passUrl = passWordMailUrl + "?" + params;
		emailService.sendRestPwdMail(email, passUrl, user.getNickName());
	}

	/**
	 * 重置密码(email)
	 * 
	 * @param passWrod
	 * @param confirmPassWrod
	 * @param udid
	 * @param phoneNumber
	 */
	public boolean resetPasswordForEmail(String passWord, String email) throws XueWenServiceException {
		logger.info("重置密码的email为==============" + email);
		User user = userRepo.findOneByEmail(email);
		if (null == user) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOUSER_201, null);
		}
		user.setPassWord(passWord);
		userRepo.save(user);
		return true;
	}

	/**
	 * 将email注册校验调整为通过
	 * 
	 * @param email
	 * @return
	 * @throws XueWenServiceException
	 */
	public User changeEmailChecked(String email, String appkey) throws XueWenServiceException {
		User user = userRepo.findOneByEmail(email);
		if (null == user) {
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_NOUSER_201, null);
		}
		user.setEmailChecked(true);
		userRepo.save(user);
		user = login(user, appkey);
		return user;
	}

	/**
	 * 
	 * @Title: findOneByEmail
	 * @auther Tangli
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param email
	 * @return User
	 * @throws
	 */
	public User findOneByEmail(String email) {
		return userRepo.findOneByEmail(email);
	}

	/**
	 * 
	 * @Title: joinGroup
	 * @auther Tangli
	 * @Description: 邀请注册时 加入群用
	 * @param groupNumber
	 * @param user
	 * @throws XueWenServiceException
	 *             void
	 * @throws
	 */
	public void joinGroup(String groupNumber, User user) throws XueWenServiceException {
		XueWenGroup group = groupService.findByGroupNumber(Long.valueOf(groupNumber));
		groupService.join(group, user.getId());
	}
    
	public JSONObject shutDown(User user,String groupNumber){
		Map<String, Object>map=new HashMap<String,Object>();
		XueWenGroup group = groupService.findByGroupNumber(Long.valueOf(groupNumber));

		map.put("groupId", group.getId());
		//JSONObject object=YXTJSONHelper.addAndModifyAttrJsonObject(user, map,);
		JSONObject object=YXTJSONHelper.getExObjectAttrJsonObject(user, map,"job","userTageName","interestJob","openFireUser");
		return object;
	}
	/**
	 * @throws XueWenServiceException
	 * 
	 * @Title: findOneByPhoneOrEmail
	 * @auther Tangli
	 * @Description: 校验手机或者邮件
	 * @param mp
	 * @return boolean
	 * @throws
	 */
	public boolean findOneByPhoneOrEmail(String mp) throws XueWenServiceException {
		if (StringUtil.isBlank(mp)) {
			throw new XueWenServiceException(Config.STATUS_201, "请填写正确的邮件和手机号", null);
		}
		User user = userRepo.findOneByPhoneNumberOrEmail(mp, mp);
		return user == null ? false : true;
	}

	public User getOneByPhoneOrEmail(String mp) throws XueWenServiceException {
		if (StringUtil.isBlank(mp)) {
			throw new XueWenServiceException(Config.STATUS_201, "请填写正确的邮件和手机号", null);
		}
		User user = userRepo.findOneByPhoneNumberOrEmail(mp, mp);
		return user;
	}

	/**
	 * @throws XueWenServiceException
	 * 
	 * @Title: bindEmail
	 * @auther Tangli
	 * @Description: 绑定邮箱
	 * @param userId
	 * @param email
	 *            void
	 * @throws
	 */
	public User bindEmail(String userId, String email, String code) throws XueWenServiceException {
		if (StringUtil.isBlank(userId) || StringUtil.isBlank(email) || StringUtil.isBlank(code)) {
			throw new XueWenServiceException(Config.STATUS_201, "参数不能为空", null);
		} else if (findOneByPhoneOrEmail(email)) {
			throw new XueWenServiceException(Config.STATUS_201, "邮箱已使用过！", null);
		} else {
			MailCode mailCode = mailCodeService.findOneByEmail(email);
			if (mailCode != null && code.equals(mailCode.getCode())) {
				User user = findOne(userId);
				user.setEmail(email);
				user.setEmailChecked(true);
				save(user);
				return user;
			} else {
				throw new XueWenServiceException(Config.STATUS_201, "验证码错误", null);
			}
		}

	}

	/**
	 * 
	 * @Title: bindPhone
	 * @auther Tangli
	 * @Description: 绑定手机
	 * @param userId
	 * @param phone
	 * @param code
	 * @throws XueWenServiceException
	 *             void
	 * @throws
	 */
	public void bindPhone(String userId, String phone, String code) throws XueWenServiceException {
		if (StringUtil.isBlank(userId) || StringUtil.isBlank(phone) || StringUtil.isBlank(code)) {
			throw new XueWenServiceException(Config.STATUS_201, "参数不能为空", null);
		} else if (findOneByPhoneOrEmail(phone)) {
			throw new XueWenServiceException(Config.STATUS_201, "手机已使用过！", null);
		} else if (smsService.checkSms(phone, "1", code)) {
			User user = findOne(userId);
			user.setPhoneNumber(phone);
			user.setPhoneChecked(true);
			save(user);
		} else {
			throw new XueWenServiceException(Config.STATUS_201, "验证码错误", null);
		}

	}

	/**
	 * 
	 * @Title: save
	 * @auther Tangli
	 * @Description: 保存
	 * @param user
	 *            void
	 * @throws
	 */
	public void save(User user) {
		userRepo.save(user);
	}

	/**
	 * @throws XueWenServiceException
	 * 
	 * @Title: modifiPwdByPhoneOrEmail
	 * @auther Tangli
	 * @Description: 修改密码
	 * @param userId
	 * @param email
	 * @param phone
	 * @param code
	 *            void
	 * @throws
	 */
	public void modifiPwdByPhoneOrEmail(String ep, String pwd) throws XueWenServiceException {
		User user = getOneByPhoneOrEmail(ep);
		user.setPassWord(pwd);
		save(user);

	}

	/**
	 * @throws XueWenServiceException
	 * 
	 * @Title: resetPasswordByEmail
	 * @auther Tangli
	 * @Description: 邮箱修改密码
	 * @param email
	 *            邮件地址
	 * @param pwd
	 *            void
	 * @throws
	 */
	private boolean checkEmailCode(String email, String code) {
		MailCode mailCode = mailCodeService.findOneByEmail(email);
		if (mailCode != null && code.equals(mailCode.getCode())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @throws XueWenServiceException
	 * 
	 * @Title: checkPhoneOrEmailCode
	 * @auther Tangli
	 * @Description: 验证码
	 * @param ep
	 * @param code
	 * @param type
	 * @return boolean
	 * @throws
	 */
	public boolean checkPhoneOrEmailCode(String ep, String code, int type) throws XueWenServiceException {
		if (Config.MODIFYPWD_TYPE_EMAIL == type) {
			// 验证邮件
			return checkEmailCode(ep, code);
		} else if (Config.MODIFYPWD_TYPE_PHONE == type) {
			return smsService.checkSms(ep, "2", code);
		}
		return false;
	}

	/**
	 * 合并用户数据
	 * 
	 * @param fromUserId
	 * @param toUser
	 * @throws XueWenServiceException
	 */
	public void mergeUserAccount(String fromUserId, User toUser) throws XueWenServiceException {

		// 合并群组
		groupService.mergeUserGroup(fromUserId, toUser.getId());
		// 合并干货
		drycarGoService.mergeDrycargo(fromUserId, toUser.getId(), toUser.getNickName(), toUser.getLogoURL());
		// 合并课程
		newCourseService.mergeNewCourse(fromUserId, toUser.getId());
		// 合并知识
		knoeledgeService.mergeKnowledge(fromUserId, toUser.getId());
		// 合并课时
		lessonService.mergeLesson(fromUserId, toUser.getId());
		// 合并群组课程
		newGroupCourseService.mergeNewGroupCourse(fromUserId, toUser.getId());
		// 合并用户群组课程
		userGroupCourseService.mergeUserGroupCourse(fromUserId, toUser.getId());
		// 合并话题
		topicService.mergeTopic(fromUserId, toUser.getId(), toUser.getNickName(), toUser.getLogoURL());
		// 合并主楼回复
		postService.mergePost(fromUserId, toUser.getId(), toUser.getNickName(), toUser.getLogoURL());
		// 合并副楼回复
		postService.mergeSubPost(fromUserId, toUser.getId(), toUser.getNickName(), toUser.getLogoURL());
		// 合并副楼回复的回复
		postService.mergeSubPostToOther(fromUserId, toUser.getId(), toUser.getNickName());
		//合并联系人
		contactUserService.mergeContactUser(fromUserId, toUser.getId());
		//合并账户基本信息
		this.mergeUserBaseAccount(fromUserId, toUser);
		//合并订单信息
		mergeOrderInfo(fromUserId, toUser.getId(), toUser.getNickName(), toUser.getLogoURL());
	}
	/**
	 * 
	 * @param fromUserId
	 * @param toUserId
	 * @param toUserNickName
	 * @param toUserLogoUrl
	 * @throws XueWenServiceException
	 */
	public void mergeOrderInfo(String fromUserId,String toUserId,String toUserNickName,String toUserLogoUrl)throws XueWenServiceException{
		String url = ztiaoPayUrl+ Config.ORDER_MERGE_URL;
		
		Map<String,String> map=new HashMap<String, String>();
		map.put("fromUserId", fromUserId);
		map.put("toUserId", toUserId);
		map.put("toUserNickName", toUserNickName);
		map.put("toLogoUrl", toUserLogoUrl);
		RestfulTemplateUtil restfulTemplateUtil=new RestfulTemplateUtil();
		JSONObject obj=restfulTemplateUtil.getRestApiData(url, map);
	}

	/**
	 * 判断用户是否设置密码
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean checkIsSetPassWord(String userId) throws XueWenServiceException {
		User user = userTemplate.checkIsSetPassword(userId);
		if (user == null || StringUtil.isBlank(user.getPassWord())) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 判断是否有其他用户绑定过此手机号，如果有则返回此用户，如果没有则返回空
	 * 
	 * @param phoneNumber
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public User isHasUserSamplePhone(String phoneNumber, String userId) throws XueWenServiceException {
		return userRepo.findOneByIdNotAndPhoneNumberAndIsPhoneChecked(userId, phoneNumber, true);
	}

	/**
	 * 绑定手机号，在绑定之前应该经过验证码校验，账户合并，密码设置等流程
	 * 
	 * @param userId
	 * @param phoneNumber
	 * @throws XueWenServiceException
	 */
	public User boundPhoneNumber(User user, String phoneNumber, String passWord) throws XueWenServiceException {
		// 将其他所有账户的绑定状态设置为未绑定
		userTemplate.setAllPhoneNotChecked(phoneNumber);
		// 将本账户绑定此手机号
		user.setPhoneNumber(phoneNumber);
		user.setPassWord(passWord);
		user.setPhoneChecked(true);
		user =userRepo.save(user);
		onlineUserRedisService.addOrUpdateOnlineUser(user.getId(), user);
		return user;
	}
	/**
	 * 各注册类型用户统计
	 * @return
	 * @throws XueWenServiceException
	 */
	public Object countUser()throws XueWenServiceException{
		int phoneRegists=userRepo.countByRegistType(Config.USER_REGIST_PHONE);
		int emailRegists=userRepo.countByRegistType(Config.USER_REGIST_EMAIL);
		int qqRegists=userRepo.countByRegistType(Config.USER_REGIST_QQ);
		int weixinRegists=userRepo.countByRegistType(Config.USER_REGIST_WEIXIN);
		int sinaRegists=userRepo.countByRegistType(Config.USER_REGIST_SINA);
		long allRegists=userRepo.count();
	 	JSONObject member = new JSONObject();  
	    member.put("phoneRegists",phoneRegists);  
	    member.put("emailRegists",emailRegists);  
	    member.put("qqRegists",qqRegists);  
	    member.put("weixinRegists", weixinRegists);  
	    member.put("sinaRegists",sinaRegists);  
	    member.put("allRegists",allRegists);  
	    return member;  
		
	}

	public ResponseUser createResUser(User user) throws XueWenServiceException {
		ResponseUser rps=new ResponseUser(user);
		rps.setFollowerCount(contactUserService.getUserContact(user.getId(),"0"));
		rps.setAttentionCount(contactUserService.getUserContact(user.getId(),"1"));
		return rps;
	}
	
	/**
	 * @throws XueWenServiceException 
	 * 
	 * @Title: delUserFav
	 * @auther tangli
	 * @Description:删除用户id
	 * @param user 操作用户
	 * @param id   收藏Id
	 * @return boolean 
	 * @throws
	 */
	public boolean delUserFav(User user, String id) throws XueWenServiceException {
		if(StringUtil.isBlank(id)){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201_ARGS_ERRO, null);
		}
		Fav fav=favService.findOne(id);
		if(fav!=null&&user.getId().equals(fav.getUserId())){
			//1 删除fav
			favService.deleteById(id);
			//2 更新收藏对象中的收藏数量  -1
			favService.updateCountAfterDelFav(fav.getSourceId(),fav.getFavType(),user.getId());
			return true;
		}
		return false;
		
	}
	/**
	 * 合并账户基本信息
	 * @param fromUser
	 * @param toUser
	 * @throws XueWenServiceException
	 */
	public void mergeUserBaseAccount(String fromUserId,User toUser)throws XueWenServiceException {
		User fromUser = userRepo.findOne(fromUserId);
		if(fromUser!=null){
			if(StringUtil.isBlank(toUser.getNickName())){//如果昵称为空
				toUser.setNickName(fromUser.getNickName());
			} if(StringUtil.isBlank(toUser.getLogoURL())){//如果头像为空
				toUser.setLogoURL(fromUser.getLogoURL());
			} if(0==toUser.getAge()) {//如果年纪为0
				toUser.setAge(fromUser.getAge());
			} if(StringUtil.isBlank(toUser.getSex())){//如果性别为空
				toUser.setSex(fromUser.getSex());
			} if(StringUtil.isBlank(toUser.getEmail())){//如果邮箱为空
				toUser.setEmail(fromUser.getEmail());
			} if(StringUtil.isBlank(toUser.getPhoneNumber())){//如果手机号为空
				toUser.setPhoneNumber(fromUser.getPhoneNumber());
			} if(StringUtil.isBlank(toUser.getIntro())){//如果介绍为空
				toUser.setIntro(fromUser.getIntro());
			} if(0==toUser.getBirthday()){//如果生日为空
				toUser.setBirthday(fromUser.getBirthday());
			} if(0==toUser.getGroupCount()){//如果群组数量为空
				toUser.setGroupCount(fromUser.getGroupCount());
			} if(StringUtil.isBlank(toUser.getConstelLation())){//如果星座为空
				toUser.setConstelLation(fromUser.getConstelLation());
			} if(null==toUser.getIndustry()||toUser.getIndustry().size()==0){//如果方向为空
				toUser.setIndustry(fromUser.getIndustry());
			} if(StringUtil.isBlank(toUser.getDirection())){//如果行业为空
				toUser.setDirection(fromUser.getDirection());
			} if(StringUtil.isBlank(toUser.getIndustryName())){//如果方向名称为空
				toUser.setIndustryName(fromUser.getIndustryName());
			} if(StringUtil.isBlank(toUser.getStation())){//如果岗位为空
				toUser.setStation(fromUser.getStation());
			} if(StringUtil.isBlank(toUser.getEducation())){//如果学历为空
				toUser.setEducation(fromUser.getEducation());
			} if(StringUtil.isBlank(toUser.getSchool())){//如果学校为空
				toUser.setSchool(fromUser.getSchool());
			} if(StringUtil.isBlank(toUser.getArea())){//如果地区为空
				toUser.setArea(fromUser.getArea());
			} if(StringUtil.isBlank(toUser.getCompany())){//如果公司为空
				toUser.setCompany(fromUser.getCompany());
			} if(null==toUser.getInterest()||toUser.getInterest().size()==0){//如果感兴趣为空
				toUser.setInterest(fromUser.getInterest());
			} if(null==toUser.getSpecial()||toUser.getSpecial().size()==0){//如果擅长为空
				toUser.setSpecial(fromUser.getSpecial());
			}
			userRepo.save(toUser);
		}
	}
	/**
	 * 
	 * @Title: getMyCreated
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param userId
	 * @param pageable
	 * @return
	 * @throws XueWenServiceException Map<String,Object>
	 * @throws
	 */
	public Map<String, Object> getMyCreated(String userId,QueryModel dm) throws XueWenServiceException {
		Map<String, Object> res = new HashMap<String, Object>();
		List<JSONObject> jObjects = new ArrayList<JSONObject>();
//		dm.setSort("praiseCount");
		dm.setN(0);
		dm.setS(9);
		Pageable pageable=PageRequestTools.pageRequesMake(dm);
		Page<NewCourse> courses=newCourseService.getMyCourses(userId, pageable);
		if(courses.getContent().size()!=0){
			for (NewCourse newCourse : courses) {
				JSONObject map=new JSONObject();
				map.put("itemId", newCourse.getId());
				map.put("title", newCourse.getTitle());
				map.put("intro", newCourse.getIntro());
				JSONObject obj = newCourseService.getCourseJson(newCourse, "");
				map.put("groupId", (String) obj.get("groupId"));
				map.put("groupName", (String) obj.get("groupName"));
				map.put("praiseCount", newCourse.getPraiseCount());
				User user=findOne(newCourse.getCreateUser().toString());
				if (user!=null) {
					map.put("logoURL",user.getLogoURL() );
					map.put("uId",user.getId() );
				}
				map.put("coverLogoUrl", newCourse.getLogoUrl());
				map.put("searchType", Config.TYPE_COURSE_GROUP);
				map.put("ctime", newCourse.getCtime());
				jObjects.add(map);
				
			}
		}
//		dm.setSort("likesCount");
//		Pageable pageable1=PageRequestTools.pageRequesMake(dm);
		Page<Topic> topics=topicService.findAllByUserID(userId, pageable);
		if (topics.getContent().size()!=0) {
			for (Topic topic : topics.getContent()) {
				JSONObject map=new JSONObject();
				map.put("itemId", topic.getTopicId());
				map.put("title", topic.getTitle());
				map.put("intro", topic.getContent());
				map.put("groupId", topic.getSourceId());
				map.put("groupName", topic.getSourceName());
				map.put("praiseCount", topic.getLikesCount());
				map.put("logoURL",topic.getAuthorLogoUrl());
				map.put("uId",topic.getAuthorId());
				map.put("searchType", Config.TYPE_TOPIC_GROUP);
				map.put("images", topic.getImages());
				map.put("ctime", topic.getCtime());
				jObjects.add(map);
			}
		}
//		dm.setSort("likesCount");
//		Pageable pageable2=PageRequestTools.pageRequesMake(dm);
		Page<Drycargo> drys=drycarGoService.getUserDrycargoByDryFlag(userId, 0, pageable);
		if(drys.getContent().size()!=0){
			for (Drycargo drycargo : drys) {
				JSONObject map=new JSONObject();
				map.put("itemId", drycargo.getId());
				map.put("title", drycargo.getMessage());
				map.put("intro", drycargo.getDescription());
				map.put("groupId",drycargo.getGroup());
				map.put("groupName", drycargo.getGroupName());
				map.put("praiseCount", drycargo.getLikesCount());
				map.put("logoURL",drycargo.getAuthorLogoUrl());
				map.put("uId",drycargo.getAuthorId());
				map.put("coverLogoUrl", drycargo.getFileUrl());
				map.put("searchType",Config.TYPE_DRYCARGO_GROUP);
				map.put("ctime", drycargo.getCtime());
				jObjects.add(map);
			}
		}
		ListComparator comparator =new ListComparator("ctime");
		Collections.sort(jObjects,comparator);
		int s=jObjects.size()>27?27:jObjects.size();
		//排序
		res.put("items", jObjects.subList(0, s));
		
		return res;
	}
	
	/**
	 * 根据用户ID获取用户昵称
	 * @param userId
	 * @return
	 * @throws XueWenServiceException
	 */
	public String getUserNickNameByUserId(String userId)throws XueWenServiceException{
		if(!StringUtil.isBlank(userId)){
			return "";
		}
		User user=userTemplate.getUserNickNameByUserId(userId);
		if(user == null){
			return "";
		}else{
			return user.getNickName();
		}
		
	}
	
	
	
	/**
	 * 根据用户ID 查询用户推送列表信息
	 * 
	 * @param user
	 * @param page
	 * @return
	 * @throws XueWenServiceException
	 */
	public Page<NewUserNickName> findUserNickName(String keywords,Pageable pageable) throws XueWenServiceException {
		if (StringUtil.isBlank(keywords)) {
			return newUserNickNameRepository.findAll(pageable);
		} else {
			keywords = ".*?(?i)" + keywords + ".*";
			return newUserNickNameRepository.findByNameRegex(keywords, pageable);
		}
	}
	
	
	/**
	 * 根据用户ID 查询用户推送列表信息
	 * 
	 * @param user
	 * @param page
	 * @return
	 * @throws XueWenServiceException
	 */
	public long countUserNickName() throws XueWenServiceException {
			return newUserNickNameRepository.count();
	}
	
	/**
	 * 根据用户ID 查询用户推送列表信息
	 * 
	 * @param user
	 * @param page
	 * @return
	 * @throws XueWenServiceException
	 */
	public void deleteUserNickName(NewUserNickName n) throws XueWenServiceException {
			 newUserNickNameRepository.delete(n);
	}
	
	
	/**
	 * 根据用户ID 查询用户推送列表信息
	 * 
	 * @param user
	 * @param page
	 * @return
	 * @throws XueWenServiceException
	 */
	public NewUserNickName findUserNickNameBynickname(String name) throws XueWenServiceException {
			return newUserNickNameRepository.findByName(name);
	}
	
	
	/**
	 * 根据用户ID 查询用户推送列表信息
	 * 
	 * @param user
	 * @param page
	 * @return
	 * @throws XueWenServiceException
	 */
	public NewUserNickName findUserNickNameByid(String id) throws XueWenServiceException {
			return newUserNickNameRepository.findOne(id);
	}
	
	
	/**
	 * 根据用户ID 查询用户推送列表信息
	 * 
	 * @param user
	 * @param page
	 * @return
	 * @throws XueWenServiceException
	 */
	public NewUserNickName findUserNickNameBynumber(int number) throws XueWenServiceException {
			return newUserNickNameRepository.findByNumber(number);
	}
	
	
	/**
	 * 根据用户ID 查询用户推送列表信息
	 * 
	 * @param user
	 * @param page
	 * @return
	 * @throws XueWenServiceException
	 */
	public void saveUserNickName(NewUserNickName n) throws XueWenServiceException {
			 newUserNickNameRepository.save(n);
	}
	

}
