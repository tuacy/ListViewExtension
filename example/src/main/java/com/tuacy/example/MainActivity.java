package com.tuacy.example;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.tuacy.example.base.BaseActivity;
import com.tuacy.example.slidedelete.SlideDeleteActivity;
import com.tuacy.example.slidemenu.SlideMenuActivity;

public class MainActivity extends BaseActivity {

	private Button mSlideMenu;
	private Button mSlideDelete;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initEvent();
		initData();
	}

	private void initView() {
		mSlideMenu = findView(R.id.button_slide_menu);
		mSlideDelete = findView(R.id.button_slide_delete);
	}

	private void initEvent() {
		mSlideMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SlideMenuActivity.startUp(mContext);
			}
		});

		mSlideDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SlideDeleteActivity.startUp(mContext);
			}
		});
	}

	private void initData() {

	}
}
