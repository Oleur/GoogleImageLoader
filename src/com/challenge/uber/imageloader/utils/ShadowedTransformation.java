package com.challenge.uber.imageloader.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;

import com.squareup.picasso.Transformation;

/**
 * Shadow transformation which apply a gradient effect to an image.
 * @author Julien Salvi
 */
public class ShadowedTransformation implements Transformation {
	
	@Override 
	public Bitmap transform(Bitmap source) {
	    Bitmap result = getShadowedBitmap(source);
	    if (result != source) {
	    	source.recycle();
	    }
	    return result;
	}
	
	@Override 
	public String key() { return "shadowed()"; }
	
	/**
	 * Apply a gradient effect to the picture in order to get a shadow from the top to the middle of the image.
	 * @param bitmap The bitmap to transform
	 * @return The transformed bitmap
	 */
	private Bitmap getShadowedBitmap(Bitmap bitmap) {
		if (bitmap == null) return null;
		//Create a new bitmap with the original dimension.
	    Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
	    
	    Canvas c = new Canvas(b);
	    c.drawBitmap(bitmap, 0, 0, null);
	    //Linear gradient for the fade effect.
	    LinearGradient grad = new LinearGradient(bitmap.getWidth()/2, 0, bitmap.getWidth()/2, bitmap.getHeight()/2, 
	    		Color.BLACK, 0x00000000, TileMode.CLAMP);
	    
	    //Setup the paint component.
	    Paint p = new Paint();
	    p.setAntiAlias(true);
	    p.setStyle(Paint.Style.FILL);
	    p.setShader(grad);
	    
	    c.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), p);
	    return b;
	}

}
