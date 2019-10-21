package com.cainiao.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cainiao.R;
import com.cainiao.bean.SelectTotal;
import com.cainiao.util.LogUtil;
import com.cainiao.util.Platforms;

import java.util.List;
import java.util.Map;

/**
 * Created by 123 on 2019/9/26.
 */

public class MakeListAdapter extends BaseAdapter{

    private List<SelectTotal.Data> mList;
    private LayoutInflater mInflater;
    private Map<String, Integer> mMap;

    public MakeListAdapter(Context context, List<SelectTotal.Data> list){
        mList = list;
        mInflater = LayoutInflater.from(context);
        mMap = Platforms.getPlatformIcons();
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
            convertView = mInflater.inflate(R.layout.item_makelist, parent, false);
            mViewHolder.ivIcon = convertView.findViewById(R.id.iv_icon);
            mViewHolder.tvName = convertView.findViewById(R.id.tv_name);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        SelectTotal.Data data = mList.get(position);
        if(mMap.containsKey(data.getImgName())){
            mViewHolder.ivIcon.setImageResource(mMap.get(data.getImgName()));
        }
        mViewHolder.tvName.setText(data.getTaskName());
        return convertView;
    }

    class ViewHolder{
        ImageView ivIcon;
        TextView tvName;
    }
}
