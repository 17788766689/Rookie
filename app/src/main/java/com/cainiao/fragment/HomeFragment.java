package com.cainiao.fragment;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.cainiao.R;
import com.cainiao.activity.ReceiptActivity;
import com.cainiao.adapter.StickyGridAdapter;
import com.cainiao.base.BaseFragment;
import com.cainiao.base.MyApp;
import com.cainiao.bean.Platform;
import com.cainiao.util.DialogUtil;
import com.cainiao.util.Platforms;
import com.cainiao.view.stickygridheaders.StickyGridHeadersGridView;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends BaseFragment implements TextWatcher{

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
        checkPermission(true);
        mGridView = view.findViewById(R.id.asset_grid);
        etSearchName = view.findViewById(R.id.et_search_name);
        mList = Platforms.getPlatforms();
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
    }


    @Override
    public void onResume() {
        super.onResume();
        if(null != mAdapter){
            mList = Platforms.getPlatforms();
            mAdapter.notifyDataSetChanged();
        }

    }


    /**
     * 点击平台
     * @param position
     */
    private void itemClick(int position){
        Platform platform = mList.get(position);
        if(null == platform){
            return;
        }
        if(platform.getStatus() != 0 || MyApp.getLog() >= 1){
            Platforms.setCurrPlatform(mList.get(position));
            startActivity(new Intent(getActivity(), ReceiptActivity.class).putExtra("position", position));
        }else{ //非免费且设备未激活
            DialogUtil.get().showInputAlertDialog(getActivity(), getString(R.string.home_un_active) + getString(R.string.home_reactive_tips), getString(R.string.home_input_code), getString(R.string.home_active_tips), new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    active(DialogUtil.get().getInputStr());
                }
            });
        }
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
