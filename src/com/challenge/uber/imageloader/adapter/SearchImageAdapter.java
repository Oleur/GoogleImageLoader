package com.challenge.uber.imageloader.adapter;

import org.json.JSONObject;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.challenge.uber.imageloader.R;
import com.challenge.uber.imageloader.utils.ShadowedTransformation;
import com.squareup.picasso.Picasso;

/**
 * Grid adpater for load and displaying the images from the google search.
 * @author Julien Salvi
 *
 */
public class SearchImageAdapter extends BaseAdapter {
	
	//Context references.
	private Context context;
	private LayoutInflater inflater;
	
	//JSON references.
	private JSONObject imagesJson;

	//View holder reference.
	private SearchImageViewHolder searchViewHolder;

	/**
	 * Constructor with the current context and the JSON data.
	 * @param c Current context
	 * @param json JSON from the Google Search.
	 */
	public SearchImageAdapter(Context c, JSONObject json) {
		context = c;
		imagesJson = json;
		inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	/**
	 * View Holder in order to display the data.
	 * @author Julien Salvi
	 */
	static class SearchImageViewHolder {
		protected ImageView imgViewGoogleSearch;
    	protected TextView textImage;
	}

	@Override
	public int getCount() {
		try  {
			return imagesJson.getJSONObject("responseData").getJSONArray("results").length();
		} catch (Exception e){
			return 0;
		}
	}

	@Override
	public JSONObject getItem(int position) {
		try {
			return imagesJson.getJSONObject("responseData").getJSONArray("results").getJSONObject(position);
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
		searchViewHolder = null;
		
		if (convertView == null) {
			searchViewHolder = new SearchImageViewHolder();
			view = inflater.inflate(R.layout.item_image_grid, null);
			searchViewHolder.imgViewGoogleSearch = (ImageView) view.findViewById(R.id.image_googlesearch);
			searchViewHolder.textImage = (TextView) view.findViewById(R.id.text_image_title);
			view.setTag(searchViewHolder);
		} else {
			view = convertView;
			searchViewHolder = (SearchImageViewHolder)view.getTag();
		}
		
		try {
			// Setup image and title
			searchViewHolder.textImage.setText(
					Html.fromHtml(imagesJson.getJSONObject("responseData").getJSONArray("results").getJSONObject(position).optString("title", "no title")));
			Picasso
				.with(context)
				.load(Html.fromHtml(
						imagesJson.getJSONObject("responseData").getJSONArray("results").getJSONObject(position).optString("unescapedUrl")).toString())
				.placeholder(R.drawable.default_img)
				.error(R.drawable.default_img)
				.transform(new ShadowedTransformation())
				.into(searchViewHolder.imgViewGoogleSearch);
		} catch (Exception e) { }
	    
		return view;
	}
	
	public JSONObject getImagesJson() {
		return imagesJson;
	}

	public void setImagesJson(JSONObject imagesJson) {
		this.imagesJson = imagesJson;
	}

}
