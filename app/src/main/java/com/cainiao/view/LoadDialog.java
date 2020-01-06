package com.cainiao.view;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.cainiao.R;


/**
 * Created by WJH on 2017/8/7.
 */

public class LoadDialog extends Dialog {

    private static int default_width = 160; //默认宽度
    private static int default_height = 120;//默认高度

    private TextView tvMsg;
    private RoundProgress rp;

    public LoadDialog(Context context, int layout, int style) {
        this(context, default_width, default_height, layout, style);
    }

    public LoadDialog(Context context, int width, int height, int layout, int style) {
        super(context, style);
        //set content
        setContentView(layout);
        tvMsg = findViewById(R.id.message);
        rp = findViewById(R.id.rp_progress);
        //set window params
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();

        //set width,height by density and gravity
        float density = getDensity(context);
        params.width = (int) (width*density);
        params.height = (int) (height*density);
        params.gravity = Gravity.CENTER;

        window.setAttributes(params);
    }

    /**设置显示的内容*/
    public void setMsg(String msg){
        if(tvMsg != null) tvMsg.setText(msg);
    }

    public void setProgress(int progress){
        if(rp != null) rp.setProgress(progress);
    }

    /**获取屏幕密度*/
    private float getDensity(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.density;
    }

}