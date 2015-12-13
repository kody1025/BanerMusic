package com.banermusic.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;

import com.banermusic.constant.BaseConstants;

import java.io.IOException;

/**
 * Created by kodywu on 16/11/15.
 */
public class ImageUtil {

    private static int inSampleSize = 1;

    public static Bitmap createBitmap(Context context, int rid){

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), rid, options);

        int targetDensity = context.getResources().getDisplayMetrics().densityDpi;

        int x = BaseConstants.widthPixels;
        int y = BaseConstants.heightPixels;
        options.inSampleSize = calculateInSampleSize(options, x, y);

        double xSScale = ((double)options.outWidth) / ((double)x);
        double ySScale = ((double)options.outHeight) / ((double)y);

        double startScale = xSScale > ySScale ? xSScale : ySScale;

        options.inScaled = true;
        options.inDensity = (int) (targetDensity*startScale);
        options.inTargetDensity = targetDensity;
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), rid, options);

        return bitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
