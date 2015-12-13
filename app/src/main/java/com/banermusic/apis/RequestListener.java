package com.banermusic.apis;


import com.android.volley.VolleyError;

public interface RequestListener
{
	void onResponse(String response);
	void onErrorResponse(VolleyError error);//网络问题
}
