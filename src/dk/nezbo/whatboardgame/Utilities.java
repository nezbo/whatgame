package dk.nezbo.whatboardgame;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Utilities {

	private static final int IO_BUFFER_SIZE = 4 * 1024;
	
	public static String getElementValue(Node elem) {
		Node child;
		if (elem != null) {
			if (elem.hasChildNodes()) {
				for (child = elem.getFirstChild(); child != null; child = child
						.getNextSibling()) {
					if (child.getNodeType() == Node.TEXT_NODE) {
						return child.getNodeValue();
					}
				}
			}
		}
		return "";
	}

	public static Node getFirstChild(Node parent, String name) {
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node cur = children.item(i);
			String localName = cur.getNodeName();
			if (localName.equals(name)
					&& cur.getNodeType() == Node.ELEMENT_NODE) {
				return cur;
			}
		}
		return null;
	}

	public static Node getNextSibling(Node current) {
		Node next = current.getNextSibling();
		while (next != null && next.getNodeType() != Node.ELEMENT_NODE) {
			next = next.getNextSibling();
		}
		return next;
	}

	public static Node getNumberChild(Node parent, String name, int number) {
		int count = 1;
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node cur = children.item(i);
			String localName = cur.getNodeName();
			if (localName.equals(name)
					&& cur.getNodeType() == Node.ELEMENT_NODE) {
				if (count == number)
					return cur;
				count++;
			}

		}
		return null;
	}

	public static String getChildValue(Node target, String name) {
		return getElementValue(getFirstChild(target, name));
	}

	public static String getAttribute(Node target, String attribute) {
		NamedNodeMap attributes = target.getAttributes();

		// if(attributes == null) return null;

		for (int i = 0; i < attributes.getLength(); i++) {
			Node attr = attributes.item(i);
			if (attr.getNodeName().equals(attribute)) {
				return attr.getNodeValue();
			}
		}
		return null;
	}

	public static Document getDocumentFromXML(String xml) {
		Document doc = null;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;

			db = dbf.newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = db.parse(is);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return doc;
	}

	public static Bitmap loadBitmap(String url) {
		Bitmap bitmap = null;
		InputStream in = null;
		BufferedOutputStream out = null;

		try {
			in = new BufferedInputStream(new URL(url).openStream(),
					IO_BUFFER_SIZE);

			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
			copy(in, out);
			out.flush();

			final byte[] data = dataStream.toByteArray();
			BitmapFactory.Options options = new BitmapFactory.Options();
			// options.inSampleSize = 1;

			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
					options);
		} catch (IOException e) {
			System.err.println("Could not load Bitmap from: " + url);
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				// System.err.println("Failed to load Bitmap from internet.");
			}
		}

		return bitmap;
	}

	private static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	public static String makeHTTPGetRequest(String uri) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();

			request.setURI(new URI(uri));

			HttpResponse response = client.execute(request);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}

			in.close();

			return sb.toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static void showProgressBeforeList(final Activity c, final int[] gameIds, final String listname){
		final ArrayList<Game> games = new ArrayList<Game>(gameIds.length);
		
		// setting up loading progress
		final ProgressDialog pdialog = new ProgressDialog(c);
		pdialog.setCancelable(true);
		pdialog.setMessage("Initializing");
		pdialog.setTitle("Loading...");
		pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pdialog.setProgress(0);
		pdialog.setMax(1);
		pdialog.show();

		Thread t = new Thread(new Runnable() {

			public void run() {
				int total = gameIds.length;
				
				postSetMax(pdialog, total + 1);

				for (int g : gameIds) {
					postMessage(pdialog, "Downloading games");
					games.add(new Game(g,c));
					postIncrement(pdialog);
				}

				// display opening list
				postMessage(pdialog, "Making game list");
				Utilities.startGameListIntent(new GameCollection(games,listname), listname, c);
				postIncrement(pdialog);

				pdialog.dismiss();

				// unlock orientation again
				handler.post(new Runnable() {
					public void run() {
						c.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					}
				});
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
			
			Handler handler = new Handler();

		});
		t.start();
	}
	
	public static void startGameIntent(int targetIndex, ArrayList<Game> games, Context c) {
		Game target = games.get(targetIndex);
		
		Intent next = new Intent("dk.nezbo.bggrandom.GAME");
		Bundle bundle = new Bundle();
		bundle.putInt("id", target.getId());
		if(targetIndex > 0) bundle.putIntegerArrayList("previous", Utilities.getIds(games,0,targetIndex-1));
		if(targetIndex < games.size()-1) bundle.putIntegerArrayList("next", Utilities.getIds(games,targetIndex+1,games.size()-1));
		next.putExtras(bundle);

		c.startActivity(next);
	}
	
	public static ArrayList<Integer> getIds(ArrayList<Game> games, int startId, int endId){
		ArrayList<Integer> result = new ArrayList<Integer>(endId-startId);
		
		for(int i = startId; i <= endId; i++){
			try{
				result.add(games.get(i).getId());
			}catch (IndexOutOfBoundsException e){/* shhh */}
		}
		
		return result;
	}

	public static void startGameListIntent(GameCollection games, String header, Context c) {
		Intent i = new Intent("dk.nezbo.bggrandom.GAMELIST");
		Bundle b = new Bundle();

		// create package
		int[] ids = new int[games.getCount()];
		for (int index = 0; index < games.getCount(); index++)
			ids[index] = games.getIndex(index).getId();

		b.putIntArray("ids", ids);
		b.putString("header", header);
		i.putExtras(b);
		c.startActivity(i);
	}
	
	public static void startYoutubeIntent(String uri, Context c) {
		c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
	}
	
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(),
				MeasureSpec.AT_MOST);
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	public static int numMissingDetailed(ArrayList<Game> games){
		int i = 0;
		for(Game g : games) if(!g.hasDetailed() && !g.detailsInCache()) i++;
		return i;
	}

	public static void showKeyboard(final EditText editText, final Context c){
	    editText.setOnFocusChangeListener(new OnFocusChangeListener() {
	        public void onFocusChange(View v, boolean hasFocus) {
	            editText.post(new Runnable() {
	                public void run() {
	                    InputMethodManager inputMethodManager= (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
	                    inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
	                }
	            });
	        }
	    });
	    editText.requestFocus();
	}
	
	public static int parseOrZero(String number){
		try{
			return Integer.parseInt(number);
		}catch(NumberFormatException e){
			
		}
		return 0;
	}
}
