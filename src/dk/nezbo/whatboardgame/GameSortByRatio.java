package dk.nezbo.whatboardgame;

import java.util.Comparator;

public class GameSortByRatio implements Comparator<Game>{
	
	private int players;
	
	public GameSortByRatio(int numberOfPlayers){
		players = numberOfPlayers;
	}

	public int compare(Game first, Game second) {
		return new Float(first.getRatioForPlayers(players)).compareTo(second.getRatioForPlayers(players));
	}

}
