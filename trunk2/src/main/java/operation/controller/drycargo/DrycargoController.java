package operation.controller.drycargo;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;
import operation.BaseController;
import operation.exception.XueWenServiceException;
import operation.pojo.drycargo.Drycargo;
//import operation.pojo.drycargo.DrycargoBean;
//import operation.pojo.drycargo.DrycargoBeanResponse;
import operation.pojo.drycargo.DrycargoResponse;
import operation.pojo.pub.QueryModel;
import operation.pojo.pub.QueryModelMul;
import operation.pojo.topics.Images;
import operation.pojo.topics.Topic;
import operation.pojo.user.User;
//import operation.service.drycargo.DrycargoBeanService;
import operation.service.drycargo.DrycargoService;

import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tools.Config;
import tools.JSON2ObjUtil;
import tools.PageRequestTools;
import tools.ReponseDataTools;
import tools.ResponseContainer;

@RestController
@RequestMapping("/drycargo")
public class DrycargoController extends BaseController {

	private static final Logger logger = Logger.getLogger(DrycargoController.class);
	@Autowired
	public DrycargoService drycargoService;
	/**
	 * 上传干货信息（2015-1-26去掉干货池对象）
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping("uploadDrycargo")
	public @ResponseBody ResponseContainer uploadDrycargo(HttpServletRequest request, Drycargo drycargo) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String tagName = request.getParameter("tagName");
			String isOriginal = request.getParameter("isOriginal");
			String image = request.getParameter("image");
			// 非空校验
			if (StringUtil.isBlank(drycargo.getUrl()) || currentUser == null) {
				return addResponse(Config.STATUS_500, Config.MSG_500, false,Config.RESP_MODE_10, "");
			}
			Drycargo db = drycargoService.uploadDry(currentUser, drycargo,tagName,isOriginal,image);
			return addResponse(Config.STATUS_200, Config.MSG_200, db,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============" + e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============" + e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 根据群组查询干货列表Pc
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("allPc")
	public @ResponseBody ResponseContainer allPc(HttpServletRequest request,String keyWords,QueryModelMul dm) {
		
		try {
			String groupId = request.getParameter("groupId");
			if(StringUtil.isBlank(keyWords)||"null".equals(keyWords)){
				keyWords="";
				
			}
			List<String> sort = new ArrayList<String>();
			sort.add("displayOrder");
			sort.add("displayTime");
			sort.add("ctime");
			dm.setSort(sort);
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<Drycargo> dryCargoResult= drycargoService.allPc(groupId, keyWords, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), dryCargoResult);
			this.getReponseData().setResult(dryCargoResult.getContent());
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
	 * 根据群组查询干货列表
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("all")
	public @ResponseBody ResponseContainer all(HttpServletRequest request,QueryModelMul dm) {
		// 根据请求参数封装一个分页信息对象
		List<String> sort = new ArrayList<String>();
		//sort.add("weightSort");
		sort.add("displayOrder");
		sort.add("displayTime");
		sort.add("ctime");
		sort.add("replyCount");
		dm.setSort(sort);
		Pageable pageable = PageRequestTools.pageRequesMake(dm);
		Page<Drycargo> dryCargoResult;
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String keywords = request.getParameter("groupId");
			String dryFlag = request.getParameter("dryFlag");//0干货1炫页
			if(StringUtil.isBlank(dryFlag)){
				dryFlag = "0";
			}
			dryCargoResult = drycargoService.all(keywords,Integer.parseInt(dryFlag), pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), dryCargoResult);
			this.getReponseData().setResult((drycargoService.toResponeses(dryCargoResult.getContent(),currentUser.getId())));
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
	 * 上传干货信息（2015-1-26去掉干货池对象）
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping("uploadDrycargoPc")
	public @ResponseBody ResponseContainer uploadDrycargoPc(
			HttpServletRequest request, Drycargo drycargo) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String tagName = request.getParameter("tagName");
			String isOriginal = request.getParameter("isOriginal");
			String image = request.getParameter("image");
			Drycargo db = drycargoService.uploadDryPc(currentUser, drycargo,tagName,isOriginal,image);
			return addResponse(Config.STATUS_200, Config.MSG_200, db,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============" + e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============" + e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 搜索干货
	 * @param request
	 * @param topic
	 * @param dm
	 * @return
	 */
	@RequestMapping("search")
	public @ResponseBody ResponseContainer search(HttpServletRequest request,Topic topic, QueryModelMul dm) {
		List<String> sort = new ArrayList<String>();
	//	sort.add("weightSort");
		sort.add("replyCount");
		sort.add("ctime");
		dm.setSort(sort);
		Pageable pageable = PageRequestTools.pageRequesMake(dm);
		Page<Drycargo> dryCargoResult;
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String keywords = request.getParameter("keywords");
			String dryFlag = request.getParameter("dryFlag");//0干货1炫页
			if(StringUtil.isBlank(dryFlag)){
				dryFlag = "0";
			}
			dryCargoResult = drycargoService.searchByDryFlag(keywords,Integer.parseInt(dryFlag), pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), dryCargoResult);
			this.getReponseData().setResult((drycargoService.toResponeses(dryCargoResult.getContent(),currentUser.getId())));
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
	 * 查询干货详情（从小组课堂查看干货详情）
	 * @param request
	 * @return
	 */
	@RequestMapping("/groupDryDetail")
	public @ResponseBody ResponseContainer groupDryDetail(HttpServletRequest request) {
		try {
		String token = request.getParameter("token");
		User currentUser = this.getCurrentUser(token);
		String dryCargoId = request.getParameter("dryCargoId");//干货id
		String groupId = request.getParameter("groupId");
		DrycargoResponse db = drycargoService.dryDetail(currentUser,dryCargoId,groupId);
		return addResponse(Config.STATUS_200, Config.MSG_200, db,Config.RESP_MODE_10, "");
		}catch (XueWenServiceException e) {
			e.printStackTrace();
			logger.error("==========业务错误，根据群组ID查询群成员列表失败============" + e);
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("==========未知错误，根据群组ID查询群成员列表失败============" + e);
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 干货赞
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping("like")
	public @ResponseBody ResponseContainer like(HttpServletRequest request) {
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			String dryCargoId = request.getParameter("dryCargoId");
			String groupId = request.getParameter("groupId");
			drycargoService.dryCargoAddParise(currentUser,dryCargoId,groupId);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
			}
			catch (XueWenServiceException e) {
				e.printStackTrace();
				return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
			} catch (Exception e) {
				e.printStackTrace();
				return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
			}	
	}
	
	/**
	 * 干货赞Pc
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping("praiseDryCargoPc")
	public @ResponseBody ResponseContainer praiseDryCargoPc(HttpServletRequest request) {
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			String dryCargoId = request.getParameter("id");
			String groupId = request.getParameter("groupId");
			drycargoService.dryCargoAddParisePc(currentUser,dryCargoId,groupId);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
			}
			catch (XueWenServiceException e) {
				e.printStackTrace();
				return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
			} catch (Exception e) {
				e.printStackTrace();
				return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
			}
	}
	
	/**
	 * 干货不赞
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping("unlike")
	public @ResponseBody ResponseContainer unlike(HttpServletRequest request) {
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			String dryCargoId = request.getParameter("dryCargoId");
			String groupId = request.getParameter("groupId");
			drycargoService.dryCargoAddUnParise(currentUser,dryCargoId,groupId);
			return addResponse(Config.STATUS_200, Config.MSG_200, true,Config.RESP_MODE_10, "");
			}
			catch (XueWenServiceException e) {
				e.printStackTrace();
				return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
			} catch (Exception e) {
				e.printStackTrace();
				return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
			}	
	}
	
	/**
	 * 分享干货
	 * @param request
	 * @return
	 */
	@RequestMapping("/shareDryCargo")
	public @ResponseBody ResponseContainer shareCourse(HttpServletRequest request) {

		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String fromGroupId=request.getParameter("fromGroupId");
			String toGroupId=request.getParameter("toGroupId");
			String dryCargoId=request.getParameter("dryCargoId"); //干货ID
			String appkey = request.getParameter("appKey");
			String toType = request.getParameter("toType");
			String toAddr = request.getParameter("toAddr");
			drycargoService.shareDryCargo(fromGroupId, dryCargoId, toGroupId, currentUser,appkey,toType,toAddr);
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
	 * 分享干货批量
	 * @param request
	 * @return
	 */
	@RequestMapping("/shareDryCargos")
	public @ResponseBody ResponseContainer shareDryCargos(HttpServletRequest request) {

		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String group=request.getParameter("groupId");
			String dryCargoIds=request.getParameter("dryCargoIds"); //干货IDs
			String appkey = request.getParameter("appKey");
			String toType = request.getParameter("toType");
			String toAddr = request.getParameter("toAddr");
			drycargoService.shareDryCargosToGroup(group, dryCargoIds, currentUser, appkey, toType, toAddr);
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
	 * 收藏干货
	 * @param request
	 * @return
	 */
	@RequestMapping("/favDrycargo")
	public @ResponseBody ResponseContainer favDrycargo(HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String fromGroupId=request.getParameter("fromGroupId");
			String dryCargoId=request.getParameter("dryCargoId"); //干货ID
			String appkey = request.getParameter("appKey");//新增appkey如 ios，android，pc,oss
			String dryFlag = request.getParameter("dryFlag");
			if(StringUtil.isBlank(dryFlag)){
				dryFlag = "0";
			}
			drycargoService.favDrycargo(fromGroupId, dryCargoId, currentUser,appkey,Integer.parseInt(dryFlag));
			return addResponse(Config.STATUS_200, Config.MSG_FAV_200, true,Config.RESP_MODE_10, "");
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
	 * 收藏干货pc
	 * @param request
	 * @return
	 */
	@RequestMapping("/favDrycargoPc")
	public @ResponseBody ResponseContainer favDrycargoPc(HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String fromGroupId=request.getParameter("groupId");
			String dryCargoId=request.getParameter("id"); //干货ID
			String dryFlag = request.getParameter("dryFlag");
			if(StringUtil.isBlank(dryFlag)){
				dryFlag = "0";
			}
			drycargoService.favDrycargo(fromGroupId, dryCargoId, currentUser,Config.APPKEY_PC,Integer.parseInt(dryFlag));
			return addResponse(Config.STATUS_200, Config.MSG_FAV_200, true,Config.RESP_MODE_10, "");
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
	 * 获得用户创建的干货列表
	 * 
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("userDrycargo")
	public @ResponseBody ResponseContainer userDrycargo(HttpServletRequest request,QueryModelMul dm) {
		List<String> sort = new ArrayList<String>();
		sort.add("weightSort");
		sort.add("ctime");
		sort.add("replyCount");
		dm.setSort(sort);
		Pageable pageable = PageRequestTools.pageRequesMake(dm);
		Page<Drycargo> dryCargoResult;
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String userId = request.getParameter("userId");
			String dryFlag = request.getParameter("dryFlag");//0干货1炫页
			if(StringUtil.isBlank(dryFlag)){
				dryFlag = "0";
			}
			dryCargoResult = drycargoService.getUserCreateDrycargoByDryFlag(userId,Integer.parseInt(dryFlag), pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), dryCargoResult);
			this.getReponseData().setResult((drycargoService.toResponeses(dryCargoResult.getContent(),currentUser.getId())));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 
	 * @Title: getTop10
	 * @Description: 获取top10干货
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("gettop10")
	public @ResponseBody ResponseContainer getTop10(int s){	  
		List<JSONObject> drys=drycargoService.getTop10(s);
		return addResponse(Config.STATUS_200, Config.MSG_200, drys,Config.RESP_MODE_10, "");
	}   
	
	/**
	 * 
	 * @Title: searchDry
	 * @Description:干货搜索
	 * @param keywords
	 * @param dm
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("searchDry")
	public @ResponseBody ResponseContainer searchDry(String keywords,QueryModel dm){
		Pageable pageable= PageRequestTools.pageRequesMake(dm);
		 try {
			Page<Drycargo> drys= drycargoService.searchByKeyWordsAndTagNamesLike(keywords, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), drys);
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		}
		
	}
	
	/**
	 * 删除
	 * 
	 * @param request
	 * @return
	 * @throws
	 * @throws IOException
	 */
	@RequestMapping("deleteDry")
	public @ResponseBody ResponseContainer deleteDry(HttpServletRequest request) {
		try {
			String dryCargoId = request.getParameter("dryCargoId");
			return addResponse(Config.STATUS_200, Config.MSG_DELETE_200, drycargoService.deleteById(dryCargoId), Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false, Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 更新干货
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/updateOne")
	public @ResponseBody ResponseContainer updateOne(HttpServletRequest request) throws XueWenServiceException {
		try {
			String dryCargoId = request.getParameter("dryCargoId");
			String fileUrl = request.getParameter("fileUrl");
			String groupid = request.getParameter("groupid");
			String tagName = request.getParameter("tagName");
			String height = request.getParameter("height");
			String width = request.getParameter("width");
			//String categoryId = request.getParameter("categoryId");
			//String childCategoryId = request.getParameter("childCategoryId");
			if (fileUrl != null) {
				fileUrl = URLDecoder.decode(fileUrl, "utf-8");
			}
			String message = request.getParameter("message");
			String description = request.getParameter("description");
			String image = request.getParameter("image");
			String context = request.getParameter("context");
			Drycargo db = drycargoService.findOneById(dryCargoId);
			//db.setCategoryId(categoryId);
			//db.setChildCategoryId(childCategoryId);
			if(!StringUtil.isBlank(fileUrl)){
				db.setFileUrl(fileUrl);
			}
			if(!StringUtil.isBlank(message)){
			db.setMessage(message);
			}
			if(!StringUtil.isBlank(description)){
			db.setDescription(description);
			}
			if(!StringUtil.isBlank(tagName)){	
			db.setDrycargoTagName(tagName);
			}
			if(!(StringUtil.isBlank(height))&&!(StringUtil.isBlank(width))){
				db.setPicHeight(Float.parseFloat(height));
				db.setPicWidth(Float.parseFloat(width));
			}
			if(!StringUtil.isBlank(context)){
				db.setContext(context);
			}
			
			if(!StringUtil.isBlank(image)){
				List<Images> imageList = JSON2ObjUtil.getDTOList(image, Images.class);
				if(imageList!=null && imageList.size() > 0){
					db.setImages(imageList);
				}
			}

			return addResponse(Config.STATUS_200, Config.MSG_200, drycargoService.updateDrycargo(db,tagName), Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false, Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 干货置顶或取消置顶
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/display")
	public @ResponseBody ResponseContainer display(HttpServletRequest request) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			String topicId = request.getParameter("dryCargoId");
			String disType = request.getParameter("disType"); // 1 置顶 0 取消置顶
			DrycargoResponse drycargo = drycargoService.display(topicId,currentUser,disType);
			if(Config.DISPALY == Integer.parseInt(disType)){
				return addResponse(Config.STATUS_200, Config.MSG_DISPALY_200, drycargo, Config.RESP_MODE_10, "");
			}else{
				return addResponse(Config.STATUS_200, Config.MSG_NODISPALY_200, drycargo, Config.RESP_MODE_10, "");
			}
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), false, Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false, Config.RESP_MODE_10, "");
		}
	}
	@RequestMapping(value = "/saveReplyCount")
	public @ResponseBody ResponseContainer saveReplyCount(HttpServletRequest request) {
		try {
			String sourceId = request.getParameter("sourceId");
			String num = request.getParameter("num");
			drycargoService.saveReplyCount(sourceId, Integer.valueOf(num));
			return addResponse(Config.STATUS_200, Config.MSG_NODISPALY_200, true, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), false, Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false, Config.RESP_MODE_10, "");
		}
	}
	
}


