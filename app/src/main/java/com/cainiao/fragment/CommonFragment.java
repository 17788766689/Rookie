package com.cainiao.fragment;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.alibaba.fastjson.JSONArray;
import com.cainiao.R;
import com.cainiao.activity.ReceiptActivity;
import com.cainiao.adapter.StickyGridAdapter;
import com.cainiao.base.BaseFragment;
import com.cainiao.base.MyApp;
import com.cainiao.bean.Platform;
import com.cainiao.util.DbUtil;
import com.cainiao.util.DialogUtil;
import com.cainiao.util.LogUtil;
import com.cainiao.util.Platforms;
import com.cainiao.util.SPUtil;
import com.cainiao.view.stickygridheaders.StickyGridHeadersGridView;

import java.util.ArrayList;
import java.util.List;

/**
 * 常用
 */
public class CommonFragment extends BaseFragment implements TextWatcher{

    private List<Platform> mList;
    private List<Platform> mTempList;
    private StickyGridHeadersGridView mGridView;
    private StickyGridAdapter mAdapter;
    private EditText etSearchName;

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void init(View view) {
        findUser();
        mGridView = view.findViewById(R.id.asset_grid);
        etSearchName = view.findViewById(R.id.et_search_name);
        mList = Platforms.getLatestPlaforms();
        mTempList = new ArrayList<>();
        mTempList.addAll(mList);
        mAdapter = new StickyGridAdapter(getActivity(), mList);
        mGridView.setNumColumns(4); //设置一行有4个item
        mGridView.setAdapter(mAdapter);
        etSearchName.addTextChangedListener(this);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> view, View view1, int i, long l) {
                itemClick(i);
            }
        });
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> view, View view1, int i, long l) {
                itemLongClick(i);
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mList = Platforms.getLatestPlaforms();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 点击平台
     * @param position
     */
    private void itemClick(int position){
        Platform platform = mList.get(position);
        if(platform.getStatus() != 0 || MyApp.getTime() >= 1){
            List<Platform> list = Platforms.getPlatforms();
            for(int i = 0, len = list.size(); i < len; i++){   //获取点击进来的平台对应的下标，因为这里是从常用页面点击的
                if(TextUtils.equals(platform.getPkgName(), list.get(i).getPkgName())){
                    Platforms.setCurrPlatform(list.get(i));
                    startActivity(new Intent(getActivity(), ReceiptActivity.class).putExtra("position", i));
                    break;
                }
            }
        }else{ //非免费且设备未激活
            DialogUtil.get().showInputAlertDialog(getActivity(), getString(R.string.home_un_active) + getString(R.string.home_reactive_tips), getString(R.string.home_input_code), getString(R.string.home_active_tips), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    active(DialogUtil.get().getInputStr());
                }
            });
        }
    }

    /**
     * 长按平台，从常用里面删除
     * @param position
     */
    private void itemLongClick(int position){
        Platform platform = mList.remove(position);
        mAdapter.notifyDataSetChanged();
        DbUtil.delete(platform);
    }

    /**
     * 激活成功回调
     */
    @Override
    protected void activeSuccess() {
        findUser();
    }

    @Override
    public void beforeTextChanged(CharSequence sequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence sequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        String content = editable.toString().trim();
        mList.clear();
        if(content.trim().length() == 0){
            mList.addAll(mTempList);
        }else{
            for(Platform platform : mTempList){
                //如果平台名称或者状态里面包含有搜索的关键字，则将平台加入搜索结果的列表
                if(platform.getName().contains(content) || platform.statusArr[platform.getStatus()].contains(content)){
                    mList.add(platform);
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }
}
