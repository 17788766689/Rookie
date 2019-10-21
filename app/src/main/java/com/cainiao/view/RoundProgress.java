package com.cainiao.view;

/**
 * Created by WJH on 2018/3/14.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.cainiao.R;


public class RoundProgress extends View {
    private static final int DEFAULT_BG_COLOR = Color.GRAY;
    private static final int DEFAULT_ROUND_COLOR = Color.RED;
    private static final float DEFAULT_TEXT_SIZE = 16;
    private static final float DEFAULT_ROUND_WIDTH = 10;
    private static final int DEFAULT_TEXT_COLOR = Color.BLACK;
    private static final int DEFAULT_PROGRESS = 0;
    private int mBgColor;
    private int mRoundColor;
    private float mTextSize;
    private float mRoundWidth;
    private int mTextColor;
    private Paint mPaint;
    private int mCenterY;
    private int mCenterX;
    private float mRadius;
    private RectF mRectF;
    private int mProgress;
    private String tipsText;

    public RoundProgress(Context context) {
        this(context,null);
    }

    public RoundProgress(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public RoundProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getResources().obtainAttributes(attrs, R.styleable.RoundProgress);
        //拿到在xml配置的属性，如果没有配置，就使用默认的。
        mBgColor = a.getColor(R.styleable.RoundProgress_bgColor, DEFAULT_BG_COLOR);
        mRoundColor = a.getColor(R.styleable.RoundProgress_roundColor, DEFAULT_ROUND_COLOR);
        mTextSize = a.getDimension(R.styleable.RoundProgress_roundTextSize, DEFAULT_TEXT_SIZE);
        mRoundWidth = a.getDimension(R.styleable.RoundProgress_roundWidth, DEFAULT_ROUND_WIDTH);
        mTextColor = a.getColor(R.styleable.RoundProgress_roundTextColor, mRoundColor);
        mProgress = a.getInteger(R.styleable.RoundProgress_curProgress, DEFAULT_PROGRESS);
        tipsText = a.getString(R.styleable.RoundProgress_tipsText);
        a.recycle();//释放资源
        init();//初始化画笔
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    /**
     * 当layout大小变化后会回调次方法
     * 通过这方法获取宽高
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;//控宽的中心点
        mCenterY = h / 2;//控件高的中心点
        //防止宽高不一致
        int min = Math.min(mCenterX, mCenterY);
        //半径
        mRadius = min-mRoundWidth/2;
        float delta = mRadius - 2 * mRoundWidth;
        //为画圆弧准备
        mRectF = new RectF(mCenterX -  delta,mCenterY  - delta,mCenterX  + delta,mCenterY  + delta);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //        super.onDraw(canvas); 我们自己来画

        //1、先画背景填充圆环
        mPaint.setColor(mBgColor);
        mPaint.setStrokeWidth(mRoundWidth);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mCenterX, mCenterY,mRadius -  mRoundWidth / 2,mPaint);

        //2、先画背景进度圆环
        mPaint.setStyle(Paint.Style.STROKE); //设为空心
        mPaint.setColor(mRoundColor);
        mPaint.setStrokeWidth(mRoundWidth);
        canvas.drawCircle(mCenterX, mCenterY,mRadius - 2 * mRoundWidth,mPaint);
        //3、画动态圆弧
        mPaint.setColor(mTextColor);
        canvas.drawArc(mRectF,-90, (float) (3.6*mProgress),false,mPaint);

        mPaint.setStyle(Paint.Style.FILL); //设为实心
        //4、画中间的文字
        mPaint.setColor(mTextColor);
        mPaint.setStrokeWidth(0);//如果不设置回0，很难看
        mPaint.setTextSize(mTextSize);
        //测量字体的宽度
        float width = mPaint.measureText(tipsText);
        canvas.drawText(tipsText,mCenterX-width/2,mCenterY-mTextSize,mPaint);
        float mProgressSize = mTextSize * 2.2F;
        mPaint.setTextSize(mProgressSize);
        //测量字体的宽度
        width = mPaint.measureText(mProgress + "%");
        canvas.drawText(mProgress + "%",mCenterX-width/2,mCenterY+mProgressSize/2,mPaint);
    }
    public void setProgress(int progress){
        mProgress=progress;
        postInvalidate();
    }
}
