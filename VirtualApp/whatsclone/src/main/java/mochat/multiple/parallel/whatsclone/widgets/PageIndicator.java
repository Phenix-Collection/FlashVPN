package mochat.multiple.parallel.whatsclone.widgets;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import mochat.multiple.parallel.whatsclone.utils.DisplayUtils;

import java.util.ArrayList;

/**
 * Created by yxx on 2016/7/14.
 */
public class PageIndicator extends LinearLayout {
    private static final float DEFAULT_RADIUS = 3;
    private static final float DEFAULT_PADDING = 6;
    private static final float DEFAULT_MARGIN = 3;
    private static final int DEFAULT_COLOR = Color.parseColor("#4cffffff");
    private static final int DEFAULT_RECT_COLOR = Color.parseColor("#3490fb");
    private Paint mPaint;
    private int radius;
    private int padding;
    private int margin;
    private int bgCircleColor = DEFAULT_COLOR;
    private int rectColor = DEFAULT_RECT_COLOR;
    private ArrayList<Circle> circleList;
    private Context mContext;
    private int selectedPage;
    private RectF selectedRect;
    private int selectedLeftPadding;

    public PageIndicator(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    public PageIndicator(Context context, AttributeSet paramAttributeSet) {
        super(context, paramAttributeSet);
        this.mContext = context;
        initView();
    }

    private void initView() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(2);

        radius = DisplayUtils.dip2px(mContext, DEFAULT_RADIUS);
        padding = DisplayUtils.dip2px(mContext, DEFAULT_PADDING);
        margin = DisplayUtils.dip2px(mContext, DEFAULT_MARGIN);

        circleList = new ArrayList<>();
        selectedRect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (selectedPage < 0) {
            selectedPage = 0;
        } else if (selectedPage >= circleList.size()) {
            selectedPage = circleList.size() - 1;
        }
        for (int i = 0; i < circleList.size(); i++) {
            mPaint.setColor(bgCircleColor);
            canvas.drawCircle(circleList.get(i).centerX, getHeight() / 2, radius, mPaint);
        }

        selectedRect.left = circleList.get(selectedPage).centerX - radius;
        selectedRect.right = circleList.get(selectedPage + 1).centerX + radius;
        selectedRect.top = getHeight() / 2 - radius;
        selectedRect.bottom = getHeight() / 2 + radius;

        mPaint.setColor(rectColor);
        drawHalfCircleRect(canvas,selectedRect,4 * radius);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int paddingCount = circleList.size() >= 1 ? circleList.size() - 1 : 0;
        int resultWidth = circleList.size() * radius * 2 + paddingCount * padding + 2 * margin;
        int resultHeight = 2 * radius + padding;

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(resultWidth, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(resultHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void addPage() {
        drawCircle(circleList.size() + 1);
    }

    public void removePage() {
        drawCircle(circleList.size() - 1);
    }

    public void setCurrentPage(int position) {
        if (position >= 0 && position < circleList.size()) {
            this.selectedPage = position;
        }
        invalidate();
    }

    public void setTotalPageSize(final int totalPageSize) {
        drawCircle(totalPageSize);
    }

    private void drawCircle(int pageSize) {
        //选中的item占用两个圆圈
        pageSize ++;
        if (pageSize == circleList.size())
            return;
        if (pageSize > circleList.size()) {
            for (int i = circleList.size(); i < pageSize; i++) {
                Circle circle = new Circle();
                circle.centerX = i * (radius * 2 + padding) + radius + margin;
                circleList.add(circle);
            }
        } else {
            while (pageSize < circleList.size()) {
                circleList.remove(circleList.size() - 1);
            }
        }
        requestLayout();
        invalidate();
    }

    private void drawHalfCircleRect(Canvas canvas, RectF rect, float innerRectWidth) {
        if (rect.width() < innerRectWidth) {
            return;
        }
        RectF leftCircleRect = new RectF();
        RectF rightCircleRect = new RectF();

        leftCircleRect.left = rect.left;
        leftCircleRect.right = rect.left + rect.height();
        leftCircleRect.top = rect.top;
        leftCircleRect.bottom = rect.bottom;

        rightCircleRect.left = rect.right - rect.height();
        rightCircleRect.right = rect.right;
        rightCircleRect.top = rect.top;
        rightCircleRect.bottom = rect.bottom;

        Path path = new Path();
        path.moveTo(rect.left + rect.height() / 2, rect.top);
        path.moveTo(rect.left + rect.height() / 2, rect.top);
        path.lineTo(rect.right - rect.height() / 2, rect.top);
        path.arcTo(rightCircleRect, 270, 180);
        path.lineTo(rect.left + rect.height() / 2, rect.bottom);
        path.arcTo(leftCircleRect, 90, 180);
        path.close();

        canvas.drawPath(path, mPaint);

    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    class Circle {
        float centerX;
//        float centerY;
    }
}
