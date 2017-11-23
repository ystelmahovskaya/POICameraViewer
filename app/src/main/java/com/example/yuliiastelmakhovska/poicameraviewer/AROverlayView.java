package com.example.yuliiastelmakhovska.poicameraviewer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.yuliiastelmakhovska.poicameraviewer.helper.LocationHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class AROverlayView extends View {

    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    private ArrayList<POI> arPoints;
    ArrayList<POIButton> poiButtons = new ArrayList<>();
    ArrayList<POI> favorites = new ArrayList<>();


    public AROverlayView(Context context) {
        super(context);

        this.context = context;

    }

    public void setFavorites(ArrayList<POI> favorites) {
        this.favorites = favorites;
    }

    public void setArPoints(ArrayList<POI> arPoints) throws ExecutionException, InterruptedException {
        this.arPoints = arPoints;
        FrameLayout fl = (FrameLayout) getParent();


        //remove buttons from view
        for (int i = 0; i < poiButtons.size(); i++) {
            fl.removeView(poiButtons.get(i));
        }
        poiButtons.clear();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for (POI p : arPoints) {

            POIButton pb = new POIButton(context);
            pb.setLocation(p.getLocation());
            pb.setLayoutParams(params);
            pb.setGravity(Gravity.CENTER_VERTICAL);
            pb.setTextSize(8);
            pb.setIcon(p.getIcon());
            pb.setName(p.getName());
            pb.setReference(p.getReference());
            pb.setText(p.getName() + "\n" + distanceConverter((int) Math.round(currentLocation.distanceTo(pb.getLocation()))));
            if (!FullscreenMainActivity.controller.repo.isFavoriteItem(p)) {
                GetIcon task = new GetIcon();
                pb.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(getResources(), task.execute(pb.getIcon()).get()), null, null, null);
                poiButtons.add(pb);
                fl.addView(pb);
            } else {
                pb.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_star_10, 0, 0, 0);
                poiButtons.add(pb);
                fl.addView(pb);
            }
        }
        if (favorites != null) {
            for (POI p : favorites) {
                POIButton pb = new POIButton(context);
                pb.setLocation(p.getLocation());
                pb.setLayoutParams(params);
                pb.setGravity(Gravity.CENTER_VERTICAL);
                pb.setTextSize(8);
                pb.setIcon(p.getIcon());
                pb.setName(p.getName());
                pb.setReference(p.getReference());
                pb.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_star_10, 0, 0, 0);
                pb.setText(p.getName() + "\n" + distanceConverter((int) Math.round(currentLocation.distanceTo(pb.getLocation()))));
                poiButtons.add(pb);
                fl.addView(pb);
            }
        }
    }

    public String distanceConverter(int distance) {
        if (distance < 1000) {
            return distance + "m";
        } else {
            return distance / 1000 + "km";
        }
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (currentLocation == null) {
            return;
        }

        if (poiButtons != null) {
            for (int i = 0; i < poiButtons.size(); i++) {
                float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
                float[] pointInECEF = LocationHelper.WSG84toECEF(poiButtons.get(i).getLocation());
                float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

                float[] cameraCoordinateVector = new float[4];
                Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

                POIButton poiButton = poiButtons.get(i);
                if (cameraCoordinateVector[2] < 0) {
                    float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
                    float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();

                    if (x >= -poiButton.getWidth() && x < canvas.getWidth() && y >= -poiButton.getHeight() && y <= canvas.getHeight()) {
                        poiButton.setX(x - poiButton.getWidth() / 2);
                        poiButton.setY(y - poiButton.getHeight() / 2);
                        poiButton.setVisibility(View.VISIBLE);

                        final int index = i;
                        poiButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                Log.i("onclick", "works");
                                showDetails(poiButtons.get(index));
                            }
                        });
                    } else {
                        poiButton.setVisibility(View.INVISIBLE);
                    }
                } else {
                    poiButton.setVisibility(View.INVISIBLE);
                }

            }
        }
    }

    public void showDetails(POIButton poiButton) {
        POI poi = new POI();
        poi.setLocation(poiButton.getLocation().getLatitude(), poiButton.getLocation().getLongitude(), poiButton.getLocation().getProvider());
        poi.setIcon(poiButton.getIcon());
        poi.setName(poiButton.getName());
        poi.setReference(poiButton.getReference());
        poi.setAltitude(poiButton.getLocation().getAltitude());
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra("POI", poi);
        context.startActivity(intent);
    }

    class GetIcon extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            URL url = null;
            try {

                Log.d("RemoteImageHandler", "URL " + params[0]);
                //url = new URL(URLEncoder.encode(params[0], "UTF-8"));
                url = new URL(params[0]);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setDoInput(true);
                c.connect();
                InputStream is = c.getInputStream();
                Bitmap img;
                img = BitmapFactory.decodeStream(is);
                return img;
            } catch (MalformedURLException e) {
                Log.d("RemoteImageHandler", "fetchImage passed invalid URL ");
            } catch (IOException e) {
                Log.d("RemoteImageHandler", "fetchImage IO exception: " + e);
            }
            return null;
        }
    }
}
