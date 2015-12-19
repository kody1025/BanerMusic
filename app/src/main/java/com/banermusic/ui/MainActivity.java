package com.banermusic.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.autoupdatesdk.AppUpdateInfo;
import com.baidu.autoupdatesdk.AppUpdateInfoForInstall;
import com.baidu.autoupdatesdk.BDAutoUpdateSDK;
import com.baidu.autoupdatesdk.CPCheckUpdateCallback;
import com.baidu.autoupdatesdk.CPUpdateDownloadCallback;
import com.baidu.autoupdatesdk.UICheckUpdateCallback;
import com.banermusic.R;
import com.banermusic.bean.SceneBean;
import com.banermusic.bean.SongBean;
import com.banermusic.constant.BaseConstants;
import com.banermusic.db.SongDB;
import com.banermusic.event.FragmentMessage;
import com.banermusic.event.PlayerMessage;
import com.banermusic.event.SongDBMessage;
import com.banermusic.event.SongMessage;
import com.banermusic.manage.MediaManage;
import com.banermusic.receiver.PhoneReceiver;
import com.banermusic.services.MediaPlayerService;
import com.banermusic.util.DialogUtil;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private FragmentManager manager;
    private MediaManage mediaManage;
    private Long lastBackTime = null;
    private Fragment currentFragment;

    private ImageView ivSwitch;

    private AudioManager mAudioManager;
    private SceneBean selectedScene;

    private VolumnReceiver volumnReceiver;

    private PlayerPopupWindow playerPopupWindow;

    // 百度更新loading
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSystemBar(this);
        setContentView(R.layout.activity_main);

        // 设置状态栏透明
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        EventBus.getDefault().register(this);

        // 初始化第一个Fragment
        manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = new SceneFragment();
        transaction.add(R.id.frameLayout, fragment, SceneFragment.class.getSimpleName());
        transaction.commit();
        currentFragment = fragment;

        ivSwitch = (ImageView) findViewById(R.id.ivSwitch);
        ivSwitch.setOnClickListener(this);

        // 启动播放服务
        startService(new Intent(MainActivity.this, MediaPlayerService.class));

        ComponentName name = new ComponentName(this.getPackageName(),
                PhoneReceiver.class.getName());
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.registerMediaButtonEventReceiver(name);

        // 接收系统音乐声音改变广播
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUMN_CHANGED_ACTION");
        volumnReceiver = new VolumnReceiver();
        registerReceiver(volumnReceiver,filter);

        // 百度自动更新
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.show();
        BDAutoUpdateSDK.uiUpdateAction(this, new MyUICheckUpdateCallback());
    }

    private class VolumnReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("android.media.VOLUMN_CHANGED_ACTION")){
                int volumn = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                PlayerMessage.VOLUMN_VALUE = volumn;
                EventBus.getDefault().post(PlayerMessage.getInstance(PlayerMessage.VOLUMN_DISPLAY_MSG));
            }
        }
    }

    public void showOrHidePopupWindow(){
        if(playerPopupWindow == null){
            playerPopupWindow = new PlayerPopupWindow(this, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOrHidePopupWindow();
                }
            });
        }

        if(playerPopupWindow.isShowing()){
            playerPopupWindow.dismiss();
        }else{
            playerPopupWindow.showAtLocation(findViewById(R.id.frameLayout), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseConstants.APPCLOSE = true;
        if(playerPopupWindow != null){
            EventBus.getDefault().unregister(playerPopupWindow);
        }
        EventBus.getDefault().unregister(this);
        stopService(new Intent(MainActivity.this, MediaPlayerService.class));
        unregisterReceiver(volumnReceiver);
    }

    public void onEventMainThread(FragmentMessage fragmentMessage) {
        switch (fragmentMessage.getType()) {
            case FragmentMessage.SCENETOMUSICLIST:
                //switchFragment(SceneFragment.class, MusicListFragment.class);
                switchFragment(SceneFragment.class, AlbumMusicListFragment.class);
                if (manager.executePendingTransactions() && fragmentMessage.getData() != null) {
                    fragmentMessage.setType(FragmentMessage.DATAMESSAGE);
                    EventBus.getDefault().post(fragmentMessage);
                }
                break;
            case FragmentMessage.HOMETOPLAYER:
                switchFragment(SceneFragment.class, PlayerFragment.class);
                break;
            case FragmentMessage.SCENETOCURRENTLIST:
                switchFragment(SceneFragment.class, MyMusicFragment.class);
                break;
        }
    }

    public void onEventMainThread(SongMessage songMessage) {
        switch (songMessage.getType()) {
            case SongMessage.ERROR:
                Toast.makeText(this, songMessage.getErrorMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void onEventBackgroundThread(SongDBMessage songDBMessage){
        switch (songDBMessage.getType()){
            case SongDBMessage.SONGDB_ADD_MSG:
                SongDB.getInstance().addCurrentSong(songDBMessage.getSongBean());
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public void switchFragment(Class<? extends Fragment> fromFragmentClass, Class<? extends Fragment> toFragmentClass) {
        String fromFragmentTag = fromFragmentClass.getSimpleName();
        Fragment fromFragment = manager.findFragmentByTag(fromFragmentTag);

        //查找切换的Fragment
        String toFragmentTag = toFragmentClass.getSimpleName();
        Fragment toFragment = manager.findFragmentByTag(toFragmentTag);

        //如果要切换到的Fragment不存在，则创建
        if (toFragment == null) {
            try {
                toFragment = toFragmentClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        FragmentTransaction transaction = manager.beginTransaction();

        //设置Fragment切换效果
        //transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);

        // 如果要切换到的Fragment没有被Fragment事务添加，则隐藏被切换的Fragment，添加要切换的Fragment
        // 否则，则隐藏被切换的Fragment，显示要切换的Fragment
        if (!toFragment.isAdded()) {
            transaction.hide(fromFragment).add(R.id.frameLayout, toFragment, toFragmentTag).addToBackStack(null);
        } else {
            transaction.hide(fromFragment).show(toFragment);
        }

        currentFragment = toFragment;

        transaction.commit();

        Log.i("SwitchFragment","FromFlagment:"+fromFragmentTag+" ToFlagment:"+ toFragmentTag);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Long currentTime = new Date().getTime();
        if (getSupportFragmentManager().getBackStackEntryCount() == 0
                && event.KEYCODE_BACK == keyCode) {
            if (lastBackTime == null || currentTime - lastBackTime > 2000) {
                DialogUtil.showToast(this, "再按一下退出程序");
                lastBackTime = new Date().getTime();
            } else {
                finish();
            }
            return true;
        }
        /*else if(KeyEvent.KEYCODE_VOLUME_UP == keyCode || KeyEvent.KEYCODE_VOLUME_DOWN == keyCode){ // 按增加声音物理键时,按减少声音物理键时
            int volumn = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            PlayerMessage.VOLUMN_VALUE = volumn;
            EventBus.getDefault().post(PlayerMessage.getInstance(PlayerMessage.VOLUMN_DISPLAY_MSG));
        }else if(KeyEvent.KEYCODE_VOLUME_MUTE == keyCode){ // 按禁止声音物理键时
            PlayerMessage.VOLUMN_VALUE = 0;
            EventBus.getDefault().post(PlayerMessage.getInstance(PlayerMessage.VOLUMN_DISPLAY_MSG));
        }*/
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivSwitch:

                showOrHidePopupWindow();

                /*if(currentFragment.getClass().getSimpleName().equals(PlayerFragment.class.getSimpleName())){
                    switchFragment(PlayerFragment.class, SceneFragment.class);
                    //manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }else if(SceneFragment.class.getSimpleName().equals(currentFragment.getClass().getSimpleName())){
                    switchFragment(SceneFragment.class, PlayerFragment.class);
                }else if(MusicListFragment.class.getSimpleName().equals(currentFragment.getClass().getSimpleName())){
                    switchFragment(MusicListFragment.class, PlayerFragment.class);
                }*/

                break;
        }
    }

    public static void initSystemBar(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(activity, true);
        }

        SystemBarTintManager tintManager = new SystemBarTintManager(activity);

        tintManager.setStatusBarTintEnabled(true);

        // 使用颜色资源
        tintManager.setStatusBarTintResource(R.color.transparent);
    }


    @TargetApi(19)
    private static void setTranslucentStatus(Activity activity, boolean on) {

        Window win = activity.getWindow();

        WindowManager.LayoutParams winParams = win.getAttributes();

        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }

        win.setAttributes(winParams);
    }

    private class MyUICheckUpdateCallback implements UICheckUpdateCallback {

        @Override
        public void onCheckComplete() {
            dialog.dismiss();
        }

    }
}
