package com.banermusic.apis;

import android.content.Context;

import com.banermusic.constant.BaseConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kody on 2015/9/24.
 */
public class CommonPost extends BaseApi {

    /**
     * 获取音乐场景
     * @param context
     * @param listener
     */
    public static void listScene(final Context context, RequestListener listener){
        sendMapRequest(context, BaseConstants.LIST_SCENE, new HashMap<String, String>(), listener);
    }

    /**
     * 获取场景当期专辑
     * @param context
     * @param sceneId
     *          场景ID
     * @param listener
     */
    public static void currentAlbum(final Context context, int sceneId, RequestListener listener){
        Map<String,String> map = new HashMap<String, String>();
        map.put("scene_id", String.valueOf(sceneId));
        sendMapRequest(context, BaseConstants.CURRENT_ALBUM, map, listener);
    }

    /**
     * 获取场景专辑列表
     * @param context
     * @param sceneId
     *          场景ID
     * @param listener
     */
    public static void listAlbum(final Context context, int sceneId, RequestListener listener){
        Map<String,String> map = new HashMap<String, String>();
        map.put("scene_id", String.valueOf(sceneId));
        sendMapRequest(context, BaseConstants.LIST_ALBUM, map, listener);
    }

    /**
     * 专辑歌曲
     * @param context
     * @param albumId
     *          专辑ID
     * @param listener
     */
    public static void albumSong(final Context context, int albumId, RequestListener listener){
        Map<String,String> map = new HashMap<String, String>();
        map.put("album_id", String.valueOf(albumId));
        sendMapRequest(context, BaseConstants.ALBUM_SONG, map, listener);
    }

    /**
     * 歌曲信息记录
     * @param context
     * @param clientId
     *          客户端标识
     * @param songId
     *          歌曲ID
     * @param type
     *          类型：1播放，2收藏，3下载
     * @param playTime
     *          播放时间yyyy-MM-dd HH:mm:ss（type=1时有效）
     * @param endTime
     *          播放结束时间yyyy-MM-dd HH:mm:ss（type=1时有效）
     * @param listener
     */
    public static void infoRecord(final Context context, String clientId, int songId, int type, String playTime, String endTime, RequestListener listener){
        Map<String,String> map = new HashMap<String, String>();
        map.put("client_id", clientId);
        map.put("song_id", String.valueOf(songId));
        map.put("type", String.valueOf(type));
        map.put("play_time", playTime);
        map.put("end_time", endTime);
        sendMapRequest(context, BaseConstants.INFO_RECORD, map, listener);
    }

    /**
     * list banner
     * @param context
     * @param listener
     */
    public static void listBanner(final Context context, RequestListener listener){
        sendMapRequest(context, BaseConstants.LIST_BANNER, new HashMap<String, String>(), listener);
    }

    
}
