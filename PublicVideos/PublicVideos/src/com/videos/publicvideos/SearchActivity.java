package com.videos.publicvideos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
	static ArrayList<SearchResults> searchResults = new ArrayList<SearchResults>();

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

	public static void SearchVideo(String search, int Page) {
		String uri = "https://api.dailymotion.com/videos?search=" + search
				+ "&fields=id,title,owner.screenname,thumbnail_120_url&page="
				+ Page;
		new RequestTask().execute(uri);
		textView.setText("Search Results (" + search + ")");
	}

	public void onClickBtn(View v) {
		String searchTxt = edit.getText().toString();
		searchTxt.trim();
		if (!searchTxt.isEmpty()) {
			String temp = searchTxt;
			if (searchTxt.contains(" ")) {
				temp = searchTxt.replace(" ", "+");
			}
			searchTerm = temp;
			progress.show();
			SearchVideo(temp, 1);
			pageCount = 1;
			ShowMore = true;
			searchResults.clear();
		} else {
			Toast.makeText(context, "Please Enter Something",
					Toast.LENGTH_SHORT).show();
		}
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
						CreateView();
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
		listView.setAdapter(new MyCustomBaseAdapter(context, searchResults));

		btnLoadMore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (ShowMore) {
					progress.show();
					SearchVideo(searchTerm, ++pageCount);
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
				Intent myIntent = new Intent(context, MainActivity.class);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				myIntent.putExtra("key", fullObject.getId());
				myIntent.putExtra("title", fullObject.getTitle());
				context.startActivity(myIntent);
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
		String uri = "https://api.dailymotion.com/videos?search=" + searchTerm
				+ "&fields=id,title,owner.screenname,thumbnail_120_url&page="
				+ 1;
		new RequestTask().execute(uri);
		listView = (ListView) findViewById(R.id.ListView01);
		edit = (EditText) findViewById(R.id.searchText);
		textView = (TextView) findViewById(R.id.ListHeadingText);
		TextView textView = (TextView) findViewById(R.id.ListHeadingText);
		textView.setText("Search Results (trailers)");
	}

}