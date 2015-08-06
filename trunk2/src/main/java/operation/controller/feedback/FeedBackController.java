package operation.controller.feedback;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import operation.BaseController;
import operation.exception.XueWenServiceException;
import operation.pojo.category.Category;
import operation.pojo.feedback.FeedBack;
import operation.pojo.user.User;
import operation.service.feedback.FeedBackService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tools.Config;
import tools.ResponseContainer;
import tools.StringUtil;

@RestController
@RequestMapping("/feedback")
public class FeedBackController extends BaseController{
	@Autowired
	public FeedBackService feedBackService;
	
	public FeedBackController(){
		
	}
	
	/**
	 * 反馈信息
	 * @param request
	 * @param category
	 * @return
	 */
	@RequestMapping("create")
	public @ResponseBody ResponseContainer create(HttpServletRequest request,
			FeedBack feedBack) {
		try{
		//	将中文字段转码
			if(null != feedBack.getContext()){
				if(!StringUtil.isEmpty(feedBack.getContext().toString())){
					feedBack.setContext((URLDecoder.decode(feedBack.getContext(),"UTF-8")));
				}
			}
		String token = request.getParameter("token");
		User currentUser = this.getCurrentUser(token);
		feedBack.setUser(currentUser.getId());
		feedBack.setCtime(System.currentTimeMillis());
		feedBack.setUserPhone(currentUser.getUserName());
		feedBack.setUsername(currentUser.getNickName());
		FeedBack feed = feedBackService.save(feedBack);
		
		return addResponse(Config.STATUS_200,Config.MSG_TOADMIN_200,feed,Config.RESP_MODE_10,"");
	} catch (XueWenServiceException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return addResponse(e.getCode(),e.getMessage(),false,Config.RESP_MODE_10,"");
	} catch (Exception e){
		e.printStackTrace();
		return addResponse(Config.STATUS_505,Config.MSG_505,false,Config.RESP_MODE_10,"");
	}
	}

}
