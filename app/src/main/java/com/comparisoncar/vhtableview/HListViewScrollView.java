package com.comparisoncar.vhtableview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class HListViewScrollView extends HorizontalScrollView {


	public HListViewScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	
	public HListViewScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HListViewScrollView(Context context) {
		super(context);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		//记录当前触摸的HListViewScrollView
		if(null!=this.listener){
			listener.setCurrentTouchView(this);
		}
		return super.onTouchEvent(ev);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		//当当前的HListViewScrollView被触摸时，滑动其它
		if(null!=this.listener&&null!=listener.getCurrentTouchView()&&listener.getCurrentTouchView() == this) {
			listener.onUIScrollChanged(l, t, oldl, oldt);
		}else{
			super.onScrollChanged(l, t, oldl, oldt);
		}
	}

	private ScrollChangedListener listener;
	public void setScrollChangedListener(ScrollChangedListener listener){
		this.listener=listener;
	}

	public interface ScrollChangedListener{

		public void setCurrentTouchView(HListViewScrollView currentTouchView);

		public HListViewScrollView getCurrentTouchView();
		public void onUIScrollChanged(int l, int t, int oldl, int oldt);
	}
}
