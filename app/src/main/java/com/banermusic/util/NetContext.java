package com.banermusic.util;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * 使用NetContext来访问网络，NetContext底部使用Volley作为通信框架 
 */  
public class NetContext {  
      
    /** json请求队列  */  
    private RequestQueue jsonRequestQueue;
      
    private static NetContext instance = null;  
      
      
    /** 
     * 构造函数 
     *  
     * @param context android应用上下文 
     */  
    private NetContext(Context context){
        this.jsonRequestQueue = Volley.newRequestQueue(context, new MultiPartStack());
    }  
      
    /** 
     * 单例模式 
     *  
     * @param context 
     * @return NetUtil 
     */  
    public static NetContext getInstance(Context context){
        if (instance == null) {  
            synchronized (NetContext.class) {  
                if (instance == null) {  
                    instance = new NetContext(context);  
                }  
            }  
        }  
        return instance;  
    }  

    /** 
     * 得到json的请求队列 
     * @return RequestQueue 
     */  
    public RequestQueue getJsonRequestQueue() {
        return jsonRequestQueue;  
    }  
}  