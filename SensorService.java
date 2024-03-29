package com.myesis.classifierandsensorservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

//import com.myesis.classifierandsensorservice.DataSet;

public class SensorService extends Service implements SensorEventListener {
    public static boolean isStarted;

    private int freq = 500000;
    private boolean hostingActivityRunning;
    private List<DataSet> dataArray;

    private long lastBroadcastTime;

    private SensorManager sensorManager;

    private Sensor
            sensorACC,
            sensorMagnetic,
            sensorGyro;

    private PowerManager.WakeLock mWakeLock;
    private PowerManager pm;
    private int wakeLockType = PowerManager.PARTIAL_WAKE_LOCK;
    private PowerManager.WakeLock fWakelock;

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    private SimpleDateFormat s;


    private final IBinder mBinder = new LocalBinder();

    DataSet data;

    public class LocalBinder extends Binder {
        public SensorService getService() {
            return SensorService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        data = new DataSet();
        dataArray = new ArrayList<>();

        s = new SimpleDateFormat(DATE_FORMAT);

        Toast.makeText(this, "Service created ...", Toast.LENGTH_LONG).show();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        setSensors();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        enableSensor();
        Log.i("My Code", "Service started");
        isStarted = true;

        if (intent != null && intent.hasExtra("sampleRate"))
            freq = intent.getIntExtra("sampleRate", 500) * 1000;


        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        mWakeLock = pm
                .newWakeLock(
                        (PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP),
                        "bbb");

        mWakeLock.acquire();
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        isStarted = false;

        disableSensor();

        if (mWakeLock != null && mWakeLock.isHeld())
            mWakeLock.release();

        if (fWakelock != null && fWakelock.isHeld())
            fWakelock.release();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long time = System.currentTimeMillis();
        //only broadcast if freq amount time has passed; android doesn't always respect this
        //in getting sensor updates

        Sensor source = event.sensor;

        if (source.equals(sensorACC)) {
            data.setAccelX(event.values[0]);
            data.setAccelY(event.values[1]);
            data.setAccelZ(event.values[2]);

        } else if (source.equals(sensorMagnetic)) {
            data.setMagX(event.values[0]);
            data.setMagY(event.values[1]);
            data.setMagZ(event.values[2]);

        } else if (source.equals(sensorGyro)) {
            data.setGyroX(event.values[0]);
            data.setGyroY(event.values[1]);
            data.setGyroZ(event.values[2]);
        }

        long dt = time - lastBroadcastTime;
        if (dt >= freq / 1000.0 && hostingActivityRunning) {

            if(dataArray.size() > 0){
                DataSet last = getDeltaDataSet(dataArray);

                dt /= 1000.0;//to keep scale reasonable

                data.setD_accelX((data.getAccelX() - last.getAccelX())/dt);
                data.setD_accelY((data.getAccelY() - last.getAccelY())/dt);
                data.setD_accelZ((data.getAccelZ() - last.getAccelZ())/dt);

                data.setD_gyroX((data.getGyroX() - last.getGyroX())/dt);
                data.setD_gyroY((data.getGyroY() - last.getGyroY())/dt);
                data.setD_gyroZ((data.getGyroZ() - last.getGyroZ())/dt);

                data.setD_magX((data.getMagX() - last.getMagX())/dt);
                data.setD_magY((data.getMagY() - last.getMagY())/dt);
                data.setD_magZ((data.getMagZ() - last.getMagZ())/dt);

            }

            try {
                dataArray.add((DataSet)data.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            lastBroadcastTime = time;

            if(hostingActivityRunning){
                Intent localIntent = new Intent(Constants.BROADCAST_SENSOR_DATA).putExtra(Constants.DATA, data);
                LocalBroadcastManager.getInstance(SensorService.this).sendBroadcast(localIntent);
            }
        }
    }

    public DataSet getDeltaDataSet(List<DataSet> dataArray){
        return dataArray.get(dataArray.size() - 1);
    }

    public void enableSensor() {
        sensorManager.registerListener(this, sensorACC, freq);
        sensorManager.registerListener(this, sensorMagnetic, freq);
        sensorManager.registerListener(this, sensorGyro, freq);
    }


    public void disableSensor() {
        sensorManager.unregisterListener(this);
    }

    public void setSensors() {

        if (sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
            sensorACC = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        } else {
            Toast.makeText(this, "You don't have an accelerometer", Toast.LENGTH_SHORT).show();
        }



        if (sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).size() != 0) {
            sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        } else {
            Toast.makeText(this, "You don't have an magnetic field sensor", Toast.LENGTH_SHORT).show();
        }

        if (sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE).size() != 0) {
            sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        } else {
            Toast.makeText(this, "You don't have a gyroscope", Toast.LENGTH_SHORT).show();
        }

    }

    public int getFreq() {
        return freq;
    }


    public List<DataSet> getDataArray() {
        return dataArray;
    }

    public void setDataArray(List<DataSet> dataArray) {
        this.dataArray = dataArray;
    }

    public void setFreq(int freq) {
        this.freq = freq*1000;
    }

    public static boolean isStarted() {
        return isStarted;
    }

    public static void setIsStarted(boolean isStarted) {
        com.myesis.classifierandsensorservice.SensorService.isStarted = isStarted;
    }

    public boolean isHostingActivityRunning() {
        return hostingActivityRunning;
    }

    public void setHostingActivityRunning(boolean hostingActivityRunning) {
        this.hostingActivityRunning = hostingActivityRunning;
    }


}
