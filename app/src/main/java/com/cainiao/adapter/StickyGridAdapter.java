package com.cainiao.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cainiao.R;
import com.cainiao.bean.Platform;
import com.cainiao.util.Const;
import com.cainiao.view.stickygridheaders.StickyGridHeadersSimpleAdapter;

import java.util.List;

public class StickyGridAdapter extends BaseAdapter implements
        StickyGridHeadersSimpleAdapter {

    private List<Platform> items;
    private LayoutInflater mInflater;
    private Context mContext;

    public StickyGridAdapter(Context context, List<Platform> items) {
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.items = items;
    }


    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_platform, parent, false);
            mViewHolder.tvStatus = convertView.findViewById(R.id.tv_status);
            mViewHolder.ivIcon = convertView.findViewById(R.id.iv_icon);
            mViewHolder.tvName = convertView.findViewById(R.id.tv_name);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        Platform item = items.get(position);

        mViewHolder.ivIcon.setImageResource(item.getResId());
        mViewHolder.tvName.setText(item.getName());
        mViewHolder.tvStatus.setText(item.getStatusTip(item.getStatus()));
        ColorStateList cs;
        if(item.getStatus() == Const.BJSHA){  //空闲中
            cs = mContext.getResources().getColorStateList(R.color.platform_BJSHA_text);
        }else if(item.getStatus() == Const.KSHG_AW){    //接单成功
            cs = mContext.getResources().getColorStateList(R.color.platform_success_text);
        }else{
            cs = mContext.getResources().getColorStateList(R.color.platform_free_text);
        }
        mViewHolder.tvStatus.setTextColor(cs);
        return convertView;
    }


    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder mHeaderHolder;

        if (convertView == null) {
            mHeaderHolder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.item_platform_header, parent, false);
            mHeaderHolder.tvHeader = convertView.findViewById(R.id.tv_header);
            convertView.setTag(mHeaderHolder);
        } else {
            mHeaderHolder = (HeaderViewHolder) convertView.getTag();
        }
        Platform item = items.get(position);
        mHeaderHolder.tvHeader.setText(item.getHeaderTip(item.getHeaderId()));
        return convertView;
    }

    /**
     * 获取HeaderId, 只要HeaderId不相等就添加一个Header
     */
    @Override
    public long getHeaderId(int position) {
        return items.get(position).getHeaderId();
    }


    public static class ViewHolder {
        TextView tvStatus;
        ImageView ivIcon;
        TextView tvName;
    }

    public static class HeaderViewHolder {
        TextView tvHeader;
    }

}
