package com.challenge.uber.imageloader.fragments;

import org.json.JSONException;
import org.json.JSONObject;

import com.challenge.uber.imageloader.R;
import com.squareup.picasso.Picasso;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Simple fragment in order to display the selected image in big.
 * @author Julien Salvi
 *
 */
public class ImageViewerFragment extends Fragment {

	//UI references.
	private ImageView gImage;
	//JSON reference.
	private JSONObject imageObject;
	
	public ImageViewerFragment() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Check arguments
		if (getArguments() != null) {
			try {
				imageObject = new JSONObject(getArguments().getString("image_data", null));
			} catch (JSONException e) {
				imageObject = null;
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_image_view, container, false);
		
		//Setup UI
		gImage = (ImageView) view.findViewById(R.id.image_googlesearch);
		
		if (imageObject != null) {
			//Setup the image title
			getActivity().getActionBar().setTitle(
					Html.fromHtml(imageObject.optString("title", "no title")));
			// Setup image and title
			Picasso
				.with(getActivity())
				.load(Html.fromHtml(
						imageObject.optString("unescapedUrl", "no_image")).toString())
				.error(R.drawable.default_img)
				.into(gImage);
		}
		return view;
	}

}
