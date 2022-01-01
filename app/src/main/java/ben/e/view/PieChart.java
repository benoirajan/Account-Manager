package ben.e.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ben.e.R;

/**
 * TODO: document your custom view class.
 */
public class PieChart extends View {
    private String mExampleString; // TODO: use a default from R.string...
    private int mShadowColor = Color.GRAY; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;
    private Paint piePaint, strockPaint;
    private Paint shadowPaint;
    private RectF bounds;
    List<ChartData> data;

    public PieChart(Context context) {
        super(context);
        init(null, 0);
    }

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PieChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.PieChart, defStyle, 0);

        mShadowColor = a.getColor(
                R.styleable.PieChart_shadowColor,
                mShadowColor);

        float sRadius = a.getDimension(R.styleable.PieChart_shadowRadius, 0);

        float strokeWidth = a.getDimension(R.styleable.PieChart_strokeWidth, 0);
        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        piePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        piePaint.setColor(Color.RED);
        piePaint.setStyle(Paint.Style.FILL);

        strockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strockPaint.setColor(Color.WHITE);
        strockPaint.setStyle(Paint.Style.STROKE);
        strockPaint.setStrokeWidth(strokeWidth);

        shadowPaint = new Paint(0);
        shadowPaint.setColor(mShadowColor);
        if (!isInEditMode())
            shadowPaint.setMaskFilter(new BlurMaskFilter(sRadius, BlurMaskFilter.Blur.NORMAL));
        // Update TextPaint and text measurements from attributes
        if (isInEditMode()) {
            int[] colors = {0xFFFF5722, 0xFFFF9800};
            double[] vals = {6, 4};
            setData(10, vals, colors);
        }
    }

    public void setData(double total, double[] values, @ColorInt int[] colors) {
        data = new ArrayList<>();
        assert values.length == colors.length :
                String.format(Locale.getDefault(),
                        "Size of Arrays are different. values=%d colors=%d",
                        values.length,
                        colors.length);

        float prev = 0;
        for (int i = 0; i < values.length; i++) {
            ChartData item = new ChartData();
            item.startAngle = prev;
            item.sweepAngle = (float) (values[i] * 360 / total);
            item.color = colors[i];
            prev = item.startAngle + item.sweepAngle;
            data.add(item);
        }
        if (data != null) {
            invalidate();
        }
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mShadowColor);
        mTextWidth = mTextPaint.measureText(mExampleString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        canvas.save();
        canvas.rotate(-90, getPivotX(), getPivotY());
        if (data != null && data.size() > 0) {
            //canvas.drawOval(bounds, shadowPaint);
            for (ChartData it : data) {
                piePaint.setColor(it.color);
                canvas.drawArc(bounds, it.startAngle, it.sweepAngle, true, piePaint);
                canvas.drawArc(bounds, it.startAngle, it.sweepAngle, true, strockPaint);
            }
        }
        canvas.restore();
        // Draw the text.
    }

   /* @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = resolveSizeAndState(200, widthMeasureSpec, getMeasuredState());
        int h = resolveSizeAndState(200, heightMeasureSpec, getMeasuredState());
        int min = Math.min(w,h);
        setMeasuredDimension(min,min);
    }*/

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int contentWidth = w - getPaddingLeft() - getPaddingRight();
        int contentHeight = h - getPaddingTop() - getPaddingBottom();
        float diameter = Math.min(contentHeight, contentWidth);
        float left = (w - diameter) / 2f;
        float top = (h - diameter) / 2f;
        bounds = new RectF(left, top, left + diameter, top + diameter);
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view"s example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mShadowColor;
    }

    /**
     * Sets the view"s example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mShadowColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view"s example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    private static class ChartData {
        @ColorInt
        int color;
        float startAngle, sweepAngle;
    }
}