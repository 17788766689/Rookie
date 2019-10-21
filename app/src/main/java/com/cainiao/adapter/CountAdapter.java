package com.cainiao.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cainiao.R;
import com.cainiao.bean.SelectTotal;

import java.util.List;

/**
 * Created by 123 on 2019/9/26.
 */

public class CountAdapter extends BaseAdapter{

    private List<SelectTotal.Data> mList;
    private LayoutInflater mInflater;

    public CountAdapter(Context context, List<SelectTotal.Data> list){
        mList = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_count, parent, false);
            mViewHolder.tvName = convertView.findViewById(R.id.tv_name);
            mViewHolder.tvTotalNum = convertView.findViewById(R.id.tv_total_num);
            mViewHolder.tvTodayNum = convertView.findViewById(R.id.tv_today_num);
            mViewHolder.tvLastTime = convertView.findViewById(R.id.tv_last_time);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        SelectTotal.Data data = mList.get(position);
        mViewHolder.tvName.setText((position + 1) + ". " + data.getTaskName());
        mViewHolder.tvTotalNum.setText(data.getTotalNumber());
        mViewHolder.tvTodayNum.setText(data.getTodayNumber());
        mViewHolder.tvLastTime.setText(data.getLastTime());
        return convertView;
    }

    class ViewHolder{
        TextView tvName;
        TextView tvTotalNum;
        TextView tvTodayNum;
        TextView tvLastTime;
    }
}
