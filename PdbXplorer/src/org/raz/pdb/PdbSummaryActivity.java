package org.raz.pdb;

import java.util.List;

import org.raz.pdb.view.PdbSummaryViewFragment;
import org.razib.pdb.model.PdbSummary;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class PdbSummaryActivity extends FragmentActivity {
	private static final PdbApplication pdbApplication = PdbApplication.getInstance();
	// Need a view pager and an adapter
    private MyAdapter mAdapter;
    private ViewPager mPager;	

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.pdbsummary_pager);
        List<PdbSummary> pdbSummaries = pdbApplication.getPdbSummaries();
		mAdapter = new MyAdapter(getSupportFragmentManager(), pdbSummaries);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);	 
        mPager.setCurrentItem(pdbApplication.getCurrentSummary());
	}


    /**
     * This adapter provide the note view fragments to the View Pager
     *
     */
	public static class MyAdapter extends FragmentPagerAdapter {

	    private List<PdbSummary> summaries;

        public MyAdapter(FragmentManager fm, List<PdbSummary> summaries) {
            super(fm);
        	this.summaries = summaries;
        }

        @Override
        public int getCount() {
            return summaries.size();
        }
        
    	@Override
		public Object instantiateItem(View container, int position) {
    		PdbSummaryViewFragment summaryFragment = (PdbSummaryViewFragment) 
    				super.instantiateItem(container, position);
    		// Since fragments can be reused and tied to a position,
    		// we need to update the note id so the fragment will pull
    		// the right data to display
    		summaryFragment.setPdbSummary(summaries.get(position));
    		return summaryFragment;
		}

        @Override
        public Fragment getItem(int position) {
            return PdbSummaryViewFragment.newInstance(summaries.get(position));
        }
        
    	@Override
		public int getItemPosition(Object object) {
    		// This essentially clears the adapter
    		return POSITION_NONE;
		}
    }
}
