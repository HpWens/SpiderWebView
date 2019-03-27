# SpiderWebView

### 前言
《都挺好》迎来了大结局，相信看哭了很多人。在大结局中，所有之前让人气的牙痒痒的人设，比如 “你们太让我失望” 的苏明哲，还有妈宝男苏明成，包括一天不作就难受的苏大强，最终都成功洗白。一家人最终化解恩怨，和和气气的过日子。还有谁也喜欢《都挺好》这部剧吗？

在剧中，苏明哲同我们一样也是一名程序员，一味地迁就老爹，搞得最后差点与老婆离婚，看来程序员不能一根筋啊。转变下思维来看看网页版动态背景「五彩蛛网」是怎么实现的？

先来看看效果图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190326164627600.gif)![在这里插入图片描述](https://img-blog.csdnimg.cn/20190326164634380.gif)
### 初步分析

在效果图中，可以看到许多「小点」在屏幕中匀速运动并与「邻近的点」相连，每条连线的颜色随机，「小点」触碰到屏幕边缘则回弹；还有一个效果就是，手指在屏幕中移动、拖拽，与手指触摸点连线的点向触摸点靠拢。何为「邻近的点」，与某点的距离小于特定的阈值的点称为「邻近的点」。

提到运动，「运动」在[物理学](https://baike.baidu.com/item/%E8%BF%90%E5%8A%A8/2134957?fr=aladdin)中指物体在空间中的相对位置随着时间而变化。

那么大家还记得「位移」与「速度」公式吗？

```java
位移 = 初位移 + 速度 * 时间
速度 = 初速度 + 加速度
```

时间、位移、速度、加速度构成了现代科学的运动体系。我们使用 view 来模拟物体的运动。

- 时间：在 view 的 onDraw 方法中调用 invalidate 方法，达到无限刷新来模拟时间流，每次刷新间隔，记为：1U

- 位移：物体在屏幕中的像素位置，每个像素距离为：1px

- 速度：默认设置一个值，单位（px / U）

- 加速度：默认设置一个值，单位（px / U^2）

模拟「蛛网点」物体类：

```java
public class SpiderPoint extends Point {

    // x 方向加速度
    public int aX;

    // y 方向加速度
    public int aY;

    // 小球颜色
    public int color;
    
    // 小球半径
    public int r;

    // x 轴方向速度
    public float vX;

    // y 轴方向速度
    public float vY;
    
    // 点
    public float x;
    public float y;

    public SpiderPoint(int x, int y) {
        super(x, y);
    }
}
```

#### 蛛网点匀速直线运动

搭建测试 View，初始位置 (0,0) ，x 方向速度 10、y 方向速度 0 的蛛网点：

```java
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
    private float pointVY = 0;
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
                startMove = false;
                invalidate();
                break;
        }
        return true;
    }
}
```

1、水平运行运动：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190326190110388.gif)

根据上文中的位移公式，`位移 = 初位移 + 速度 * 时间` ，这里的时间为 1U，更新小球位置的相关代码如下：

```java
    /**
     * 更新小球
     */
    private void updateBall() {
        //TODO --运动数据都由此函数变换
        mSpiderPoint.x += mSpiderPoint.vX;
    }
```

2、回弹效果

回弹，速度取反，x 轴方向大于 400 则回弹：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190326191356689.gif)

3、无限回弹，回弹变色

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190326192213201.gif)

相关代码如下：

```java
    /**
     * 更新小球
     */
    private void updateBall() {
        //TODO --运动数据都由此函数变换
        mSpiderPoint.x += mSpiderPoint.vX;
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
    }
```

`randomRGB` 方法的代码如下：

```java
    /**
     * @return 获取到随机颜色值
     */
    private int randomRGB() {
        Random random = new Random();
        return Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }
```

3、箱式弹跳

小球在 y 轴方向的平移与 x 轴方向的平移一致，这里不再讲解，看一下 x ，y 轴同时具有初速度，即速度斜向的情况。

![图源网络，侵权必删](https://img-blog.csdnimg.cn/20190326192529502.png)

改变 y 轴方向初速度：

```java
    // 默认y方向速度
    private float pointVY = 6;
```

在 updateBall 方法中增加对 y 方向的修改：

```java
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
```

效果如下图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/2019032619422161.gif)

蛛网「小点」并没有涉及到变速运动，有关变速运动可以链接以下地址进行查阅：

[Android原生绘图之让你了解View的运动](https://juejin.im/post/5bee10376fb9a04a0e2cc4c2#heading-1)

### 构思代码
通过观察网页「蛛网」动态效果，可以细分为以下几点：

- 绘制一定数量的小球（蛛网点）

- 小球斜向运动（具有 x，y 轴方向速度），越界回弹

- 遍历所有小球，若小球 A 与其他小球的距离小于一定值，则两小球连线，反之则不连线

- 若小球 A 先与小球 B 连线，为了提高性能，防止过度绘制，小球 B 不再与小球 A 连线

- 在手指触摸点绘制小球，同连线规则一致，连线其他小球，若手指移动，连线的所有小球向触摸点靠拢

接下来，具体看看代码该怎么写。

### 编写代码

#### 起名字

取名是一门学问，好的名字能够让你记忆犹新，那就叫 **SpiderWebView**  （蛛网控件）。

#### 创建SpiderWebView

先是成员变量：

```java
    // 控件宽高
    private int mWidth;
    private int mHeight;
    // 画笔
    private Paint mPointPaint;
    private Paint mLinePaint;
    private Paint mTouchPaint;
    // 触摸点坐标
    private float mTouchX = -1;
    private float mTouchY = -1;
    // 数据源
    private List<SpiderPoint> mSpiderPointList;
    // 相关参数配置
    private SpiderConfig mConfig;
    // 随机数
    private Random mRandom;
    // 手势帮助类 用于处理滚动与拖拽
    private GestureDetector mGestureDetector;
```

然后是构造函数：

```java
    // view 的默认构造函数 参数不做讲解
    public SpiderWebView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // setLayerType(LAYER_TYPE_HARDWARE, null);
        mSpiderPointList = new ArrayList<>();
        mConfig = new SpiderConfig();
        mRandom = new Random();
        mGestureDetector = new GestureDetector(context, mSimpleOnGestureListener);
        // 画笔初始化
        initPaint();
    }
```

接着按着「构思代码」中的效果逐一实现。

#### 绘制一定数量的小球

指定数量为 50，每个小球的位置、颜色随机，并且具有不同的加速度。相关代码如下：

```java
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }
```

先获取控件到控件的宽高。然后初始化小球集合：

```java
    /**
     * 初始化小点
     */
    private void initPoint() {
        for (int i = 0; i < mConfig.pointNum; i++) {
            int width = (int) (mRandom.nextFloat() * mWidth);
            int height = (int) (mRandom.nextFloat() * mHeight);

            SpiderPoint point = new SpiderPoint(width, height);
            int aX = 0;
            int aY = 0;
            // 获取加速度
            while (aX == 0) {
                aX = (int) ((mRandom.nextFloat() - 0.5F) * mConfig.pointAcceleration);
            }
            while (aY == 0) {
                aY = (int) ((mRandom.nextFloat() - 0.5F) * mConfig.pointAcceleration);
            }
            point.aX = aX;
            point.aY = aY;
            // 颜色随机
            point.color = randomRGB();
            mSpiderPointList.add(point);
        }
    }
```

`mConfig` 表示配置参数，具体有以下成员变量：

```java
public class SpiderConfig {
    // 小点半径 1
    public int pointRadius = DEFAULT_POINT_RADIUS;
    // 小点之间连线的粗细(宽度) 2
    public int lineWidth = DEFAULT_LINE_WIDTH;
    // 小点之间连线的透明度 150
    public int lineAlpha = DEFAULT_LINE_ALPHA;
    // 小点数量 50
    public int pointNum = DEFAULT_POINT_NUMBER;
    // 小点加速度 7
    public int pointAcceleration = DEFAULT_POINT_ACCELERATION;
    // 小点之间最长直线距离 280
    public int maxDistance = DEFAULT_MAX_DISTANCE;
    // 触摸点半径 1
    public int touchPointRadius = DEFAULT_TOUCH_POINT_RADIUS;
    // 引力大小 50
    public int gravitation_strength = DEFAULT_GRAVITATION_STRENGTH;
}
```

获取到小球集合，最后绘制小球：

```java
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制小球
        mPointPaint.setColor(spiderPoint.color);
        canvas.drawCircle(spiderPoint.x, spiderPoint.y, mConfig.pointRadius, mPointPaint);
        }
```

效果图如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/2019032622532685.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTI1NTEzNTA=,size_16,color_FFFFFF,t_70)

#### 小球斜向运动，越界回弹

根据位移与速度公式 `位移 = 初位移 + 速度 * 时间` ，`速度 = 初速度 + 加速度` ，由于初速度为 0 ，时间为 1U，得到 `位移 = 初位移 + 加速度` ：

```java
    spiderPoint.x += spiderPoint.aX;
    spiderPoint.y += spiderPoint.aY;
```

判定越界，原理在上文中已经提到：

```java
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (SpiderPoint spiderPoint : mSpiderPointList) {

            spiderPoint.x += spiderPoint.aX;
            spiderPoint.y += spiderPoint.aY;

            // 越界反弹
            if (spiderPoint.x <= mConfig.pointRadius) {
                spiderPoint.x = mConfig.pointRadius;
                spiderPoint.aX = -spiderPoint.aX;
            } else if (spiderPoint.x >= (mWidth - mConfig.pointRadius)) {
                spiderPoint.x = (mWidth - mConfig.pointRadius);
                spiderPoint.aX = -spiderPoint.aX;
            }

            if (spiderPoint.y <= mConfig.pointRadius) {
                spiderPoint.y = mConfig.pointRadius;
                spiderPoint.aY = -spiderPoint.aY;
            } else if (spiderPoint.y >= (mHeight - mConfig.pointRadius)) {
                spiderPoint.y = (mHeight - mConfig.pointRadius);
                spiderPoint.aY = -spiderPoint.aY;
            }
		}
	}
```

效果图如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190326231354428.gif)

#### 两球连线

循环遍历所有小球，若小球 A 与其他小球的距离小于一定值，则两小球连线，反之则不连线。双层遍历会导致一个问题，如果小球数量过多，双层遍历效率极低，从而引起界面卡顿，目前并没有找到更好的算法来解决这个问题，为了防止卡顿，对小球的数量有所控制，不能超过 150 个。

```java
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (SpiderPoint spiderPoint : mSpiderPointList) {
            // 绘制连线
            for (int i = 0; i < mSpiderPointList.size(); i++) {
                SpiderPoint point = mSpiderPointList.get(i);
                // 判定当前点与其他点之间的距离
                if (spiderPoint != point) {
                    int distance = disPos2d(point.x, point.y, spiderPoint.x, spiderPoint.y);
                    if (distance < mConfig.maxDistance) {
                        // 绘制小点间的连线
                        int alpha = (int) ((1.0F - (float) distance / mConfig.maxDistance) * mConfig.lineAlpha);

                        mLinePaint.setColor(point.color);
                        mLinePaint.setAlpha(alpha);
                        canvas.drawLine(spiderPoint.x, spiderPoint.y, point.x, point.y, mLinePaint);
                    }
                }
            }
        }
        invalidate();
    }
```

`disPos2d` 方法用于计算两点之间的距离：

```java
    /**
     * 两点间距离函数
     */
    public static int disPos2d(float x1, float y1, float x2, float y2) {
        return (int) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
```

如果两小球的距离在 `maxDistance` 范围内，距离越近透明度越小：

```java
	int alpha = (int) ((1.0F - (float) distance / mConfig.maxDistance) * mConfig.lineAlpha);
```

一起来看看两球连线的效果：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190326233236187.gif)

#### 防止过度绘制

由于双层遍历，若小球 A 先与小球 B 连线，为了提高性能，防止过度绘制，小球 B 不再与小球 A 连线。最开始的想法是记录小球 A 与其他小球的连线状态，当其他小球与小球 A 连线时，根据状态判定是否连线，如果小球 A 先与许多小球连线，必然会在小球 A 对象内部维护一个集合，用于存储小球 A 已经与哪些小球连线，这样效率并不高，反而把简单的问题变复杂了。最后用了一个取巧的办法：记录第一次循环的索引值，第二次循环从当前的索引值开始，这样就避免了两小球之间的多次连线。相关代码如下：

```java
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int index = 0;
        for (SpiderPoint spiderPoint : mSpiderPointList) {
            // 绘制连线
            for (int i = index; i < mSpiderPointList.size(); i++) {
                SpiderPoint point = mSpiderPointList.get(i);
                // 判定当前点与其他点之间的距离
                if (spiderPoint != point) {
                    int distance = disPos2d(point.x, point.y, spiderPoint.x, spiderPoint.y);
                    if (distance < mConfig.maxDistance) {
                        // 绘制小点间的连线
                        int alpha = (int) ((1.0F - (float) distance / mConfig.maxDistance) * mConfig.lineAlpha);

                        mLinePaint.setColor(point.color);
                        mLinePaint.setAlpha(alpha);
                        canvas.drawLine(spiderPoint.x, spiderPoint.y, point.x, point.y, mLinePaint);
                    }
                }
            }
          index++;
        }
        invalidate();
    }
```

#### 手势处理

还记得吗？在文章 [第一站小红书图片裁剪控件，深度解析大厂炫酷控件](https://blog.csdn.net/u012551350/article/details/87928720) 已经讲解了手势的处理流程。在网页版中触摸点（鼠标按下点）跟随鼠标移动而移动，在手机屏幕中「触摸点」（手指按下点）跟随手指移动而移动，从而需要重写手势类的 `onScroll` 方法：

```java
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // 单根手指操作
            if (e1.getPointerCount() == e2.getPointerCount() && e1.getPointerCount() == 1) {
                mTouchX = e2.getX();
                mTouchY = e2.getY();
                return true;
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
```

`onFling` 方法与 `onScroll` 方法处理方式一致，实时获取到「触摸点」位置。获取到了位置，绘制触摸点：

```java
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制触摸点
        if (mTouchY != -1 && mTouchX != -1) {
            canvas.drawPoint(mTouchX, mTouchY, mTouchPaint);
        }
	}
```

若「触摸点」与其他小球的距离小于一定值，则两小球连线，反之则不连线：

```java
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
            // 绘制触摸点与其他点的连线
            if (mTouchX != -1 && mTouchY != -1) {
                int offsetX = (int) (mTouchX - spiderPoint.x);
                int offsetY = (int) (mTouchY - spiderPoint.y);
                int distance = (int) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
                if (distance < mConfig.maxDistance) {
                    int alpha = (int) ((1.0F - (float) distance / mConfig.maxDistance) * mConfig.lineAlpha);
                    mLinePaint.setColor(spiderPoint.color);
                    mLinePaint.setAlpha(alpha);
                    canvas.drawLine(spiderPoint.x, spiderPoint.y, mTouchX, mTouchY, mLinePaint);
                }
            }
	}
```

同时还具有与「触摸点」连线的所有小球向「触摸点」靠拢的效果，可采用「位移相对减少」的方案来实现靠拢的效果，相关代码如下：

```java
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
            // 绘制触摸点与其他点的连线
            if (mTouchX != -1 && mTouchY != -1) {
       		....... // 省略相关代码          
                if (distance < mConfig.maxDistance) {
                    if (distance >= (mConfig.maxDistance - mConfig.gravitation_strength)) {
                        // x 轴方向位移减少
                        if (spiderPoint.x > mTouchX) {
                            spiderPoint.x -= 0.03F * -offsetX;
                        } else {
                            spiderPoint.x += 0.03F * offsetX;
                        }
                        // y 轴方向位移减少
                        if (spiderPoint.y > mTouchY) {
                            spiderPoint.y -= 0.03F * -offsetY;
                        } else {
                            spiderPoint.y += 0.03F * offsetY;
                        }
                    } 
		....... // 省略相关代码    
```

看看效果图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/201903270933057.gif)

「五彩蛛网」控件差不多就讲到这里，有什么疑问，请留言讨论？

### 结束语

熬夜写的文章，有道不明的，还请多多包涵。同时也希望各位小伙伴都能过得都挺好。

源码如下：

https://github.com/HpWens/MeiWidgetView

https://github.com/HpWens/SpiderWebView

希望有志之士能够与我一起维护「控件人生」公众号。

<div align=center><img src="https://upload-images.jianshu.io/upload_images/2258857-196f00b808ab8668.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240" width="200px"/></div>

<div align=center>扫一扫 关注我的公众号</div>

<div align=center>想了解更多炫酷控件吗~</div>





