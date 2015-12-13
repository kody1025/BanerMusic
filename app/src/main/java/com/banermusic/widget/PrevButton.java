package com.banermusic.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.banermusic.event.SongMessage;

import de.greenrobot.event.EventBus;

public class PrevButton extends Button {

	private Paint mPaint;
	private boolean isChecked = false;

	public PrevButton(Context context) {
		super(context);
	}

	public PrevButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mPaint = new Paint();

		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new SongMessage(SongMessage.PREVMUSIC));
				v.setClickable(false);
			}
		});
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		mPaint.setColor(Color.GRAY);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(0);
		int center = getWidth() / 2;// 三角形,圆圈中央
		int sideLength = center / 5 * 3; // 三角形边长
		
		int circleWidth = sideLength / 5;
		mPaint.setStrokeWidth(circleWidth);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		canvas.drawCircle(center, center, center - circleWidth, mPaint);

		drawStop(canvas, center, sideLength);

		super.onDraw(canvas);
	}

	/**
	 * 画暂停状态
	 * 
	 * @param canvas
	 * @param center
	 *            三角形中心横纵坐标
	 * @param sideLength
	 *            三角形边长
	 */
	private void drawStop(Canvas canvas, int center, int sideLength) {
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setColor(Color.WHITE);
		float genSan = (float) Math.sqrt(3);
		Path path2 = new Path();
		path2.moveTo(center, center - sideLength / (2));
		path2.lineTo((center - 2 * sideLength / (2 * genSan)), center);
		path2.lineTo(center, center + sideLength / (2));
		path2.close();
		canvas.drawPath(path2, mPaint);

		Path path3 = new Path();
		path3.moveTo((center + 2 * sideLength / (2 * genSan)), center - sideLength / 2);
		path3.lineTo(center, center);
		path3.lineTo((center + 2 * sideLength / (2 * genSan)), center + sideLength / 2);
		path3.close();
		canvas.drawPath(path3, mPaint);
	}
	
	// 设置为wrap_content 时的控件高宽
    private int defultWidth = 27;
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int finalWidth = 0;
        int finaLHeight = 0;
        if (widthMode == MeasureSpec.EXACTLY) {
            finalWidth = widthSize;
        } else {
            finalWidth = (int) (getPaddingLeft() + defultWidth + getPaddingRight());
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            finaLHeight = heightSize;
        } else {
            finaLHeight = (int) (getPaddingTop() + defultWidth + getPaddingBottom());
        }
        setMeasuredDimension(finalWidth, finaLHeight);
    }

}
