package com.example.sx.practicalassistant.utils;

import android.content.Context;

/**
 * 
 * @项目名称 InnovativeExperiment
 * @类名称 ToastUtil
 * @类描述
 * @创建人 dalu
 * @创建时间 2015年4月21日 上午11:22:51
 * @修改人 dalu
 * @修改时间 2015年4月21日 上午11:22:51
 * @修改备注
 * @version
 */
public class ToastUtil {

	private static android.widget.Toast toast;

	/**
	 * 短时间显示Toast(默认)
	 * 
	 * @param context
	 * @param message
	 */
	public static void showShortDefault(Context context, CharSequence message) {
		if (null == toast && context != null) {
			toast = android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT);
		} else {
			toast.setText(message);
		}
		toast.show();
	}

	/**
	 * 短时间显示Toast(默认)
	 * 
	 * @param context
	 * @param message
	 */
	public static void showShortDefault(Context context, int message) {
		if (null == toast && context != null) {
			toast = android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT);
		} else {
			toast.setText(message);
		}
		toast.show();
	}

	/**
	 * 长时间显示Toast(默认)
	 * 
	 * @param context
	 * @param message
	 */
	public static void showLongDefault(Context context, CharSequence message) {
		if (null == toast) {
			toast = android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG);
		} else {
			toast.setText(message);
		}
		toast.show();
	}

	/**
	 * 长时间显示Toast(默认)
	 * 
	 * @param context
	 * @param message
	 */
	public static void showLongDefault(Context context, int message) {
		if (null == toast) {
			toast = android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG);
		} else {
			toast.setText(message);
		}
		toast.show();
	}
}
