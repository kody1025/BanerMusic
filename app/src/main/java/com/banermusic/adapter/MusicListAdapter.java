package com.banermusic.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.banermusic.R;
import com.banermusic.bean.SongBean;
import com.banermusic.manage.MediaManage;
import com.banermusic.ui.MorePopupWindow;

import java.util.List;

/**
 * Created by kodywu on 28/9/15.
 */
public class MusicListAdapter extends BaseAdapter {

    private List<SongBean> songBeans;
    private Context context;
    private MorePopupWindow mPopupWindow;

    public MusicListAdapter(Context context, List<SongBean> songBeans){
        this.context = context;
        this.songBeans = songBeans;
    }

    @Override
    public int getCount() {
        return songBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return songBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return songBeans.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.fragment_musiclist_list_item, null);
            viewHolder = new ViewHolder(convertView);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        final SongBean songBean = songBeans.get(position);
        viewHolder.txSongTitle.setText(songBean.getSong_name());
        viewHolder.ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // popupwindow
                showPopupWindow();
            }
        });

        if(MediaManage.getMediaManage(context).getPlaySongInfo() != null && songBean.getId() == MediaManage.getMediaManage(context).getPlaySongInfo().getId()){
            if(MediaManage.isPlaying()){
                viewHolder.ivPlayFlag.setBackgroundResource(R.drawable.music_list_stop_button);
            }else{
                viewHolder.ivPlayFlag.setBackgroundResource(R.drawable.music_list_play_button);
            }
        }else{
            viewHolder.ivPlayFlag.setBackgroundColor(Color.TRANSPARENT);
        }

        convertView.setTag(viewHolder);
        return convertView;
    }

    class ViewHolder{
        ImageView ivPlayFlag;
        TextView txSongTitle;
        ImageView ivMore;

        public ViewHolder(View view){
            ivPlayFlag = (ImageView) view.findViewById(R.id.musicList_item_playFlag);
            txSongTitle = (TextView) view.findViewById(R.id.musicList_item_songTitle);
            ivMore = (ImageView) view.findViewById(R.id.musicList_item_more);
        }

    }

    public void showPopupWindow(){
        if(mPopupWindow == null){
            mPopupWindow = new MorePopupWindow(context, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()){
                        case R.id.tvDownload:
                            Log.i("aa","click");
                            break;
                    }
                }
            });
        }

        mPopupWindow.showAtLocation(LayoutInflater.from(context).inflate(R.layout.fragment_musiclist_list, null), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }
}
