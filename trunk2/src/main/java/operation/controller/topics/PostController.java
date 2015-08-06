package operation.controller.topics;

import javax.servlet.http.HttpServletRequest;

import operation.BaseController;
import operation.exception.XueWenServiceException;
import operation.pojo.pub.QueryModel;
import operation.pojo.topics.Post;
import operation.pojo.topics.SubPost;
import operation.pojo.user.User;
import operation.service.topics.PostService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tools.Config;
import tools.PageRequestTools;
import tools.ReponseDataTools;
import tools.ResponseContainer;
@RestController
@RequestMapping("/post")
public class PostController extends BaseController{
	@Autowired
	private PostService postService;
	
	/**
	 * 回复主楼
	 * @param request
	 * @param file
	 * @return
	 */
	@RequestMapping(value="replyTopic",method = RequestMethod.POST)
	public @ResponseBody ResponseContainer replyTopic(HttpServletRequest request,Post post) {
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			String image = request.getParameter("image");
			Post postResult = postService.replyTopic(currentUser,post,image);
			return addResponse(Config.STATUS_200, Config.MSG_200, postResult,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}	
	}
	
	/**
	 * 回复付楼
	 * @param request
	 * @param file
	 * @return
	 */
	@RequestMapping(value="replyPost",method = RequestMethod.POST)
	public @ResponseBody ResponseContainer replyPost(HttpServletRequest request,SubPost subPost) {
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			SubPost subPostResult = postService.replyPost(currentUser,subPost);
			return addResponse(Config.STATUS_200, Config.MSG_200, subPostResult,Config.RESP_MODE_10, "");
		}catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}	
	}
	/**
	 * 查询主楼回复信息
	 * @param dm
	 * @param request
	 * @return
	 */
	@RequestMapping("findPost")
	public ResponseContainer findPost(QueryModel dm,HttpServletRequest request){
		try {
            String topicId = request.getParameter("topicId");
            String token = request.getParameter("token");
            User currentUser = this.getCurrentUser(token);
			//根据请求参数封装一个分页信息对象
            dm.setMode("ASC");
            Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<Post> postList = postService.getTopicPost(topicId, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), postList);
			this.getReponseData().setResult(postService.toPostResponseList(currentUser.getId(), postList.getContent()));
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
	 * 查询副楼回复信息
	 * @param dm
	 * @param request
	 * @return
	 */
	@RequestMapping("findSubPost")
	public ResponseContainer findSubPost(QueryModel dm,HttpServletRequest request){
		try {
            String postId = request.getParameter("postId");
			//根据请求参数封装一个分页信息对象
            dm.setMode("ASC");
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<SubPost> subPostList = postService.getSubPost(postId, pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), subPostList);
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
	 * 主题赞
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping("/{id}/like")
	public @ResponseBody ResponseContainer like(@PathVariable("id") String id,HttpServletRequest request) {
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			Post post = postService.addPraise(currentUser,id);
			return addResponse(Config.STATUS_200, Config.MSG_200, post,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}	
	}
	
	/**
	 * 主题赞
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping("/praisePostPc")
	public @ResponseBody ResponseContainer praisePostPc(HttpServletRequest request) {
		String token = request.getParameter("token");
		String id = request.getParameter("id");
		try {
			User currentUser = this.getCurrentUser(token);
			Post post = postService.addPraisePc(currentUser,id);
			return addResponse(Config.STATUS_200, Config.MSG_200, post, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), false, Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false, Config.RESP_MODE_10, "");
		}	
	}

	/**
	 * 回复干货主楼
	 * @param request
	 * @param file
	 * @return
	 */
	@RequestMapping(value="replyDrycargo",method = RequestMethod.POST)
	public @ResponseBody ResponseContainer replyDrycargo(HttpServletRequest request,Post post) {
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			String groupId = request.getParameter("sourceId");
			Post postResult = postService.replyDrycargo(currentUser,post,groupId);
			return addResponse(Config.STATUS_200, Config.MSG_200, postResult,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}	
	}
	
	/**
	 * 查询干货主楼回复信息
	 * @param dm
	 * @param request
	 * @return
	 */
	@RequestMapping("findDrycargoPost")
	public ResponseContainer findDrycargoPost(QueryModel dm,HttpServletRequest request){
		try {
            String dryCargoId = request.getParameter("dryCargoId");
            String token = request.getParameter("token");
            User currentUser = this.getCurrentUser(token);
            String groupId = request.getParameter("sourceId");
			//根据请求参数封装一个分页信息对象
            dm.setMode("AES");
			Pageable pageable = PageRequestTools.pageRequesMake(dm);
			Page<Post> postList = postService.getDrycargoPost(dryCargoId, groupId,pageable);
			ReponseDataTools.getClientReponseData(getReponseData(), postList);
			this.getReponseData().setResult((postService.toPostResponseList(currentUser.getId(), postList.getContent())));
			return addPageResponse(Config.STATUS_200, Config.MSG_200, getReponseData(),Config.RESP_MODE_10, "");
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
	 * 获取主楼
	 * @param request
	 * @param file
	 * @return
	 */
	@RequestMapping(value="findOneById")
	public @ResponseBody ResponseContainer findOneById(HttpServletRequest request,String postId) {
		try {
			Post postResult = postService.findOneById(postId);
			return addResponse(Config.STATUS_200, Config.MSG_200, postResult,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}	
	}
	/**
	 * 
	 * @Title: deleteByPostId
	 * @Description: 删除主楼回复
	 * @param request
	 * @param topicId
	 * @param postId
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping(value="deleteByPostId")
	public @ResponseBody ResponseContainer deleteByPostId(HttpServletRequest request,String topicId,String postId) {
		try {
			postService.deletePostByPostId(topicId, postId);
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
	 * 删除副楼回复
	 * @Title: deleteBySubPostId
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param request
	 * @param subPostId
	 * @param postId
	 * @return ResponseContainer
	 * @throws
	 */
	@RequestMapping(value="deleteBySubPostId")
	public @ResponseBody ResponseContainer deleteBySubPostId(HttpServletRequest request,String subPostId,String postId) {
		try {
			postService.deleteSubPostBySubPostId(subPostId, postId);
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
	 * 回复主楼
	 * @param request
	 * @param file
	 * @return
	 */
	@RequestMapping(value="replySubject",method = RequestMethod.POST)
	public @ResponseBody ResponseContainer replySubject(HttpServletRequest request,Post post) {
		String token = request.getParameter("token");
		try {
			User currentUser = this.getCurrentUser(token);
			String image = request.getParameter("image");
			Post postResult = postService.replySubject(currentUser,post,image);
			return addResponse(Config.STATUS_200, Config.MSG_200, postResult,Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			e.printStackTrace();
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			e.printStackTrace();
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}	
	}
	
}
