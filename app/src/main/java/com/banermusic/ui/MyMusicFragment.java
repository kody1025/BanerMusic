package com.banermusic.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import com.banermusic.db.SongDB;
import com.banermusic.event.FragmentMessage;
import com.banermusic.event.PlayerMessage;
import com.banermusic.event.SongMessage;
import com.banermusic.manage.MediaManage;
import com.banermusic.util.LoadImage;
import com.banermusic.util.NetContext;
import com.banermusic.widget.MyGridView;
import com.umeng.analytics.MobclickAgent;

import java.util.List;
import java.util.Random;

import de.greenrobot.event.EventBus;

public class MyMusicFragment extends Fragment implements AbsListView.OnItemClickListener,View.OnClickListener {

    private Context context;
    private ImageLoader mImageLoader;

    private ImageView ivBack;

    private SceneBean scene;
    private List<AlbumBean> albumBeans;
    private List<SongBean> songBeanList;

    private AlbumAdapter albumAdapter;
    private MediaManage mediaManage;

    private ImageView ivAlbumnImage;
    private TextView tvAlbumnDesc;
    private TextView tvSongCount;
    private CharSequence songCountText;

    private LinearLayout musicListHeaderLayout;
    private View musicListRootView;

    private final int playListID = -1;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;


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

        musicListHeaderLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.musiclist_header, null);
        ivAlbumnImage = (ImageView) musicListHeaderLayout.findViewById(R.id.musicList_ivAlbumnImage);
        tvAlbumnDesc = (TextView) musicListHeaderLayout.findViewById(R.id.musicList_tvAlbumnDesc);
        tvSongCount = (TextView) musicListHeaderLayout.findViewById(R.id.musicList_tvSongCount);

        songBeanList = SongDB.getInstance().getSongList();

        //ImageLoader.ImageListener listener = ImageLoader.getImageListener(ivAlbumnImage, R.drawable.home_loading, R.drawable.home_loading);
        //mImageLoader.get(albumBean.getResult().getAlbum_icon(), listener);
        ivAlbumnImage.setImageResource(R.drawable.loding_pic);
        //tvAlbumnDesc.setText(albumBean.getResult().getAlbum_desc());
        tvAlbumnDesc.setText("最近播放歌曲");
        if (songCountText == null) {
            songCountText = getResources().getText(R.string.musiclist_header_song_count);
        }
        if(songBeanList != null){
            tvSongCount.setText(String.valueOf(songBeanList.size()) + songCountText);
        }else{
            tvSongCount.setText("0" + songCountText);
        }

        mListView.addHeaderView(musicListHeaderLayout);

        View footView = new View(context);
        ListView.LayoutParams layoutParams = new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,200);
        footView.setLayoutParams(layoutParams);
        mListView.addFooterView(footView);

        mAdapter = new MusicListAdapter(context, songBeanList);
        mListView.setAdapter(mAdapter);

        /*if (!MediaManage.isPlaying() && songBeanList != null && songBeanList.size() > 0) {
            // 随机播放
            int index = new Random().nextInt(songBeanList.size());
            SongBean playSongBean = songBeanList.get(index);
            // 查找是否有播放过的歌曲，如果有则继续播放
            for(SongBean songBean : songBeanList){
                if(songBean.getPlayProgress() > 0){
                    playSongBean = songBean;
                    break;
                }
            }
            mediaManage.setDataSource(songBeanList, playListID, playSongBean.getId());
            EventBus.getDefault().post(new SongMessage(SongMessage.PLAYORSTOPMUSIC));
        }*/

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position == 0){
            return;
        }
        SongBean songBean = songBeanList.get(position - 1);
        mediaManage.setDataSource(songBeanList, playListID, songBean.getId());
        EventBus.getDefault().post(new SongMessage(SongMessage.SELECTPLAY));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(FragmentMessage fragmentMessage){
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
