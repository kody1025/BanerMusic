package com.banermusic.util;

/**
 *
 */

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.banermusic.constant.BaseConstants;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class HttpUtil
{

	/**
	 * 防止工具类被实例化
	 */
	private HttpUtil()
	{
		throw new AssertionError();
	}

	/**
	 * 
	 * @param url
	 *            发送请求的URL
	 * @return 服务器响应字符串
	 * @throws Exception
	 */
	public static String getRequest(final String url) throws Exception
	{
		FutureTask<String> task = new FutureTask<String>(new Callable<String>()
		{
			@Override
			public String call() throws Exception
			{
				// 创建HttpGet对象。
				HttpGet get = new HttpGet(url);
				// 发送GET请求
				HttpResponse httpResponse = BaseConstants.httpClient.execute(get);
				// 如果服务器成功地返回响应
				if (httpResponse.getStatusLine().getStatusCode() == 200)
				{
					// 获取服务器响应字符串
					String result = EntityUtils.toString(httpResponse.getEntity());
					return result;
				}
				return null;
			}
		});
		new Thread(task).start();
		return task.get();
	}

	/**
	 * @param url
	 *            发送请求的URL
	 * @param params
	 *            请求参数
	 * @return 服务器响应字符串
	 * @throws Exception
	 */
	public static String postRequest(final String url, final Map<String, String> rawParams) throws Exception
	{
		FutureTask<String> task = new FutureTask<String>(new Callable<String>()
		{
			@Override
			public String call() throws Exception
			{
				// 创建HttpPost对象。
				HttpPost post = new HttpPost(url);
				// 如果传递参数个数比较多的话可以对传递的参数进行封装

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				for (String key : rawParams.keySet())
				{
					// 封装请求参数
					params.add(new BasicNameValuePair(key, rawParams.get(key)));
				}
				// 设置请求参数
				post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
				// 发送POST请求
				BaseConstants.httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
				HttpResponse httpResponse = BaseConstants.httpClient.execute(post);
				// 如果服务器成功地返回响应
				if (httpResponse.getStatusLine().getStatusCode() == 200)
				{
					// 获取服务器响应字符串
					String result = EntityUtils.toString(httpResponse.getEntity());
					return result;
				}
				return null;
			}
		});
		new Thread(task).start();
		return task.get();
	}

	/**
	 * 以GET或者POST方式发送jsonObjectRequest
	 *
	 * @param context
	 *            android应用上下文
	 * @param requestUrl
	 *            请求的Url
	 * @param requestParameter
	 *            请求的参数，如果为null，则使用GET方法调用，否则使用POST方法调用
	 * @param listener
	 *            正确返回之后的lisenter
	 * @param errorListener
	 *            错误返回的lisenter
	 */
	public static void sendJsonObjectRequest(Context context, String requestUrl, JSONObject requestParameter, Listener<JSONObject> listener, ErrorListener errorListener)
	{
		JsonObjectRequest request = new JsonObjectRequest(requestUrl, requestParameter, listener, errorListener);
		NetContext netContext = NetContext.getInstance(context);
		request.setRetryPolicy(new DefaultRetryPolicy(5 * 1000, 1, 1.0f));
		netContext.getJsonRequestQueue().add(request);
		// AppController.getInstance().addToRequestQueue(request);
	}

	public static void sendMapRequest(Context context, String requestUrl, Map<String, String> requestParameter, Listener<String> listener, ErrorListener errorListener)
	{
		JsonObejctPostRequestMap request = new JsonObejctPostRequestMap(requestUrl, requestParameter, listener, errorListener);
		NetContext netContext = NetContext.getInstance(context);
		request.setRetryPolicy(new DefaultRetryPolicy(5 * 1000, 1, 1.0f));
		netContext.getJsonRequestQueue().add(request);
		// AppController.getInstance().addToRequestQueue(request);
	}

	/**
	 * 以GET方式发送jsonArrayRequest
	 *
	 * @param context
	 *            android应用上下文
	 * @param requestUrl
	 *            请求的Url
	 * @param listener
	 *            正确返回之后的lisenter
	 * @param errorListener
	 *            错误返回的lisenter
	 */
	public static void sendJsonArrayMapRequest(Context context, String requestUrl, Map<String, String> requestParameter, Listener<JSONArray> listener, ErrorListener errorListener)
	{
		JsonArrayPostRequestMap request = new JsonArrayPostRequestMap(requestUrl, requestParameter, listener, errorListener);
		NetContext netContext = NetContext.getInstance(context);
		request.setRetryPolicy(new DefaultRetryPolicy(5 * 1000, 1, 1.0f));
		netContext.getJsonRequestQueue().add(request);
		// AppController.getInstance().addToRequestQueue(request);
	}

	/**
	 * 以GET方式发送jsonArrayRequest
	 *
	 * @param context
	 *            android应用上下文
	 * @param requestUrl
	 *            请求的Url
	 * @param listener
	 *            正确返回之后的lisenter
	 * @param errorListener
	 *            错误返回的lisenter
	 */
	public static void sendJsonArrayRequest(Context context, String requestUrl, Listener<JSONArray> listener, ErrorListener errorListener)
	{
		JsonArrayRequest request = new JsonArrayRequest(requestUrl, listener, errorListener);
		NetContext netContext = NetContext.getInstance(context);
		request.setRetryPolicy(new DefaultRetryPolicy(5 * 1000, 1, 1.0f));
		netContext.getJsonRequestQueue().add(request);
		// AppController.getInstance().addToRequestQueue(request);
	}

	public static void addPostUploadFileRequest(Context context,final String url,
			final Map<String, File> files, final Map<String, String> params,
			final Listener<String> responseListener, final ErrorListener errorListener,
			final Object tag) {
		if (null == url || null == responseListener) {
			return;
		}

		MultiPartStringRequest multiPartRequest = new MultiPartStringRequest(
				Request.Method.POST, url, responseListener, errorListener) {

			@Override
			public Map<String, File> getFileUploads() {
				return files;
			}

			@Override
			public Map<String, String> getStringUploads() {
				return params;
			}

		};

		Log.i("头像上传", " volley put : uploadFile " + url);
		NetContext netContext = NetContext.getInstance(context);
		multiPartRequest.setRetryPolicy(new DefaultRetryPolicy(30 * 1000, 1, 1.0f));
		netContext.getJsonRequestQueue().add(multiPartRequest);

	}
}
