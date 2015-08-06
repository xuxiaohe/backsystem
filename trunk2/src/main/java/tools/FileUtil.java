package tools;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;

import javax.imageio.ImageIO;

import operation.exception.XueWenServiceException;

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

public class FileUtil {
	
	private static final Logger logger=Logger.getLogger(FileUtil.class);

	/**
	 * 上传图片
	 * @param file
	 * @param oldLogoURL
	 * @param id
	 * @return
	 * @throws XueWenServiceException
	 */
	public static String uploadFile(MultipartFile file, String cate,String obj,String oldLogoURL,String id) throws XueWenServiceException {
//		String name = null;
//		try {
//			if (null != file && !file.isEmpty()) {
////				String  dir=Config.IMAGESPath+Config.IMAGESURLPath+"/"+cate+idSpilt(id,4)+"/"+obj+"/";//文件存储绝对路径
//				logger.info("======文件存储的绝对路径："+dir);
//				String basePath=Config.IMAGESURLPath+"/"+cate+idSpilt(id,4)+"/"+obj+"/";//相对路径
//				logger.info("=======文件存储的相对路径:"+basePath);
//				name =URLEncoder.encode(file.getOriginalFilename(),"UTF-8");
//				String imgURL = Config.contextPath +basePath+ name;
//				logger.info("========文件的外网访问路径："+imgURL);
//				if (null == oldLogoURL || !oldLogoURL.equals(imgURL)) {
//					File dirFile=new File(dir);
//					if(!dirFile.exists()){
//						dirFile.mkdirs();
//					}
//					byte[] bytes = file.getBytes();
//					File file2 = new File(dir+name);
//					if (file2.exists() && file2.canWrite()) {
//
//					} else {
//						file2.createNewFile();
//						file2.setWritable(true);
//					}
//					BufferedOutputStream stream = new BufferedOutputStream(
//							new FileOutputStream(file2));
//					stream.write(bytes);
//					stream.close();
//					logger.info("===============文件上传成功======================");
//					return imgURL;
////					group.setLogoUrl(imgURL);
////					group=groupRepo.save(group);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_UPLOAD_201, null);
//		}
		return null;
	}
	
	
	
	
//	/**
//	 * 上传图片,并裁剪为三个图片尺寸 small :140*140  mid:280*280  以及原有尺寸;
//	 * @param file
//	 * @param oldLogoURL
//	 * @param id
//	 * @return
//	 * @throws XueWenServiceException
//	 */
//	public static String uploadFile(MultipartFile file, String cate,String obj,String oldLogoURL,String id) throws XueWenServiceException {
//		String name = null;
//		try {
//			if (null != file && !file.isEmpty()) {
//				String  dir=Config.IMAGESPath+Config.IMAGESURLPath+"/"+cate+idSpilt(id,4)+"/"+obj+"/";//文件存储绝对路径
//				logger.info("======文件存储的绝对路径："+dir);
//				String basePath=Config.IMAGESURLPath+"/"+cate+idSpilt(id,4)+"/"+obj+"/";//相对路径
//				logger.info("=======文件存储的相对路径:"+basePath);
//				name =URLEncoder.encode(file.getOriginalFilename(),"UTF-8");
//				String imgURL = Config.contextPath +basePath+ name;
//				if (null == oldLogoURL || !oldLogoURL.equals(imgURL)) {
////					String imgUrl_small=
//					File dirFile=new File(dir);
//					if(!dirFile.exists()){
//						dirFile.mkdirs();
//					}
//					byte[] bytes = file.getBytes();
//					File file2 = new File(dir+name);
//					if (file2.exists() && file2.canWrite()) {
//
//					} else {
//						file2.createNewFile();
//						file2.setWritable(true);
//					}
//					BufferedOutputStream stream = new BufferedOutputStream(
//							new FileOutputStream(file2));
//					stream.write(bytes);
//					stream.close();
//					logger.info("===============文件上传成功======================");
//					//生成small 图
//					String smallFileName=UploadUtil.getFileNameWithSign(name,"_small",UploadUtil.getExt(name));
//					boolean small=UploadUtil.uploadZip(file2, Config.IMAGESPath, smallFileName, dir, 140, 140);
//					logger.info("==========================小图上传是否成功："+small);
//					//生成mid图
//					String midFileName=UploadUtil.getFileNameWithSign(name,"_mid",UploadUtil.getExt(name));
//					boolean mid=UploadUtil.uploadZip(file2, Config.IMAGESPath,midFileName, dir, 280,280);
//					logger.info("==========================小图上传是否成功："+mid);
//					return imgURL;
////					group.setLogoUrl(imgURL);
////					group=groupRepo.save(group);
//				}
//				logger.info("========文件的外网访问路径："+imgURL);
//				
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_UPLOAD_201, null);
//		}
//		return null;
//	}
//	
//	
//	
	
	/**
	 * 生成缩略图 
	 * fromFileStr:原图片路径
	 *  saveToFileStr:缩略图路径 
	 *  width:缩略图的宽 
	 *  height:缩略图的高
	 */
	public static void saveImageAsJpg(String fromFileStr, String saveToFileStr,
			int width, int height,boolean equalProportion) throws Exception {
		BufferedImage srcImage;
        String imgType="JPEG";
        if(fromFileStr.toLowerCase().endsWith(".png")){
        	imgType="PNG";
        }
        File fromFile=new File(fromFileStr);
        File saveFile=new File(saveToFileStr);
        srcImage=ImageIO.read(fromFile);
        if(width>0||height>0){
        	srcImage=resize(srcImage,width,height,equalProportion);
        }
        ImageIO.write(srcImage,imgType,saveFile);
	}
    
	/**
	 * 将原图片的BufferedImage对象生成缩略图
	 * source：原图片的BufferedImage对象
	 * targetW:缩略图的宽
	 * targetH:缩略图的高
	 */
	public static BufferedImage resize(BufferedImage source,int targetW,int targetH,boolean equalProportion){
		int type=source.getType();
		BufferedImage target=null;
		double sx=(double)targetW/source.getWidth();
		double sy=(double)targetH/source.getHeight();
		//这里想实现在targetW，targetH范围内实现等比例的缩放
		  //如果不需要等比例的缩放则下面的if else语句注释调即可
		if(equalProportion){
			if(sx>sy){
				sx=sy;
				targetW=(int)(sx*source.getWidth());
			}else{
				sy=sx;
				targetH=(int)(sx*source.getHeight());
			}
		}
		if(type==BufferedImage.TYPE_CUSTOM){
			ColorModel cm=source.getColorModel();
			WritableRaster raster=cm.createCompatibleWritableRaster(targetW,targetH);
		    boolean alphaPremultiplied=cm.isAlphaPremultiplied();
		    target=new BufferedImage(cm,raster,alphaPremultiplied,null);
		}else{
			target=new BufferedImage(targetW,targetH,type);
			Graphics2D g=target.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
			g.drawRenderedImage(source,AffineTransform.getScaleInstance(sx,sy));
			g.dispose();
		}
		return target;
	}
	
	public static String idSpilt(String id,int spiltNum)throws XueWenServiceException{
		if(id==null){
			logger.info("===id不符合规则=======id:"+id);
			throw new XueWenServiceException(Config.STATUS_201,Config.MSG_UPLOAD_201, null);
		}else{
			String spilt="";
			while(id.length()>0){
				if(id.length()>spiltNum){
					spilt+="/"+id.substring(0,spiltNum);
					id=id.substring(spiltNum);
				}else{
					spilt+="/"+id;
					id="";
				}
			}
//			spilt="/"+id.substring(0,4)+"/"+id.substring(4,8)+"/"+id.substring(8,12)+"/"+id.substring(12,16)+"/"+id.substring(16,20)+"/"+id.substring(20);
			return spilt;
		}
	}
	
	public static void main(String[] args) {
		try {
			String id="53e057a3e4b07984c386baf5";
			System.out.println(FileUtil.idSpilt(id,8));
		} catch (XueWenServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
}
