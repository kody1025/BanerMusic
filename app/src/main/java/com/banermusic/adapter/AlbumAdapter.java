package com.banermusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.banermusic.R;
import com.banermusic.bean.AlbumBean;
import com.banermusic.bean.SceneBean;
import com.banermusic.event.FragmentMessage;
import com.banermusic.util.LoadImage;
import com.banermusic.util.NetContext;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/9/29.
 */
public class AlbumAdapter extends BaseAdapter {

    private List<AlbumBean> albumBeans;
    private Context context;
    private ImageLoader mImageLoader;

    public AlbumAdapter(Context context, List<AlbumBean> albumBeans){
        this.context = context;
        this.albumBeans = albumBeans;

        RequestQueue mQueue = NetContext.getInstance(context).getInstance(context).getJsonRequestQueue();

        mImageLoader = new ImageLoader(mQueue, LoadImage.loadImageByVolley(context));
    }

    public void setDataSource(List<AlbumBean> albumBeans){
        this.albumBeans = albumBeans;
    }

    @Override
    public int getCount() {
        return albumBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return albumBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return albumBeans.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.fragment_album_gridview_item, null);
            viewHolder = new ViewHolder(convertView);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final AlbumBean albumBean = albumBeans.get(position);
        viewHolder.tvAlbumText.setText(albumBean.getAlbum_name());
        viewHolder.llAlbumItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentMessage fragmentMessage = new FragmentMessage(FragmentMessage.REFRESH);
                fragmentMessage.setData(albumBean);
                EventBus.getDefault().post(fragmentMessage);
            }
        });

        ImageLoader.ImageListener listener = ImageLoader.getImageListener(viewHolder.ivAlbumIcon,R.drawable.home_loading,R.drawable.home_loading);
        mImageLoader.get(albumBean.getAlbum_icon(),listener);

        convertView.setTag(viewHolder);
        return convertView;
    }

    class ViewHolder{
        LinearLayout llAlbumItem;
        ImageView ivAlbumIcon;
        TextView tvAlbumText;

        public ViewHolder(View view){
            ivAlbumIcon = (ImageView) view.findViewById(R.id.ivAlbumIcon);
            tvAlbumText = (TextView) view.findViewById(R.id.tvAlbumText);
            llAlbumItem = (LinearLayout) view.findViewById(R.id.llAlbumItem);
        }
    }
}
