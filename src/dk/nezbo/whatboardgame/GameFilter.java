package dk.nezbo.whatboardgame;

public class GameFilter {

	private int players;
	private int time;
	private int age;
	private String category;

	public GameFilter(int players, int time, int age, String category){
		this.players = players;
		this.time = time;
		this.age = age;
		this.category = category;
	}
	
	public int getPlayers(){
		return players;
	}
	
	public int getTime(){
		return time;
	}
	
	public int getAge(){
		return age;
	}
	
	public String getCategory(){
		return category;
	}
	
	public boolean verifyGame(Game game){
		if(!game.fitsPlayers(this.players)) return false;
		if(!game.fitsPlayingTime(this.time)) return false;
		if(!game.fitsAge(this.age)) return false;
		if(!game.fitsCategory(this.category)) return false;
		
		return true;
	}
	
	public boolean requiresDetails(){
		return age != 0 || !category.equals("ANY");
	}
	
	@Override
	public boolean equals(Object that) {
		if(that == null) return false;
		if(!that.getClass().equals(this.getClass())) return false;
		
		GameFilter other = (GameFilter) that;
		if(other.players != this.players) return false;
		if(other.time != this.time) return false;
		if(other.age != this.age) return false;
		if(!other.category.equals(this.category)) return false;
		
		return true;
	}
	
	@Override
	public String toString(){
		return "GAMEFILTER(Players: "+players+", Time: "+time+", Age: "+age+", Category: "+category+")";
	}
}
