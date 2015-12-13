package com.banermusic.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.banermusic.bean.SongBean;
import com.banermusic.event.PlayServiceMessage;
import com.banermusic.event.ProgressMessage;
import com.banermusic.event.SongMessage;
import com.banermusic.constant.BaseConstants;
import com.banermusic.logger.MyLogger;
import com.banermusic.manage.MediaManage;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import de.greenrobot.event.EventBus;

/**
 * 
 * 播放器服务，主要负责播放
 * 
 */
public class MediaPlayerService extends Service {
	private MyLogger logger = MyLogger.getLogger(MediaPlayerService.class.getName());

	public static Boolean isServiceRunning = false;

	public static int STATUS = 0;
	public final static int STOPED = 0;
	public final static int PLAYING = 1;
	public final static int PAUSING = 2;
	public final static int PREPARING = 3;

	private Thread playerThread = null;
	private SongMessage songMessage;
	private MediaPlayer player;
	private Context context;

	private SongBean songBean;

	private Boolean isFirstStart = true;

	/**
	 * 音频管理
	 */
	private AudioManager audioManager;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		context = MediaPlayerService.this.getBaseContext();
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		EventBus.getDefault().register(this);
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		isServiceRunning = true;
		if (!isFirstStart) {
			isFirstStart = false;
			play();
		}
		logger.i("------MediaPlayerService被创建了------");
	}

	/**
	 * 播放
	 */
	private void play() {

		// 正在准备歌曲，拒绝其他操作
		if(this.STATUS == PREPARING){
			return;
		}

		songBean = MediaManage.getMediaManage(context).getPlaySongInfo();
		if (songBean == null) {
			return;
		}

		try {
			if (player == null && this.STATUS == STOPED) {
				player = new MediaPlayer();
				player.setOnPreparedListener(onPreparedListener);
				player.setOnCompletionListener(onCompletionListener);
				player.setOnErrorListener(onErrorListener);
			}else if(this.STATUS == PLAYING || this.STATUS == PAUSING){
				player.stop();
				// 播放器重置
				player.reset();
			}

			this.STATUS = PREPARING;

			// 检查本地路径是否存在歌曲文件，不存在则播放网络文件
			String path = null;
			if(songBean.getPath() != null){
				File file = new File(songBean.getPath());
				if(file.exists()){
					path = songBean.getPath();
				}
			}else{
				path = songBean.getSong_url();
			}

			player.setDataSource(path);
			//logger.i("设置歌曲播放路径：" + path);
			player.prepareAsync();

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.e(sw.toString());

			songMessage = new SongMessage(SongMessage.ERROR);
			String errorMessage = "播放歌曲出错，跳转下一首!!";
			songMessage.setErrorMessage(errorMessage);
			EventBus.getDefault().post(songMessage);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			if (songMessage != null) {
				// 跳转下一首
				songMessage = new SongMessage(SongMessage.NEXTMUSIC);
				EventBus.getDefault().post(songMessage);
			}
		}

		/*player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				if(percent < 100){
					SongMessage songMessage = new SongMessage(SongMessage.BUFFER_UPDATE);
					songBean.setBufferProgress(player.getDuration() / 100 * percent);
					songMessage.setSongBean(songBean);
					EventBus.getDefault().post(songMessage);
				}
			}
		});*/
	}

	MediaPlayer.OnErrorListener onErrorListener = new OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer mp, int what, int arg2) {

			switch (what){
				case MediaPlayer.MEDIA_ERROR_IO:
					logger.e("MediaPlayer Error:MEDIA_ERROR_IO");
					break;
				case MediaPlayer.MEDIA_ERROR_MALFORMED:
					logger.e("MediaPlayer Error:MEDIA_ERROR_MALFORMED");
					break;
				case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
					logger.e("MediaPlayer Error:MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
					break;
				case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
					logger.e("MediaPlayer Error:MEDIA_ERROR_SERVER_DIED");
					break;
				case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
					logger.e("MediaPlayer Error:MEDIA_ERROR_TIMED_OUT");
					break;
				case MediaPlayer.MEDIA_ERROR_UNKNOWN:
					logger.e("MediaPlayer Error:MEDIA_ERROR_UNKNOWN");
					break;
				case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
					logger.e("MediaPlayer Error:MEDIA_ERROR_UNSUPPORTED");
					break;
				default:
					logger.e("MediaPlayer Error:"+what);
					break;
			}

			songMessage = new SongMessage(SongMessage.ERROR);
			String errorMessage = "播放歌曲出错，跳转下一首!!";
			songMessage.setErrorMessage(errorMessage);
			EventBus.getDefault().post(songMessage);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}

			player.reset();

			if (songMessage != null) {
				// 跳转下一首
				songMessage = new SongMessage(SongMessage.NEXTMUSIC);
				EventBus.getDefault().post(songMessage);
			}
			return true;
		}
	};

	MediaPlayer.OnCompletionListener onCompletionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			// 下一首
			SongMessage songMessage = new SongMessage(SongMessage.FINISHNEXTMUSICED);
			EventBus.getDefault().post(songMessage);
		}
	};

	MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer mp) {

			logger.i("播放器准备就绪");
			// 按照上次进度继续播放
			if (songBean.getPlayProgress() != 0) {
				player.seekTo((int) songBean.getPlayProgress());
			}

			songBean.setDuration(player.getDuration());

			// 请求播放的音频焦点
			int result = audioManager.requestAudioFocus(afChangeListener,
					// 指定所使用的音频流
					AudioManager.STREAM_MUSIC,
					// 请求长时间的音频焦点
					AudioManager.AUDIOFOCUS_GAIN);
			if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				logger.i("获取音频焦点成功");

				player.start();
				MediaPlayerService.STATUS = PLAYING;
				if (playerThread == null) {
					playerThread = new Thread(new PlayerRunable());
					playerThread.start();
				}

				// 发送播放消息，其他页面更新显示
				songMessage = new SongMessage(SongMessage.PLAY_UI);
				songMessage.setSongBean(songBean);
				EventBus.getDefault().post(songMessage);

			} else {
				logger.i("获取音频焦点失败!!");
			}
		}
	};

	OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			/**
			 * AUDIOFOCUS_GAIN：获得音频焦点。
			 * AUDIOFOCUS_LOSS：失去音频焦点，并且会持续很长时间。这是我们需要停止MediaPlayer的播放。
			 * AUDIOFOCUS_LOSS_TRANSIENT
			 * ：失去音频焦点，但并不会持续很长时间，需要暂停MediaPlayer的播放，等待重新获得音频焦点。
			 * AUDIOFOCUS_REQUEST_GRANTED 永久获取媒体焦点（播放音乐）
			 * AUDIOFOCUS_GAIN_TRANSIENT 暂时获取焦点 适用于短暂的音频
			 * AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK Duck我们应用跟其他应用共用焦点
			 * 我们播放的时候其他音频会降低音量
			 */
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
				logger.i("AUDIOFOCUS_LOSS_TRANSIENT");
				// Toast.makeText(context, "AUDIOFOCUS_LOSS_TRANSIENT",
				// Toast.LENGTH_LONG).show();
				//stop();

			} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {

				// CAN_DUCK = true;
				if (player != null) {
					player.setVolume(0.5f, 0.5f);
				}

				// 降低音量
				// Toast.makeText(context, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK",
				// Toast.LENGTH_LONG).show();
				logger.i("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:" + focusChange);

			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

				if (player != null) {
					player.setVolume(1.0f, 1.0f);
				}
				// CAN_DUCK = false;

				// 恢复至正常音量
				logger.i("AUDIOFOCUS_GAIN");
				// Toast.makeText(context, "AUDIOFOCUS_GAIN", Toast.LENGTH_LONG)
				// .show();

				// if (player == null) {
				// play();
				// }

			} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				logger.i("AUDIOFOCUS_LOSS");
				// Toast.makeText(context, "AUDIOFOCUS_LOSS", Toast.LENGTH_LONG)
				// .show();
				// audioManager.abandonAudioFocus(afChangeListener);
				//stop();
			}
			// else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
			// {
			// logger.i("AUDIOFOCUS_REQUEST_GRANTED");
			// Toast.makeText(context, "AUDIOFOCUS_REQUEST_GRANTED",
			// Toast.LENGTH_LONG).show();
			// play();
			//
			// }
			else {
				// Toast.makeText(context, "focusChange:" + focusChange,
				// Toast.LENGTH_LONG).show();
				logger.i("focusChange:" + focusChange);
			}
		}
	};

	private class PlayerRunable implements Runnable {

		private boolean isRegister = false;
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(100);

					if (player != null && player.isPlaying()) {

						if (songBean != null) {
							songBean.setPlayProgress(player
									.getCurrentPosition());

							// 没有订阅此事件时，不发送事件
							if(!isRegister){
								if(EventBus.getDefault().hasSubscriberForEvent(ProgressMessage.class)){
									isRegister = true;
								}else{
									continue;
								}
							}
							ProgressMessage.getInstance().setSongBean(songBean);
							EventBus.getDefault().post(ProgressMessage.getInstance());
						}
					}
				} catch (InterruptedException e) {
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					logger.e(sw.toString());
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		logger.i("------MediaPlayerService被回收了------");
		isServiceRunning = false;
		audioManager.abandonAudioFocus(afChangeListener);
		if (player != null) {
			if (player.isPlaying()) {
				player.stop();
				this.STATUS = STOPED;
				if (songBean != null) {
					songMessage = new SongMessage(SongMessage.STOPING);
					songMessage.setSongBean(songBean);
					EventBus.getDefault().post(songMessage);
				}
			}
			player.reset();
			player.release();
			player = null;
		}
		EventBus.getDefault().unregister(this);
		super.onDestroy();
		logger.i("status:-->" + this.STATUS);
		logger.i("BaseConstants.APPCLOSE:-->" + BaseConstants.APPCLOSE);
		// 如果当前的状态不是暂停，如果播放服务被回收了，要重新启动服务
		if (!BaseConstants.APPCLOSE && this.STATUS != PLAYING) {
			// 在此重新启动,使服务常驻内存
			startService(new Intent(this, MediaPlayerService.class));
		}
	}

/*	public void stop() {
		if (player != null) {
			if (player.isPlaying()) {
				player.stop();
				if (songBean != null) {
					songMessage = new SongMessage(SongMessage.STOPING);
					songMessage.setSongBean(songBean);
					EventBus.getDefault().post(songMessage);
				}
			}
			player.reset();
			player.release();
			player = null;
			this.STATUS = STOPED;
		}
	}*/

	public void onEventBackgroundThread(PlayServiceMessage serviceMessage){
		switch (serviceMessage.getType()){
			case PlayServiceMessage.SEEKTO:
				player.seekTo(serviceMessage.getSongBean().getPlayProgress());
				break;
			case PlayServiceMessage.PAUSE:
				if(this.STATUS == PLAYING){
					player.pause();
					this.STATUS = PAUSING;

					songMessage.setType(SongMessage.PAUSE_UI);
					EventBus.getDefault().post(songMessage);
				}
				break;
			case PlayServiceMessage.TOPLAY:
				play();
				break;
			case PlayServiceMessage.PLAY:
				if(this.STATUS == PAUSING){
					player.start();
					this.STATUS = PLAYING;

					songMessage.setType(SongMessage.PLAY_UI);
					EventBus.getDefault().post(songMessage);
				}
				break;
		}
	}
}
