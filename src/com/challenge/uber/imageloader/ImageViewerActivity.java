package com.challenge.uber.imageloader;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.challenge.uber.imageloader.fragments.ImageViewerFragment;
import com.challenge.uber.imageloader.parser.JSONParser;
import com.challenge.uber.imageloader.utils.Utils;

/**
 * Activity for viewing and swiping between images you loaded perviously.
 * @author Julien Salvi
 *
 */
public class ImageViewerActivity extends FragmentActivity {
	
	//Pager references.
	public SectionsPagerAdapter sectionsPagerAdapter;
	public ViewPager pager;
	public RelativeLayout container;
	private List<Fragment> pagerFrags;

	//JSON references.
	private JSONParser parser;
	private JSONObject imagesData;
	
	//Variables references.
	private int start = 0;
	private int position = 0;
	private String query, imgExtra;
	
	//AsyncTask references.
	private LoadSwipeImagesTask swipeImgTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_viewer);
		
		//Override the transition animation.
		overridePendingTransition(R.anim.slide_right_in, android.R.anim.fade_out);
		
		//Setup UI.
		pager = (ViewPager) findViewById(R.id.pager_images);
		
		//Setup the fragments list and get extras if not null.
		pagerFrags = new ArrayList<Fragment>();
		if (getIntent().getExtras() != null) {
			position = getIntent().getExtras().getInt("position", 0);
			imgExtra = getIntent().getExtras().getString("images_data", null);
			query = getIntent().getExtras().getString("query", "");
		}
		
		if (imgExtra != null) {
			//Setup the JSON parser
			parser = new JSONParser();
			try {
				imagesData = new JSONObject(imgExtra);
				setupFragmentsList(imagesData.getJSONObject("responseData").getJSONArray("results"));
				//Setup the view pager
				sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
				pager.setAdapter(sectionsPagerAdapter);
				pager.setOnPageChangeListener(new OnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						if (position == (pagerFrags.size()-1)) {
							start++;
							loadList();
						}
					}
					
					@Override
					public void onPageScrollStateChanged(int state) { }

					@Override
					public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
					
				});
				//Move to the selected image.
				pager.setCurrentItem(position, false);
			} catch (Exception e) {
				//Launch a single fragment.
				Bundle arguments = new Bundle();
				try {
					arguments.putString("image_data", 
							imagesData.getJSONObject("responseData").getJSONArray("results").getJSONObject(position).toString());
					ImageViewerFragment fragment = new ImageViewerFragment();
					getSupportFragmentManager().beginTransaction()
							.add(R.id.image_container, fragment, "IMAGE_DETAILS").commit();
				} catch (JSONException ex) {
					Toast.makeText(this, "Fail to load the image", Toast.LENGTH_SHORT).show();
				}
				
			}
		} else {
			if (savedInstanceState == null) {
				// Create the detail fragment and add it to the activity
				// using a fragment transaction.
				Bundle arguments = new Bundle();
				try {
					arguments.putString("image_data", 
							imagesData.getJSONObject("responseData").getJSONArray("results").getJSONObject(position).toString());
					ImageViewerFragment fragment = new ImageViewerFragment();
					getSupportFragmentManager().beginTransaction()
							.add(R.id.image_container, fragment, "IMAGE_DETAILS").commit();
				} catch (Exception e) {
					Toast.makeText(this, "Fail to load the image", Toast.LENGTH_SHORT).show();
				}
				
			}
		}
	}
	
	/**
	 * Load more images to swipe
	 */
	protected void loadList() {
		if (swipeImgTask != null) {
			return;
		} else {
			if (parser == null) 
				parser = new JSONParser();
			//Init the new async task.
			swipeImgTask = new LoadSwipeImagesTask(true);
			swipeImgTask.execute(query);
		}
	}

	/**
	 * Setup the list of fragment in order to build the view pager.
	 * @param images
	 * @throws JSONException 
	 */
	private void setupFragmentsList(JSONArray images) throws JSONException {
		pagerFrags.clear();
		int len = images.length();
		for (int i = 0; i < len; i++) {
			//Init arguments and build the fragment
			Bundle arguments = new Bundle();
			arguments.putString("image_data", images.getJSONObject(i).toString());
			ImageViewerFragment fragment = new ImageViewerFragment();
			fragment.setArguments(arguments);
			//Add fragment to list if not null
			if (fragment != null)
				pagerFrags.add(fragment);
		}
	}

	/**
	 * Pager adapter from swiping between images.
	 * @author Julien Salvi
	 *
	 */
	private class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (pagerFrags.get(position) != null) {
				return pagerFrags.get(position);
			} else {
				return null;
			}
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
		}

		@Override
		public int getCount() {	
			return pagerFrags.size();
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return "imageFrag";
		}
	}
	
	/**
	 * AsyncTask in order to laod more images in the view pager.
	 * @author Julien Salvi
	 *
	 */
	private class LoadSwipeImagesTask extends AsyncTask<String, Void, Boolean> {
		
		//Local variables.
		private JSONObject imgLoadJson = null;
		private boolean load;
		
		public LoadSwipeImagesTask(boolean loadmore) {
			load = loadmore;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				imgLoadJson = parser.fastGetRequest(Utils.SEARCH_IMG_URL+params[0]+"&start="+start+"&imgsz=medium");
				int len = imgLoadJson.getJSONObject("responseData").getJSONArray("results").length();
				for (int i=0; i < len; i++) {
					imagesData.getJSONObject("responseData").getJSONArray("results").put(
							imagesData.getJSONObject("responseData").getJSONArray("results").getJSONObject(i));
				}
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			swipeImgTask = null;
	    	try {
	    		if (load && sectionsPagerAdapter != null) {
	    			//Load more images if pager not null.
	    			setupFragmentsList(imagesData.getJSONObject("responseData").getJSONArray("results"));
	    			sectionsPagerAdapter.notifyDataSetChanged();
	    		} else {
	    			//If not load, instantiate a new pager adapter with the new data.
	    			setupFragmentsList(imagesData.getJSONObject("responseData").getJSONArray("results"));
	    			sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
					pager.setAdapter(sectionsPagerAdapter);
					sectionsPagerAdapter.notifyDataSetChanged();
	    		}
			} catch (Exception e) {
				Toast.makeText(ImageViewerActivity.this, "Fail to load more images.", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onCancelled() {
			swipeImgTask = null;
		}
	}

}
