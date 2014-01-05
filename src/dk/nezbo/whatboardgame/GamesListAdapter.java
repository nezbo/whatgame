package dk.nezbo.whatboardgame;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GamesListAdapter extends BaseAdapter implements Runnable{

	private ArrayList<Game> games;
	private Context context;

	public GamesListAdapter(Context c, ArrayList<Game> games) {
		context = c;
		this.games = games;
		
		new Thread(this).start();
	}

	public int getCount() {
		return games.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ImageView image;
		TextView title,extra;
		
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.game_item, null);
		}
		
		image = (ImageView) v.findViewById(R.id.ivListImage);
		title = (TextView) v.findViewById(R.id.tvListTitle);
		extra = (TextView) v.findViewById(R.id.tvListExtra);
		
		// set content
		Game cur = games.get(position);
		
		image.setImageBitmap(cur.getThumbnail());
		title.setText(cur.getName());
		extra.setText(makeExtraText(cur));
		
		// return
		return v;
	}

	private CharSequence makeExtraText(Game cur) {
		return cur.getPlayersString();
	}

	public void run() {
		for(Game g : games) g.getThumbnail();
		System.out.println(">> All Images loaded.");
	}

}
