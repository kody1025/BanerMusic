package com.banermusic.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.banermusic.R;
import com.banermusic.adapter.SceneAdapter;
import com.banermusic.apis.CommonPost;
import com.banermusic.apis.RequestListener;
import com.banermusic.bean.AlbumBean;
import com.banermusic.bean.BannerBean;
import com.banermusic.bean.SceneBean;
import com.banermusic.bean.SongBean;
import com.banermusic.event.FragmentMessage;
import com.banermusic.event.PlayerMessage;
import com.banermusic.event.SongMessage;
import com.banermusic.manage.MediaManage;
import com.banermusic.util.LoadImage;
import com.banermusic.util.NetContext;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import in.srain.cube.views.GridViewWithHeaderAndFooter;


public class SceneFragment extends Fragment {

    private List<SceneBean> sceneBeanList;
    private SceneAdapter sceneAdapter;
    //private GridView gvScene;
    private GridViewWithHeaderAndFooter gvScene;
    Button button;

    private Context context;
    private SliderLayout mSliderLayout;

    private ImageLoader mImageLoader;

    public SceneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        this.context = getContext();

        RequestQueue mQueue = NetContext.getInstance(context).getInstance(context).getJsonRequestQueue();

        mImageLoader = new ImageLoader(mQueue, LoadImage.loadImageByVolley(context));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_scene, container, false);

        mSliderLayout = (SliderLayout)view.findViewById(R.id.slider);
        mSliderLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mSliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSliderLayout.setCustomAnimation(new DescriptionAnimation());
        mSliderLayout.setDuration(4000);
        mSliderLayout.setPresetTransformer(SliderLayout.Transformer.Default);

        CommonPost.listBanner(context, new RequestListener() {
            @Override
            public void onResponse(String response) {
                BannerBean banners = JSON.parseObject(response, BannerBean.class);
                if (banners != null && !banners.getRet_code().equals("0")) {
                    SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                    songMessage.setErrorMessage(banners.getRet_msg());
                    EventBus.getDefault().post(songMessage);
                    return;
                }

                for (BannerBean banner : banners.getResult_list()) {
                    if (banner.getState() == 1) {
                        DefaultSliderView sliderView = new DefaultSliderView(context);
                        sliderView.image(banner.getBanner_icon()).setScaleType(BaseSliderView.ScaleType.Fit);
                        sliderView.setOnSliderClickListener(new SliderClickListener(banner));
                        mSliderLayout.addSlider(sliderView);
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

        //gvScene = (GridView)view.findViewById(R.id.gvScene);
        gvScene = (GridViewWithHeaderAndFooter)view.findViewById(R.id.gvScene);

        CommonPost.listScene(context, new RequestListener() {
            @Override
            public void onResponse(String response) {
                SceneBean scence = JSON.parseObject(response, SceneBean.class);
                if (scence != null && !scence.getRet_code().equals("0")) {
                    SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                    songMessage.setErrorMessage(scence.getRet_msg());
                    EventBus.getDefault().post(songMessage);
                    return;
                }
                sceneBeanList = scence.getResult_list();

                // 添加最近播放按钮
                SceneBean sceneBean = new SceneBean();
                sceneBean.setId(-1);
                sceneBeanList.add(0, sceneBean);

                sceneAdapter = new SceneAdapter(context, sceneBeanList);
                View view = new View(context);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
                view.setLayoutParams(layoutParams);
                gvScene.addFooterView(view);
                gvScene.setAdapter(sceneAdapter);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                SongMessage songMessage = new SongMessage(SongMessage.ERROR);
                songMessage.setErrorMessage(getResources().getText(R.string.service_error).toString());
                EventBus.getDefault().post(songMessage);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("SceneList");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("SceneList");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(SongMessage songMessage){

    }

    private class SliderClickListener implements BaseSliderView.OnSliderClickListener{

        private BannerBean banner;

        public SliderClickListener(BannerBean banner){
            this.banner = banner;
        }

        @Override
        public void onSliderClick(BaseSliderView baseSliderView) {
            switch (banner.getBanner_type()){
                case 1:
                    FragmentMessage fragmentMessage = new FragmentMessage(FragmentMessage.SCENETOMUSICLIST);
                    AlbumBean albumBean = banner.getAlbum_dto();

                    fragmentMessage.setData(albumBean);
                    EventBus.getDefault().post(fragmentMessage);
                    break;
                case 2:
                    MediaManage mediaManage = MediaManage.getMediaManage(context); banner.getSong_id();
                    mediaManage.setDataSource(new ArrayList<SongBean>(){
                        {
                            add(new SongBean());
                        }
                    }, -2, banner.getSong_id());
                    break;
                case 3:
                    break;
            }
        }
    }
}
