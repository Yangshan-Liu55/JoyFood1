package com.example.shan.joyfood;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * Created by shan on 2018-03-05.
 */

public class MyFragmentAdapter extends FragmentStatePagerAdapter {

    Context context;
    List<Fragment> listFragment; //listFragment用来存储显示页面对象。

    public MyFragmentAdapter(FragmentManager fm, Context context, List<Fragment> listFragment) {
        super(fm);
        this.context = context;
        this.listFragment = listFragment;
    }

    @Override
    public Fragment getItem(int position) {
        return listFragment.get(position);
    }

    @Override
    public int getCount() {
        return listFragment.size();
    }
}
