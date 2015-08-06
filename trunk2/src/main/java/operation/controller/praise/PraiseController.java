package operation.controller.praise;


import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import operation.BaseController;
import operation.exception.XueWenServiceException;
import operation.pojo.praise.Praise;
import operation.pojo.pub.QueryModel;
import operation.pojo.user.User;
import operation.service.praise.PraiseService;
import tools.Config;
import tools.PageRequestTools;
import tools.ReponseDataTools;
import tools.ResponseContainer;


@RestController
@RequestMapping("/like")
public class PraiseController extends BaseController{
	
	private static final Logger logger=Logger.getLogger(PraiseController.class);
	
	@Autowired
	private PraiseService praiseService;
	/**
	 * 点赞
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("addLike")
	public @ResponseBody ResponseContainer addLike(HttpServletRequest request,Praise praise) {
		try {
			String token=request.getParameter("token");
			User user=this.getCurrentUser(token);
			praiseService.addPraise(praise,user.getId());
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("业务错误："+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}
	}
	
	@RequestMapping("likeCount")
	public @ResponseBody ResponseContainer likeCount(HttpServletRequest request) {
		try {
			String sourceId=request.getParameter("sourceId");
			String type=request.getParameter("type");
			int count=praiseService.countByDomainAndSourceIdAndType(Config.YXTDOMAIN, sourceId, type);
			return addResponse(Config.STATUS_200, Config.MSG_200, count,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("业务错误："+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 查看赞用户列表
	 * @param request
	 * @return
	 */
	@RequestMapping("userLike")
	public @ResponseBody ResponseContainer userLike(HttpServletRequest request,QueryModel dm) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String sourceId=request.getParameter("sourceId");
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<Praise> praises = praiseService.findOnePraiseByDomainAndSourceId(Config.YXTDOMAIN, sourceId, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), praises);
			this.getReponseData().setResult((praiseService.toResponeses(praises.getContent(),currentUser.getId())));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),
					Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("业务错误："+e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}
	}
	
	
}
