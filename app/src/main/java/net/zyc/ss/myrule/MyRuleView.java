package net.zyc.ss.myrule;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import java.util.List;

/**
 * Created by 龚志星 on 2017/11/30 at 12:27
 */

public class MyRuleView extends View {
    public String TAG = "MyRuleView";
    private int childCountPerUnit = 10;
    private float unitWidth = 140f;

    private Paint paint;
    private float unitLineHeight = 60f;
    private float middleLineHeight = unitLineHeight * 2 / 3;
    private float childUnitLineHeight = unitLineHeight / 2;
    private float unitLineWidth = 6;
    private float middleLineWidth = 4;
    private float childUnitLineWidth = 2;

    private GestureDetector mDetector;
    private Scroller mScroller;

    private List<String> dataList;

    private ScrollStopListener scrollStopListener;
    private int needFillUnitCountPerSize;
    private boolean showMiddleLine = true;
    private int pointIndex;
    private boolean pointDefault = true;

    private static final int ACTION_IDLE = 0;
    private static final int ACTION_DRAG = 1;
    private static final int ACTION_FILING = 2;
    private static final int ACTION_SET_OR_CORRECT = 3;
    private int action = ACTION_IDLE;


    public MyRuleView(Context context) {
        super(context);
        init();
    }

    public MyRuleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyRuleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyRuleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    public void setScrollStopListener(ScrollStopListener scrollStopListener) {
        this.scrollStopListener = scrollStopListener;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
       int width = measureWidth(widthMeasureSpec);
         int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width,height);
        Log.e(TAG, "measuredWidth" + getMeasuredWidth());
    }

    private int measureWidth(int measureSpec) {
        int result = 200;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(result, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 200;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(result, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }


    /**
     * 在左右两边填充空白单元以使最左/又的有效单元能够移动到指针位置
     *
     * @return 每边附加的空白单元个数
     */
    private int getFillCount() {
        if (needFillUnitCountPerSize == 0) {
            float halfWidth = 0.5f * getWidth();
            needFillUnitCountPerSize = (int) Math.ceil(halfWidth / unitWidth);
            pointIndex = dataList.size() / 2 + needFillUnitCountPerSize - 1;
        }
        return needFillUnitCountPerSize;
    }

    public List<String> getDataList() {
        return dataList;
    }

    public void setDataList(List<String> dataList) {
        this.dataList = dataList;
        pointDefault = true;
        invalidate();
    }

    private void scrollToIndex(int destIndex, boolean immediately) {
        if (immediately)
            scrollTo((int) (destIndex * unitWidth), 0);
        else {
            int dx = (int) (destIndex * unitWidth - getScrollX());
            mScroller.startScroll(getScrollX(), 0, dx, 0);
            invalidate();
        }
    }


    private void corret() {
        float v = (getWidth() % unitWidth) / unitWidth;
        int index = (int) (getScrollX() / unitWidth);
        if (v >= 0.5f) {
            scrollToIndex(index + 1, false);
        } else {
            scrollToIndex(index, false);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw");
        if (dataList == null) {
            return;
        }
        paint.setTextSize(40);
        canvas.translate(getWidth() / 2, 0);
        for (int i = 0; i < dataList.size() + 2 * getFillCount(); i++) {
            for (int j = 0; j < childCountPerUnit; j++) {
                float startX = (i + (j * 1f) / childCountPerUnit) * unitWidth;
                if (willDrawMiddleLine() && j == childCountPerUnit / 2) {//middle line
                    paint.setColor(Color.BLACK);
                    paint.setStrokeWidth(middleLineWidth);
                    canvas.drawLine(startX, 0, startX, middleLineHeight, paint);
                    // Log.e("drawMiddleLine", startX + "");
                } else if (j == 0) {//long line
                    if (i != 0) {
                        paint.setStrokeWidth(unitLineWidth);
                        paint.setColor(Color.BLACK);
                        canvas.drawLine(startX, 0, startX, unitLineHeight, paint);
                    }
                    paint.setColor(Color.BLACK);
                   // canvas.drawText((String.valueOf(i)), startX, unitLineHeight + 10, paint);
                    if (getFillCount() - 1 < i && i < dataList.size() + getFillCount()) {
                        canvas.drawText(dataList.get(i - getFillCount()), startX, unitLineHeight + 40, paint);
                    }
                } else {
                    paint.setColor(Color.BLACK);
                    paint.setStrokeWidth(childUnitLineWidth);
                    canvas.drawLine(startX, 0, startX, childUnitLineHeight, paint);
                    // Log.e("drawShortLine", startX + "");
                }
            }
        }
        //draw point line
        paint.setStrokeWidth(childUnitLineWidth);
        paint.setColor(Color.GREEN);
        canvas.drawLine(getScrollX(), 0, getScrollX(), unitLineHeight + 40, paint);

        if (pointDefault) {
            pointIndex = getFillCount() + dataList.size() / 2;
            scrollToIndex(pointIndex, true);
            pointDefault = false;
        }

        pointIndex = (int) (getScrollX() / unitWidth);

        Log.d("index", pointIndex + ";scroll:" + getScrollX());
        super.onDraw(canvas);
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(0.7f);
        mScroller = new Scroller(getContext(), decelerateInterpolator);
        mDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                mScroller.forceFinished(true);
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.e("onSingleTab", "onS");
                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.e(TAG, "onFling" + velocityX);
                action = ACTION_FILING;
                if (!mScroller.computeScrollOffset()) {
                    mScroller.fling(getScrollX(), 0, -(int) (velocityX), 0,
                            0,
                            (int) ((dataList.size() + 2 * getFillCount()) * unitWidth), -10000, 10000);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                action = ACTION_DRAG;
                Log.e(TAG, "onScroll");
                float rightMax = (2 * getFillCount() + dataList.size()) * unitWidth;
                if (getScrollX() + distanceX < rightMax && getScrollX() + distanceX >= 0) {
                    scrollBy((int) distanceX, 0);
                }
                return true;
            }

        });
    }


    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            // Log.e("computeScroll", "mScroller.getCurrX()=" + mScroller.getCurrX());
        } else {

            switch (action) {
                case ACTION_FILING:
                    onStopScroll();
                    action = ACTION_IDLE;
                    break;
                case ACTION_IDLE:
                    break;
                case ACTION_DRAG:
                    break;


            }
            //  corret();
        }
        super.computeScroll();
    }

    private void onStopScroll() {
        if (scrollStopListener != null) {
            if (pointIndex >= getFillCount() && pointIndex < dataList.size() + getFillCount()) {
                scrollStopListener.onScrollStop(String.valueOf(getId()), pointIndex - getFillCount(), dataList.get(pointIndex - getFillCount()));
            }
            Log.e(TAG, "onStopScroll: pointIndex"+pointIndex );
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "ACTION_DOWN");
                break;
            case MotionEvent.ACTION_UP:
                if (action == ACTION_DRAG) {
                    onStopScroll();
                }
                Log.e(TAG, "ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.e(TAG, "ACTION_CANCEL");
                break;

        }
        boolean b = mDetector.onTouchEvent(event);//手势必须在onTouchEvent前面判断,这样才能区分是
        // on scroll-> on filing->action up 还是on scroll->action up
        return b;//super.onTouchEvent(event);
    }

    /**
     * 每个单元子单元个数为奇数时不显示中线,否则根据用户设置来决定是否显示中线
     *
     * @return
     */
    public boolean willDrawMiddleLine() {
        return showMiddleLine && childCountPerUnit % 2 == 0;
    }

    public void setShowMiddleLine(boolean showMiddleLine) {
        this.showMiddleLine = showMiddleLine;
    }

    /**
     * @return 指针指向的数据所在list的位置
     */
    public int getPointPos() {
        return pointIndex - getFillCount();
    }

    /**
     * @param pointPos    要指向数据所在list的位置
     * @param immediately 是否立刻
     */
    public void setPointPos(int pointPos, boolean immediately) {
        action = ACTION_SET_OR_CORRECT;
        if (pointPos >= 0 && pointPos < dataList.size()) {
            this.pointIndex = getFillCount() + pointPos;
            scrollToIndex(pointIndex, immediately);
        } else {
            Log.e(TAG, "setPointPos=" + pointPos + "error");
        }
    }

    /**
     * 立刻移动到指定位置
     *
     * @param pointPos
     */
    public void setPointPos(int pointPos) {
        setPointPos(pointPos, true);
    }

    interface ScrollStopListener {
        void onScrollStop(String rule, int pointPosition, String pointValue);
    }
}
