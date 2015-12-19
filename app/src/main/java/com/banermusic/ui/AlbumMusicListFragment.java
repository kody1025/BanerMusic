package com.banermusic.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.android.volley.VolleyError;
import com.astuetz.PagerSlidingTabStrip;
import com.banermusic.R;
import com.banermusic.apis.CommonPost;
import com.banermusic.apis.RequestListener;
import com.banermusic.bean.AlbumBean;
import com.banermusic.bean.SceneBean;
import com.banermusic.event.FragmentMessage;
import com.banermusic.event.SongMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;

public class AlbumMusicListFragment extends Fragment {

    private List<Fragment> fragmentList;

    private SceneBean scene;

    private AlbumMusicListFragmentPagerAdapter pagerAdapter;

    private ViewPager pager;

    private PagerSlidingTabStrip tabs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentList = new ArrayList<>();
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_musiclist, container, false);

        //tabs = (PagerSlidingTabStrip) view.findViewById(R.id.tab_album_musiclist);

        pager = (ViewPager) view.findViewById(R.id.vp_musiclist);

        pagerAdapter = new AlbumMusicListFragmentPagerAdapter(getChildFragmentManager());

        return view;
    }


    public class AlbumMusicListFragmentPagerAdapter extends FragmentPagerAdapter {

        public AlbumMusicListFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
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

    public void onEventMainThread(FragmentMessage fragmentMessage) {
        if (fragmentMessage.getType() == FragmentMessage.DATAMESSAGE && fragmentMessage.getData() instanceof SceneBean) {
            scene = (SceneBean) fragmentMessage.getData();

            CommonPost.currentAlbum(getContext(), scene.getId(), new RequestListener() {
                @Override
                public void onResponse(String response) {
                    final AlbumBean currentAlbumBean = JSON.parseObject(response, AlbumBean.class);
                    if (currentAlbumBean != null && !currentAlbumBean.getRet_code().equals("0")) {
                        SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                        songMessage.setErrorMessage(currentAlbumBean.getRet_msg());
                        EventBus.getDefault().post(songMessage);
                        return;
                    }

                    CommonPost.listAlbum(getContext(), scene.getId(), new RequestListener() {
                        @Override
                        public void onResponse(String response) {
                            AlbumBean albumBean = JSON.parseObject(response, AlbumBean.class);
                            if (albumBean != null && !albumBean.getRet_code().equals("0")) {
                                SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                                songMessage.setErrorMessage(albumBean.getRet_msg());
                                EventBus.getDefault().post(songMessage);
                                return;
                            }


                            List<AlbumBean> albumBeans = albumBean.getResult_list();

                            // 添加推荐专辑
                            fragmentList.add(MusicListFragment.NewInstance(currentAlbumBean.getResult()));

                            // 添加其他专辑
                            if(albumBeans != null){
                                Iterator<AlbumBean> iterator = albumBeans.iterator();
                                while (iterator.hasNext()){
                                    AlbumBean bean = iterator.next();
                                    if(bean.getId() != currentAlbumBean.getResult().getId()){
                                        fragmentList.add(MusicListFragment.NewInstance(bean));
                                    }
                                }
                            }
                            pager.setAdapter(pagerAdapter);

                            // Bind the tabs to the ViewPager
                            //tabs.setViewPager(pager);

                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                            songMessage.setErrorMessage(getResources().getText(R.string.service_error).toString());
                            EventBus.getDefault().post(songMessage);
                        }
                    });

                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                    songMessage.setErrorMessage(getResources().getText(R.string.service_error).toString());
                    EventBus.getDefault().post(songMessage);
                }
            });
        }else if(fragmentMessage.getType() == FragmentMessage.DATAMESSAGE && fragmentMessage.getData() instanceof AlbumBean){

            final AlbumBean albumBean = (AlbumBean) fragmentMessage.getData();
            fragmentList.add(MusicListFragment.NewInstance(albumBean));
            pager.setAdapter(pagerAdapter);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
