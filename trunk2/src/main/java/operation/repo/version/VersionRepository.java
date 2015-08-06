package operation.repo.version;

import java.util.List;

import operation.pojo.version.YunXueTangVersion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VersionRepository extends MongoRepository<YunXueTangVersion, String>{
	List<YunXueTangVersion> findByDevice(String device,Sort st);

	YunXueTangVersion findByVersionIdAndDevice(String versionId,String device);
	
	Page<YunXueTangVersion> findAll(Pageable page);

}
