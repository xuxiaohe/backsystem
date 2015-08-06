package tools;

import java.io.File;
import java.io.IOException;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import operation.exception.XueWenServiceException;
import operation.pojo.email.YxtRegMail;

import org.apache.log4j.Logger;
/**
 * 
* @ClassName: MailTemple
* @Description: TODO(这里用一句话描述这个类的作用)
* @author JackTang
* @date 2015年2月2日 下午5:27:14
*
 */
public class MailTemple {
	private static final Logger logger=Logger.getLogger(MailTemple.class);
	

	
	
	
	/**
	 * 云学堂邮件注册模板
	 * @param itemName
	 * @param url
	 * @return
	 * @throws XueWenServiceException
	 * @throws IOException 
	 */
	public static Multipart getYxtRegMailTemple(YxtRegMail yxtRegMail)throws XueWenServiceException, IOException{
		Multipart multipart = new MimeMultipart("alternative");
		try {
			// 添加html形式的邮件正文
			BodyPart part1 = new MimeBodyPart();
			part1.setHeader("Content-Type", "text/html;charset=UTF-8");
			part1.setHeader("Content-Transfer-Encoding", "base64");
			File file=new File("\\mallTemplete\\yxtReg.html");
			String   htmlContent="<!doctype html>"+
			"<html lang='en'>"+                                                                                                                                                                    
			"<head>                                                                                                                                                                                  "+
			"	<meta charset=\"UTF-8\">                                                                                                                                                             "+
			"	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no\" />                                                                         "+
			"	<title>邀请注册邮箱</title>                                                                                                                                                          "+
			"	<style type=\"text/css\">                                                                                                                                                            "+
			"		body {font-size: 13px;font-family: 微软雅黑;color: #333;padding: 0;margin: 0;background-color: #FFF;}                                                                     "+
	        "		div,a,img{padding: 0;margin:0;}                                                                     "+
	        "		p {margin-bottom: 15px;margin-top: 0;}                                                                     "+
	        "		.nav {height: 50px;background-color: #2aabe6;max-width: 754px;box-shadow: 0 3px 3px 0 #DDD;}                                                                     "+
	        "		.nav img {margin-top:5px;vertical-align: top;}                                                                     "+
	        "		.main {max-width: 734px;margin: 0 10px;}                                                                     "+
	        "		.content {max-width: 485px;margin:45px auto;}                                                                     "+
	        "		.text-highlight {color: #52A0EA;}                                                                     "+
	        "		a {color: #0A8CD2;}                                                                     "+
			"	</style>                                                                                                                                                                             "+
			"</head>                                                                                                                                                                                 "+
			"<body>                                                                                                                                                                                  "+
			"	<div class=\"nav\">                                                                                                                                                                  "+
			"		<img src=\"http://7sbnvf.com2.z0.glb.clouddn.com/g/img/logo.png\"/>                                                                                                                                                 "+
			"	</div>                                                                                                                                                                               "+
			"	<div class=\"main\">                                                                                                                                                                 "+
			"		<div class=\"content\">                                                                                                                                                          "+
			"			<p class=\"font-weight:bold;font-size:16px;\">亲爱的用户</p>                                                                                                                 "+
			"			<p>您的好友&nbsp;&nbsp;&nbsp;&nbsp;<span class=\"text-highlight\" style=\"font-weight: bold;font-size: 16px;\" >"+
				yxtRegMail.getUserNick()+ "</span>("+yxtRegMail.getUserName()+")</p>"+
			"			<p>正在学习小组&nbsp;&nbsp;&nbsp;&nbsp;<a class=\"text-highlight;text-decoration:none;\" href='"+yxtRegMail.getGroupUrl() +"'>"+
				yxtRegMail.getGroupName()+"</a>&nbsp;&nbsp;&nbsp;&nbsp;中积极学习</p>                                               "+
			"			<p>他同时也向你发出了加入邀请，点击下方按钮接受，与TA共同享受学习的乐趣吧~</p>  "
			+ "<div style='margin:30px 0;text-align: center;'>"
			+"	<a href='"+yxtRegMail.getToUserRegUrl()+"' style='background-color: #2aabe6;color: #FFF;text-decoration:none;padding:10px;border:0;font-size: 18px;'>立即注册账号</a>"
			+"</div>"+
			"<p>如果以上按钮无法打开，请把下面的链接复制到浏览器地址栏中打开：</p>"+
			"			<p style=\"word-wrap: break-word;\"><a href=\""+yxtRegMail.getToUserRegUrl()+"\">"+yxtRegMail.getToUserRegUrl()+"</a></p>                                                                                                              "+
			"		</div>                                                                                                                                                                           "+
			"		<div style=\"border-top:solid 1px #E4E4E4;padding: 20px 0;\">                                                                                                                    "+
			"			<div style=\"width: 168px;font-size:14px;margin-left:30px;margin-top:15px;margin-bottom:15px;float: left;\">您可以通过新浪微博、微信关注我们，与我们取得联系</div>           "+
			"			<div style=\"float: left;margin-left:30px;margin-bottom:15px;border-right: solid 1px #E4E4E4;padding-right: 40px;\">                                                         "+
			"				<img src=\"http://7sbnvf.com2.z0.glb.clouddn.com/g/img/code-weibo.png\" width=\"70\" height=\"70\" />                                                                                                                              "+
			"				<img src=\"http://7sbnvf.com2.z0.glb.clouddn.com/g/img/code-weixin.png\" width=\"70\" height=\"70\" style=\"margin-left:30px;\" />                                                                                                  "+
			"			</div>                                                                                                                                                                       "+
			"			<div style=\"margin-left: 40px;float: left;\">                                                                                                                               "+
			"				<p style=\"font-weight: bold;\">客服电话</p>                                                                                                                             "+
			"				<p class=\"text-highlight\" style=\"font-size: 28px;\">400 0707 218</p>                                                                                                  "+
			"			</div>                                                                                                                                                                       "+
			"		</div>                                                                                                                                                                           "+
			"	</div>                                                                                                                                                                               "+
			"</body>                                                                                                                                                                                 "+
			"</html> ";                                                                                                                                                                                                                                                                                                                       
			part1.setContent(htmlContent, "text/html;charset=UTF-8");                                                                                                                                
			multipart.addBodyPart(part1);                                                                                                                                                            
		} catch (MessagingException e) {                                                                                                                                                                                                                                                                                                               
			logger.error("邮件模板生成失败："+e);                                                                                                                                                   
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_MAILTEMPLE_ERROR,e);                                                                                                      
		}                                                                                                                                                                                           
        return multipart;                                                                                                                                                                           
	}                                                                                                                                                                                               
	                                                                                                                                                                                                
	
}
