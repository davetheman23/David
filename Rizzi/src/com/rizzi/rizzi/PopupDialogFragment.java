package com.rizzi.rizzi;


import java.util.List;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.parse.ParseObject;
import com.rizzi.rizzi.parseclasses.TripPosts;
import com.rizzi.rizzi.utils.HeightWrappingViewPager;

public class PopupDialogFragment extends DialogFragment implements 
							RidePostFragment.OnMatchedMeClickedListener{
	/**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 2;
    
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private HeightWrappingViewPager mPager;
    
    View viewPagerView;
    
    Fragment postFragment1 = null;
    Fragment postFragment2 = null;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private FragmentStatePagerAdapter mPagerAdapter;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		//LayoutInflater inflater = getActivity().getLayoutInflater();
		//viewPagerView = inflater.inflate(R.layout.test_view_pager, null);
		
		//AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//builder.setCustomTitle(null)
		//		.setView(viewPagerView);
		
		//final Dialog dialog = builder.create();
		// create a new empty dialog on the activity
		final Dialog dialog = new Dialog(getActivity());
		// initialize dialog box basic configurations
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
											WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		//dialog.setContentView(R.layout.test_view_pager);
		
		return dialog;

	}
	public void setFragments(Fragment fragment1,Fragment fragment2){
		postFragment1 = fragment1;
		postFragment2 = fragment2;
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflate view pager
		viewPagerView = inflater.inflate(R.layout.frag_ridepost_container_viewpager, container);
		
		// set up the page adapter for the view pager instance
		mPager =(HeightWrappingViewPager) viewPagerView.findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setOffscreenPageLimit(2);
		mPager.setCurrentItem(0);
		
		return viewPagerView;
	}
	
	/**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	switch (position) {
			case 1:
				return postFragment2;
			default:
				return postFragment1;
			}
        	
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

	@Override
	public void onMatchMeClicked(ParseObject data) {
		if (mPager != null){
			mPager.setCurrentItem(1);
		}
		if (postFragment2 != null){
			((RideListFragment)postFragment2).setQueryConstraints((TripPosts)data);
			((RideListFragment)postFragment2).updatePostList();
			
		}
	}
    
   

}
