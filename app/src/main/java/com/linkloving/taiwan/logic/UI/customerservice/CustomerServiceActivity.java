package com.linkloving.taiwan.logic.UI.customerservice;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.linkloving.taiwan.R;
import com.linkloving.taiwan.basic.toolbar.ToolBarActivity;
import com.linkloving.taiwan.logic.UI.customerservice.serviceItem.Feedback;
import com.linkloving.taiwan.logic.UI.customerservice.serviceItem.ServiceHotline;
import com.viewpagerindicator.TabPageIndicator;

public class CustomerServiceActivity extends ToolBarActivity {
    public final static String TAG = CustomerServiceActivity.class.getSimpleName();
    private Fragment[] myfragment;

    private ViewPager mViewPager;

    private TabPageIndicator titleIndicator;

    private int page_index;

    private static final String[]  TITLE= new String[1];
//            { "客服热线",getString(R.string.main_more_faq),"咨询反馈"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String str1=getString(R.string.main_more_rexian);
//        String str2=getString(R.string.main_more_faq);
//        String str3=getString(R.string.main_more_adv);
        TITLE[0]=str1;
//        TITLE[1]=str2;
//        TITLE[2]=str3;
        setContentView(R.layout.activity_customer_service);

    }

    @Override
    protected void getIntentforActivity() {
        page_index = getIntent().getIntExtra("__inedx__", Feedback.PAGE_INDEX_ONE);
    }

    @Override
    protected void initView() {
        SetBarTitleText(getString((R.string.service_center_title)));
        myfragment=new Fragment[1];
        myfragment[0]=new ServiceHotline();
//        myfragment[1]=new FQAPage();
//        myfragment[2]=new Feedback();

        mViewPager= (ViewPager) findViewById(R.id.service_center_ViewPager);
        //滑动便签
        titleIndicator= (TabPageIndicator) findViewById(R.id.service_center_indicator);
        titleIndicator.setVisibility(View.GONE);

        FragmentPagerAdapter adapter = new TabFragmentAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(adapter);

        titleIndicator.setViewPager(mViewPager);
        mViewPager.setCurrentItem(page_index);
        titleIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void initListeners() {

    }
    class TabFragmentAdapter extends FragmentPagerAdapter {
        private int mCount = TITLE.length;
        public TabFragmentAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            //新建一个Fragment来展示ViewPager item的内容，并传递参数
            return myfragment[position];
        }
        @Override
        public int getCount() {
            return mCount;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return TITLE[position];
        }
    }
}
