package com.myesis.classifierandsensorservice;


import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marksargent on 10/18/15.
 */
public abstract class Predictor {
    private long id;
    private String category;
    private String name;
    private String model; //JSON string
    private String parameterString; //JSON string
    private String method;
    List<String> sensorList;
    List<Double> features;


    public Predictor(){};

    public abstract double predictProb(DataSet data);

    public abstract boolean predict(DataSet data);

    public String readParamFile(Context context, String filename){
        //get text file from assets or raw folder, populate fields in predictor
        StringBuffer buff = new StringBuffer();
        try {
            InputStreamReader stream = new InputStreamReader(context.getAssets().open(filename));
            String contents = "";
            BufferedReader bf = new BufferedReader(stream);

            while((contents = bf.readLine()) != null){
                buff.append(contents);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = buff.toString();
        Log.i("My Code", "From params file: " + result);
        return result;

    }

    public List<Double> getFeaturesPerSensorList(List<String> sensors, DataSet data) {
        List<Double> features = new ArrayList<>();
        for(String s: sensors){
            features.add(getFeatureForSensor(s, data));
        }
        return features;
    }

    public double getFeatureForSensor(String sensor, DataSet data){

        switch(sensor){
            case Constants.ACCELX:
                return data.getAccelX();
            case Constants.ACCELY:
                return data.getAccelY();
            case Constants.ACCELZ:
                return data.getAccelZ();
            case Constants.D_ACCELX:
                return data.getD_accelX();
            case Constants.D_ACCELY:
                return data.getD_accelY();
            case Constants.D_ACCELZ:
                return data.getD_accelZ();

            case Constants.GRAVITYX:
                return data.getGravX();
            case Constants.GRAVITYY:
                return data.getGravY();
            case Constants.GRAVITYZ:
                return data.getGravZ();

            case Constants.GYROX:
                return data.getGyroX();
            case Constants.GYROY:
                return data.getGyroY();
            case Constants.GYROZ:
                return data.getGyroZ();
            case Constants.D_GYROX:
                return data.getD_gyroX();
            case Constants.D_GYROY:
                return data.getD_gyroY();
            case Constants.D_GYROZ:
                return data.getD_gyroZ();

            case Constants.MAGNETICX:
                return data.getMagX();
            case Constants.MAGNETICY:
                return data.getMagY();
            case Constants.MAGNETICZ:
                return data.getMagZ();
            case Constants.D_MAGNETICX:
                return data.getD_magX();
            case Constants.D_MAGNETICY:
                return data.getD_magY();
            case Constants.D_MAGNETICZ:
                return data.getD_magZ();

            case Constants.ORIENTX:
                return data.getOrientX();
            case Constants.ORIENTY:
                return data.getOrientY();
            case Constants.ORIENTZ:
                return data.getOrientZ();

            case Constants.LINEARX:
                return data.getLinX();
            case Constants.LINEARY:
                return data.getLinY();
            case Constants.LINEARZ:
                return data.getLinZ();

            default:
                return 0;

        }

    }

    //Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }


    public String getParameterString() {
        return parameterString;
    }

    public void setParameterString(String parameterString) {
        this.parameterString = parameterString;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
