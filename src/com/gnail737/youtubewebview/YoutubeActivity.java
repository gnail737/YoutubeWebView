package com.gnail737.youtubewebview;


import java.lang.ref.WeakReference;
import java.util.Date;




import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class YoutubeActivity extends Activity {
	private static final String TAG = "YoutubeActivity";
	private static final String URL_PATTERN = "youtube.com/watch?v=";
    private static final String SUB_URL = "https://m.youtube.com/feed/subscriptions";
    public static final int MSG_UPDATE_PROGRESS_BAR = 0;
    public static final int MSG_DOWNLOAD_STARTED = 1;
    public static final int MSG_DOWNLOAD_FINISHED = 2;
    
    private static boolean progressVisible = false;
	//references to UI items
	TextView mTextView;
	WebView mWebView;
	ProgressBar mProgressBar;
	public ProgressBar getProgressBar() {
		return mProgressBar;
	}
	String trueTitle, tempTitle;
	//handler etc
	static MainThreadHandler mHandler = null;
	
	
	//factory method for MainThreadHandler;
	public MainThreadHandler getSingletonHandler() {
		if (mHandler == null) {
			mHandler = new MainThreadHandler(this);
		} else if (mHandler.mMainContext.get() == null){
			mHandler = new MainThreadHandler(this);
		}
		return mHandler;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_youtube);
		
		mTextView = (TextView) findViewById(R.id.titleTextView);
		mWebView = (WebView) findViewById(R.id.webView);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		
		final YoutubeActivity selfThis = this;
		//mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.setWebViewClient(new WebViewClient(){	
			@Override
			public void onPageFinished(final WebView view, String url){
				super.onPageFinished(view, url);
				}
			});
		
		   mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView webView, String title) {
				//Log.i(TAG, "current URL for this page is "+webView.getUrl());
				//Log.i(TAG, "current Title for this page is "+webView.getTitle());
				if (webView.getTitle().indexOf(" - YouTube") != -1) {
					final String uTitle = webView.getTitle().substring(0, webView.getTitle().indexOf(" - YouTube"));
					final String url = webView.getUrl();
			  	    if (url.indexOf(URL_PATTERN) != -1){
					    VideoItem vItem = new VideoItem(uTitle, url);
						VideoDownloader.sendDownloadingMsg(vItem);
				    }
				}
				
		    }
		});
		VideoDownloader.getDownloaderInstance(this).start();
		mWebView.loadUrl(SUB_URL);
	}

	@Override
	protected void onPause() {
		mWebView.onPause();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mWebView.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.youtube, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
    static class MainThreadHandler extends Handler {	
		private WeakReference<Context> mMainContext;
		private MainThreadHandler(Context context){
			super();
			mMainContext = new WeakReference<Context>(context);
		}
		@Override
		public void handleMessage(Message msg) {
			YoutubeActivity mainRef = (YoutubeActivity)mMainContext.get();
			if (mainRef == null) return;
			
			if (msg.what == MSG_UPDATE_PROGRESS_BAR) {
				if ( mainRef.getProgressBar() != null )
					mainRef.mProgressBar.setProgress(Integer.parseInt((String)msg.obj));
			} else if (msg.what == MSG_DOWNLOAD_STARTED) {
				VideoItem vItem = (VideoItem)msg.obj;
				mainRef.mTextView.setText(vItem.getmTitle());
				mainRef.mTextView.setVisibility(View.VISIBLE);
				mainRef.mProgressBar.setProgress(0);
				mainRef.mProgressBar.setVisibility(View.VISIBLE);
			} else if (msg.what == MSG_DOWNLOAD_FINISHED) {
				mainRef.mTextView.setText("");
				mainRef.mProgressBar.setVisibility(View.INVISIBLE);
				mainRef.mTextView.setVisibility(View.INVISIBLE);
			}
		}
	}
}
