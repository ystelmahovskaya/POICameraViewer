package com.example.yuliiastelmakhovska.poicameraviewer;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yuliiastelmakhovska on 2017-02-25.
 */

public class POI implements Parcelable {
    Location location;
    String name;
String icon;
String image;
    String reference;


    public POI(String name, double lat, double lon) {

        this.name = name;
        location = new Location("ARPoint");
        location.setLatitude(lat);
        location.setLongitude(lon);

    }
    public POI(){}


    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(double lat, double lng, String name) {
        Location location= new Location(name);
        location.setLatitude(lat);
        location.setLongitude(lng);
        this.location = location;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }


    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getIcon() {
        return icon;
    }

    public String getImage() {
        return image;
    }

    protected POI(Parcel in) {

        name = in.readString();
        location = new Location(in.readString());
        location.setLatitude(in.readDouble());
        location.setLongitude(in.readDouble());
        location.setAltitude(in.readDouble());
        icon=in.readString();
        image=in.readString();
        reference=in.readString();


    }

    public static final Creator<POI> CREATOR = new Creator<POI>() {
        @Override
        public POI createFromParcel(Parcel in) {
            return new POI(in);
        }

        @Override
        public POI[] newArray(int size) {
            return new POI[size];
        }
    };

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(location.getProvider());
        dest.writeDouble(location.getLatitude());
        dest.writeDouble(location.getLongitude());
        dest.writeDouble(location.getAltitude());
        dest.writeString(icon);
        dest.writeString(image);
        dest.writeString(reference);

    }
    public void setAltitude(double altitude){
        this.location.setAltitude(altitude);

    }

}
