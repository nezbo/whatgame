package dk.nezbo.whatboardgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class GameListActivity extends Activity implements OnItemSelectedListener{

	private Context context;
	
	private ArrayList<Game> games;
	private GamesListAdapter adapter;
	private ListView lv;
	private Spinner sortBy;
	
	private int lastSorted = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gamelist);
		
		init();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		Intent intent = new Intent(this, GameListActivity.class);
		Bundle bundle = new Bundle();
		Bundle oldbundle = getIntent().getExtras();
		
		
		bundle.putIntArray("ids", oldbundle.getIntArray("ids"));
		bundle.putString("header", oldbundle.getString("header"));
		bundle.putInt("sort", this.lastSorted);
		
		System.out.println("Orientation Changed");
		intent.putExtras(bundle);
		startActivity(intent);
	}

	private void init() {
		context = this;
		// data
		Bundle bundle = getIntent().getExtras();
		
		int[] ids = bundle.getIntArray("ids");
		String header = bundle.getString("header");
		lastSorted = bundle.getInt("sort");
		
		games = new ArrayList<Game>();
		
		for(int id : ids){
			games.add(StorageManager.loadGame(id, this));
		}
		if(lastSorted != 0) Collections.sort(games,getSorter(lastSorted));
		
		// games adapter
		adapter = new GamesListAdapter(this,games);
		
		lv = (ListView) findViewById(R.id.lvListGames);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Utilities.startGameIntent(position, games, context);
			}
		});
		
		// sort by -adapter
		sortBy = (Spinner) findViewById(R.id.spSortMethod);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.sort_options, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sortBy.setAdapter(adapter);
		sortBy.setOnItemSelectedListener(this);
		
		// other
		((TextView)findViewById(R.id.tvListHeader)).setText(header);
		((TextView)findViewById(R.id.tvListCount)).setText(""+games.size()+(games.size() > 1 ? " Games" : " Game"));
	}
	
	private Comparator<Game> getSorter(int sort) {
		Comparator<Game> choice = null;
		switch(sort){
		case 2: choice = Game.ComparatorPlayers; break;
		case 3: choice = Game.ComparatorRank; break;
		case 4: choice = Game.ComparatorRating; break;
		case 5: choice = Game.ComparatorUserRating; break;
		case 6: choice = Game.ComparatorYear; break;
		case 7: choice = Game.ComparatorPlayingTime; break;
		case 8: choice = Game.ComparatorAge; break;
		default: choice = Game.ComparatorName; break;
		}
		return choice;
	}
	
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		handleSort(pos+1);
	}
	
	public void onNothingSelected(AdapterView<?> view) {
		System.out.println("Nothing selected for sort");
	}
	
	public void handleSort(int sortNum){
		if(sortNum == this.lastSorted || sortNum*-1 == this.lastSorted){
			lastSorted = lastSorted*-1;
			Collections.reverse(games);
		}else{
			lastSorted = sortNum;
			Collections.sort(games, getSorter(lastSorted));
		}
		
		adapter.notifyDataSetChanged();
		lv.smoothScrollToPosition(0, 0);
	}
}
