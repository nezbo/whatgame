package dk.nezbo.whatboardgame;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends Activity{
	
	private Context c;
	
	private TextView title, rank, rating, userRat, time, year, age;
	private ImageView image;
	private ScrollView sv;
	private LinearLayout videoLL, playerOptions, categoryList, mechanicList;
	
	private ArrayList<Integer> previous;
	private ArrayList<Integer> next;
	private Game game;
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_detailed);
		init();
	}

	private void init() {
		c = this;
		title = (TextView) findViewById(R.id.tvGameDetTitle);
		image = (ImageView) findViewById(R.id.ivGameDetImage);
		rank = (TextView) findViewById(R.id.tvGameDetRank);
		rating = (TextView) findViewById(R.id.tvGameDetRating);
		userRat = (TextView) findViewById(R.id.tvGameDetUserRating);
		time = (TextView) findViewById(R.id.tvGameDetPlayingTime);
		year = (TextView) findViewById(R.id.tvGameDetYear);
		age = (TextView) findViewById(R.id.tvGameDetSuggAge);
		sv = (ScrollView)findViewById(R.id.svGameDetScroll);
		videoLL = (LinearLayout)findViewById(R.id.llGameDetVideos);
		playerOptions = (LinearLayout) findViewById(R.id.llPlayerOptions);
		categoryList = (LinearLayout) findViewById(R.id.llGameDetCategories);
		mechanicList = (LinearLayout) findViewById(R.id.llGameDetMechanics);
		
		int id = getIntent().getExtras().getInt("id");
		loadGame(id);
		
        // populate ids for previous and next
        previous = getIntent().getExtras().getIntegerArrayList("previous");
        if(previous == null) previous = new ArrayList<Integer>();
        
        next = getIntent().getExtras().getIntegerArrayList("next");
        if(next == null) next = new ArrayList<Integer>();
		
		// detect horizontal scroll
        gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        sv.setOnTouchListener(gestureListener);
	}
	
    private void loadGame(int id) {
    	game = StorageManager.loadGame(id, this);
		title.setText(game.getName());
		image.setImageBitmap(game.getThumbnail());
		
		// data
		rank.setText(game.getRankString());
		rating.setText(""+game.getRating());
		int userRating = game.getUsersRating(-1);//getIntent().getExtras().getInt("rating");
		if(userRating > 0){
			userRat.setText("("+userRating+")");
		}else{
			userRat.setText("");
		}
		
		int iTime = game.getPlayingtime();
		time.setText(iTime == -1 ? "Unknown" : iTime+" minutes");
		
		year.setText(game.getYearString());
		age.setText(game.getPlayingAgeString());
		
		// player options
		playerOptions.removeAllViews();
		ArrayList<String> options = game.getPlayerNumberOptions();
		for(int i = 0; i < options.size(); i++){
			View v = this.getLayoutInflater().inflate(R.layout.numplayers_item, null);
			TextView title = (TextView) v.findViewById(R.id.tvPlayersNumber);
			ProgressBar values = (ProgressBar) v.findViewById(R.id.pbPlayersProgress);
			
			// set content
			String numbers = options.get(i);
			title.setText(numbers);
			
			int best = game.getVotesBest(numbers);
			int rec = game.getVotesRecommended(numbers);
			int notRec = game.getVotesNotRecommended(numbers);
			int sum = best + rec + notRec;
			
			values.setMax(sum);
			values.setProgress(best);
			values.setSecondaryProgress(best + rec);
			
			playerOptions.addView(v);
		}
		
		// categories and mechanics
		categoryList.removeAllViews();
		ArrayList<String> cats = game.getCategories();
		for(final String category : cats){
			View child = this.getLayoutInflater().inflate(R.layout.small_text, null);
			TextView text = (TextView) child.findViewById(R.id.tvSmallText);
			text.setText(category);
			text.setGravity(Gravity.LEFT);
			categoryList.addView(child);
		}
		
		mechanicList.removeAllViews();
		ArrayList<String> mecs = game.getMechanics();
		for(final String mechanic : mecs){
			View child = this.getLayoutInflater().inflate(R.layout.small_text, null);
			TextView text = (TextView) child.findViewById(R.id.tvSmallText);
			text.setText(mechanic);
			text.setGravity(Gravity.LEFT);
			mechanicList.addView(child);
		}
		
		// videos
		videoLL.removeAllViews();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		ArrayList<Video> vids = game.getVideos(sharedPrefs.getString("pref_language", "English"));
		
		for(final Video v : vids){
			View child = this.getLayoutInflater().inflate(R.layout.video_item,null);
			TextView title = (TextView) child.findViewById(R.id.tvVideoTitle);
			TextView category = (TextView) child.findViewById(R.id.tvVideoCategory);
			title.setText(v.getTitle());
			category.setText(v.getCategory());
			child.setOnClickListener(new OnClickListener(){

				public void onClick(View view) {
					Utilities.startYoutubeIntent(v.getURL(),c);
				}
				
			});
			
			videoLL.addView(child);
		}
		
		if(vids.isEmpty()){
			View child = this.getLayoutInflater().inflate(R.layout.small_text,null);
			((TextView)child.findViewById(R.id.tvSmallText)).setText("There are no videos for this game");
			videoLL.addView(child);
		}
		
		sv.smoothScrollTo(0, 0);
	}
    
    private void previousGame(){
    	if(previous.size() == 0) return;
    	
    	int id = previous.get(previous.size()-1);
    	
    	previous.remove(previous.size()-1);
    	next.add(0, game.getId());
    	
    	loadGame(id);
    }
    
    private void nextGame(){
    	if(next.size() == 0) return;
    	
    	int id = next.get(0);
    	
    	next.remove(0);
    	previous.add(game.getId());
    	
    	loadGame(id);
    }

	class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    nextGame();
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    previousGame();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

    }
}
