package operation.service.ad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import operation.pojo.ad.Ad;
import operation.pojo.ad.AdSeller;
import operation.pojo.log.UserRegistLog;
import operation.repo.ad.AdReposity;
import operation.service.log.UserRegistLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import tools.StringUtil;
import tools.YXTJSONHelper;

@Service
public class AdService {
	@Autowired
	private AdReposity adReposity;
	@Autowired
	private UserRegistLogService userRegistLogService;
	@Autowired
	private AdSellerService adSellerService;
	public Ad createAd(Ad ad){
		AdSeller adSeller=adSellerService.findById(ad.getAdSid());
		ad.setAdSellerName(adSeller.getName());
		ad.setAdSellerId(adSeller.getAdSellerId());
		ad.setCtime(System.currentTimeMillis());
		ad.setUtime(System.currentTimeMillis());
		this.adReposity.save(ad);
		userRegistLogService.save(new UserRegistLog(ad));
		return ad;
	}
	public void deleteAd(Ad ad){
		this.adReposity.delete(ad);
	}
	public Page<Ad> page(Pageable pageable){
		return this.adReposity.findAll(pageable);
	}
	public List<Ad> adList(){
		return this.adReposity.findAll();
	}
	public Page<Ad> searchQd(long ctime, long etime, String qdId,
			String qdName, Pageable pageable) {
		if(StringUtil.isBlank(qdId)&&StringUtil.isBlank(qdName)){
			return adReposity.findByCtimeBetween(ctime,etime,pageable);
		}
		if(StringUtil.isBlank(qdId)&&(!StringUtil.isBlank(qdName))){
			return adReposity.findByAdSellerNameLikeAndCtimeBetween(qdName,ctime,etime,pageable);

		}
		if(StringUtil.isBlank(qdName)&&(!StringUtil.isBlank(qdId))){
			return adReposity.findByAdSellerIdLikeAndCtimeBetween(qdId,ctime,etime,pageable);

		}
		if((!StringUtil.isBlank(qdId))&&(!StringUtil.isBlank(qdName))){
			return adReposity.findAllInfo(qdId,qdName,ctime,etime,pageable);
		}
		return null;
	}
	public Ad findById(String a) {
		return adReposity.findOne(a);
		
	}
	
	public Ad findOne(String id){
		return adReposity.findOne(id);
	}

}
