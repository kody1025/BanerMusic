package com.banermusic.constant;

import android.os.Environment;
import android.os.Message;
import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 基本常量
 * 
 * @author Kody
 * 
 */
public class BaseConstants
{
	public static final String APPNAME = "BanerMusic";
	/**
	 * 创建HttpClient对象
	 */
	public static HttpClient httpClient = new DefaultHttpClient();

	/**
	 * 共有的url地址
	 * 生产URL 121.40.126.50
	 * 测试URL 120.24.166.135:8080
	 */

	public static final String BASE_SERVERIP = "http://121.40.126.50";
	
	/**
	 * 共有的url地址
	 */
	public static final String BASE_URL = BASE_SERVERIP+"/baner-openapi-server/rest/";
	/**
	 * 是否debug模式
	 */
	public static final boolean IS_DEBUG = true;

	/**
	 * 获取音乐场景
	 */
	public static final String LIST_SCENE = BASE_URL + "music/list_scene";
	/**
	 * 获取场景当期专辑
	 */
	public static final String CURRENT_ALBUM = BASE_URL + "music/current_album";
	/**
	 * 获取场景专辑列表
	 */
	public static final String LIST_ALBUM = BASE_URL + "music/list_album";
	/**
	 * 专辑歌曲
	 */
	public static final String ALBUM_SONG = BASE_URL + "music/album_song";
	/**
	 * 歌曲信息记录
	 */
	public static final String INFO_RECORD = BASE_URL + "music/info_record";

	/**
	 * Banner列表
	 */
	public static final String LIST_BANNER = BASE_URL + "music/list_banner";

	/**
	 * 是否是第一次使用，默认值是true
	 */
	public static boolean THE_FIRST = true;

	/**
	 * app是否退出 0是否 1是 退出
	 */
	public static boolean APPCLOSE = false;

	/**
	 * 临时目录
	 */
	public final static String PATH_TEMP = Environment
			.getExternalStorageDirectory() + File.separator + "BanerMusic";

	/**
	 * Logcat日志目录
	 */
	public final static String PATH_LOGCAT = PATH_TEMP + File.separator
			+ "logcat";

	private static String UDID = null;

	public static void initUDID(String imei,String macAddress){
		try {
			if(macAddress == null){
				macAddress = "abcdefghijk";
			}
			String temp = imei + "|" + macAddress;
			int length = temp.length();
			if(length < 32){
				for(int i = 1; i <= 32 - length; i++){
					temp += "0";
				}
			}
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(temp.getBytes(), 0, 32);
			byte md5Data[] = md.digest();
			String udid = new String();
			for (int i=0;i<md5Data.length;i++) {
				int b =  (0xFF & md5Data[i]);
				// if it is a single digit, make sure it have 0 in front (proper padding)
				if (b <= 0xF)
					udid+="0";
				// add number to string
				udid+=Integer.toHexString(b);
			}   // hex string to uppercase
			UDID = udid.toUpperCase();
			Log.i("udid",UDID);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static String getUDID(){
		return UDID;
	}

	public static int widthPixels = 0;

	public static int heightPixels = 0;
}
