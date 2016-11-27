package com.ecitypower.www.smartbms;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

import devlight.io.library.ntb.NavigationTabBar;

/**
 * Created by GIGAMOLE on 28.03.2016.
 */
public class TabBarActivity extends FragmentActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_bar);
        f = new ArrayList<Fragment>();
        initUI();
    }

    public void test(){
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fff = fm.getFragments();
        ((FirstFragment)fff.get(3)).testTV();


    }

    private ViewPager viewPager;
    ArrayList<Fragment> f;

    private void initUI() {
        viewPager = (ViewPager) findViewById(R.id.tabBar);
        viewPager.setOffscreenPageLimit(5);

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                    if (position == 0){
                        return StatusActivity.newInstance();
                    }

                    return FirstFragment.newInstance("New instance" + position);


            }

            @Override
            public int getCount() {
                return 3;
            }
        });
//        viewPager.setAdapter(new PagerAdapter() {
//            @Override
//            public int getCount() {
//                return 5;
//            }
//
//            @Override
//            public boolean isViewFromObject(final View view, final Object object) {
//                return view.equals(object);
//            }
//
//            @Override
//            public void destroyItem(final View container, final int position, final Object object) {
//                ((ViewPager) container).removeView((View) object);
//            }
//
//            @Override
//            public Object instantiateItem(final ViewGroup container, final int position) {
//                Log.i("a", "current position " + Integer.toString(position));
//                final View view = LayoutInflater.from(
//                        getBaseContext()).inflate(R.layout.item_vp, null, false);
//
//                final TextView txtPage = (TextView) view.findViewById(R.id.txt_vp_item_page);
//                txtPage.setText(String.format("Page #%d", position));
//
//                container.addView(view);
//                return view;
//            }
//        });

        final String[] colors = getResources().getStringArray(R.array.tabBarColors);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.battery),
                        Color.parseColor(colors[0]))
                        .selectedIcon(getResources().getDrawable(R.drawable.battery_click))
                        .title("Data")
                        .badgeTitle("NTB")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.chart),
                        Color.parseColor(colors[1]))
                        .selectedIcon(getResources().getDrawable(R.drawable.chart_click))
                        .title("Chart")
                        .badgeTitle("with")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.setting),
                        Color.parseColor(colors[2]))
                        .selectedIcon(getResources().getDrawable(R.drawable.setting_click))
                        .title("Setting")
                        .badgeTitle("state")
                        .build()
        );
//        models.add(
//                new NavigationTabBar.Model.Builder(
//                        getResources().getDrawable(R.drawable.ic_fourth),
//                        Color.parseColor(colors[3]))
////                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
//                        .title("Flag")
//                        .badgeTitle("icon")
//                        .build()
//        );
//        models.add(
//                new NavigationTabBar.Model.Builder(
//                        getResources().getDrawable(R.drawable.ic_fifth),
//                        Color.parseColor(colors[4]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
//                        .title("Medal")
//                        .badgeTitle("777")
//                        .build()
//        );

        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 0);
        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                navigationTabBar.getModels().get(position).hideBadge();
            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });

        navigationTabBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < navigationTabBar.getModels().size(); i++) {
                    final NavigationTabBar.Model model = navigationTabBar.getModels().get(i);
                    navigationTabBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            model.showBadge();
                        }
                    }, i * 100);
                }
            }
        }, 500);
    }
}
