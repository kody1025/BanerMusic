package com.banermusic.event;

import com.banermusic.bean.SongBean;

import java.util.Map;

/**
 * Created by kodywu on 8/12/15.
 */
public class DownloadMessage {
    public final static int DOWNLOAD = 1;
    public final static int DOWNLOADPROGRESS = 2;

    public DownloadMessage(int type){
        this.type = type;
    }

    private int type;

    private static Map<Integer,SongBean> downloadMap;

    private SongBean downloadSong;

    public static Map<Integer, SongBean> getDownloadMap() {
        return downloadMap;
    }

    public static void setDownloadMap(Map<Integer, SongBean> downloadMap) {
        DownloadMessage.downloadMap = downloadMap;
    }

    public SongBean getDownloadSong() {
        return downloadSong;
    }

    public void setDownloadSong(SongBean downloadSong) {
        this.downloadSong = downloadSong;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
