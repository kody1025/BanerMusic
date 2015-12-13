package com.banermusic.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class LoadImage
{

	public static ImageCache loadImageByVolley(Context context)
	{
		final int memory = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		// 用应用1/8的内存来缓存图片资源
		final int cacheSize = 1024 * 1024 * memory / 8;
		System.out.println("memory===" + memory);
		//NetContext netContext = NetContext.getInstance(context);
		
		//RequestQueue requestQueue = Volley.newRequestQueue(context);
		final LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(cacheSize)
		{

			@SuppressLint("NewApi")
			@Override
			protected int sizeOf(String key, Bitmap value)
			{
				return value.getByteCount();
			}
		};

		ImageCache imageCache = new ImageCache()
		{
			@Override
			public void putBitmap(String key, Bitmap value)
			{
				lruCache.put(key, value);
			}

			@Override
			public Bitmap getBitmap(String key)
			{
				return lruCache.get(key);
			}
		};
		
		return imageCache;
//
//		@SuppressWarnings("static-access")
//		ImageLoader imageLoader = new ImageLoader(netContext.getInstance(context).getJsonRequestQueue(), imageCache);
//		ImageListener listener = ImageLoader.getImageListener(mImageView, R.drawable.zhuanti_img, R.drawable.zhuanti_img);
//		imageLoader.get(imageUrl, listener);
		
	}
	
}
