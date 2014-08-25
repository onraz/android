package org.raz.pdb.graphics;

import org.raz.pdb.R;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class SearcherTab extends TabActivity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchertab);    
		initTabs();
	}

	public void finishFromChild(Activity child) {
		// Pass-through result from child using child's intent (not child's result intent)
		if (child.getIntent().getDataString() != null) {
			Intent i = child.getIntent();
			setResult(RESULT_OK, i);
		} else {
			setResult(RESULT_CANCELED);
		}
		super.finishFromChild(child); 
	} 

	protected void initTabs(){
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;

		intent = new Intent().setClass(this, PDBSearcher.class);
		spec = tabHost.newTabSpec("Tab1").setIndicator("PDB").setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, PubChemSearcher.class);
		spec = tabHost.newTabSpec("Tab2").setIndicator("PubChem").setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
	}
}
