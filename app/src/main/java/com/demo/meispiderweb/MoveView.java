package com.demo.meispiderweb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

/**
 * Created by wenshi on 2019/3/26.
 * Description
 */
public class MoveView extends View {

    // 画笔
    private Paint mPointPaint;
    // 蛛网点对象（类似小球）
    private SpiderPoint mSpiderPoint;
    // 坐标系
    private Point mCoordinate;

    // 蛛网点 默认小球半径
    private int pointRadius = 20;
    // 默认颜色
    private int pointColor = Color.RED;
    // 默认x方向速度
    private float pointVX = 10;
    // 默认y方向速度
    private float pointVY = 6;
    // 默认 小球加速度
    private int pointAX = 0;
    private int pointAY = 0;

    // 是否开始运动
    private boolean startMove = false;

    public MoveView(Context context) {
        this(context, null);
    }

    public MoveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
        initPaint();
    }

    private void initData() {
        mCoordinate = new Point(500, 500);
        mSpiderPoint = new SpiderPoint();
        mSpiderPoint.color = pointColor;
        mSpiderPoint.vX = pointVX;
        mSpiderPoint.vY = pointVY;
        mSpiderPoint.aX = pointAX;
        mSpiderPoint.aY = pointAY;
        mSpiderPoint.r = pointRadius;
    }

    // 初始化画笔
    private void initPaint() {
        mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointPaint.setColor(pointColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(mCoordinate.x, mCoordinate.y);
        drawSpiderPoint(canvas, mSpiderPoint);

        canvas.drawLine(400 + mSpiderPoint.r, -500, 400 + mSpiderPoint.r, getHeight(), mPointPaint);

        canvas.drawLine(-400 - mSpiderPoint.r, -500, -400 - mSpiderPoint.r, getHeight(), mPointPaint);

        canvas.drawLine(-400 - mSpiderPoint.r, -400, 400 + mSpiderPoint.r, -400, mPointPaint);

        canvas.drawLine(-400 - mSpiderPoint.r, 400, 400 + mSpiderPoint.r, 400, mPointPaint);

        canvas.restore();

        // 刷新视图 再次调用onDraw方法模拟时间流
        if (startMove) {
            updateBall();
            invalidate();
        }


    }

    /**
     * 绘制蛛网点
     *
     * @param canvas
     * @param spiderPoint
     */
    private void drawSpiderPoint(Canvas canvas, SpiderPoint spiderPoint) {
        mPointPaint.setColor(spiderPoint.color);
        canvas.drawCircle(spiderPoint.x, spiderPoint.y, spiderPoint.r, mPointPaint);
    }

    /**
     * 更新小球
     */
    private void updateBall() {
        //TODO --运动数据都由此函数变换
        mSpiderPoint.x += mSpiderPoint.vX;
        mSpiderPoint.y += mSpiderPoint.vY;
        if (mSpiderPoint.x > 400) {
            // 更改颜色
            mSpiderPoint.color = randomRGB();
            mSpiderPoint.vX = -mSpiderPoint.vX;
        }
        if (mSpiderPoint.x < -400) {
            mSpiderPoint.vX = -mSpiderPoint.vX;
            // 更改颜色
            mSpiderPoint.color = randomRGB();
        }

        if (mSpiderPoint.y > 400) {
            // 更改颜色
            mSpiderPoint.color = randomRGB();
            mSpiderPoint.vY = -mSpiderPoint.vY;
        }
        if (mSpiderPoint.y < -400) {
            mSpiderPoint.vY = -mSpiderPoint.vY;
            // 更改颜色
            mSpiderPoint.color = randomRGB();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 开启时间流
                startMove = true;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                // 暂停时间流
                // startMove = false;
                // invalidate();
                break;
        }
        return true;
    }

    /**
     * 两点间距离函数
     */
    public static int disPos2d(float x1, float y1, float x2, float y2) {
        return (int) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**
     * 获取范围随机整数：如 rangeInt(1,9)
     *
     * @param s 前数(包括)
     * @param e 后数(包括)
     * @return 范围随机整数
     */
    public static int rangeInt(int s, int e) {
        int max = Math.max(s, e);
        int min = Math.min(s, e) - 1;
        return (int) (min + Math.ceil(Math.random() * (max - min)));
    }

    /**
     * @return 获取到随机颜色值
     */
    private int randomRGB() {
        Random random = new Random();
        return Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }
}
