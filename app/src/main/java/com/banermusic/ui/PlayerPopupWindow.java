package com.banermusic.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.banermusic.R;
import com.banermusic.event.PlayerMessage;
import com.banermusic.event.ProgressMessage;
import com.banermusic.event.SongMessage;
import com.banermusic.manage.MediaManage;
import com.banermusic.util.MediaUtils;
import com.banermusic.widget.BaseSeekBar;
import com.banermusic.widget.HBaseSeekBar;
import com.banermusic.widget.NextButton;
import com.banermusic.widget.PlayButton;
import com.banermusic.widget.PrevButton;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import de.greenrobot.event.EventBus;

public class PlayerPopupWindow extends PopupWindow {

    private Context context;

    private SeekBar seekBar;
    private PlayButton playButton;
    private NextButton nextButton;
    private PrevButton prevButton;

    private TextView tvSongCurrentTime;
    private TextView tvSongTotalTime;

    private TextView tvSongTitle;
    private TextView tvSinger;

    private HBaseSeekBar seekBarVolumn;

    private MediaManage mediaManage;

    private AudioManager mAudioManager;

    private ImageView ivSwitch;

    /**
     * 判断SeekBar其是否是正在拖动
     */
    private boolean isStartTrackingTouch = false;

    public PlayerPopupWindow(final Context context, View.OnClickListener switchOnClickListener) {
        super(context);

        this.context = context;
        EventBus.getDefault().register(this);

        View view = LayoutInflater.from(context).inflate(R.layout.fragment_player,null, false);
        mediaManage = MediaManage.getMediaManage(context);

        playButton = (PlayButton) view.findViewById(R.id.player_PlayButton);
        prevButton = (PrevButton) view.findViewById(R.id.player_PrevButton);
        nextButton = (NextButton) view.findViewById(R.id.player_NextButton);

        tvSongCurrentTime = (TextView) view.findViewById(R.id.player_tvSongCurrentTime);
        tvSongTotalTime = (TextView) view.findViewById(R.id.player_tvSongTotalTime);

        tvSongTitle = (TextView) view.findViewById(R.id.player_tvSongTitle);
        tvSinger = (TextView) view.findViewById(R.id.player_tvSinger);

        seekBarVolumn = (HBaseSeekBar) view.findViewById(R.id.player_sbVolumn);

        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        seekBarVolumn.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBarVolumn.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        seekBarVolumn.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int process = seekBar.getProgress();
                PlayerMessage.VOLUMN_VALUE = process;
                EventBus.getDefault().post(PlayerMessage.getInstance(PlayerMessage.VOLUMN_UPDATE_MSG));
            }
        });

        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setEnabled(true);
        seekBar.setProgress(0);

        if(MediaManage.isPlaying()){
            seekBar.setMax((int)mediaManage.getPlaySongInfo().getDuration());
            tvSongTotalTime.setText(MediaUtils.formatTime((int) mediaManage.getPlaySongInfo().getDuration()));
            playButton.setIsChecked(true);
            tvSongTitle.setText(mediaManage.getPlaySongInfo().getSong_name());
        }else{
            tvSongTotalTime.setText(MediaUtils.formatTime(0));
            playButton.setIsChecked(false);
            tvSongTitle.setText("");
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                // // 拖动条进度改变的时候调用
                if (isStartTrackingTouch) {
                    int progress = seekBar.getProgress();
                    // System.out.println("onProgressChanged :--->" + progress);
                    // 往弹出窗口传输相关的进度
                    //seekBar.popupWindowShow(progress, mMenu,
                    //  kscTwoLineLyricsView.getTimeLrc(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                int progress = seekBar.getProgress();
                // System.out.println("onStartTrackingTouch :--->" + progress);
                // 拖动条开始拖动的时候调用
                //seekBar.popupWindowShow(progress, mMenu,
                //  kscTwoLineLyricsView.getTimeLrc(progress));
                isStartTrackingTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                isStartTrackingTouch = false;
                // 拖动条停止拖动的时候调用
                //seekBar.popupWindowDismiss();

                int progress = seekBar.getProgress();

                if(MediaManage.getMediaManage(context).getPlaySongInfo() != null){
                    SongMessage songMessage = new SongMessage(SongMessage.SEEKTO);
                    songMessage.setProgress(progress);
                    EventBus.getDefault().post(songMessage);
                }
            }
        });

        /**
         * 顺序播放
         */
        final ImageView modeALLImageButton = (ImageView) view
                .findViewById(R.id.mode_all_buttom);
        /**
         * 随机播放
         */
        final ImageView modeRandomImageButton = (ImageView) view
                .findViewById(R.id.mode_random_buttom);
        /**
         * 单曲循环
         */
        final ImageView modeSingleImageButton = (ImageView) view
                .findViewById(R.id.mode_single_buttom);

        modeALLImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                modeALLImageButton.setVisibility(View.INVISIBLE);
                modeRandomImageButton.setVisibility(View.VISIBLE);
                modeSingleImageButton.setVisibility(View.INVISIBLE);
                Toast.makeText(context, "随机播放", Toast.LENGTH_SHORT)
                        .show();

                PlayerMessage.PLAY_MODE = 2;
            }
        });

        modeRandomImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                modeALLImageButton.setVisibility(View.INVISIBLE);
                modeRandomImageButton.setVisibility(View.INVISIBLE);
                modeSingleImageButton.setVisibility(View.VISIBLE);
                Toast.makeText(context, "单曲循环", Toast.LENGTH_SHORT)
                        .show();

                PlayerMessage.PLAY_MODE = 0;
            }
        });

        modeSingleImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                modeALLImageButton.setVisibility(View.VISIBLE);
                modeRandomImageButton.setVisibility(View.INVISIBLE);
                modeSingleImageButton.setVisibility(View.INVISIBLE);

                Toast.makeText(context, "顺序播放", Toast.LENGTH_SHORT)
                        .show();

                PlayerMessage.PLAY_MODE = 1;
            }
        });

        // 默认是0单曲循环，1顺序播放，2随机播放
        switch (PlayerMessage.PLAY_MODE) {
            case 0:
                modeALLImageButton.setVisibility(View.INVISIBLE);
                modeRandomImageButton.setVisibility(View.INVISIBLE);
                modeSingleImageButton.setVisibility(View.VISIBLE);
                break;
            case 1:
                modeALLImageButton.setVisibility(View.VISIBLE);
                modeRandomImageButton.setVisibility(View.INVISIBLE);
                modeSingleImageButton.setVisibility(View.INVISIBLE);
                break;
            case 2:
                modeALLImageButton.setVisibility(View.INVISIBLE);
                modeRandomImageButton.setVisibility(View.VISIBLE);
                modeSingleImageButton.setVisibility(View.INVISIBLE);
                break;
        }

        ivSwitch = (ImageView) view.findViewById(R.id.ivSwitch);
        ivSwitch.setOnClickListener(switchOnClickListener);

        this.setContentView(view);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);

        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);

        ColorDrawable dw = new ColorDrawable(0xFFFFFFFF);
        this.setBackgroundDrawable(dw);

        this.setAnimationStyle(R.style.AnimBottom);
    }

    public void onEventMainThread(ProgressMessage progressMessage){
        if (!isStartTrackingTouch) {
            seekBar.setProgress((int) progressMessage.getSongBean().getPlayProgress());
            tvSongCurrentTime.setText(MediaUtils.formatTime((int) progressMessage.getSongBean().getPlayProgress()));
        }
    }

    public void onEventMainThread(SongMessage songMessage){
        switch (songMessage.getType()){
            case SongMessage.PLAY_UI:
                seekBar.setMax((int) songMessage.getSongBean().getDuration());
                seekBar.setProgress((int) songMessage.getSongBean().getPlayProgress());
                tvSongTotalTime.setText(MediaUtils.formatTime((int) songMessage.getSongBean().getDuration()));
                playButton.setIsChecked(true);
                tvSongTitle.setText(songMessage.getSongBean().getSong_name());

                playButton.setClickable(true);
                prevButton.setClickable(true);
                nextButton.setClickable(true);
                break;
            case SongMessage.PAUSE_UI:
                playButton.setIsChecked(false);
                break;
            case SongMessage.BUFFER_UPDATE:
                if(!isStartTrackingTouch){
                    seekBar.setSecondaryProgress(songMessage.getSongBean().getBufferProgress());
                }
                break;
            case SongMessage.INIT:
                seekBar.setProgress(0);
                playButton.setIsChecked(false);
                tvSongCurrentTime.setText(MediaUtils.formatTime(0));

                playButton.setClickable(false);
                prevButton.setClickable(false);
                nextButton.setClickable(false);
                break;

        }
    }

    public void onEventMainThread(PlayerMessage playerMessage){
        switch (playerMessage.getType()){
            case PlayerMessage.VOLUMN_UPDATE_MSG:
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,PlayerMessage.VOLUMN_VALUE,0);
                break;
            case PlayerMessage.VOLUMN_DISPLAY_MSG:
                seekBarVolumn.setProgress(PlayerMessage.VOLUMN_VALUE);
                break;
        }
    }



}
