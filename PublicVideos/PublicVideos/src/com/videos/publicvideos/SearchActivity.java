package com.videos.publicvideos;

import java.io.IOException;
import java.util.ArrayList;

import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Window;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;



import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity {
	static ListView listView;
	static EditText edit;
	static TextView textView;
	static String searchTerm = "trailers";
	static ProgressDialog progress;
	static boolean ShowMore = true;
	private static Context context;
	public static Handler UIHandler;
	private static int pageCount = 1;
	String sortBy="relevance";
	static MyCustomBaseAdapter adapter;
	static ArrayList<SearchResults> searchResults = new ArrayList<SearchResults>();
	
	
	private Handler handler;

	static {
		UIHandler = new Handler(Looper.getMainLooper());
	}

	public static void runOnUI(Runnable runnable) {
		UIHandler.post(runnable);
	}

	public static Map<String, String> parse(JSONObject json,
			Map<String, String> out) throws JSONException {
		Iterator<String> keys = json.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			String val = null;
			try {
				JSONObject value = json.getJSONObject(key);
				parse(value, out);
			} catch (Exception e) {
				val = json.getString(key);
			}

			if (val != null) {
				out.put(key, val);
			}
		}
		return out;
	}

	public static void SearchOnDailyMotion(String search, int Page) {
		String uri = "https://api.dailymotion.com/videos?search=" + search
				+ "&fields=id,title,owner.screenname,thumbnail_120_url&page="
				+ Page;
		new RequestTask().execute(uri);
		textView.setText("Search Results (" + search.replace('+', ' ') + ")");
	}

	public void onClickBtn(View v) throws IOException {
		String searchTxt = edit.getText().toString();
		searchTxt.trim();
		View view = this.getCurrentFocus();
		if (view != null) {  
		    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
		if (!searchTxt.isEmpty()) {
			String temp = searchTxt;
			if (searchTxt.contains(" ")) {
				temp = searchTxt.replace(" ", "+");
			}
			searchTerm = temp;
			progress.show();
			SearchOnDailyMotion(temp, 1);
			searchOnYoutube(temp);
			SearchOnVimeo(temp);
			pageCount = 1;
			ShowMore = true;
			searchResults.clear();
		} else {
			Toast.makeText(context, "Please Enter Something",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	public void onClickImg(View v) {
		//open popupwindow here
		final Dialog myDialog = new Dialog(this);
		myDialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		View view = LayoutInflater.from(this).inflate(
				R.layout.popup, null);
		final RadioGroup radio=(RadioGroup)view.findViewById(R.id.radioGroup1);
		final RadioButton r1=(RadioButton)view.findViewById(R.id.radio0);
		final RadioButton r2=(RadioButton)view.findViewById(R.id.radio1);
		final RadioButton r3=(RadioButton)view.findViewById(R.id.radio2);
		Button exit = (Button) view.findViewById(R.id.button1);
		exit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int selectedID=radio.getCheckedRadioButtonId();
				if(selectedID==r1.getId())
					sortBy="relevance";
				if(selectedID==r2.getId())
					sortBy="ranking";
				if(selectedID==r3.getId())
					sortBy="recent";
				
				myDialog.dismiss();
				
			}
		});
		myDialog.setContentView(view);
		myDialog.setTitle("settings");
		myDialog.show();
		
		myDialog.getWindow().setLayout(580, 540); // Controlling width and
													// height.
	}

	public static void GetDMResponse(String result) {
		try {
			if (result.contains("Exception")) {
				SearchActivity.runOnUI(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(context,
								"Internet connectivity not detected",
								Toast.LENGTH_LONG).show();
					}
				});
			} else {
				JSONObject resultObj = new JSONObject(result);
				if (!resultObj.getString("has_more").contains("true")) {
					ShowMore = false;
				}
				for (int i = 0; i < resultObj.getJSONArray("list").length(); i++) {
					JSONObject object = (JSONObject) resultObj.getJSONArray(
							"list").get(i);
					Map<String, String> out = new HashMap<String, String>();
					parse(object, out);
					SearchResults temp = new SearchResults();
					temp.setTitle(out.get("title"));
					temp.setOwner("By: " + out.get("owner.screenname"));
					temp.setId(out.get("id"));
					temp.setThumbnailUrl(out.get("thumbnail_120_url"));
					temp.setProvider("DailyMotion");
					searchResults.add(temp);
				}
				progress.dismiss();
				
				SearchActivity.runOnUI(new Runnable() {
					@Override
					public void run() {
						if(pageCount==1)
							CreateView();
						else
							adapter.notifyDataSetChanged();
					}
				});
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void CreateView() {
		Button btnLoadMore = new Button(context);
		btnLoadMore.setText("Load More");
		if (listView.getFooterViewsCount() == 0)
			listView.addFooterView(btnLoadMore);
		adapter = new MyCustomBaseAdapter(context, searchResults);
		listView.setAdapter(adapter);

		btnLoadMore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (ShowMore) {
					//progress.show();
					pageCount++;
					SearchOnDailyMotion(searchTerm, pageCount);
				} else {
					Toast.makeText(context, "More results not available",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				Object o = listView.getItemAtPosition(position);
				SearchResults fullObject = (SearchResults) o;
				if(fullObject.getProvider().compareTo("DailyMotion")==0)
				{	Intent myIntent = new Intent(context, MainActivity.class);
					myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					myIntent.putExtra("key", fullObject.getId());
					myIntent.putExtra("title", fullObject.getTitle());
					myIntent.putExtra("player", "DM");
					context.startActivity(myIntent);
				}
				else if (fullObject.getProvider().compareTo("Youtube")==0)
				{
					Intent intent = new Intent(context,YTPlayerActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("VIDEO_ID", fullObject.getId());
					context.startActivity(intent);
				}
				else if (fullObject.getProvider().compareTo("Vimeo")==0)
				{
					Intent intent = new Intent(context,MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("key", fullObject.getId());
					intent.putExtra("player", "Vimeo");
					context.startActivity(intent);
				}
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		context = getApplicationContext();
		progress = new ProgressDialog(this);
		progress.setTitle("Searching");
		progress.setMessage("Please wait while videos are being searched...");
		progress.show();
		String uri = makeUri(null,"trending", "1");
		new RequestTask().execute(uri);
		listView = (ListView) findViewById(R.id.ListView01);
		edit = (EditText) findViewById(R.id.searchText);
		
		handler=new Handler();
		
		textView = (TextView) findViewById(R.id.ListHeadingText);
		TextView textView = (TextView) findViewById(R.id.ListHeadingText);
		textView.setText("Showing:Trending Videos");
		
	}
	
	public static String makeUri(String search, String sort, String page)
	{
		return "https://api.dailymotion.com/videos?search=" + search +
				"&sort=" + sort
				+ "&fields=id,title,owner.screenname,thumbnail_120_url&page="
				+ page;
	}
	
	
	private void searchOnYoutube(final String keywords){
		new Thread(){
			public void run(){
				YoutubeConnector yc = new YoutubeConnector(SearchActivity.this);
				searchResults.addAll(yc.search(keywords,sortBy,pageCount));				
				/*handler.post(new Runnable(){
					public void run(){
						updateVideosFound();
					}
				});*/
			}
		}.start();
	}

	private void SearchOnVimeo(final String keywords){
		new Thread(){
			public void run(){
				try {
					searchResults.addAll(Vimeo.searchVideos(keywords,String.valueOf(pageCount),"10"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}
}