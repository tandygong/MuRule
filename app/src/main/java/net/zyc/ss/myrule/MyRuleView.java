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

/**
 * Created by 龚志星 on 2017/11/30 at 12:27
 */

public class MyRuleView extends View implements GestureDetector.OnGestureListener {
    public String TAG = "MyRuleView";
    private int childUnitNum = 10;
    private float unitWidth = 140f;

    private Paint paint;
    private float unitLineHeight = 60f;
    private float middleLineHeight = unitLineHeight * 2 / 3;
    private float childUnitLineHeight = unitLineHeight / 2;
    private float unitLineWidth = 4;
    private float middleLineWidth = 2;
    private float childUnitLineWidth = 1;

    private float marginLeft = 0;
    private GestureDetector mDetector;
    private Scroller mScroller;


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

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(0.7f);
        mScroller = new Scroller(getContext(), decelerateInterpolator);
        mDetector = new GestureDetector(getContext(), this);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, "measuredWidth" + getMeasuredWidth());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 10; j++) {
                float startX;
                if (j == 0) {
                    paint.setStrokeWidth(unitLineWidth);
                    paint.setColor(Color.BLACK);
                    startX = marginLeft + i * unitWidth;
                    canvas.drawLine(startX, 0, startX, unitLineHeight, paint);
                    canvas.drawText(String.valueOf(i), startX, unitLineHeight + 20, paint);
                    //Log.e("drawLongLine", startX + "");
                } else if (j == 5) {
                    paint.setColor(Color.BLACK);
                    paint.setStrokeWidth(middleLineWidth);
                    startX = marginLeft + (i + (float) j / childUnitNum) * unitWidth;
                    canvas.drawLine(startX, 0, startX, middleLineHeight, paint);
                   // Log.e("drawMiddleLine", startX + "");
                } else {
                    paint.setColor(Color.BLACK);
                    paint.setStrokeWidth(childUnitLineWidth);
                    startX = marginLeft + (i + (float) j / childUnitNum) * unitWidth;
                    canvas.drawLine(startX, 0, startX, childUnitLineHeight, paint);
                   // Log.e("drawShortLine", startX + "");
                }
            }
        }

        int currX = mScroller.getCurrX();
        paint.setStrokeWidth(childUnitLineWidth);
        paint.setColor(Color.GREEN);
        int startX = currX + getWidth() / 2;
        canvas.drawLine(startX, 0, startX, unitLineHeight, paint);
       // Log.e("drawShortLine", startX + "");

        super.onDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);

    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.e(TAG, "onDown");
        mScroller.forceFinished(true);
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.e(TAG, "distanceX:" + distanceX + "distanceY" + distanceY);
        // layout(getLeft()-(int)distanceX,getTop()-(int)distanceY,getRight()-(int)distanceX,getBottom()-(int)distanceY);
        //scrollBy((int) distanceX, 0);
        mScroller.startScroll(mScroller.getCurrX(),0, (int) distanceX, 0);
         invalidate();
        return false;
    }


    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.e(TAG, "velocityX:" + velocityX + "velocityY:" + velocityY);
        if (!mScroller.computeScrollOffset()) {
            mScroller.fling(getScrollX(), 0, -(int) (velocityX), 0, (int) (-50 * unitWidth), (int) (40 * unitWidth), 0, 0);

        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            float currVelocity = mScroller.getCurrVelocity();
           // Log.e("currVelocity", currVelocity + "");
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
        super.computeScroll();
    }
}
