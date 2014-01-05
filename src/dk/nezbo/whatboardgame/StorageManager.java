package dk.nezbo.whatboardgame;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class StorageManager {
	
	private static final int IMAGE_MAX_WIDTH = 1024;
	private static final int IMAGE_MAX_HEIGHT = 1024;

	public static void saveImage(Bitmap bitmap, String filename, Context c) {
		if(bitmap == null) return;
		
		try {
			FileOutputStream fos = c.openFileOutput(filename,
					Context.MODE_PRIVATE);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
			System.out.println("Image saved to cache.");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Bitmap loadImage(String filename, Context c) {
		if (!fileExists(filename, c))
			return null;

		try {
			FileInputStream fis = c.openFileInput(filename);
			System.out.println("Image loaded from cache.");
			return BitmapFactory.decodeStream(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void saveUserCollection(GameCollection collection,
			String username, Context c) {
		// save game id list for user
		StringBuilder builder = new StringBuilder();
		ArrayList<Game> games = collection.getGames();

		for (Game g : games) {
			builder.append(g.getId());
			builder.append("\n");
		}
		saveStringToFile(builder.toString(),
				getUserCollectionFilename(username), c);

		// save individual games
		for (Game g : games) {
			saveGame(g, c);
		}

		System.out.println("User Collection saved to cache.");
	}

	private static void saveGame(Game game, Context c) {
		saveStringToFile(game.getSimpleAsString(), getGameFilename(game.getId()),c);
	}
	
	public static void saveGameDetails(final Game game, final Context c) {
		new Thread(new Runnable(){

			public void run() {
				String details = game.getDetailedAsString();
				
				if(!details.equals("")){
					saveStringToFile(details,getGameDetailsFilename(game.getId()),c);
				}else{
					System.err.println("ERROR: Unable to save game: name="+game.getName()+", id="+game.getId());
				}
				
			}
			
		}).start();
	}

	public static Game loadGame(int id, Context c) {
		String file = getGameFilename(id);
		if(fileExists(file,c)){
			return new Game(loadStringFromFile(file,c),false,c); // simple
		}
		file = getGameDetailsFilename(id);
		if(fileExists(file,c)){
			return new Game(loadStringFromFile(file,c),c); // advanced
		}
		return null;
	}
	
	public static Node loadGameDetails(int gameId, Context c) {
		try{
			String filename = getGameDetailsFilename(gameId);
			if(fileExists(filename,c)){
				String xml = loadStringFromFile(filename,c);
				Document doc = Utilities.getDocumentFromXML(xml);
				return doc.getFirstChild();
			}
		}catch(NullPointerException e){ 
			// something wrong with file, just pretend it wasn't there :)
		}

		return null;
	}

	private static void saveStringToFile(String text, String filename, Context c) {
		FileOutputStream fos;
		try {
			fos = c.openFileOutput(filename, Context.MODE_PRIVATE);
			Writer writer = new OutputStreamWriter(fos);

			writer.write(text);
			writer.flush();
			writer.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String loadStringFromFile(String filename, Context c) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					c.openFileInput(filename), "UTF-8"));
			StringBuilder builder = new StringBuilder();

			String temp = null;
			while ((temp = reader.readLine()) != null) {
				builder.append(temp);
				builder.append("\n");
			}
			reader.close();
			
			return builder.toString();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static GameCollection loadUserCollection(String username, Context c) {
		String filename = getUserCollectionFilename(username);
		if (!fileExists(filename, c))
			return null;
		
		// load ids
		String result = loadStringFromFile(filename,c);
		String[] ids = result.split("\n");
		System.out.println("User Collection loaded from cache.");

		// load individual games
		ArrayList<Game> games = new ArrayList<Game>();

		for (String sid : ids) {
			games.add(StorageManager.loadGame(Integer.parseInt(sid), c));
		}

		return new GameCollection(games, username);

	}

	public static String getUserCollectionFilename(String username) {
		return username + "-user.txt";
	}
	
	public static String getGameFilename(int gameId){
		return gameId + "-game.txt";
	}
	
	public static String getGameDetailsFilename(int gameId) {
		return gameId + "-gameDetails.txt";
	}

	public static boolean fileExists(String filename, Context c) {
		return c.getFileStreamPath(filename).exists();
	}

	public static String getThumbnailFilename(int gameId) {
		return gameId+"-thumbnail.jpg";
	}
	
	public static String getImageFilename(int gameId){
		return gameId + "-image.jpg";
	}
	
	public static float calcScaleFactor(int width, int height, int reqWidth, int reqHeight) {
		if(width < reqWidth || height < reqHeight) return 1;
		
		float ratio;
		if(width > height){
			ratio = (int) (((float)width) / reqWidth);
		}else{
			ratio = (int) (((float)height) / reqHeight);
		}
		return ratio;
	}

	public static Bitmap scale(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float scale = calcScaleFactor(width,height,IMAGE_MAX_WIDTH,IMAGE_MAX_HEIGHT);
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap result = Bitmap.createBitmap(bitmap,0,0,width,height,matrix,false);
		bitmap.recycle();
		return result;
	}
}
