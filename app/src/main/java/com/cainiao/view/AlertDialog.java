package com.cainiao.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.cainiao.R;
import com.cainiao.activity.MainActivity;
import com.cainiao.util.DialogUtil;
import com.cainiao.util.HttpUtil;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;


public class AlertDialog {
	private Context context;
	private Dialog dialog;
	private LinearLayout lLayout_bg;
	private TextView txt_title;
	private TextView txt_msg;
	private EditText et_input;
	private Button btn_neg;
	private Button btn_pos;
	private ImageView img_line;
	private Display display;
	private boolean showTitle = false;
	private boolean showMsg = false;
	private boolean showInput = false;
	private boolean showPosBtn = false;
	private boolean showNegBtn = false;

	public AlertDialog(Context context) {
		this.context = context;
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		display = windowManager.getDefaultDisplay();
	}

	public AlertDialog builder() {
		// 获取Dialog布局
		View view = LayoutInflater.from(context).inflate(
				R.layout.layout_alertdialog, null);

		// 获取自定义Dialog布局中的控件
		lLayout_bg = view.findViewById(R.id.lLayout_bg);
		txt_title = view.findViewById(R.id.txt_title);
		txt_title.setVisibility(View.GONE);
		txt_msg = view.findViewById(R.id.txt_msg);
		txt_msg.setVisibility(View.GONE);
		et_input = view.findViewById(R.id.et_name);
		et_input.setVisibility(View.GONE);
		btn_neg = view.findViewById(R.id.btn_neg);
		btn_neg.setVisibility(View.GONE);
		btn_pos = view.findViewById(R.id.btn_pos);
		btn_pos.setVisibility(View.GONE);
		img_line = view.findViewById(R.id.img_line);
		img_line.setVisibility(View.GONE);

		// 定义Dialog布局和参数
		dialog = new Dialog(context, R.style.AlertDialogStyle);
		dialog.setContentView(view);

		// 调整dialog背景大小
		lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams((int) (display
				.getWidth() * 0.7), LayoutParams.WRAP_CONTENT));

		return this;
	}

	public AlertDialog setTitle(String title) {
		showTitle = true;
		txt_title.setText(title);
		return this;
	}

	public AlertDialog setMsg(String msg) {
		showMsg = true;
		txt_msg.setText(msg);
		return this;
	}

	public AlertDialog setHint(String hint) {
		showInput = true;
		et_input.setText("");	//清空输入框内容
		et_input.setHint(hint);
		return this;
	}

	public AlertDialog setCancelable(boolean cancel) {
		dialog.setCancelable(cancel);
		return this;
	}

	public AlertDialog setPositiveButton(String text,
										 final OnClickListener listener) {
		showPosBtn = true;
		btn_pos.setText(text);
		btn_pos.setOnClickListener(v -> {
            if(listener != null)
                listener.onClick(v);
            DialogUtil.get().closeAlertDialog();
        });
		return this;
	}

	public AlertDialog setNegativeButton(String text,
										 final OnClickListener listener) {
		showNegBtn = true;
		btn_neg.setText(text);
		btn_neg.setOnClickListener(v -> {
            if(listener != null)
                listener.onClick(v);
            DialogUtil.get().closeAlertDialog();
        });
		return this;
	}

	private void setLayout() {

		if (showTitle) {
			txt_title.setVisibility(View.VISIBLE);
		}

		if (showMsg) {
			txt_msg.setVisibility(View.VISIBLE);
		}

		if(showInput){
			et_input.setVisibility(View.VISIBLE);
		}

		if (showPosBtn && showNegBtn) {
			btn_pos.setVisibility(View.VISIBLE);
			btn_pos.setBackgroundResource(R.drawable.alertdialog_right_selector);
			btn_neg.setVisibility(View.VISIBLE);
			btn_neg.setBackgroundResource(R.drawable.alertdialog_left_selector);
			img_line.setVisibility(View.VISIBLE);
		}

		if (showPosBtn && !showNegBtn) {
			btn_pos.setVisibility(View.VISIBLE);
			btn_pos.setBackgroundResource(R.drawable.alertdialog_single_selector);
		}

		if (!showPosBtn && showNegBtn) {
			btn_neg.setVisibility(View.VISIBLE);
			btn_neg.setBackgroundResource(R.drawable.alertdialog_single_selector);
		}
	}

	/**
	 * 获取输入框的内容
	 * @return
	 */
	public String getInput(){
		return et_input.getText().toString().trim();
	}

	public void show() {
		if(dialog == null) return;
		setLayout();
		dialog.show();
	}

	public boolean isShowing(){
		return dialog == null ? false : dialog.isShowing();
	}

	public void dismiss(){
		if(dialog != null && dialog.isShowing())
			dialog.dismiss();
	}

	public void setCancelListener(DialogInterface.OnCancelListener listener) {
		if (dialog == null || listener == null)return;
		dialog.setOnCancelListener(listener);

	}
}
