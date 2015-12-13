package com.banermusic.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.banermusic.R;
import com.banermusic.bean.SceneBean;
import com.banermusic.db.SongDB;
import com.banermusic.event.FragmentMessage;
import com.banermusic.event.PlayerMessage;
import com.banermusic.event.SongMessage;
import com.banermusic.util.LoadImage;
import com.banermusic.util.NetContext;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/9/29.
 */
public class SceneAdapter extends BaseAdapter {

    private List<SceneBean> sceneBeans;
    private Context context;
    private ImageLoader mImageLoader;

    private boolean fillBlank;

    public SceneAdapter(Context context, List<SceneBean> sceneBeans){
        this.context = context;
        this.sceneBeans = sceneBeans;
        this.fillBlank = false;

        RequestQueue mQueue = NetContext.getInstance(context).getInstance(context).getJsonRequestQueue();

        mImageLoader = new ImageLoader(mQueue, LoadImage.loadImageByVolley(context));
    }

    @Override
    public int getCount() {
        return sceneBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return sceneBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return sceneBeans.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.fragment_scene_gridview_item, null);
            viewHolder = new ViewHolder(convertView);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final SceneBean sceneBean = sceneBeans.get(position);
        //viewHolder.tvSceneText.setText(sceneBean.getScene_name());
        viewHolder.llSceneItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sceneBean.getId() > 0){
                    FragmentMessage fragmentMessage = new FragmentMessage(FragmentMessage.SCENETOMUSICLIST);
                    fragmentMessage.setData(sceneBean);
                    EventBus.getDefault().post(fragmentMessage);
                }else{
                    if(SongDB.getInstance().getSongList().size() > 0){
                        FragmentMessage fragmentMessage = new FragmentMessage(FragmentMessage.SCENETOCURRENTLIST);
                        EventBus.getDefault().post(fragmentMessage);
                    }else{
                        SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                        songMessage.setErrorMessage("还没有最近播放记录");
                        EventBus.getDefault().post(songMessage);
                    }

                }

            }
        });

        if(sceneBean.getId() > 0){
            ImageLoader.ImageListener listener = ImageLoader.getImageListener(viewHolder.ivSceneIcon,R.drawable.home_loading,R.drawable.home_loading);
            mImageLoader.get(sceneBean.getScene_icon(), listener);
        }else{
            viewHolder.ivSceneIcon.setImageResource(R.drawable.home_cd_w);
        }

        convertView.setTag(viewHolder);

        return convertView;
    }

    class ViewHolder{
        LinearLayout llSceneItem;
        ImageView ivSceneIcon;
        //TextView tvSceneText;

        public ViewHolder(View view){
            ivSceneIcon = (ImageView) view.findViewById(R.id.ivSceneIcon);
            //tvSceneText = (TextView) view.findViewById(R.id.tvSceneText);
            llSceneItem = (LinearLayout) view.findViewById(R.id.llSceneItem);
        }
    }
}
