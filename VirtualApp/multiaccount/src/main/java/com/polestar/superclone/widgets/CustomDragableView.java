package com.polestar.superclone.widgets;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

import com.polestar.superclone.utils.DisplayUtils;

import java.util.ArrayList;

/**
 * reference By ViewPager {@link android.support.v4.view.ViewPager}
 * Created by yxx on 2016/7/18.
 */
public class CustomDragableView extends ViewGroup {

    public final static boolean DEBUG = true;

    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips
    private static final int CLOSE_ENOUGH = 2; // dp
    private static final int DEFAULT_PADDING_TOP = 0; // dp
    private static final int DEFAULT_PADDING_BOTTOM = 50; // dp
    private static final int DEFAULT_PADDING_LEFT = 20; // dp
    private static final int DEFAULT_PADDING_RIGHT = 20; // dp
    private static final int MAX_SETTLE_DURATION = 600; // ms

    private static final int DEFAULT_COL_COUNT = 3;//默认列数
    private static final int DEFAULT_ROW_COUNT = 4;//默认行数
    private static final int DEFAULT_GUTTER_SIZE = 1;//默认间距

    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;

    public static final int DRAG_OUTSIDE_TOP_LEFT = 0;
    public static final int DRAG_OUTSIDE_TOP_RIGHT = 1;
    public static final int DRAG_OUTSIDE_BOTTOM_LEFT = 2;
    public static final int DRAG_OUTSIDE_BOTTOM_RIGHT = 3;

    public static final float SCALE_FLOAT = 0.9091f;

    private static final int MIN_FLING_VELOCITY = 400;

    private static final int EDGE_LFET = 0;
    private static final int EDGE_RIGHT = 1;

    private static final int DEFAULT_CONTETN_PERCENT = 7;
    private static final int DEFAULT_PADDINGBOTTOM_PERCENT = 1;

    private static final long ANIMATION_DURATION = 150; // ms
    private static final long EDGE_HOLD_DURATION = 800; // ms

    private int mOverscrollDistance;
    /**
     * 判断手指是否滑动的临界点，只有当滑动距离大于mTouchSlop时才会触发ACTION_MOVE
     */
    private int mTouchSlop;

    private Scroller mScroller;
    private boolean mIsScrollStarted;

    /**
     * 控制动画执行速率
     */
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };
    /**
     * 跟踪滑动速率，这里用于快速滑动时切换页面
     */
    private VelocityTracker mVelocityTracker;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mFlingDistance;
    private int mCloseEnough;

    private int mColCount = DEFAULT_COL_COUNT;
    private int mRowCount = DEFAULT_ROW_COUNT;
    private int mGapSize;

    private int paddingBottomPercent = DEFAULT_PADDINGBOTTOM_PERCENT;
    private int contentPercent = DEFAULT_CONTETN_PERCENT;
    private float layoutPercent = 7f / 8f;
    private int mPageCount;
    private int mPageSize = mColCount * mRowCount;
    private int itemWidth;
    private int itemHeight;
    private int currentPage;
    private int mEdgeSize;
    private boolean mIsBeingDragged;
    private boolean mIsUnableToDrag;
    private int mScrollState = SCROLL_STATE_IDLE;
    private final Runnable mEndScrollRunnable = new Runnable() {
        public void run() {
            setScrollState(SCROLL_STATE_IDLE);
        }
    };

    /**
     * Position of the last motion event.
     */
    private float mLastMotionX;
    private float mLastMotionY;
    private float mInitialMotionX;
    private float mInitialMotionY;
    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;
    /**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;
    private ArrayList<Integer> newPositions = new ArrayList<Integer>();

    private ListAdapter mAdapter;
    private DataSetObserver mDataSetObserver;

    private OnPageChangeListener mOnPageChangeListener;
    private AdapterView.OnItemClickListener mOnItemClickListener;
    private AdapterView.OnItemLongClickListener mOnItemLongClickListener;
    private OnRearrangeListener mOnRearrangeListener;
    private OnDragListener onDragListener;

    private Context mContext;
    private boolean overScrollEnabled;

    private int mLastPosition = -1;

    // rearrange
    private int mLastDragged = -1;
    private int mLastTarget = -1;

    private int mLastEdge = -1;
    private boolean hasPostForChangePage;
    private int paddingLeft;
    private int paddingRight;
    private int paddingTop;
    private int paddingBottom;

    public interface OnPageChangeListener extends ViewPager.OnPageChangeListener {
        void onPageCountChanged(int pageCount);
    }

    public interface OnRearrangeListener {

        void onRearrange(int oldIndex, int newIndex);
    }


    public interface OnDragListener {

        void onDragStart(View view);

        void onDragEnd(View view);

        boolean onDragOutSide(int dragLocation);

        boolean completeDragOutSide(int dragLocation, int itemPosition);

        boolean onCancleDragOutSide();
    }

    public CustomDragableView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public CustomDragableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    protected void initView() {
        setFocusable(true);
        setWillNotDraw(false);
        //焦点先分发给childView处理，如果所有childView都没处理，则自己处理
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        setChildrenDrawingOrderEnabled(true);

        final float density = mContext.getResources().getDisplayMetrics().density;

        mScroller = new Scroller(mContext, sInterpolator);

        ViewConfiguration localViewConfiguration = ViewConfiguration.get(getContext());
        mTouchSlop = localViewConfiguration.getScaledTouchSlop();
        mMinimumVelocity = localViewConfiguration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = localViewConfiguration.getScaledMaximumFlingVelocity();
        mOverscrollDistance = localViewConfiguration.getScaledOverscrollDistance();

        mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);
        mCloseEnough = (int) (CLOSE_ENOUGH * density);
        mGapSize = (int) (DEFAULT_GUTTER_SIZE * density);
        paddingBottom = (int) (DEFAULT_PADDING_BOTTOM * density);
        paddingTop = (int) (DEFAULT_PADDING_TOP * density);
        paddingRight = (int) (DEFAULT_PADDING_RIGHT * density);
        paddingLeft = (int) (DEFAULT_PADDING_LEFT * density);

    }

    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            removeAllViews();
            scrollTo(0, 0);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mDataSetObserver = new DataSetObserver() {
                @Override
                public void onChanged() {
                    onDataSetChanged();
                }
            };
            mAdapter.registerDataSetObserver(mDataSetObserver);
            for (int i = 0; i < mAdapter.getCount(); i++) {
                final View child = mAdapter.getView(i, null, this);
                addView(child);
            }
            changePageCount();
        }
    }

    private void changePageCount() {
        int newPageCount = (getChildCount() + mPageSize - 1) / mPageSize;
        if (newPageCount != mPageCount && mOnPageChangeListener != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    mOnPageChangeListener.onPageCountChanged(newPageCount);
                }
            });
        }
        mPageCount = newPageCount;
    }

    public int getPageCount() {
        return mPageCount;
    }

    private void onDataSetChanged() {
        while (getChildCount() > mAdapter.getCount()) {
            removeViewAt(getChildCount() - 1);
        }

        for (int i = 0; i < getChildCount() && i < mAdapter.getCount(); i++) {
            final View child = getChildAt(i);
            final View newChild = mAdapter.getView(i, child, this);
            if (newChild != child) {
                removeViewAt(i);
                addView(newChild, i);
            }
        }

        for (int i = getChildCount(); i < mAdapter.getCount(); i++) {
            final View child = mAdapter.getView(i, null, this);
            addView(child);
        }

        changePageCount();
        if (currentPage >= mPageCount && mPageCount > 0) {
            setCurrentPage(mPageCount - 1);
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * {@link android.support.v4.view.ViewPager}
     */
    public void setCurrentPage(int item) {
        setCurrentPageInternal(item, false, false);
    }

    public void setCurrentPage(int item, boolean smoothScroll) {
        setCurrentPageInternal(item, smoothScroll, false);
    }

    void setCurrentPageInternal(int item, boolean smoothScroll, boolean always) {
        setCurrentPageInternal(item, smoothScroll, always, 0);
    }

    void setCurrentPageInternal(int item, boolean smoothScroll, boolean always, int velocity) {
        if (item <= 0) {
            item = 0;
        } else if (item >= mPageCount) {
            item = mPageCount - 1;
        }
        final boolean dispatchSelected = currentPage != item;
        currentPage = item;
        scrollToItem(item, smoothScroll, velocity, dispatchSelected);
    }

    private void scrollToItem(int item, boolean smoothScroll, int velocity, boolean dispatchSelected) {
        final int destX = getWidth() * item;
        if (smoothScroll) {
            smoothScrollTo(destX, 0, velocity);
            if (dispatchSelected && mOnPageChangeListener != null) {
                onPageSelected(item);
            }
        } else {
            if (dispatchSelected && mOnPageChangeListener != null) {
                onPageSelected(item);
            }
            completeScroll(false);
            scrollTo(destX, 0);
            pageScrolled(destX);
        }
    }

    private void onPageSelected(int item){
        post(new Runnable() {
            @Override
            public void run() {
                mOnPageChangeListener.onPageSelected(item);
            }
        });
    }

    /**
     * {@link android.support.v4.view.ViewPager}
     */
    void smoothScrollTo(int x, int y) {
        smoothScrollTo(x, y, 0);
    }

    void smoothScrollTo(int x, int y, int velocity) {
        int sx;
        boolean wasScrolling = (mScroller != null) && !mScroller.isFinished();
        if (wasScrolling) {
            // We're in the middle of a previously initiated scrolling. Check to see
            // whether that scrolling has actually started (if we always call getStartX
            // we can get a stale value from the scroller if it hadn't yet had its first
            // computeScrollOffset call) to decide what is the current scrolling position.
            sx = mIsScrollStarted ? mScroller.getCurrX() : mScroller.getStartX();
            // And abort the current scrolling.
            mScroller.abortAnimation();
        } else {
            sx = getScrollX();
        }
        int sy = getScrollY();
        int dx = x - sx;
        int dy = y - sy;
        if (dx == 0 && dy == 0) {
            completeScroll(false);
            setScrollState(SCROLL_STATE_IDLE);
            return;
        }

        setScrollState(SCROLL_STATE_SETTLING);

        final int width = getWidth();
        final int halfWidth = width / 2;
        final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / width);
        final float distance = halfWidth + halfWidth *
                distanceInfluenceForSnapDuration(distanceRatio);

        int duration = 0;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            final float pageDelta = (float) Math.abs(dx) / width;
            duration = (int) ((pageDelta + 5) * 100);
        }
        duration = Math.min(duration, MAX_SETTLE_DURATION);

        mIsScrollStarted = false;
        mScroller.startScroll(sx, sy, dx, dy, duration);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void computeScroll() {
        mIsScrollStarted = true;
        if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if (oldX != x || oldY != y) {
                scrollTo(x, y);
                if (!pageScrolled(x)) {
                    mScroller.abortAnimation();
                    scrollTo(0, y);
                }
            }

            // Keep on drawing until the animation has finished.
            ViewCompat.postInvalidateOnAnimation(this);
            return;
        }

        // Done with scroll, clean up state.
        completeScroll(true);
    }

    private boolean pageScrolled(int xpos) {
        if (mPageCount <= 0) {
            onPageScrolled(0, 0, 0);
            return false;
        }
        int width = getWidth();
        if(width <= 0){
            width = DisplayUtils.getScreenWidth(mContext);
        }
        final int currentPage = xpos / width;
        final int offsetPixels = xpos - currentPage * width;
        final float pageOffset = (float) offsetPixels / (float) width;

        onPageScrolled(currentPage, pageOffset, offsetPixels);
        return true;
    }

    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrolled(position, offset, offsetPixels);
        }
    }

    float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    private void completeScroll(boolean postEvents) {
        if (mScrollState == SCROLL_STATE_SETTLING) {
            // Done with scroll, no longer want to cache view drawing.
            mScroller.abortAnimation();
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            if (oldX != x || oldY != y) {
                scrollTo(x, y);
            }
            if (postEvents) {
                ViewCompat.postOnAnimation(this, mEndScrollRunnable);
            } else {
                mEndScrollRunnable.run();
            }
        }
    }

    private void setScrollState(int newState) {
        if (mScrollState == newState) {
            return;
        }
        mScrollState = newState;
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrollStateChanged(newState);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

        // Always take care of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the drag.
            mActivePointerId = INVALID_POINTER;
            endDrag();
            return false;
        }

        // Nothing more to do here if we have decided whether or not we
        // are dragging.
        if (action != MotionEvent.ACTION_DOWN) {
            if (mIsBeingDragged) {
                return true;
            }
            if (mIsUnableToDrag) {
                return false;
            }
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                * Locally do absolute value. mLastMotionY is set to the y value
                * of the down event.
                */
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float dx = x - mLastMotionX;
                final float xDiff = Math.abs(dx);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float yDiff = Math.abs(y - mInitialMotionY);

                if (xDiff > mTouchSlop && xDiff * 0.5f > yDiff) {
                    mIsBeingDragged = true;
                    requestParentDisallowInterceptTouchEvent(true);
                    setScrollState(SCROLL_STATE_DRAGGING);
                    mLastMotionX = dx > 0 ? mInitialMotionX + mTouchSlop :
                            mInitialMotionX - mTouchSlop;
                    mLastMotionY = y;
                } else if (yDiff > mTouchSlop) {
                    // The finger has moved enough in the vertical
                    // direction to be counted as a drag...  abort
                    // any attempt to drag horizontally, to work correctly
                    // with children that have scrolling containers.
                    mIsUnableToDrag = true;
                }
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    if (performDrag(x)) {
                        ViewCompat.postInvalidateOnAnimation(this);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                if (ev.getY() > getContentHeight()) {
                    return false;
                }
                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsUnableToDrag = false;

                mIsScrollStarted = true;
                mScroller.computeScrollOffset();
                if (mScrollState == SCROLL_STATE_SETTLING &&
                        Math.abs(mScroller.getFinalX() - mScroller.getCurrX()) > mCloseEnough) {
                    // Let the user 'catch' the pager as it animates.
                    mScroller.abortAnimation();
                    mIsBeingDragged = true;
                    requestParentDisallowInterceptTouchEvent(true);
                    setScrollState(SCROLL_STATE_DRAGGING);
                } else {
                    completeScroll(false);
                    mIsBeingDragged = false;
                }

                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
            // Don't handle edge touches immediately -- they may actually belong to one of our
            // descendants.
            return false;
        }

        if (mAdapter == null || mAdapter.getCount() == 0) {
            // Nothing to present or scroll; nothing to touch.
            return false;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();
        boolean needsInvalidate = false;
        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if (ev.getY() > getContentHeight()) {
                    return false;
                }

                mScroller.abortAnimation();
                // Remember where the motion event started
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

                if (!mIsBeingDragged && mScrollState == SCROLL_STATE_IDLE) {
                    mLastPosition = getPositionByXY((int) mLastMotionX, (int) mLastMotionY);
                } else {
                    mLastPosition = -1;
                }
                if (mLastPosition >= 0) {
                    checkForLongClick();
                }
                mLastDragged = -1;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                removeCallbacks(nextCheckForChangePageOnDrag);
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                if (!pointInChildView(ev.getX(), y, mTouchSlop)) {
                    removeCallbacks(mPendingCheckForLongPress);
                }
                if (mLastDragged >= 0) {
                    // change draw location of dragged visual
                    final View v = getChildAt(mLastDragged);
                    Rect rect = getDragedRectByPosition(mLastDragged);
//                    final int l = (int) (rect.left  + x - mLastMotionX);
//                    final int t = (int) (rect.top + y - mLastMotionY);
                    final int l = (int) (rect.left - itemWidth * (1 - SCALE_FLOAT) / 2 + x - mLastMotionX);

                    final int t = (int) (rect.top - itemHeight * (1 - SCALE_FLOAT) / 2 + y - mLastMotionY);
//                    final int l = getScrollX() + (int) x - v.getWidth() / 2;
//                    final int t = getScrollY() + (int) y - v.getHeight() / 2;
                    v.layout(l, t, l + v.getWidth(), t + v.getHeight());

                    // check for new target hover
                    if (mScrollState == SCROLL_STATE_IDLE) {
                        isDragOutside(x, y);
                        final int target = getTargetByXY((int) x, (int) y);
                        if (target != -1 && mLastTarget != target) {
                            animateGap(target);
                            mLastTarget = target;
                        }
                        // edge holding
                        final int edge = getEdgeByXY((int) x, (int) y);
                        if(edge != -1){
                            if(!hasPostForChangePage){
                                hasPostForChangePage = true;
                                mLastEdge = edge;
                                checkForChangePageOnDrag();
                            }else if (edge != mLastEdge) {
                                mLastEdge = edge;
                                removeCallbacks(mCheckForChangePageOnDrag);
                                checkForChangePageOnDrag();
                            }
                        }else{
                            removeCallbacks(mCheckForChangePageOnDrag);
                        }
                        if (mLastEdge == -1) {
                            if (edge != mLastEdge) {
                                mLastEdge = edge;
                                checkForChangePageOnDrag();
                            }
                        } else {
                            if (edge != mLastEdge) {
                                mLastEdge = -1;
                                removeCallbacks(mCheckForChangePageOnDrag);
                                checkForChangePageOnDrag();
                            }
                        }
                    }
                } else if (!mIsBeingDragged) {
                    final float xDiff = Math.abs(x - mLastMotionX);
                    final float yDiff = Math.abs(y - mLastMotionY);

                    if (xDiff > mTouchSlop && xDiff > yDiff) {
                        removeCallbacks(mPendingCheckForLongPress);
                        mIsBeingDragged = true;
                        requestParentDisallowInterceptTouchEvent(true);
                        mLastMotionX = x - mInitialMotionX > 0 ? mInitialMotionX + mTouchSlop :
                                mInitialMotionX - mTouchSlop;
                        mLastMotionY = y;
                        setScrollState(SCROLL_STATE_DRAGGING);
                    }
                }
                // Not else! Note that mIsBeingDragged can be set above.
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    needsInvalidate |= performDrag(x);
                } else if (mLastPosition != getPositionByXY((int) x, (int) y)) {
                    mLastPosition = -1;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                removeCallbacks(mPendingCheckForLongPress);
                removeCallbacks(mCheckForChangePageOnDrag);
                removeCallbacks(nextCheckForChangePageOnDrag);
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                if (mLastDragged >= 0) {
                    if (onDragListener != null) {
                        onDragListener.onDragEnd(getChildAt(mLastDragged));
                    }
                    boolean needRearRange = true;
                    if (dragOutSideLocation > 0) {
                        if (onDragListener != null) {
                            needRearRange = !onDragListener.completeDragOutSide(dragOutSideLocation, mLastDragged);
                        }
                        dragOutSideLocation = 0;
                    }
                    if (needRearRange) {
                        rearrange();
                    } else {
                        for (int i = 0; i < getChildCount(); i++) {
                            getChildAt(i).clearAnimation();
                        }
                        mLastDragged = -1;
                        mLastTarget = -1;
                    }
                } else if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) VelocityTrackerCompat.getXVelocity(velocityTracker, mActivePointerId);
                    final int width = getWidth();
                    final int scrollX = getScrollX();
                    final int currentPage = scrollX / width;
                    final int offsetPixels = scrollX - currentPage * width;
                    final float pageOffset = (float) offsetPixels / (float) width;
                    final int totalDelta = (int) (x - mInitialMotionX);
                    int nextPage = determineTargetPage(currentPage, pageOffset, initialVelocity, totalDelta);
                    setCurrentPageInternal(nextPage, true, true, initialVelocity);

                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                } else if (mLastPosition >= 0) {
                    final int currentPosition = getPositionByXY((int) x, (int) y);
                    if (currentPosition == mLastPosition) {
                        onItemClick(currentPosition);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
                removeCallbacks(mPendingCheckForLongPress);
                removeCallbacks(mCheckForChangePageOnDrag);
                removeCallbacks(nextCheckForChangePageOnDrag);
                if (mLastDragged >= 0) {
                    if (onDragListener != null) {
                        onDragListener.onDragEnd(getChildAt(mLastDragged));
                    }
                    rearrange();
                } else if (mIsBeingDragged) {
                    scrollToItem(currentPage, true, 0, false);
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                if (dragOutSideLocation > 0) {
                    dragOutSideLocation = 0;
                    onDragListener.onCancleDragOutSide();
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                removeCallbacks(mPendingCheckForLongPress);
//                final int index = MotionEventCompat.getActionIndex(ev);
//                final float x = MotionEventCompat.getX(ev, index);
//                mLastMotionX = x;
//                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                removeCallbacks(mPendingCheckForLongPress);
                onSecondaryPointerUp(ev);
//                mLastMotionX = MotionEventCompat.getX(ev,
//                        MotionEventCompat.findPointerIndex(ev, mActivePointerId));
                break;
        }
        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
        return true;
    }

    private int dragOutSideLocation;

    private void isDragOutside(float x, float y) {
        if (onDragListener == null)
            return;
        if (y < 0) {
            if (x <= getWidth() / 2) {
                dragOutSideLocation = DRAG_OUTSIDE_TOP_LEFT;
                onDragListener.onDragOutSide(DRAG_OUTSIDE_TOP_LEFT);
            } else {
                dragOutSideLocation = DRAG_OUTSIDE_TOP_RIGHT;
                onDragListener.onDragOutSide(DRAG_OUTSIDE_TOP_RIGHT);
            }
        } else if ((y + itemHeight / 2) > getContentHeight()) {
            if (x <= getWidth() / 2) {
                dragOutSideLocation = DRAG_OUTSIDE_BOTTOM_LEFT;
                onDragListener.onDragOutSide(DRAG_OUTSIDE_BOTTOM_LEFT);
            } else {
                dragOutSideLocation = DRAG_OUTSIDE_BOTTOM_RIGHT;
                onDragListener.onDragOutSide(DRAG_OUTSIDE_BOTTOM_RIGHT);
            }
        } else {
            dragOutSideLocation = 0;
            onDragListener.onCancleDragOutSide();
        }
    }

    private int getPositionByXY(int x, int y) {
        final int col = (x - getPaddingLeft() - paddingLeft) / (itemWidth + mGapSize);
        final int row = (y - getPaddingTop() - paddingTop) / (itemHeight + mGapSize);
        if (x < getPaddingLeft() + paddingLeft || x >= (getPaddingLeft() + paddingLeft + col * (itemWidth + mGapSize) + itemWidth) ||
                y < getPaddingTop() + paddingTop || y >= (getPaddingTop() + paddingTop + row * (itemHeight + mGapSize) + itemHeight) ||
                col < 0 || col >= mColCount ||
                row < 0 || row >= mRowCount) {
            // touch in padding
            return -1;
        }
        final int position = currentPage * mPageSize + row * mColCount + col;
        if (position < 0 || position >= getChildCount()) {
            // empty item
            return -1;
        }
        if (!mAdapter.isEnabled(position)) {
            return -1;
        }
        return position;
    }

    private int getTargetByXY(int x, int y) {
        final int position = getPositionByXY(x, y);
        if (position < 0) {
            return -1;
        }
        final Rect r = getRectByPosition(position);
        final int page = position / mPageSize;
        r.inset(r.width() / 4, r.height() / 4);
        r.offset(-getWidth() * page, 0);
        if (!r.contains(x, y)) {
            return -1;
        }
        return position;
    }

    private int determineTargetPage(int currentPage, float pageOffset, int velocity, int deltaX) {
        int targetPage;
        if (Math.abs(deltaX) > mFlingDistance && Math.abs(velocity) > mMinimumVelocity) {
            targetPage = velocity > 0 ? currentPage : currentPage + 1;
        } else {
            final float truncator = currentPage >= currentPage ? 0.4f : 0.6f;
            targetPage = (int) (currentPage + pageOffset + truncator);
        }
        return targetPage;
    }


    private void onItemClick(int position) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, getChildAt(position), position, position / mColCount);
        }
    }

    private boolean onItemLongClick(int position) {
        if (mOnItemLongClickListener != null) {
            return mOnItemLongClickListener.onItemLongClick(null, getChildAt(position), position, position / mColCount);
        }
        return false;
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (mLastDragged == -1) {
            return i;
        } else if (i == childCount - 1) {
            return mLastDragged;
        } else if (i >= mLastDragged) {
            return i + 1;
        }
        return i;
    }

    private void animateDragged() {
        if (mLastDragged >= 0) {
            final View v = getChildAt(mLastDragged);
            final Rect r = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            r.inset(-r.width() / 20, -r.height() / 20);
            v.measure(MeasureSpec.makeMeasureSpec(r.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(r.height(), MeasureSpec.EXACTLY));
            v.layout(r.left, r.top, r.right, r.bottom);

//            AnimationSet animSet = new AnimationSet(true);
//            ScaleAnimation scale = new ScaleAnimation(SCALE_FLOAT, 1, SCALE_FLOAT, 1, v.getWidth() / 2, v.getHeight() / 2);
//            scale.setDuration(ANIMATION_DURATION);
//            AlphaAnimation alpha = new AlphaAnimation(1, .5f);
//            alpha.setDuration(ANIMATION_DURATION);
//
//            animSet.addAnimation(scale);
//            animSet.addAnimation(alpha);
//            animSet.setFillEnabled(true);
//            animSet.setFillAfter(true);
//
//            v.clearAnimation();
//            v.startAnimation(animSet);
        }
    }

    private void animateGap(int target) {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (i == mLastDragged) {
                continue;
            }

            int newPos = i;
            if (mLastDragged < target && i >= mLastDragged + 1 && i <= target) {
                newPos--;
            } else if (target < mLastDragged && i >= target && i < mLastDragged) {
                newPos++;
            }

            int oldPos = i;
            if (newPositions.get(i) != -1) {
                oldPos = newPositions.get(i);
            }

            if (oldPos == newPos) {
                continue;
            }

            // animate
            final Rect oldRect = getRectByPosition(oldPos);
            final Rect newRect = getRectByPosition(newPos);
            oldRect.offset(-v.getLeft(), -v.getTop());
            newRect.offset(-v.getLeft(), -v.getTop());

            TranslateAnimation translate = new TranslateAnimation(
                    oldRect.left, newRect.left,
                    oldRect.top, newRect.top);
            translate.setDuration(ANIMATION_DURATION);
            translate.setFillEnabled(true);
            translate.setFillAfter(true);
            v.clearAnimation();
            v.startAnimation(translate);

            newPositions.set(i, newPos);
        }
    }

    private void rearrange() {
        if (mLastDragged >= 0) {
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).clearAnimation();
            }
            if (mLastTarget >= 0 && mLastDragged != mLastTarget) {
                final View child = getChildAt(mLastDragged);
                removeViewAt(mLastDragged);
                addView(child, mLastTarget);
                if (mOnRearrangeListener != null) {
                    mOnRearrangeListener.onRearrange(mLastDragged, mLastTarget);
                }
            }
            mLastDragged = -1;
            mLastTarget = -1;
            requestLayout();
            invalidate();
        }
    }

    private int getEdgeByXY(int x, int y) {
        if (x < mEdgeSize) {
            return EDGE_LFET;
        } else if (x >= (getWidth() - mEdgeSize)) {
            return EDGE_RIGHT;
        }
        return -1;
    }

    private boolean triggerSwipe(int edge) {
        if (edge == EDGE_LFET && currentPage > 0) {
            setCurrentPage(currentPage - 1, true);
            if (mLastDragged >= 0) {
                // change draw location of dragged visual
                final View v = getChildAt(mLastDragged);
                v.layout(v.getLeft() - getWidth(), v.getTop(), v.getLeft() + v.getWidth() - getWidth(), v.getTop() + v.getHeight());
            }
            return true;
        } else if (edge == EDGE_RIGHT && currentPage < mPageCount - 1) {
            setCurrentPage(currentPage + 1, true);
            if (mLastDragged >= 0) {
                // change draw location of dragged visual
                final View v = getChildAt(mLastDragged);
                v.layout(v.getLeft() + getWidth(), v.getTop(), v.getLeft() + v.getWidth() + getWidth(), v.getTop() + v.getHeight());
            }
            return true;
        }
        return false;
    }


    private void endDrag() {
        mIsBeingDragged = false;
        mIsUnableToDrag = false;

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private boolean performDrag(float x) {
        boolean needsInvalidate = false;

        final float deltaX = mLastMotionX - x;
        mLastMotionX = x;

        float oldScrollX = getScrollX();
        float scrollX = oldScrollX + deltaX;
        final int width = getWidth();

        float leftBound = width * 0;
        float rightBound = width * (mPageCount - 1);

        if (scrollX < leftBound) {
            final float over = Math.min(leftBound - scrollX, mOverscrollDistance);
            scrollX = leftBound - over;
        } else if (scrollX > rightBound) {
            final float over = Math.min(scrollX - rightBound, mOverscrollDistance);
            scrollX = rightBound + over;
        }
        // Don't lose the rounded component
        mLastMotionX += scrollX - (int) scrollX;
        scrollTo((int) scrollX, getScrollY());
        pageScrolled((int) scrollX);

        return needsInvalidate;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = MotionEventCompat.getX(ev, newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private int getContentHeight() {
        return (int) (layoutPercent * getHeight());
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        final int childCount = getChildCount();
        itemWidth = (getWidth() - paddingLeft - paddingRight - (mColCount - 1) * mGapSize) / mColCount - getPaddingLeft() - getPaddingRight();
        itemHeight = (getContentHeight() - paddingTop - paddingBottom - (mRowCount - 1) * mGapSize) / mRowCount - getPaddingTop() - getPaddingBottom();
//        itemWidth = itemHeight = Math.min(itemWidth, itemHeight);
        mEdgeSize = itemWidth / 2;
        newPositions.clear();
        for (int j = 0; j < childCount; j++) {
            if (mLastDragged == j) {
                newPositions.add(-1);
                continue;
            }
            final View child = getChildAt(j);
            final Rect rect = getRectByPosition(j);
            child.measure(MeasureSpec.makeMeasureSpec(rect.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(rect.height(), MeasureSpec.EXACTLY));
            child.layout(rect.left, rect.top, rect.right, rect.bottom);
            newPositions.add(-1);
        }
    }

    private Rect getRectByPosition(int position) {
        final int page = position / mPageSize;
        final int row = (position % mPageSize) / mColCount;
        final int col = (position % mPageSize) % mColCount;
        final int left = getWidth() * page + getPaddingLeft() + col * (itemWidth + mGapSize) + paddingLeft;
        final int top = getPaddingTop() + row * (itemHeight + mGapSize) + paddingTop;
        return new Rect(left, top, left + itemWidth, top + itemHeight);
    }
    private Rect getDragedRectByPosition(int position) {
        final int page = position / mPageSize;
        final int offset =( currentPage - page ) * getWidth();
        final int row = (position % mPageSize) / mColCount;
        final int col = (position % mPageSize) % mColCount;
        final int left = getWidth() * page + getPaddingLeft() + col * (itemWidth + mGapSize) + paddingLeft + offset;
        final int top = getPaddingTop() + row * (itemHeight + mGapSize) + paddingTop;
        return new Rect(left, top, left + itemWidth, top + itemHeight);
    }

    public void setOnPageChangeListener(OnPageChangeListener mOnPageChangeListener) {
        this.mOnPageChangeListener = mOnPageChangeListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener mOnItemLongClickListener) {
        this.mOnItemLongClickListener = mOnItemLongClickListener;
    }

    public void setOnRearrangeListener(OnRearrangeListener mOnRearrangeListener) {
        this.mOnRearrangeListener = mOnRearrangeListener;
    }

    public void setOverScrollEnabled(boolean overScrollEnabled) {
        this.overScrollEnabled = overScrollEnabled;
    }

    public void setOnDragListener(OnDragListener onDragListener) {
        this.onDragListener = onDragListener;
    }

    public void setLayoutPercent(float percent) {
        this.layoutPercent = percent;
    }

    public void setGapSize(int mGapSize) {
        this.mGapSize = mGapSize;
    }

    public void setColCount(int colCount) {
        if (colCount < 1) {
            colCount = 1;
        }
        mColCount = colCount;
        mPageSize = mColCount * mRowCount;
        requestLayout();
    }

    public void setRowCount(int rowCount) {
        if (rowCount < 1) {
            rowCount = 1;
        }
        mRowCount = rowCount;
        mPageSize = mColCount * mRowCount;
        requestLayout();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentPage = savedState.curItem;
        requestLayout();
        if (currentPage > 0) {
            final int curPage = currentPage;
            currentPage = 0;
            setCurrentPage(curPage);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.curItem = currentPage;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int curItem;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            curItem = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(curItem);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    private Runnable mPendingCheckForLongPress;

    private void checkForLongClick() {
        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress();
        }
        postDelayed(mPendingCheckForLongPress,
                ViewConfiguration.getLongPressTimeout());
    }

    private Runnable mCheckForChangePageOnDrag;
    private Runnable nextCheckForChangePageOnDrag;

    private void checkForChangePageOnDrag() {
        if (mCheckForChangePageOnDrag == null) {
            mCheckForChangePageOnDrag = new CheckForChangePageOnDrag();
        }
        postDelayed(mCheckForChangePageOnDrag, EDGE_HOLD_DURATION);
    }
    private void nextCheckForChangePageOnDrag() {
        if (nextCheckForChangePageOnDrag == null) {
            nextCheckForChangePageOnDrag = new CheckForChangePageOnDrag();
        }
        postDelayed(nextCheckForChangePageOnDrag, EDGE_HOLD_DURATION);
    }

    class CheckForChangePageOnDrag implements Runnable {
        @Override
        public void run() {
            hasPostForChangePage = false;
            if (triggerSwipe(mLastEdge)) {
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
            nextCheckForChangePageOnDrag();
        }
    }

    class CheckForLongPress implements Runnable {

        public void run() {
            final int currentPosition = getPositionByXY((int) mLastMotionX, (int) mLastMotionY);
            if (currentPosition >= 0) {
                if (onItemLongClick(currentPosition)) {
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    mLastDragged = mLastPosition;
                    requestParentDisallowInterceptTouchEvent(true);
                    mLastTarget = -1;
                    animateDragged();
                    mLastPosition = -1;
                    if (mLastDragged >= 0 && onDragListener != null) {
                        onDragListener.onDragStart(getChildAt(mLastDragged));
                    }
                }
            }
        }

    }

    public boolean pointInChildView(float localX, float localY, float slop) {
        if (mLastPosition >= 0) {
            int page = mLastPosition / (mRowCount * mColCount);
            View child = getChildAt(mLastPosition);
//            MLogs.e("left = " + child.getLeft() + "\nx = " + localX);
//            MLogs.e("right = " + child.getRight() + "\nx = " + localX);
//            MLogs.e("top = " + child.getTop() + "\ny = " + localY);
//            MLogs.e("bottom = " + child.getRight() + "\ny = " + localY);
//            MLogs.e("page = " + page);
            if(child != null){
                return localX >= child.getLeft() - slop - page * getWidth() && localY >= child.getTop() - slop && localX < (float) child.getRight() - page * getWidth() +
                        slop && localY < (float) child.getBottom() + slop;
            }
        }
        return false;
    }

    public int getmPageSize() {
        return mPageSize;
    }
}
