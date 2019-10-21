package com.cainiao.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cainiao.R;
import com.cainiao.bean.Mine;

import java.util.List;

/**
 * Created by 123 on 2019/9/26.
 */

public class MineAdapter extends BaseAdapter{

    private List<Mine> mList;
    private LayoutInflater mInflater;

    public MineAdapter(Context context, List<Mine> list){
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
            convertView = mInflater.inflate(R.layout.item_mine, parent, false);
            mViewHolder.ivIcon = convertView.findViewById(R.id.iv_icon);
            mViewHolder.tvTitle = convertView.findViewById(R.id.tv_title);
            mViewHolder.tvMsg = convertView.findViewById(R.id.tv_msg);
            mViewHolder.tvTips = convertView.findViewById(R.id.tv_tips);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        Mine mine = mList.get(position);
        mViewHolder.ivIcon.setImageResource(mine.getResId());
        mViewHolder.tvTitle.setText(mine.getTitle());
        mViewHolder.tvMsg.setText(mine.getMsg());
        mViewHolder.tvTips.setText(mine.getTips());
        return convertView;
    }

    class ViewHolder{
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvMsg;
        TextView tvTips;
    }
}
