package com.banermusic.bean;

import java.util.List;

/**
 * Created by Kody on 2015/9/24.
 */
public class AlbumBean extends BaseBean {

    private AlbumBean result;

    private int id;
    private int scene_id;
    private String album_name;
    private String album_desc;
    private String album_icon;
    private List<AlbumBean> result_list;

    public AlbumBean getResult() {
        return result;
    }

    public void setResult(AlbumBean result) {
        this.result = result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScene_id() {
        return scene_id;
    }

    public void setScene_id(int scene_id) {
        this.scene_id = scene_id;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }

    public String getAlbum_desc() {
        return album_desc;
    }

    public void setAlbum_desc(String album_desc) {
        this.album_desc = album_desc;
    }

    public String getAlbum_icon() {
        return album_icon;
    }

    public void setAlbum_icon(String album_icon) {
        this.album_icon = album_icon;
    }

    public List<AlbumBean> getResult_list() {
        return result_list;
    }

    public void setResult_list(List<AlbumBean> result_list) {
        this.result_list = result_list;
    }
}
