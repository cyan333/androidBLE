package com.ecitypower.www.smartbms;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Fangming on 11/26/16.
 */

public class FirstFragment extends Fragment {

    private TextView tv;
    public static FirstFragment newInstance(String text) {

        FirstFragment f = new FirstFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Log.i("a", getArguments().getString("msg"));
        View v = inflater.inflate(R.layout.fragment_first, container, false);

        tv = (TextView) v.findViewById(R.id.tvLabel);
        tv.setText(getArguments().getString("msg"));

        Button b = (Button) v.findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv.setText("Scanning");
            }
        });

        return v;
    }

    public void testTV(){
        tv.setText("success");
        Log.i("a","hi, it's me");

    }



}