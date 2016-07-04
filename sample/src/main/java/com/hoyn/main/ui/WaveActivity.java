package com.hoyn.main.ui;

/**
 * Created by Hoyn on 2016/7/4.
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hoyn.main.adapter.WaveAdapter;

/**
 * Created by Hoyn on 2016/7/4.
 */
public class WaveActivity extends AppCompatActivity {
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.hoyn.circlerippleview.R.layout.activity_wave);
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        findViewById(com.hoyn.circlerippleview.R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("btn");
            }
        });
        ListView listView = (ListView) findViewById(com.hoyn.circlerippleview.R.id.listView);
        listView.setAdapter(new WaveAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showToast("ListView:" + position);
            }
        });
    }

    private void showToast(String text) {
        toast.setText(text);
        toast.show();
    }

}
