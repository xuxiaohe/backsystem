package operation.controller.oss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import operation.BaseController;
import operation.exception.XueWenServiceException;
import operation.pojo.ad.ZtiaoAd;
import operation.pojo.pub.QueryModelMul;
import operation.service.ad.ZtiaoAdService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tools.Config;
import tools.PageRequestTools;
import tools.ResponseContainer;

@RestController
@RequestMapping("/oss/ztiaoad")
public class OssZtiaoAdController extends BaseController {

	@Autowired
	private ZtiaoAdService ztiaoAdService;
	/**
	 * 创建新的广告位条目
	 * @param request
	 * @param ztiaoAd
	 * @return
	 */
	@RequestMapping("addNew")
	public @ResponseBody ResponseContainer addNew(HttpServletRequest request,ZtiaoAd ztiaoAd) {
		try {
		float count=ztiaoAdService.getAppIndexAdCount(ztiaoAd.getAdId());
		//线上为5，测试为10
		if(count<10){
			ztiaoAd.setIndex((int)count);
			ztiaoAdService.addNew(ztiaoAd);
		}
		
		return addResponse(Config.STATUS_200, Config.MSG_CREATE_200,
					true, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	/**
	 * APP首页获取广告信息
	 * @param request
	 * @param ztiaoAd
	 * @return
	 */
	@RequestMapping("getAppIndexAd")
	public @ResponseBody ResponseContainer getAppIndexAd(HttpServletRequest request,ZtiaoAd ztiaoAd,String adid) {
		try {
			
			return addResponse(Config.STATUS_200, Config.MSG_CREATE_200,
					ztiaoAdService.getAppIndexAd(adid), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	/**
	 * 搜索APP首页获取广告信息
	 * @param request
	 * @param ztiaoAd
	 * @return
	 */
	@RequestMapping("searchAd")
	public @ResponseBody ResponseContainer searchAd(HttpServletRequest request,ZtiaoAd ztiaoAd,QueryModelMul dm) {
		try {
			List<String> sort = new ArrayList<String>();
			sort.add("index");
			dm.setSort(sort);
			dm.setMode("ASC");
			Pageable pageable=PageRequestTools.pageRequesMake(dm);
			Map m=new HashMap();
			m.put("result", ztiaoAdService.findAd(ztiaoAd.getAdId(),pageable));
			m.put("counts", ztiaoAdService.Count("0"));
			
			
			return addResponse(Config.STATUS_200, Config.MSG_CREATE_200,
					m, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	/**
	 * 获取APP首页获取广告详细信息
	 * @param request
	 * @param ztiaoAd
	 * @return
	 */
	@RequestMapping("searchAdInfo")
	public @ResponseBody ResponseContainer findAdinfo(HttpServletRequest request) {
		try {
			String id=request.getParameter("id");
			return addResponse(Config.STATUS_200, Config.MSG_CREATE_200,
					ztiaoAdService.findAdIInfo(id), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	/**
	 * 广告位上移或下移
	 * @param request
	 * @param ztiaoAd
	 * @return
	 */
	@RequestMapping("movead")
	public @ResponseBody ResponseContainer movead(HttpServletRequest request) {
		try {
			//当前位置
			String currindex=request.getParameter("currindex");
			int i=Integer.parseInt(currindex);
			//0上移(-1)   1下移(+1)
			String state=request.getParameter("state");
			//0上移(-1)
			if("0".equals(state)){
				int j=i-1;
				//当前的位置
				ZtiaoAd z=ztiaoAdService.findAdByindex(i);
				ZtiaoAd zz=ztiaoAdService.findAdByindex(j);
				
				z.setIndex(j);
				ztiaoAdService.savead(z);
				
				zz.setIndex(i);
				ztiaoAdService.savead(zz);
			}
			// 1下移(+1)
			if("1".equals(state)){
				int j=i+1;
				//当前的位置
				ZtiaoAd z=ztiaoAdService.findAdByindex(i);
				ZtiaoAd zz=ztiaoAdService.findAdByindex(j);
				
				z.setIndex(j);
				ztiaoAdService.savead(z);
				
				zz.setIndex(i);
				ztiaoAdService.savead(zz);
			}
			
			return addResponse(Config.STATUS_200, Config.MSG_CREATE_200,
					true, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
	
	
	
	/**
	 * 更新APP首页获取广告详细信息
	 * @param request
	 * @param ztiaoAd
	 * @return
	 */
	@RequestMapping("updateAdInfo")
	public @ResponseBody ResponseContainer updateAdInfo(HttpServletRequest request,ZtiaoAd ztiaoAd) {
		try {
			ZtiaoAd  z=ztiaoAdService.findAdIInfo(ztiaoAd.getId());
			//添加更新的内容
			ztiaoAd.setCtime(z.getCtime());
			ztiaoAd.setUtime(System.currentTimeMillis());
			ztiaoAd.setCcount(z.getCcount());
			ztiaoAd.setIndex(z.getIndex());
			
			
			ztiaoAdService.updateAdIInfo(ztiaoAd);
			return addResponse(Config.STATUS_200, Config.MSG_CREATE_200,
					ztiaoAdService.findAdIInfo(ztiaoAd.getAdId()), Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(e.getCode(), e.getMessage(), false,Config.RESP_MODE_10, "");
		} catch (Exception e) {
			return addResponse(Config.STATUS_505, Config.MSG_505, false,Config.RESP_MODE_10, "");
		}
	}
}