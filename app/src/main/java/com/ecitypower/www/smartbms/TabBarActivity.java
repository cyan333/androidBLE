package com.ecitypower.www.smartbms;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import devlight.io.library.ntb.NavigationTabBar;

//https://github.com/DevLight-Mobile-Agency/NavigationTabBar

/**
 * Created by GIGAMOLE on 28.03.2016.
 */
public class TabBarActivity extends FragmentActivity {

    private BluetoothDevice connectedDevice;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_bar);
        f = new ArrayList<Fragment>();
        initUI();
    }

    public void LEDControl(String ledControl){
        FragmentManager fragmentManager = getSupportFragmentManager();

        List<Fragment> fragments = fragmentManager.getFragments();
        Log.i("debug","LED::" + ledControl);
        //status Activity Tab bar index 1
        ((StatusActivity) fragments.get(1)).writeData(ledControl);
    }

    public void saveBLENameAddress (String connectedDeviceName, String connectedDeviceAddress){
        FragmentManager fragmentManager = getSupportFragmentManager();

        List<Fragment> fragments = fragmentManager.getFragments();
        ((SettingActivity) fragments.get(0)).saveDeviceAddressandName(connectedDeviceName,connectedDeviceAddress);
    }

    private ViewPager viewPager;
    ArrayList<Fragment> f;

    private void initUI() {
        viewPager = (ViewPager) findViewById(R.id.tabBar);
        viewPager.setOffscreenPageLimit(2);

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                    switch (position){
                        default: return StatusActivity.newInstance();
                        case 1: return FirstFragment.newInstance("New instance" + position);
                        case 2: return SettingActivity.newInstance();
                    }

            }

            @Override
            public int getCount() {
                return 3;
            }
        });

        final String[] colors = getResources().getStringArray(R.array.tabBarColors);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.battery),
                        Color.parseColor(colors[0]))
                        .selectedIcon(getResources().getDrawable(R.drawable.battery_click))
                        .title(getResources().getString(R.string.Tab_Data))
                        .badgeTitle("NTB")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.chart),
                        Color.parseColor(colors[1]))
                        .selectedIcon(getResources().getDrawable(R.drawable.chart_click))
                        .title(getResources().getString(R.string.Tab_Chart))
                        .badgeTitle("with")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.setting),
                        Color.parseColor(colors[2]))
                        .selectedIcon(getResources().getDrawable(R.drawable.setting_click))
                        .title(getResources().getString(R.string.Tab_Setting))
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
        navigationTabBar.setIsBadged(false);
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
