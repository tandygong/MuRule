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

public class MyRuleView extends View{
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

    private float marginLeft = 0;
    private GestureDetector mDetector;
    private Scroller mScroller;

    private ScrollStopListener scrollStopListener;
    private int minUnitNum;


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
        mDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                mScroller.forceFinished(true);
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                
                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (!mScroller.computeScrollOffset()) {
                    mScroller.fling(getScrollX(), 0, -(int) (velocityX), 0, (int) (getMinUnit()/2*unitLineWidth), (int) (getMinUnit()/2*unitLineWidth), 0, 0);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                scrollBy((int) distanceX, 0);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });

    }

    public void setScrollStopListener(ScrollStopListener scrollStopListener) {
        this.scrollStopListener = scrollStopListener;
    }

    public ScrollStopListener getScrollStopListener() {
        return scrollStopListener;
    }




    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, "measuredWidth" + getMeasuredWidth());
    }


    private int getMinUnit(){
        if (minUnitNum ==0) {
            float halfWidth = 0.5f*getWidth();
            minUnitNum= 2*(int) Math.ceil(halfWidth / unitWidth);
        }

      return minUnitNum;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int minUnitHalfSide = getMinUnit();
        Log.e("minUnitHalfSize", minUnitHalfSide/2 + "");
        canvas.translate(getWidth()/2,0);
        paint.setStrokeWidth(childUnitLineWidth);
        paint.setColor(Color.GREEN);
        canvas.drawLine(getScrollX(), 0, getScrollX(), unitLineHeight + 40, paint);




        for (int i = 0; i < minUnitNum/2; i++) {
            for (int j = 0; j < childCountPerUnit; j++) {
                float startX;
                if (j == 0) {
                    paint.setStrokeWidth(unitLineWidth);
                    paint.setColor(Color.BLACK);
                    startX = marginLeft + i * unitWidth;
                    canvas.drawLine(startX, 0, startX, unitLineHeight, paint);
                    canvas.drawLine(-startX, 0, -startX, unitLineHeight, paint);
                    canvas.drawText(String.valueOf(i), startX, unitLineHeight + 20, paint);
                    //Log.e("drawLongLine", startX + "");
                } else if (j == childCountPerUnit/2) {
                    paint.setColor(Color.BLACK);
                    paint.setStrokeWidth(middleLineWidth);
                    startX = marginLeft + (i + (float) j / childCountPerUnit) * unitWidth;
                    canvas.drawLine(startX, 0, startX, middleLineHeight, paint);
                    canvas.drawLine(-startX, 0, -startX, middleLineHeight, paint);
                    // Log.e("drawMiddleLine", startX + "");
                } else {
                    paint.setColor(Color.BLACK);
                    paint.setStrokeWidth(childUnitLineWidth);
                    startX = marginLeft + (i + (float) j / childCountPerUnit) * unitWidth;
                    canvas.drawLine(startX, 0, startX, childUnitLineHeight, paint);
                    canvas.drawLine(-startX, 0, -startX, childUnitLineHeight, paint);
                    // Log.e("drawShortLine", startX + "");
                }
            }
        }


        // Log.e("drawShortLine", startX + "");

        super.onDraw(canvas);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            Log.e("computeScroll", "mScroller.getCurrX()="+mScroller.getCurrX());
        }else{
            if (scrollStopListener != null) {
                scrollStopListener.onScrollStop(String.valueOf(getId()),5,"10");
            }
        }
        super.computeScroll();
    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }





    interface  ScrollStopListener{
        void  onScrollStop(String rule,int pointPosition,String pointValue );
    }
}
