package com.myesis.classifierandsensorservice;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marksargent on 10/18/15.
 */
public class LogisticPredictor extends Predictor{

    List<Double> paramList;
    private double yIntercept;

    public LogisticPredictor(Context context, String filename){
        String paramString = readParamFile(context, filename);
        try {
            JSONObject object = new JSONObject(paramString);
            JSONArray params = object.getJSONArray("params");
            JSONArray sensors = object.getJSONArray("sensors");

            String yInt = object.getString("intercept");
            yIntercept = Double.parseDouble(yInt);

            sensorList = new ArrayList<>();
            for(int i = 0; i<sensors.length(); i++) {
                sensorList.add(sensors.getString(i));
            }

            paramList = new ArrayList<>();
            for(int i = 0; i<params.length(); i++) {
                paramList.add(params.getDouble(i));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double predictProb(DataSet data) {
        features = getFeaturesPerSensorList(sensorList, data);

        if(paramList.size() != features.size() || features == null) throw new IllegalArgumentException("Feature and parameter arrays must be the same length.");
        double pTX = 0;

        for(int i = 0; i < features.size(); i++){
            pTX += paramList.get(i) * features.get(i);
        }

        return sigmoid(pTX + yIntercept);
    }


    @Override
    public boolean predict(DataSet data) {
        return predictProb(data) > 0.5;
    }

    public double sigmoid(double pTransposeF) throws IllegalArgumentException{

        return 1 / (1 + Math.exp(- pTransposeF));

    }

    public double getyIntercept() {
        return yIntercept;
    }

    public void setyIntercept(double yIntercept) {
        this.yIntercept = yIntercept;
    }


}
