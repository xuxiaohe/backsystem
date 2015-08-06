package operation.controller.oss;
import java.io.IOException;
import java.util.List;

import operation.BaseController;
import operation.exception.XueWenServiceException;
import operation.pojo.email.MailTempter;
import operation.pojo.email.YxtRegMail;
import operation.service.email.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import tools.Config;
import tools.ResponseContainer;

@Controller
@RequestMapping("/oss/email")
public class OssEmailController extends BaseController {
	@Autowired
	private EmailService emailService;
	
	@RequestMapping("sendRefEamil")
	@ResponseBody
	public ResponseContainer send(YxtRegMail yxtRegMail,String addresses){
		try {
			emailService.sendMails(yxtRegMail,addresses);
			return addResponse(Config.STATUS_200, Config.MSG_200, true, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(Config.STATUS_500, Config.MSG_500, false, Config.RESP_MODE_10, "");
		} catch (IOException e) {
			return addResponse(Config.STATUS_500, Config.MSG_500, false, Config.RESP_MODE_10, "");
		}
	}
	@RequestMapping("addMail")
	@ResponseBody
	public ResponseContainer add(String name,String content){
		try {
			emailService.add(content, name);
			return addResponse(Config.STATUS_200, Config.MSG_200, true, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(Config.STATUS_200, Config.MSG_200, true, Config.RESP_MODE_10, "");
		}
		
	}
	
	@RequestMapping("modify")
	@ResponseBody
	public ResponseContainer addtem(String id,String content){           
		try {
			emailService.modify(id, content);
			return addResponse(Config.STATUS_200, Config.MSG_200, true, Config.RESP_MODE_10, "");
		} catch (XueWenServiceException e) {
			return addResponse(Config.STATUS_200, Config.MSG_200, true, Config.RESP_MODE_10, "");
		}
		
	}
	
	@RequestMapping("getByName")
	@ResponseBody
	public ResponseContainer getByName(String name) {
		MailTempter temp = emailService.getTemppterByName(name);
		return addResponse(Config.STATUS_200, Config.MSG_200, temp,
				Config.RESP_MODE_10, "");

	}
	
	@RequestMapping("getById")
	@ResponseBody
	public ResponseContainer getById(String id) {
		MailTempter temp = emailService.findOne(id);
		return addResponse(Config.STATUS_200, Config.MSG_200, temp,
				Config.RESP_MODE_10, "");

	}
	
	@RequestMapping("getAll")
	@ResponseBody
	public ResponseContainer getAll() {
		List<MailTempter> temp = emailService.getAll();
		return addResponse(Config.STATUS_200, Config.MSG_200, temp,
				Config.RESP_MODE_10, "");

	}
	
	
	
	
	
}
