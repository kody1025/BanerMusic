package com.banermusic.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.banermusic.R;
import com.banermusic.adapter.AlbumAdapter;
import com.banermusic.adapter.MusicListAdapter;
import com.banermusic.apis.CommonPost;
import com.banermusic.apis.RequestListener;
import com.banermusic.bean.AlbumBean;
import com.banermusic.bean.SceneBean;
import com.banermusic.bean.SongBean;
import com.banermusic.constant.BaseConstants;
import com.banermusic.event.FragmentMessage;
import com.banermusic.event.PlayerMessage;
import com.banermusic.event.SongMessage;
import com.banermusic.manage.MediaManage;
import com.banermusic.util.LoadImage;
import com.banermusic.util.NetContext;
import com.banermusic.widget.MyGridView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.greenrobot.event.EventBus;

public class MusicListFragment extends Fragment implements AbsListView.OnItemClickListener,View.OnClickListener {

    private Context context;
    private ImageLoader mImageLoader;

    private ImageView ivBack;

    private SceneBean scene;
    private List<AlbumBean> albumBeans;
    private List<SongBean> songBeanList;

    private MyGridView gvAlbum;
    private AlbumAdapter albumAdapter;
    private MediaManage mediaManage;

    private ImageView ivAlbumnImage;
    private TextView tvAlbumnDesc;
    private TextView tvSongCount;
    private CharSequence songCountText;

    private LinearLayout albumsLayout;
    private LinearLayout musicListHeaderLayout;
    private View musicListRootView;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private BaseAdapter mAdapter;

    private ImageView lastPlayFlagImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        context = getContext();
        mediaManage = mediaManage.getMediaManage(context);

        RequestQueue mQueue = NetContext.getInstance(context).getInstance(context).getJsonRequestQueue();

        mImageLoader = new ImageLoader(mQueue, LoadImage.loadImageByVolley(context));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_musiclist_list, container, false);

        ivBack = (ImageView) view.findViewById(R.id.musicList_ivBack);
        ivBack.setOnClickListener(this);

        mListView = (ListView) view.findViewById(R.id.musicList_listView);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        albumsLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.fragment_album_gridview, null);
        gvAlbum = (MyGridView) albumsLayout.findViewById(R.id.gvAlbum);
        gvAlbum.setHaveScrollbar(false);

        musicListHeaderLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.musiclist_header, null);
        ivAlbumnImage = (ImageView) musicListHeaderLayout.findViewById(R.id.musicList_ivAlbumnImage);
        tvAlbumnDesc = (TextView) musicListHeaderLayout.findViewById(R.id.musicList_tvAlbumnDesc);
        tvSongCount = (TextView) musicListHeaderLayout.findViewById(R.id.musicList_tvSongCount);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position == 0 || position > songBeanList.size()){
            return;
        }
        SongBean songBean = songBeanList.get(position - 1);

        if(mediaManage.getPlaySongInfo() != null && mediaManage.getPlaySongInfo().getId() == songBean.getId()){
            if(MediaManage.isPlaying()){
                view.findViewById(R.id.musicList_item_playFlag).setBackgroundResource(R.drawable.music_list_play_button);
                EventBus.getDefault().post(new SongMessage(SongMessage.PAUSE));
            }else{
                view.findViewById(R.id.musicList_item_playFlag).setBackgroundResource(R.drawable.music_list_stop_button);
                EventBus.getDefault().post(new SongMessage(SongMessage.PLAY));
            }
        }else{
            //view.findViewById(R.id.musicList_item_playFlag).setBackgroundResource(R.drawable.music_list_stop_button);
            mediaManage.setDataSource(songBeanList, songBean.getAlbum_id(), songBean.getId());
            EventBus.getDefault().post(new SongMessage(SongMessage.SELECTPLAY));

            // 恢复上次选中行状态
            /*if(lastPlayFlagImageView != null){
                lastPlayFlagImageView.setBackgroundResource(R.drawable.musiclist_init_selector);
                lastPlayFlagImageView = (ImageView)view.findViewById(R.id.musicList_item_playFlag);
            }else{
                lastPlayFlagImageView = (ImageView)view.findViewById(R.id.musicList_item_playFlag);
            }*/

        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(SongMessage songMessage){
        switch (songMessage.getType()){
            case SongMessage.PLAY_UI:
                mAdapter.notifyDataSetChanged();
                break;
            case SongMessage.PAUSE_UI:
                mAdapter.notifyDataSetChanged();
                break;
        }
    }

    public void onEventMainThread(FragmentMessage fragmentMessage){
       if(fragmentMessage.getType() == FragmentMessage.DATAMESSAGE && fragmentMessage.getData() instanceof SceneBean){

           scene = (SceneBean)fragmentMessage.getData();

           CommonPost.currentAlbum(context, scene.getId(), new RequestListener() {
               @Override
               public void onResponse(String response) {
                   final AlbumBean albumBean = JSON.parseObject(response, AlbumBean.class);
                   if (albumBean != null && !albumBean.getRet_code().equals("0")) {
                       SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                       songMessage.setErrorMessage(albumBean.getRet_msg());
                       EventBus.getDefault().post(songMessage);
                       return;
                   }


                   CommonPost.albumSong(context, albumBean.getResult().getId(), new RequestListener() {
                       @Override
                       public void onResponse(String response) {
                           SongBean songBean = JSON.parseObject(response, SongBean.class);
                           if (songBean != null && !songBean.getRet_code().equals("0")) {
                               SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                               songMessage.setErrorMessage(songBean.getRet_msg());
                               EventBus.getDefault().post(songMessage);
                               return;
                           }

                           songBeanList = songBean.getResult_list();
                           if (songBeanList != null && songBeanList.size() > 0) {

                               ImageLoader.ImageListener listener = ImageLoader.getImageListener(ivAlbumnImage,R.drawable.home_loading,R.drawable.home_loading);
                               mImageLoader.get(albumBean.getResult().getAlbum_icon(), listener);
                               tvAlbumnDesc.setText(albumBean.getResult().getAlbum_desc());
                               if(songCountText == null){
                                   songCountText = getResources().getText(R.string.musiclist_header_song_count);
                               }
                               tvSongCount.setText(String.valueOf(songBeanList.size()) + songCountText);
                               mListView.addHeaderView(musicListHeaderLayout);

                               mAdapter = new MusicListAdapter(context, songBeanList);
                               mListView.setAdapter(mAdapter);

                               if (!MediaManage.isPlaying()) {
                                   int index = new Random().nextInt(songBeanList.size());

                                   SongBean songBean1 = songBeanList.get(index);
                                   mediaManage.setDataSource(songBeanList, songBean1.getAlbum_id(), songBean1.getId());
                                   EventBus.getDefault().post(new SongMessage(SongMessage.SELECTPLAY));
                               }
                           }
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

           CommonPost.listAlbum(context, scene.getId(), new RequestListener() {
               @Override
               public void onResponse(String response) {
                   AlbumBean albumBean = JSON.parseObject(response, AlbumBean.class);
                   if (albumBean != null && !albumBean.getRet_code().equals("0")) {
                       SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                       songMessage.setErrorMessage(albumBean.getRet_msg());
                       EventBus.getDefault().post(songMessage);
                       return;
                   }

                   albumBeans = albumBean.getResult_list();
                   if(albumAdapter == null){
                       albumAdapter = new AlbumAdapter(context, albumBeans);
                       gvAlbum.setAdapter(albumAdapter);

                       if(mListView.getFooterViewsCount() == 0){
                           View view = new View(context);
                           ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,200);
                           view.setLayoutParams(layoutParams);
                           albumsLayout.addView(view);
                           mListView.addFooterView(albumsLayout);
                       }
                   }else{
                       albumAdapter.notifyDataSetChanged();
                   }

               }

               @Override
               public void onErrorResponse(VolleyError error) {
                   SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                   songMessage.setErrorMessage(getResources().getText(R.string.service_error).toString());
                   EventBus.getDefault().post(songMessage);
               }
           });
       }else if (fragmentMessage.getType() == FragmentMessage.REFRESH && fragmentMessage.getData() instanceof AlbumBean){

           final AlbumBean albumBean = (AlbumBean) fragmentMessage.getData();
           CommonPost.albumSong(getContext(), albumBean.getId(), new RequestListener() {
               @Override
               public void onResponse(String response) {
                   SongBean songBean = JSON.parseObject(response, SongBean.class);
                   if (songBean != null && !songBean.getRet_code().equals("0")) {
                       SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                       songMessage.setErrorMessage(songBean.getRet_msg());
                       EventBus.getDefault().post(songMessage);
                       return;
                   }

                   songBeanList = songBean.getResult_list();
                   if (songBeanList != null && songBeanList.size() > 0) {

                       String album_icon = albumBean.getAlbum_icon() == null ?
                               songBeanList.get(0).getAlbum_icon() : albumBean.getAlbum_icon();
                       String album_desc = albumBean.getAlbum_desc() == null ?
                               songBeanList.get(0).getAlbum_desc() : albumBean.getAlbum_desc();

                       ImageLoader.ImageListener listener = ImageLoader.getImageListener(ivAlbumnImage,R.drawable.home_loading,R.drawable.home_loading);
                       mImageLoader.get(album_icon, listener);
                       tvAlbumnDesc.setText(album_desc);
                       if(songCountText == null){
                           songCountText = getResources().getText(R.string.musiclist_header_song_count);
                       }
                       tvSongCount.setText(String.valueOf(songBeanList.size())+songCountText);

                       mAdapter = new MusicListAdapter(getContext(), songBeanList);
                       mListView.setAdapter(mAdapter);

                       if (!MediaManage.isPlaying()) {
                           int index = new Random().nextInt(songBeanList.size());
                           SongBean songBean1 = songBeanList.get(index);

                           mediaManage.setDataSource(songBeanList, songBean1.getAlbum_id(), songBean1.getId());
                           EventBus.getDefault().post(new SongMessage(SongMessage.SELECTPLAY));
                       }
                   }
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
           CommonPost.albumSong(context, albumBean.getId(), new RequestListener() {
               @Override
               public void onResponse(String response) {
                   SongBean songBean = JSON.parseObject(response, SongBean.class);
                   if (songBean != null && !songBean.getRet_code().equals("0")) {
                       SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                       songMessage.setErrorMessage(songBean.getRet_msg());
                       EventBus.getDefault().post(songMessage);
                       return;
                   }

                   songBeanList = songBean.getResult_list();
                   if (songBeanList != null && songBeanList.size() > 0) {

                       String album_icon = albumBean.getAlbum_icon() == null ?
                               songBeanList.get(0).getAlbum_icon() : albumBean.getAlbum_icon();
                       String album_desc = albumBean.getAlbum_desc() == null ?
                               songBeanList.get(0).getAlbum_desc() : albumBean.getAlbum_desc();

                       ImageLoader.ImageListener listener = ImageLoader.getImageListener(ivAlbumnImage,R.drawable.home_loading,R.drawable.home_loading);
                       mImageLoader.get(album_icon, listener);
                       tvAlbumnDesc.setText(album_desc);
                       if(songCountText == null){
                           songCountText = getResources().getText(R.string.musiclist_header_song_count);
                       }
                       tvSongCount.setText(String.valueOf(songBeanList.size())+songCountText);
                       mListView.addHeaderView(musicListHeaderLayout);

                       mAdapter = new MusicListAdapter(context, songBeanList);
                       mListView.setAdapter(mAdapter);

                       View view = new View(context);
                       AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,200);
                       view.setLayoutParams(layoutParams);
                       mListView.addFooterView(view);

                       if (!MediaManage.isPlaying()) {
                           int index = new Random().nextInt(songBeanList.size());
                           SongBean songBean1 = songBeanList.get(index);
                           mediaManage.setDataSource(songBeanList, songBean1.getAlbum_id(), songBean1.getId());
                           EventBus.getDefault().post(new SongMessage(SongMessage.SELECTPLAY));
                       }
                   }
               }

               @Override
               public void onErrorResponse(VolleyError error) {
                   SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                   songMessage.setErrorMessage(getResources().getText(R.string.service_error).toString());
                   EventBus.getDefault().post(songMessage);
               }
           });


       }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("MusicList");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("MusicList");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.musicList_ivBack:
                getFragmentManager().popBackStack();
                break;
        }
    }
}
