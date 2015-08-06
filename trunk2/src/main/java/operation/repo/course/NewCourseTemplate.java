package operation.repo.course;

import java.util.List;

import operation.exception.XueWenServiceException;
import operation.pojo.course.Knowledge;
import operation.pojo.course.NewCourse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import tools.StringUtil;


/**
 * 课程实例 template 操作 
 * @author hjn
 *
 */
@Service
@Component
public class NewCourseTemplate {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	public NewCourseTemplate(){
		super();
	}
	
	/**
	 * 根据课程ID返回课程基本信息（只有，ID，title，intro,tags，logoUrl）
	 * @author hjn
	 * @param courseId
	 * @return
	 * @throws XueWenServiceException
	 */
	public NewCourse findOneCourseBasicInfo(String courseId)throws XueWenServiceException{
		Query query=new Query(Criteria.where("id").is(courseId));
		query.fields().include("id").include("title").include("intro").include("tags").include("logoUrl").include("categoryId")
		.include("childCategoryId").include("price").include("createUser").include("createUserName").include("userLogo").
		include("pricemodel").include("favProp").include("buyCount").include("postCount");
		return mongoTemplate.findOne(query, NewCourse.class);
	}
	/**
	 * 根据课程ID返回课程基本信息包括chapter集合（只有，ID，title，intro,tags，logoUrl,chapter）
	 * @author hjn
	 * @param courseId
	 * @return
	 * @throws XueWenServiceException
	 */
	public NewCourse findOneCourseBasicInfoIncludeChapter(String courseId)throws XueWenServiceException{
		Query query=new Query(Criteria.where("id").is(courseId));
		query.fields().include("id").include("title").include("intro").include("tags").include("logoUrl").include("chapters").include("categoryId")
		.include("childCategoryId").include("price").include("createUser").include("createUserName").include("userLogo").include("pricemodel").
		include("favProp").include("buyCount").include("postCount");
		return mongoTemplate.findOne(query, NewCourse.class);
	}
	/**
	 * 根据课程ID返回课程ID和chapters节点（只有，ID，chapters）
	 * @author hjn
	 * @param courseId
	 * @return
	 * @throws XueWenServiceException
	 */
	public NewCourse findOneNewCourseByIdRspOnlyIdAndChapters(String courseId)throws XueWenServiceException{
		Query query=new Query(Criteria.where("id").is(courseId));
		query.fields().include("id").include("chapters");
		return mongoTemplate.findOne(query, NewCourse.class);
	}
	
	/**
	 * 增加课程收藏统计数量
	 * @param courseId
	 * @return
	 * @throws XueWenServiceException
	 */
	public void increaseFavCount(String courseId,int increaseNum)throws XueWenServiceException{
		Query query=new Query(Criteria.where("id").is(courseId));
		Update update=new Update();
		update.inc("favCount", increaseNum);
		mongoTemplate.updateMulti(query, update, NewCourse.class);
	}
	/**
	 * 增加课程收藏统计数量
	 * @param courseId
	 * @return
	 * @throws XueWenServiceException
	 */
	public void increaseStudyCount(String courseId,int increaseNum)throws XueWenServiceException{
		Query query=new Query(Criteria.where("id").is(courseId));
		Update update=new Update();
		update.inc("studyCount", increaseNum);
		mongoTemplate.updateMulti(query, update, NewCourse.class);
	}
	/**
	 * 增加课程收藏统计数量
	 * @param courseId
	 * @return
	 * @throws XueWenServiceException
	 */
	public void increaseShareCount(String courseId,int increaseNum)throws XueWenServiceException{
		Query query=new Query(Criteria.where("id").is(courseId));
		Update update=new Update();
		update.inc("shareCount", increaseNum);
		mongoTemplate.updateMulti(query, update, NewCourse.class);
	}
	/**
	 * 搜索课程
	 * @param courseId
	 * @return
	 * @throws XueWenServiceException
	 */
	public List<NewCourse> searchByTitelAndIntro(String keyword)throws XueWenServiceException{
		if(StringUtil.isBlank(keyword)){
			Query query=new Query(Criteria.where("checked").is(true));
			//默认选10个
			query.limit(10);
			query.fields().include("id");
			return mongoTemplate.find(query,NewCourse.class);
		}else{
			Query query=new Query();
			Criteria cr = new Criteria();
			query.addCriteria(Criteria.where("checked").is(true));
			query.addCriteria(cr.orOperator(
			    Criteria.where("intro").regex(keyword)
			    ,Criteria.where("title").regex(keyword)
			));
			query.fields().include("id");
			return mongoTemplate.find(query,NewCourse.class);
		}
		
	}
	
	/**
	 * 根据课程Id删除课程记录
	 * @param courseId
	 * @throws XueWenServiceException
	 */
	public void deleteById(String courseId)throws XueWenServiceException{
		Query query=new Query(Criteria.where("id").is(courseId));
		mongoTemplate.remove(query, NewCourse.class);
	}
	
    /**
     * 
     * @Title: addViewCount
     * @Description: 根据Id给课程增加浏览数量
     * @param courseId void
     * @throws
     */
	public void addViewCount(String courseId) {
		Query query=new Query(Criteria.where("id").is(courseId));
		Update update = new Update().inc("viewCount", 1).set("utime", System.currentTimeMillis());
		mongoTemplate.updateFirst(query, update, NewCourse.class);
	}
	
	/**
	 * 增加购买数量
	 * @param courseId
	 */
	public void addBuyCount(String courseId) {
		Query query=new Query(Criteria.where("id").is(courseId));
		Update update = new Update().inc("buyCount", 1).set("utime", System.currentTimeMillis());
		mongoTemplate.updateMulti(query, update, NewCourse.class);
	}
	
	/**
	 * 根据ID查询课程，只返回课程的分类信息
	 * @param courseId
	 * @return
	 * @throws XueWenServiceException
	 */
	public NewCourse findOneNewCourseByIdRspCategoryInfo(String courseId)throws XueWenServiceException{
		Query query=new Query(Criteria.where("id").is(courseId));
		query.fields().include("categoryId").include("childCategoryId");
		return mongoTemplate.findOne(query, NewCourse.class);
	}
	
	/**
	 * 为所有老的课程添加未审核字段
	 * @throws XueWenServiceException
	 */
	public void addNotCheckedForAllCourse()throws XueWenServiceException{
		Query query=new Query();
		Update update=new Update();
		update.set("checked",false);
		mongoTemplate.updateMulti(query, update, NewCourse.class);
	}
	/**
	 * 合并用户，将fromUser 创建的课程合并到toUserId
	 * @param fromUserId
	 * @param toUserId
	 * @throws XueWenServiceException
	 */
	public void mergeNewCourse(String fromUserId,String toUserId)throws XueWenServiceException{
		Query query=new Query(Criteria.where("createUser").is(fromUserId));
		Update update=new Update();
		update.set("createUser",toUserId);
		mongoTemplate.updateMulti(query, update, NewCourse.class);
	}
	
	/**
	 * 
	 * @Title: addFavCount
	 * @auther Tangli
	 * @Description:收藏数量
	 * @param sourceId
	 * @param i void
	 * @throws
	 */
	public void addFavCount(String sourceId, int i) {
		Query query=new Query(Criteria.where("id").is(sourceId));
		Update update=new Update();
		update.inc("favCount", i);
		mongoTemplate.updateFirst(query, update, NewCourse.class);	
	}
	
	
}
