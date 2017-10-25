package org.weymouth.linearaccel;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.TextView;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "...accel.MainActivity";

    private float lastX, lastY, lastZ;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float deltaXMax = -1.0f;
    private float deltaYMax = -1.0f;
    private float deltaZMax = -1.0f;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private float displayX = 0;
    private float displayY = 0;
    private float displayZ = 0;

    private float getLastX = 0;
    private float getLastY = 0;
    private float getLastZ = 0;

    private float vibrateThreshold = 0;

    private TextView currentX, currentY, currentZ, maxX, maxY, maxZ;

    private LineGraphSeries<DataPoint> xSeries;
    private LineGraphSeries<DataPoint> ySeries;
    private LineGraphSeries<DataPoint> zSeries;
    private final Handler mHandler = new Handler();
    private Runnable mTimer;
    private int maxDataCount = 40;
    private int windowSize = 5;
    private int dataStartsAt = 0;
    private List<AccPoint> lastNData = new ArrayList<AccPoint>();

    public Vibrator v;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        } else {
            Log.e(TAG, "No TYPE_ACCELEROMETER");
            // fail! we dont have an accelerometer!

        }

        //initialize vibration
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

    }

    public void initializeViews() {
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);

        maxX = (TextView) findViewById(R.id.maxX);
        maxY = (TextView) findViewById(R.id.maxY);
        maxZ = (TextView) findViewById(R.id.maxZ);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        xSeries = new LineGraphSeries<>();
        graph.addSeries(xSeries);
        ySeries = new LineGraphSeries<>();
        graph.addSeries(ySeries);
        zSeries = new LineGraphSeries<>();
        graph.addSeries(zSeries);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0d);
        graph.getViewport().setMaxX(40d);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-15d);
        graph.getViewport().setMaxY(15d);
    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mTimer = new Runnable() {
            @Override
            public void run() {

                DataPoint[][] values = generateData();
                xSeries.resetData(values[0]);
                ySeries.resetData(values[1]);
                zSeries.resetData(values[2]);
                mHandler.postDelayed(this, 300);
            }
        };
        mHandler.postDelayed(mTimer, 300);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        mHandler.removeCallbacks(mTimer);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.e(TAG, "onAccuracyChanged()");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // clean current values
        displayCleanValues();
        // display the current x,y,z accelerometer values
        displayCurrentValues();
        // display the max x,y,z accelerometer values
        displayMaxValues();

        // get the change of the x,y,z values of the accelerometer
        deltaX = Math.abs(lastX - event.values[0]);
        deltaY = Math.abs(lastY - event.values[1]);
        deltaZ = Math.abs(lastZ - event.values[2]);

        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];

        // if the change is below 2, it is just plain noise
        if (deltaX < 2)
            deltaX = 0;
        if (deltaY < 2)
            deltaY = 0;
        if (deltaZ < 2)
            deltaZ = 0;
        if ((deltaZ > vibrateThreshold) ||(deltaY > vibrateThreshold) || (deltaZ > vibrateThreshold)){
            Log.e(TAG, "bump x: " + String.format(Locale.US, "%f", deltaX));
            Log.e(TAG, "bump y: " + String.format(Locale.US, "%f", deltaY));
            Log.e(TAG, "bump z: " + String.format(Locale.US, "%f", deltaZ));
            //v.vibrate(50);
        }
        int maxSize = maxDataCount + windowSize;
        Double x = new Double(lastX);
        Double y = new Double(lastY);
        Double z = new Double(lastZ);
        if (lastNData.size() >= maxSize) {
            lastNData.remove(0);
        }
        lastNData.add(new AccPoint(x,y,z));
    }

    public void displayCleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {
        if (deltaX > displayX) {
            displayX = deltaX;
        } else {
            displayX = 0.8f * displayX + 0.2f * deltaX;
        }
        if (deltaY > displayY) {
            displayY = deltaY;
        } else {
            displayY = 0.8f * displayY + 0.2f * deltaY;
        }
        if (deltaZ > displayZ) {
            displayZ = deltaZ;
        } else {
            displayZ = 0.8f * displayZ + 0.2f * deltaZ;
        }
        currentX.setText(String.format(Locale.US, "%f", displayX));
        currentY.setText(String.format(Locale.US, "%f", displayY));
        currentZ.setText(String.format(Locale.US, "%f", displayZ));
    }

    // display the max x,y,z accelerometer values
    public void displayMaxValues() {
        if (deltaX > deltaXMax) {
            deltaXMax = deltaX;
            maxX.setText(String.format(Locale.US, "%f", deltaXMax));
        }
        if (deltaY > deltaYMax) {
            deltaYMax = deltaY;
            maxY.setText(String.format(Locale.US, "%f", deltaYMax));
        }
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ;
            maxZ.setText(String.format(Locale.US, "%f", deltaZMax));
        }
    }

    private DataPoint[][] generateData() {
        int dataCount = lastNData.size() - windowSize;
        if (dataCount < 0) {
            dataCount = 0;
        }
        DataPoint[][] ret = new DataPoint[3][dataCount];
        for (int i=0; i<dataCount; i++) {
            double x = 0d;
            double y = 0d;
            double z = 0d;
            for (int n = 0; n<windowSize; n++) {
                x += lastNData.get(i+n).x;
                y += lastNData.get(i+n).x;
                z += lastNData.get(i+n).x;
            }
            x = x / ((double) windowSize);
            y = y / ((double) windowSize);
            z = z / ((double) windowSize);

            double gx = i;
            double gy1 = x;
            double gy2 = y;
            double gy3 = z;

            ret[0][i] = new DataPoint(gx,gy1);
            ret[1][i] = new DataPoint(gx,gy2);
            ret[2][i] = new DataPoint(gx,gy3);
        }
        return ret;
    }

    private class AccPoint {
        final double x;
        final double y;
        final double z;

        AccPoint(double x, double y, double z){
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

}
