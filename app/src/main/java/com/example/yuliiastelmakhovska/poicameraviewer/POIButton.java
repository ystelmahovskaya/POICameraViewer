package com.example.yuliiastelmakhovska.poicameraviewer;

import android.content.Context;
import android.location.Location;
import android.widget.Button;

/**
 * Created by yuliiastelmakhovska on 2017-02-28.
 */

public class POIButton extends Button {
    Location location;
    String name;
    String icon;
    String image;
    String reference;

    public POIButton(Context context) {
        super(context);
    }


    public void setName(String name) {
        this.name = name;
        setText(name);
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
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
}
