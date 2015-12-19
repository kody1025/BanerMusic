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
	 * 1.1 获取音乐场景
	 */
	public static final String LIST_SCENE = BASE_URL + "music/list_scene";

	/**
	 * 1.2 获取场景当期专辑
	 */
	public static final String CURRENT_ALBUM = BASE_URL + "music/current_album";

	/**
	 * 1.3 获取场景专辑列表
	 */
	public static final String LIST_ALBUM = BASE_URL + "music/list_album";

	/**
	 * 1.4 专辑歌曲 (已过时)
	 */
	@Deprecated
	public static final String ALBUM_SONG = BASE_URL + "music/album_song";

	/**
	 * 1.5 歌曲信息记录
	 */
	public static final String INFO_RECORD = BASE_URL + "music/info_record";

	/**
	 * 1.6 Banner列表
	 */
	public static final String LIST_BANNER = BASE_URL + "music/list_banner";

	/**
	 * 1.7 今日推荐专辑列表
	 */
	public static final String RECOMMEND_ALBUM = BASE_URL + "music/recommend_album";

	/**
	 * 1.8 歌曲举报
	 */
	public static final String REPORT_MUSIC = BASE_URL + "music/report_music";

	/**
	 * 1.9 歌曲列表 (替换1.4)
	 */
	public static final String ALBUM_SONG_LIST = BASE_URL + "music/album_song_list";

	/**
	 * 1.10 搜索歌曲
	 */
	public static final String SEARCH_SONG = BASE_URL + "music/song_list";

	/**
	 * 2.1 校验是否可注册，可注册返回验证码
	 */
	public static final String CHECK_ACCOUNT = BASE_URL + "user/check_account";

	/**
	 * 2.2 注册
	 */
	public static final String REGISTER = BASE_URL + "user/register";

	/**
	 * 2.3 注册详细信息
	 */
	public static final String REGISTER_DETAILS = BASE_URL + "user/register_details";

	/**
	 * 2.4 登录
	 */
	public static final String LOGIN = BASE_URL + "user/login";

	/**
	 * 2.5 退出
	 */
	public static final String LOGOUT = BASE_URL + "user/logout";

	/**
	 * 2.6 打赏
	 */
	public static final String REWARD = BASE_URL + "user/";

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

	/**
	 * 设备唯一ID
	 */
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

	/**
	 * 存储空间至少预留10M才能下载
	 */
	public final static int SD_REMAIN_SIZE = 10;

	/** 单次缓存文件最大值 */
	public static final int AUDIO_BUFFER_MAX_LENGTH = 4 * 1024 * 1024;

	// Http Header Name
	public final static String CONTENT_RANGE = "Content-Range";
	public final static String CONTENT_LENGTH = "Content-Length";
	public final static String RANGE = "Range";
	public final static String HOST = "Host";
	public final static String USER_AGENT = "User-Agent";
	// Http Header Value Parts
	public final static String RANGE_PARAMS = "bytes=";
	public final static String RANGE_PARAMS_0 = "bytes=0-";
	public final static String CONTENT_RANGE_PARAMS = "bytes ";

	public final static String LINE_BREAK = "\r\n";
	public final static String HTTP_END = LINE_BREAK + LINE_BREAK;
}
