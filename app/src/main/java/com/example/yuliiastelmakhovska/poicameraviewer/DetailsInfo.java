package com.example.yuliiastelmakhovska.poicameraviewer;

import java.util.ArrayList;

/**
 * Created by yuliiastelmakhovska on 2017-04-19.
 */

public class DetailsInfo {
     String formatted_address;
     String international_phone_number;
     String webbsite;
    String rating;
     ArrayList<Review> reviews;


    public String getFormatted_address() {
        return formatted_address;
    }

    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }

    public String getInternational_phone_number() {
        return international_phone_number;
    }

    public void setInternational_phone_number(String international_phone_number) {
        this.international_phone_number = international_phone_number;
    }

    public String getWebbsite() {
        return webbsite;
    }

    public void setWebbsite(String webbsite) {
        this.webbsite = webbsite;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }

    public String prRe(ArrayList<Review> reviews){
        String s ="";
        if (reviews!=null) {
            for (Review r : reviews
                    ) {
                s = s + "Author_name" + r.getAuthor_name() + "rating" + r.getRating() + "text" + r.getText() + "time" + r.getTime() + "\n";
            }
        }
        return s;
    }

    public String getStringValueofRating(){
        return String.valueOf(rating);
    }
    @Override
    public String toString() {
        return "formatted_address"+formatted_address+"\n"+
                "international_phone_number"+international_phone_number+"\n"+
                "webbsite"+webbsite+"\n"+
                "rating"+rating+"\n"+
                "reviews"+prRe(reviews);
    }
}
