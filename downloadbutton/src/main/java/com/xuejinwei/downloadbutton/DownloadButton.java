package com.xuejinwei.downloadbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by xuejinwei on 2017/12/25.
 * Email:xuejinwei@outlook.com
 */

public class DownloadButton extends View {

    private static final String TAG = "DownloadButton";

    private static final float DEFAULT_TEXTVIEW_SIZE = 15f;//默认文字大小，单位sp
    private static final int   DEFAULT_COLOR         = 0xff1494F7;// 默认颜色，文字和边框
    private static final float DEFAULT_STROKE_WIDTH  = 2.0f;    // 默认边框宽度, dp
    private static final float DEFAULT_CORNER_RADIUS = 3.0f;   // 默认圆角半径, dp
    private static final float DEFAULT_LR_PADDING    = 16.0f;      // 默认左右内边距
    private static final float DEFAULT_TB_PADDING    = 6.0f;      // 默认上下内边距

    private final int mTextSize;
    private final int mTextColor;
    private final int mStrokeWidth;// 边框的宽度
    private final int mStrokeWidthdBinary;// 边框宽度的二分之一
    private String mText = "下载";

    private int mTextWidth;// 文字的所有宽度

    private int mRoundRectWidthBinary;// 矩形宽度，出去边框
    private int mRoundRectHeighBinary;// 矩形高度，出去边框

    private int mTopBottomPadding;// 中间文字上下边距
    private int mLeftRightPadding;// 中间文字左右边距

    private int mRadiu;// 半径
    private int mProgress = 0;// 百分比

//    private boolean isLoadingEnd = false;// 是否loading动画结束

    private Paint     mPaint;
    private Paint     mPaintIcon;
    private TextPaint mTextPaint;// 中间文字画笔

    private RectF contentRect;
    private RectF pauseLeftRect, pauseRightRect;

    private Path pathContinue;

    // 绘制外边框的四个顶点，和View 当中的mLeft……有区别的
    private int left;
    private int right;
    private int top;
    private int bottom;

    private ObjectAnimator shrinkAnim;// 缩小动画圆角
    private ObjectAnimator widthAnim;// 缩小动画，宽度

    private State mCurrentState;

    private OnClickListener mOnClickListener;

    public DownloadListener getDownloadListener() {
        return mDownloadListener;
    }

    public void setDownloadListener(DownloadListener downloadListener) {
        mDownloadListener = downloadListener;
    }

    private DownloadListener mDownloadListener;

    enum State {
        INITIAL,// 初始状态
        FODDING,// 折叠中状态
        LOADDING,// loading 中状态
        LOADDING_PAUSE,// loading中暂停状态
        COMPLETED_ERROR,// 失败
        COMPLETED_SUCCESSED// 成功
    }

    public DownloadButton(Context context) {
        this(context, null);
    }

    public DownloadButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 1);
    }

    public DownloadButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DownloadButton);

        mText = typedArray.getString(R.styleable.DownloadButton_android_text);
        mTextColor = typedArray.getColor(R.styleable.DownloadButton_android_textColor, DEFAULT_COLOR);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.DownloadButton_android_textSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXTVIEW_SIZE, context.getResources().getDisplayMetrics()));
        mStrokeWidth = typedArray.getDimensionPixelOffset(R.styleable.DownloadButton_strokeWidth, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, DEFAULT_STROKE_WIDTH, context.getResources().getDisplayMetrics()));
        mStrokeWidthdBinary = mStrokeWidth;
        mTopBottomPadding = typedArray.getDimensionPixelOffset(R.styleable.DownloadButton_contentPaddingTB, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TB_PADDING, context.getResources().getDisplayMetrics()));
        mLeftRightPadding = typedArray.getDimensionPixelOffset(R.styleable.DownloadButton_contentPaddingLR, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, DEFAULT_LR_PADDING, context.getResources().getDisplayMetrics()));

        mRadiu = typedArray.getDimensionPixelOffset(R.styleable.DownloadButton_radiu, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CORNER_RADIUS, context.getResources().getDisplayMetrics()));

        typedArray.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mTextColor);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaintIcon = new Paint();
        mPaintIcon.setAntiAlias(true);
        mPaintIcon.setColor(mTextColor);
        mPaintIcon.setStrokeCap(Paint.Cap.ROUND);
        mPaintIcon.setStyle(Paint.Style.FILL);

        mTextPaint = new TextPaint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);

        contentRect = new RectF();
        Log.i(TAG, "mTextSize:" + mTextSize + ";mStrokeWidth:" + mStrokeWidth + ";mTopBottomPadding" + mTopBottomPadding + ";mLeftRightPadding" + mLeftRightPadding);

        mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mDownloadListener == null) return;

                if (mCurrentState == State.INITIAL) {// 如果当前状态为初始状态
                    shrink();
                    return;
                }
                if (mCurrentState == State.LOADDING) {//loading ,再次点击暂停
                    mCurrentState = State.LOADDING_PAUSE;
                    invaidateSelft();
                    return;
                }

                if (mCurrentState == State.LOADDING_PAUSE) {// 暂停中 ,再次点击继续
                    mCurrentState = State.LOADDING;
                    invaidateSelft();
                    return;
                }
            }
        };
        setOnClickListener(mOnClickListener);

        mCurrentState = State.INITIAL;//初始化状态

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int resultW = widthSize;// 最终的测量宽度
        int resultH = heightSize;// 最终的测量高度

        mTextWidth = (int) mTextPaint.measureText(mText);

        if (heightMode == MeasureSpec.AT_MOST) {
            resultH = mTextSize + mTopBottomPadding * 2 + mRadiu * 2;
            resultH = resultH < heightSize ? resultH : heightSize;
        }

        if (widthMode == MeasureSpec.AT_MOST) {
            resultW = mTextWidth + mLeftRightPadding * 2 + mRadiu * 2;
            resultW = resultW < widthSize ? resultW : widthSize;
        }

        resultW = resultW < 2 * mRadiu ? 2 * mRadiu : resultW;
        resultH = resultH < 2 * mRadiu ? 2 * mRadiu : resultH;

        //因为有线条宽度，所以在确定绘制区域的时候考虑线条宽度
        mRoundRectWidthBinary = resultW / 2 - mStrokeWidthdBinary;
        if (resultW < resultH) {// 即使宽<长，也只显示宽度相同的矩形，
            mRoundRectHeighBinary = resultW / 2 - mStrokeWidthdBinary;
        } else {
            mRoundRectHeighBinary = resultH / 2 - mStrokeWidthdBinary;
        }
        setMeasuredDimension(resultW, resultH);

        Log.d(TAG, "onMeasure: w:" + resultW + " h:" + resultH + ";mRadiu" + mRadiu);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        drawRoundRect(canvas, cx, cy);
        drawText(canvas, cx, cy);

    }

    /**
     * 绘制边框
     *
     * @param canvas anvas
     * @param cx     view的中心相对位置x轴
     * @param cy     view的中心相对位置y轴
     */
    private void drawRoundRect(Canvas canvas, int cx, int cy) {

        mPaint.setStrokeWidth(mStrokeWidth / 2);

        left = cx - mRoundRectWidthBinary;
        top = cy - mRoundRectHeighBinary;
        right = cx + mRoundRectWidthBinary;
        bottom = cy + mRoundRectHeighBinary;
        contentRect.set(left, top, right, bottom);
        canvas.drawRoundRect(contentRect, mRadiu, mRadiu, mPaint);
        if (mCurrentState == State.LOADDING || mCurrentState == State.LOADDING_PAUSE) {
            float sweepAngle = (float) mProgress / 100f * 360;
            mPaint.setStrokeWidth(mStrokeWidth);
            canvas.drawArc(contentRect, -90f, sweepAngle, false, mPaint);
        }
    }

    /**
     * 绘制文字
     *
     * @param canvas canvas
     * @param cx     view的中心相对位置x轴
     * @param cy     view的中心相对位置y轴
     */
    private void drawText(Canvas canvas, int cx, int cy) {
        if (mCurrentState == State.LOADDING) {//绘制暂停按钮，两竖线
            if (pauseLeftRect == null || pauseRightRect == null) {

                int pauseWidthBinary = (int) (mRoundRectHeighBinary / 3.5f);//整个暂停按钮的宽度&高度的一半
                int pauseLeftRightWidth = pauseWidthBinary * 2 / 3;// pause 暂停按钮竖线的宽度，为宽高1/3
                pauseLeftRect = new RectF();
                pauseRightRect = new RectF();
                pauseLeftRect.set(cx - pauseWidthBinary, cy - pauseWidthBinary, cx - pauseWidthBinary + pauseLeftRightWidth, cy + pauseWidthBinary);
                pauseRightRect.set(cx + pauseWidthBinary - pauseLeftRightWidth, cy - pauseWidthBinary, cx + pauseWidthBinary, cy + pauseWidthBinary);
            }
            canvas.drawRect(pauseLeftRect, mPaintIcon);
            canvas.drawRect(pauseRightRect, mPaintIcon);
        } else if (mCurrentState == State.LOADDING_PAUSE) {// 绘制继续三角形
            if (pathContinue == null) {
                int iconWidthBinary = mRoundRectHeighBinary / 3;//整个继续按钮的宽度&高度的一半
                int offset = iconWidthBinary / 2;// 三角形左边竖线偏移量 为1/3
                pathContinue = new Path();
                pathContinue.moveTo(cx - iconWidthBinary + offset, cy - iconWidthBinary);
                pathContinue.lineTo(cx + iconWidthBinary, cy);
                pathContinue.lineTo(cx - iconWidthBinary + offset, cy + iconWidthBinary);
                pathContinue.close();
            }
            canvas.drawPath(pathContinue, mPaintIcon);
        } else {
            int textDescent = (int) mTextPaint.getFontMetrics().descent;
            int textAscent = (int) mTextPaint.getFontMetrics().ascent;
            int delta = Math.abs(textAscent) - textDescent;

            canvas.drawText(mText, cx, cy + delta / 2, mTextPaint);
        }
    }

    public void shrink() {
        mCurrentState = State.FODDING;
        if (shrinkAnim == null) {
            shrinkAnim = ObjectAnimator.ofInt(this, "radiu", mRadiu, mRoundRectHeighBinary);
            shrinkAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mCurrentState = State.LOADDING;
                    mDownloadListener.onStart();
                }
            });
            shrinkAnim.setDuration(500);
        }
        shrinkAnim.start();

        if (mRoundRectWidthBinary > mRoundRectHeighBinary) {
            // 只有：宽>长的时候才执行矩形缩小
            // 某些特殊情况：长>宽的时候不执行
            if (widthAnim == null) {
                widthAnim = ObjectAnimator.ofInt(this, "roundRectWidthBinary", mRoundRectWidthBinary, mRoundRectHeighBinary);
            }

            widthAnim.setDuration(500);
            widthAnim.start();
        }
    }

    public void setRadiu(int radiu) {
        mRadiu = radiu;
        invaidateSelft();
    }

    public void setRoundRectWidthBinary(int roundRectWidthBinary) {
        mRoundRectWidthBinary = roundRectWidthBinary;
        invaidateSelft();
    }

    public void setProgress(int progress) {
        if (progress > 100) {
            mProgress = 100;
        } else {
            mProgress = progress;
        }
        invaidateSelft();
    }

    private void invaidateSelft() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    @Override
    public void setOnClickListener(@Nullable final OnClickListener l) {
        super.setOnClickListener(mOnClickListener);
    }
}
