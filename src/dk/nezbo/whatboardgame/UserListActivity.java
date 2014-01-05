package dk.nezbo.whatboardgame;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class UserListActivity extends Activity{
	
	ListView list;
	String[] users;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.userlist);
		init();
	}

	private void init() {
		list = (ListView) findViewById(R.id.lvUserList);
		users = findCachedUsers();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.user_item,users);
		
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// make result and return
				Intent result = new Intent();
				Bundle b = new Bundle();
				b.putString("user", users[position]);
				result.putExtras(b);
				setResult(Activity.RESULT_OK,result);
				finish();
			}
			
		});
	}

	private String[] findCachedUsers() {
		String[] all = this.fileList();
		ArrayList<String> matches = new ArrayList<String>(16);
		for(String user : all){
			if(user.endsWith("-user.txt")) matches.add(user.substring(0,user.length()-9));
		}
		
		return matches.toArray(new String[matches.size()]);
	}

}
