package com.tuacy.example.slidedelete.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tuacy.example.R;
import com.tuacy.slidedelete.SlideDeleteBaseAdapter;

import java.util.List;

public class DeleteAdapter extends SlideDeleteBaseAdapter<String> {


	public DeleteAdapter(Context context, List<String> data) {
		super(context, data);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_delete, parent, false);
			holder.mMsg = (TextView) convertView.findViewById(R.id.text_msg);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mMsg.setText(mData.get(position));
		return convertView;
	}

	private static class ViewHolder {

		TextView mMsg;
	}
}
