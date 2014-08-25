package org.raz.pdb.graphics;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;

import org.apache.http.HttpException;
import org.raz.pdb.R;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class Downloader {
	String uri;
	String dest;
	final int SUCCESS = 0;
	final int ERROR = -1;
	final int NO3DSDF = -2;
	final int CANCELED = -3;
	

	MoleculeViewActivity parent; // FIXME: this is not very good solution....
	
	public Downloader(MoleculeViewActivity parent, String uri, String dest) {
		Log.d("Downloader", "From " + uri + " To " + dest);

		this.uri = uri;
		this.dest = dest;
		this.parent = parent;
		
		String[] tmp = {};
		new DownloadTask().execute(tmp);
	}
	
	public class DownloadTask extends AsyncTask<String, Integer, Integer> {
		boolean isKilled = false;
		ProgressDialog progress = null;
		
		@Override
		protected void onPreExecute() {
			progress = new ProgressDialog(parent);

			progress.setTitle(parent.getString(R.string.downloading));
			progress.setMessage(parent.getString(R.string.pleaseWait));
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			progress.setButton(
					DialogInterface.BUTTON_NEGATIVE,
					"Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							isKilled = true;
							dialog.cancel(); 
						}
					}
			);
			progress.show();
		}

		@Override
		protected Integer doInBackground(String... dummy) {
			try {			
				URL url = new URL(uri);
				HttpURLConnection httpConn;
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parent);
				
				if (prefs.getBoolean(parent.getString(R.string.useProxy), false)) {
					String proxyAddress = prefs.getString(parent.getString(R.string.proxyHost), "");
					int proxyPort = Integer.parseInt(prefs.getString(parent.getString(R.string.proxyPort), "8080"));
					
					SocketAddress addr = new InetSocketAddress(proxyAddress, proxyPort);
					Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
					Log.d("Downloader", "Proxy enabled: " + proxyAddress + ":" + proxyPort);
					
					httpConn = (HttpURLConnection)url.openConnection(proxy);
				} else {
					httpConn = (HttpURLConnection)url.openConnection();
				}
				httpConn.setInstanceFollowRedirects(true);
				httpConn.setRequestMethod("GET");
				httpConn.connect();
				if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					throw new HttpException("File not found.");
				}
				
//				max = httpConn.getContentLength();
				// PDB web server doesn't send Content-Length header.
				// we cannot setProgressStyle after the dialog is showed.
//				Log.d("Downloader", "size :" + max);
//				if (max > 0) {
//					progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//					progress.setMax(max);				
//				}
				
				DataInputStream inp = new DataInputStream(httpConn.getInputStream());
				FileOutputStream out = new FileOutputStream(dest);
				byte[] buffer = new byte[10000];
				int read;
				boolean first = true;
				while ((read = inp.read(buffer)) > 0) {
					if (isKilled) return CANCELED;
					if (first) {
						first = false;
						String str = new String(buffer);
						if (str.indexOf("3D info is not available") != -1) return NO3DSDF;
					}
//					if (max > 0) progress.incrementProgressBy(read);
					out.write(buffer, 0, read);
				}
				inp.close();
				out.close();
				httpConn.disconnect();

			} catch (Exception e) {
				Log.d("Downloader", "failed " + e.getMessage());
				Log.d("Downloader", "failed " + e.toString());
				Log.d("Downloader", "failed " + e.getStackTrace()[0].getClassName() + e.getStackTrace()[0].getLineNumber());
				return ERROR;
			}
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			progress.dismiss();
			if (result == SUCCESS) {
				parent.readURI("file://" + dest);
				return;
			} else if (result == NO3DSDF) {
				parent.alert(parent.getString(R.string.no3DSDF));
			} else if (result == ERROR) {
				parent.alert(parent.getString(R.string.downloadError));
			} else if (result == CANCELED) {
			}
			File output = new File(dest);
			output.delete();
		}
	}
}
