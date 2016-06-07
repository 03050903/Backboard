package com.tumblr.backboard.example;

import android.app.Fragment;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.MotionProperty;
import com.tumblr.backboard.imitator.Imitator;
import com.tumblr.backboard.imitator.MotionImitator;
import com.tumblr.backboard.imitator.ToggleImitator;
import com.tumblr.backboard.performer.MapPerformer;
import com.tumblr.backboard.performer.Performer;

/**
 * A ring of views that bloom and then contract, with a selector that follows the finger.
 * <p/>
 * Created by ericleong on 5/7/14.
 */
public class FlowerFragment extends Fragment {

	private static final int DIAMETER = 50;
	private static final int RING_DIAMETER = 7 * DIAMETER;

	private RelativeLayout mRootView;
	private View mCircle;
	private View[] mCircles;

	private static final int OPEN = 1;
	private static final int CLOSED = 0;

	/**
	 * 计算两点之间的直线距离 其实属性并不差 只是没能灵活运用到程序的处理中 值得深思
     */
	private static double distSq(double x1, double y1, double x2, double y2) {
		return Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
	}

	/**
	 * 找到直线距离最小的View
     */
	private static View nearest(float x, float y, View[] views) {
		double minDistSq = Double.MAX_VALUE;
		View minView = null;

		for (View view : views) {
			double distSq = distSq(x, y, view.getX() + view.getMeasuredWidth() / 2,
					view.getY() + view.getMeasuredHeight() / 2);

			//两点之间的距离小于1.5倍 view宽度的时候 或者 小于记录的离view最小的距离
			if (distSq < Math.pow(1.0f * view.getMeasuredWidth(), 2) && distSq < minDistSq) {
				//记录最小的距离
				minDistSq = distSq;
				//记录距离最小的view
				minView = view;
			}
		}

		return minView;
	}

	/**
	 * Snaps to the nearest circle.
	 */
	private class SnapImitator extends MotionImitator {

		public SnapImitator(MotionProperty property) {
			super(property, 0, Imitator.TRACK_ABSOLUTE, Imitator.FOLLOW_SPRING);
		}

		@Override
		public void mime(float offset, float value, float delta, float dt, MotionEvent event) {
			// find the nearest view
			//event.getX() 是表示第一个点按下的x坐标 相对于view的x坐标
			//event.getX() + view.getX() 才表示实际移动到的x坐标 我在想为啥不用 getRawX

			//其实在此用getRawX 都是一样的效果 还少计算一次 但是在此有个问题就是 getRawY后 需要减去statusBar的高度
			//event.getY() + mCircle.getY() 通过此种方式替代 getRawY 方法能省去减statusBar的操作
			final View nearest = nearest(
					event.getX() + mCircle.getX(),
					event.getY() + mCircle.getY(), mCircles);

			if (nearest != null) {
                // snap to it - remember to compensate for translation
                //符合snap条件 直接移动到目标view的位置 x,y坐标都要变
                float centerNearestX = nearest.getX() + nearest.getWidth() / 2;
                float centerNearestY = nearest.getY() + nearest.getHeight() / 2;
                float centerCircleX = mCircle.getX() + mCircle.getWidth() / 2;
                float centerCircleY = mCircle.getY() + mCircle.getHeight() / 2;
                double sq = distSq(centerCircleX, centerCircleY, centerNearestX, centerNearestY);
                double sqrt = Math.sqrt(sq);

                if(sqrt != 0) {
                    Log.e("tag","sqrt:"+sqrt);
//                    float v = (float) SpringUtil.mapValueFromRangeToRange(sqrt, diameter, 0, 1, 1.5);
//                    nearest.setScaleX(v);
//                    nearest.setScaleY(v);
                    Spring spring = springSystem.createSpring();
                    spring.addListener(new MapPerformer(nearest, View.SCALE_X, diameter, 0, 1, 1.5f));
                    spring.addListener(new MapPerformer(nearest, View.SCALE_Y, diameter, 0, 1, 1.5f));
                    spring.setCurrentValue(sqrt);
                }

//                switch (mProperty) {
//					case X:
//						getSpring().setEndValue(nearest.getX() + nearest.getWidth() / 2
//								- mCircle.getLeft() - mCircle.getWidth() / 2);
//						break;
//					case Y:
//						getSpring().setEndValue(nearest.getY() + nearest.getHeight() / 2
//								- mCircle.getTop() - mCircle.getHeight() / 2);
//						break;
//				}

				//如果有触及到view,将对应的view放大处理
//				final SpringSystem springSystem = SpringSystem.create();
//				// create spring
//				final Spring spring = springSystem.createSpring();
//				spring.addListener(new Performer(nearest,View.SCALE_X));
//				spring.addListener(new Performer(nearest,View.SCALE_Y));
//				if(spring.getCurrentValue() != 1.2) {
//					spring.setCurrentValue(1.2);
//				}
			} else {
				//当没有触及到任何view时 重置所有Circle为初始状态
//				final SpringSystem springSystem = SpringSystem.create();
//				// create spring
				final Spring spring = springSystem.createSpring();
				for (View view : mCircles) {
					spring.addListener(new Performer(view, View.SCALE_X));
					spring.addListener(new Performer(view, View.SCALE_Y));
					spring.setCurrentValue(1);
				}

			}
				// follow finger
            super.mime(offset, value, delta, dt, event);
        }
	}

    final SpringSystem springSystem = SpringSystem.create();
    float diameter;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		mRootView = (RelativeLayout) inflater.inflate(R.layout.fragment_flower, container, false);

		mCircles = new View[6];
		mCircle = mRootView.findViewById(R.id.circle);

        diameter = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DIAMETER,
				getResources().getDisplayMetrics());

		final TypedArray circles = getResources().obtainTypedArray(R.array.circles);

		// layout params
		final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) diameter,
				(int) diameter);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);

		// create the circle views
		int colorIndex = 0;
		for (int i = 0; i < mCircles.length; i++) {
			mCircles[i] = new View(getActivity());

			mCircles[i].setLayoutParams(params);

			mCircles[i].setBackgroundDrawable(getResources().getDrawable(
					circles.getResourceId(colorIndex, -1)));

			colorIndex++;
			if (colorIndex >= circles.length()) {
				colorIndex = 0;
			}

			mRootView.addView(mCircles[i], 0);
		}

		//资源回收
		circles.recycle();

		/* Animations! */

		final SpringSystem springSystem = SpringSystem.create();

		// create spring
		final Spring spring = springSystem.createSpring();

		// add listeners along arc
		final double arc = 2 * Math.PI / mCircles.length;

		for (int i = 0; i < mCircles.length; i++) {
			View view = mCircles[i];

			// map spring to a line segment from the center to the edge of the ring
			spring.addListener(new MapPerformer(view, View.TRANSLATION_X, 0, 1,
					0, (float) (RING_DIAMETER * Math.cos(i * arc))));

			spring.addListener(new MapPerformer(view, View.TRANSLATION_Y, 0, 1,
					0, (float) (RING_DIAMETER * Math.sin(i * arc))));

			spring.setEndValue(CLOSED);
		}

		//专门处理单击事件的Imitator,处理了 ACTION_DOWN 和 ACTION_UP
		final ToggleImitator imitator = new ToggleImitator(spring, CLOSED, OPEN);

		// move circle using finger, snap when near another circle, and bloom when touched
		new Actor.Builder(SpringSystem.create(), mCircle)
				.addMotion(new SnapImitator(MotionProperty.X), View.TRANSLATION_X)
				.addMotion(new SnapImitator(MotionProperty.Y), View.TRANSLATION_Y)
				.onTouchListener(imitator)//截断默认的touch事件 用imitator来处理
				.build();

		return mRootView;
	}
}
