package dk.nezbo.whatboardgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class GameCollection implements Iterable<Game>{
	//private Context c;
	
	private ArrayList<Game> games;
	private String username;
	public boolean hasExpansions = true;
	
	public static GameCollection GetUsersGames(String username, Context c){
		String[] users = username.split("[,+]", 10);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
		GameCollection col = null;
		for(int i = 0; i < users.length; i++){
			if(users[i].length() == 0) continue; // empty name
			
			users[i] = users[i].trim();
			String curUsername = users[i].replace(" ", "%20");
			String simpleUri = "http://www.boardgamegeek.com/xmlapi2/collection?username="+ curUsername + "&own=1&stats=1";
			GameCollection cur = httpOrCache(simpleUri,users[i],c);
			
			// exclude expansions of wanted
			if(sharedPrefs.getBoolean("pref_expansions", false)){
				cur.hasExpansions = false;
				simpleUri = "http://www.boardgamegeek.com/xmlapi2/collection?username="+ curUsername + "&subtype=boardgameexpansion";
				GameCollection exps = httpOrCache(simpleUri,users[i],c);
				cur.removeAll(exps);
			}
			
			// place somewhere
			if(col == null){
				if(users.length == 1){
					col = cur;
				}else{
					col = new GameCollection(username); // multiple users
					col.addCollection(cur);
				}
				
			}else{
				col.addCollection(cur);
			}
		}
		
		// save the combination
		if(users.length > 1) StorageManager.saveUserCollection(col, username, c);
		
		return col;
	}
	
	private void removeAll(GameCollection exps) {
		games.removeAll(exps.getGames());
	}

	public static int[] GetHotGames(Context c){
		String hotstring = Utilities.makeHTTPGetRequest("http://www.boardgamegeek.com/xmlapi2/hot?type=boardgame");
		NodeList items = Utilities.getDocumentFromXML(hotstring).getElementsByTagName("item");
		int end = items.getLength();
		int[] games = new int[end];
		
		for(int i = 0; i < end; i++){
			games[i] = Integer.parseInt(Utilities.getAttribute(items.item(i), "id"));
		}
		
		return games;
	}
	
	public static int[] Search(String query, int limit, Context c){
		query = query.replace(" ", "%20");
		String result = Utilities.makeHTTPGetRequest("http://www.boardgamegeek.com/xmlapi2/search?query="+query+"&type=boardgame,boardgameexpansion");
		NodeList items = Utilities.getDocumentFromXML(result).getElementsByTagName("item");
		int end = items.getLength() < limit ? items.getLength() : limit;
		int[] games = new int[end];
		
		
		for(int i = 0; i < end; i++){
			games[i] = Integer.parseInt(Utilities.getAttribute(items.item(i), "id"));
		}
		
		return games;
	}
	
	// non-static
	public GameCollection(String username){
		this.games = new ArrayList<Game>();
		this.username = username;
	}
	
	public GameCollection(String xml, String username, Context c) {
		this(gamesFromString(xml,c),username);
	}
	
	public GameCollection(ArrayList<Game> games, String username){
		this.games = games;
		this.username = username;
	}
	
	private static ArrayList<Game> gamesFromString(String xml, Context c){
		Document doc = Utilities.getDocumentFromXML(xml);
		
		// extract games
		ArrayList<Game> games = new ArrayList<Game>();
		NodeList items = doc.getElementsByTagName("item");
		int end = items.getLength();
		for(int i = 0; i < end; i++){
			games.add(new Game(items.item(i),false,c));
		}
		return games;
	}

	private static GameCollection httpOrCache(String uri, String username, Context c){
		String result = Utilities.makeHTTPGetRequest(uri);
		GameCollection col;
		
		if(result != null){
			// save collection
			col = new GameCollection(result,username,c);
			StorageManager.saveUserCollection(col, username, c);
			
			return col;
		}
		
		col = StorageManager.loadUserCollection(username, c);
		
		if(col != null) Toast.makeText(c, "User loaded from cache.", Toast.LENGTH_SHORT).show();
		
		return col;
	}

	// public methods
	
	public int getCount(){
		return games.size();
	}
	
	public Game getIndex(int index){
		return games.get(index);
	}
	
	public String getUsername(){
		return username;
	}
	
	public GameCollection getGamesFor(GameFilter filter, Context c){
		ArrayList<Game> matches = new ArrayList<Game>();
		
		for(Game g : games){
			if(filter.verifyGame(g)) matches.add(g);
		}
		
		return new GameCollection(matches,this.getUsername());
	}
	
	public ArrayList<Game> getGames(){
		return games;
	}
	
	public void addGame(Game game){
		games.add(game);
		sortGames();
	}
	
	public void addCollection(GameCollection other){
		games.addAll(other.getGames());
		removeDuplicates(games);
		sortGames();
	}
	
	private void removeDuplicates(ArrayList<Game> games){
		ArrayList<Integer> ids = new ArrayList<Integer>(games.size());
		ArrayList<Integer> remove = new ArrayList<Integer>();
		for(int i = 0; i < games.size(); i++){
			int id = games.get(i).getId();
			if(ids.contains(id)) remove.add(i);
			ids.add(id);
		}
		Collections.reverse(remove);
		for(int index : remove) games.remove(index);
	}
	
	private void sortGames(){
		Collections.sort(games, Game.ComparatorName);
	}
	
	public int getNumMissingDetailed(){
		return Utilities.numMissingDetailed(games);
	}
	
	public double getRatioUserRated(){
		int rated = 0;
		for(Game g : games) if(g.getUsersRating(-1) > -1) rated++;
		return ((double)rated)/games.size();
	}
	
	public Iterator<Game> iterator() {
		return games.iterator();
	}
}
