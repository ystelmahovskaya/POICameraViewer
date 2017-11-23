package com.example.yuliiastelmakhovska.poicameraviewer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutionException;


public class Controller extends BaseObservable implements DownloadResultReceiver.Receiver{


    private DownloadResultReceiver mReceiver;
    private Context context;
    AROverlayView arOverlayView;
    FavoritesRepo repo;


    public static ObservableArrayList<POI> currentSelectedItems= new ObservableArrayList<>();
    @Bindable
    public ObservableArrayList<POI> poiListFromDB= new ObservableArrayList<>();



    public Controller(AROverlayView arOverlayView) {
        mReceiver = new DownloadResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        this.arOverlayView = arOverlayView;

    }

    public void setContext(Context context) {
        this.context = context;
        repo = new FavoritesRepo(context);
        try {
            poiListFromDB.addAll(repo.getPOI());

        } catch (ParseException e) {
            e.printStackTrace();
        }
        notifyChange();
    }

    public void sendRequest(double lat, double lng) {
        Intent intent = new Intent(context, MyIntentService.class);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> selections = sharedPrefs.getStringSet("listPlaces", null);
        int radius=Integer.parseInt(sharedPrefs.getString("listRadius","200"));
        Log.i("radius: ", ""+ radius);
        Log.i("selections: ", ""+ selections);
        if (selections != null && !selections.isEmpty()) {
            String places = "";
            for (String s : selections) {
                places += s + "|";
            }
            Log.i("places: ", places.substring(0, places.length() - 1));


            String key = "AIzaSyA9jU7SOTlki4cRdxarrxK8OWfXCjWdPGA";//AIzaSyCo75txQ8EVBW2rwWLHDnYC3eq6MrzEukw";
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + lng + "&radius="+radius+"&types=" + places.substring(0, places.length() - 1) + "&key=" + key;

            intent.putExtra("url", url);
            intent.putExtra("receiver", mReceiver);
            intent.putExtra("requestId", 101);

            context.startService(intent);

        }
        if(selections.isEmpty()){
            try {
                arOverlayView.setArPoints(new ArrayList<POI>());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            arOverlayView.invalidate();
        }
    }

    @BindingAdapter("app:items")
    public static void bindList(RecyclerView view, ObservableArrayList<POI> list) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        view.setLayoutManager(layoutManager);
       view.setAdapter(new POIAdapter(list,new POIAdapter.OnItemCheckListener() {
           @Override
           public void onItemCheck(POI item) {
               currentSelectedItems.add(item);
               Log.i("onItemCheck",""+item);
           }

           @Override
           public void onItemUncheck(POI item) {
               currentSelectedItems.remove(item);
               Log.i("onItemUncheck",""+item);
           }
       }));
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)  {
        switch (resultCode) {
            case MyIntentService.STATUS_RUNNING:

                break;
            case MyIntentService.STATUS_FINISHED:
                ArrayList<POI> points = resultData.getParcelableArrayList("result");
                try {
                    arOverlayView.setArPoints(points);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case MyIntentService.STATUS_ERROR:
                String error = resultData.getString(Intent.EXTRA_TEXT);
                // Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;

        }

    }

    public void setFavorites() throws ParseException {
        ArrayList<POI> points= repo.getPOI();

        arOverlayView.setFavorites(points);
    }
    public void disableFavorites()  {
        arOverlayView.setFavorites(null);
    }

public void addToFavorites(POI poi){//TODO add reference
repo.insert(poi.getName(),poi.getLocation().getLatitude(),poi.getLocation().getLongitude(),poi.getLocation().getAltitude(),poi.getReference());
    poiListFromDB.add(poi);
}
}
