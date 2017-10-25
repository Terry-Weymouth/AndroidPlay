package org.weymouth.sensorlist;

import android.os.Bundle;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    SensorManager smm;
    List<Sensor> sensor;
    ListView lv;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        tv = (TextView) findViewById(R.id.textView01);
        lv = (ListView) findViewById (R.id.listView01);
        sensor = smm.getSensorList(Sensor.TYPE_ALL);
        int size = sensor.size();
        String label = "There are " + size + " sensors.";
        lv.setAdapter(new ArrayAdapter<Sensor>(this, android.R.layout.simple_list_item_1,  sensor));
        tv.setText(label);
    }
}