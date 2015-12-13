package com.banermusic.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.banermusic.R;
import com.banermusic.constant.BaseConstants;
import com.banermusic.db.SongDB;
import com.banermusic.util.ImageUtil;

public class WellcomeActivity extends Activity implements Runnable {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellcome);

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


        // 初始化屏幕大小
        this.initWindowSize();

        // 初始化图片
        ImageView imageView = (ImageView)findViewById(R.id.iv_wellcome);
        Bitmap bitmap = ImageUtil.createBitmap(this, R.drawable.wellcome);
        imageView.setImageBitmap(bitmap);

        new Thread(this).start();
    }

    @Override
    public void run() {
        // 初始化最近播放记录
        SongDB.initInstance(this);

        // 初始化 UDID
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
        BaseConstants.initUDID(tm.getDeviceId(), wm.getConnectionInfo().getMacAddress());

        try {
            Thread.sleep(2000);

            SharedPreferences sp = getSharedPreferences("setting",MODE_PRIVATE);
            if(sp.getBoolean("firstTime",true)){

                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("firstTime",false);
                editor.commit();

                startActivity(new Intent(WellcomeActivity.this,GuideActivity.class));
            }else{
                startActivity(new Intent(WellcomeActivity.this,MainActivity.class));
            }

            finish();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initWindowSize(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        BaseConstants.widthPixels = dm.widthPixels;
        BaseConstants.heightPixels = dm.heightPixels;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return false;
    }
}
