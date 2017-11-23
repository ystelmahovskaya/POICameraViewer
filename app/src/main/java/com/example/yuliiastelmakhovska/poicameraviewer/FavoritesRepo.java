package com.example.yuliiastelmakhovska.poicameraviewer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by yuliiastelmakhovska on 2017-03-01.
 */

class FavoritesRepo extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "FavoritesDb";
    private static final int VERSION = 2;



    public FavoritesRepo(Context context)
    {
        super(context,DATABASE_NAME,null,VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("","onCreate Database!");
        db.execSQL("CREATE TABLE Favorites (name TEXT,"+
                "lat DOUBLE,"+
                "lng DOUBLE,"+
                "alt DOUBLE,"+
        "reference TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if( oldVersion == 1 && newVersion == 2)
        {
            //Change name on notes id column to notes _id
            db.beginTransaction();
            try {
                //Rename existing table
                db.execSQL("ALTER TABLE Favorites RENAME TO tmp_Favorites;");
                //Create new Notes table with _id column name
                db.execSQL("CREATE TABLE Favorites (name TEXT,"+
                        "lat DOUBLE,"+
                        "lng DOUBLE,"+
                        "alt DOUBLE,"+
                        "reference TEXT)");

                db.execSQL("INSERT INTO Favorites(name,lat,lng,alt,reference) " +
                        "SELECT name, lat,lng,alt,reference " +
                        "FROM tmp_Favorites;");
                //Remove old table
                db.execSQL("DROP TABLE tmp_Favorites;");
                db.setTransactionSuccessful();
            }catch (SQLiteException e){}
            finally {
                db.endTransaction();
            }


        }
    }
//

    public void insert(String name, double lat, double lng, double alt, String reference)
    {
        SQLiteDatabase FavoritesDb = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("lat", lat);
        values.put("lng", lng);
        values.put("alt", alt);
        values.put("reference", reference);
        try {

            FavoritesDb.insertOrThrow("Favorites", null, values);
            POI poi= new POI();
            poi.setLocation(lat,lng,name);
            poi.setAltitude(alt);
            poi.setReference(reference);

        }
        catch (android.database.sqlite.SQLiteConstraintException e){Log.e("Favorites.insert", e.toString());}

    }


    public ArrayList<POI> getPOI() throws ParseException {
        SQLiteDatabase FavoritesDb = this.getReadableDatabase();
        Cursor cursor = FavoritesDb.rawQuery("SELECT * FROM Favorites", null);
        ArrayList<POI> list = new ArrayList<>();

        int column_name = cursor.getColumnIndex("name");
        int column_lat = cursor.getColumnIndex("lat");
        int column_lng = cursor.getColumnIndex("lng");
        int column_alt = cursor.getColumnIndex("alt");
        int column_reference = cursor.getColumnIndex("reference");

        while(cursor.moveToNext())
        {
            POI p = new POI();

            p.setName(cursor.getString(column_name));
            p.setLocation(cursor.getDouble(column_lat), cursor.getDouble(column_lng), cursor.getString(column_name));
            p.setAltitude(cursor.getDouble(column_alt));
            p.setReference(cursor.getString(column_reference));
            list.add(p);
        }
        cursor.close();
        FavoritesDb.close();
        return list;
    }

    public boolean isFavoriteItem(POI poi){
        SQLiteDatabase FavoritesDb = this.getReadableDatabase();
        long numRows =0;
        numRows = DatabaseUtils.longForQuery(FavoritesDb, "SELECT COUNT(*) FROM Favorites WHERE lat ="+poi.getLocation().getLatitude()
                +" AND lng="+poi.getLocation().getLongitude(), null);
        if(numRows!=0){
            return true;
        }
        return false;
    }

    public void deleteItemFromDB(POI poi){
        SQLiteDatabase favoritesDb = this.getWritableDatabase();
        favoritesDb.delete("Favorites","lat=? and lng=?",new String[]{String.valueOf(poi.getLocation().getLatitude()),String.valueOf(poi.getLocation().getLongitude())});
        for(int i=0; i<FullscreenMainActivity.controller.poiListFromDB.size(); i++){
            if(FullscreenMainActivity.controller.poiListFromDB.get(i).getLocation().getAltitude()==poi.getLocation().getAltitude()&&
                    FullscreenMainActivity.controller.poiListFromDB.get(i).getLocation().getLongitude()==poi.getLocation().getLongitude()){
                FullscreenMainActivity.controller.poiListFromDB.remove(i);
            }
        }
        favoritesDb.close();
    }
    public void deleteAll(){
        SQLiteDatabase favoritesDb = this.getWritableDatabase();
        favoritesDb.execSQL("delete from Favorites");
        FullscreenMainActivity.controller.poiListFromDB.clear();
        favoritesDb.close();
    }
}




