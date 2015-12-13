package com.banermusic.event;

import com.banermusic.bean.SongBean;

/**
 * Created by kodywu on 5/12/15.
 */
public class ProgressMessage {

    private static ProgressMessage _ProgressMessage;

    public static ProgressMessage getInstance(){
        if(_ProgressMessage == null){
            _ProgressMessage = new ProgressMessage();
        }
        return _ProgressMessage;
    }

    private SongBean songBean;// 歌曲数据

    public SongBean getSongBean() {
        return songBean;
    }

    public void setSongBean(SongBean songBean) {
        this.songBean = songBean;
    }
}
