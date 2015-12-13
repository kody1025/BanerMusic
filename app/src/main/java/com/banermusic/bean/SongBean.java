package com.banermusic.bean;

import java.util.List;

/**
 * Created by Kody on 2015/9/24.
 */
public class SongBean extends BaseBean {

    private List<SongBean> result_list;

    private int id;
    private String song_name;// 歌曲名称
    private String song_url;// 歌曲网络路径
    private int album_id;// 专辑id
    private String album_name;// 专辑名称
    private String album_icon;// 专辑图片
    private String album_desc;// 专辑描述
    private String song_desc;// 歌曲描述
    private long duration;// 歌曲时长
    private long size; // 歌曲大小
    private String path; // 歌曲本地路径
    private long downSize;// 已经下载的进度
    private int playProgress;// 播放的进度
    private int bufferProgress;// 缓存进度

    public List<SongBean> getResult_list() {
        return result_list;
    }

    public void setResult_list(List<SongBean> result_list) {
        this.result_list = result_list;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSong_name() {
        return song_name;
    }

    public void setSong_name(String song_name) {
        this.song_name = song_name;
    }

    public String getSong_url() {
        return song_url;
    }

    public void setSong_url(String song_url) {
        this.song_url = song_url;
    }

    public int getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(int album_id) {
        this.album_id = album_id;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }

    public String getAlbum_icon() {
        return album_icon;
    }

    public void setAlbum_icon(String album_icon) {
        this.album_icon = album_icon;
    }

    public String getAlbum_desc() {
        return album_desc;
    }

    public void setAlbum_desc(String album_desc) {
        this.album_desc = album_desc;
    }

    public String getSong_desc() {
        return song_desc;
    }

    public void setSong_desc(String song_desc) {
        this.song_desc = song_desc;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDownSize() {
        return downSize;
    }

    public void setDownSize(long downSize) {
        this.downSize = downSize;
    }

    public int getPlayProgress() {
        return playProgress;
    }

    public void setPlayProgress(int playProgress) {
        this.playProgress = playProgress;
    }

    public int getBufferProgress() {
        return bufferProgress;
    }

    public void setBufferProgress(int bufferProgress) {
        this.bufferProgress = bufferProgress;
    }
}
