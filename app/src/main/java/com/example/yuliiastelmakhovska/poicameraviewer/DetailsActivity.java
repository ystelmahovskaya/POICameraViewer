package com.example.yuliiastelmakhovska.poicameraviewer;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.yuliiastelmakhovska.poicameraviewer.databinding.ActivityDetailsBinding;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.concurrent.ExecutionException;


public class DetailsActivity extends AppCompatActivity  {

    public POI poi = new POI();
    public DetailsInfo detailsInfo = new DetailsInfo();
    ReviewViewModel reviewViewModel = new ReviewViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        poi = getIntent().getParcelableExtra("POI");

        ActivityDetailsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_details);
        binding.setPoi(poi);

        if (poi.getImage() != null) {//todo fix image null favorites
            new DownloadImageTask((ImageView) findViewById(R.id.imageView2))
                    .execute("https://maps.googleapis.com/maps/api/place/photo?maxwidth=1480&photoreference=" +
                            poi.getImage() +
                            "&key=AIzaSyA9jU7SOTlki4cRdxarrxK8OWfXCjWdPGA");
        }

        GetInfoTask getInfotask= new GetInfoTask();

        try {
            detailsInfo=getInfotask.execute(poi).get();
            binding.setInfo(detailsInfo);
            if(detailsInfo.getReviews()!=null) {
                reviewViewModel.setReviews(detailsInfo.getReviews());
                binding.setModel(reviewViewModel);
                Log.i("GetInfoTask", "" + detailsInfo);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        final FloatingActionButton addToDb = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        if (FullscreenMainActivity.controller.repo.isFavoriteItem(poi)) {
            addToDb.setImageResource(android.R.drawable.star_on);
        }
        addToDb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (FullscreenMainActivity.controller.repo.isFavoriteItem(poi)) {
                    addToDb.setImageResource(android.R.drawable.star_off);
                    FullscreenMainActivity.controller.repo.deleteItemFromDB(poi);
                    FullscreenMainActivity.lastLocation = null;//TODO redraw
                } else {
                    addToDb.setImageResource(android.R.drawable.star_on);//todo check elevation
                    GetElevationTask getElevationTask = new GetElevationTask();
                    try {
                        Double elevationTMP = getElevationTask.execute(poi).get();
                        poi.setAltitude(elevationTMP.doubleValue());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    FullscreenMainActivity.controller.addToFavorites(poi);
                    FullscreenMainActivity.lastLocation = null;
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
        switch (item.getItemId()) {
            case android.R.id.home:
                FullscreenMainActivity.lastLocation = null;
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bm = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bm = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bm;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private class GetInfoTask extends AsyncTask<POI, Void, DetailsInfo> {
        DetailsInfo detailsInfo = new DetailsInfo();


        @Override
        protected DetailsInfo doInBackground(POI... params) {
            try {
                detailsInfo = getDetailsInfo(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DownloadException e) {
                e.printStackTrace();
            }
            return detailsInfo;
        }

        private DetailsInfo getDetailsInfo(POI poi) throws IOException, DownloadException {
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            String reference = poi.getReference();
            Log.i("reference", reference);
            DetailsInfo detailsInfo = new DetailsInfo();

            String elevationRequestUrl = "https://maps.googleapis.com/maps/api/place/details/json?reference=" + reference + "&key=AIzaSyA9jU7SOTlki4cRdxarrxK8OWfXCjWdPGA";
            URL url = null;
            try {
                url = new URL(elevationRequestUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            urlConnection = (HttpURLConnection) url.openConnection();

        /* optional request header */
            urlConnection.setRequestProperty("Content-Type", "application/json");

        /* optional request header */
            urlConnection.setRequestProperty("Accept", "application/json");

        /* for Get request */

            urlConnection.setRequestMethod("GET");
            int statusCode = urlConnection.getResponseCode();

        /* 200 represents HTTP OK */
            if (statusCode == 200) {
                Log.i("statusCode", "200");
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertInputStreamToString(inputStream);
                detailsInfo = parseDetailsInfo(response);

            } else {
                throw new DownloadException("Failed to fetch data!!");
            }
            return detailsInfo;
        }

        private DetailsInfo parseDetailsInfo(String response) {

            DetailsInfo detailsInfo = new DetailsInfo();

            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("result")) {
                    JSONObject resultObject = new JSONObject(response).getJSONObject("result");

                        String formatted_address;
                        String international_phone_number;
                        String website;
                    String rating;
                        ArrayList<Review> reviews = new ArrayList<>();

                        if (resultObject.has("formatted_address")) {
                            formatted_address = resultObject.optString("formatted_address");

                            detailsInfo.setFormatted_address(formatted_address);
                        }
                        if (resultObject.has("international_phone_number")) {
                            international_phone_number = resultObject.optString("international_phone_number");
                            detailsInfo.setInternational_phone_number(international_phone_number);
                        }
                        if (resultObject.has("website")) {
                            website = resultObject.optString("website");
                            detailsInfo.setWebbsite(website);
                        }
                        if (resultObject.has("rating")) {
                            rating = resultObject.optString("rating");
                            detailsInfo.setRating(rating);
                        }

                        if (resultObject.has("reviews")) {
                            JSONArray reviewsJSON = resultObject.getJSONArray("reviews");
                            for (int j = 0; j < reviewsJSON.length(); j++) {
                                Review review = new Review();
                                if (resultObject.getJSONArray("reviews").getJSONObject(j).has("author_name")) {
                                    review.setAuthor_name(resultObject.getJSONArray("reviews").getJSONObject(j).getString("author_name"));
                                }
                                if (resultObject.getJSONArray("reviews").getJSONObject(j).has("rating")) {
                                    review.setRating(resultObject.getJSONArray("reviews").getJSONObject(j).getString("rating"));
                                }
                                if (resultObject.getJSONArray("reviews").getJSONObject(j).has("text")) {
                                    review.setText(resultObject.getJSONArray("reviews").getJSONObject(j).getString("text"));
                                }
                                if (resultObject.getJSONArray("reviews").getJSONObject(j).has("time")) {
                                    review.setTime(resultObject.getJSONArray("reviews").getJSONObject(j).getLong("time"));
                                }

                                reviews.add(review);
                            }
                            detailsInfo.setReviews(reviews);
                        }
                    }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return detailsInfo;
        }

    }


    private class GetElevationTask extends AsyncTask<POI, Void, Double> {
        Double aDouble = new Double(0);

        @Override
        protected Double doInBackground(POI... params) {
            try {
                double elevation = getElevation(params[0]);
                aDouble = Double.valueOf(elevation);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DownloadException e) {
                e.printStackTrace();
            }
            return aDouble;
        }


        private double getElevation(POI poi) throws IOException, DownloadException {//TODO move to details activity
            String latlngparameters = "";
            double elevation = 0;
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            latlngparameters = poi.getLocation().getLatitude() + "," + poi.getLocation().getLongitude();
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
                elevation = parseElevation(response);

            } else {
                throw new DownloadException("Failed to fetch data!!");
            }
            return elevation;
        }

        private double parseElevation(String response) {
            double elevation = 0;
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("results")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (jsonArray.getJSONObject(i).has("elevation")) {
                            elevation = jsonArray.getJSONObject(i).getDouble("elevation");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return elevation;
        }


    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        if (null != inputStream) {
            inputStream.close();
        }
        return result;
    }

    class DownloadException extends Exception {

        public DownloadException(String message) {
            super(message);
            Log.d("DownloadException", message);
        }
    }
}
