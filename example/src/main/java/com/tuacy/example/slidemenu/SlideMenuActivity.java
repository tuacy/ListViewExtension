package com.tuacy.example.slidemenu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tuacy.example.R;
import com.tuacy.example.base.BaseActivity;

public class SlideMenuActivity extends BaseActivity{

	public static void startUp(Context context) {
		context.startActivity(new Intent(context, SlideMenuActivity.class));
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_slide_menu);
	}
}
