package com.banermusic.db;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.banermusic.bean.SongBean;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by kodywu on 31/10/15.
 */
public class SongDB {

    private File savePath = null;
    private final static int currentSongListLimit = 10;
    private static SongDB songDB;
    private static List<SongBean> songList = new ArrayList<>();

    public List<SongBean> getSongList(){
        return songList;
    }

    private SongDB(Context context){
        this.savePath = context.getExternalFilesDir(null);
        this.syncCurrentSong();
    }

    public static void initInstance(Context context){
        if(songDB == null){
            songDB = new SongDB(context);
        }
    }

    public static SongDB getInstance(){
        return songDB;
    }

    public void addCurrentSong(SongBean songBean){
        if(songList.contains(songBean)){
            return;
        }
        // 超过10首歌曲的时候，移除并保留10首
        while(songList.size() >= currentSongListLimit){
            songList.remove(0);
        }
        // 添加最近播放歌曲
        songList.add(songBean);

        if(this.savePath != null){
            // 持久化
            String json = JSON.toJSONString(songList);
            File file = new File(savePath,"song.json");
            if(file.exists()){
                file.delete();
            }
            try {
                FileWriter fw = new FileWriter(file,false);
                fw.write(json.trim());
                fw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void syncCurrentSong(){
        if(this.savePath != null){
            File file = new File(savePath,"song.json");
            try {
                if(file.exists()){
                    FileReader fr = new FileReader(file);
                    if(fr.ready()){
                        StringBuffer stringBuffer = new StringBuffer();
                        char[] buffer = new char[1024];
                        while (fr.read(buffer) > -1){
                            stringBuffer.append(buffer);
                        }
                        fr.close();
                        List<SongBean> list = JSON.parseArray(stringBuffer.toString().trim(),SongBean.class);
                        if(list != null && list.size() > 0){
                            songList = list;
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
                if(file.exists()){
                    file.delete();
                }
            }
        }
    }
}
