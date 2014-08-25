package org.raz.pdb;

import java.util.ArrayList;
import java.util.List;

import org.raz.pdb.service.PdbService;
import org.razib.pdb.model.PdbSummary;
import org.razib.pdb.queries.Query;
import org.razib.pdb.queries.QueryParam;
import org.razib.pdb.queries.QueryResult;
import org.razib.pdb.queries.QueryType;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class PdbXplorerActivity extends ListActivity implements OnEditorActionListener {
    
	private static final PdbApplication pdbApplication = PdbApplication.getInstance();
	private static final String TAG = "PdbXplorer";
	private PdbService pdbService;
	private AutoCompleteTextView autoTextView;
	final List<PdbSummary> pdbSummaries = new ArrayList<PdbSummary>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		 
	    MyStateSaver data = (MyStateSaver) getLastNonConfigurationInstance();
	    if (data != null) {
	    	Log.d(TAG, "onCreate: Resuming activity for first run");
	        // Rebuild your UI with your saved state here
	        setContentView(R.layout.main);
	    } else {
	    	Log.d(TAG, "onCreate: Initialising activity for first run");
	        showSplashScreen();
	        // Do your heavy loading here on a background thread
	        setContentView(R.layout.main);
	    }
	    
	    autoTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        autoTextView.setOnEditorActionListener(this);
        pdbService = pdbApplication.getPdbService();
        
        Log.d(TAG, "onCreate: Finished initialising activity");
    }

	@Override
	public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
		// ignore events for unrelated editor actions in some phones
		if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
            return false;
        }

        if ( actionId != EditorInfo.IME_ACTION_NEXT && actionId != EditorInfo.IME_NULL ) {
            return false;
        }
        
		Log.d(TAG, "onEditorAction started");
		autoTextView.setEnabled(false);
		try {
			Query textQuery = new Query(QueryType.TextSearch);
			textQuery.setQueryParam(QueryParam.Keywords, textView.getText().toString());
			new PdbQueryTask(this).execute(textQuery);
		} catch (Throwable ex) {
			Toast.makeText(this,"Unable to communicate with Pdb.\nPlease check if you are " +
					"connected to the internet.", Toast.LENGTH_LONG).show();
			Log.e(TAG, "Error executing PDB query : " + ex.getMessage(), ex);
		}
		Log.d(TAG, "onEditorAction completed");
		return true;
	}
	
	/**
	 * Asynchronously execute the Pdb Query and Populate Results
	 * @author raz
	 *
	 */
	private class PdbQueryTask extends AsyncTask<Query, Integer, QueryResult<List<String>>> {

		private Context context;

		public PdbQueryTask(Context context) {
			this.context = context;
		}
		
		@Override
		protected QueryResult<List<String>> doInBackground(Query... textQuery) {
			Log.i(TAG, "Sending query to PDB ");
			pdbSummaries.clear();
			QueryResult<List<String>> queryResult = null;
			try {	
				queryResult = pdbService.findPdbIds(textQuery[0]);
			} catch (Throwable ex) {
				Toast.makeText(context,"Unable to communicate with Pdb.\nPlease check if you are " +
						"connected to the internet.", Toast.LENGTH_LONG).show();
				Log.e(TAG, "Error executing PDB query : " + ex.getMessage(), ex);
			}
			Log.i(TAG, "Found result : " + queryResult.getResult());			
			return queryResult;
		}
		
		@Override
		protected void onPostExecute(QueryResult<List<String>> queryResult) {
			// Not using simpleadapter to reduce memory usage
			final List<String> pdbIds = queryResult.getResult();
			if (pdbIds.size() > 200) {
				// TODO break it down and use threads 
				Log.w("PdbXplorer", "Ignoring the huge query size as current implementation can't handle it.");
				List<String> pdbIdList = new ArrayList<String>();
				for (int i = 200; i < pdbIds.size(); i++) {
					pdbIdList.add(pdbIds.get(i));
				}
				pdbIds.removeAll(pdbIdList);
			}			
//			HashMap<String, String> values = new HashMap<String, String>();
//			for (String pdbId : results) {
//				values.put("title", pdbId)
//			}
//			SimpleAdapter adapter = new SimpleAdapter(context, 
//														(List<? extends Map<String, ?>>) new ArrayList<Map<String,?>>(), 
//														R.layout.pdbresult, 
//														new String[]{}, 
//														new int[]{});
			final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, 
												R.layout.pdbresult,
												R.id.title,
												pdbIds) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					View view = super.getView(position, convertView, parent);
					TextView details = (TextView) view.findViewById(R.id.details);
					if (pdbSummaries.size() == 0) {
						details.setText("Loading Protein Data...");
					} else {
						details.setText(pdbSummaries.get(position).getTitle());
					}
					return view;
				}
			};
			setListAdapter(arrayAdapter);
			autoTextView.setEnabled(true);
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
				try {
					pdbSummaries.clear();
					pdbSummaries.addAll(pdbService.getPdbSummaries(pdbIds));
				} catch (Throwable ex) {
//					Toast.makeText(context,"Unable to communicate with Pdb.\nPlease check if you are " +
//							"connected to the internet.", Toast.LENGTH_LONG).show();
					Log.e(TAG, "Error executing PDB query : " + ex.getMessage(), ex);
				}
					return null;
				}
				
				protected void onPostExecute(Void result) {
					arrayAdapter.notifyDataSetChanged();
				}
				
			}.execute((Void)null);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (pdbSummaries.size() > 0) {
//			String item = (String) getListAdapter().getItem(position);
			//Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
			pdbApplication.setCurrentSummary(position);
			pdbApplication.setPdbSummaries(pdbSummaries);
			startActivity(new Intent(this, PdbSummaryActivity.class));
		}
	}
	
	// ///////////////////  Splash Screen ////////////////////////////
	Dialog splashDialog; 
	 
	@Override
	public Object onRetainNonConfigurationInstance() {
	    MyStateSaver data = new MyStateSaver();
	    // Save your important data here
	    // e.g. data.setShowSplashScreen(false);
	    return data;
	}
	 
	/**
	 * Removes the Dialog that displays the splash screen
	 */
	protected void removeSplashScreen() {
		if (splashDialog != null) {
			splashDialog.dismiss();
		}
	}
	 
	/**
	 * Shows the splash screen over the full Activity
	 */
	protected void showSplashScreen() {
		splashDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		splashDialog.setContentView(R.layout.splashscreen);
		splashDialog.show();
	    // Set Runnable to remove splash screen just in case
	    final Handler handler = new Handler();
	    handler.postDelayed(new Runnable() {
	      @Override
	      public void run() {
	        removeSplashScreen();
	      }
	    }, 3000);
	}
	 
	/**
	 * Simple class for storing important data across config changes
	 */
	private class MyStateSaver {
		// data
	}	
	
}