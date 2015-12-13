package com.banermusic.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.banermusic.R;
import com.banermusic.constant.BaseConstants;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends FragmentActivity implements ViewPager.OnPageChangeListener {

    private ViewPager viewPager;

    private Fragment firstFragment;
    private Fragment secondFragment;
    private Fragment thirdFragment;
    private Fragment fourthFragment;
    //private Fragment fifthFragment;

    private List<Fragment> fragmentList;

    private MyFragmentPagerAdapter pagerAdapter;

    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        firstFragment = GuideFragment.newInstance(R.drawable.guide1);
        secondFragment = GuideFragment.newInstance(R.drawable.guide2);
        thirdFragment = GuideFragment.newInstance(R.drawable.guide3);
        fourthFragment = GuideFragment.newInstance(R.drawable.guide4);

        fragmentList = new ArrayList<Fragment>();
        fragmentList.add(firstFragment);
        fragmentList.add(secondFragment);
        fragmentList.add(thirdFragment);
        fragmentList.add(fourthFragment);

        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager)findViewById(R.id.vp_guide);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        currentPosition = position;
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        /*if(currentPosition == fragmentList.size() - 1 && state == ViewPager.SCROLL_STATE_DRAGGING){
            startActivity(new Intent(GuideActivity.this,MainActivity.class));
            finish();
        }*/
    }

    private float startX;
    private float endX;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean result = super.dispatchTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                //Log.i("startX:",String.valueOf(startX));
                break;
            case MotionEvent.ACTION_UP:
                endX = event.getX();
                if(startX - endX > 50f && currentPosition == fragmentList.size() - 1){
                    startActivity(new Intent(GuideActivity.this,MainActivity.class));
                    finish();
                }
                //Log.i("endX:",String.valueOf(endX));
                break;
        }
        return result;
    }



    private class MyFragmentPagerAdapter extends FragmentPagerAdapter{

        public MyFragmentPagerAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }
}
