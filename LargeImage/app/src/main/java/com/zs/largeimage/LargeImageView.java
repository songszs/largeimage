package com.zs.largeimage;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by user on 2016/9/28.
 */
public class LargeImageView extends ImageView
{

    private BitmapFactory.Options decodeImgOptions;
    private BitmapRegionDecoder mRegionDecoder;
    private Rect decodeRect;
    private int imgIntrinsicWidth;
    private int imgIntrinsicHeight;

    private int viewWidth;
    private int viewHeight;
    private Rect imgIntrinsicRect;

    private float VIEW_TOUCH_FLOP;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    private Matrix matrix, prevMatrix;
    private float matchViewWidth, matchViewHeight;


    public LargeImageView(Context context)
    {
        super(context);
        init(context);
    }

    public LargeImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public LargeImageView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LargeImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context)
    {
        VIEW_TOUCH_FLOP = ViewConfiguration.get(context)
                                           .getScaledTouchSlop();

        gestureDetector = new GestureDetector(context,new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context,new ScaleGestureListener());

        matrix = new Matrix();
        prevMatrix = new Matrix();
//        setImageMatrix(matrix);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    public void setInputStream(InputStream in)
    {
        try
        {
            mRegionDecoder = BitmapRegionDecoder.newInstance(in, false);

            //压缩图片
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, opt);
            imgIntrinsicWidth = opt.outWidth;
            imgIntrinsicHeight = opt.outHeight;
            imgIntrinsicRect = new Rect(0, 0, imgIntrinsicWidth, imgIntrinsicHeight);
            int simpleSize = getSimpleSize(opt.outWidth, opt.outHeight);
            decodeImgOptions = new BitmapFactory.Options();
            //            decodeImgOptions.inSampleSize = 0;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();
        initDecodeRect();
    }

    /**
     * 计算图片需要压缩的比例
     *
     * @param imgIntrinsicWidth  显示图片的固有宽
     * @param imgIntrinsicHeight 显示图片的固有高
     */
    private int getSimpleSize(int imgIntrinsicWidth, int imgIntrinsicHeight)
    {
        int simpleSize = 2;

        //获取view可显示的宽高
        int viewWidth = getMeasuredWidth();
        int viewHeight = getMeasuredHeight();

        return simpleSize;
    }


    @Override
    protected void onDraw(Canvas canvas)
    {

//        canvas.drawBitmap(bitmap, 0, 0, null);
//        canvas.setMatrix(matrix);

        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    /**
     * 计算要加载的图片区域
     *
     * @param dx x轴的增量
     * @param dy y轴的增量
     * @return 加载区域是否改变 true为改变
     */
    private boolean calcDecodeRect(float dx, float dy)
    {
        int resultDx;
        int resultDy;
        boolean unchange;
        Rect result = new Rect();
        result.set(decodeRect);

        //平移x，如果区域符合平移，否则不平移
        result.offset((int) dx, 0);
        resultDx = result.left >= imgIntrinsicRect.left && result.right <= imgIntrinsicRect.right ? (int) dx : 0;
        decodeRect.offset(resultDx, 0);

        //平移y，如果区域符合平移，否则不平移
        result.offset(0, (int) dy);
        resultDy = result.top >= imgIntrinsicRect.top && result.bottom <= imgIntrinsicRect.bottom ? (int) dy : 0;
        decodeRect.offset(0, resultDy);

        unchange = resultDx == 0 && resultDy == 0;
        return !unchange;

    }

    private void initDecodeRect()
    {
        if(decodeRect == null)
        {
            int decodeWidth = Math.min(viewWidth,imgIntrinsicWidth);
            int decodeHeight = Math.min(viewHeight,imgIntrinsicHeight);
            decodeRect = new Rect(0, 0, decodeWidth, decodeHeight);
            fitImageToView();
            setScaleType(ScaleType.MATRIX);
        }
    }

    private class GestureListener implements  GestureDetector.OnGestureListener
    {

        @Override
        public boolean onDown(MotionEvent motionEvent)
        {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent)
        {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent)
        {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent start, MotionEvent end, float dx,
                                float dy)
        {
            //move事件是否有效
            dx = Math.abs(dx) < VIEW_TOUCH_FLOP ? 0 : dx ;
            dy = Math.abs(dy) < VIEW_TOUCH_FLOP ? 0 : dy ;
            if (calcDecodeRect(dx, dy))
            {
                draw();
                return true;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent)
        {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1)
        {
            return false;
        }
    }

    private class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener
    {

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector)
        {
            scaleGestureDetector.getScaleFactor();
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector)
        {
            return false;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector)
        {

        }
    }

    private void fitImageToView()
    {

        float scale = 0f;
        float scaleX = (float) imgIntrinsicWidth/viewWidth;
        float scaleY = (float) imgIntrinsicHeight/viewHeight;

        //空白区域
        float redundantXSpace = viewWidth -  decodeRect.right;
        float redundantYSpace = viewHeight - decodeRect.bottom;
        matchViewWidth = viewWidth - redundantXSpace;
        matchViewHeight = viewHeight - redundantYSpace;

        //如果图长宽小
        if(scaleY>=1 && scaleX<=1)
        {
            scale = scaleX+1;
        }


        matrix.preTranslate(viewHeight / 2,viewHeight/2);
        matrix.setScale(scale, scale);
        matrix.postTranslate(-viewHeight / 2,-viewHeight/2);
        setImageMatrix(matrix);
        draw();

    }

    private void draw()
    {
        Bitmap bitmap = mRegionDecoder.decodeRegion(decodeRect, decodeImgOptions);
        setImageBitmap(bitmap);
    }

}
