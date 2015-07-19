package com.gnail737.youtubewebview;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class VideoDownloader extends HandlerThread { 
	
	private static final String TAG = "VideoDownloader";

	private static VideoDownloader singletonInstance = null;
	
	private static String fileSavePath;
	
	//handler for Main Thread and Background Thread(this thread)
	static Handler mainHandler;
	static Handler thisHandler;
	WeakReference<Context> mainContext;

	public final static int MESSAGE_EXTRACT_URL = 3;
	
	public VideoDownloader() {
		super(TAG);
	}
	
	private VideoDownloader(Context ctx, Handler handler) {
		super(TAG);
		mainHandler = handler;
		mainContext = new WeakReference<Context>(ctx);
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(state)){
			fileSavePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		}
	}
	
	//this factory mehtod must be called from main thread
	public static synchronized VideoDownloader getDownloaderInstance(
				YoutubeActivity pActivity) {
		if (singletonInstance == null) {
			singletonInstance = new VideoDownloader(pActivity, pActivity.getSingletonHandler());
			return singletonInstance;
		}
		//there is chance that background thread outlive foreground activity so ref to main Context is not valid anymore
		else if (singletonInstance.mainContext.get() != null 
				&& singletonInstance.mainContext.get() != pActivity) 
		{
			Log.e(TAG, "Singleton Instance's Context changed !!!!!");
			//quiting previous thread.
			singletonInstance.quit();
			singletonInstance = new VideoDownloader(pActivity, pActivity.getSingletonHandler());
			return singletonInstance;
		}
		else
		    return singletonInstance;
	}
		
	@Override
	public synchronized void start() {
		if (!isAlive()){ //protect against re-entrancy
			super.start();
			super.getLooper();
		}
	}

    protected void onLooperPrepared() {
    	thisHandler = new Handler() {
    		@Override
    		public void handleMessage(Message msg) {
    			if (msg.what == MESSAGE_EXTRACT_URL) {
    				Log.i(TAG, "Got a request to extract from Video URL"+ ((VideoItem)msg.obj).getmDownloadUrl());
    				handleExtractURLRequest((VideoItem)msg.obj);
    			}
    		}};
    }
    
	private void handleExtractURLRequest(VideoItem item) {
		final Context mContext = mainContext.get();
		if (mContext == null) return; //main thread been GCed no need to do anything;
		
		String rawResponse;
		try {
			rawResponse = HTTPCommUtil.getUrl(item.getmDownloadUrl());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		//Matcher matcher = p.matcher(rawResponse);
		int startIndex = rawResponse.indexOf("url_encoded_fmt_stream_map");
		int nextIndexOne = rawResponse.indexOf("url=http", startIndex);
		int nextIndexTwo = rawResponse.indexOf("url=http", nextIndexOne+8);
		String completeUrl = Uri.decode(rawResponse.substring(nextIndexOne, nextIndexTwo+1));
		
		if (completeUrl.indexOf("=") == -1) return;
		
		String matchedUrl = completeUrl.substring(4, completeUrl.indexOf("\\\\u0026"));
		
		//remove all white spaces
		if (!(new File(fileSavePath+"/YoutubeVideo")).isDirectory()) {//no existing directory create one
			if (!(new File(fileSavePath+"/YoutubeVideo")).mkdirs()) 
			{throw new RuntimeException("Unable to create directory!");}
		}
		String filePath = fileSavePath+"/YoutubeVideo/" + item.getmTitle().replaceAll("\\s", "")
				                                                          .replaceAll("\\W", "_");
		//String filePath = VideoItem.fileSavePath+"/YoutubeVideo/VideoPlayer";
		filePath += ".mp4";
		final String finalFilePath = filePath;
		
		//Before downloading make Progress Bar visible
		
		mainHandler.obtainMessage(YoutubeActivity.MSG_DOWNLOAD_STARTED, item).sendToTarget();;
		
		try {
			HTTPCommUtil.writeUrlBytes(matchedUrl, filePath, 
			new HTTPCommUtil.PublishProgressListener() {	
				@Override
				public void reportProgress(String p) {
					mainHandler.obtainMessage(YoutubeActivity.MSG_UPDATE_PROGRESS_BAR, p).sendToTarget();;
				}
				@Override
				public void onDownloadFinished() {
					mainHandler.obtainMessage(YoutubeActivity.MSG_DOWNLOAD_FINISHED).sendToTarget();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void sendDownloadingMsg(VideoItem item) {
		if (thisHandler != null) {
			thisHandler.sendMessageDelayed(thisHandler.obtainMessage(MESSAGE_EXTRACT_URL, item), 
					    1000);
		}
	}
}

