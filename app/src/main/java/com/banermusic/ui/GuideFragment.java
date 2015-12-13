package com.banermusic.ui;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.banermusic.R;
import com.banermusic.constant.BaseConstants;
import com.banermusic.util.ImageUtil;

public class GuideFragment extends Fragment {

    private static final String ARG_RESOURCEID = "resourceId";

    private int imageResource;

    public GuideFragment() {
        // Required empty public constructor
    }

    public static GuideFragment newInstance(int resourceId) {
        GuideFragment fragment = new GuideFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_RESOURCEID, resourceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageResource = getArguments().getInt(ARG_RESOURCEID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_guide, container, false);

        ImageView ivGuide = (ImageView)view.findViewById(R.id.iv_guide);

        Bitmap scaleBitmap = ImageUtil.createBitmap(getContext(), imageResource);

        ivGuide.setImageBitmap(scaleBitmap);

        /*Log.i("height", String.valueOf(scaleBitmap.getHeight()));
        Log.i("width", String.valueOf(scaleBitmap.getWidth()));*/

        return view;
    }

}
