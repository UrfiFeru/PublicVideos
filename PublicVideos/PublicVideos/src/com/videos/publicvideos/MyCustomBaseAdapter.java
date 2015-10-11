package com.videos.publicvideos;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import com.squareup.picasso.Picasso;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MyCustomBaseAdapter extends BaseAdapter {

	public static Handler UIHandler;
	public static String StaticimageURL;
	public static String StaticfileName;
	public static String StaticProvider;
	public static Boolean toAskIfOverride = false;
	private static ArrayList<SearchResults> searchArrayList;
	private Context context1;
	private LayoutInflater mInflater;
	static {
		UIHandler = new Handler(Looper.getMainLooper());
	}

	public static void runOnUI(Runnable runnable) {
		UIHandler.post(runnable);
	}

	public MyCustomBaseAdapter(Context context, ArrayList<SearchResults> results) {
		searchArrayList = results;
		context1 = context;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return searchArrayList.size();
	}

	public Object getItem(int position) {
		return searchArrayList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.custom_row_view, null);
			holder = new ViewHolder();
			holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
			holder.txtOwner = (TextView) convertView.findViewById(R.id.owner);
			holder.txtProvider = (TextView) convertView
					.findViewById(R.id.provider);
			holder.imageUrl = (ImageView) convertView.findViewById(R.id.img);
			holder.DownloadBtn = (ImageView) convertView
					.findViewById(R.id.DownloadBtn);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		OnClickListener DownloadClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (searchArrayList.get(position).getDownloadState()==1) {
					Toast.makeText(context1,
							"The File is already being downloaded",
							Toast.LENGTH_SHORT).show();
				} else if(searchArrayList.get(position).getDownloadState()==0) {
					Toast.makeText(context1,
							searchArrayList.get(position).getTitle() + " has started downloading",
							Toast.LENGTH_LONG).show();
					searchArrayList.get(position).setDownloadState(1);
					StaticProvider = searchArrayList.get(position)
							.getProvider();
					if (searchArrayList.get(position).getProvider() == "DailyMotion") {
						final String url = "http://www.dailymotion.com/video/"
								+ searchArrayList.get(position).getId();
						final String fileName = searchArrayList.get(position)
								.getTitle() + ".mp4";
						StaticfileName = fileName;
						new Thread() {
							public void run() {
								try {
									DownloadFileDM(url, fileName);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}.start();
					} else if (searchArrayList.get(position).getProvider() == "Vimeo") {
						final String url = "https://player.vimeo.com/video/"
								+ searchArrayList.get(position).getId();
						final String fileName = searchArrayList.get(position)
								.getTitle() + ".mp4";
						StaticfileName = fileName;
						new Thread() {
							public void run() {
								try {
									DownloadFileVimeo(url, fileName);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}.start();
					} else if (searchArrayList.get(position).getProvider() == "Youtube") {
						final String url = "http://www.dailymotion.com/video/"
								+ searchArrayList.get(position).getId();
						final String fileName = searchArrayList.get(position)
								.getTitle() + ".mp4";
						StaticfileName = fileName;
						new Thread() {
							public void run() {
								try {
									DownloadFileYT(url, fileName);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}.start();
					}
				}
				else if(searchArrayList.get(position).getDownloadState()==2)
				{
					Toast.makeText(context1,
							"The File has been downloaded",
							Toast.LENGTH_SHORT).show();
				}
			}
		};
		holder.txtTitle.setText(searchArrayList.get(position).getTitle());
		holder.txtOwner.setText(searchArrayList.get(position).getOwner());
		holder.txtProvider.setText(searchArrayList.get(position).getProvider());
		Picasso.with(context1)
				.load(searchArrayList.get(position).getThumbnailUrl())
				.placeholder(R.drawable.images).error(R.drawable.download)
				.resize(140, 120).centerInside().into(holder.imageUrl);
		holder.DownloadBtn.setOnClickListener(DownloadClick);
		return convertView;
	}

	static class ViewHolder {
		TextView txtTitle;
		TextView txtOwner;
		TextView txtProvider;
		ImageView imageUrl;
		ImageView DownloadBtn;
	}

	public void DownloadFileDM(String imageURL, String fileName)
			throws Exception {
		URL url = new URL(imageURL);
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url.toURI());
		ResponseHandler<String> handler = new BasicResponseHandler();
		String response = "";
		try {
			response = client.execute(request, handler);
			response = response.substring(response.indexOf("buildPlayer"));
			response = response.substring(response.indexOf("qualities"));
			response = response.substring(response.indexOf("video\\/mp4"));
			response = response.substring(response.indexOf("http:"),
					response.indexOf("}]") - 1);
			response = response.replaceAll("\\\\", "");
		} catch (IOException e) {
			e.printStackTrace();
		}
		StaticimageURL = response;
		WriteToFile(response, fileName);
	}

	public void DownloadFileVimeo(String imageURL, String fileName)
			throws Exception {
		URL url = new URL(imageURL);
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url.toURI());
		ResponseHandler<String> handler = new BasicResponseHandler();
		String response = "";
		try {
			response = client.execute(request, handler);
			response = response.substring(response.indexOf("cdn_url") + 7);
			response = response.substring(response.indexOf("url") + 3);
			response = response.substring(response.indexOf("http"),
					response.indexOf(",") - 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		StaticimageURL = response;
		WriteToFile(response, fileName);
	}

	public void DownloadFileYT(String imageURL, String fileName)
			throws Exception {
		int begin, end;
		String tmpstr = null;
		String result;
		try {
			URL url = new URL(
					"http://youtubeinmp3.com/fetch/?video=http://www.youtube.com/watch?v=i62Zjga8JOM");
			// URL url=new
			// URL("https://www.youtube.com/watch?v=y12-1miZHLs&nomobile=1");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			InputStream stream = con.getInputStream();
			InputStreamReader reader = new InputStreamReader(stream);
			StringBuffer buffer = new StringBuffer();
			char[] buf = new char[2262144];
			int chars_read;
			while ((chars_read = reader.read(buf, 0, 2262144)) != -1) {
				buffer.append(buf, 0, chars_read);
			}
			tmpstr = buffer.toString();

			begin = tmpstr.indexOf("url_encoded_fmt_stream_map=");
			end = tmpstr.indexOf("&", begin + 27);
			if (end == -1) {
				end = tmpstr.indexOf("\"", begin + 27);
			}
			tmpstr = URLDecoder.decode(tmpstr.substring(begin + 27, end));

		} catch (MalformedURLException e) {
			throw new RuntimeException();
		} catch (IOException e) {
			throw new RuntimeException();
		}

		Vector<String> url_encoded_fmt_stream_map = new Vector();
		begin = 0;
		end = tmpstr.indexOf(",");

		while (end != -1) {
			url_encoded_fmt_stream_map.addElement(tmpstr.substring(begin, end));
			begin = end + 1;
			end = tmpstr.indexOf(",", begin);
		}

		url_encoded_fmt_stream_map.addElement(tmpstr.substring(begin,
				tmpstr.length()));
		String result1 = "";
		Enumeration<String> url_encoded_fmt_stream_map_enum = url_encoded_fmt_stream_map
				.elements();
		while (url_encoded_fmt_stream_map_enum.hasMoreElements()) {
			tmpstr = (String) url_encoded_fmt_stream_map_enum.nextElement();
			begin = tmpstr.indexOf("itag=");
			if (begin != -1) {
				end = tmpstr.indexOf("&", begin + 5);

				if (end == -1) {
					end = tmpstr.length();
				}

				int fmt = Integer.parseInt(tmpstr.substring(begin + 5, end));

				if (fmt == 35) {
					begin = tmpstr.indexOf("url=");
					if (begin != -1) {
						end = tmpstr.indexOf("&", begin + 4);
						if (end == -1) {
							end = tmpstr.length();
						}
						result = URLDecoder.decode(tmpstr.substring(begin + 4,
								end));
						result1 = result;
						break;
					}
				}
			}
		}
		StaticimageURL = result1;
		WriteToFile(result1, fileName);
	}

	public FileOutputStream CreateFile(final String fileName) {
		String RootDir = Environment.getExternalStorageDirectory()
				+ File.separator + "Video";
		File RootFile = new File(RootDir);
		RootFile.mkdir();
		final File outputFile = new File(RootFile, fileName);
		FileOutputStream f = null;
		if (!outputFile.exists()) {
			try {
				outputFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (!toAskIfOverride) {
			final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						try {
							new Thread() {
								public void run() {
									try {
										toAskIfOverride = true;
										if (StaticProvider == "DailyMotion") {
											DownloadFileDM(StaticimageURL,
													StaticfileName);
										} else if (StaticProvider == "Vimeo") {
											DownloadFileVimeo(StaticimageURL,
													StaticfileName);
										} else if (StaticProvider == "Youtube") {
											DownloadFileYT(StaticimageURL,
													StaticfileName);
										}
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}.start();
						} catch (Exception e) {
							Log.i("", "");
						}
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						// No button clicked
						break;
					}
				}
			};
			MyCustomBaseAdapter.runOnUI(new Runnable() {
				@Override
				public void run() {
					AlertDialog.Builder ab = new AlertDialog.Builder(
							SearchActivity.SearchActivityContext);
					ab.setMessage(
							"A file with the filename '"
									+ fileName
									+ "' already exists. Do you want to overwrite it?")
							.setPositiveButton("Yes", dialogClickListener)
							.setNegativeButton("No", dialogClickListener)
							.show();
				}
			});
			return null;
		}

		try {
			f = new FileOutputStream(outputFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f;
	}

	public void WriteToFile(String response, String fileName) {
		try {

			FileOutputStream outStream = CreateFile(fileName);
			URLConnection ucon = new URL(response).openConnection();
			final int TIMEOUT_CONNECTION = 5000;// 5sec
			final int TIMEOUT_SOCKET = 30000;// 30sec

			// this timeout affects how long it takes for the app to realize
			// there's a connection problem
			ucon.setReadTimeout(TIMEOUT_CONNECTION);
			ucon.setConnectTimeout(TIMEOUT_SOCKET);
			// Define InputStreams to read from the URLConnection.
			// uses 3KB download buffer
			InputStream is = ucon.getInputStream();
			toAskIfOverride = false;
			StaticimageURL = "";
			StaticfileName = "";
			StaticProvider = "";
			if (outStream != null) {
				BufferedInputStream inStream = new BufferedInputStream(is,
						1024 * 5);
				byte[] buff = new byte[5 * 1024];

				// Read bytes (and store them) until there is nothing more to
				// read(-1)
				int len;
				while ((len = inStream.read(buff)) != -1) {
					outStream.write(buff, 0, len);
				}

				// clean up
				outStream.flush();
				outStream.close();
				inStream.close();
				for(int i=0;i<searchArrayList.size();i++)
				{
					if(fileName.contains(searchArrayList.get(i).getTitle()))
					{
						searchArrayList.get(i).setDownloadState(2);
						break;
					}
				}
			}
		} catch (Exception e) {
			Log.i(e.toString(), "a");
		}

	}
}