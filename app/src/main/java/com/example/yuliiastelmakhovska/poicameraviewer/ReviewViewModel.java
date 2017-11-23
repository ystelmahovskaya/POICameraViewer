package com.example.yuliiastelmakhovska.poicameraviewer;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by yuliiastelmakhovska on 2017-04-20.
 */

public class ReviewViewModel extends BaseObservable {

    @Bindable
    ObservableArrayList<Review> reviews = new ObservableArrayList<>();
    public ReviewViewModel (){

    }

    public void setReviews(ArrayList<Review> rev) {
        for (Review r :rev) {
           reviews.add(r);
        }
    }

    public ObservableArrayList<Review> getReviews() {
        return reviews;
    }
    @BindingAdapter("app:items")
    public static void bindList(RecyclerView view, ObservableArrayList<Review> list) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        view.setLayoutManager(layoutManager);
        view.setAdapter(new ReviewAdapter(list));
    }
}
