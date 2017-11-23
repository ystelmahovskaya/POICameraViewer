package com.example.yuliiastelmakhovska.poicameraviewer;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by yuliiastelmakhovska on 2017-03-02.
 */

public class PlacesSuggestionProvider extends ContentProvider {
    private static final String LOG_TAG = "";

    public static final String AUTHORITY = "com.example.yuliiastelmakhovska.poicameraviewer.PlacesSuggestionProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/search");


    // UriMatcher constant for search suggestions
    private static final int SEARCH_SUGGEST = 1;

    private static final UriMatcher uriMatcher;

    private static final String[] SEARCH_SUGGEST_COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID

    };

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
    }

    @Override
    public int delete(Uri uri, String arg1, String[] arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Log.d(LOG_TAG, "query = " + uri);

        // Use the UriMatcher to see what kind of query we have
        switch (uriMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                Log.d(LOG_TAG, "Search suggestions requested.");
                MatrixCursor cursor = new MatrixCursor(SEARCH_SUGGEST_COLUMNS, 1);

                GetPlaces task = new GetPlaces();
                String searchItem = uri.getLastPathSegment().toLowerCase();
                //now pass the argument in the textview to the task
                ArrayList<Pair<String, String>> results = new ArrayList<>();
                try {
                    results = task.execute(searchItem).get();//
                } catch (Exception e) {
                    Log.w("Google stuff", "shit happens");
                }

                if (results != null) {
                    for (Pair s: results) {
                        cursor.addRow(new String[]{
                                "1", s.first.toString(), s.second.toString(), "content_id"
                        });

                    }
                }
                return cursor;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues arg1, String arg2, String[] arg3) {
        throw new UnsupportedOperationException();
    }
    class GetPlaces extends AsyncTask<String, Void, ArrayList<Pair<String, String>>> {
        @Override
        protected ArrayList<Pair<String, String>> doInBackground(String... args)
        {
            Log.d("PlacesListActivity", "doInBackground");
            ArrayList<Pair<String, String>> predictionsArr = new ArrayList<Pair<String, String>>();
            try
            {
                URL googlePlaces = new URL(
                        "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" +
                                URLEncoder.encode(args[0], "UTF-8") +
                                "&types=establishment&language=en&sensor=true&key=AIzaSyA9jU7SOTlki4cRdxarrxK8OWfXCjWdPGA");

                Log.d("PlacesListActivity", googlePlaces.toString());

                URLConnection tc = googlePlaces.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        tc.getInputStream()));

                String line;
                StringBuffer sb = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                JSONObject predictions = new JSONObject(sb.toString());
                JSONArray ja = new JSONArray(predictions.getString("predictions"));

                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = (JSONObject) ja.get(i);
                    predictionsArr.add(new Pair<String, String>(jo.getString("description"),jo.getString("reference")));

                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e)
            {
                Log.e("PlacesListActivity", "GetPlaces : doInBackground", e);
            } catch (JSONException e)
            {
                Log.e("PlacesListActivity", "GetPlaces : doInBackground", e);
            }

            return predictionsArr;
        }


    }
}
