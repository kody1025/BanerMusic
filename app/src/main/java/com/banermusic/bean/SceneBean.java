package com.banermusic.bean;

import java.util.List;

/**
 * Created by Kody on 2015/9/24.
 */
public class SceneBean extends BaseBean {

    private List<SceneBean> result_list;

    private int id;
    private String scene_name;
    private String scene_icon;

    public List<SceneBean> getResult_list() {
        return result_list;
    }

    public void setResult_list(List<SceneBean> result_list) {
        this.result_list = result_list;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getScene_name() {
        return scene_name;
    }

    public void setScene_name(String scene_name) {
        this.scene_name = scene_name;
    }

    public String getScene_icon() {
        return scene_icon;
    }

    public void setScene_icon(String scene_icon) {
        this.scene_icon = scene_icon;
    }
}
