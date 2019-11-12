package com.cainiao.fragment;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cainiao.R;
import com.cainiao.activity.MainActivity;
import com.cainiao.adapter.MineAdapter;
import com.cainiao.base.BaseFragment;
import com.cainiao.base.MyApp;
import com.cainiao.bean.Mine;
import com.cainiao.util.AppUtil;
import com.cainiao.util.Const;
import com.cainiao.util.DialogUtil;
import com.cainiao.util.Utils;
import com.cainiao.view.toasty.MyToast;

import java.util.ArrayList;
import java.util.List;

public class MineFragment extends BaseFragment {

    private List<Mine> mList;
    private ListView lv;
    private MineAdapter mAdapter;
    private String a = "";

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_mine;
    }

    @Override
    protected void init(View view) {
        a = String.format("剩余%d天", MyApp.getLog());
        lv = view.findViewById(R.id.lv);
        initData();
        lv.setAdapter(mAdapter = new MineAdapter(getActivity(), mList));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> view, View view1, int position, long l) {
                listItemClick(position);
            }
        });
    }

    private void initData(){
        mList = new ArrayList<>();
        mList.add(new Mine(R.mipmap._60x60, "我的菜鸟", a, "点我续费"));
        mList.add(new Mine(R.mipmap._60x60, "获取设备码", "换绑设备时使用", "如需换绑请找客服"));
        mList.add(new Mine(R.mipmap._60x60, "购卡地址", "不推荐使用", "点我前往，仅限购买不到激活码的用户使用"));
        mList.add(new Mine(R.mipmap._60x60, "菜鸟抢单App", "下载地址", Const.OUTER_DOWNLOAD_URL));
        mList.add(new Mine(R.mipmap._60x60, "问题反馈", "点我跳转QQ", Const.SERVICE_QQ));
    }

    /**
     * 激活成功回调
     */
    @Override
    protected void activeSuccess() {
        findUser();
    }

    /**
     * 查询用户是否激活回调
     */
    @Override
    protected void findUserCallback() {
        Mine mine = mList.get(0);
        mine.setMsg(String.format("剩余%d天", MyApp.getLog()));
        mList.set(0, mine);
        mAdapter.notifyDataSetChanged();
    }

    private void listItemClick(int position){
        switch (position){
            case 0:     //我的菜鸟
                DialogUtil.get().showInputAlertDialog(getActivity(), getString(R.string.mine_renew), getString(R.string.home_input_code), getString(R.string.mine_renew_tips), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        active(DialogUtil.get().getInputStr());
                    }
                });
                break;
            case 1:     //获取设备码
                Utils.setClipboardStr(Utils.getUuid());
                MyToast.success("设备码复制成功");
                break;
            case 2:     //购卡地址
                AppUtil.openUrlInOuter(Const.OUTER_BUY_URL);
                break;
            case 3:     //菜鸟抢单App
                AppUtil.openUrlInOuter(Const.OUTER_DOWNLOAD_URL);
                break;
            case 4:     //问题反馈
                if(AppUtil.isInstalled("com.tencent.mobileqq")){
                    AppUtil.openUrlInOuter(Const.SERVICE_URI);
                }
                break;
        }
    }
}
