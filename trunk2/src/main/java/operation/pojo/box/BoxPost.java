package operation.pojo.box;

import org.springframework.data.annotation.Id;

public class BoxPost {

	@Id
	private String id;
	
	private String chinaName;
	
	private String englishName;
	
	private String local;
	
	private String type;
	
	private int size;  //-1无限制  >0 长度限制
	
	private long  ctime;

	public BoxPost(){
		super();
	}
	
	public BoxPost(String chinaName,String englishName,String local ,String type,int size){
		super();
		this.chinaName=chinaName;
		this.englishName=englishName;
		this.local=local;
		this.type=type;
		this.size=size;
		this.ctime=System.currentTimeMillis();
	}
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getChinaName() {
		return chinaName;
	}

	public void setChinaName(String chinaName) {
		this.chinaName = chinaName;
	}

	public String getEnglishName() {
		return englishName;
	}

	public void setEnglishName(String englishName) {
		this.englishName = englishName;
	}
	

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public long getCtime() {
		return ctime;
	}

	public void setCtime() {
		this.ctime = System.currentTimeMillis();
	}
}
