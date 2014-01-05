package dk.nezbo.whatboardgame;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PlayerScoreAdapter extends BaseAdapter{
	
	private Game game;
	private Context c;
	
	private ArrayList<String> options;
	
	public PlayerScoreAdapter(Game game, Context c){
		this.game = game;
		this.c = c;
		
		// extract options
		options = game.getPlayerNumberOptions();
	}

	public int getCount() {
		return options.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		TextView title;
		ProgressBar values;
		
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) c
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.numplayers_item, null);
		}
		
		title = (TextView) v.findViewById(R.id.tvPlayersNumber);
		values = (ProgressBar) v.findViewById(R.id.pbPlayersProgress);
		
		// set content
		String numbers = options.get(position);
		title.setText(numbers);
		
		int best = game.getVotesBest(numbers);
		int rec = game.getVotesRecommended(numbers);
		int notRec = game.getVotesNotRecommended(numbers);
		int sum = best + rec + notRec;
		
		values.setMax(sum);
		values.setProgress(best);
		values.setSecondaryProgress(best + rec);
		
		// return
		return v;
	}

}
