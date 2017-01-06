package com.ecitypower.www.smartbms;

/**
 * Created by Fangming on 1/5/17.
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by Fangming on 11/26/16.
 */

public class ParamSettingActivity extends Activity {

    private ParamSettingListAdapter mParamSettingListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_param_setting);

        mParamSettingListAdapter = new ParamSettingListAdapter();
        ListView paramList = (ListView)findViewById(R.id.setting_Param_list);
        paramList.setAdapter(mParamSettingListAdapter);
    }


//    ListView paramList = (ListView)statusView.findViewById(R.id.setting_list);
//    paramList.setAdapter(mSettingListAdapter);

    private class ParamSettingListAdapter extends BaseAdapter {
        private LayoutInflater mInflator;

        public ParamSettingListAdapter() {
            super();
            mInflator = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            switch (position) {
                default:
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_section, viewGroup, false);
                    }
                    return view;

                case 1:
                    TextView paramTitle_Current;
                    TextView paramUnit_Current;
                    //EditText paramEdit_Current;

                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_input, viewGroup, false);
                    }
                    paramTitle_Current = (TextView) view.findViewById(R.id.Title);
                    paramTitle_Current.setText(R.string.Set_Current_Limit);

                    paramUnit_Current = (TextView) view.findViewById(R.id.Unit);
                    paramUnit_Current.setText(R.string.Current_Unit);


                    return view;
                case 2:
                    TextView paramTitle_Voltage;
                    TextView paramUnit_Voltage;
                    //EditText paramEdit_Current;

                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_input, viewGroup, false);
                    }
                    paramTitle_Voltage = (TextView) view.findViewById(R.id.Title);
                    paramTitle_Voltage.setText(R.string.Set_Voltage_Limit);

                    paramUnit_Voltage = (TextView) view.findViewById(R.id.Unit);
                    paramUnit_Voltage.setText(R.string.Voltage_Unit);

                    return view;

                case 3:
                    TextView paramTitle_LED;
                    TextView paramUnit_LED;
                    //EditText paramEdit_Current;

                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_input, viewGroup, false);
                    }
                    paramTitle_LED = (TextView) view.findViewById(R.id.Title);
                    paramTitle_LED.setText(R.string.Set_LED_Toggle_Current);

                    paramUnit_LED = (TextView) view.findViewById(R.id.Unit);
                    paramUnit_LED.setText(R.string.Current_Unit);

                    return view;


            }


        }
    }
}
