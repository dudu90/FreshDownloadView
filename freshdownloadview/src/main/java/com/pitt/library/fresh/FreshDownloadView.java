package com.pitt.library.fresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.AbsSavedState;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;


/**
 * A download progressbar with cool animator
 * https://github.com/dudu90/FreshDownloadView
 *
 * @author Pitt
 *
 *         Licensed under the Apache License 2.0 license see:
 *         http://www.apache.org/licenses/LICENSE-2.0
 */

public class FreshDownloadView extends View {


    private final String TAG = FreshDownloadView.class.getSimpleName();

    //the circular radius
    private float radius;
    private int circular_color;
    private int circular_progress_color;
    private float circular_width;

    private float circular_edge;


    private Rect bounds;
    private RectF mTempBounds;
    private float mRealLeft;
    private boolean mPrepareAniRun = false;
    private float mRealTop;

    private float mProgressTextSize;
    private Rect textBounds;
    private final String STR_PERCENT = "%";
    private float mMarkOklength;

    private AnimatorSet mOkAnimatorSet;
    private AnimatorSet mErrorAnimatorSet;
    private float mMarkArcAngle;
    private float mMarkOkdegree;
    private float mMarkOkstart;
    private boolean mMarkOkAniRun;
    private float mErrorPathLengthLeft;
    private float mErrorPathLengthRight;
    private float mErrorRightDegree;
    private boolean mIfShowError;
    private float mErrorLeftDegree;
    private boolean mIfShowMarkRun = false;
    private boolean mAttached;

    private boolean mUsing;
    private Path mDst = new Path();


    /**
     * the view's Status
     */
    public enum STATUS {
        PREPARE, DOWNLOADING, DOWNLOADED, ERROR
    }


    //used when in downloaded
    private enum STATUS_MARK {
        DRAW_ARC, DRAW_MARK
    }


    private Paint publicPaint;

    private Path path1;
    private Path path2;
    private Path path3;
    private PathMeasure pathMeasure1;
    private PathMeasure pathMeasure2;
    private PathMeasure pathMeasure3;


    private float mArrowStart;
    private float startingArrow;

    private float mArrow_left_length;
    private float mArrow_right_length;
    private float mArrow_center_length;

    private DashPathEffect mArrow_center_effect;
    private DashPathEffect mArrow_left_effect;
    private DashPathEffect mArrow_right_effect;


    private STATUS status = STATUS.PREPARE;

    private STATUS_MARK status_mark;


    private AnimatorSet prepareAnimator;


    private float mProgress;
    private final static float START_ANGLE = -90f;
    private final static float TOTAL_ANGLE = 360f;
    private final static float MARK_START_ANGLE = 65f;
    private final static float DEGREE_END_ANGLE = 270f;


    public FreshDownloadView(Context context) {
        this(context, null);
    }

    public FreshDownloadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public FreshDownloadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        circular_edge = getResources().getDimension(R.dimen.edge);

        bounds = new Rect();
        mTempBounds = new RectF();
        publicPaint = new Paint();
        path1 = new Path();
        path2 = new Path();
        path3 = new Path();
        pathMeasure1 = new PathMeasure();
        pathMeasure2 = new PathMeasure();
        pathMeasure3 = new PathMeasure();
        textBounds = new Rect();

        parseAttrs(context.obtainStyledAttributes(attrs, R.styleable.FreshDownloadView));
        initPaint();
    }

    private void parseAttrs(TypedArray array) {
        if (array != null) {
            try {
                setRadius(array.getDimension(R.styleable.FreshDownloadView_circular_radius, getResources().getDimension(R.dimen.default_radius)));
                setCircularColor(array.getColor(R.styleable.FreshDownloadView_circular_color, getResources().getColor(R.color.default_circular_color)));
                setProgressColor(array.getColor(R.styleable.FreshDownloadView_circular_progress_color, getResources().getColor(R.color.default_circular_progress_color)));
                setCircularWidth(array.getDimension(R.styleable.FreshDownloadView_circular_width, getResources().getDimension(R.dimen.default_circular_width)));
                setProgressTextSize(array.getDimension(R.styleable.FreshDownloadView_progress_text_size, getResources().getDimension(R.dimen.default_text_size)));
            } finally {
                array.recycle();
            }
        }


    }

    private void initPaint() {
        publicPaint.setStrokeCap(Paint.Cap.ROUND);
        publicPaint.setStrokeWidth(getCircularWidth());
        publicPaint.setStyle(Paint.Style.STROKE);
        publicPaint.setAntiAlias(true);
    }

    public void startDownload() {
        mUsing = true;
        if (prepareAnimator != null && mPrepareAniRun) {
            return;
        }

        if (prepareAnimator == null) {
            prepareAnimator = getPrepareAnimator();
        }

        prepareAnimator.start();
    }

    private AnimatorSet getPrepareAnimator() {
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator downAnimaor = ValueAnimator.ofFloat(0f, 0.3f, 0f).setDuration(500);
        downAnimaor.setInterpolator(new DecelerateInterpolator());
        downAnimaor.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (float) animation.getAnimatedValue();
                mArrowStart = startingArrow + (2 - .48f - 1f) * getRadius() * value;
                updateArrow();
                invalidate();
            }
        });

        ValueAnimator upAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(800);
        upAnimator.setInterpolator(new DecelerateInterpolator());
        upAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (float) animation.getAnimatedValue();
                mArrow_left_effect = new DashPathEffect(new float[]{mArrow_left_length, mArrow_left_length}, value * mArrow_left_length);

                mArrow_right_effect = new DashPathEffect(new float[]{mArrow_right_length, mArrow_right_length}, value * mArrow_right_length);


                float reduceDis = (1 - value) * (startingArrow - mRealTop);
                path1.reset();
                path1.moveTo(mRealLeft + radius, mRealTop + reduceDis);
                path1.lineTo(mRealLeft + radius, mRealTop + reduceDis + mArrow_center_length);


                mArrow_center_effect = new DashPathEffect(new float[]{mArrow_center_length, mArrow_center_length}, value * mArrow_center_length);
                invalidate();
            }
        });
        upAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mArrow_center_effect = null;
                mArrow_right_effect = null;
                mArrow_left_effect = null;
                updateArrow();
            }

            @Override
            public void onAnimationStart(Animator animation) {

            }
        });
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                status = STATUS.DOWNLOADING;
                invalidate();
            }
        });
        animatorSet.play(downAnimaor).before(upAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                mPrepareAniRun = false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mPrepareAniRun = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mPrepareAniRun = false;
            }
        });
        return animatorSet;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int dx = 0;
        int dy = 0;
        dx += getPaddingLeft() + getPaddingRight() + getCurrentWidth();
        dy += getPaddingTop() + getPaddingBottom() + getCurrentHeight();
        final int measureWidth = resolveSizeAndState(dx, widthMeasureSpec, 0);
        final int measureHeight = resolveSizeAndState(dy, heightMeasureSpec, 0);
        setMeasuredDimension(Math.max(getSuggestedMinimumWidth(), measureWidth), Math.max(getSuggestedMinimumHeight(), measureHeight));
    }

    private int getCurrentHeight() {
        return (int) ((getRadius() * 2) + circular_edge * 2);
    }


    private int getCurrentWidth() {
        return (int) ((getRadius() * 2) + circular_edge * 2);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int top = 0 + getPaddingTop();
        final int bottom = getHeight() - getPaddingBottom();
        final int left = 0 + getPaddingLeft();
        final int right = getWidth() - getPaddingRight();
        updateBounds(top, bottom, left, right);
        initArrowPath(top, bottom, left, right, getRadius());
    }

    private float buildAngle_percent_of_pain_width() {
        final double perimeter = getRadius() * 2 * Math.PI;
        final double width = getCircularWidth();
        return (float) (width / perimeter);
    }

    /**
     * update the Bounds of circular
     *
     * @param top
     * @param bottom
     * @param left
     * @param right
     */
    private void updateBounds(int top, int bottom, int left, int right) {
        bounds.set(left, top, right, bottom);
    }

    private void initArrowPath(int top, int bottom, int left, int right, float radius) {
        final float realTop = top + circular_edge;
        mRealLeft = left + circular_edge;
        mRealTop = realTop;
        startingArrow = realTop + radius * .48f;
        mArrowStart = startingArrow;
        status = STATUS.PREPARE;
        updateArrow();
    }

    /**
     * update the Arrow's Status
     */
    private void updateArrow() {
        path1.reset();
        path2.reset();
        path3.reset();
        path1.moveTo(mRealLeft + radius, mArrowStart);
        path1.lineTo(mRealLeft + radius, mArrowStart + radius);
        path2.moveTo(mRealLeft + radius, mArrowStart + radius);
        path2.lineTo((float) (mRealLeft + radius - Math.tan(Math.toRadians(40)) * radius * 0.46f), mArrowStart + radius - radius * .46f);
        path3.moveTo(mRealLeft + radius, mArrowStart + radius);
        path3.lineTo((float) (mRealLeft + radius + Math.tan(Math.toRadians(40)) * radius * 0.46f), mArrowStart + radius - radius * .46f);
        pathMeasure1.setPath(path1, false);
        pathMeasure2.setPath(path2, false);
        pathMeasure3.setPath(path3, false);
        mArrow_center_length = pathMeasure1.getLength();
        mArrow_left_length = pathMeasure2.getLength();
        mArrow_right_length = pathMeasure3.getLength();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        publicPaint.setPathEffect(null);
        publicPaint.setStyle(Paint.Style.STROKE);
        publicPaint.setColor(getCircularColor());
        final RectF arcBounds = mTempBounds;
        arcBounds.set(bounds);
        arcBounds.inset(circular_edge, circular_edge);
        canvas.drawArc(arcBounds, 0, 360, false, publicPaint);
        switch (status) {
            case PREPARE:
                drawPrepare(canvas);
                break;
            case DOWNLOADING:
                drawDownLoading(canvas, arcBounds);
                break;
            case DOWNLOADED:
                drawDownLoaded(canvas, status_mark, arcBounds, mMarkArcAngle);
                break;
            case ERROR:
                drawDownError(canvas);
                break;
            default:
        }
    }

    /**
     * Draw the Arrow
     */
    private void drawPrepare(Canvas canvas) {
        publicPaint.setColor(getProgressColor());
        if (mArrow_center_effect != null) {
            publicPaint.setPathEffect(mArrow_center_effect);
        }
        canvas.drawPath(path1, publicPaint);
        if (mArrow_left_effect != null) {
            publicPaint.setPathEffect(mArrow_left_effect);
        }
        canvas.drawPath(path2, publicPaint);
        if (mArrow_right_effect != null) {
            publicPaint.setPathEffect(mArrow_right_effect);
        }
        canvas.drawPath(path3, publicPaint);
    }

    /**
     * Draw the Progress
     */
    private void drawDownLoading(Canvas canvas, RectF arcBounds) {
        final float progress_degree = mProgress;
        publicPaint.setColor(getProgressColor());

        if (progress_degree <= 0) {
            canvas.drawPoint(mRealLeft + radius, mRealTop, publicPaint);
        } else {
            canvas.drawArc(arcBounds, START_ANGLE, (progress_degree) * TOTAL_ANGLE, false, publicPaint);
        }
        drawText(canvas, progress_degree);
    }

    private void drawText(Canvas canvas, float progress_degree) {
        final String sDegree = String.valueOf(Math.round(progress_degree * 100));
        final Rect rect = bounds;
        publicPaint.setStyle(Paint.Style.FILL);
        publicPaint.setTextSize(getProgressTextSize());
        publicPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetricsInt fontMetrics = publicPaint.getFontMetricsInt();
        int baseline = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText(sDegree, rect.centerX(), baseline, publicPaint);
        publicPaint.getTextBounds(sDegree, 0, sDegree.length(), textBounds);
        publicPaint.setTextSize(getProgressTextSize() / 3);
        publicPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(STR_PERCENT, rect.centerX() + textBounds.width() / 2 + .1f * radius, baseline, publicPaint);
    }

    /**
     * Draw success
     */
    private void drawDownLoaded(Canvas canvas, STATUS_MARK status, RectF bounds, float angle) {
        publicPaint.setColor(getProgressColor());
        switch (status) {
            case DRAW_ARC:
                canvas.drawArc(bounds, DEGREE_END_ANGLE - angle, 0.001f * TOTAL_ANGLE, false, publicPaint);
                break;
            case DRAW_MARK:
                final Path dst = mDst;
                dst.reset();
                //to fix hardware speedup bug
                dst.lineTo(0, 0);
                pathMeasure1.getSegment(mMarkOkstart * mMarkOklength, (mMarkOkstart + mMarkOkdegree) * mMarkOklength, dst, true);
                canvas.drawPath(dst, publicPaint);
                break;
        }
    }

    /**
     * Draw error
     */
    private void drawDownError(Canvas canvas) {
        if (mIfShowMarkRun) {
            final float progress = mProgress;
            drawText(canvas, progress);
        }
        publicPaint.setColor(Color.WHITE);
        final Path dst = mDst;
        dst.reset();
        //to fix hardware speedup bug
        dst.lineTo(0, 0);
        pathMeasure1.getSegment(0.2f * mErrorPathLengthLeft, mErrorRightDegree * mErrorPathLengthLeft, dst, true);
        canvas.drawPath(dst, publicPaint);
        dst.reset();
        //to fix hardware speedup bug
        dst.lineTo(0, 0);
        pathMeasure2.getSegment(0.2f * mErrorPathLengthRight, mErrorLeftDegree * mErrorPathLengthRight, dst, true);
        canvas.drawPath(dst, publicPaint);
    }

    /**
     * update progress
     *
     * @param progress percent of 100,the value must from 0f to 1f
     */
    public void upDateProgress(float progress) {
        setProgressInternal(progress);
    }

    /**
     * update progress
     *
     * @param progress the value must from 0 to 100;
     */
    public void upDateProgress(int progress) {
        upDateProgress((float) progress / 100);
    }

    /**
     * call it when you want to reset all;
     */
    public void reset() {
        resetStatus();
    }

    /**
     * Called when you want to reset the Status.
     * when @see #status==DOWNLOADING or animators are running,the call will be invalid.
     */
    private void resetStatus() {
        if (status == STATUS.DOWNLOADING || mPrepareAniRun || mIfShowError || mMarkOkAniRun) return;
        status = STATUS.PREPARE;
        mArrowStart = startingArrow;
        updateArrow();
        postInvalidate();
        this.mProgress = 0;
        mMarkOkdegree = 0f;
        mMarkArcAngle = 0f;
        mMarkOkstart = 0f;
        mUsing = false;
        mErrorLeftDegree = 0f;
        mErrorRightDegree = 0f;
    }

    /**
     * get Use Status
     *
     * @return if use by some task.
     */
    public boolean using() {
        return mUsing;
    }

    synchronized void setProgressInternal(float progressInternal) {
        this.mProgress = progressInternal;
        if (status == STATUS.PREPARE) {
            startDownload();
        }

        invalidate();
        if (progressInternal >= 1) {
            showDownloadOk();
        }
    }

    /**
     * showDownLoadOK
     */
    public void showDownloadOk() {
        status = STATUS.DOWNLOADED;
        makeOkPath();
        if (mOkAnimatorSet != null && mMarkOkAniRun) {
            return;
        }
        if (mOkAnimatorSet == null) {
            mOkAnimatorSet = getDownloadOkAnimator();
        }
        mOkAnimatorSet.start();
    }

    /**
     * make the Path to show
     */
    private void makeOkPath() {
        path1.reset();
        int w2 = getMeasuredWidth() / 2;
        int h2 = getMeasuredHeight() / 2;
        double a = Math.cos(Math.toRadians(25)) * getRadius();
        double c = Math.sin(Math.toRadians(25)) * getRadius();
        double l = Math.cos(Math.toRadians(53)) * 2 * a;
        double b = Math.sin(Math.toRadians(53)) * l;
        double m = Math.cos(Math.toRadians(53)) * l;
        path1.moveTo((float) (w2 - a), (float) (h2 - c));
        path1.lineTo((float) (w2 - a + m), (float) (h2 - c + Math.sin(Math.toRadians(53)) * l));
        path1.lineTo((float) (w2 + a), (float) (h2 - c));
        pathMeasure1.setPath(path1, false);
        mMarkOklength = pathMeasure1.getLength();
    }

    /**
     * create a new DownLoadOkAnimator
     *
     * @return a new Animatorset for DownloadOk.
     */
    private AnimatorSet getDownloadOkAnimator() {
        AnimatorSet animatorSet = new AnimatorSet();

        ValueAnimator roundAnimator = ValueAnimator.ofFloat(0f, MARK_START_ANGLE).setDuration(100);
        roundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMarkArcAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        roundAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                status_mark = STATUS_MARK.DRAW_ARC;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                status_mark = STATUS_MARK.DRAW_MARK;
            }
        });
        ValueAnimator firstAnimator = ValueAnimator.ofFloat(0f, 0.7f).setDuration(200);
        firstAnimator.setInterpolator(new AccelerateInterpolator());
        firstAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMarkOkdegree = (float) animation.getAnimatedValue();

                invalidate();
            }
        });

        ValueAnimator secondAnimator = ValueAnimator.ofFloat(0f, 0.35f, 0.2f).setDuration(500);
        secondAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMarkOkstart = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        secondAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                mIfShowMarkRun = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIfShowMarkRun = false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mIfShowMarkRun = true;
            }
        });
        animatorSet.play(firstAnimator).after(roundAnimator);
        animatorSet.play(secondAnimator).after(200);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mMarkOkAniRun = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mMarkOkAniRun = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mMarkOkAniRun = false;
            }
        });
        return animatorSet;
    }

    public void showDownloadError() {
        status = STATUS.ERROR;
        makeErrorPath();
        invalidate();
        if (mErrorAnimatorSet != null && mIfShowError) {
            return;
        }
        if (mErrorAnimatorSet == null) {
            mErrorAnimatorSet = getDownLoadErrorAnimator();
        }
        mErrorAnimatorSet.start();
    }

    private void makeErrorPath() {
        final Rect bounds = this.bounds;
        final int w2 = bounds.centerX();
        final int h2 = bounds.centerY();
        path1.reset();
        path1.moveTo((float) (w2 - Math.cos(Math.toRadians(45)) * getRadius()), (float) (h2 - Math.sin(Math.toRadians(45)) * getRadius()));
        path1.lineTo((float) (w2 + Math.cos(Math.toRadians(45)) * getRadius()), (float) (h2 + Math.sin(Math.toRadians(45)) * getRadius()));
        pathMeasure1.setPath(path1, false);
        mErrorPathLengthLeft = pathMeasure1.getLength();
        path1.reset();
        path1.moveTo((float) (w2 + Math.cos(Math.toRadians(45)) * getRadius()), (float) (h2 - Math.sin(Math.toRadians(45)) * getRadius()));
        path1.lineTo((float) (w2 - Math.cos(Math.toRadians(45)) * getRadius()), (float) (h2 + Math.sin(Math.toRadians(45)) * getRadius()));
        pathMeasure2.setPath(path1, false);
        mErrorPathLengthRight = pathMeasure2.getLength();
    }


    private AnimatorSet getDownLoadErrorAnimator() {
        final AnimatorSet errorSet = new AnimatorSet();
        ValueAnimator animator1 = ValueAnimator.ofFloat(0.2f, 0.8f).setDuration(300);
        animator1.setInterpolator(new OvershootInterpolator());
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mErrorLeftDegree = value;
                invalidate();
            }
        });

        ValueAnimator animator2 = ValueAnimator.ofFloat(0.2f, 0.8f).setDuration(300);
        animator2.setInterpolator(new OvershootInterpolator());
        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mErrorRightDegree = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        errorSet.play(animator1);
        errorSet.play(animator2).after(100);
        errorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                mIfShowError = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIfShowError = false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mIfShowError = true;
            }
        });
        return errorSet;
    }


    public float getProgressTextSize() {
        return mProgressTextSize;
    }

    public void setProgressTextSize(float mProgressTextSize) {
        this.mProgressTextSize = mProgressTextSize;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getCircularColor() {
        return circular_color;
    }

    public void setCircularColor(int circular_color) {
        this.circular_color = circular_color;
    }

    public int getProgressColor() {
        return circular_progress_color;
    }

    public void setProgressColor(int circular_progress_color) {
        this.circular_progress_color = circular_progress_color;
    }

    public float getCircularWidth() {
        return circular_width;
    }

    public void setCircularWidth(float circular_width) {
        this.circular_width = circular_width;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttached = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttached = false;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        FreshDownloadStatus fds = new FreshDownloadStatus(superState);
        fds.circular_color = this.circular_color;
        fds.circular_progress_color = this.circular_progress_color;
        fds.circular_width = this.circular_width;
        fds.progress = this.mProgress;
        fds.radius = this.radius;
        fds.status = this.status;
        fds.mProgressTextSize = this.mProgressTextSize;
        return fds;
    }


    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof FreshDownloadStatus)) {
            super.onRestoreInstanceState(state);
            return;
        }
        FreshDownloadStatus fds = (FreshDownloadStatus) state;
        this.circular_color = fds.circular_color;
        this.circular_progress_color = fds.circular_progress_color;
        this.circular_width = fds.circular_width;
        this.mProgress = fds.progress;
        this.radius = fds.radius;
        this.status = fds.status;
        this.mProgressTextSize = fds.mProgressTextSize;
    }

    static class FreshDownloadStatus extends AbsSavedState {
        public STATUS status;
        public float progress;
        public float radius;
        public int circular_color;
        public int circular_progress_color;
        public float circular_width;
        public float mProgressTextSize;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.status == null ? -1 : this.status.ordinal());
            dest.writeFloat(this.progress);
            dest.writeFloat(this.radius);
            dest.writeInt(this.circular_color);
            dest.writeInt(this.circular_progress_color);
            dest.writeFloat(this.circular_width);
            dest.writeFloat(this.mProgressTextSize);
        }

        public FreshDownloadStatus(Parcelable superState) {
            super(superState);
        }

        protected FreshDownloadStatus(Parcel in) {
            super(in);
            int tmpStatus = in.readInt();
            this.status = tmpStatus == -1 ? null : STATUS.values()[tmpStatus];
            this.progress = in.readFloat();
            this.radius = in.readFloat();
            this.circular_color = in.readInt();
            this.circular_progress_color = in.readInt();
            this.circular_width = in.readFloat();
            this.mProgressTextSize = in.readFloat();
        }

        public static final Creator<FreshDownloadStatus> CREATOR = new Creator<FreshDownloadStatus>() {
            @Override
            public FreshDownloadStatus createFromParcel(Parcel source) {
                return new FreshDownloadStatus(source);
            }

            @Override
            public FreshDownloadStatus[] newArray(int size) {
                return new FreshDownloadStatus[size];
            }
        };
    }


}
