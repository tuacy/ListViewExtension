package com.tuacy.slidedelete;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class SlideDeleteBaseAdapter<T> extends BaseAdapter {

	protected Context mContext;
	protected List<T> mData;

	public SlideDeleteBaseAdapter(Context context) {
		this(context, null);
	}

	public SlideDeleteBaseAdapter(Context context, List<T> data) {
		mContext = context;
		mData = data;
	}

	public void setData(List<T> data) {
		mData = data;
		notifyDataSetChanged();
	}

	void dismiss(int position) {
		T dismissItem = mData.get(position);
		mData.remove(dismissItem);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mData == null ? 0 : mData.size();
	}

	@Override
	public T getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
