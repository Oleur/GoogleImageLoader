package com.challenge.uber.imageloader;

import java.lang.reflect.Field;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.TextView;
import android.widget.Toast;

import com.challenge.uber.imageloader.adapter.SearchImageAdapter;
import com.challenge.uber.imageloader.adapter.SearchHistoryAdapter;
import com.challenge.uber.imageloader.parser.JSONParser;
import com.challenge.uber.imageloader.utils.Utils;
import com.squareup.okhttp.OkHttpClient;

/**
 * Main activity where a use is able to search images thanks to a search bar
 * using the Google Search Request API. Each search made by the user will be
 * saved in the user preferences.
 * The History will be displayed as a list when the search bar is triggered.
 * @author Julien Salvi
 *
 */
public class SearchImageActivity extends Activity implements OnScrollListener, OnItemClickListener {
	
	//UI references.
	private TextView emptyText;
	private GridView imgResGrid;
	private SearchImageAdapter imgAdapter;
	
	//JSON references
	private JSONParser parser;
	private JSONObject imgObject;
	
	//Search references.
	private SearchView searchView;
	private MenuItem searchMenuItem;
	private String query;
	private RelativeLayout searchContainer;
	private ListView suggestionList;
	private SearchHistoryAdapter suggestAdapter;
	private JSONArray histArray;
	private SharedPreferences settings;
	private PullToRefreshLayout mPullToRefreshLayout;
	
	//Refresh references.
	private int start = 0;
	private boolean loadMore = false;
	private LoadImagesTask imgTask;
	protected boolean closeFrag = false;
	protected boolean searchDef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_image);
		
		//Set handler factory for avoiding SIGNAL 11 failure.
		try {
			URL.setURLStreamHandlerFactory(new OkHttpClient());
		} catch (Error e) {
			Log.i("INFO", "Factory already set.");
		} catch (Exception e) {
			Log.i("INFO", "Factory already set.");
		}
		
		//In/Out animation.
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		
		//Setup action bar
		getActionBar().setTitle("Show History");
		getActionBar().setHomeButtonEnabled(true);
		
		//Setup JSON parser
		parser = new JSONParser();
		
		//Setup settings and load current history
		settings = getSharedPreferences(Utils.PREFS_NAME, Activity.MODE_PRIVATE);
		try {
			histArray = new JSONArray(settings.getString("history", "[]"));
		} catch (JSONException e) {
			histArray = null;
		}
		
		//Setup search container.
		searchContainer = (RelativeLayout) findViewById(R.id.suggestion_container);
		suggestionList = (ListView) findViewById(R.id.suggestion_list);
		if (histArray != null) {
			suggestAdapter = new SearchHistoryAdapter(this, histArray);
			suggestionList.setAdapter(suggestAdapter);
			suggestAdapter.notifyDataSetChanged();
		}
		
		suggestionList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
				query = suggestAdapter.getItem(position);
				searchView.setQuery(query, false);
				//Hide keyboard
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				//Hide the search list.
				Animation slideDownOut = AnimationUtils.loadAnimation(SearchImageActivity.this, R.anim.slide_down_out);
				searchContainer.startAnimation(slideDownOut);
				searchContainer.setVisibility(View.GONE);
				getActionBar().setTitle("Show History");
				//Launch the search
				start = 0;
				//Clear cache
				Utils.trimCache(SearchImageActivity.this);
				//Refresh the list if online
				if (Utils.isOnline(SearchImageActivity.this))
					loadImages(query.replace(" ", "%2B"));
			}
		});
		suggestAdapter.notifyDataSetChanged();
		
		//Setup grid and text views.
		emptyText = (TextView) findViewById(android.R.id.empty);
		emptyText.setVisibility(View.VISIBLE);
		emptyText.setText("Search images in Google!");
		imgResGrid = (GridView) findViewById(android.R.id.list);
		imgResGrid.setOnItemClickListener(this);
		imgResGrid.setOnScrollListener(this);
		imgResGrid.setFastScrollEnabled(false);
		imgResGrid.setSmoothScrollbarEnabled(true);
		
		//Setup the action bar pull-to-refresh.
		mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(this)
                .theseChildrenArePullable(android.R.id.list, android.R.id.empty)
                .listener(new OnRefreshListener() {
					@Override
					public void onRefreshStarted(View view) {
						start = 0;
						//Clear cache
						Utils.trimCache(SearchImageActivity.this);
						//Refresh the list.
						loadImages(query.replace(" ", "%2B"));
					}
				})
                .options(Options.create().scrollDistance(0.5f).build())
                .setup(mPullToRefreshLayout);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_image, menu);
		
		SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
	    searchMenuItem = menu.findItem(R.id.search_image);
	    searchView = (SearchView) searchMenuItem.getActionView();
	    // Assumes current activity is the searchable activity
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    
	    //Set the searchview text color and icon.
	    int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
	    final int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
	    
	    TextView textView = (TextView) searchView.findViewById(id);
	    textView.setTextColor(Color.WHITE);
        
        final int textViewID = searchView.getContext().getResources().getIdentifier("android:id/search_src_text",null, null);
        final AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(textViewID);
        searchTextView.clearFocus();
        searchTextView.setTextColor(Color.WHITE);
        searchTextView.setHint(R.string.hint_search);
        
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, 0);
        } catch (Exception e) {}
        
        searchView.setIconified(true);
	    searchView.setQueryRefinementEnabled(true);
	    searchView.setOnSearchClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Animation slideFromRight = AnimationUtils.loadAnimation(SearchImageActivity.this, R.anim.slide_right_in);
				searchView.findViewById(searchPlateId).startAnimation(slideFromRight);
				closeFrag = true;
				Animation slideDownIn = AnimationUtils.loadAnimation(SearchImageActivity.this, R.anim.slide_down_in);
				searchContainer.startAnimation(slideDownIn);
				searchContainer.setVisibility(View.VISIBLE);
				getActionBar().setTitle("Hide History");
                searchView.setQuery("", false);
                searchView.onActionViewExpanded();
			}
		});
	    searchView.setOnCloseListener(new OnCloseListener() {
			@Override
			public boolean onClose() {
				Animation slideRightOut = AnimationUtils.loadAnimation(SearchImageActivity.this, android.R.anim.slide_out_right);
				searchView.findViewById(searchPlateId).startAnimation(slideRightOut);
				closeFrag = false;
				Animation slideDownOut = AnimationUtils.loadAnimation(SearchImageActivity.this, R.anim.slide_down_out);
				searchContainer.startAnimation(slideDownOut);
				searchContainer.setVisibility(View.GONE);
				getActionBar().setTitle("Show History");
				searchView.setQuery("", false);
				searchView.onActionViewCollapsed();
				return false;
			}
		});
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
            public boolean onQueryTextSubmit(String queryText) {
                query = queryText;
                Animation slideRightOut = AnimationUtils.loadAnimation(SearchImageActivity.this, android.R.anim.slide_out_right);
				searchView.findViewById(searchPlateId).startAnimation(slideRightOut);
				closeFrag = false;
				Animation slideDownOut = AnimationUtils.loadAnimation(SearchImageActivity.this, R.anim.slide_down_out);
				searchContainer.startAnimation(slideDownOut);
				searchContainer.setVisibility(View.GONE);
				getActionBar().setTitle("Show History");
				//Set the query in the search bar and update the history
				searchView.setQuery(queryText, false);
				updateHistory(query);
				if (suggestAdapter != null) {
					suggestAdapter.setHistoryArray(histArray);
					suggestAdapter.notifyDataSetChanged();
				}
				//Init start to 0 for a new query and load the images.
				start = 0;
				mPullToRefreshLayout.setRefreshing(true);
				emptyText.setVisibility(View.GONE);
				loadImages(query.replace(" ", "%2B"));
				searchView.onActionViewCollapsed();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (searchContainer.getVisibility() == View.VISIBLE) {
				//Hide history by clicking on the home menu
				searchView.setQuery("", false);
				searchView.onActionViewCollapsed();
				Animation slideDownOut = AnimationUtils.loadAnimation(SearchImageActivity.this, R.anim.slide_down_out);
				searchContainer.startAnimation(slideDownOut);
				searchContainer.setVisibility(View.GONE);
				getActionBar().setTitle("Show History");
			} else {
				Animation slideDownIn = AnimationUtils.loadAnimation(SearchImageActivity.this, R.anim.slide_down_in);
				searchContainer.startAnimation(slideDownIn);
				searchContainer.setVisibility(View.VISIBLE);
				getActionBar().setTitle("Hide History");
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
	    if (closeFrag) {
	    	searchView.setQuery("", false);
	    	searchView.onActionViewCollapsed();
	    	Animation slideDownOut = AnimationUtils.loadAnimation(SearchImageActivity.this, R.anim.slide_down_out);
			searchContainer.startAnimation(slideDownOut);
			searchContainer.setVisibility(View.GONE);
			getActionBar().setTitle("Show History");
            closeFrag = false;
	    } else {
	    	Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Utils.trimCache(this);
            startActivity(homeIntent);
	    }
	}
	
	/**
	 * Update the current history.
	 * @param savedQuery Query to save
	 */
	protected void updateHistory(String savedQuery) {
		if (histArray != null) {
			histArray.put(savedQuery);
			settings.edit().putString("history", histArray.toString()).commit();
		}
	}

	/**
	 * Load the images with respect to the query the user entered or load more images.
	 * @param query Search query.
	 */
	private void loadImages(String query) {
		if (imgTask != null) {
			return;
		} else {
			try {
				imgTask = new LoadImagesTask();
				imgTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
			} catch (Exception e) {
				Toast.makeText(this, "Enable to load images...", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 * AsyncTask in order to launch the search with the requested query. 
	 * Once the JSON data retrieved, populate the list in the onPostExcecute method.
	 * @author Julien Salvi
	 */
	public class LoadImagesTask extends AsyncTask<String, Void, Boolean> {
		
		private JSONObject loadJson = null;
				
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				if (start == 0) {
					imgObject = parser.fastGetRequest(Utils.SEARCH_IMG_URL+params[0]+"&start="+start);
				} else {
					loadJson = parser.fastGetRequest(Utils.SEARCH_IMG_URL+params[0]+"&start="+start);
					int len = loadJson.getJSONObject("responseData").getJSONArray("results").length();
					try {
						for (int i=0; i < len; i++) {
							imgObject.getJSONObject("responseData").getJSONArray("results").put(
									loadJson.getJSONObject("responseData").getJSONArray("results").getJSONObject(i));
						}
					} catch (Exception e) {
						return false;
					}
				}
		    	return true;
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean success) {
			imgTask = null;
			if (success) {
				try {
		    		//Enable more loading images.
			    	loadMore = true;
			    	//Check if the first time or load more images.
		    		if (loadMore && imgAdapter != null) {
		    			imgAdapter.setImagesJson(imgObject);
		    			imgAdapter.notifyDataSetChanged();
		    		} else {
		    			imgAdapter = new SearchImageAdapter(SearchImageActivity.this, imgObject);
		    			imgResGrid.setAdapter(imgAdapter);
		    			imgAdapter.notifyDataSetChanged();
		    		}
					
			    	if (imgResGrid != null) {
				    	if (imgAdapter.getCount() == 0) {
				    		imgResGrid.setOnScrollListener(null);
				    		emptyText.setVisibility(View.VISIBLE);
				    		emptyText.setText("No images found!");
				    	} else {
				    		emptyText.setVisibility(View.GONE);
				    		imgResGrid.setOnScrollListener(SearchImageActivity.this);
				    	}
			    	}
				} catch (Exception e) {
					imgResGrid.setOnScrollListener(null);
				}
		    	
		    	//Refresh complete.
		    	mPullToRefreshLayout.setRefreshComplete();
		    	//Enable more loading after notifying the change in the list.
		    	loadMore = true;
			} else {
		    	mPullToRefreshLayout.setRefreshComplete();
				emptyText.setVisibility(View.VISIBLE);
	    		emptyText.setText("Images unavailable!");
			}
		}

		@Override
		protected void onCancelled() {
			imgTask = null;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		int lastInScreen = firstVisibleItem + visibleItemCount;
		//If load more true and you reach the end of the list, then load more images.
		if ((lastInScreen == totalItemCount) && loadMore) {
			if (query != null && Utils.isOnline(this)) {
				loadMore = false;
	    		start += 8;
	    		mPullToRefreshLayout.setRefreshing(true);
    			loadImages(query.replace(" ", "%2B"));
			}
        }
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) { }

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		//Hide search and history features
		if (searchContainer.getVisibility() == View.VISIBLE) {
			//Clear search bar
			searchView.setQuery("", false);
			searchView.onActionViewCollapsed();
			//Close container with a sliding animation
			Animation slideDownOut = AnimationUtils.loadAnimation(SearchImageActivity.this, R.anim.slide_down_out);
			searchContainer.startAnimation(slideDownOut);
			searchContainer.setVisibility(View.GONE);
			getActionBar().setTitle("Show History");
		}
		//Open the image viewer activity
		Intent imageViewerIntent = new Intent(this, ImageViewerActivity.class);
		try {
			imageViewerIntent.putExtra("images_data", imgObject.toString());
			imageViewerIntent.putExtra("position", position);
			imageViewerIntent.putExtra("query", query.replace(" ", "%2B"));
			startActivity(imageViewerIntent);
		} catch (Exception e) {
			Toast.makeText(this, "Fail to open the image page...", Toast.LENGTH_SHORT).show();
		}
	}

}
