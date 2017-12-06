package net.zyc.ss.myrule;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
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
    public final static int MODE_DEFAULT_SELECTION_DISABLED = 0;//选中模式禁用-普通模式
    public final static int MODE_SELECTED = 1;//选中模式开启-选中状态
    public final static int MODE_UNSELECTED = 2;//选中模式开启-未选中状态
    public final static int MODE_FORBID_USE = 3;//控件禁用模式-不可滑动

    private static final int ACTION_SET = -1;
    private static final int ACTION_IDLE = 0;
    private static final int ACTION_DOWN = 1;
    private static final int ACTION_DRAG = 2;
    private static final int ACTION_FILING = 3;
    private static final int ACTION_CORRECT = 4;


    public final static int CORRECT_TYPE_FLOOR = 1;//向下取整:指针指向3.0-3.9则认为指针指向3
    public final static int CORRECT_TYPE_ROUND = 0;//四舍五入:指针指向2.5-3.4 认为是3,指针指向3.5-4.4认为是4

    private int childCountPerUnit = 10;
    private float unitWidth = 140f;
    private Paint paint;
    private Paint shaderPaint;
    private float longTickHeight = 60f;
    private float middleTickHeight = longTickHeight * 3 / 4;
    private float showTickHeight = longTickHeight * 2 / 3;
    private float longTickWidth = 4;
    private float middleTickWidth = 3;
    private float shortTickWidth = 2;
    private int textMarginTick = 30;
    private int textColor;//字体颜色
    private int tickColor;//度线颜色
    private int pointColor;//指针颜色
    private Shader mRadialGradient;
    private int overDragUnitCount = 3;//拖拽尺子时,指针能够指向超过有效刻度的最大单元个数
    private int overFlingUnitCount = 4;//filing尺子时,指针能够指向指向超过有效刻度的最大单元个数
    private boolean autoCorrect = true;//开启自动校正
    private GestureDetector mDetector;
    private Scroller mScroller;

    private List<String> dataList;

    private ScrollStopListener scrollStopListener;
    private int needFillUnitCountPerSize;
    private boolean showMiddleLine = true;//是否突出显示中线
    private int pointIndex;
    private boolean pointDefault = true;
    private int action = ACTION_IDLE;
    private int autoCorrectType = CORRECT_TYPE_FLOOR;//默认采用向下取整
    public int mSelectedColor = 0xAA000000;
    public int mUnSelectedColor = 0xCC3C3C3C;
    private int mode = -1;

    public MyRuleView(Context context) {
        this(context, null, 0);
        init();
    }

    public MyRuleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyRuleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MyRuleView, 0, 0);
        String ruleName = typedArray.getString(R.styleable.MyRuleView_ruleName);//not use
        pointColor = typedArray.getColor(R.styleable.MyRuleView_pointColor, Color.parseColor("#0BB88D"));
        textColor = typedArray.getColor(R.styleable.MyRuleView_textColor, Color.WHITE);
        tickColor = typedArray.getColor(R.styleable.MyRuleView_tickColor, Color.WHITE);
        typedArray.recycle();
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyRuleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        invalidate();
    }

    public int getTextColor() {
        return textColor;
    }

    public int getTickColor() {
        return tickColor;
    }

    public void setTickColor(int tickColor) {
        this.tickColor = tickColor;
        invalidate();
    }

    public int getPointColor() {
        return pointColor;
    }

    public void setPointColor(int pointColor) {
        this.pointColor = pointColor;
        invalidate();
    }

    public void setMode(int mode) {
        if (this.mode != mode) {
            this.mode = mode;
            applyNewMode(mode);
        }
    }

    public int getMode() {
        return mode;
    }

    private void applyNewMode(int mode) {
        Log.e("applyNewModel", mode + "");
        switch (mode) {
            case MODE_DEFAULT_SELECTION_DISABLED:
                setBackgroundColor(mSelectedColor);
                setAlpha(1.0f);
                break;
            case MODE_SELECTED:
                setAlpha(1.0f);
                setBackgroundColor(mSelectedColor);
                break;
            case MODE_UNSELECTED:
                setBackgroundColor(mUnSelectedColor);
                setAlpha(1.0f);
                break;
            case MODE_FORBID_USE:
                setBackgroundColor(0xffffff);
                setAlpha(0.3f);
                break;

        }
        invalidate();
    }


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
        return result + 20;
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
        action = ACTION_CORRECT;
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
        if (dataList == null || dataList.size() == 0) {
            return;
        }
        canvas.translate(getWidth() / 2, 0);
        if (mode == MODE_DEFAULT_SELECTION_DISABLED || mode == MODE_SELECTED) {
            getRadialGradient();
            shaderPaint.setShader(mRadialGradient);
            canvas.drawRect(getScrollX() - getWidth() / 2, 0, getScrollX() + getWidth() / 2, getHeight(), shaderPaint);
        }


        for (int i = 0; i < dataList.size() + 2 * getFillCount(); i++) {
            for (int j = 0; j < childCountPerUnit; j++) {
                float startX = (i + (j * 1f) / childCountPerUnit) * unitWidth;
                if (willDrawMiddleLine() && j == childCountPerUnit / 2) {//middle line
                    paint.setColor(tickColor);
                    paint.setStrokeWidth(middleTickWidth);
                    canvas.drawLine(startX, 0, startX, middleTickHeight, paint);
                    // Log.e("drawMiddleLine", startX + "");
                } else if (j == 0) {//long line
                    paint.setStrokeWidth(longTickWidth);
                    paint.setColor(tickColor);
                    canvas.drawLine(startX, 0, startX, longTickHeight, paint);

                    paint.setColor(textColor);
                    paint.setTextAlign(Paint.Align.CENTER);
                    //  paint.setTextSize(24);
                    // canvas.drawText((String.valueOf(i)), startX, longTickHeight + 20, paint);
                    paint.setTextSize(30);
                    if (getFillCount() - 1 < i && i < dataList.size() + getFillCount()) {
                        canvas.drawText(dataList.get(i - getFillCount()), startX, longTickHeight + textMarginTick, paint);
                    }
                    if (i == dataList.size() + 2 * getFillCount() - 1) {
                        break;
                    }
                } else {
                    paint.setColor(tickColor);
                    paint.setStrokeWidth(shortTickWidth);
                    canvas.drawLine(startX, 0, startX, showTickHeight, paint);
                    // Log.e("drawShortLine", startX + "");
                }
            }
        }
        //draw point line
        paint.setStrokeWidth(middleTickWidth);
        paint.setColor(pointColor);
        canvas.drawLine(getScrollX(), 0, getScrollX(), longTickHeight + textMarginTick, paint);

        if (pointDefault) {
            pointIndex = getFillCount() + dataList.size() / 2;
            scrollToIndex(pointIndex, true);
            pointDefault = false;
        }

        pointIndex = (int) (getScrollX() / unitWidth);
        Log.d("index", pointIndex + ";scroll:" + getScrollX());
        super.onDraw(canvas);
    }

    private void getRadialGradient() {
        mRadialGradient = new RadialGradient(
                getScrollX() + 35,
                getHeight() / 2 - 10,
                getWidth() / 2,
                new int[]{0x80FFFFFF, 0x50000000},
                new float[]{0.05f, 0.8f},
                Shader.TileMode.CLAMP);
    }

    private void init() {
        setMode(MODE_DEFAULT_SELECTION_DISABLED);
        setAutoCorrect(true);
        setAutoCorrectType(CORRECT_TYPE_FLOOR);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        shaderPaint = new Paint();

        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(0.7f);
        mScroller = new Scroller(getContext(), decelerateInterpolator);
        mDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
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
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                action = ACTION_DRAG;
                float rightMax = (getFillCount() + dataList.size() - 1) * unitWidth;
                float leftMin = getFillCount() * unitWidth;
                if (getScrollX() + distanceX > rightMax) {
                    float f = (getScrollX() - rightMax) * 1f / overDragUnitCount / unitWidth;
                    scrollBy((int) ((1 - f * f) * (distanceX)), 0);
                } else if (getScrollX() + distanceX < leftMin) {
                    float f = (getScrollX() - leftMin) * 1f / overDragUnitCount / unitWidth;
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
            // Log.e("computeScroll", "mScroller.getCurrX()=" + mScroller.getCurrX() + "getScrollX()" + getScrollX());
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
                case ACTION_CORRECT:
                    float v = (getScrollX() % unitWidth) / unitWidth;
                    int index = (int) (getScrollX() / unitWidth);
                    onStopScroll(autoCorrectType == CORRECT_TYPE_ROUND && v >= 0.5f ? index + 1 : index);
                    action = ACTION_IDLE;
                    break;
                case ACTION_DOWN:
                    autoCorrect();
                    break;

            }
        }
        super.computeScroll();
    }

    private void onStopScroll(int pointIndex) {
        if (scrollStopListener != null) {
            if (pointIndex >= getFillCount() && pointIndex < dataList.size() + getFillCount()) {
                scrollStopListener.onScrollStop(this, pointIndex - getFillCount(), dataList.get(pointIndex - getFillCount()));
            }
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "onStopScroll: pointIndex" + pointIndex + " value=" + dataList.get(pointIndex - getFillCount()));
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean b = mDetector.onTouchEvent(event);//手势必须在onTouchEvent前面判断,这样才能区分是
        // on scroll-> on filing->action up 还是on scroll->action up
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // Log.e(TAG, "ACTION_DOWN");
                if (mode == MODE_UNSELECTED) {
                    setMode(MODE_SELECTED);
                }
                if (action == ACTION_FILING) {
                    mScroller.forceFinished(true);
                }
                action = ACTION_DOWN;

                break;
            case MotionEvent.ACTION_UP:
                if (action == ACTION_DRAG) {
                    autoCorrect();//手指离开view
                }
                // Log.e(TAG, "ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                if (action == ACTION_DRAG) {
                    autoCorrect();//action cancel
                }
                // Log.e(TAG, "ACTION_CANCEL");
                break;

        }

        return b;//super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return !(mode == MODE_FORBID_USE || mode == MODE_UNSELECTED) && super.dispatchTouchEvent(event);
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
        action = ACTION_SET;
        pointDefault = false;
        if (dataList == null) {
            return;
        }
        if (pointPos >= 0 && pointPos < dataList.size()) {
            this.pointIndex = getFillCount() + pointPos;
            scrollToIndex(pointIndex, immediately);
        } else {
            Log.e(TAG, "setPointPos=" + pointPos + "error");
        }
    }

    public void setPointPos(int pointPos) {
        setPointPos(pointPos, true);
    }

    public String getPointValue() {
        if (dataList != null) {
            return dataList.get(getPointPos());
        }
        return "";

    }

    public interface ScrollStopListener {
        void onScrollStop(MyRuleView rule, int pointPosition, String pointValue);
    }
}
