package dk.nezbo.whatboardgame;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.graphics.Bitmap;

public class Game {

	public static final Comparator<Game> ComparatorName = new Comparator<Game>() {
		public int compare(Game lhs, Game rhs) {
			return lhs.getName().compareTo(rhs.getName());
		}
	};

	public static final Comparator<Game> ComparatorRank = new Comparator<Game>() {
		public int compare(Game lhs, Game rhs) {
			return Integer.valueOf(lhs.getRank()).compareTo(rhs.getRank());
		}
	};
	public static final Comparator<Game> ComparatorRating = new Comparator<Game>() {
		public int compare(Game lhs, Game rhs) {
			return Double.valueOf(rhs.getRating()).compareTo(lhs.getRating());
		}
	};
	public static final Comparator<Game> ComparatorUserRating = new Comparator<Game>() {
		public int compare(Game lhs, Game rhs) {
			return Integer.valueOf(rhs.getUsersRating(5)).compareTo(lhs.getUsersRating(5));
		}
	};
	public static final Comparator<Game> ComparatorPlayers = new Comparator<Game>() {
		public int compare(Game lhs, Game rhs) {
			int min = lhs.getMinPlayers() - rhs.getMinPlayers();
			if (min != 0)
				return min;

			return lhs.getMaxPlayers() - rhs.getMaxPlayers();
		}
	};
	public static final Comparator<Game> ComparatorPlayingTime = new Comparator<Game>() {
		public int compare(Game lhs, Game rhs) {
			return Integer.valueOf(lhs.getPlayingtime()).compareTo(rhs
					.getPlayingtime());
		}
	};
	public static final Comparator<Game> ComparatorYear = new Comparator<Game>() {
		public int compare(Game lhs, Game rhs) {
			return Integer.valueOf(lhs.getYear()).compareTo(rhs
					.getYear());
		}
	};
	public static final Comparator<Game> ComparatorAge = new Comparator<Game>() {
		public int compare(Game lhs, Game rhs) {
			return Integer.valueOf(lhs.getPlayingAge()).compareTo(rhs
					.getPlayingAge());
		}
	};
	
	public static class ComparatorScore implements Comparator<Game>{
		
		private int players, method;
		
		public ComparatorScore(int players, int method){
			this.players = players;
			this.method = method;
		}

		public int compare(Game lhs, Game rhs) {
			return Double.valueOf(lhs.getScoreForPlayers(players, method)).compareTo(rhs.getScoreForPlayers(players, method));
		}
		
	}

	// Statics finished

	private Node simple = null;
	private Node detailed = null;
	private Bitmap thumbnail = null;
	private Bitmap image = null;

	private Context c;

	public Game(Node xml, boolean loadAdvanced, Context c) {
		this.simple = xml;
		this.c = c;

		if (loadAdvanced)
			loadDetails();
	}

	public Game(String doc, boolean loadDetails, Context c) {
		this.simple = Utilities.getDocumentFromXML(doc).getFirstChild();
		this.c = c;

		if (loadDetails)
			loadDetails();
	}

	public Game(String simple, String detailed, Context c) {
		this.simple = Utilities.getDocumentFromXML(simple).getFirstChild();
		this.detailed = Utilities.getDocumentFromXML(detailed).getFirstChild();
		this.c = c;
	}

	public Game(String advanced, Context c) {
		this.detailed = Utilities.getDocumentFromXML(advanced).getFirstChild();
		this.c = c;
	}

	public Game(int id, Context c) {
		this.c = c;
		loadDetails(id);
	}

	public boolean detailsInCache() {
		return StorageManager.fileExists(
				StorageManager.getGameDetailsFilename(getId()), c);
	}

	public void loadDetails() {
		loadDetails(getId());
	}

	private void loadDetails(int id) {
		if (detailed != null)
			return;

		detailed = StorageManager.loadGameDetails(id, c);

		if (detailed == null) { // not saved already :(
			String xml = Utilities
					.makeHTTPGetRequest("http://www.boardgamegeek.com/xmlapi2/thing?id="
							+ id + "&stats=1&videos=1");
			
			detailed = Utilities
					.getDocumentFromXML(xml)
					.getFirstChild().getFirstChild();
			
			StorageManager.saveGameDetails(this, c);
			System.out.println("Game details downloaded from BGG (id="
					+ getId() + ")");
		} else {
			System.out.println("Game details loaded from cache. (id=" + getId()
					+ ")");
		}
	}

	public boolean hasDetailed() {
		return detailed != null;
	}

	// from simple

	public String getName() {
		if (simple != null) {
			return Utilities.getChildValue(simple, "name");
		} else {
			return Utilities.getAttribute(
					Utilities.getFirstChild(detailed, "name"), "value"); // not
																			// sure
																			// if
																			// always
																			// first
		}
	}

	public int getYear() {
		try {
			return Integer.parseInt(getYearString());
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	public String getYearString(){
		if(simple != null){
			return Utilities.getChildValue(simple, "yearpublished");
		}else{
			return Utilities.getAttribute(Utilities.getFirstChild(detailed, "yearpublished"),"value");
		}
	}

	public String getImageUrl() {
		return Utilities.getChildValue(simple == null ? detailed : simple,
				"image");
	}

	public String getThumbnailUrl() {
		return Utilities.getChildValue(simple == null ? detailed : simple,
				"thumbnail");
	}

	public Bitmap getThumbnail() {
		if (thumbnail == null) {
			// load from cache?
			String filename = StorageManager.getThumbnailFilename(this.getId());
			thumbnail = StorageManager.loadImage(filename, c);
			if (thumbnail != null)
				return thumbnail;

			// from url then
			thumbnail = Utilities.loadBitmap(getThumbnailUrl());
			StorageManager.saveImage(thumbnail, filename, c);
		}

		return thumbnail;
	}

	public Bitmap getImage() { // no downloading, should be done elsewhere in
								// the background
		if (image == null) {
			String imageFilename = StorageManager.getImageFilename(getId());
			image = StorageManager.loadImage(imageFilename, c);
		}
		return image;
	}

	public int getId() {
		if (simple != null) {
			return Integer.parseInt(Utilities.getAttribute(simple, "objectid"));
		} else {
			return Integer.parseInt(Utilities.getAttribute(detailed, "id"));
		}
	}

	public int getMinPlayers() {
		try {
			if (simple != null) {
				return Integer
						.parseInt(Utilities.getAttribute(
								Utilities.getFirstChild(simple, "stats"),
								"minplayers"));
			} else {
				return Integer.parseInt(Utilities.getAttribute(
						Utilities.getFirstChild(detailed, "minplayers"),
						"value"));
			}

		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public int getMaxPlayers() {
		try {
			if (simple != null) {
				return Integer
						.parseInt(Utilities.getAttribute(
								Utilities.getFirstChild(simple, "stats"),
								"maxplayers"));
			} else {
				return Integer.parseInt(Utilities.getAttribute(
						Utilities.getFirstChild(detailed, "maxplayers"),
						"value"));
			}

		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public int getPlayingtime() {
		try {
			if (simple != null) {
				return Integer
						.parseInt(Utilities.getAttribute(
								Utilities.getFirstChild(simple, "stats"),
								"playingtime"));
			} else {
				return Integer.parseInt(Utilities.getAttribute(
						Utilities.getFirstChild(detailed, "playingtime"),
						"value"));
			}

		} catch (NumberFormatException e) {
			return -1;
		} catch (NullPointerException e) {
			return -1;
		}

	}

	public int getNumberOwners() {
		return Integer.parseInt(Utilities.getAttribute(Utilities.getFirstChild(
				simple == null ? detailed : simple, "stats"), "numowned"));
	}

	public int getUsersRating(int defaultValue) {
		System.out.println(getId());
		try{
			return Integer.parseInt(Utilities.getAttribute(Utilities.getFirstChild(
					Utilities.getFirstChild(simple,
							"stats"), "rating"), "value"));
		}catch(NumberFormatException e){
			return defaultValue;
		}catch(NullPointerException e){
			return defaultValue;
		}
	}

	public String getSimpleAsString() {
		return convertNodeToString(simple);
	}

	public String getDetailedAsString() {
		return convertNodeToString(detailed);
	}

	private String convertNodeToString(Node target) {
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(target), new StreamResult(
					writer));
			String output = writer.getBuffer().toString()
					.replaceAll("\n|\r", "");
			return output;
		} catch (TransformerException e) {
			System.err.println("Game toString failed");
		}
		return null;
	}

	public String getPlayersString() {
		int min = this.getMinPlayers();
		int max = this.getMaxPlayers();

		if (min == 0 || max == 0)
			return "";
		if (min == max)
			return min + " players";
		return min + " - " + max + " players";
	}

	// from detailed
	
	public String getType(){
		loadDetails();
		return Utilities.getAttribute(detailed, "type");
	}

	public String getDescription() {
		loadDetails();
		return Utilities.getChildValue(detailed, "description");
	}
	
	public double getRating() {
		loadDetails();
		return Double.parseDouble(Utilities.getAttribute(Utilities
				.getFirstChild(Utilities.getFirstChild(
						Utilities.getFirstChild(detailed, "statistics"),
						"ratings"), "average"), "value"));
	}

	public ArrayList<Video> getVideos(String language) {
		ArrayList<Video> videos = new ArrayList<Video>(5);
		try {
			Node videosRoot = Utilities.getFirstChild(detailed, "videos");
			NodeList videosNode = videosRoot.getChildNodes();

			for (int i = 0; i < videosNode.getLength(); i++) {
				Node cur = videosNode.item(i);
				if (cur.getNodeType() == Node.ELEMENT_NODE
						&& correctLanguage(cur, language)) {
					videos.add(new Video(Utilities.getAttribute(cur, "title"),
							Utilities.getAttribute(cur, "category"), Utilities
									.getAttribute(cur, "link")));
				}
			}
		} catch (NullPointerException e) {
			System.err.println("Videos element missing from xml");
		}
		System.out.println(videos.size() + " videos found");
		return videos;
	}

	private boolean correctLanguage(Node cur, String language) {
		String lowerLang = language.toLowerCase(Locale.ENGLISH);
		return language == null
				|| lowerLang.equals("all")
				|| Utilities.getAttribute(cur, "language").toLowerCase(Locale.ENGLISH)
						.equals(lowerLang);
	}

	// suggested players methods

	public ArrayList<String> getPlayerNumberOptions() {
		loadDetails();

		Node suggPlayers = Utilities.getFirstChild(detailed, "poll");
		NodeList options = suggPlayers.getChildNodes();
		ArrayList<String> result = new ArrayList<String>(options.getLength());

		for (int i = 0; i < options.getLength(); i++) {
			if (options.item(i).getNodeType() == Node.ELEMENT_NODE) {
				result.add(Utilities.getAttribute(options.item(i), "numplayers"));
			}
		}
		return result;
	}

	public int getVotesBest(String numberOfPlayers) {
		loadDetails();

		Node results = getVoteResults(
				Utilities.getFirstChild(detailed, "poll"), numberOfPlayers);

		if (results == null)
			return -1; // illegal number of players

		try {
			return Integer.parseInt(Utilities.getAttribute(
					Utilities.getFirstChild(results, "result"), "numvotes"));
		} catch (NullPointerException e) {
			return 0;
		}

	}

	public int getVotesRecommended(String numberOfPlayers) {
		loadDetails();

		Node results = getVoteResults(
				Utilities.getFirstChild(detailed, "poll"), numberOfPlayers);

		if (results == null)
			return -1; // illegal number of players

		try {
			return Integer
					.parseInt(Utilities.getAttribute(
							Utilities.getNumberChild(results, "result", 2),
							"numvotes"));
		} catch (NullPointerException e) {
			return 0;
		}
	}

	public int getVotesNotRecommended(String numberOfPlayers) {
		loadDetails();

		Node results = getVoteResults(
				Utilities.getFirstChild(detailed, "poll"), numberOfPlayers);

		if (results == null)
			return -1; // illegal number of players

		try {
			return Integer
					.parseInt(Utilities.getAttribute(
							Utilities.getNumberChild(results, "result", 3),
							"numvotes"));
		} catch (NullPointerException e) {
			return 0;
		}
	}

	private Node getVoteResults(Node poll, String playerCount) {
		loadDetails();

		Node cur = Utilities.getFirstChild(poll, "results");
		while (cur != null) {
			if (Utilities.getAttribute(cur, "numplayers").equals(playerCount)) {
				return cur;
			}
			cur = Utilities.getNextSibling(cur);
		}
		return null;
	}
	
	public ArrayList<String> getCategories(){
		return getLinks("boardgamecategory");
	}
	
	public ArrayList<String> getMechanics(){
		return getLinks("boardgamemechanic");
	}
	
	public ArrayList<String> getBoardGameFamily(){
		return getLinks("boardgamefamily");
	}
	
	public ArrayList<String> getDesigners(){
		return getLinks("boardgamedesigner");
	}
	
	public ArrayList<String> getArtists(){
		return getLinks("boardgameartist");
	}
	
	public ArrayList<String> getPublishers(){
		return getLinks("boardgamepublisher");
	}
	
	private ArrayList<String> getLinks(String type){
		loadDetails();
		
		ArrayList<String> matches = new ArrayList<String>();
		
		Node cur = Utilities.getFirstChild(detailed, "link");
		while(cur != null){
			String curType = Utilities.getAttribute(cur, "type");
			if(curType.equals(type)){
				matches.add(Utilities.getAttribute(cur, "value"));
			}
			cur = Utilities.getNextSibling(cur);
			if(!cur.getNodeName().equals("link")) break;
		}
		
		return matches;
	}

	// more methods

	public boolean fitsPlayers(int number) {
		if (number == 0)
			return true;
		return this.getMinPlayers() <= number && this.getMaxPlayers() >= number;
	}

	public boolean fitsPlayingTime(int time) {
		if (time == 0)
			return true;
		return this.getPlayingtime() <= time;
	}
	
	public boolean fitsAge(int age){
		return age == 0 || getPlayingAge() <= age;
	}
	
	public boolean fitsCategory(String category){
		if(category.equals("ANY")) return true;
		
		loadDetails();
		
		if(getCategories().contains(category) || getMechanics().contains(category)) return true;
		
		/*Node cur = Utilities.getFirstChild(detailed, "link");
		while(cur != null){
			String type = Utilities.getAttribute(cur, "type");
			if((type.equals("boardgamecategory") || type.equals("boardgamemechanic")) && Utilities.getAttribute(cur, "value").equals(category)) return true;
			
			cur = Utilities.getNextSibling(cur);
			if(!cur.getNodeName().equals("link")) break;
		}*/
		
		return false;
	}

	public int getPlayingAge() {
		loadDetails();
		return Integer.parseInt(Utilities.getAttribute(
				Utilities.getFirstChild(detailed, "minage"), "value"));
	}
	
	public String getPlayingAgeString(){
		return getPlayingAge() +" and up";
	}

	public int getRank() {
		String value = getRankString();

		try {
			int result = Integer.parseInt(value);
			return result;
		} catch (NumberFormatException e) {
			return Integer.MAX_VALUE;
		}
	}

	public double getScoreForPlayers(int number, int method){
		if(!fitsPlayers(number)) return 0.0;
		
		float ratio = getRatioForPlayers(number);
		double rank = method == 0 ? getRank() : 11-getUsersRating(5);
		
		double curScore = rank / ratio;
		
		System.out.println("Score for: " + getName() + " = " + curScore
				+ " | rank = " + rank + " | ratio = " + ratio);
		return curScore;
	}
	
	public String getRankString() {
		loadDetails();
		Node rank = Utilities.getFirstChild(Utilities.getFirstChild(Utilities
				.getFirstChild(Utilities.getFirstChild(detailed, "statistics"),
						"ratings"), "ranks"), "rank");
		return Utilities.getAttribute(rank, "value");
	}

	// combinations

	public int getBestPlayers() {
		loadDetails();

		String highest = null;
		int value = 0;
		for (String option : getPlayerNumberOptions()) {
			int curValue = getVotesBest(option);
			if (curValue > value) {
				value = curValue;
				highest = option;
			}
		}

		try {
			return Integer.parseInt(highest);
		} catch (NumberFormatException e) {
			return 0;
		}

	}

	public float getRatioForPlayers(int players) {
		String p = "" + players;
		int good = this.getVotesBest(p) + this.getVotesRecommended(p) / 2;
		int bad = this.getVotesNotRecommended(p);
		
		if(good == 0) return 0.0001f;
		if(bad == 0) return 1.0f;
		return ((float) good) / bad;
	}

	// overwrites
	
	public int compare(Game lhs, Game rhs) {
		return lhs.getId() - rhs.getId();
	}


	@Override
	public boolean equals(Object o) {
		if(!o.getClass().equals(this.getClass())) return false;
		
		Game that = (Game) o;
		if(this.getId() == that.getId()) return true;
		return false;
	}

	@Override
	public int hashCode() {
		return this.getId();
	}

}
