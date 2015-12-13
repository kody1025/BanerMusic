package com.banermusic.apis;

import android.content.Context;

import com.banermusic.util.HttpUtil;

import java.util.Map;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

public class BaseApi
{
	
	public static void sendMapRequest(Context context, String url, Map<String, String> map, final RequestListener listener)
	{
		try{
		HttpUtil.sendMapRequest(context, url, map, new Listener<String>() {
			@Override
			public void onResponse(String response) {
				if (listener != null) {
					listener.onResponse(response);
				}

			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (listener != null) {
					listener.onErrorResponse(error);
				}
			}
		});
		}
		catch(Exception e){
			System.out.println(e.toString());
		}
	}
}
