package com.banermusic.bean;

import java.util.List;

/**
 * Created by kodywu on 19/10/15.
 */
public class BannerBean extends BaseBean {

    private List<BannerBean> result_list;

    /**
     * 主键ID
     */
    private int id;
    /**
     * 图标
     */
    private String banner_icon;
    /**
     * 描述
     */
    private String banner_desc;
    /**
     * 类型：1专辑，2歌曲，3广告
     */
    private int banner_type;
    /**
     * 状态：0不展示，1正常
     */
    private int state;
    /**
     * 专辑ID(banner_type==1 或 banner_type==2有效)
     */
    private int album_id;
    /**
     * 歌曲ID(banner_type==2有效)
     */
    private int song_id;
    /**
     * 广告链接(banner_type==3有效)
     */
    private String ad_url;

    /**
     * 专辑描述
     */
    private AlbumBean album_dto;

    public List<BannerBean> getResult_list() {
        return result_list;
    }

    public void setResult_list(List<BannerBean> result_list) {
        this.result_list = result_list;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBanner_icon() {
        return banner_icon;
    }

    public void setBanner_icon(String banner_icon) {
        this.banner_icon = banner_icon;
    }

    public String getBanner_desc() {
        return banner_desc;
    }

    public void setBanner_desc(String banner_desc) {
        this.banner_desc = banner_desc;
    }

    public int getBanner_type() {
        return banner_type;
    }

    public void setBanner_type(int banner_type) {
        this.banner_type = banner_type;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(int album_id) {
        this.album_id = album_id;
    }

    public int getSong_id() {
        return song_id;
    }

    public void setSong_id(int song_id) {
        this.song_id = song_id;
    }

    public String getAd_url() {
        return ad_url;
    }

    public void setAd_url(String ad_url) {
        this.ad_url = ad_url;
    }

    public AlbumBean getAlbum_dto() {
        return album_dto;
    }

    public void setAlbum_dto(AlbumBean album_dto) {
        this.album_dto = album_dto;
    }
}
