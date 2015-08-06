package operation.repo.course;

import java.util.List;

import operation.pojo.course.UserGroupCourse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserGroupCourseRepository extends MongoRepository<UserGroupCourse, String>{
	Page<UserGroupCourse> findByGroupAndCourseAndStudyed(String groupId,String course,boolean studyed,Pageable pageable);
	Page<UserGroupCourse> findByUserIdAndFavedAndStudyed(String userId,boolean faved,boolean studyed,Pageable pageable);
	Page<UserGroupCourse> findByGroupAndCourseAndFaved(String group,String course,boolean faved,Pageable pageable);
	UserGroupCourse findOneUserGroupCourseByUserIdAndGroupAndCourse(String userId,String group,String courseId);
	UserGroupCourse findOneUserGroupCourseByUserIdAndGroupCourseId(String userId,String groupCourseId);
	int countByUserIdAndFavedAndStudyed(String userId,boolean faved,boolean studyed);
	int countByUserId(String userId);
	Page<UserGroupCourse> findByUserId(String userId,Pageable pageable);
	
	List<UserGroupCourse> findByUserId(String userId);
	
	Page<UserGroupCourse> findByUserIdAndFaved(String userId,boolean faved,Pageable pageable);
}
