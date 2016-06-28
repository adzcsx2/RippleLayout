package com.sanniuben.circlerippleview.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.sanniuben.circlerippleview.R;
import com.sanniuben.circlerippleview.view.WaveView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "btn", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.btn_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "btn2", Toast.LENGTH_SHORT).show();
            }
        });

//        ListView listView = (ListView) findViewById(R.id.listView);
//        listView.setAdapter(new Adapter());
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(MainActivity.this, "listView "+position, Toast.LENGTH_SHORT).show();
//            }
//        });

        WaveView waveView = (WaveView) findViewById(R.id.waveView);
    }
}
