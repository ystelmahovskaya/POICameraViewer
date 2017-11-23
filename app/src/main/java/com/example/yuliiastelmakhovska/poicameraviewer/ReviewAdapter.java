package com.example.yuliiastelmakhovska.poicameraviewer;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yuliiastelmakhovska.poicameraviewer.databinding.ReviewItemBinding;

/**
 * Created by yuliiastelmakhovska on 2017-04-20.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ReviewItemBinding binder;

        public ViewHolder(View v) {
            super(v);
            binder = DataBindingUtil.bind(v);
        }
    }

    private ObservableArrayList<Review> list;

    public ReviewAdapter(ObservableArrayList<Review> l) {
        list = l;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Review r = list.get(position);
        holder.binder.setReview(r);
        holder.binder.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        if (list != null)
            return list.size();
        else
            return 0;
    }
}
