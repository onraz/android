/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.raz.pdb.view;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.raz.pdb.R;
import org.raz.pdb.graphics.MoleculeViewActivity;
import org.razib.pdb.model.PdbSummary;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PdbSummaryViewFragment extends Fragment {

	private static final String PDB_SUMMARY_KEY = "pdbSummary";
	protected TextView mTitleText;
	protected TextView mBodyText;
	protected ImageView mImage;

	// expose the currently displayed summary
	protected PdbSummary pdbSummary;

		
	public static PdbSummaryViewFragment newInstance(PdbSummary summary) {
		PdbSummaryViewFragment summaryFragment = new PdbSummaryViewFragment();
        Bundle args = new Bundle();
        args.putSerializable(PDB_SUMMARY_KEY, summary);
        summaryFragment.setArguments(args);
        return summaryFragment;
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pdbSummary = (PdbSummary) getArguments().getSerializable(PDB_SUMMARY_KEY);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.pdbsummary, container, false);

		mTitleText = (TextView) v.findViewById(R.id.header);
		mBodyText = (TextView) v.findViewById(R.id.body);
		mImage = (ImageView) v.findViewById(R.id.proteinImage);
		mImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Dialog splashDialog = new Dialog(v.getContext(), 
												android.R.style.Theme_Black_NoTitleBar_Fullscreen);
				splashDialog.setContentView(R.layout.molimager);
				splashDialog.show();
				TextView title = (TextView) splashDialog.findViewById(R.id.molViewTitle);
				title.setText(pdbSummary.getTitle());
				WebView molview = (WebView) splashDialog.findViewById(R.id.molview);
				molview.getSettings().setBuiltInZoomControls(true);
				molview.getSettings().setJavaScriptEnabled(false);
				molview.getSettings().setPluginsEnabled(false);
				molview.getSettings().setGeolocationEnabled(false);
				molview.getSettings().setLightTouchEnabled(false);
				molview.loadUrl(pdbSummary.getImageUrlHd());
			}
		});
		

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(PDB_SUMMARY_KEY)) {
			pdbSummary = (PdbSummary) savedInstanceState.getSerializable(PDB_SUMMARY_KEY);
		}
		populateFields();
		Button molLauncher = (Button) v.findViewById(R.id.molLauncher);
        molLauncher.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(v.getContext(), MoleculeViewActivity.class));
			}
		});
		return v;
	}

	
	public Bitmap getRemoteImage(String imageUrl) {
		BufferedInputStream bis = null;
	    try {
	    	final URL aURL = new URL(imageUrl);
	        final URLConnection conn = aURL.openConnection();
	        conn.connect();
			bis = new BufferedInputStream(conn.getInputStream());
	        final Bitmap bm = BitmapFactory.decodeStream(bis);
	        bis.close();
	        return bm;
	    } catch (IOException e) {
	        Log.d("PdbXplorer", "Oh noooz an error...");
	    } finally {
	    	if (bis != null) {
	    		try {
					bis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    }
	    return null;
	}	
	/**
	 * Display a particular note in this fragment.
	 * 
	 * @param noteUri The Uri of the note to display
	 */
	protected void loadNote(PdbSummary summary) {
		pdbSummary = summary;
		if (isAdded()) {
			populateFields();
		}
	}

	/**
	 * Clear all fields on this fragment.
	 */
	protected void clear() {
		mTitleText.setText(null);
		mBodyText.setText(null);
		pdbSummary = null;
	}
	
	
	/**
	 * Helper method which retrieves & displays the content of the current note.
	 */
	void populateFields() {
		if (pdbSummary != null) {
			mTitleText.setText(pdbSummary.getTitle());
			mBodyText.setText(pdbSummary.toString());
			
			new AsyncTask<Void, Void, Bitmap>() {
				@Override
				protected Bitmap doInBackground(Void... params) {
					// TODO Auto-generated method stub
					Bitmap proteinImage = getRemoteImage(pdbSummary.getImageUrl());
					return proteinImage;
				}
				@Override
				protected void onPostExecute(Bitmap result) {
					loadImage(result);
				}

			}.execute((Void)null);
		}
	}
	
	private void loadImage(Bitmap proteinImage) {
		mImage.setImageBitmap(proteinImage);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (pdbSummary != null) {
			outState.putSerializable(PDB_SUMMARY_KEY, pdbSummary);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		populateFields();
	}
	
	public void setPdbSummary(PdbSummary summary) {
		pdbSummary = summary;
		if (isAdded()) {
			populateFields();
		}
	}

}
