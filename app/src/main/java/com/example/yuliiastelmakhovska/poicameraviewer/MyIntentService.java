package com.example.yuliiastelmakhovska.poicameraviewer;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MyIntentService extends IntentService {

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String url = intent.getStringExtra("url");


        Bundle bundle = new Bundle();

        if (!TextUtils.isEmpty(url)) {
            ArrayList<POI> pointsList = new ArrayList<>();
            try {
                pointsList = downloadData(url);
            } catch (IOException e) {
                e.printStackTrace();
                receiver.send(STATUS_ERROR, bundle);
            } catch (DownloadException e) {
                e.printStackTrace();
                receiver.send(STATUS_ERROR, bundle);
            }
            if (null != pointsList && pointsList.size() > 0) {
                bundle.putParcelableArrayList("result", pointsList);
                receiver.send(STATUS_FINISHED, bundle);
            } else {
                bundle.putParcelableArrayList("result", pointsList);
                receiver.send(STATUS_FINISHED, bundle);
            }
        }

        this.stopSelf();
    }

    private ArrayList<POI> downloadData(String requestUrl) throws IOException, DownloadException {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        URL url = new URL(requestUrl);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestMethod("GET");
        int statusCode = urlConnection.getResponseCode();
        if (statusCode == 200) {
            Log.i("statusCode", "200");
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            String response = convertInputStreamToString(inputStream);
            ArrayList<POI> points = parseResult(response);

            return points;
        } else {
            throw new DownloadException("Failed to fetch data!!");
        }
    }

    private ArrayList<POI> parseResult(String response) {
        ArrayList<POI> points = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("results")) {

                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < jsonArray.length(); i++) {
                    POI poi = new POI();
                    String namel;
                    String Adress;
                    String reference;
                    String icon;
                    String image;
                    double lat = 0;
                    double lng = 0;
                    if (jsonArray.getJSONObject(i).has("name")) {

                        poi.setName(jsonArray.getJSONObject(i).optString("name"));
                        namel = jsonArray.getJSONObject(i).optString("name");

                        if (jsonArray.getJSONObject(i).has("geometry")) {
                            if (jsonArray.getJSONObject(i).getJSONObject("geometry").has("location")) {
                                if (jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").has("lat") ||
                                        jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").has("lng")) {
                                    lat = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                                    lng = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                                }
                            }
                        }
                        if(jsonArray.getJSONObject(i).has("icon")){
                            icon=jsonArray.getJSONObject(i).optString("icon");
                            poi.setIcon(icon);
                        }
                        if(jsonArray.getJSONObject(i).has("photos")){
                            JSONArray photos = jsonArray.getJSONObject(i).getJSONArray("photos");
                            for (int j=0; j<photos.length();j++){
                                if(jsonArray.getJSONObject(i).getJSONArray("photos").getJSONObject(j).has("photo_reference")){
                                    image=jsonArray.getJSONObject(i).getJSONArray("photos").getJSONObject(j).getString("photo_reference");
                                    poi.setImage(image);
                                }
                            }
                        }

                        if(jsonArray.getJSONObject(i).has("reference")){
                            reference=jsonArray.getJSONObject(i).optString("reference");
                            poi.setReference(reference);
                        }
                        poi.setLocation(lat, lng, namel);

                    }
                    points.add(poi);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList();
        }


        try {
            getElevations(points);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DownloadException e) {
            e.printStackTrace();
        }

        return points;
    }

    public void getElevations(ArrayList<POI> poiList) throws IOException, DownloadException {
        ArrayList<POIElevationHelper> elevationList;
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
            String latlngparameters = "";

        for (int i = 0; i < poiList.size(); i++) {
            if (i != poiList.size() - 1) {
                latlngparameters += poiList.get(i).getLocation().getLatitude() + "," + poiList.get(i).getLocation().getLongitude() + "|";
            } else {
                latlngparameters += poiList.get(i).getLocation().getLatitude() + "," + poiList.get(i).getLocation().getLongitude();
            }
        }
        String elevationRequestUrl = "https://maps.googleapis.com/maps/api/elevation/json?locations=" + latlngparameters + "&key=AIzaSyA9jU7SOTlki4cRdxarrxK8OWfXCjWdPGA";

        URL url = null;
        try {
            url = new URL(elevationRequestUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestMethod("GET");
        int statusCode = urlConnection.getResponseCode();
        if (statusCode == 200) {
            Log.i("statusCode", "200");
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            String response = convertInputStreamToString(inputStream);
            elevationList = parseElevationResult(response);

        } else {
            throw new DownloadException("Failed to fetch data!!");
        }

        for (int i = 0; i < poiList.size(); i++) {
            if (poiList.get(i).getLocation().getLatitude() == elevationList.get(i).latitude &&
                    poiList.get(i).getLocation().getLongitude() == elevationList.get(i).longitude) {
                double d = elevationList.get(i).altitude;
                poiList.get(i).setAltitude(d);

            }
        }
    }

    private ArrayList<POIElevationHelper> parseElevationResult(String response) {
        ArrayList<POIElevationHelper> elevationList = new ArrayList<>();
        try {

            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("results")) {

                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < jsonArray.length(); i++) {
                    double lat = 0;
                    double lng = 0;
                    double alt = 0;

                    if (jsonArray.getJSONObject(i).has("elevation")) {

                        alt = jsonArray.getJSONObject(i).getDouble("elevation");

                        if (jsonArray.getJSONObject(i).has("location")) {
                            if (jsonArray.getJSONObject(i).getJSONObject("location").has("lat") ||
                                    jsonArray.getJSONObject(i).getJSONObject("location").has("lng")) {
                                lat = jsonArray.getJSONObject(i).getJSONObject("location").getDouble("lat");
                                lng = jsonArray.getJSONObject(i).getJSONObject("location").getDouble("lng");
                            }
                        }

                    }
                    elevationList.add(new POIElevationHelper(lat, lng, alt));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList();
        }

        return elevationList;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

            /* Close Stream */
        if (null != inputStream) {
            inputStream.close();
        }

        return result;
    }

    public class DownloadException extends Exception {

        public DownloadException(String message) {
            super(message);
            Log.d("DownloadException", message);
        }

        public DownloadException(String message, Throwable cause) {
            super(message, cause);
            Log.d("Throwable", "");
        }
    }


}
