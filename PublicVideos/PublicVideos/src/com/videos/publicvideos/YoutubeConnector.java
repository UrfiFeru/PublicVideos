package com.videos.publicvideos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

public class YoutubeConnector {
	private YouTube youtube; 
	private YouTube.Search.List query;
	public static String nextPageToken="";
	// Your developer key goes here
	public static final String KEY = "AIzaSyCHqrtdT-WXaKAHWUDkF1URdBKkpf6kO1o";
	
	public YoutubeConnector(Context context) { 
		youtube = new YouTube.Builder(new NetHttpTransport(), 
				new JacksonFactory(), new HttpRequestInitializer() {			
			@Override
			public void initialize(HttpRequest hr) throws IOException {}
		}).setApplicationName("SimplePlayer").build();
		
		try{
			query = youtube.search().list("id,snippet");
			query.setKey(KEY);
			query.setMaxResults((long)10);
			query.setType("video");
			query.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)");								
		}catch(IOException e){
			Log.d("YC", "Could not initialize: "+e);
		}
	}
	
	public ArrayList<SearchResults> search(String keywords, String sort, int page){
		
		query.setQ(keywords);
		query.setOrder(sort);
	
		if(page!=1) query.setPageToken(nextPageToken);
		try{
			SearchListResponse response = query.execute();
			List<SearchResult> results = response.getItems();
			nextPageToken=response.getNextPageToken();
			ArrayList<SearchResults> items = new ArrayList<SearchResults>();
			for(SearchResult result:results){
				SearchResults item = new SearchResults();
				item.setTitle(result.getSnippet().getTitle());
				item.setDescription(result.getSnippet().getDescription());
				item.setThumbnailUrl(result.getSnippet().getThumbnails().getDefault().getUrl());
				item.setId(result.getId().getVideoId());
				item.setProvider("Youtube");				
				items.add(item);			
			}
			return items;
		}catch(IOException e){
			Log.d("YC", "Could not search: "+e);
			return null;
		}		
	}
}
