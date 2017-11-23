package com.example.yuliiastelmakhovska.poicameraviewer;

/**
 * Created by yuliiastelmakhovska on 2017-02-26.
 */

public class POIElevationHelper {
    double latitude;
    double longitude;
    double altitude;

    public POIElevationHelper(double latitude, double longitude, double altitude){
        this.latitude=latitude;
        this.longitude=longitude;
        this.altitude=altitude;
    }

    @Override
    public String toString() {
        return "latitude"+latitude+"longitude"+longitude+"altitude"+altitude;
    }
}
