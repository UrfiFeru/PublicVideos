package com.videos.publicvideos;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.StrictMode;

public class Vimeo {
    private static final String VIMEO_SERVER = "https://api.vimeo.com";
    private static String token = "23ca6d63e78b430c847e8e067d329d0f";
    private static String tokenType = "bearer";

    public Vimeo(String token) {
        this(token, "bearer");
    }

    public Vimeo(String token, String tokenType) {
    	Vimeo.token = token;
        Vimeo.tokenType = tokenType;
    }

    public static ArrayList<SearchResults> searchVideos(String query, String pageNumber, String itemsPerPage) throws IOException, JSONException {
        String apiRequestEndpoint = "/videos?page=" + pageNumber + "&per_page=" + itemsPerPage + "&fields=uri,name,pictures" + "&query=" + query;
        return apiRequest(apiRequestEndpoint, HttpGet.METHOD_NAME, null, null);
    }

//    public VimeoResponse getTextTracks(String videoEndPoint) throws IOException {
//        return apiRequest(new StringBuffer(videoEndPoint).append("/texttracks").toString(), HttpGet.METHOD_NAME, null, null);
//    }
//
//    public VimeoResponse getTextTrack(String videoEndPoint, String textTrackId) throws IOException {
//        return apiRequest(new StringBuffer(videoEndPoint).append("/texttracks/").append(textTrackId).toString(), HttpGet.METHOD_NAME, null, null);
//    }
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

    private static ArrayList<SearchResults> apiRequest(String endpoint, String methodName, Map<String, String> params, File file) throws IOException, JSONException {
    	HttpClient client = new DefaultHttpClient();
        HttpRequestBase request = null;
        String url = null;
        if (endpoint.startsWith("http")) {
            url = endpoint;
        } else {
            url = new StringBuffer(VIMEO_SERVER).append(endpoint).toString();
        }
        if (methodName.equals(HttpGet.METHOD_NAME)) {
            request = new HttpGet(url);
        } else if (methodName.equals(HttpPost.METHOD_NAME)) {
            request = new HttpPost(url);
        }
        request.addHeader("Accept", "application/vnd.vimeo.*+json; version=3.2");
        request.addHeader("Authorization", new StringBuffer(tokenType).append(" ").append(token).toString());
        HttpResponse response = client.execute(request);
        String responseAsString = null;
        int statusCode = response.getStatusLine().getStatusCode();
    	if (statusCode != 204) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            responseAsString = out.toString("UTF-8");
            out.close();
        }
        JSONObject json = null;
        try {
            json = new JSONObject(responseAsString);
        } catch (Exception e) {
            json = new JSONObject();
        }
        ArrayList<SearchResults> tempSearch = new ArrayList<SearchResults>();
		for (int i = 0; i < json.getJSONArray("data").length(); i++) {
			JSONObject object = (JSONObject) json.getJSONArray(
					"data").get(i);
			Map<String, String> out = new HashMap<String, String>();
			parse(object, out);
			SearchResults temp = new SearchResults();
			temp.setTitle(out.get("name"));
//			temp.setOwner("By: " + out.get("owner.screenname"));
			temp.setId(out.get("uri").substring(out.get("uri").lastIndexOf('/')+1));
			JSONArray tJson = new JSONArray(out.get("sizes"));
			for(int j=0;j<tJson.length();j++)
			{
				parse((JSONObject)tJson.get(j),out);
				if(out.get("width").equals("200"))
				{
					temp.setThumbnailUrl(out.get("link"));
					break;
				}
			}
			temp.setProvider("Vimeo");
			tempSearch.add(temp);
		}
		return tempSearch;
    }
}