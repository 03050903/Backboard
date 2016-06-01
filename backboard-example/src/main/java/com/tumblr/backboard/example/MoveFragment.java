package com.tumblr.backboard.example;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.facebook.rebound.SpringSystem;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.MotionProperty;
import com.tumblr.backboard.imitator.Imitator;

/**
 * Demonstrates a draggable view that bounces back when released.
 * <p/>
 * Created by ericleong on 5/7/14.
 */
public class MoveFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_move, container, false);

		new Actor.Builder(SpringSystem.create(), rootView.findViewById(R.id.circle))
				.addTranslateMotion(Imitator.TRACK_DELTA, Imitator.FOLLOW_EXACT, MotionProperty.X)
				.addTranslateMotion(Imitator.TRACK_DELTA, Imitator.FOLLOW_EXACT, MotionProperty.Y)
				.build();

		return rootView;
	}

	/**
     	Performer 是将 Spring 当前的值赋给 现在执行动画view的属性中
	 这个类是非常牛逼的 完全可以将View.xxx 任何属性传入,然后根据Spring的update回调来更新view对应的属性
	 具体可看源码 {@link com.tumblr.backboard.performer.Performer}

        Imitator 持续扰乱(perturbs) 其依附的 Spring,这种扰乱(Perturbs)会发生在很多情况下:
        EventImitator 是将 MotionEvent 映射到 Spring 中(//TODO 暂时没理解)

	 //以下四个参数完全可以通过设置不同的值看实际的效果
	 Imitator.TRACK_ABSOLUTE = 1; // 拖动的点始终会跳转到view的center
	 Imitator.TRACK_DELTA = 2;//在view的范围内点击拖动,拖动的点就会在哪里

     Imitator.FOLLOW_EXACT //表示拖动view的时候会将current spring value立马赋值给view
	 Imitator.FOLLOW_SPRING //表示拖动view的时候 只会将 end spring value 赋值给view，view位置有相应的延迟
	 */
}




















