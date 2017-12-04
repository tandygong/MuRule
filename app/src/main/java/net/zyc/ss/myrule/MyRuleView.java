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
    /**
     * 选中模式禁用-普通模式
     */
    public final static int MODE_DEFAULT_SELECTION_DISABLED = -1;
    /**
     * 选中模式开启-选中状态
     */
    public final static int MODE_SELECTED = 0;
    /**
     * 选中模式开启-未选中状态
     */
    public final static int MODE_UNSELECTED = 1;
    /**
     * 控件禁用模式-不可滑动
     */
    public final static int MODE_FORBID_USE = 2;

    private int mode = MODE_DEFAULT_SELECTION_DISABLED;


    /**
     * 拖拽尺子时,指针能够指向超过有效刻度的最大单元个数
     */
    private int overDragUnitCount = 3;
    /**
     * filing尺子时,指针能够指向指向超过有效刻度的最大单元个数
     */
    private int overFlingUnitCount = 4;
    /**
     * 开启自动校正
     */
    private boolean autoCorrect = true;
    private GestureDetector mDetector;
    private Scroller mScroller;

    private List<String> dataList;

    private ScrollStopListener scrollStopListener;
    private int needFillUnitCountPerSize;
    /**
     * 是否突出显示中线
     */
    private boolean showMiddleLine = true;
    private int pointIndex;
    private boolean pointDefault = true;

    private static final int ACTION_IDLE = 0;
    private static final int ACTION_DRAG = 1;
    private static final int ACTION_FILING = 2;
    private static final int ACTION_SET_OR_CORRECT = 3;
    private int action = ACTION_IDLE;
    /**
     * 向下取整:指针指向3.0-3.9则认为指针指向3
     */
    public final static int CORRECT_TYPE_FLOOR = 1;
    /**
     * 四舍五入:指针指向2.5-3.4 认为是3,指针指向3.5-4.4认为是4
     */
    public final static int CORRECT_TYPE_ROUND = 0;
    /**
     * 默认采用向下取整
     */
    private int autoCorrectType = CORRECT_TYPE_FLOOR;


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

    public void setMode(int mode) {
        if (this.mode != mode) {
            this.mode = mode;
            //applyNewMode(mode);
        }
    }

    /*private void applyNewMode(int mode) {
        switch (mode) {
            case MODE_DEFAULT_SELECTION_DISABLED:
                //pointer.setTextColor(Color.BLUE);
                ruleNameText.setTextColor(Color.WHITE);
                pointerValueText.setTextColor(Color.WHITE);
                setAlpha(1.0f);
                break;
            case MODE_SELECTED:
                //pointer.setTextColor(Color.BLUE);
                ruleNameText.setTextColor(Color.parseColor("#01ade5"));
                pointerValueText.setTextColor(Color.parseColor("#01ade5"));
                setAlpha(1.0f);
                setBackgroundColor(mSelectedColor);
                break;
            case MODE_UNSELECTED:
                //pointer.setTextColor(Color.WHITE);
                ruleNameText.setTextColor(Color.WHITE);
                pointerValueText.setTextColor(Color.WHITE);
                setBackground(defaultColor);
                setAlpha(1.0f);
                break;
            case MODE_FORBID_USE:
                // pointer.setTextColor(Color.WHITE);
                pointerValueText.setTextColor(Color.WHITE);
                ruleNameText.setTextColor(Color.WHITE);
                setAlpha(0.3f);
                break;
        }
    }*/


    public void setScrollStopListener(ScrollStopListener scrollStopListener) {
        this.scrollStopListener = scrollStopListener;
    }

    public void setOverDragUnitCount(int overDragUnitCount) {
        this.overDragUnitCount = overDragUnitCount;
    }

    public List<String> getDataList() {
        return dataList;
    }

    public void setDataList(List<String> dataList) {
        this.dataList = dataList;
        pointDefault = true;
        invalidate();
    }


    public boolean isAutoCorrect() {
        return autoCorrect;
    }

    /**
     * 设置自动校正是否开启
     *
     * @param autoCorrect 开启时,会自动指向整数位
     */
    public void setAutoCorrect(boolean autoCorrect) {
        this.autoCorrect = autoCorrect;
    }

    public void setAutoCorrectType(int autoCorrectType) {
        this.autoCorrectType = autoCorrectType;
    }

    public int getAutoCorrectType() {
        return autoCorrectType;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
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
     * 在左右两边填充空白单元以使最左/右的有效刻度单元能够移动到指针位置
     *
     * @return 每边附加的空白单元个数
     */
    private int getFillCount() {
        if (needFillUnitCountPerSize == 0) {
            float halfWidth = 0.5f * getWidth();
            needFillUnitCountPerSize = (int) Math.ceil(halfWidth / unitWidth) + overDragUnitCount;
            pointIndex = dataList.size() / 2 + needFillUnitCountPerSize - 1;
        }
        return needFillUnitCountPerSize;
    }


    private void scrollToIndex(int destIndex, boolean immediately) {
        if (immediately)
            scrollTo((int) (destIndex * unitWidth), 0);
        else {
            int dx = (int) (destIndex * unitWidth - getScrollX());
            mScroller.startScroll(getScrollX(), 0, dx, 0);
            invalidate();
            Log.e(TAG, "scrollToIndex: destIndex" + destIndex + " destX " + destIndex * unitWidth);
        }

    }


    private void autoCorrect() {
        action = ACTION_SET_OR_CORRECT;
        float v = (getScrollX() % unitWidth) / unitWidth;
        int index = (int) (getScrollX() / unitWidth);
        Log.e(TAG, "autoCorrect: v" + v + " index:" + index);
        if (index < getFillCount()) {
            scrollToIndex(getFillCount(), false);
        } else if (index >= getFillCount() + dataList.size() - 1) {
            scrollToIndex(getFillCount() + dataList.size() - 1, false);
        } else {
            if (autoCorrect) {
                scrollToIndex(autoCorrectType == CORRECT_TYPE_ROUND && v >= 0.5f ? index + 1 : index, false);
            } else {
                onStopScroll(autoCorrectType == CORRECT_TYPE_ROUND && v >= 0.5f ? index + 1 : index);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw");
        if (dataList == null || dataList.size() == 0) {
            return;
        }

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
                    paint.setStrokeWidth(unitLineWidth);
                    paint.setColor(Color.BLACK);
                    canvas.drawLine(startX, 0, startX, unitLineHeight, paint);

                    paint.setColor(Color.BLACK);
                    paint.setTextAlign(Paint.Align.CENTER);
                    paint.setTextSize(24);
                    canvas.drawText((String.valueOf(i)), startX, unitLineHeight + 20, paint);
                    paint.setTextSize(40);
                    if (getFillCount() - 1 < i && i < dataList.size() + getFillCount()) {
                        canvas.drawText(dataList.get(i - getFillCount()), startX, unitLineHeight + 60, paint);
                    }
                    if (i == dataList.size() + 2 * getFillCount() - 1) {
                        break;
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
        setAutoCorrect(true);
        setAutoCorrectType(CORRECT_TYPE_FLOOR);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(0.7f);
        mScroller = new Scroller(getContext(), decelerateInterpolator);
        mDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                Log.e(TAG, "onDown");
                if (action != ACTION_SET_OR_CORRECT) {
                    mScroller.forceFinished(true);
                }
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.e(TAG, "onFling" + velocityX);
                action = ACTION_FILING;
                if (!mScroller.computeScrollOffset()) {
                    Log.e(TAG, "mScroller filing ");
                    mScroller.fling(getScrollX(), 0, -(int) (velocityX), 0,
                            0,
                            (int) ((dataList.size() + 2 * getFillCount() - 1) * unitWidth), -10000, 10000);
                    invalidate();
                } else {
                    Log.e(TAG, "onFling: computeScrollOffset and autoCorrect");
                    autoCorrect();//onFling完成了滑动过程
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                action = ACTION_DRAG;
                float rightMax = (getFillCount() + dataList.size() - 1) * unitWidth;
                float leftMin = getFillCount() * unitWidth;
                if (getScrollX() + distanceX > rightMax) {
                    float f = (getScrollX() - rightMax) * 1f / overDragUnitCount / unitWidth;
                    Log.e(TAG, "onScroll: f_right=" + f);
                    scrollBy((int) ((1 - f * f) * (distanceX)), 0);
                } else if (getScrollX() + distanceX < leftMin) {
                    float f = (getScrollX() - leftMin) * 1f / overDragUnitCount / unitWidth;
                    Log.e(TAG, "onScroll: f_left=" + f);
                    scrollBy((int) ((1 - f * f) * (distanceX)), 0);
                } else {
                    scrollBy((int) distanceX, 0);
                }
                return true;
            }

        });
    }


    @Override
    public void computeScroll() {

        if (mScroller.computeScrollOffset()) {
            Log.e("computeScroll", "mScroller.getCurrX()=" + mScroller.getCurrX() + "getScrollX()" + getScrollX());
            if (getScrollX() == mScroller.getCurrX() && getScrollY() == mScroller.getCurrY()) {
                Log.e(TAG, "scroll not execute");
                // mScroller.forceFinished(true);
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                invalidate();
            } else {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }

        } else {
            Log.e(TAG, "computeScroll: switchAction" + action);
            switch (action) {
                case ACTION_FILING:
                    autoCorrect();
                    break;
                case ACTION_IDLE:
                    break;
                case ACTION_DRAG:
                    break;
                case ACTION_SET_OR_CORRECT:
                    float v = (getScrollX() % unitWidth) / unitWidth;
                    int index = (int) (getScrollX() / unitWidth);
                    onStopScroll(autoCorrectType == CORRECT_TYPE_ROUND && v >= 0.5f ? index + 1 : index);
                    break;


            }
        }
        super.

                computeScroll();
    }

    private void onStopScroll(int pointIndex) {
        if (scrollStopListener != null) {
            if (pointIndex >= getFillCount() && pointIndex < dataList.size() + getFillCount()) {
                scrollStopListener.onScrollStop(String.valueOf(getId()), pointIndex - getFillCount(), dataList.get(pointIndex - getFillCount()));
            }
            Log.e(TAG, "onStopScroll: pointIndex" + pointIndex);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean b = mDetector.onTouchEvent(event);//手势必须在onTouchEvent前面判断,这样才能区分是
        // on scroll-> on filing->action up 还是on scroll->action up
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "ACTION_DOWN");
                break;
            case MotionEvent.ACTION_UP:
                if (action == ACTION_DRAG) {
                    autoCorrect();
                }
                Log.e(TAG, "ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.e(TAG, "ACTION_CANCEL");
                break;

        }

        return b;//super.onTouchEvent(event);
    }

    /**
     * {@link #childCountPerUnit}值为奇数时不突出显示中线,否则根据用户设置的值{@link #showMiddleLine}来决定是否显示中线
     *
     * @return 是否会突出显示中线
     */
    public boolean willDrawMiddleLine() {
        return showMiddleLine && childCountPerUnit % 2 == 0;
    }

    /**
     * 设置是否突出显示中线,如果设置为true,在{@link #childCountPerUnit}为值双数的时候将会突出显示,否则不会突出显示
     *
     * @param showMiddleLine 是否显示
     */
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
     * @param pointPos 要指向数据所在list的位置
     */
    public void setPointPos(int pointPos) {
        setPointPos(pointPos, true);
    }


    /**
     * 手指离开view,且view 停止滑动的监听
     */
    interface ScrollStopListener {
        /**
         * @param rule          rule 的名字或者id
         * @param pointPosition 指针指向的数据所在list的位置
         * @param pointValue    指针指向的值
         */
        void onScrollStop(String rule, int pointPosition, String pointValue);
    }
}
