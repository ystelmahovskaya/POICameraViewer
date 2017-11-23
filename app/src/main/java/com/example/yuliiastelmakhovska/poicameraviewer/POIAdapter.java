package com.example.yuliiastelmakhovska.poicameraviewer;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.example.yuliiastelmakhovska.poicameraviewer.databinding.ListItemBinding;

    public class POIAdapter extends RecyclerView.Adapter<POIAdapter.ViewHolder> {
        private ObservableArrayList<POI> list;
        @NonNull
        private OnItemCheckListener onItemCheckListener;
        public boolean isSelectedAll;

        interface OnItemCheckListener {
            void onItemCheck(POI item);
            void onItemUncheck(POI item);
        }

        public void setSelectedAll(boolean selectedAll) {
            isSelectedAll = selectedAll;
            notifyDataSetChanged();
        }

        public POIAdapter(ObservableArrayList<POI> l, @NonNull OnItemCheckListener onItemCheckListener) {
            list = l;
            this.onItemCheckListener=onItemCheckListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final POI poi = list.get(position);


            if (holder instanceof ViewHolder) {

                holder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.checkbox.setChecked(
                                !holder.checkbox.isChecked());
                        if (holder.checkbox.isChecked()) {
                            onItemCheckListener.onItemCheck(poi);
                        } else {
                            onItemCheckListener.onItemUncheck(poi);
                        }
                    }
                });

                if(isSelectedAll){
                    holder.checkbox.setChecked(true);
                }
                if(!isSelectedAll){
                    holder.checkbox.setChecked(false);
                }

            }
            holder.binder.setPoi(poi);
            holder.binder.executePendingBindings();

        }

        @Override
        public int getItemCount() {
            if (list != null)
                return list.size();
            else
                return 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkbox;
            public ListItemBinding binder;
            View itemView;

            public ViewHolder(View v) {
                super(v);
                itemView=v;
                binder = DataBindingUtil.bind(v);
                checkbox = (CheckBox) v.findViewById(R.id.checkBox);
               checkbox.setClickable(false);
            }
            public void setOnClickListener(View.OnClickListener onClickListener) {
                itemView.setOnClickListener(onClickListener);
            }

        }

    }



