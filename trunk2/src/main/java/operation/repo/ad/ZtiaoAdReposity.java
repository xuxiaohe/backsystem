package operation.repo.ad;

import java.util.List;

import operation.pojo.ad.ZtiaoAd;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ZtiaoAdReposity extends MongoRepository<ZtiaoAd, String> {

	List<ZtiaoAd> findByAdIdAndEffective(String adId,boolean effective);
	Page<ZtiaoAd> findByAdId(String adId,Pageable page);
	ZtiaoAd findById(String id);
	ZtiaoAd findByIndex(int index);
	float countByAdId(String adId);
	List<ZtiaoAd> findByGroupId(String groupId);
}
