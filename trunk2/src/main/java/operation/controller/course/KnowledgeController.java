package operation.controller.course;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;
import operation.BaseController;
import operation.exception.XueWenServiceException;
import operation.pojo.course.GroupShareKnowledge;
import operation.pojo.course.Knowledge;
import operation.pojo.pub.QueryModel;
import operation.pojo.user.User;
import operation.service.course.GroupShareKnowledgeService;
import operation.service.course.KnowledgeService;
import operation.service.user.UserService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tools.Config;
import tools.PageRequestTools;
import tools.ReponseDataTools;
import tools.ResponseContainer;
import tools.StringToList;

/**
 * 
 * @ClassName: KnowledgeController
 * @Description: 知识Controller
 * @author Jack Tang
 * @date 2014年12月23日 下午5:13:30
 *
 */
@RestController
@RequestMapping("knowledge")
public class KnowledgeController extends BaseController {
	
	private static final Logger logger=Logger.getLogger(KnowledgeController.class);
	
	@Autowired
	KnowledgeService knowledgeService;
	@Autowired
	private GroupShareKnowledgeService groupshareKnowledgeService;
	@Autowired
	private UserService userService;

	/**
	 * 
	 * @Title: addKnowledge
	 * @Description: 添加一条知识
	 * @param knowledge
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping(value = "addKnowledge", method = RequestMethod.POST)
	public @ResponseBody ResponseContainer addKnowledge(Knowledge knowledge,
			String groupId, String token) {
		try {
			User user = this.getCurrentUser(token);

			knowledgeService.addKnowledge(knowledge, groupId, user.getId());

			return addResponse(Config.STATUS_200, Config.MSG_200, knowledge,
					Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addPageResponse(e.getCode(), e.getMessage(), null,
					Config.RESP_MODE_10, "");
		}
	}

	/**
	 * 
	 * @Title: addCitem
	 * @Description:云储存转码回调入口
	 * @param citems
	 * @param code
	 * @param cid
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping(value = "addCitems", method = RequestMethod.POST)
	public @ResponseBody ResponseContainer addCitem(String citems, int code,
			String cid, String logourl, Integer words, Integer pages,
			Integer duration) {
		try {
			knowledgeService.addCitems(citems, cid, code, logourl, words,
					pages, duration);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,
					Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addPageResponse(e.getCode(), e.getMessage(), null,
					Config.RESP_MODE_10, "");

		}

	}

	/**
	 * 
	 * @Title: 

		nowledge
	 * @Description: 获取单个知识
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping(value = "getKnowledge")
	public @ResponseBody ResponseContainer getKnowledge(String id,String token) {
		try {
			User user=null;
			if(token!=null){
				user=getCurrentUser(token);
			}
			JSONObject jsonObject = knowledgeService.getKnowledgeAndUser(id,user);
			return addResponse(Config.STATUS_200, Config.MSG_200, jsonObject, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addPageResponse(e.getCode(), e.getMessage(), null,
					Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 
	 * @Title: 

		nowledge
	 * @Description: 获取单个知识
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping(value = "getMyKnowledge")
	public @ResponseBody ResponseContainer getMyKnowledge(String id) {
		try {
			JSONObject jsonObject = knowledgeService.getMyKnowledgeAndUser(id);
			return addResponse(Config.STATUS_200, Config.MSG_200, jsonObject, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addPageResponse(e.getCode(), e.getMessage(), null,
					Config.RESP_MODE_10, "");
		}
	}

	/**
	 * 
	 * @Title: getKngByGroupId
	 * @Description:获取群组的知识列表
	 * @param groupId
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping(value = "getKnowledgeByGid")
	public @ResponseBody ResponseContainer getKngByGroupId(String groupId,QueryModel dm) {
		try {
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			List<GroupShareKnowledge> ks = groupshareKnowledgeService.getByGroupId(groupId);
		
			Page<Knowledge> result = knowledgeService.getKnowledgeList(ks,pageable);
			getReponseData().setResult(result);
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),
					Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), null,
					Config.RESP_MODE_10, "");
		}
	}

	/**
	 * 
	 * @Title: getPreTransKnowledge
	 * @Description:获取等待转码的一条知识
	 * @return
	 * @throws XueWenServiceException
	 *             ResponseContainer
	 * @throws
	 */
	@RequestMapping(value = "getPreTransKnowledge")
	public @ResponseBody ResponseContainer getPreTransKnowledge()
			throws XueWenServiceException {
		JSONObject knowledge = knowledgeService.getPreTransKnowledge();
		if(knowledge!=null){
			return addResponse(Config.STATUS_200, Config.MSG_200, knowledge,
					Config.RESP_MODE_10, "");
		}else{
			return addResponse(Config.STATUS_201, Config.MSG_201, knowledge,
					Config.RESP_MODE_10, "");
		}
		
	}

	/**
	 * 
	 * @Title: getTransData
	 * @Description: 获取等待转码的数据
	 * @return ResponseContainer
	 * @throws
	 */

	@RequestMapping(value = "getTransData")
	public @ResponseBody ResponseContainer getTransData() {
		return addResponse(Config.STATUS_200, Config.MSG_200,
				knowledgeService.getTransData(), Config.RESP_MODE_10, "");
	}
    
	/**
	 * 
	 * @Title: getTop10
	 * @Description: 根据热度获取top10
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping(value = "getTop10")
	public @ResponseBody ResponseContainer getTop10() {
		List<JSONObject> kngs = knowledgeService.gettop10();
		return addResponse(Config.STATUS_200, Config.MSG_200, kngs,
				Config.RESP_MODE_10, "");
	}

	/**
	 * 
	 * @Title: getUserKnowledge
	 * @Description: 获取用户上传的知识
	 * @param token
	 * @param dm
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping(value = "getUserKnowledge")
	public @ResponseBody ResponseContainer getUserKnowledge(String name,
			Long ctime, Long ltime, String token, QueryModel dm) {
		try {
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			User user = getCurrentUser(token);
			Page<Knowledge> ks = knowledgeService.getUserKnowledge(user.getId(),
					pageable, name, ctime, ltime);
			ReponseDataTools.getClientReponseData(getReponseData(), ks);
			if(getReponseData().getTotal_rows()==0){
				getReponseData().setResult(null);
			}
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),
					Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}

	}
	
	/**
	 * 
	 * @Title: getUserKnowledge
	 * @Description: 获取用户上传的知识
	 * @param token
	 * @param dm
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping(value = "getUserKnowledgeById")
	public @ResponseBody ResponseContainer getUserKnowledgeById(String userId,
			String name, Long ctime, Long ltime, String token, QueryModel dm) {

		Pageable pageable = PageRequestTools.pageRequesMake(dm);
		User user = userService.findOne(userId);
		Page<Knowledge> ks = knowledgeService.getUserKnowledge(user.getId(),
				pageable, name, ctime, ltime);
		ReponseDataTools.getClientReponseData(getReponseData(), ks);
		if (getReponseData().getTotal_rows() == 0) {
			getReponseData().setResult(null);
		}
		return addPageResponse(Config.STATUS_200, Config.MSG_200,
				getReponseData(), Config.RESP_MODE_10, "");
	}

	/**
	 * 
	 * @Title: modifyKng
	 * @Description: 修改知识
	 * @param knowledge
	 * @param citemList
	 * @return
	 * @throws XueWenServiceException
	 *             ResponseContainer
	 * @throws
	 */
	@RequestMapping(value = "modifyKng")
	public @ResponseBody ResponseContainer modifyKng(Knowledge knowledge,
			int modifyType, String token) {

		try {
			User user = getCurrentUser(token);
			Knowledge kng = knowledgeService.modifyKnowledge(knowledge,
					modifyType, user);
			return addResponse(Config.STATUS_200, Config.MSG_200, kng,
					Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		}

	}
	
	@RequestMapping("praiseKnowledgePc")
	public @ResponseBody ResponseContainer praiseKnowledge(HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String id=request.getParameter("id");
			knowledgeService.praiseKnowledgePc(id, currentUser);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("==========业务错误，分享点赞失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("==========未知错误，分享点赞失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	@RequestMapping("favKnowledgePc")
	public @ResponseBody ResponseContainer favKnowledgePc(HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String id=request.getParameter("id");
			knowledgeService.favKnowledgePc(id, currentUser.getId());
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("==========业务错误，分享收藏失败============"+e);
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		} catch (Exception e) {
			logger.error("==========未知错误，分享收藏失败============"+e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}

	/**
	 * 
	 * @Title: delKng
	 * @Description: 删除群知识
	 * @param gId
	 * @param kId
	 * @param token
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("delKng")
	public @ResponseBody ResponseContainer delKng(String gId, String kId,
			String token) {
		
		try {
			User user = getCurrentUser(token);
			knowledgeService.delGroupKng(gId, kId, user);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,
					Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		}

	}
	
//	/**
//	 * 
//	 * @Title: getKnowledgeByTagPc
//	 * @Description: 按标签查找分享（在群组中的分享）
//	 * @param tagName
//	 * @param dm
//	 * @return ResponseContainer
//	 * @throws
//	 */
//	@SuppressWarnings("unchecked")
//	@RequestMapping("getKnowledgeByTagPc")
//	public @ResponseBody ResponseContainer getKnowledgeByTagPc(String tagName,QueryModel dm){
//		Pageable pageable = PageRequestTools.pageRequesMake(dm);
//		try {
//			Map<String,Object> knowledges = knowledgeService.getKnowledgeByTagPc(tagName, pageable);
//			ReponseDataTools.getClientReponseData(getReponseData(), (Page<GroupCourse>)knowledges.get("page"));
//			getReponseData().setResult(knowledges.get("result"));
//			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(), Config.RESP_MODE_10, "");
//		} catch (XueWenServiceException e) {
//			return addResponse(e.getCode(), e.getMessage(), false, Config.RESP_MODE_10, "");
//		} catch (Exception e) {
//			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
//		}
//	}
	
	/**
	 * 
	 * @Title: getKnowledgeByKeyWordsPc
	 * @Description: 按条件查找分享（在群组中的分享）
	 * @param tagName
	 * @param dm
	 * @return ResponseContainer
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("getKnowledgeByKeyWordsPc")
	public @ResponseBody ResponseContainer getKnowledgeByKeyWordsPc(String keyWords,QueryModel dm){
		Pageable pageable = PageRequestTools.pageRequesMake(dm);
		try {
			Map<String,Object> knowledges = knowledgeService.getKnowledgeByKeyWordsPc(keyWords, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), (Page<Knowledge>)knowledges.get("page"));
			getReponseData().setResult(knowledges.get("result"));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(), Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 
	 * @Title: addViewCount
	 * @Description:浏览次数加1 
	 * @param kngId
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("addViewCount")
	public @ResponseBody ResponseContainer addViewCount(String kngId){
		try {
			knowledgeService.addViewCount(kngId);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,
					Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,
					Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 
	 * @Title: searchAllPublicKnowledge
	 * @Description: 搜索所有公开知识
	 * @param keyword
	 * @param dm
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("searchAllPublicKnowledge")
	public @ResponseBody ResponseContainer searchAllPublicKnowledge(
			String keyword, QueryModel dm) {
		Pageable pageable = PageRequestTools.pageRequesMake(dm);
		Page<Knowledge> kngs = knowledgeService.searchAllPublicKnowledge(
				keyword, pageable);
		ReponseDataTools.getClientReponseData(getReponseData(), kngs);
		return addPageResponse(Config.STATUS_200, Config.MSG_200,
				getReponseData(), Config.RESP_MODE_10, "");
	}
	/**
	 * 
	 * @Title: searchUserKnowledge
	 * @Description:搜索用户知识
	 * @param token
	 * @param keyword
	 * @param dm
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("searchUserKnowledge")
	public @ResponseBody ResponseContainer searchUserKnowledge(String token,
			String keyword, QueryModel dm) {
		try {
			User user = getCurrentUser(token);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<Knowledge> kngs = knowledgeService.searchUserKnowledge(keyword,
					pageable, user.getId());
			ReponseDataTools.getClientReponseData(getReponseData(), kngs);
			return addPageResponse(Config.STATUS_200, Config.MSG_200,
					getReponseData(), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(),
					false, Config.RESP_MODE_10, "");
		}
	}
	
	
	/**
	 * 
	 * @Title: getUserKnowledge
	 * @auther Tangli
	 * @Description: 获取用户正常状态的分享文库
	 * @param userId 用户Id
	 * @param dm
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("getUserKnowledges")
	public @ResponseBody ResponseContainer getUserKnowledge(String userId,
			QueryModel dm) {
		Pageable pageable = PageRequestTools.pageRequesMake(dm);
		Page<Knowledge> kngs;
		try {
			kngs = knowledgeService.findByUserIdAndCcodeAndStatus(userId,
					Config.KNOWLEDGE_CCODE_OK, Config.KNOWLEDGE_STAT_PASS,
					pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), kngs);
			if (kngs.getContent().size() == 0) {
				getReponseData().setResult(null);
			}
			return addPageResponse(Config.STATUS_200, Config.MSG_200,
					getReponseData(), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			logger.error("=========获取用户分享失败 非法参数============");
			return addResponse(e.getCode(), e.getMessage(), null,
					Config.RESP_MODE_10, "");
		}
	}
	@RequestMapping(value = "/delete")
	public @ResponseBody ResponseContainer delete(String token,HttpServletRequest request) {
		try {
			User user = getCurrentUser(token);
			String knowledgeIds = request.getParameter("knowledgeIds");
			return addResponse(Config.STATUS_200, Config.MSG_DELETE_200, knowledgeService.deleteByIds(knowledgeIds), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), false, Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false, Config.RESP_MODE_10, "");
		}
	}
	
}
