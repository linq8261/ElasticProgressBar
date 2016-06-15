package com.audienl.elasticprogressbarcore;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

public class ElasticProgressBar extends View {
    public static final long ANIMATION_DURATION_BASE = 1250;

    private Context context;

    /** 用户自定义值 */
    private int mBackLineColor;
    private int mFrontLineColor;
    private int mBubbleColor;
    private int mTextColor;

    /** 最大最小长宽，onMeasure用 */
    private int mWidthMin;
    private int mHeightMin;
    /** 真实长宽 */
    private int mWidth;
    private int mHeight;
    /** Bar的宽度 */
    private int mLineWidth;
    /** 其它数据 */
    private int mBubbleWidth;
    private int mBubbleHeight;
    private int mPadding;
    /** 各种Paint */
    private Paint mPaintBackLine;
    private Paint mPaintFrontLine;
    private Paint mPaintBubble;
    private Paint mPaintText;
    /** 各种Path */
    private Path mPathBackLine;
    private Path mPathFrontLine;
    private Path mPathBubble;

    private int bubbleAnchorX, bubbleAnchorY;
    private float mDensity = getResources().getDisplayMetrics().density;
    private float mProgress = 0, mTarget = 0, mSpeedAngle = 0, mBubbleAngle = 0, mFailAngle = 0, mFlipFactor;
    private State mState = State.STATE_WORKING;

    private enum State {
        STATE_WORKING,
        STATE_FAILED,
        STATE_SUCCESS
    }

    public ElasticProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        // 获取用户自定义数据
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ElasticProgressBar);
        mBackLineColor = a.getColor(R.styleable.ElasticProgressBar_back_line_color, context.getResources()
                                                                                           .getColor(R.color.back_line));
        mFrontLineColor = a.getColor(R.styleable.ElasticProgressBar_front_line_color, context.getResources()
                                                                                             .getColor(R.color.front_line));
        mBubbleColor = a.getColor(R.styleable.ElasticProgressBar_bubble_color, context.getResources()
                                                                                      .getColor(R.color.bubble_color));
        mTextColor = a.getColor(R.styleable.ElasticProgressBar_text_color, context.getResources()
                                                                                  .getColor(R.color.text_color));
        a.recycle();

        mWidthMin = dp2px(150);
        mHeightMin = dp2px(150);
        mBubbleWidth = dp2px(45);
        mBubbleHeight = dp2px(35);

        mPadding = dp2px(30);
        setPadding(mPadding, 0, mPadding, 0);

        mPaintBackLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBackLine.setStyle(Paint.Style.STROKE);
        mPaintBackLine.setStrokeWidth(5 * mDensity);
        mPaintBackLine.setColor(mBackLineColor);
        mPaintBackLine.setStrokeCap(Paint.Cap.ROUND);

        mPaintFrontLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintFrontLine.setStyle(Paint.Style.STROKE);
        mPaintFrontLine.setStrokeWidth(5 * mDensity);
        mPaintFrontLine.setColor(mFrontLineColor);
        mPaintFrontLine.setStrokeCap(Paint.Cap.ROUND);

        mPaintBubble = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBubble.setColor(mBubbleColor);
        mPaintBubble.setStyle(Paint.Style.FILL);

        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setColor(mTextColor);
        mPaintText.setStyle(Paint.Style.FILL);
        mPaintText.setTextSize(12 * mDensity);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mPathFrontLine != null && mPathBackLine != null) {
            float textX = getXNow() - mBubbleWidth / 4.0f;
            float textY = mHeight / 2 - mBubbleHeight / 2 + calculateDeltaY();

            switch (mState) {
                case STATE_WORKING:
                    // Save and restore prevent the rest of the canvas to not be rotated
                    canvas.save();
                    float speed = (mProgress - mTarget) / 20;
                    mBubbleAngle += speed * 10;
                    if (mBubbleAngle > 20) {
                        mBubbleAngle = 20;
                    }
                    if (mBubbleAngle < -20) {
                        mBubbleAngle = -20;
                    }
                    if (Math.abs(speed) < 1) {
                        mSpeedAngle -= mBubbleAngle / 20;
                        mSpeedAngle *= .9f;
                    }
                    mBubbleAngle += mSpeedAngle;

                    canvas.rotate(mBubbleAngle, bubbleAnchorX, bubbleAnchorY);
                    canvas.drawPath(mPathBubble, mPaintBubble);
                    canvas.drawText(String.valueOf((int) mProgress) + " %", textX, textY, mPaintText);
                    canvas.restore();
                    break;
                case STATE_FAILED:
                    canvas.save();
                    canvas.rotate(mFailAngle, bubbleAnchorX, bubbleAnchorY);
                    canvas.drawPath(mPathBubble, mPaintBubble);
                    canvas.rotate(mFailAngle, bubbleAnchorX, textY - mBubbleHeight / 7);
                    //                    mPaintText.setColor(getResources().getColor(R.color.red_wine));
                    textX = getXNow() - mBubbleWidth / 3.2f;
                    canvas.drawText(getResources().getString(R.string.failed), textX, textY, mPaintText);
                    canvas.restore();
                    break;
                case STATE_SUCCESS:
                    canvas.save();
                    //                    mPaintText.setColor(getResources().getColor(R.color.green_grass));
                    textX = getXNow() - mBubbleWidth / 3.2f;
                    Matrix flipMatrix = new Matrix();
                    flipMatrix.setScale(mFlipFactor, 1, bubbleAnchorX, bubbleAnchorY);
                    canvas.concat(flipMatrix);
                    canvas.drawPath(mPathBubble, mPaintBubble);
                    canvas.concat(flipMatrix);
                    canvas.drawText(getResources().getString(R.string.done), textX, textY, mPaintText);
                    canvas.restore();
                    break;
            }

            canvas.drawPath(mPathBackLine, mPaintBackLine);
            canvas.drawPath(mPathFrontLine, mPaintFrontLine);
        }
    }

    // =========================================================
    // 对外接口
    // =========================================================

    public void start() {
        post(new Runnable() {
            @Override
            public void run() {
                setProgressSelf(0);
            }
        });
    }

    public void setProgress(final float progress) {
        HandlerUtils.post(new Runnable() {
            @Override
            public void run() {
                setPercentage(progress);
            }
        });
    }

    public void success() {
        HandlerUtils.post(new Runnable() {
            @Override
            public void run() {
                drawSuccess();
            }
        });
    }

    public void fail() {
        HandlerUtils.post(new Runnable() {
            @Override
            public void run() {
                drawFail();
            }
        });
    }

    // =========================================================
    // 动画需要设置属性 setProgress setFailAngle setFlip
    // =========================================================

    private void setPercentage(float newProgress) {
        if (newProgress < 0 || newProgress > 100) {
            throw new IllegalArgumentException("setPercentage not between 0 and 100");
        }

        mState = State.STATE_WORKING;
        mTarget = newProgress;

        ValueAnimator animator = ValueAnimator.ofFloat(mProgress, mTarget);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration((long) (ANIMATION_DURATION_BASE + Math.abs(mTarget * 10 - mProgress * 10) / 2));
        animator.start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                setProgressSelf(value);
            }
        });
    }

    /** 内部使用方法 */
    private void setProgressSelf(float progress) {
        mProgress = progress;
        makePathBackLine();
        makePathFrontLine();
        makePathBubble();
        postInvalidate();
    }

    private void setFailAngle(float failAngle) {
        mFailAngle = failAngle;
        makePathBackLine();
        makePathFrontLine();
        makePathBubble();
        invalidate();
    }

    private void setFlip(float flipValue) {
        mFlipFactor = flipValue;
        makePathBackLine();
        makePathFrontLine();
        makePathBubble();
        invalidate();
    }

    private void drawFail() {
        mState = State.STATE_FAILED;

        ObjectAnimator failAnim = ObjectAnimator.ofFloat(this, "failAngle", 0, 180);
        failAnim.setInterpolator(new OvershootInterpolator());

        //This one doesn't do much actually, we just use it to take advantage of associating two different interpolators
        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "progressSelf", mProgress, mTarget);
        anim.setInterpolator(new AccelerateInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.setDuration((long) (ANIMATION_DURATION_BASE / 1.7f));
        set.playTogether(failAnim, anim);
        set.start();
    }

    private void drawSuccess() {
        mTarget = 100;

        final ObjectAnimator successAnim = ObjectAnimator.ofFloat(this, "flip", 1, -1);
        successAnim.setInterpolator(new OvershootInterpolator());
        successAnim.setDuration(ANIMATION_DURATION_BASE);

        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "progressSelf", mProgress, mTarget);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration((long) (ANIMATION_DURATION_BASE + Math.abs(mTarget * 10 - mProgress * 10) / 2));
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mState = State.STATE_SUCCESS;
                successAnim.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        anim.start();
    }

    private void makePathBackLine() {
        if (mPathBackLine == null) {
            mPathBackLine = new Path();
        }

        Path p = new Path();
        p.moveTo(getXNow(), mHeight / 2 + calculateDeltaY());
        p.lineTo(mWidth - mPadding, mHeight / 2);

        mPathBackLine.set(p);
    }

    private void makePathFrontLine() {
        if (mPathFrontLine == null) {
            mPathFrontLine = new Path();
        }

        Path p = new Path();
        p.moveTo(mPadding, mHeight / 2);
        p.lineTo(getXNow(), mHeight / 2 + calculateDeltaY());

        mPathFrontLine.set(p);
    }

    private void makePathBubble() {
        if (mPathBubble == null) {
            mPathBubble = new Path();
        }

        int width = mBubbleWidth;
        int height = mBubbleHeight;
        int arrowWidth = width / 3;

        Rect r = new Rect((int) (getXNow() - width / 2), (int) (mHeight / 2 - height + calculateDeltaY()), (int) (getXNow() + width / 2), (int) (mHeight / 2 + height - height + calculateDeltaY()));
        int arrowHeight = (int) (arrowWidth / 1.5f);
        int radius = 8;

        Path path = new Path();

        // Down arrow
        path.moveTo(r.left + r.width() / 2 - arrowWidth / 2, r.top + r.height() - arrowHeight);
        bubbleAnchorX = r.left + r.width() / 2;
        bubbleAnchorY = r.top + r.height();
        path.lineTo(bubbleAnchorX, bubbleAnchorY);
        path.lineTo(r.left + r.width() / 2 + arrowWidth / 2, r.top + r.height() - arrowHeight);

        // Go to bottom-right
        path.lineTo(r.left + r.width() - radius, r.top + r.height() - arrowHeight);

        // Bottom-right arc
        path.arcTo(new RectF(r.left + r.width() - 2 * radius, r.top + r.height() - arrowHeight - 2 * radius, r.left + r.width(), r.top + r.height() - arrowHeight), 90, -90);

        // Go to upper-right
        path.lineTo(r.left + r.width(), r.top + arrowHeight);

        // Upper-right arc
        path.arcTo(new RectF(r.left + r.width() - 2 * radius, r.top, r.right, r.top + 2 * radius), 0, -90);

        // Go to upper-left
        path.lineTo(r.left + radius, r.top);

        // Upper-left arc
        path.arcTo(new RectF(r.left, r.top, r.left + 2 * radius, r.top + 2 * radius), 270, -90);

        // Go to bottom-left
        path.lineTo(r.left, r.top + r.height() - arrowHeight - radius);

        // Bottom-left arc
        path.arcTo(new RectF(r.left, r.top + r.height() - arrowHeight - 2 * radius, r.left + 2 * radius, r.top + r.height() - arrowHeight), 180, -90);

        path.close();

        mPathBubble.set(path);
    }

    private float getXNow() {
        return mPadding + mProgress * mLineWidth / 100;
    }

    private float calculateDeltaY() {
        int wireTension = 15;
        if (mProgress <= 50) {
            return (mProgress * mLineWidth / wireTension) / 50 + Math.abs((mTarget - mProgress) / wireTension) + Math.abs(mBubbleAngle);
        } else {
            return ((100 - mProgress) * mLineWidth / wireTension) / 50 + Math.abs((mTarget - mProgress) / wireTension) + Math.abs(mBubbleAngle);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                mWidth = Math.min(mWidthMin, widthSize);
                break;
            case MeasureSpec.EXACTLY:
                mWidth = widthSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                mWidth = mWidthMin;
                break;
        }
        switch (heightMode) {
            case MeasureSpec.AT_MOST:
                mHeight = Math.min(mHeightMin, heightSize);
                break;
            case MeasureSpec.EXACTLY:
                mHeight = heightSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                mHeight = mHeightMin;
                break;
        }

        mLineWidth = mWidth - mPadding * 2;

        setMeasuredDimension(mWidth, mHeight);
    }

    private int dp2px(int dp) {
        return (int) (dp * context.getResources()
                                  .getDisplayMetrics().density);
    }
}
