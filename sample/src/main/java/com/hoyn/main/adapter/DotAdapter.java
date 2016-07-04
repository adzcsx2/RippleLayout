package com.hoyn.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hoyn.circlerippleview.R;


/**
 * Created by Hoyn on 2016/6/24.
 */
public class DotAdapter extends BaseAdapter{

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        convertView =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dot,parent,false);

        TextView tv = (TextView) convertView.findViewById(R.id.tv);
        tv.setText("Click Me");
        return convertView;
    }
}
