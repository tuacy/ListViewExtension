package com.tuacy.example.slidemenu.adapter;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tuacy.example.R;
import com.tuacy.slidemenu.adapter.SlideMenuBaseAdapter;

import java.util.ArrayList;

public class SlideMenuListAdapter extends SlideMenuBaseAdapter {

	private ArrayList<String> mData;

	public SlideMenuListAdapter(Context context, ArrayList<String> data) {
		super(context);
		mData = data;
	}

	@Override
	public int getSlideContentLayoutId(int position) {
		return R.layout.item_slide_content;
	}

	@Override
	public int getSlideLeftLayoutId(int position) {
		return R.layout.item_slide_left;
	}

	@Override
	public int getSlideRightLayoutId(int position) {
		return R.layout.item_slide_right;
	}

	@Override
	public int getCount() {
		return mData == null ? 0 : mData.size();
	}

	@Override
	public String getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = createConvertView(position);
			viewHolder = new ViewHolder();
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		return convertView;
	}

	private static class ViewHolder {
		Button mTitle;
		Button mEdit;
		Button mDelete;
		Button mDetail;
	}
}
