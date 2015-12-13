package com.banermusic.event;

/**
 * Created by kodywu on 14/10/15.
 */
public class PlayerMessage {

    private static PlayerMessage _PlayerMessage;

    private PlayerMessage(){

    }

    public static PlayerMessage getInstance(int type){
        if(_PlayerMessage == null){
            _PlayerMessage = new PlayerMessage();
        }
        _PlayerMessage.type = type;
        return _PlayerMessage;
    }

    private int type = 0;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * 声音大小
     */
    public static final int VOLUMN_DISPLAY_MSG = 1;

    public static final int VOLUMN_UPDATE_MSG = 2;

    /**
     * 声音值
     */
    public static int VOLUMN_VALUE = 0;

    /**
     * 播放模式
     */
    public static int PLAY_MODE = 1;

}
