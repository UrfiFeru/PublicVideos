package com.videos.publicvideos;

public class SearchResults {
	 private String id = "";
	 private String title = "";
	 private String owner = "";
	 private String ThumbnailUrl = "";
	 private String Provider = "";

	 public void setId(String id) {
	  this.id = id;
	 }

	 public String getId() {
	  return id;
	 }

	 public void setTitle(String title) {
	  this.title = title;
	 }

	 public String getTitle() {
	  return title;
	 }

	 public void setOwner(String owner) {
	  this.owner = owner;
	 }

	 public String getOwner() {
	  return owner;
	 }

	 public void setThumbnailUrl(String ThumbnailUrl) {
		  this.ThumbnailUrl = ThumbnailUrl;
	 }

	 public String getThumbnailUrl() {
		  return ThumbnailUrl;
	 }
	 public void setProvider(String Provider) {
		  this.Provider = Provider;
	 }

	 public String getProvider() {
		  return Provider;
	 }
}