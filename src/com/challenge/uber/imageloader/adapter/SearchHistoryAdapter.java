package com.challenge.uber.imageloader.adapter;

import org.json.JSONArray;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.challenge.uber.imageloader.R;

/**
 * Adapter for the search history when you are searching for new images.
 * @author Julien Salvi
 *
 */
public class SearchHistoryAdapter extends BaseAdapter {
	
	//Context references.
	private LayoutInflater inflater;
	private SuggestionViewHolder sugViewHolder;
	
	//JSON references.
	private JSONArray historyArray;

	/**
	 * View Holder in order to display the history queries.
	 * @author Julien Salvi
	 *
	 */
	static class SuggestionViewHolder {
		protected TextView textSuggestion;
	}
	
	/**
	 * Constructor
	 * @param c Current context
	 * @param hist History as a JSON array.
	 */
	public SearchHistoryAdapter(Context c, JSONArray hist) {
		inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		historyArray = hist;
	}

	@Override
	public int getCount() {
		try {
			return historyArray.length();
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public String getItem(int position) {
		try {
			return historyArray.getString(position);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		sugViewHolder = null;
		if (convertView == null) {
			sugViewHolder = new SuggestionViewHolder();
			view = inflater.inflate(R.layout.item_suggestion, parent, false);
			sugViewHolder.textSuggestion = (TextView) view.findViewById(R.id.text_sliding_item);
			view.setTag(sugViewHolder);
		} else {
			view = convertView;
			sugViewHolder = (SuggestionViewHolder)view.getTag();
		}
		
		try {
			sugViewHolder.textSuggestion.setText(historyArray.getString(position));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return view;
	}

	/**
	 * Get the current history
	 * @return
	 */
	public JSONArray getHistoryArray() {
		return historyArray;
	}

	/**
	 * Update the new history
	 * @param suggestionArray New JSON array history
	 */
	public void setHistoryArray(JSONArray suggestionArray) {
		this.historyArray = suggestionArray;
	}
}
