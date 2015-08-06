package operation.controller.oss;

import java.util.List;

import net.sf.json.JSONObject;
import operation.BaseController;
import operation.exception.XueWenServiceException;
import operation.pojo.activity.Activity;
import operation.pojo.category.Category;
import operation.pojo.course.NewGroupCourse;
import operation.pojo.drycargo.Drycargo;
import operation.pojo.group.XueWenGroup;
import operation.pojo.pub.QueryModel;
import operation.pojo.topics.Topic;
import operation.service.ossRecomend.OssRecomendService;
import operation.service.topics.TopicService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tools.Config;
import tools.PageRequestTools;
import tools.ReponseDataTools;
import tools.ResponseContainer;


/**
 * 
* @ClassName: OssIndexController
* @Description: 运营平台运维数据管理
* @author tangli
* @date 2015年3月2日 下午5:16:36
*
 */
@RestController
@RequestMapping("/oss/recomend")
public class OssRecomendController extends BaseController {
	@Autowired
	private OssRecomendService ossRecomendService;
	@Autowired
	private TopicService topicService;
	
	/**
	 * 
	 * @Title: getBoxPostByName
	 * @auther Tangli
	 * @Description: 取首页干货内容
	 * @param name
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("getRecomendDry")
	public @ResponseBody ResponseContainer getRecomendDry(String name,QueryModel dm){
		try {
			Pageable pageable=PageRequestTools.pageRequesMake(dm);
			Page<Drycargo> drys=ossRecomendService.findDry(name,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(),drys);
			List<JSONObject>objs=ossRecomendService.shutDown(drys);
			getReponseData().setResult(objs);
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(Config.STATUS_201, Config.MSG_201, null, Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 
	 * @Title: getBoxPostByName
	 * @auther Tangli
	 * @Description: 取首页群组内容
	 * @param name
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("getRecomendGroup")
	public @ResponseBody ResponseContainer getRecomendGroup(String name,QueryModel dm){
		try {
			Pageable pageable=PageRequestTools.pageRequesMake(dm);
			Page<XueWenGroup>groups=ossRecomendService.findGroups(name,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), groups);
			List<JSONObject>objs=ossRecomendService.shutDownGroups(groups);
			getReponseData().setResult(objs);
			return addPageResponse(Config.STATUS_200, Config.MSG_200,getReponseData(), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addPageResponse(Config.STATUS_201, Config.MSG_201, null, Config.RESP_MODE_10, "");

		}
	}
	
	
	/**
	 * 
	 * @Title: getBoxPostByName
	 * @auther Tangli
	 * @Description: 取首页话题内容
	 * @param name
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("getRecomendTopic")
	public @ResponseBody ResponseContainer getRecomendTopic(String name,QueryModel dm){
		try {
			Pageable pageable=PageRequestTools.pageRequesMake(dm);
			Page<Topic>topics=ossRecomendService.findTopics(name ,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), topics);
			List<JSONObject> res=topicService.shoutlistforpc(topics.getContent());
			getReponseData().setResult(res);
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(Config.STATUS_201, Config.MSG_201, null, Config.RESP_MODE_10, "");

		}
	}
	
	

	/**
	 * 
	 * @Title: getBoxPostByName
	 * @auther Tangli
	 * @Description: 取首页课程内容
	 * @param name
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("getRecomendCourse")
	public @ResponseBody ResponseContainer getRecomendCourse(String name,QueryModel dm){
		try {
			Pageable pageable=PageRequestTools.pageRequesMake(dm);
			Page<NewGroupCourse>res=ossRecomendService.findCourse(name,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), res);
			List<JSONObject>objects=ossRecomendService.shoutDownCourse(res.getContent());
			getReponseData().setResult(objects);
			return addPageResponse(Config.STATUS_200, Config.MSG_200,getReponseData(), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(Config.STATUS_201, Config.MSG_201, null, Config.RESP_MODE_10, "");
		}
	}
	
	
	/**
	 * 
	 * @Title: getRecomendActivity
	 * @auther shenbin
	 * @Description: 取活动列表
	 * @param name
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("getRecomendActivity")
	public @ResponseBody ResponseContainer getRecomendActivity(String name){
		try {
			List<Activity> activities=ossRecomendService.findActivity(name);
			return addResponse(Config.STATUS_200, Config.MSG_200, activities, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(Config.STATUS_201, Config.MSG_201, null, Config.RESP_MODE_10, "");
		}
	}
	/**
	 * 
	 * @Title: getRecomendCategory
	 * @auther shenbin
	 * @Description: 小组首页的分类列表
	 * @param name
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping("getRecomendCategory")
	public @ResponseBody ResponseContainer getRecomendCategory(String name){
		try {
			List<JSONObject> categorys=ossRecomendService.getCategoryJson(name);
			return addResponse(Config.STATUS_200, Config.MSG_200, categorys, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(Config.STATUS_201, Config.MSG_201, null, Config.RESP_MODE_10, "");
		}
	}
	
	
	
}
