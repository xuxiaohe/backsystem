package operation.repo.study;

import java.util.List;

import love.cq.util.StringUtil;
import operation.exception.XueWenServiceException;
import operation.pojo.study.Study;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import tools.Config;

@Service
@Component
public class StudyTemplate {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	/**
	 * 根据用户ID和域和类型ID和类型判断用户是否已经收藏
	 * @author hjn
	 * @param userId
	 * @param domain
	 * @param appKey
	 * @param sourceId
	 * @param favType
	 * @return
	 * @throws XueWenServiceException
	 */
	public boolean existsByUserIdAndDomainAndSourceIdAndStudyType(String userId,String domain,String sourceId,String studyType)throws XueWenServiceException{
		if(StringUtil.isBlank(userId) || StringUtil.isBlank(domain) || StringUtil.isBlank(sourceId)){
			throw new XueWenServiceException(Config.STATUS_201, Config.MSG_201,null);
		}
		Query query=new Query(Criteria.where("userId").is(userId).and("domain").is(domain).and("sourceId").is(sourceId).and("studyType").is(studyType));
		return mongoTemplate.exists(query, Study.class);
	}
	
	/**
	 * 根据来源列表删除分享记录
	 * @param sourceIds
	 * @throws XueWenServiceException
	 */
	public void deleteBySourceIds(List<Object> sourceIds)throws XueWenServiceException{
		Query query=new Query(Criteria.where("sourceId").in(sourceIds));
		mongoTemplate.remove(query, Study.class);
	}
	
	/**
	 * 根据用户Id和来源列表删除收藏记录
	 * @param sourceIds
	 * @throws XueWenServiceException
	 */
	public void deleteByUserIdAndSourceIds(String userId,List<Object> sourceIds)throws XueWenServiceException{
		Query query=new Query(Criteria.where("userId").is(userId).and("sourceId").in(sourceIds));
		mongoTemplate.remove(query, Study.class);
	}
	/**
	 * 根据来源删除学习列表
	 * @param sourceIds
	 * @throws XueWenServiceException
	 */
	public void deleteBySourceId(String sourceId)throws XueWenServiceException{
		Query query=new Query(Criteria.where("sourceId").is(sourceId));
		mongoTemplate.remove(query, Study.class);
	}
}
