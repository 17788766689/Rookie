package com.cainiao.fragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.cainiao.R;
import com.cainiao.adapter.CountAdapter;
import com.cainiao.base.BaseFragment;
import com.cainiao.bean.SelectTotal;
import com.cainiao.util.HttpUtil;
import com.cainiao.view.toasty.MyToast;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.util.ArrayList;
import java.util.List;

public class CountFragment extends BaseFragment {

    private SwipeRefreshLayout mRefreshLayout;
    private ListView lv;
    private EditText etSearchName;
    private TextView tvSearch;
    private List<SelectTotal.Data> mList;
    private CountAdapter mAdapter;

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_count;
    }

    @Override
    protected void init(View view) {
        mList = new ArrayList<>();
        mRefreshLayout = view.findViewById(R.id.swiperefreshlayout);
        lv = view.findViewById(R.id.lv);
        etSearchName = view.findViewById(R.id.et_search_name);
        tvSearch = view.findViewById(R.id.tv_search);
        lv.setAdapter(mAdapter = new CountAdapter(getActivity(), mList));
        //设置下拉时圆圈的颜色（可以由多种颜色拼成）
        mRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light);
        //设置下拉刷新时的操作
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mList.clear();
                selectTotal("");
            }
        });

        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mList.clear();
                selectTotal(etSearchName.getText().toString().trim());
            }
        });
        selectTotal("");
    }

    /**
     * 查询接单统计
     * @param selectName 需要查询的平台名称
     */
    private void selectTotal(String selectName){
        HttpUtil.selectTotal(selectName, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if(mRefreshLayout.isRefreshing()){
                    mRefreshLayout.setRefreshing(false);
                    MyToast.success(getString(R.string.refresh_success));
                }
                if(TextUtils.isEmpty(response.body())) return;
                SelectTotal selectTotal = JSON.parseObject(response.body(), SelectTotal.class);
                mList.addAll(selectTotal.getData());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                if(mRefreshLayout.isRefreshing()) {
                    mRefreshLayout.setRefreshing(false);
                    MyToast.error(getString(R.string.refresh_fail));
                }
            }
        });
    }
}
