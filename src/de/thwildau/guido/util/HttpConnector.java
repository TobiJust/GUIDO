package de.thwildau.guido.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Observable;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.AsyncTask;
import android.util.Log;

/**
 * This class establishes a http connection to the web server to
 * communicate via him with the data base. It is observed by the 
 * Activity which initiated the data exchange.
 * @author GUIDO
 * @version 2013-12-13
 * @see Observable
 */
public class HttpConnector extends Observable {

	/**
	 * The url the connection shall be established to.
	 */
	private String url;

	/**
	 * A list of key value pairs which contains the data to transmit.
	 */
	private List<NameValuePair> data;

	/**
	 * Constructor setting the assigned url and data.
	 * @param url The url the connection shall be established to.
	 * @param data The list of key value pairs which contains the data to transmit.
	 */
	public HttpConnector(String url, List<NameValuePair> data) {
		this.data = data;
		this.url = url;
	}

	/**
	 * Creates a new instance of an AsyncTask and execute it by calling 
	 * {@link DownloadWebpageTask#execute(String...)}.
	 */
	public void executeTask() {
			new DownloadWebpageTask().execute(url);
	}

	/**
	 * Uses AsyncTask to create a task away from the main UI thread. This task takes a 
	 * URL string and uses it to create an HttpUrlConnection. Once the connection
	 * has been established, the AsyncTask downloads the contents of the webpage as
	 * an InputStream. Finally, the InputStream is converted into a string, which is
	 * returned to the UI by the AsyncTask's onPostExecute method.
	 * @author GUIDO
	 * @version 2013-12-13
	 * @see AsyncTask
	 */
	private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

		/**
		 * Computations in this method are performed in the background thread.
		 * Calls downloadUrl().
		 * @param urls The parameters of the task (in this case URLs)
		 */
		@Override
		protected String doInBackground(String... urls) {
			return performHttpPost(urls[0]);
		}

		/**
		 * invoked on the UI thread after the background computation finishes. 
		 * The result of the background computation is passed to this step as a parameter.
		 * Sets the {@link DatabaseInteractor}s result and notifies the observers about the 
		 * change. 
		 * @param result The background computations result.
		 */
		@Override
		protected void onPostExecute(String response) {
			DatabaseInteractor.setResponse(response);
			setChanged();
			notifyObservers();
		}

		/**
		 * Performs a http post for the assigned URL by using a {@link HttpClient}. 
		 * Transmits the data and saves the response in a {@link HttpResponse}.
		 * Gets the responses content with a stream and writes it to a String which
		 * will be returned.
		 * @param url The url the connection shall be established to.
		 * @return The responses content.
		 */
		private String performHttpPost(String url) {
			// Create a new HttpClient and Post Header
			HttpParams params = new BasicHttpParams();
//			HttpConnectionParams.setConnectionTimeout(params, 10000);
//			HttpConnectionParams.setSoTimeout(params, 10000);
			HttpClient httpclient = new DefaultHttpClient(params);
			HttpPost httppost = new HttpPost(url);
			HttpResponse response = null;
			try {
				// TODO: Fix password bug (by using utf-8 on server and client side?)
				httppost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
				httppost.setEntity(new UrlEncodedFormEntity(data));
				Log.i("HTTPPOST", "SENDING REQUEST");
				// Execute HTTP Post Request
				response = httpclient.execute(httppost);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.i("HTTPPOST", "READING RESPONSE");
			BufferedReader buff;
			String content = "";
			try {
				buff = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line;
				while((line = buff.readLine()) != null)
					content += line;
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
				return null;
			}
			return content;
		}
	}
}
