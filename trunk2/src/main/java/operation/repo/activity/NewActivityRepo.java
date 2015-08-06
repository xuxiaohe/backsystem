package operation.repo.activity;


import operation.pojo.activity.NewActivity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NewActivityRepo extends MongoRepository<NewActivity, String>{

	Page<NewActivity> findByGroup(String groupId,Pageable able);
	Page<NewActivity> findByCreateUser(String user,Pageable able);
	Page<NewActivity> findAll(Pageable able);
	
}
