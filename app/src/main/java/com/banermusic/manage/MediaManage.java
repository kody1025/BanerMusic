package com.banermusic.manage;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.android.volley.VolleyError;
import com.banermusic.apis.CommonPost;
import com.banermusic.apis.RequestListener;
import com.banermusic.bean.BaseBean;
import com.banermusic.bean.SongBean;
import com.banermusic.event.PlayServiceMessage;
import com.banermusic.event.PlayerMessage;
import com.banermusic.event.SongDBMessage;
import com.banermusic.event.SongMessage;
import com.banermusic.constant.BaseConstants;
import com.banermusic.logger.MyLogger;
import com.banermusic.services.MediaPlayerService;

import java.util.List;
import java.util.Random;

import de.greenrobot.event.EventBus;

public class MediaManage {
    /**
     * 播放管理单例对象
     */
    private static MediaManage _mediaManage;
    /**
     * 当前播放列表
     */
    private List<SongBean> playList;
    /**
     * 当前播放列表id，用于判断是否更新当前播放列表
     * 如果是普通播放列表，则为专辑id
     * 如果是最近播放列表，则标识为-1
     */
    private int playListId = 0;
    /**
     * 当前播放对象
     */
    private SongBean playSongInfo;
    /**
     * 播放列表索引
     */
    private int playIndex = -1;
    /**
     * 当前播放的歌曲sid
     */
    private int playSID = -1;

    private SongMessage songMessage;

    private Context context;

    private MyLogger logger = MyLogger.getLogger(MediaManage.class.getName());

    public MediaManage(Context context) {
        this.context = context;
        EventBus.getDefault().register(this);
    }

    /**
     * 获取播放管理单例对象
     * @param context
     * @return
     */
    public static MediaManage getMediaManage(Context context) {
        if (_mediaManage == null) {
            _mediaManage = new MediaManage(context);
        }
        return _mediaManage;
    }

    /**
     * 设置播放列表数据
     * @param playList
     * @param playListId
     * @param playSID
     */
    public void setDataSource(List<SongBean> playList, int playListId, int playSID) {
        if(this.playListId != playListId) {
            this.playList = playList;
            this.playListId = playListId;

            // 更换播放列表时，先暂停播放服务
            if(MediaPlayerService.STATUS == MediaPlayerService.PLAYING){
                pause();
            }
        }

        this.playSID = playSID;
        init(context);
    }

    /**
     * 初始化播放歌曲对象
     *
     * @param context
     */
    private void init(Context context) {

        for (int i = 0; i < playList.size(); i++) {
            SongBean tempSongInfo = playList.get(i);
            if (tempSongInfo.getId() == playSID) {

                playIndex = i;

                playSongInfo = tempSongInfo;

                /*// 发送历史歌曲数据给其它页面
                SongMessage songMessage = new SongMessage(SongMessage.INIT);
                songMessage.setSongBean(tempSongInfo);
                EventBus.getDefault().post(songMessage);*/
                break;
            }
        }
    }

    public void setPlaySID(int playId){
        this.playSID = playId;
    }

    /**
     * 获取歌曲列表的大小
     *
     * @return
     */
    public int getCount() {
        return playList.size();
    }

    public void onEventBackgroundThread(SongMessage songMessage){
        switch (songMessage.getType()) {
            case SongMessage.SELECTPLAY:
            case SongMessage.SELECTPLAYED:
                playIndex = getPlayIndex();
                selectPlay(playList.get(playIndex));
                break;
            case SongMessage.PLAY:
                play();
                break;
            case SongMessage.NEXTMUSIC:
                nextPlay(false);
                break;
            case SongMessage.FINISHNEXTMUSICED:
                nextPlay(true);
                break;
            case SongMessage.PREVMUSIC:
                prevSong();
                break;
            case SongMessage.SEEKTO:
                int progress = songMessage.getProgress();
                seekTo(progress);
                break;
            case SongMessage.PLAYING:
                if (playSongInfo != null) {
                    SongBean songBean = songMessage.getSongBean();
                    if (playSongInfo.getId() == songBean.getId()) {
                        playSongInfo
                                .setPlayProgress(songBean.getPlayProgress());
                    }
                }
                break;
            case SongMessage.PAUSE:
                pause();
                break;
        }
    }

    /**
     * 快进
     *
     * @param progress
     */
    private void seekTo(int progress) {
        // 如果服务正在运行，则是正在播放
        if (playSongInfo == null) {
            return;
        }
        playSongInfo.setPlayProgress(progress);
        PlayServiceMessage serviceMessage = PlayServiceMessage.getInstance(PlayServiceMessage.SEEKTO);
        serviceMessage.setSongBean(playSongInfo);
        EventBus.getDefault().post(serviceMessage);
    }

    /**
     * 暂停播放
     */
    private void pause() {
        if (playIndex == -1) {
            songMessage = new SongMessage(SongMessage.ERROR);
            songMessage.setErrorMessage("没选中歌曲!");
            EventBus.getDefault().post(songMessage);
            return;
        }
        // 如果服务正在运行，则是正在播放
        if (MediaPlayerService.STATUS == MediaPlayerService.PLAYING) {
            PlayServiceMessage serviceMessage = PlayServiceMessage.getInstance(PlayServiceMessage.PAUSE);
            EventBus.getDefault().post(serviceMessage);
        }else{
            songMessage = new SongMessage(SongMessage.ERROR);
            songMessage.setErrorMessage("操作失败，请再尝试!");
            EventBus.getDefault().post(songMessage);
            logger.e("暂停音乐失败! SID:"+getPlaySongInfo().getId()+",当前状态:"+MediaPlayerService.STATUS);
        }
    }

    /**
     * 播放或者暂停
     */
    private void play() {
        if (playIndex == -1) {
            songMessage = new SongMessage(SongMessage.ERROR);
            songMessage.setErrorMessage("没选中歌曲!");
            EventBus.getDefault().post(songMessage);
            return;
        }

        // 如果服务正在运行，则是正在播放
        if (MediaPlayerService.STATUS == MediaPlayerService.PAUSING) {
            PlayServiceMessage serviceMessage = PlayServiceMessage.getInstance(PlayServiceMessage.PLAY);
            EventBus.getDefault().post(serviceMessage);
        }else{
            songMessage = new SongMessage(SongMessage.ERROR);
            songMessage.setErrorMessage("操作失败，请再尝试!");
            EventBus.getDefault().post(songMessage);
            logger.e("播放音乐失败! SID:" + getPlaySongInfo().getId() + ",当前状态:" + MediaPlayerService.STATUS);
        }
    }

    /**
     * 上一首
     */
    private void prevSong() {
        if (playList.size() == 0) {
            songMessage = new SongMessage(SongMessage.ERROR);
            songMessage.setErrorMessage("没有歌曲列表!!");
            EventBus.getDefault().post(songMessage);
            return;
        }
        playIndex = getPlayIndex();

        int playMode = PlayerMessage.PLAY_MODE;
        // 默认是0单曲循环，1顺序播放，2随机播放
        switch (playMode) {
            case 0:
                break;
            case 1:
                playIndex--;
                if (playIndex < 0) {
                    playIndex = 0;

                    songMessage = new SongMessage(SongMessage.ERROR);
                    songMessage.setErrorMessage("已经是第一首了!!");
                    EventBus.getDefault().post(songMessage);
                    return;
                }
                break;
            case 2:
                playIndex = new Random().nextInt(playList.size());
                break;
        }

        SongBean tempSongInfo = playList.get(playIndex);

        this.playSID = tempSongInfo.getId();
        /*songMessage = new SongMessage(SongMessage.PREVMUSICED);
        songMessage.setSongBean(tempSongInfo);
        EventBus.getDefault().post(songMessage);*/

        // 如果服务正在运行，则是正在播放
        if (playSongInfo != null) {
            playSongInfo = null;
        }
        toPlay(tempSongInfo);
    }

    /**
     * 下一首
     *
     * @param isFinsh 是否是播放完成后调用
     */
    private void nextPlay(boolean isFinsh) {
        if (playList.size() == 0) {
            songMessage = new SongMessage(SongMessage.ERROR);
            songMessage.setErrorMessage("没有歌曲列表!!");
            EventBus.getDefault().post(songMessage);
            return;
        }
        playIndex = getPlayIndex();

        int playMode = PlayerMessage.PLAY_MODE;
        // 默认是0单曲循环，1顺序播放，2随机播放
        switch (playMode) {
            case 0:
                if(isFinsh){
                    break;
                }
            case 1:
                playIndex++;
                if (playIndex >= playList.size()) {

                    //playIndex = playlist.size() - 1;

                    //if (isFinsh) {
                        //playSongInfo = null;
                        playIndex = 0;
                        //PlayerMessage.PLAY_SID = -1;

                        // 上一首播放完成消息，暂无用，留拓展
                        /*songMessage = new SongMessage(SongMessage.LASTPLAYFINISH);
                        SongBean tempSongInfo = new SongBean();
                        tempSongInfo.setId(-1);
                        tempSongInfo.setPlayProgress(0);
                        songMessage.setSongBean(tempSongInfo);
                        EventBus.getDefault().post(songMessage);*/

                        // 如果服务正在运行，则是正在播放
                        /*if (MediaPlayerService.STATUS == MediaPlayerService.PLAYING) {
                            MediaPlayerService.STATUS = MediaPlayerService.PAUSING;
                            songMessage = new SongMessage(SongMessage.STOP);
                            EventBus.getDefault().post(songMessage);
                        }*/

                    /*} else {
                        songMessage = new SongMessage(SongMessage.ERROR);
                        songMessage.setErrorMessage("已经是最后一首了!");
                        EventBus.getDefault().post(songMessage);
                        return;
                    }*/
                }
                break;
            case 2:
                playIndex = new Random().nextInt(playList.size());
                break;
        }

        SongBean tempSongInfo = playList.get(playIndex);

        this.playSID = tempSongInfo.getId();

        // 播放下一首准备完毕，留拓展
        /*songMessage = new SongMessage(SongMessage.NEXTMUSICED);
        songMessage.setSongBean(tempSongInfo);
        EventBus.getDefault().post(songMessage);*/

        if (playSongInfo != null) {
            playSongInfo = null;
        }
        toPlay(tempSongInfo);
    }

    /**
     * 选择播放歌曲
     *
     * @param songBean
     */
    private void selectPlay(SongBean songBean) {
        if (playSongInfo != null) {
            playSongInfo = null;
        }
        toPlay(songBean);
    }

    /**
     * 播放歌曲
     *
     * @param songBean
     */
    private void toPlay(SongBean songBean) {
        if (playSongInfo == null) {
            playSongInfo = songBean;
            playSongInfo.setPlayProgress(0);
        }

        // 通知界面复位
        songMessage = new SongMessage(SongMessage.INIT);
        EventBus.getDefault().post(songMessage);

        // 加入最近播放记录
        SongDBMessage songDBMessage = SongDBMessage.getInstance(SongDBMessage.SONGDB_ADD_MSG);
        songDBMessage.setSongBean(songBean);
        EventBus.getDefault().post(songDBMessage);

        // 启动播放服务
        PlayServiceMessage serviceMessage = PlayServiceMessage.getInstance(PlayServiceMessage.TOPLAY);
        EventBus.getDefault().post(serviceMessage);

        // 添加播放记录
        CommonPost.infoRecord(context, BaseConstants.getUDID(), songBean.getId(), 1, "", "", new RequestListener() {
            @Override
            public void onResponse(String response) {
                BaseBean result = JSON.parseObject(response, BaseBean.class);
                if (!result.getRet_code().equals("0")) {
                    SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                    songMessage.setErrorMessage(result.getRet_msg());
                    EventBus.getDefault().post(songMessage);
                    return;
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                songMessage.setErrorMessage("网络连接失败！");
                EventBus.getDefault().post(songMessage);
            }
        });

    }

    /**
     * 获取当前播放歌曲的索引
     *
     * @return
     */
    public int getPlayIndex() {
        int index = -1;
        for (int i = 0; i < playList.size(); i++) {
            SongBean tempSongInfo = playList.get(i);
            if (tempSongInfo.getId() == playSID) {
                Log.i("MediaManage","GetPlayIndex:"+String.valueOf(i)+" playSID:"+playSID);
                return i;
            }
        }
        return index;
    }

    /*private void add(SongBean SongBean) {
        if (playlist == null || playlist.size() == 0) {
            playlist = new ArrayList<SongBean>();
            playlist.add(SongBean);
            return;
        }
        char category = SongBean.getCategory().charAt(0);
        String childCategory = SongBean.getChildCategory();
        for (int i = 0; i < playlist.size(); i++) {
            SongBean tempSongInfo = playlist.get(i);
            char tempCategory = tempSongInfo.getCategory().charAt(0);
            if (category == tempCategory) {
                String tempChildCategory = tempSongInfo.getChildCategory();
                if (childCategory.compareTo(tempChildCategory) < 0) {
                    playlist.add(i, SongBean);
                    return;
                }
            } else if (category < tempCategory) {
                playlist.add(i, SongBean);
                return;
            } else if (i == playlist.size() - 1) {
                playlist.add(SongBean);
                return;
            }
        }
    }*/

    /**
     * 删除所有的歌曲列表
     */
    /*private void delAllMusic() {
        if (playlist == null || playlist.size() == 0)
            return;
        int size = 0 - playlist.size();
        for (int i = 0; i < playlist.size(); i++) {
            if (playSongInfo != null) {
                if (playlist.get(i).getSid().equals(playSongInfo.getSid())) {
                    stopMusic();
                }
            }
        }
        playlist = new ArrayList<SongBean>();
        SongDB.getSongInfoDB(context).delete();
        SongMessage songMessage = new SongMessage();
        songMessage.setNum(size);
        songMessage.setType(SongMessage.DELALLMUSICED);
        ObserverManage.getObserver().setMessage(songMessage);

    }*/

    /**
     * stop停止正在播放的歌曲
     * 暂没有用上
     */
    private void stopMusic() {

        // 如果正在播放
        if (MediaPlayerService.STATUS == MediaPlayerService.PLAYING) {
            PlayServiceMessage serviceMessage = PlayServiceMessage.getInstance(PlayServiceMessage.STOP);
            EventBus.getDefault().post(serviceMessage);
        }

        playSongInfo = null;
        playIndex = -1;
        playSID = -1;
        songMessage = new SongMessage(SongMessage.LASTPLAYFINISH);

        SongBean tempSongInfo = new SongBean();
        tempSongInfo.setId(-1);
        tempSongInfo.setPlayProgress(0);
        tempSongInfo.setDuration(100);
        songMessage.setSongBean(tempSongInfo);

        EventBus.getDefault().post(songMessage);
    }

    /**
     * 通过sid来删除 playlist 中的数据
     */
    /*private void refresh(final String sid) {
		if (playlist == null || playlist.size() == 0)
			return;
		new Thread() {

			@Override
			public void run() {
				SongDB.getSongInfoDB(context).delete(sid);
			}

		}.start();
		for (int i = 0; i < playlist.size(); i++) {
			if (playlist.get(i).getSid().equals(sid)) {
				if (playSongInfo != null) {
					if (playlist.get(i).getSid().equals(playSongInfo.getSid())) {
						stopMusic();
					}
				}
				SongMessage songMessage = new SongMessage();
				songMessage.setNum(-1);
				songMessage.setSongInfo(playlist.get(i));
				songMessage.setType(SongMessage.DEL_NUM);
				ObserverManage.getObserver().setMessage(songMessage);
				playlist.remove(i);
				break;
			}
		}
	}*/
    public List<SongBean> getPlaylist() {
        return playList;
    }

    public SongBean getPlaySongInfo() {
        return playSongInfo;
    }

    /**
     * 判断当前是否在播放
     * @return
     */
    public static boolean isPlaying(){
        return MediaPlayerService.STATUS == MediaPlayerService.PLAYING || MediaPlayerService.STATUS == MediaPlayerService.PREPARING;
    }
}
