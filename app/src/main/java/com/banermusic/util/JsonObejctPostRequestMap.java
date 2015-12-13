package com.banermusic.util;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public  class JsonObejctPostRequestMap extends Request<String> {
	private Map<String,String> mMap;
	private Listener<String> mListener;


	public JsonObejctPostRequestMap(String url, Map<String, String> map, Listener<String> listener, ErrorListener errorListener) {
		super(Request.Method.POST, url, errorListener);
		mListener=listener;
		mMap=map;

        try {
            for(String key : mMap.keySet()){
                Log.i(key, mMap.get(key).toString());
            }
        }catch (Exception e){

        }

		// TODO Auto-generated constructor stub
	}
	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		// TODO Auto-generated method stub
		return mMap;
	}

	   @Override
	    protected Response<String> parseNetworkResponse(NetworkResponse response) {
	        try {
	            String jsonString =
	                new String(response.data, HttpHeaderParser.parseCharset(response.headers));
	            return Response.success(new String(jsonString),
						HttpHeaderParser.parseCacheHeaders(response));
	        } catch (UnsupportedEncodingException e) {
	            return Response.error(new ParseError(e));
	        }
	    }

	@Override
	protected void deliverResponse(String response) {
		mListener.onResponse(response);
		
	}


}