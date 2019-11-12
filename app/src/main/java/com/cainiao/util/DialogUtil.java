package com.cainiao.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.cainiao.R;
import com.cainiao.view.AlertDialog;
import com.cainiao.view.LoadDialog;

/**
 * Created by WJH on 2017/8/7.
 */

public class DialogUtil {

    private android.app.AlertDialog noticeDialog = null;

    private DialogUtil() {
    }

    private static DialogUtil dialogUtil = null;
    private LoadDialog loadDialog = null;
    private AlertDialog alertDialog = null;

    /**
     * 实例化Dialog对象
     */
    public static DialogUtil get() {
        if (dialogUtil == null) {
            synchronized (DialogUtil.class) {
                if (dialogUtil == null) {
                    dialogUtil = new DialogUtil();
                }
            }
        }
        return dialogUtil;
    }

    /**
     * 显示进度对话框
     */
    public void showLoadDialog(Context context, String msg) {
        if (loadDialog == null) {
            loadDialog = new LoadDialog(context, 170, 170, R.layout.layout_loaddialog, R.style.LoadDialogStyle);
        }
        loadDialog.setMsg(msg.replace("\\n", "\n"));
        if (!loadDialog.isShowing() && !((Activity) context).isFinishing()) {
            loadDialog.setCancelable(false);
            loadDialog.show();
        }
    }

    /**
     * 关闭进度对话框
     */
    public void closeLoadDialog() {
        if (loadDialog != null && loadDialog.isShowing()) {
            loadDialog.cancel();
        }
        loadDialog = null;
    }

    /**
     * 显示提示对话框
     */
    public void showAlertDialog(Context context, String title, String msg, String btnMsg, View.OnClickListener callback) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog(context).builder();
            alertDialog.setTitle(title)
                    .setMsg(msg.replace("\\n", "\n"))
                    .setCancelable(false)
                    .setPositiveButton(btnMsg, callback);
            alertDialog.show();
        } else {
            alertDialog.setMsg(msg.replace("\\n", "\n"));
        }

    }

    /**
     * 显示提示对话框
     */
    public void showDoubleBtnAlertDialog(Context context, String msg, View.OnClickListener callback) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog(context).builder();
            alertDialog.setTitle("提示")
                    .setMsg(msg.replace("\\n", "\n"))
                    .setCancelable(false)
                    .setPositiveButton(context.getString(R.string.confirm), callback)
                    .setNegativeButton(context.getString(R.string.cancel), null);
            alertDialog.show();
        } else {
            alertDialog.setMsg(msg.replace("\\n", "\n"));
        }
    }


    /**
     * 显示提示对话框
     */
    public void showDoubleBtnAlertDialog(Context context, boolean cancelable, String title, String msg, String negText, String posText, View.OnClickListener negCallback, View.OnClickListener posCallback,DialogInterface.OnCancelListener nullCallback) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog(context).builder();
            alertDialog.setTitle(title)
                    .setMsg(msg.replace("\\n", "\n"))
                    .setCancelable(cancelable)
                    .setPositiveButton(posText, posCallback)
                    .setNegativeButton(negText, negCallback)
                    .setCancelListener(nullCallback);
            alertDialog.show();
        } else {
            alertDialog.setMsg(msg.replace("\\n", "\n"));
        }
    }


    /**
     * 显示输入框对话框
     */
    public void showInputAlertDialog(Context context, String msg, String hint, String positiveStr, View.OnClickListener callback) {
        alertDialog = new AlertDialog(context).builder();
        alertDialog.setTitle("提示")
                .setMsg(msg.replace("\\n", "\n"))
                .setHint(hint)
                .setCancelable(false)
                .setPositiveButton(positiveStr, callback)
                .setNegativeButton(context.getString(R.string.cancel), null);
        alertDialog.show();
    }

    /**
     * 获取输入框内容
     *
     * @return
     */
    public String getInputStr() {
        return alertDialog == null ? "" : alertDialog.getInput();
    }


    /**
     * 关闭提示的对话框
     */
    public void closeAlertDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        alertDialog = null;
    }

    /**
     * 显示进度对话框
     */
    public void showUpdateDialog(Context context, String msg) {
        if (loadDialog == null) {
            loadDialog = new LoadDialog(context, 170, 170, R.layout.layout_update_dialog, R.style.LoadDialogStyle);
        }
        loadDialog.setMsg(msg.replace("\\n", "\n"));
        if (!loadDialog.isShowing() && !((Activity) context).isFinishing()) {
            loadDialog.setCancelable(false);
            loadDialog.show();
        }
    }

    /**
     * 显示公告对话框
     */
    public void showNoticeDialog(Context context, String msg) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.layout_notice, null);
        TextView tvContent = view.findViewById(R.id.tv_content);
        TextView tvClose = view.findViewById(R.id.tv_close);
        tvContent.setText(Html.fromHtml(msg));
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(noticeDialog != null) noticeDialog.cancel();
                noticeDialog = null;
            }
        });
        noticeDialog = new android.app.AlertDialog.Builder(context).setView(view).setCancelable(false).create();
        noticeDialog.show();
    }

    public void setDownloadProgress(int progress) {
        if (loadDialog != null) loadDialog.setProgress(progress);
    }

}
