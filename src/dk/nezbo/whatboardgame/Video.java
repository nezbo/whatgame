package dk.nezbo.whatboardgame;

public class Video {
	
	private String title, category, url;
	
	public Video(String title, String category, String url){
		this.title = title;
		this.category = category;
		this.url = url;
	}
	
	public String getTitle(){
		return title;
	}
	
	public String getCategory(){
		return category;
	}
	
	public String getURL(){
		return url;
	}
}
