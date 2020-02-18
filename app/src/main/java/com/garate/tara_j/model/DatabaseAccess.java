package com.garate.tara_j.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase db;
    private static DatabaseAccess instance;
    Cursor c = null;

    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    public void open() {
        this.db = openHelper.getWritableDatabase();
    }

    public void close() {
        if (db != null) {
            this.db.close();
        }
    }

    public ArrayList getDataID(String route) {
        ArrayList<Integer> IDData = new ArrayList<>();
        int directionID = 0;

        c = db.rawQuery("SELECT directions.direction_id,lng, lat FROM points INNER JOIN directions ON directions.direction_id = points.direction_id where directions.route_id = (select route_id from routes where route_name = '" + route + "');" , new String[] {});
        if (c.moveToFirst()){
            do{
                directionID = c.getInt(c.getColumnIndex("direction_id"));
                IDData.add(directionID);
            }
            while(c.moveToNext());
        }c.close();
        return IDData;
    }

    public ArrayList getDataLat(String route) {
        ArrayList<Double> latData = new ArrayList<>();
        double lat = 0;

        c = db.rawQuery("SELECT directions.direction_id,lng, lat FROM points INNER JOIN directions ON directions.direction_id = points.direction_id where directions.route_id = (select route_id from routes where route_name = '" + route + "');" , new String[] {});
        if (c.moveToFirst()){
            do{
                lat = c.getDouble(c.getColumnIndex("lat"));
                latData.add(lat);
            }
            while(c.moveToNext());
        }c.close();
        return latData;
    }

    public ArrayList getDataLng(String route) {
        ArrayList<Double> lngData = new ArrayList<>();
        double lng = 0;

        c = db.rawQuery("SELECT directions.direction_id,lng, lat FROM points INNER JOIN directions ON directions.direction_id = points.direction_id where directions.route_id = (select route_id from routes where route_name = '" + route + "');" , new String[] {});
        if (c.moveToFirst()){
            do{
                lng = c.getDouble(c.getColumnIndex("lng"));
                lngData.add(lng);
            }
            while(c.moveToNext());
        }c.close();
        return lngData;
    }
}