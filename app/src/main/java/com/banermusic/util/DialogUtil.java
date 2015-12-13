package com.banermusic.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.banermusic.R;

import java.util.Iterator;
import java.util.Stack;

@SuppressLint("InflateParams")
public class DialogUtil {

	/**
	 * 用于放所有进度框的栈
	 */
	private static Stack<Dialog> stack = new Stack<Dialog>();

	/**
	 * 将进度框入栈
	 * 
	 * @param progressDialog
	 */
	public static void push(Dialog progressDialog) {
		stack.push(progressDialog);
		
	}

	/**
	 * 取消所有进度条显示并销毁
	 */
	public static void popAll() {
		Iterator<Dialog> iterator = stack.iterator();
		while (iterator.hasNext()) {
			Dialog dialog = iterator.next();
			dialog.dismiss();
			stack.remove(dialog);
		}
		
	}
	
	
	/**
	 * 
	 * @param context
	 * @param message
	 */
	public static void showToast(Context context, String message) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.dialog_toast, null);
		TextView msg = (TextView) view.findViewById(R.id.message);
				msg.setText(message);
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setView(view);
		toast.show();
	}

}
