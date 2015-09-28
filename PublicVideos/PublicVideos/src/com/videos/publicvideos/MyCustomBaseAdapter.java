package com.videos.publicvideos;

import java.util.ArrayList;

import com.squareup.picasso.Picasso;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyCustomBaseAdapter extends BaseAdapter {
 private static ArrayList<SearchResults> searchArrayList;
 private Context context1;
 
 private LayoutInflater mInflater;

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

 public View getView(int position, View convertView, ViewGroup parent) {
  ViewHolder holder;
  if (convertView == null) {
   convertView = mInflater.inflate(R.layout.custom_row_view, null);
   holder = new ViewHolder();
   holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
   holder.txtOwner = (TextView) convertView.findViewById(R.id.owner);
   holder.txtProvider = (TextView) convertView.findViewById(R.id.provider);
   holder.imageUrl = (ImageView) convertView.findViewById(R.id.img);
   

   convertView.setTag(holder);
  } else {
   holder = (ViewHolder) convertView.getTag();
  }
  
  holder.txtTitle.setText(searchArrayList.get(position).getTitle());
  holder.txtOwner.setText(searchArrayList.get(position).getOwner());
  holder.txtProvider.setText(searchArrayList.get(position).getProvider());
  Picasso.with(context1).load(searchArrayList.get(position).getThumbnailUrl()).placeholder(R.drawable.images).error(R.drawable.download).resize(140,120).centerInside().into(holder.imageUrl);
  return convertView;
 }

 static class ViewHolder {
  TextView txtTitle;
  TextView txtOwner;
  TextView txtProvider;
  ImageView imageUrl;
 }

}