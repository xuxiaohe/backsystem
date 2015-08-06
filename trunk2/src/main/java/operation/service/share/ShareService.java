package operation.service.share;

import java.util.List;

import operation.exception.XueWenServiceException;
import operation.pojo.share.Share;
import operation.pojo.user.User;
import operation.repo.share.ShareRepository;
import operation.repo.share.ShareTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class ShareService {
	
	
	@Autowired
	public ShareRepository shareRepository;
	@Autowired
	public ShareTemplate shareTemplate;
	
	
	public Share subjectShare(Share share)
			throws XueWenServiceException {
		
		return shareRepository.save(share);
	}
	
	
	public ShareService() {
		super();
	}

	/**
	 * 用户分享
	 * @param userId
	 * @param appKey
	 * @param sourceId
	 * @param type
	 * @throws XueWenServiceException
	 */
	public void addShare(String userId,String domain,String appKey,String sourceId,String shareType,String toType,String toAddr)throws XueWenServiceException{
		Share share=new Share();
		share.setUserId(userId);
		share.setDomain(domain);
		share.setAppKey(appKey);
		share.setSourceId(sourceId);
		share.setShareType(shareType);
		share.setToType(toType);
		share.setToAddr(toAddr);
		long time=System.currentTimeMillis();
		share.setCtime(time);
		share.setUtime(time);
		shareRepository.save(share);
	}
	/**
	 * 用户分享
	 * @param userId
	 * @param appKey
	 * @param sourceId
	 * @param type
	 * @throws XueWenServiceException
	 */
	public void addShare(User user,String domain,String appKey,String sourceId,String shareType,String toType,String toAddr)throws XueWenServiceException{
		Share share=new Share();
		share.setUserId(user.getId());
		share.setDomain(domain);
		share.setAppKey(appKey);
		share.setSourceId(sourceId);
		share.setShareType(shareType);
		share.setToType(toType);
		share.setToAddr(toAddr);
		long time=System.currentTimeMillis();
		share.setCtime(time);
		share.setUtime(time);
		shareRepository.save(share);
	}
	
	public void shareCouerse(User user,String appKey,String sourceId,String shareType)throws XueWenServiceException{
		Share one = shareRepository.findByUserIdAndSourceIdAndShareType(user.getId(), sourceId, shareType);
		if(one==null){
			Share share=new Share();
			share.setUserId(user.getId());
			share.setAppKey(appKey);
			share.setSourceId(sourceId);
			share.setShareType(shareType);
			shareRepository.save(share);
		}
	}
	
	public int courseCount(String userId,String appKey,String type){
		return shareRepository.countByUserIdAndShareTypeAndAppKey(userId, type, appKey);
		
	}
	
	/**
	 * 根据来源集合删除分享记录
	 * @param sourceIds
	 * @throws XueWenServiceException
	 */
	public void deleteBySourceIds(List<Object> sourceIds)throws XueWenServiceException{
		shareTemplate.deleteBySourceIds(sourceIds);
	}
	/**
	 * 根据用户ID和来源集合删除分享记录
	 * @param sourceIds
	 * @throws XueWenServiceException
	 */
	public void deleteByUserIdAndSourceIds(String userId,List<Object> sourceIds)throws XueWenServiceException{
		shareTemplate.deleteByUserIdAndSourceIds(userId,sourceIds);
	}
	/**
	 * 根据来源集合删除分享记录
	 * @param sourceIds
	 * @throws XueWenServiceException
	 */
	public void deleteBySourceId(String sourceId)throws XueWenServiceException{
		shareTemplate.deleteBySourceId(sourceId);
	}
	/**
	 * 根据目的地址和目的类型删除分享记录
	 * @param sourceIds
	 * @throws XueWenServiceException
	 */
	public void deleteByToAddrAndToType(String toAddr,String toType)throws XueWenServiceException{
		shareTemplate.deleteByToAddrAndToType(toAddr, toType);
	}
	
}
