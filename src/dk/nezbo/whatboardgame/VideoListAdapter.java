package dk.nezbo.whatboardgame;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class VideoListAdapter extends BaseAdapter{

	private ArrayList<Video> videos;
	private Context context;

	public VideoListAdapter(Context c, ArrayList<Video> videos) {
		context = c;
		this.videos = videos;
	}

	public int getCount() {
		return videos.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		TextView title,category;
		
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.video_item, null);
		}
		
		title = (TextView) v.findViewById(R.id.tvVideoTitle);
		category = (TextView) v.findViewById(R.id.tvVideoCategory);
		
		// set content
		Video cur = videos.get(position);
		
		title.setText(cur.getTitle());
		category.setText(cur.getCategory());
		
		// return
		return v;
	}
}
