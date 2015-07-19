package com.gnail737.youtubewebview;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.net.Uri;


public class HTTPCommUtil {
    public static final String ENDPOINT_REFRESH =
        "https://accounts.google.com/o/oauth2/token";
    public static final String ENDPOINT_SUBLIST =
        "https://gdata.youtube.com/feeds/api/users/default/newsubscriptionvideos";
    public static final String CLIENT_ID =
        "NULL"; //"2.0016PXQEYaXBiDfab32c31d3A3QDDE";
    public static final String CLIENT_SECRET =
        "NULL";
    public static final String REFRESH_TOKEN = 
    	"NULL";
    public static final String GRANT_TYPE = 
    	"refresh_token";
    
    public static interface PublishProgressListener {
    	public void reportProgress(String p);

		public void onDownloadFinished();
    }

    public static byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpsURLConnection connection =
            (HttpsURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public static String getUrl(String urlSpec) throws IOException {
        byte[] outputBytes = getUrlBytes(urlSpec);
        if (outputBytes != null)
            return new String(outputBytes, "UTF-8");
        else
            return null;
    }
    //another version of GET util method with support for http header
    public static byte[] getUrlBytes(String urlSpec, ArrayList<NameValuePair> params) throws IOException {
        URL url = new URL(urlSpec);
        HttpsURLConnection connection =
            (HttpsURLConnection)url.openConnection();
        try {
        	for (NameValuePair pair : params) {
        		connection.addRequestProperty(pair.getName(), pair.getValue());
        	}
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                return null;
            }

            String encoding = connection.getContentEncoding();
            int bytesCount = connection.getContentLength();
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public static String getUrl(String urlSpec, ArrayList<NameValuePair> params) throws IOException {
        byte[] outputBytes = getUrlBytes(urlSpec, params);
        if (outputBytes != null)
            return new String(outputBytes, "UTF-8");
        else
            return null;
    }
    
    public static void writeUrlBytes(String urlSpec, String filePath, PublishProgressListener mListener) throws IOException {
    	final int CHUNK_SIZE = 4096;
        URL url = new URL(urlSpec);
        //HackingCodePleaseRemoveWhenYouCan();
		
        HttpURLConnection connection =
            (HttpURLConnection)url.openConnection();
        FileOutputStream out = null;
        try {
        
            out = new FileOutputStream(filePath);
            InputStream in = null;
            int retryCount = 1;
            while (retryCount >= 0)
            try {
            	in = connection.getInputStream();
            	break;
            } catch(FileNotFoundException fnfE){
            	--retryCount;
            	if (retryCount < 0) throw fnfE;
            }
            
//            if (((HttpURLConnection) connection).getResponseCode() != HttpsURLConnection.HTTP_OK) {
//                return;
//            }

            //String encoding = connection.getContentEncoding();
            int bytesCount = connection.getContentLength();
            int bytesRead = 0, totalBytesRead = 0;
            byte[] buffer = new byte[CHUNK_SIZE];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
				mListener.reportProgress(String.valueOf((int)(((double)totalBytesRead/(double)bytesCount)*100)));
            }
            out.flush();
            mListener.onDownloadFinished();
        } catch(IOException ioe){
        	ioe.printStackTrace();
        }
          finally {
        	if (out != null) out.close();
            connection.disconnect();
        }
    }

    public static String prepareGSRefreshToken() {
    	String url = ENDPOINT_REFRESH;
        ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
        nvp.add(new NameValuePair("client_id", CLIENT_ID));
        nvp.add(new NameValuePair("client_secret", CLIENT_SECRET));
        nvp.add(new NameValuePair("refresh_token", REFRESH_TOKEN));
        nvp.add(new NameValuePair("grant_type", GRANT_TYPE));

        String token = null;
        try {
			String jsonString = getUrlOutputHTTPPOST(url, nvp);
			
		    JSONObject RootJSONObj = (JSONObject) new JSONTokener(jsonString).nextValue();
		    
		    token = RootJSONObj.getString("access_token");
		} catch (IOException e) {
			e.printStackTrace();
		}
        catch (JSONException e) {
			e.printStackTrace();
	    }
        return token;

    }
    
    public static String getSubListingXML(String accessToken) {
    	String url = ENDPOINT_SUBLIST;
    	String result = null;
    	ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
        nvp.add(new NameValuePair("Authorization", "Bearer "+accessToken));
    	try {
			result = getUrl(url, nvp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
    	
    }
    
    public static String getUrlPageForDownload(String vidUrl) {
    	String result = null;
    	ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
    	try {
    		result = getUrl(vidUrl, nvp);
    	} catch(IOException e){
    		e.printStackTrace();
    	}
    	return result;
    }
    
    //helper method used in getUrlOutputHTTPPOST() to encode params body

    static String getRequestBody(ArrayList<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair: params) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
    //another helper method to do HTTP POST operations

    public static String getUrlOutputHTTPPOST(String urlSpec,
                                              ArrayList<NameValuePair> params) throws IOException {
        URL url = new URL(urlSpec);
        HttpsURLConnection connection =
            (HttpsURLConnection)url.openConnection();
        try {
            //singals we are going use POST
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            //preparing all the paramters
            String requestBody = getRequestBody(params);

            OutputStreamWriter outW =
                new OutputStreamWriter(connection.getOutputStream());
            outW.write(requestBody);
            outW.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();

            return out.toString();
        } finally {
            connection.disconnect();
        }
    }
    /*
    static void HackingCodePleaseRemoveWhenYouCan() {         
	    //HACK coding started
	    TrustManager trm = (TrustManager) new X509TrustManager() {
	        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	            return null;
	        }
	
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
				// TODO Auto-generated method stub
			}
	
			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
				// TODO Auto-generated method stub
			}
	    };
	
	    SSLContext sc;
		try {
			sc = SSLContext.getInstance("SSL");
	
	    try {
			sc.init(null, new TrustManager[] { trm }, null);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    //HACK coding ended
    }
    */

}
