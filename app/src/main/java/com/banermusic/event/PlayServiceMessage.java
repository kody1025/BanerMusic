package com.banermusic.event;

import com.banermusic.bean.SongBean;

/**
 * Created by kodywu on 5/12/15.
 */
public class PlayServiceMessage {

    private static PlayServiceMessage _PlayServiceMessage;

    public static final int SEEKTO = 1;

    public static final int PAUSE = 2;

    public static final int STOP = 3;

    public static final int PLAY = 4;

    public static final int TOPLAY = 5;

    private int type;

    private SongBean songBean;

    public static PlayServiceMessage getInstance(int type){
        if(_PlayServiceMessage == null){
            _PlayServiceMessage = new PlayServiceMessage();
        }
        _PlayServiceMessage.setType(type);
        return _PlayServiceMessage;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public SongBean getSongBean() {
        return songBean;
    }

    public void setSongBean(SongBean songBean) {
        this.songBean = songBean;
    }
}
