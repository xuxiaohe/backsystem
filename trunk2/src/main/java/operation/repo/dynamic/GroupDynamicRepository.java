package operation.repo.dynamic;

import java.util.List;

import operation.pojo.dynamic.GroupDynamic;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupDynamicRepository extends MongoRepository<GroupDynamic, String>{

	Page<GroupDynamic> findByGroupIdAndCtimeGreaterThanAndChecked(String groupId,long ctime,boolean checked,Pageable pageable);
	Page<GroupDynamic> findByGroupIdAndCtimeGreaterThanAndCheckedAndTypeNot(String groupId,long ctime,boolean checked,String  type1,Pageable pageable);
	Page<GroupDynamic> findByGroupIdAndCtimeLessThanAndChecked(String groupId,long ctime,boolean checked,Pageable pageable);
	Page<GroupDynamic> findByGroupIdAndCtimeLessThanAndCheckedAndTypeNot(String groupId,long ctime,boolean checked,String type1,Pageable pageable);
	GroupDynamic findByGroupIdAndSourceId(String groupId ,String sourceId);
	List<GroupDynamic> findByCourseId(String courseId);
	Page<GroupDynamic> findByGroupIdInAndCtimeGreaterThanAndTypeLessThanAndChecked
	(List<String> groupIds,long ctime,String type,boolean checked,Pageable pageable);
	Page<GroupDynamic> findByCtimeGreaterThanAndTypeLessThanAndGroupIdInAndChecked
	(long ctime,String type,List<String> groupIds,boolean checked,Pageable pageable);
	Page<GroupDynamic> findByGroupIdInAndChecked
	(List<String> groupIds,boolean checked,Pageable pageable);
	Page<GroupDynamic> findByGroupIdInAndCtimeLessThanAndTypeLessThanAndChecked
	(List<String> groupIds,long ctime,String type,boolean checked,Pageable pageable);
	Page<GroupDynamic> findByAutherIdAndTypeLessThan(String userId,String type,Pageable pageable);
	Page<GroupDynamic> findByAutherIdAndType(String userId,String type,Pageable pageable);
	Page<GroupDynamic> findByAutherIdAndTypeOrAutherIdAndType(String userId,String type,String userId1,String type1,Pageable pageable);
	List<GroupDynamic> findBySourceIdIn(List<String> sourceIds);
}
