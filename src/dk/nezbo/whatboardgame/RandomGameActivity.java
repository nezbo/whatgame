package dk.nezbo.whatboardgame;

import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class RandomGameActivity extends Activity implements OnClickListener{

	private Handler handler;
	private Activity c;
	private static final int USERLIST_ACTIVITY = 0;

	private GameCollection original;
	private GameCollection current;
	private GameFilter filter = null;

	private EditText username;
	
	private Button bRandom, bBest, bAdd;
	private EditText players, playingTime, age;
	private Spinner category;

	private String error = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		init();
	}

	private void init() {
		handler = new Handler();
		c = this;
		username = (EditText) findViewById(R.id.etUsername);
		bRandom = (Button) findViewById(R.id.bRandom);
		bBest = (Button) findViewById(R.id.bBestMatch);
		bAdd = (Button) findViewById(R.id.bAddUser);
		players = (EditText) findViewById(R.id.etPlayers);
		playingTime = (EditText) findViewById(R.id.etPlayingTime);
		age = (EditText) findViewById(R.id.etAge);

		bRandom.setOnClickListener(this);
		bBest.setOnClickListener(this);

		// set name if in properties
		SharedPreferences prefs = this.getSharedPreferences("myPrefs",
				MODE_PRIVATE);
		username.setText(prefs.getString("username", ""));
		username.setSelection(username.getText().length());
		
		// click Add
		bAdd.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				// get search term
				AlertDialog.Builder alert = new AlertDialog.Builder(c);
				alert.setTitle("Enter Username");

				final EditText input = new EditText(c);
				alert.setView(input);

				alert.setPositiveButton("Add",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								final String query = input.getText().toString()
										.trim();

								username.setText(username.getText().toString()+","+query);
								username.setSelection(username.getText().length());
							}
						});

				alert.show();
				
				// hack to show keyboard
				Utilities.showKeyboard(input, c);
			}
		});
		
		// Category
		category = (Spinner) findViewById(R.id.spCategory);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.category_options, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		category.setAdapter(adapter);
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("ON RESTART");
		//original = null;
		//current = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		MenuInflater blow = this.getMenuInflater();
		blow.inflate(R.menu.randommenu, menu);

		return result;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		super.onMenuItemSelected(featureId, item);
		switch (item.getItemId()) {
		case R.id.menuAllGames:
			loadGames(false, new Runnable() {

				public void run() {
					if (original == null)
						return;
					Utilities.startGameListIntent(original, getHeader(), c);
				}

			}, "Retrieving all games");
			break;
		case R.id.menuValidGames:
			loadGames(false, new Runnable() {

				public void run() {
					if (current == null)
						return;
					Utilities.startGameListIntent(current, getHeader(), c);
				}

			}, "Looking back of boxes");
			break;
		case R.id.menuHot:
			Utilities.showProgressBeforeList(this, GameCollection.GetHotGames(this), "Hot Games");
			break;
		case R.id.menuSearch:
			// get search term
			AlertDialog.Builder alert = new AlertDialog.Builder(c);
			alert.setTitle("Search for game");

			final EditText input = new EditText(c);
			alert.setView(input);

			alert.setPositiveButton("Search",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							final String query = input.getText().toString()
									.trim();

							Utilities.showProgressBeforeList(c,
									GameCollection.Search(query, 50, c),
									"Results for \"" + query + "\"");
						}
					});

			alert.show();
			Utilities.showKeyboard(input, c);

			break;
		case R.id.menuUsers:
			startUsersIntent();
			break;
		case R.id.menuSettings:
			this.startActivity(new Intent("dk.nezbo.bggrandom.PREFS"));
			break;
		}
		return true;
	}

	private String getHeader() {
		if (filter.getPlayers() > 0 || filter.getTime() > 0) {
			return original.getUsername() + "'s valid games";

		} else {
			return original.getUsername() + "'s games";
		}
	}

	private void startUsersIntent() {
		this.startActivityForResult(new Intent("dk.nezbo.bggrandom.USERLIST"),
				USERLIST_ACTIVITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case USERLIST_ACTIVITY:
			String user = data.getExtras().getString("user");
			username.setText(user);
			break;
		}
	}

	private void sortByBest(GameCollection games, int lastPlayers) {
		// ranking method
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		int method = Integer.parseInt(sharedPrefs.getString("pref_algorithm", "2"));
		if(method == 2)	method = games.getRatioUserRated() > 0.7 ? 1 : 0;
		
		// if just rank
		if (lastPlayers < 1){
			Collections.sort(games.getGames(), method == 0 ? Game.ComparatorRank : Game.ComparatorUserRating);
			return;
		}
		
		Collections.sort(games.getGames(), new Game.ComparatorScore(lastPlayers, method));
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bRandom: {
			startRandomGame();
			break;
		}
		case R.id.bBestMatch: {

			loadGames(true, new Runnable() {

				public void run() {
					if (current == null)
						return;

					sortByBest(current, filter.getPlayers());

					if (current.getCount() > 0) {
						Utilities.startGameIntent(0, current.getGames(), c);
					} else {
						Toast.makeText(c,
								"No games for those requirements",
								Toast.LENGTH_SHORT).show();
					}
				}

			}, "Evaluating BGG ranks and players");
			break;
		}
		}

		// show any error
		if (error != null) {
			Toast.makeText(this, error, Toast.LENGTH_LONG).show();
			System.out.println("Displaying error: " + error);
			error = null;
		}
	}

	private void startRandomGame() {
		loadGames(false, new Runnable() {

			public void run() {
				if (error != null)
					return;

				// No games?
				if (current == null) {
					error = "No internet connection or cache";
					return;
				}

				if (current.getCount() < 1) {
					error = "No games found";
					return;
				}

				// get random game
				/*Random rand = new Random();
				int number = rand.nextInt(current.getCount());

				Game target = current.getIndex(number);*/
				
				Collections.shuffle(current.getGames());

				// start next intent
				Utilities.startGameIntent(0, current.getGames(), c);
			}

		}, "Selecting random game");
	}

	private void postMessage(final ProgressDialog p, final String message) {
		handler.post(new Runnable() {
			public void run() {
				p.setMessage(message);
			}
		});
	}

	private void postIncrement(final ProgressDialog p) {
		handler.post(new Runnable() {
			public void run() {
				p.incrementProgressBy(1);
			}
		});
	}

	private void postSetMax(final ProgressDialog p, final int max) {
		handler.post(new Runnable() {
			public void run() {
				p.setMax(max);
			}
		});
	}

	private boolean somethingChanged(String name, GameFilter old, GameFilter current) {
		return original == null || !original.getUsername().equals(name)
				|| !current.equals(old) || settingsChanged();
	}

	private void loadGames(boolean loadDetails, final Runnable doAfter,
			final String messageAfter) {
		// lock orientation
		this.setRequestedOrientation(this.getResources().getConfiguration().orientation);

		// get username
		final String name = username.getText().toString().trim();
		System.out.println("Username: \"" + name + "\"");

		if (name.equals("")) {
			error = "No username entered";
			return;
		}

		// save username
		SharedPreferences prefs = this.getSharedPreferences("myPrefs",
				MODE_PRIVATE);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString("username", name);
		edit.commit();

		// settings
		int gamers = Utilities.parseOrZero(players.getText().toString());
		int playingTime = Utilities.parseOrZero(this.playingTime.getText().toString());
		int age = Utilities.parseOrZero(this.age.getText().toString());
		String sCategory = this.category.getSelectedItem().toString();
		
		final GameFilter newFilter = new GameFilter(gamers, playingTime, age, sCategory);
		final boolean finalDetails = loadDetails || newFilter.requiresDetails();

		if (!somethingChanged(name, filter, newFilter)) {
			doAfter.run();
			return;
		}

		// setting up loading progress
		final ProgressDialog pdialog = new ProgressDialog(this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Initializing");
		pdialog.setTitle("Loading...");
		pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pdialog.setProgress(0);
		pdialog.setMax(1);
		pdialog.show();

		Thread t = new Thread(new Runnable() {

			public void run() {
				Looper.prepare();
				// check if it has already been found
				if (original == null || !original.getUsername().equals(name) || settingsChanged()) {
					postMessage(pdialog, "Loading User");

					original = GameCollection.GetUsersGames(name, c);

					postIncrement(pdialog);

					if (original == null) {
						error = "Internet connection required";
						return;
					}
					current = original;
					filter = null;
				}

				// loading aditional info
				if (finalDetails) {
					int total = current.getNumMissingDetailed();
					postSetMax(pdialog, total + 2);

					for (Game g : current.getGames()) {
						if (!g.hasDetailed() && !g.detailsInCache()) {
							postMessage(pdialog, "Downloading: " + g.getName());
							g.loadDetails();
							postIncrement(pdialog);
						}
					}
				} else {
					postIncrement(pdialog);
				}

				// changed settings
				if (!newFilter.equals(filter)) {
					current = original.getGamesFor(newFilter, c);

					filter = newFilter;
				}

				// run whatever has to go next
				postMessage(pdialog, messageAfter);
				doAfter.run();
				postIncrement(pdialog);

				pdialog.dismiss();

				// unlock orientation again
				handler.post(new Runnable() {
					public void run() {
						c.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					}
				});
			}

		});
		t.start();
	}
	
	private boolean settingsChanged() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(c);
		return sharedPrefs.getBoolean("pref_expansions", false) == original.hasExpansions;
	}
}