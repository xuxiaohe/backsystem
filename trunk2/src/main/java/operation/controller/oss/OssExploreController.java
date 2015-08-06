package operation.controller.oss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import operation.BaseController;
import operation.exception.XueWenServiceException;
import operation.pojo.box.Box;
import operation.pojo.course.GroupCourse;
import operation.pojo.course.NewCourse;
import operation.pojo.course.NewGroupCourse;
import operation.pojo.drycargo.Drycargo;
import operation.pojo.dynamic.GroupDynamic;
import operation.pojo.group.XueWenGroup;
import operation.pojo.index.Explore;
import operation.pojo.index.IndexBean;
import operation.pojo.index.NewIndexBean;
import operation.pojo.pub.QueryModel;
//import operation.pojo.drycargo.DrycargoBean;
import operation.pojo.pub.QueryModelMul;
import operation.pojo.topics.Topic;
import operation.pojo.user.User;
import operation.service.box.BoxService;
import operation.service.course.CourseService;
import operation.service.course.GroupCourseService;
import operation.service.course.NewCourseService;
import operation.service.course.NewGroupCourseService;
//import operation.service.drycargo.DrycargoBeanService;
import operation.service.drycargo.DrycargoService;
import operation.service.dynamic.GroupDynamicService;
import operation.service.group.GroupService;
import operation.service.group.MyGroupService;
import operation.service.index.IndexService;
import operation.service.ossRecomend.ExploreService;
import operation.service.topics.TopicService;

import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tools.Config;
import tools.PageRequestTools;
import tools.ReponseDataTools;
import tools.ResponseContainer;
import tools.YXTJSONHelper;


@RestController
@RequestMapping("/oss/exploreoss")
@Configuration
public class OssExploreController extends BaseController{
	
	private static final Logger logger=Logger.getLogger(OssExploreController.class);
	public OssExploreController() {
		super();
	}
	@Autowired
	private ExploreService exploreService;
	
	@Autowired
	private BoxService boxService;
	
	@Autowired
	private NewCourseService newCourseService;
	@Autowired
	private NewGroupCourseService newGroupCourseService;
	@Autowired
	private GroupService GroupService;
	
	/**
	 * 探索页面
	 * @param request
	 * @param dm
	 * @return
	 */
	@RequestMapping("findExplore")
	public @ResponseBody ResponseContainer findTopic(HttpServletRequest request,QueryModelMul dm) {
		try {
			String token = request.getParameter("token");
			User currentUser = this.getCurrentUser(token);
			
			JSONObject explore = exploreService.findExplore(currentUser,String.valueOf(dm.getN()));
			Explore exp = null;
			if(explore != null){
				JSONArray result=explore.getJSONArray("result");
				if(result != null){
					result=result.fromObject(result,YXTJSONHelper.initJSONObjectConfig());
					List ls=JSONArray.toList(result);
					System.out.println(result.toString());
					exp = exploreService.doNewIndex(result,currentUser,ls,dm.getN());
					
				}
			}
			this.getReponseData().setResult(exp);	
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
	 * 查询某一位置的盒子信息
	 * @param request
	 * @param dm
	 * @return
	 * @throws XueWenServiceException
	 */
	@RequestMapping("/findBoxById")
	public @ResponseBody ResponseContainer findBoxById(HttpServletRequest request,QueryModel dm) throws XueWenServiceException {
		try {
//			String token = request.getParameter("token");
//			User currentUser = this.getCurrentUser(token);
			String boxPostId=request.getParameter("boxPostId");
			String dataType = request.getParameter("type");
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<Box> boxList = boxService.findByBoxPostId(boxPostId, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), boxList);
			if("onecoursespecial".equals(dataType)){
				List l=new ArrayList();
				
				for(Box b:boxList){
					Map m=new HashMap();
					NewCourse c=newCourseService.findOne(b.getSourceId().toString());
					NewGroupCourse n=newGroupCourseService.findNewGroupCourseByCourseIdAndGroupId(b.getSourceId().toString(),b.getGroupid());
					XueWenGroup x=GroupService.findById(b.getGroupid());
					m.put("course", c);
					m.put("groupid", b.getGroupid());
					m.put("groupCourseid", n.getId());
					m.put("groupname",x.getGroupName());
					m.put("grouplogo", x.getLogoUrl());
					m.put("boxid", b.getId());
					l.add(m);
				}
				List ll=new ArrayList();
//				Map m=(Map)l.get(0);
//				ll.add(m);
				for(int i=0;i<l.size();){
					Map mm=(Map)l.get(i);
					 
					for(int j=1;j<l.size();j++){
						Map m1=(Map)l.get(j);
						if(mm.get("groupid").equals(m1.get("groupid"))){
							ll.add(mm);
							ll.add(m1);
							l.remove(j);
							break;
						}
					}
					 l.remove(i);
				}
				
				return addResponse(Config.STATUS_200, Config.MSG_200, ll,Config.RESP_MODE_10, "");
			}
			else{
			this.getReponseData().setResult((boxService.formatForData_3(boxList.getContent(),dataType,boxPostId)));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,
					Config.RESP_MODE_10, "");
		}
	}
	
	

}
