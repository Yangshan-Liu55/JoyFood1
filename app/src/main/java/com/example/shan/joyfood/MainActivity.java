package com.example.shan.joyfood;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainActivity extends BaseActivity {

    //private static final String TAG = "MainActivity";

    // Bottom bar
    private Stack<Integer> stackFragment; //instance is very import!
    private ViewPager viewPager;
    private BottomNavigationView navigation;//same as mOnNavigationItemSelectedListener
    List<Fragment> listFragment; //存储页面对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    //Bottom bar ViewPager Initialize view
    private void initView() {
        viewPager = (ViewPager)findViewById(R.id.view_pager);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);//can declare in onCreate

        //向ViewPager添加各页面
        listFragment = new ArrayList<>();
        listFragment.add(new HomeFragment());
        listFragment.add(new NewPostFragment());
        listFragment.add(new LoginFragment());

        MyFragmentAdapter myFragmentAdapter = new MyFragmentAdapter(getSupportFragmentManager(), this, listFragment);
        viewPager.setAdapter(myFragmentAdapter);

        //add current fragment to stack, when BackPressed pop stack first before finish Activity
        stackFragment= new Stack<Integer>();
        stackFragment.removeAllElements();
        stackFragment.push(0);

        //导航栏点击事件和ViewPager滑动事件,让两个控件相互关联
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //这里设置为：当点击到某子项，ViewPager就取listFragment中对应位置的fragment(0:Home, 1:NewPost, 2:Login)
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        viewPager.setCurrentItem(0);
                        //如果有TextView希望显示与navigation_home同名，则可以在这里textView.setText(R.id.navigation_home)
                        if (!stackFragment.lastElement().equals(0)) { stackFragment.push(0);}
                        //avoid repeating back stack between two fragment
//                        if (stackFragment.contains(0)){
//                            stackFragment.remove(0);
//                        }
//                        stackFragment.push(0);

                        return true;
                    case R.id.navigation_add:
                        viewPager.setCurrentItem(1);
                        if (!stackFragment.lastElement().equals(1)) { stackFragment.push(1);}

                        return true;
                    case R.id.navigation_setting:
                        viewPager.setCurrentItem(2);
                        if (!stackFragment.lastElement().equals(2)) { stackFragment.push(2);}

                        return true;
                    default:
                        break;
                }
                return false;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //注意这个方法滑动时会调用多次，下面是参数解释：
                //position当前所处页面索引,滑动调用的最后一次绝对是滑动停止所在页面
                //positionOffset:表示从位置的页面偏移的[0,1]的值。
                //positionOffsetPixels:以像素为单位的值，表示与位置的偏移
            }

            @Override
            public void onPageSelected(int position) {
                //该方法只在滑动停止时调用，position滑动停止所在页面位置
                //当滑动到某一位置，导航栏对应位置被按下
                navigation.getMenu().getItem(position).setChecked(true);
                //navigation.setSelectedItemId(position);
                //这里使用navigation.setSelectedItemId(position);无效，
                //setSelectedItemId(position)的官网原句：Set the selected
                // menu item ID. This behaves the same as tapping on an item
                //未找到原因
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // 这个方法在滑动时调用三次，分别对应下面三种状态
                // 这个方法对于发现用户何时开始拖动，
                // 何时寻呼机自动调整到当前页面，或何时完全停止/空闲非常有用。
                // state表示新的滑动状态，有三个值：
//                SCROLL_STATE_IDLE：开始滑动（空闲状态->滑动），实际值为0
//                SCROLL_STATE_DRAGGING：正在被拖动，实际值为1
//                SCROLL_STATE_SETTLING：拖动结束,实际值为2
            }
        });

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (stackFragment.size() > 1) {
            stackFragment.pop();
            viewPager.setCurrentItem(stackFragment.lastElement());
        } else {
            finish(); // finish the activity
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
