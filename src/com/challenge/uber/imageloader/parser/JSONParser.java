package com.challenge.uber.imageloader.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.okhttp.OkHttpClient;

/**
 * JSON Parser for converting the data from the Google Image Request API to a JSON Object.
 * @author Julien Salvi
 */
public class JSONParser {
		
	//JSON and OkHttp client reference.
	private String json;
	private OkHttpClient client;
	
	/**
	 * Default constructor with the current context.
	 * @param _context Current context.
	 */
	public JSONParser() {
		client = new OkHttpClient();
	}
	
	/**
	 * Fast get request thanks to the OkHttp library.
	 * @param url URL as a string.
	 * @return The JSON object
	 * @throws IOException
	 */
	public JSONObject fastGetRequest(String url) throws IOException {
		SSLContext sslContext = null;
	    try {
	    	sslContext = SSLContext.getInstance("TLS");
	        sslContext.init(null, null, null);
	    } catch (GeneralSecurityException e) {
	    	e.printStackTrace();
	    }
	    client.setSslSocketFactory(sslContext.getSocketFactory());
	    HttpURLConnection connection = client.open(new URL(url));
	    connection.setUseCaches(true);
	    InputStream in = null;
	    try {
	    	// Read the response.
	    	in = connection.getInputStream();
	    	byte[] response = readFully(in);
	    	json = new String(response, "UTF-8");
	    	return new JSONObject(json);
	    } catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (in != null) in.close();
	    }
		return null;
	}
	
	/**
	 * Read the input stream
	 * @param in Current input stream
	 * @return The output stream as a byte array.
	 * @throws IOException IO exception
	 */
	private byte[] readFully(InputStream in) throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    byte[] buffer = new byte[1024];
	    for (int count; (count = in.read(buffer)) != -1; ) {
	    	out.write(buffer, 0, count);
	    }
	    return out.toByteArray();
	}

}
