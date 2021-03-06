package com.challenge.uber.imageloader.utils;

import java.io.File;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class contains global methods for the whole application.
 * @author Julien Salvi
 * 
 */
public class Utils {
	
	//Public constant & URLs
	public final static String PREFS_NAME = "MyPrefsFile";
	public final static String SEARCH_IMG_URL = "http://ajax.googleapis.com/ajax/services/search/images?v=1.0&imgsz=small&as_filetype=png&rsz=8&q=";

	/**
	 * Default constructor
	 */
    private Utils() {}
    
    /**
	 * Check if the device is connected to WiFi or Data plan.
	 * @return true if connected or connecting, false otherwise.
	 */
	public static boolean isOnline(Context c) {
	    ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	/**
	 * 
	 * @param context
	 */
	public static void trimCache(Context context) {
		try {
			File dir = context.getCacheDir();
			if (dir != null && dir.isDirectory()) {
				deleteDir(dir);
			}
		} catch (Exception e) { }
	}

	/**
	 * 
	 * @param dir
	 * @return
	 */
    public static boolean deleteDir(File dir) {
	   	if (dir != null && dir.isDirectory()) {
		   	String[] children = dir.list();
		   	for (int i = 0; i < children.length; i++) {
			   	boolean success = deleteDir(new File(dir, children[i]));
			   	if (!success) {
				   	return false;
			   	}
		   	}
	   	}
	   	// The directory is now empty so delete it
	   	return dir.delete();
   	}
}
