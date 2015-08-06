//package operation.repo.vo;
//import java.util.List;
//
//import operation.vo.SearchGroupVo;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.mongodb.repository.MongoRepository;
//
//public interface SearchGroupVoReposity  extends MongoRepository<SearchGroupVo, String>{
//
//	SearchGroupVo findOneByGroupId(String id);
//	
//	Page<SearchGroupVo> findByGroupIdIn(List<String> id, Pageable pageable);
//	
//	Page<SearchGroupVo> findByGroupNameLike(String groupName,Pageable page);
//	
//	long countByGroupTag(String groupTag);
//}
