package com.example.yuliiastelmakhovska.poicameraviewer;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.yuliiastelmakhovska.poicameraviewer.databinding.ActivityListManageBinding;

import static com.example.yuliiastelmakhovska.poicameraviewer.Controller.currentSelectedItems;
import static com.example.yuliiastelmakhovska.poicameraviewer.R.id.action_delete;

public class ListManageActivity extends AppCompatActivity {
    CheckBox all;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityListManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_list_manage);
        binding.setController(FullscreenMainActivity.controller);
        all = (CheckBox) findViewById(R.id.all);
        if (FullscreenMainActivity.controller.poiListFromDB.size() == 0 || FullscreenMainActivity.controller.poiListFromDB == null) {
            all.setVisibility(View.GONE);
        }
        all.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                POIAdapter poiAdapter = (POIAdapter) binding.rvItemsAll.getAdapter();
                if (isChecked) {
                    poiAdapter.setSelectedAll(true);
                    currentSelectedItems.clear();
                    currentSelectedItems.addAll(FullscreenMainActivity.controller.poiListFromDB);
                } else {
                    poiAdapter.setSelectedAll(false);
                    currentSelectedItems.clear();
                }
            }
        });
setupActionBar();
    }
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == action_delete) {
            for (int i = 0; i < currentSelectedItems.size(); i++) {
                FullscreenMainActivity.controller.repo.deleteItemFromDB(currentSelectedItems.get(i));
            }
            if (FullscreenMainActivity.controller.poiListFromDB.size() == 0) {
                all.setVisibility(View.GONE);
            }
        }
        if (id == android.R.id.home) {
            FullscreenMainActivity.lastLocation = null;
            this.finish();
        }
        return true;
    }


}