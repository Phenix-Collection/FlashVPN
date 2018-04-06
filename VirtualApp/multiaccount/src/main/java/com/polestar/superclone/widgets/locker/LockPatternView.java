package com.polestar.superclone.widgets.locker;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.polestar.superclone.R;
import com.polestar.superclone.utils.DisplayUtils;
import com.polestar.superclone.utils.LockPatternUtils;
import com.polestar.superclone.utils.ResourcesUtil;

import java.util.ArrayList;
import java.util.List;
/**
 * Displays and detects the user's unlock attempt, which is a drag of a finger
 * across 9 regions of the screen.
 * <p/>
 * Is also capable of displaying a static pattern in "in progress", "wrong" or
 * "correct" states.
 */
public class LockPatternView extends View {


    /**
     * This is the width of the matrix (the number of dots per row and column).
     * Change this value to change the dimension of the pattern's matrix.
     *
     * @since v2.7 beta
     * @author Thomas Breitbach
     */
    public static final int MATRIX_WIDTH = 3;

    /**
     * The size of the pattern's matrix.
     */
    public static final int MATRIX_SIZE = MATRIX_WIDTH * MATRIX_WIDTH;

    private Paint mPaint = new Paint();
    private Paint mGreenPathPaint = new Paint();
    private Paint mRedPathPaint = new Paint();

    private static final int PATH_PAINT_WIDTH = DisplayUtils.dip2px(ResourcesUtil.getContext(),2);

    private int mRedAnimationMaxCount = 15;
    private int mRedAnimationCount = 0;

    /**
     * This can be used to avoid updating the display for very small motions or
     * noisy panels. It didn't seem to have much impact on the devices tested,
     * so currently set to 0.
     */
    private static final float DRAG_THRESHHOLD = 0.0f;

    private OnPatternListener mOnPatternListener;
    private ArrayList<Cell> mPattern = new ArrayList<Cell>(MATRIX_SIZE);

    /**
     * Lookup table for the circles of the pattern we are currently drawing.
     * This will be the cells of the complete pattern unless we are animating,
     * in which case we use this to hold the cells we are drawing for the in
     * progress animation.
     */
    private boolean[][] mPatternDrawLookup = new boolean[MATRIX_WIDTH][MATRIX_WIDTH];

    /**
     * the in progress point: - during interaction: where the user's finger is -
     * during animation: the current tip of the animating line
     */
    private float mInProgressX = -1;
    private float mInProgressY = -1;

    private DisplayMode mPatternDisplayMode = DisplayMode.Correct;
    private boolean mInputEnabled = true;
    private boolean mInStealthMode = false;
    private boolean mInArrowMode = true;
    private boolean mPatternInProgress = false;
    private boolean mInCircleMode = true;

    /**
     * TODO: move to attrs
     */
    private float mDiameterFactor = 0.03f;
    private final int mStrokeAlpha = 255;
    private float mHitFactor = 0.6f;

    private float mSquareWidth;
    private float mSquareHeight;

    private Bitmap mBitmapBtnDefault;
    private Bitmap mBitmapBtnTouched;
    private Bitmap mBitmapBtnIncorrect;
    private Bitmap mBitmapCircleDefault;
    private Bitmap mBitmapCircleTouched;
    private Bitmap mBitmapCircleIncorrect;

    private Bitmap mBitmapArrowGreenUp;
    private Bitmap mBitmapArrowRedUp;

    private final Path mCurrentPath = new Path();
    private final Rect mInvalidate = new Rect();
    private final Rect mTmpInvalidateRect = new Rect();

    private int mBitmapWidth;
    private int mBitmapHeight;

    protected static final int ASPECT_SQUARE_FILL = 3;
    protected static final int ASPECT_SQUARE = 0;


    protected int mAspect;
    private final Matrix mArrowMatrix = new Matrix();
    private final Matrix mCircleMatrix = new Matrix();
    private final Matrix mCircleBtnMatrix = new Matrix();

    private final int mPadding = 0;
    private final int mPaddingLeft = mPadding;
    private final int mPaddingRight = mPadding;
    private int mPaddingTop = mPadding;
    protected int mPaddingBottom = mPadding;

    private float mDotPivotX = 0.5f;
    private float mDotPivotY = 0.5f;

    private boolean useBackupStyle = false;
    /**
     * Represents a cell in the MATRIX_WIDTH x MATRIX_WIDTH matrix of the unlock
     * pattern view.
     */
    public static class Cell implements Parcelable {

        int mRow;
        int mColumn;

        /*
         * keep # objects limited to MATRIX_SIZE
         */
        static Cell[][] sCells = new Cell[MATRIX_WIDTH][MATRIX_WIDTH];
        static {
            for (int i = 0; i < MATRIX_WIDTH; i++) {
                for (int j = 0; j < MATRIX_WIDTH; j++) {
                    sCells[i][j] = new Cell(i, j);
                }
            }
        }

        /**
         * @param row
         *            The row of the cell.
         * @param column
         *            The column of the cell.
         */
        private Cell(int row, int column) {
            checkRange(row, column);
            this.mRow = row;
            this.mColumn = column;
        }

        /**
         * Gets the row index.
         *
         * @return the row index.
         */
        public int getRow() {
            return mRow;
        }// getRow()

        /**
         * Gets the column index.
         *
         * @return the column index.
         */
        public int getColumn() {
            return mColumn;
        }// getColumn()

        /**
         * @param row
         *            The row of the cell.
         * @param column
         *            The column of the cell.
         */
        public static synchronized Cell of(int row, int column) {
            checkRange(row, column);
            return sCells[row][column];
        }

        private static void checkRange(int row, int column) {
            if (row < 0 || row > MATRIX_WIDTH - 1) {
                throw new IllegalArgumentException("row must be in range 0-"
                        + (MATRIX_WIDTH - 1));
            }
            if (column < 0 || column > MATRIX_WIDTH - 1) {
                throw new IllegalArgumentException("column must be in range 0-"
                        + (MATRIX_WIDTH - 1));
            }
        }

        @Override
        public String toString() {
            return  "";// "(ROW=" + getRow() + ",COL=" + getColumn() + ")";
        }// toString()

        @Override
        public boolean equals(Object object) {
            if (object instanceof Cell)
                return getColumn() == ((Cell) object).getColumn()
                        && getRow() == ((Cell) object).getRow();
            return super.equals(object);
        }// equals()

        /*
         * PARCELABLE
         */

        @Override
        public int describeContents() {
            return 0;
        }// describeContents()

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(getColumn());
            dest.writeInt(getRow());
        }// writeToParcel()

        /**
         * Reads data from parcel.
         *
         * @param in
         *            the parcel.
         */
        public void readFromParcel(Parcel in) {
            mColumn = in.readInt();
            mRow = in.readInt();
        }// readFromParcel()

        public static final Parcelable.Creator<Cell> CREATOR = new Parcelable.Creator<Cell>() {

            public Cell createFromParcel(Parcel in) {
                return new Cell(in);
            }// createFromParcel()

            public Cell[] newArray(int size) {
                return new Cell[size];
            }// newArray()
        };// CREATOR

        private Cell(Parcel in) {
            readFromParcel(in);
        }// Cell()
    }// Cell

    /**
     * How to display the current pattern.
     */
    public enum DisplayMode {

        /**
         * The pattern drawn is correct (i.e draw it in a friendly color)
         */
        Correct,

        /**
         * The pattern is wrong (i.e draw a foreboding color)
         */
        Wrong,

        Animate

    }

    /**
     * The call back interface for detecting patterns entered by the user.
     */
    public static interface OnPatternListener {

        /**
         * A new pattern has begun.
         */
        void onPatternStart();

        /**
         * The pattern was cleared.
         */
        void onPatternCleared();

        /**
         * The user extended the pattern currently being drawn by one cell.
         *
         * @param pattern
         *            The pattern with newly added cell.
         */
        void onPatternCellAdded(List<Cell> pattern);

        /**
         * A pattern was detected from the user.
         *
         * @param pattern
         *            The pattern.
         */
        void onPatternDetected(List<Cell> pattern);
    }

    public LockPatternView(Context context) {
        this(context, null);
    }

    public LockPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefaultStyle();
        initData();
    }

    private void initData(){
        setClickable(true);

        mAspect = ASPECT_SQUARE_FILL;

        mGreenPathPaint.setAntiAlias(true);
        mGreenPathPaint.setDither(true);
        mGreenPathPaint.setColor(getContext().getResources().getColor(R.color.applock_lockpattern_pattern_path_white_light));

        mGreenPathPaint.setAlpha(mStrokeAlpha);
        mGreenPathPaint.setStyle(Paint.Style.STROKE);
        mGreenPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mGreenPathPaint.setStrokeCap(Paint.Cap.ROUND);

        mRedPathPaint.setAntiAlias(true);
        mRedPathPaint.setDither(true);
        mRedPathPaint.setColor(getContext().getResources().getColor(R.color.applock_lockpattern_pattern_path_red_light));
        mRedPathPaint.setAlpha(mStrokeAlpha);
        mRedPathPaint.setStyle(Paint.Style.STROKE);
        mRedPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mRedPathPaint.setStrokeCap(Paint.Cap.ROUND);
        /*
         * lot's of bitmaps!
         */

        mBitmapBtnDefault = ResourcesUtil.getBitmap(R.drawable.applock_pattern_default_btn_gray_point);

        mBitmapBtnTouched = ResourcesUtil.getBitmap(R.drawable.applock_lockpattern_applock_btn_code_lock_touched_holo_light);


        mBitmapBtnIncorrect = ResourcesUtil.getBitmap(R.drawable.applock_lockpattern_applock_btn_code_lock_red_holo_light);

        mBitmapCircleDefault = getBitmapFor(R.drawable.applock_lockpattern_indicator_code_lock_backgorund_holo);

        mBitmapCircleTouched = getBitmapFor(R.drawable.applock_lockpattern_indicator_code_lock_point_area_green_holo);

//        mBitmapCircleIncorrect = getBitmapFor(mDefault_CircleIncorrectResId);
        mBitmapCircleIncorrect = getBitmapFor(R.drawable.applock_lockpattern_indicator_code_lock_point_area_red_holo);


        mBitmapArrowGreenUp = getBitmapFor(ResourcesUtil.getDrawableId(getContext(), "applock_touch_pattern_arrow"));
        mBitmapArrowRedUp = getBitmapFor(ResourcesUtil.getDrawableId(getContext(), "applock_touch_pattern_arrow_red"));

        computeMaxBitmapSize();
    }

    private void computeMaxBitmapSize() {
        /*
         * bitmaps have the size of the largest bitmap in this group
         */
        Bitmap[] bmps = new Bitmap[]{mBitmapCircleDefault, mBitmapCircleIncorrect, mBitmapCircleTouched};
        for (Bitmap bitmap : bmps) {
            if (bitmap != null) {
                mBitmapWidth = Math.max(mBitmapWidth, bitmap.getWidth());
                mBitmapHeight = Math.max(mBitmapHeight, bitmap.getHeight());
            }
        }
    }

    protected Bitmap getBitmapFor(String resId) {
        Bitmap bmp = null;
        try {
            bmp = ResourcesUtil.getBitmap(resId);
        } catch (Exception e) {

        }
        return bmp;
    }

    private Bitmap getBitmapFor(int resId) {
        Bitmap bmp = null;

        try {
            bmp = BitmapFactory.decodeResource(getContext().getResources(), resId);
        } catch (Exception e) {

        }
        return bmp;
    }

    protected void setDefaultStyle() {
    }

    /**
     * Set whether the view is in stealth mode. If true, there will be no
     * visible feedback as the user enters the pattern.
     *
     * @param inStealthMode
     *            Whether in stealth mode.
     */
    public void setInStealthMode(boolean inStealthMode) {
        mInStealthMode = inStealthMode;
    }

    public void setInArrowMode(boolean inArrowMode) {
        mInArrowMode = inArrowMode;
    }

    public void setInCircleMode(boolean inCircleMode){
        mInCircleMode = inCircleMode;
    }

    public void setRedPathPaintColor(int color){
        if (mRedPathPaint != null){
            mRedPathPaint.setColor(color);
        }
    }

    public void setGreenPathPaintColor(int color){
        if(mGreenPathPaint != null){
            mGreenPathPaint.setColor(color);
        }
    }

    public void setBitmapBtnDefault(int id){
        mBitmapBtnDefault = ResourcesUtil.getBitmap(id);
    }

    public void setBitmapBtnTouched(int id){
        mBitmapBtnTouched = ResourcesUtil.getBitmap(id);
    }

    /**
     * Set the call back for pattern detection.
     *
     * @param onPatternListener
     *            The call back.
     */
    public void setOnPatternListener(OnPatternListener onPatternListener) {
        mOnPatternListener = onPatternListener;
    }

    /**
     * Set the pattern explicitely (rather than waiting for the user to input a
     * pattern).
     *
     * @param displayMode
     *            How to display the pattern.
     * @param pattern
     *            The pattern.
     */
    public void setPattern(DisplayMode displayMode, List<Cell> pattern) {
        checkIfAnimationAborted();
        mPattern.clear();
        mPattern.addAll(pattern);
        clearPatternDrawLookup();
        for (Cell cell : pattern) {
            mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
        }

        setDisplayMode(displayMode);
    }

    /**
     * Set the display mode of the current pattern. This can be useful, for
     * instance, after detecting a pattern to tell this view whether change the
     * in progress result to correct or wrong.
     *
     * @param displayMode
     *            The display mode.
     */
    public void setDisplayMode(DisplayMode displayMode) {
        mPatternDisplayMode = displayMode;
        if(mPatternDisplayMode == DisplayMode.Wrong) {
            mRedAnimationCount = mRedAnimationMaxCount;
        }

        if (displayMode == DisplayMode.Animate) {
            if (mPattern.size() == 0) {
                throw new IllegalStateException(
                        "you must have a pattern to "
                                + "animate if you want to set the display mode to animate");
            }
            mAnimatingPeriodStart = SystemClock.elapsedRealtime();
            final Cell first = mPattern.get(0);
            mInProgressX = getCenterXForColumn(first.getColumn());
            mInProgressY = getCenterYForRow(first.getRow());
            clearPatternDrawLookup();
        }

        invalidate();
    }

    /**
     * Retrieves current displaying pattern. This method is useful in case of
     * storing states and restoring them after screen orientation changed.
     *
     * @return current displaying pattern. <b>Note:</b> This is an independent
     *         list with the view's pattern itself.
     * @since v1.5.3 beta
     */
    @SuppressWarnings("unchecked")
    public List<Cell> getPattern() {
        return (List<Cell>) mPattern.clone();
    }

    private void notifyCellAdded() {
//        sendAccessEvent(R.string.alp_42447968_lockscreen_access_pattern_cell_added);
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCellAdded(mPattern);
        }
    }

    private void notifyPatternStarted() {
//        sendAccessEvent(R.string.alp_42447968_lockscreen_access_pattern_start);
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternStart();
        }
    }

    private void notifyPatternDetected() {
//        sendAccessEvent(R.string.alp_42447968_lockscreen_access_pattern_detected);
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternDetected(mPattern);
        }
    }

    private void notifyPatternCleared() {
//        sendAccessEvent(R.string.alp_42447968_lockscreen_access_pattern_cleared);
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCleared();
        }
    }

    /**
     * Clear the pattern.
     */
    public void clearPattern() {
        resetPattern();
    }

    /**
     * Reset all pattern state.
     */
    private void resetPattern() {
        mPattern.clear();
        clearPatternDrawLookup();
        mPatternDisplayMode = DisplayMode.Correct;
        invalidate();
    }

    /**
     * Clear the pattern lookup table.
     */
    private void clearPatternDrawLookup() {
        for (int i = 0; i < MATRIX_WIDTH; i++) {
            for (int j = 0; j < MATRIX_WIDTH; j++) {
                mPatternDrawLookup[i][j] = false;
            }
        }
    }

    /**
     * Disable input (for instance when displaying a message that will timeout
     * so user doesn't get view into messy state).
     */
    public void disableInput() {
        mInputEnabled = false;
    }

    /**
     * Enable input.
     */
    public void enableInput() {
        mInputEnabled = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int width = w - mPaddingLeft - mPaddingRight;
        mSquareWidth = width / (float) MATRIX_WIDTH;

        final int height = h - mPaddingTop - mPaddingBottom;
        mSquareHeight = height / (float) MATRIX_WIDTH;
    }

    public float getSquareWidth() {
        return mSquareWidth;
    }

    public float getBitmapWidth() {
        return mBitmapWidth;
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.max(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        return result;
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        /*
         * View should be large enough to contain MATRIX_WIDTH side-by-side
         * target bitmaps
         */
        return MATRIX_WIDTH * mBitmapWidth;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        /*
         * View should be large enough to contain MATRIX_WIDTH side-by-side
         * target bitmaps
         */
        return MATRIX_WIDTH * mBitmapWidth;
    }

    private int getSuggestedMinimumWidth(int measureSpec) {
        int specSize = MeasureSpec.getSize(measureSpec);
        int totalBitmapWidth = MATRIX_WIDTH * mBitmapWidth;
        switch (mAspect) {
            case ASPECT_SQUARE_FILL:
                return Math.max((int) (specSize * 0.93), totalBitmapWidth);
            default:
                return Math.min(Math.max((int) (specSize * 0.82), totalBitmapWidth), (int) (1.5 * totalBitmapWidth));
        }
    }

    private int getSuggestedMinimumHeight(int measureSpec) {
        int specSize = MeasureSpec.getSize(measureSpec);
        int totalBitmapWidth = MATRIX_WIDTH * mBitmapWidth;
        switch (mAspect) {
            case ASPECT_SQUARE_FILL:
                return Math.max((int) (specSize * 0.93), totalBitmapWidth);
            default:
                return Math.min(Math.max((int) (specSize * 0.82), totalBitmapWidth), (int) (1.5 * totalBitmapWidth));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minimumWidth = getSuggestedMinimumWidth(widthMeasureSpec);
        final int minimumHeight = getSuggestedMinimumHeight(widthMeasureSpec);
        int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);

        switch (mAspect) {
            case ASPECT_SQUARE:
                int minWidth = Math.min(minimumWidth, viewWidth);
                int minHeight = Math.min(minimumHeight, viewHeight);
                int paddingTop = 0;
                if(minHeight < viewHeight) {
                    paddingTop = (viewHeight - minHeight) /2;
                }
                viewWidth = viewHeight = Math.min(minWidth, minHeight);
                viewHeight = viewHeight + paddingTop;
                mPaddingTop = paddingTop / 2;
                mPaddingBottom = mPaddingTop;
                break;
            case ASPECT_SQUARE_FILL:
                viewWidth = LockPatternUtils.getAspectSquareFillModePatternWidth(viewWidth, viewHeight, mPaddingBottom);
                viewHeight = LockPatternUtils.getAspectSquareFillModePatternHeight(viewWidth, mPaddingBottom);
                break;
        }

        /*
         * Log.v(TAG, "LockPatternView dimensions: " + viewWidth + "x" +
         * viewHeight);
         */
        setMeasuredDimension(viewWidth, viewHeight);
    }

    /**
     * Determines whether the point x, y will add a new point to the current
     * pattern (in addition to finding the cell, also makes heuristic choices
     * such as filling in gaps based on current pattern).
     *
     * @param x
     *            The x coordinate.
     * @param y
     *            The y coordinate.
     */
    private Cell detectAndAddHit(float x, float y) {
        final Cell cell = checkForNewHit(x, y);
        if (cell != null) {

            /*
             * check for gaps in existing pattern
             */
            Cell fillInGapCell = null;
            final ArrayList<Cell> pattern = mPattern;
            if (!pattern.isEmpty()) {
                final Cell lastCell = pattern.get(pattern.size() - 1);
                int dRow = cell.mRow - lastCell.mRow;
                int dColumn = cell.mColumn - lastCell.mColumn;

                int fillInRow = lastCell.mRow;
                int fillInColumn = lastCell.mColumn;

                if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
                    fillInRow = lastCell.mRow + ((dRow > 0) ? 1 : -1);
                }

                if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
                    fillInColumn = lastCell.mColumn + ((dColumn > 0) ? 1 : -1);
                }

                fillInGapCell = Cell.of(fillInRow, fillInColumn);
            }

            if (fillInGapCell != null
                    && !mPatternDrawLookup[fillInGapCell.mRow][fillInGapCell.mColumn]) {
                addCellToPattern(fillInGapCell);
            }
            addCellToPattern(cell);
            return cell;
        }
        return null;
    }

    private void addCellToPattern(Cell newCell) {
        mPatternDrawLookup[newCell.getRow()][newCell.getColumn()] = true;
        mPattern.add(newCell);
        notifyCellAdded();
    }

    /**
     * Helper method to find which cell a point maps to.
     *
     * @param x
     * @param y
     * @return
     */
    private Cell checkForNewHit(float x, float y) {

        final int rowHit = getRowHit(y);
        if (rowHit < 0) {
            return null;
        }
        final int columnHit = getColumnHit(x);
        if (columnHit < 0) {
            return null;
        }

        if (mPatternDrawLookup[rowHit][columnHit]) {
            return null;
        }
        return Cell.of(rowHit, columnHit);
    }

    /**
     * Helper method to find the row that y falls into.
     *
     * @param y
     *            The y coordinate
     * @return The row that y falls in, or -1 if it falls in no row.
     */
    private int getRowHit(float y) {

        final float squareHeight = mSquareHeight;
        float hitSize = squareHeight * mHitFactor;

        float offset = mPaddingTop + (squareHeight * mDotPivotY) - (hitSize / 2.0f);
        for (int i = 0; i < MATRIX_WIDTH; i++) {

            final float hitTop = offset + squareHeight * i;
            if (y >= hitTop && y <= hitTop + hitSize) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Helper method to find the column x fallis into.
     *
     * @param x
     *            The x coordinate.
     * @return The column that x falls in, or -1 if it falls in no column.
     */
    private int getColumnHit(float x) {
        final float squareWidth = mSquareWidth;
        float hitSize = squareWidth * mHitFactor;

        float offset = mPaddingLeft + (squareWidth - hitSize) / 2f;
        for (int i = 0; i < MATRIX_WIDTH; i++) {

            final float hitLeft = offset + squareWidth * i;
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mPatternInProgress){
                    getParent().requestDisallowInterceptTouchEvent(true);
                }else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mInputEnabled || !isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                return true;
            case MotionEvent.ACTION_UP:
                handleActionUp(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                return true;
            case MotionEvent.ACTION_CANCEL:
            /*
             * Original source check for mPatternInProgress == true first before
             * calling next three lines. But if we do that, there will be
             * nothing happened when the user taps at empty area and releases
             * the finger. We want the pattern to be reset and the message will
             * be updated after the user did that.
             */
                mPatternInProgress = false;
                resetPattern();
                notifyPatternCleared();
                return true;
        }
        return false;
    }

    private void handleActionMove(MotionEvent event) {
        /*
         * Handle all recent motion events so we don't skip any cells even when
         * the device is busy...
         */
        final float radius = (mSquareWidth * mDiameterFactor * 0.5f);
        final int historySize = event.getHistorySize();
        mTmpInvalidateRect.setEmpty();
        boolean invalidateNow = false;
        for (int i = 0; i < historySize + 1; i++) {
            final float x = i < historySize ? event.getHistoricalX(i) : event
                    .getX();
            final float y = i < historySize ? event.getHistoricalY(i) : event
                    .getY();
            Cell hitCell = detectAndAddHit(x, y);
            final int patternSize = mPattern.size();
            if (hitCell != null && patternSize == 1) {
                mPatternInProgress = true;
                notifyPatternStarted();
            }
            /*
             * note current x and y for rubber banding of in progress patterns
             */
            final float dx = Math.abs(x - mInProgressX);
            final float dy = Math.abs(y - mInProgressY);
            if (dx >= DRAG_THRESHHOLD || dy >= DRAG_THRESHHOLD) {
                invalidateNow = true;
            }

            if (mPatternInProgress && patternSize > 0) {
                final ArrayList<Cell> pattern = mPattern;
                final Cell lastCell = pattern.get(patternSize - 1);
                float lastCellCenterX = getCenterXForColumn(lastCell.mColumn);
                float lastCellCenterY = getCenterYForRow(lastCell.mRow);

                /*
                 * Adjust for drawn segment from last cell to (x,y). Radius
                 * accounts for line width.
                 */
                float left = Math.min(lastCellCenterX, x) - radius;
                float right = Math.max(lastCellCenterX, x) + radius;
                float top = Math.min(lastCellCenterY, y) - radius;
                float bottom = Math.max(lastCellCenterY, y) + radius;

                /*
                 * Invalidate between the pattern's new cell and the pattern's
                 * previous cell
                 */
                if (hitCell != null) {
                    final float width = mSquareWidth * 0.5f;
                    final float height = mSquareHeight * 0.5f;
                    final float hitCellCenterX = getCenterXForColumn(hitCell.mColumn);
                    final float hitCellCenterY = getCenterYForRow(hitCell.mRow);

                    left = Math.min(hitCellCenterX - width, left);
                    right = Math.max(hitCellCenterX + width, right);
                    top = Math.min(hitCellCenterY - height, top);
                    bottom = Math.max(hitCellCenterY + height, bottom);
                }

                /*
                 * Invalidate between the pattern's last cell and the previous
                 * location
                 */
                mTmpInvalidateRect.union(Math.round(left), Math.round(top),
                        Math.round(right), Math.round(bottom));
            }
        }
        mInProgressX = event.getX();
        mInProgressY = event.getY();

        /*
         * To save updates, we only invalidate if the user moved beyond a
         * certain amount.
         */
        if (invalidateNow) {
            mInvalidate.union(mTmpInvalidateRect);
            invalidate(mInvalidate);
            mInvalidate.set(mTmpInvalidateRect);
        }
    }

    private void handleActionUp(MotionEvent event) {
        /*
         * report pattern detected
         */
        if (!mPattern.isEmpty()) {
            mPatternInProgress = false;
            notifyPatternDetected();
            invalidate();
        }
    }

    private void handleActionDown(MotionEvent event) {
        resetPattern();
        final float x = event.getX();
        final float y = event.getY();
        final Cell hitCell = detectAndAddHit(x, y);
        if (hitCell != null) {
            mPatternInProgress = true;
            mPatternDisplayMode = DisplayMode.Correct;
            notifyPatternStarted();
        } else {
            /*
             * Original source check for mPatternInProgress == true first before
             * calling this block. But if we do that, there will be nothing
             * happened when the user taps at empty area and releases the
             * finger. We want the pattern to be reset and the message will be
             * updated after the user did that.
             */
            mPatternInProgress = false;
            notifyPatternCleared();
        }
        if (hitCell != null) {
            final float startX = getCenterXForColumn(hitCell.mColumn);
            final float startY = getCenterYForRow(hitCell.mRow);

            final float widthOffset = mSquareWidth / 2f;
            final float heightOffset = mSquareHeight / 2f;

            invalidate((int) (startX - widthOffset),
                    (int) (startY - heightOffset),
                    (int) (startX + widthOffset), (int) (startY + heightOffset));
        }
        mInProgressX = x;
        mInProgressY = y;
        mInProgressY = y;
    }

    private float getCenterXForColumn(int column) {
        return mPaddingLeft + column * mSquareWidth + mSquareWidth * mDotPivotX;
    }

    private float getCenterYForRow(int row) {
        return mPaddingTop + row * mSquareHeight + mSquareHeight * mDotPivotY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int duration = (int) (SystemClock.elapsedRealtime() - mAnimatingPeriodStart);
        checkIfAnimationEnd(duration);

        final ArrayList<Cell> pattern = mPattern;
        final int count = pattern.size();
        final boolean[][] drawLookup = mPatternDrawLookup;

        if (mPatternDisplayMode == DisplayMode.Animate) {
            /*
             * figure out which circles to draw
             */

            /*
             * + 1 so we pause on complete pattern
             */
            final int oneCycle = (count + 1) * mAnimationDuration;
            final int spotInCycle = duration % oneCycle;
            final int currentDrawCircleIndex = spotInCycle / mAnimationDuration;

            clearPatternDrawLookup();
            for (int i = 0; i < currentDrawCircleIndex; i++) {
                final Cell cell = pattern.get(i);
                drawLookup[cell.getRow()][cell.getColumn()] = true;
            }
            checkIfAnimationStart(currentDrawCircleIndex);
            /*
             * figure out in progress portion of ghosting line
             */

            final boolean needToUpdateInProgressPoint = currentDrawCircleIndex > 0
                    && currentDrawCircleIndex < count;

            if (needToUpdateInProgressPoint) {
                final float percentageOfNextCircle = ((float) (spotInCycle % mAnimationDuration))
                        / mAnimationDuration;

                final Cell currentCell = pattern.get(currentDrawCircleIndex - 1);
                final float centerX = getCenterXForColumn(currentCell.mColumn);
                final float centerY = getCenterYForRow(currentCell.mRow);

                final Cell nextCell = pattern.get(currentDrawCircleIndex);
                final float dx = percentageOfNextCircle
                        * (getCenterXForColumn(nextCell.mColumn) - centerX);
                final float dy = percentageOfNextCircle
                        * (getCenterYForRow(nextCell.mRow) - centerY);
                mInProgressX = centerX + dx;
                mInProgressY = centerY + dy;
                handleAnimationUpdate((int)mInProgressX, (int)mInProgressY);
            }
            /*
             * TODO: Infinite loop here...
             */
            if (duration <= oneCycle || mIsAnimationRepeat) {
                invalidate();
            }
        }

        final float squareWidth = mSquareWidth;
        final float squareHeight = mSquareHeight;

//        float radius = (squareWidth * mDiameterFactor);
        mGreenPathPaint.setStrokeWidth(PATH_PAINT_WIDTH);
        mRedPathPaint.setStrokeWidth(PATH_PAINT_WIDTH);

        final Path currentPath = mCurrentPath;
        currentPath.rewind();

        /*
         * draw the arrows associated with the path (unless the user is in
         * progress, and we are in stealth mode)
         */
        boolean oldFlag = (mPaint.getFlags() & Paint.FILTER_BITMAP_FLAG) != 0;
        /*
         * draw with higher quality since we render with transforms
         */
        mPaint.setFilterBitmap(true);

        /*
         * draw the circles
         */
        final int paddingTop = mPaddingTop;
        final int paddingLeft = mPaddingLeft;

        if (mInCircleMode) {
            for (int i = 0; i < MATRIX_WIDTH; i++) {
                float topY = paddingTop + i * squareHeight;
            /*
             * float centerY = mPaddingTop + i * mSquareHeight + (mSquareHeight
             * / 2);
             */
                for (int j = 0; j < MATRIX_WIDTH; j++) {
                    float leftX = paddingLeft + j * squareWidth;
                    drawCircle(canvas, (int) leftX, (int) topY, drawLookup[i][j]);
                }
            }
        }

        /*
         * TODO: the path should be created and cached every time we hit-detect
         * a cell only the last segment of the path should be computed here draw
         * the path of the pattern (unless the user is in progress, and we are
         * in stealth mode)
         */
        final boolean drawPath = (!mInStealthMode);

        if (drawPath) {
            boolean anyCircles = false;
            for (int i = 0; i < count; i++) {
                Cell cell = pattern.get(i);

                /*
                 * only draw the part of the pattern stored in the lookup table
                 * (this is only different in the case of animation).
                 */
                if (!drawLookup[cell.mRow][cell.mColumn]) {
                    break;
                }
                anyCircles = true;

                float centerX = getCenterXForColumn(cell.mColumn);
                float centerY = getCenterYForRow(cell.mRow);
                if (i == 0) {
                    currentPath.moveTo(centerX, centerY);
                } else {
                    currentPath.lineTo(centerX, centerY);
                }
            }

            /*
             * add last in progress section
             */
            if ((mPatternInProgress || mPatternDisplayMode == DisplayMode.Animate)
                    && anyCircles && count > 0) {
                currentPath.lineTo(mInProgressX, mInProgressY);
            }
            if(mPatternDisplayMode == DisplayMode.Wrong) {
//            	int alpha = mStrokeAlpha - (mStrokeAlpha* (mRedAnimationMaxCount - mRedAnimationCount)/mRedAnimationMaxCount) ;
//            	mRedPathPaint.setAlpha(alpha);
                canvas.drawPath(currentPath, mRedPathPaint);
            } else
                canvas.drawPath(currentPath, mGreenPathPaint);
        }

        if (mInArrowMode) {
            for (int i = 0; i < count - 1; i++) {
                Cell cell = pattern.get(i);
                Cell next = pattern.get(i + 1);

                /*
                 * only draw the part of the pattern stored in the lookup table
                 * (this is only different in the case of animation).
                 */
                if (!drawLookup[next.mRow][next.mColumn]) {
                    break;
                }

                float leftX = paddingLeft + cell.mColumn * squareWidth;
                float topY = paddingTop + cell.mRow * squareHeight;

                drawArrow(canvas, leftX, topY, cell, next);
            }
        }

        for (int i = 0; i < MATRIX_WIDTH; i++) {
            float topY = paddingTop + i * squareHeight;
            /*
             * float centerY = mPaddingTop + i * mSquareHeight + (mSquareHeight
             * / 2);
             */
            for (int j = 0; j < MATRIX_WIDTH; j++) {
                float leftX = paddingLeft + j * squareWidth;
                drawBtn(canvas, (int) leftX, (int) topY, drawLookup[i][j]);
            }
        }

        /*
         * restore default flag
         */
        mPaint.setFilterBitmap(oldFlag);
        if(mPatternDisplayMode == DisplayMode.Wrong
                && mRedAnimationCount > 0 ) {
            mRedAnimationCount--;
            postInvalidateDelayed(30);
        }
    }

    private void drawArrow(Canvas canvas, float leftX, float topY, Cell start,
                           Cell end) {
        boolean green = mPatternDisplayMode != DisplayMode.Wrong;

        final int endRow = end.mRow;
        final int startRow = start.mRow;
        final int endColumn = end.mColumn;
        final int startColumn = start.mColumn;

        /*
         * offsets for centering the bitmap in the cell
         */
        final int offsetX = ((int) mSquareWidth - mBitmapWidth) / 2;
        final int offsetY = ((int) mSquareHeight - mBitmapHeight) / 2;

        /*
         * compute transform to place arrow bitmaps at correct angle inside
         * circle. This assumes that the arrow image is drawn at 12:00 with it's
         * top edge coincident with the circle bitmap's top edge.
         */
        Bitmap arrow = green ? mBitmapArrowGreenUp : mBitmapArrowRedUp;
        if (arrow == null || arrow.isRecycled()) {
            return;
        }
        final int cellWidth = mBitmapWidth;
        final int cellHeight = mBitmapHeight;

        /*
         * the up arrow bitmap is at 12:00, so find the rotation from x axis and
         * add 90 degrees.
         */
        final float theta = (float) Math.atan2((double) (endRow - startRow),
                (double) (endColumn - startColumn));
        final float angle = (float) Math.toDegrees(theta) + 90.0f;

        /*
         * compose matrix
         */
        float sx = Math.min(mSquareWidth / mBitmapWidth, 1.0f);
        float sy = Math.min(mSquareHeight / mBitmapHeight, 1.0f);
        /*
         * transform to cell position
         */
        mArrowMatrix.setTranslate(leftX + offsetX, topY + offsetY);
        mArrowMatrix.preTranslate(mBitmapWidth / 2, mBitmapHeight / 2);
        mArrowMatrix.preScale(sx, sy);
        mArrowMatrix.preTranslate(-mBitmapWidth / 2, -mBitmapHeight / 2);
        /*
         * rotate about cell center
         */
        mArrowMatrix.preRotate(angle, cellWidth / 2.0f, cellHeight / 2.0f);
        /*
         * translate to 12:00 pos
         */
        mArrowMatrix.preTranslate((cellWidth - arrow.getWidth()) / 2.0f, arrow.getHeight());
        if (mPatternDisplayMode == DisplayMode.Wrong) {
            int alpha = 255 - (255/mRedAnimationMaxCount) * (mRedAnimationMaxCount - mRedAnimationCount);
            mPaint.setAlpha(alpha);
            canvas.drawBitmap(arrow, mArrowMatrix, mPaint);
            mPaint.setAlpha(255);
        } else {
            canvas.drawBitmap(arrow, mArrowMatrix, mPaint);
            mPaint.setAlpha(255);
        }
    }

    /**
     * @param canvas
     * @param leftX
     * @param topY
     * @param partOfPattern
     *            Whether this circle is part of the pattern.
     */
    private void drawCircle(Canvas canvas, int leftX, int topY,
                            boolean partOfPattern) {

        Bitmap outerCircle;
        mPaint.setAlpha(255);
        if (!partOfPattern
                || (mInStealthMode && mPatternDisplayMode != DisplayMode.Wrong)) {
            /*
             * unselected circle
             */
            outerCircle = mBitmapCircleDefault;
        } else if (mPatternInProgress || mPatternDisplayMode == DisplayMode.Correct || mPatternDisplayMode == DisplayMode.Animate) {
            /*
             * user is in middle of drawing a pattern
             */
            outerCircle = mBitmapCircleTouched;
        } else if (mPatternDisplayMode == DisplayMode.Wrong) {
            /*
             * the pattern is wrong
             */
            if (!mInStealthMode) {
                outerCircle = mBitmapCircleIncorrect;
            } else
                outerCircle = mBitmapCircleDefault;
        } else {
            throw new IllegalStateException("unknown display mode "
                    + mPatternDisplayMode);
        }

        if (outerCircle == null || outerCircle.isRecycled()) {
            return;
        }

        final int width = mBitmapWidth;
        final int height = mBitmapHeight;

        final float squareWidth = mSquareWidth;
        final float squareHeight = mSquareHeight;

        int offsetX = (int) ((squareWidth - width) / 2f);
        int offsetY = (int) ((squareHeight - height) / 2f);

        /*
         * Allow circles to shrink if the view is too small to hold them.
         */
        float sx = Math.min(mSquareWidth / mBitmapWidth, 1.0f);
        float sy = Math.min(mSquareHeight / mBitmapHeight, 1.0f);

        mCircleMatrix.setTranslate(leftX + offsetX, topY + offsetY);
        mCircleMatrix.preTranslate(mBitmapWidth / 2, mBitmapHeight / 2);
        mCircleMatrix.preScale(sx, sy);
        mCircleMatrix.preTranslate(-mBitmapWidth / 2, -mBitmapHeight / 2);

        if (mPatternDisplayMode == DisplayMode.Wrong && !mInStealthMode) {
            if (mBitmapCircleDefault != null) {
                canvas.drawBitmap(mBitmapCircleDefault, mCircleMatrix, mPaint);
            }
            int alpha = 255 - (255 / mRedAnimationMaxCount) * (mRedAnimationMaxCount - mRedAnimationCount);
            mPaint.setAlpha(alpha);
            canvas.drawBitmap(outerCircle, mCircleMatrix, mPaint);
            mPaint.setAlpha(255);
        } else {
            canvas.drawBitmap(outerCircle, mCircleMatrix, mPaint);
        }

    }

    private void drawBtn(Canvas canvas, int leftX, int topY,
                         boolean partOfPattern) {

        Bitmap innerCircle;
        if (!partOfPattern || (mInStealthMode && mPatternDisplayMode != DisplayMode.Wrong)) {
            /*
             * unselected circle
             */
            innerCircle = mBitmapBtnDefault;
        } else if (mPatternInProgress || (mPatternDisplayMode == DisplayMode.Correct) || mPatternDisplayMode == DisplayMode.Animate) {
            /*
             * user is in middle of drawing a pattern
             */

            innerCircle = mBitmapBtnTouched;
        } else if (mPatternDisplayMode == DisplayMode.Wrong) {
            /*
             * the pattern is wrong
             */
            innerCircle = mBitmapBtnIncorrect;
        }else {
            throw new IllegalStateException("unknown display mode "
                    + mPatternDisplayMode);
        }

        if (innerCircle == null || innerCircle.isRecycled()) {
            return;
        }

        final int width = mBitmapWidth;
        final int height = mBitmapHeight;

        final float squareWidth = mSquareWidth;
        final float squareHeight = mSquareHeight;

        int offsetX = (int) ((squareWidth - width) / 2f);
        int offsetY = (int) ((squareHeight - height) / 2f);

        float sx = Math.min(mSquareWidth / mBitmapWidth, 1.0f);
        float sy = Math.min(mSquareHeight / mBitmapHeight, 1.0f);

        final int cellWidth = mBitmapWidth;
        final int cellHeight = mBitmapHeight;
        mCircleBtnMatrix.setTranslate(leftX + offsetX, topY + offsetY);
        mCircleBtnMatrix.preTranslate(mBitmapWidth / 2, mBitmapHeight / 2);
        mCircleBtnMatrix.preScale(sx, sy);
        mCircleBtnMatrix.preTranslate(-mBitmapWidth / 2, -mBitmapHeight / 2);
        mCircleBtnMatrix.preTranslate((cellWidth - innerCircle.getWidth()) / 2.0f, (cellHeight - innerCircle.getHeight()) / 2);

        if (mPatternDisplayMode == DisplayMode.Wrong) {
//            if (mBitmapBtnDefault != null) {
//                canvas.drawBitmap(mBitmapBtnDefault, mCircleBtnMatrix, mPaint);
//            }
//            int alpha = 255 - (255/mRedAnimationMaxCount) * (mRedAnimationMaxCount - mRedAnimationCount);
//	    	mPaint.setAlpha(alpha);
            canvas.drawBitmap(innerCircle, mCircleBtnMatrix, mPaint);
            mPaint.setAlpha(255);
        } else {
            canvas.drawBitmap(innerCircle, mCircleBtnMatrix, mPaint);
        }
    }

    private static int resolveAttribute(Context context, int resId,
                                        int defaultValue) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(resId, typedValue, true))
            return typedValue.resourceId;
        return defaultValue;
    }// resolveAttribute()

    private boolean mIsCaptureLocationOnScreen = false;
    private boolean mIsAnimationStarted = false;
    private boolean mIsAnimationRepeat = true;
    private AnimationListener mAnimationListener;
    private int mAnimationDuration = MILLIS_PER_CIRCLE_ANIMATING;
    private static final int MILLIS_PER_CIRCLE_ANIMATING = 700;
    private long mAnimatingPeriodStart;

    public void setAnimationRepeat(boolean repeat) {
        mIsAnimationRepeat = repeat;
    }

    public void setAnimationDuration(int duration) {
        mAnimationDuration = duration / (getPattern().size() + 1);
    }

    public void setAnimationListener(AnimationListener listener) {
        mAnimationListener = listener;
    }

    public interface AnimationListener {
        public void onAnimationStart();
        public void onAnimationEnd();
        public void onAnimationUpdate(int x, int y);
    }

    public void setCaptureLocationOnScreen(boolean isCapture) {
        if(mIsCaptureLocationOnScreen != isCapture && isCapture) {
            requestLayout();
        }
        mIsCaptureLocationOnScreen = isCapture;
    }

    public int getLastCircleXOfPatternOnScreen(ArrayList<Cell> pattern) {
        if(pattern == null || pattern.size() <= 0) {
            return 0;
        }
        final Cell lastCell = pattern.get(pattern.size() - 1);
        return getXOnScreen(getLastCircleXOfPattern(lastCell));
    }

    private int getLastCircleXOfPattern(Cell lastCell) {
        if(lastCell == null) {
            return 0;
        }

        return (int) getCenterXForColumn(lastCell.mColumn);
    }

    private int getXOnScreen(int x) {
        if(mTempXY == null) {
            return x;
        }
        return mTempXY[0] + x;
    }

    private int getYOnScreen(int y) {
        if(mTempXY == null) {
            return y;
        }
        return mTempXY[1] + y;
    }

    private int[] mTempXY;

    public int getLastCircleYOfPatternOnScreen(ArrayList<Cell> pattern) {
        if(pattern == null || pattern.size() <= 0) {
            return 0;
        }
        final Cell lastCell = pattern.get(pattern.size() - 1);
        return getYOnScreen(getLastCircleYOfPattern(lastCell));
    }

    private int getLastCircleYOfPattern(Cell lastCell) {
        if(lastCell == null) {
            return 0;
        }
        return (int) getCenterYForRow(lastCell.mRow);
    }

    private void checkIfAnimationStart(int currShownCircles) {
        if(!mIsAnimationStarted && currShownCircles >= 1 && !mIsAnimationRepeat) {
            mIsAnimationStarted = true;
            if(mAnimationListener != null) {
                mAnimationListener.onAnimationStart();
            }
        }
    }

    private void handleAnimationUpdate(int x, int y) {
        if(mAnimationListener != null && !mIsAnimationRepeat) {
            mAnimationListener.onAnimationUpdate(getXOnScreen(x), getYOnScreen(y));
        }
    }

    protected int getCustomCircleWidth() {
        return mBitmapWidth;
    }

    protected int getCustomCircleHeight() {
        return mBitmapHeight;
    }

    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(mIsCaptureLocationOnScreen) {
            if (mTempXY == null) {
                mTempXY = new int[2];
            }
            getLocationOnScreen(mTempXY);
        }
    }


    private void checkIfAnimationEnd(int duration) {
        // Call this before drawing everything to avoid the flash issue.
        if(mPatternDisplayMode == DisplayMode.Animate && duration >= (mAnimationDuration * (mPattern.size() + 1)) && !mIsAnimationRepeat) {
            handleAnimationEnd();
        }
    }

    private void checkIfAnimationAborted() {
        if(mPatternDisplayMode == DisplayMode.Animate && mIsAnimationStarted && !mIsAnimationRepeat && mPattern != null) {
            handleAnimationEnd();
        }
    }

    private void handleAnimationEnd() {
        mPatternDisplayMode = DisplayMode.Correct;
        mIsAnimationStarted = false;
        if (mAnimationListener != null) {
            // report the origin location of the last circle.
            final Cell lastCell = mPattern.get(mPattern.size() - 1);
            handleAnimationUpdate(getLastCircleXOfPattern(lastCell), getLastCircleYOfPattern(lastCell));
            mAnimationListener.onAnimationEnd();
        }
    }
}

