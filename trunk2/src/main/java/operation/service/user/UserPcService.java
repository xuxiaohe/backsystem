package operation.service.user;

import java.util.List;

import operation.exception.XueWenServiceException;
import operation.pojo.black.Black;
import operation.pojo.group.XueWenGroup;
import operation.pojo.user.OpenFireUser;
import operation.pojo.user.ResponsePcUser;
import operation.pojo.user.User;
import operation.pojo.user.UserName;
import operation.repo.group.GroupRepository;
import operation.repo.user.UserRepository;
import operation.repo.user.UserTemplate;
import operation.service.black.BlackService;
import operation.service.drycargo.DrycargoService;
import operation.service.group.GroupService;
import operation.service.log.UserLoginLogService;
import operation.service.tags.TagService;
import operation.service.topics.TopicService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import tools.Config;
import tools.StringUtil;
@Service
@Component
@EnableScheduling
/**
 * 
* @ClassName: UserPcService
* @Description: pc端的数据瘦身
* @author shenb
* @date 2014年12月17日 下午2:26:52
*
 */
public class UserPcService {
	private static final Logger logger=Logger.getLogger(UserPcService.class);
	@Autowired
	public UserRepository userRepo;
	@Autowired
	public UserTemplate userTemplate;
	
	@Autowired
	public UserLoginLogService userLoginLogService;
	
	@Autowired
	private TagService tagService;
	
	@Autowired
	public UserNumService userNumService;
	
	@Autowired
	private GroupService groupService;
	
	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private UserNameService userNameService;
	@Autowired
	private UserService userService;
	
	@Autowired
	private DrycargoService drycargoService;
	@Autowired
	private TopicService topicService;
	@Autowired
	private ContactUserService contactUserService;
	
	//系统参数
//	@Value("${image.default.path}")
//	private String imagDefaultPath;
//	@Value("${image.default.url}")
//	private String imagDefaultUrl;
	
	@Autowired
	private BlackService blackService;
	/**
	 * 
	 * @Title: registPc
	 * @Description: 注册
	 * @param user
	 * @return
	 * @throws XueWenServiceException User
	 * @throws
	 */
	public User registPc(User user) throws XueWenServiceException {
		Black black = blackService.getBlackByUser(user.getUserName());
		if(black!=null){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_REGISTFAIL_201,null);
		}
		if (!userTemplate.isExiseByUserName(user.getUserName())) {
			long time=System.currentTimeMillis();
			user.setCtime(time);// 获取系统时间戳
			user.setUtime(time);
			/**
			 *注册用户时需要将用户写入openfire，否则用户无法使用APP聊天 
			 */
			//20140911增加创建用户时，增加用户号
			String userNumber = userNumService.getGroupNum();
			user.setUserNumber(Long.parseLong(userNumber));	
			//随机获取用户名
			List<UserName> userName =  userNameService.getUserName();
			UserName un = userName.get(StringUtil.getOneInt(userName.size()));
			if (StringUtil.isBlank(user.getNickName())) {
				user.setNickName(un.getName());
			}
			
			//随机获取用户头像
			String userLogoUrl = userService.userLogoUrl();
			user.setLogoURL(userLogoUrl);
			user.setPhoneNumber(user.getUserName());
			// 注册成功后，将注册用户信息同步到openFire服务端
			OpenFireUser ofu = userService.creatOpenFireUser(String.valueOf(user.getUserNumber()),
					user.getPassWord(),user.getOpenId(),user.getEmail());
			user.setOpenFireUser(ofu);
			//创建openfire用户
			user=userRepo.save(user);
			//如果为邀请客户，需要将该用户加入到邀请群中，并修改邀请对象 操作为已操作
			return user;
		} else {
			throw new XueWenServiceException(Config.STATUS_400, Config.MSG_400,
					null);
		}
	}
	/**
	 * 根据用户ID查询用户信息pc
	 * 
	 * @return
	 */
	public ResponsePcUser findUserPc(String id) throws XueWenServiceException {
		User userResult = userRepo.findOneById(id);
		if (null != userResult) {
			ResponsePcUser user= new ResponsePcUser(userResult.getId(), userResult.getUserName(), userResult.getToken(), userResult.getUdid(), userResult.getNickName(), userResult.getLogoURL(), userResult.getTag(), userResult.getEmail(), userResult.getPhoneNumber(), userResult.getIntro(),userResult.getArea(),userResult.getStation());
			user.setStation(userResult.getStation());
			user.setDryCargoCount(drycargoService.getDryCount(id, 0));
			user.setTopicCount(topicService.getCountsByUserId(id));
			user.setAttentionCount(contactUserService.getUserContact(id, "1"));
			return user;
		} else {
			throw new XueWenServiceException(Config.STATUS_404, Config.MSG_404,
					null);
		}
	}
	/**
	 * 
	 * @Title: registByGroupId
	 * @Description: 与群组关联注册
	 * @param user
	 * @param groupId
	 * @return
	 * @throws XueWenServiceException User
	 * @throws
	 */
	public User registByGroupId(User user,String groupNumber) throws XueWenServiceException {
		if(StringUtil.isBlank(groupNumber)){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,
					null);
		}
		XueWenGroup group=groupService.findByGroupNumber(Long.parseLong(groupNumber));
		user=this.registPc(user);
		//将注册后的用户添加到群组
		groupService.join(group, user.getId());
		logger.info("==================加入群组成功=========");
		return user;
	}
	
}
