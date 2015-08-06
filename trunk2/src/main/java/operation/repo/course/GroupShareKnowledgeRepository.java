package operation.repo.course;

import java.util.List;

import operation.pojo.course.GroupShareKnowledge;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupShareKnowledgeRepository extends MongoRepository<GroupShareKnowledge, String>{
	
	Page<GroupShareKnowledge> findByGroupId(String groupId, Pageable pageable);
	List<GroupShareKnowledge> findByGroupId(String groupId);
	GroupShareKnowledge findOneByGroupIdAndKnowledge(String groupId,String kid);

	Page<GroupShareKnowledge> findByKnowledgeIn(List<String> knowledgeIds,Pageable pageable);
	
	int countByGroupId(String groupId);
	
	GroupShareKnowledge findOneByKnowledge(String knowledge);
	Page<GroupShareKnowledge> findByGroupIdAndKngNameLike(String groupId,
			String keyWords, Pageable pageable);
	
	List<GroupShareKnowledge> findByGroupIdAndKngNameLike(String groupId,
			String keyWords);
	
	List<GroupShareKnowledge> findByKngType(Integer type);
	List<GroupShareKnowledge> findByKnowledgeIn(List<String> knowledgeIds);
	
}

