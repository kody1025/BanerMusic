package com.banermusic.event;

import com.banermusic.bean.SongBean;

/**
 * Created by kodywu on 1/11/15.
 */
public class SongDBMessage {

    private static SongDBMessage _SongDBMessage;

    private SongDBMessage(){

    }

    public static SongDBMessage getInstance(int type){
        if(_SongDBMessage == null){
            _SongDBMessage = new SongDBMessage();
        }
        _SongDBMessage.type = type;
        return _SongDBMessage;
    }

    private int type = 0;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    // 添加播放记录
    public static final int SONGDB_ADD_MSG = 1;

    private SongBean songBean;

    public SongBean getSongBean() {
        return songBean;
    }

    public void setSongBean(SongBean songBean) {
        this.songBean = songBean;
    }
}
