package com.tuacy.example.slidedelete;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tuacy.example.R;
import com.tuacy.example.base.BaseActivity;
import com.tuacy.example.slidedelete.adapter.DeleteAdapter;
import com.tuacy.slidedelete.SlideDeleteListView;

import java.util.ArrayList;
import java.util.List;

public class SlideDeleteActivity extends BaseActivity {

	public static void startUp(Context context) {
		context.startActivity(new Intent(context, SlideDeleteActivity.class));
	}

	private SlideDeleteListView mDeleteListView;
	private List<String>        mListData;
	private DeleteAdapter       mAdapter;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_slide_delete);
		initTestData();
		initView();
		initEvent();
		initData();
	}

	private void initView() {
		mDeleteListView = findView(R.id.delete_list_view);
	}

	private void initEvent() {

	}

	private void initData() {
		mAdapter = new DeleteAdapter(mContext, mListData);
		mDeleteListView.setAdapter(mAdapter);
	}

	private void initTestData() {
		mListData = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			mListData.add("滑动删除" + i);
		}
	}
}
