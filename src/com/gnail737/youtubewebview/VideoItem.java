package com.gnail737.youtubewebview;

public class VideoItem {

	final String mTitle;
	final String mDownloadUrl;
	public VideoItem(String mTitle, String mDownloadUrl) {
		this.mTitle = mTitle;
		this.mDownloadUrl = mDownloadUrl;
	}
	public String getmTitle() {
		return mTitle;
	}
	public String getmDownloadUrl() {
		return mDownloadUrl;
	}
}
