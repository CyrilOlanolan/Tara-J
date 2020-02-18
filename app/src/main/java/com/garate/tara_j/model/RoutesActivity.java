package com.garate.tara_j.model;

import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.garate.tara_j.MapsActivity;
import com.garate.tara_j.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Map;

public class RoutesActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public AutoCompleteTextView searchBar;
    public Button searchButton;
    private ImageButton backButton;
    public ArrayList<Integer> id;
    public ArrayList<Double> lat;
    public ArrayList<Double> lng;
    public Polyline route = null;
    public static final String[] ROUTES = new String[] {
            "Bago Aplaya",
            "Bangkal",
            "Bo. Obrero",
            "Calinan",
            "Ecoland Subdivision",
            "Maa-Agdao",
            "Matina Aplaya",
            "Panacan-SM City Davao",
            "Puan",
            "Route 01",
            "Route 02",
            "Route 03",
            "Route 04",
            "Route 05",
            "Route 06",
            "Route 07",
            "Route 08",
            "Route 09",
            "Route 10",
            "Route 10B",
            "Route 11",
            "Route 12",
            "Route 13",
            "Route 14",
            "Route 15",
            "Sasa via Cabaguio",
            "Sasa via JP Laurel",
            "Sasa via R. Castillo",
            "Talomo",
            "Tibungco via Buhangin",
            "Tibungco via Cabaguio",
            "Tibungco via R. Castillo",
            "Toril",
            "Ulas",
            "Matina Pangi",
            "Mintal",
            "Catalunan Grande",
            "Matina Crossing",
            "Matina",
            "Maa-Bankerohan",
            "Magtuod",
            "Wa-an",
            "Buhangin via JP Laurel",
            "Buhangin via Dacudao",
            "Lasang via Buhangin",
            "Lasang via Sasa",
            "El Rio Vista Village",
            "Panacan via Cabaguio",
            "Bunawan via Buhangin",
            "Bunawan via Sasa",
            "Camp Catitipan via JP Laurel",
            "Jade Valley",
            "Emily Homes (Cabantian)"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Widget Declarations
        searchBar = findViewById(R.id.searchBar);
        searchButton = findViewById(R.id.findButton);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ROUTES);
        searchBar = findViewById(R.id.searchBar);
        searchBar.setAdapter(adapter);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        searchBar.setImeActionLabel("Tara!", KeyEvent.KEYCODE_ENTER);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
                databaseAccess.open();

                String route = searchBar.getText().toString();
                id = databaseAccess.getDataID(route);
                lat = databaseAccess.getDataLat(route);
                lng = databaseAccess.getDataLng(route);

                if (id.size() != 0) {
                    for (int z = 0; z < id.size(); z++) {
                        System.out.println(id.get(z) + " " + lat.get(z) + " " + lng.get(z));
                    }
                    drawRoute(id, lat, lng);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "No Routes Found", Toast.LENGTH_SHORT);
                    toast.show();
                }

                databaseAccess.close();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // default when nothing is showing up
        mMap = googleMap;
        LatLng davaoDefault = new LatLng(7.1279, 125.5931);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(davaoDefault, 12f));
    }

    public void drawRoute(ArrayList<Integer> id, ArrayList<Double> lat, ArrayList<Double> lng) {
        if (route != null) {
            mMap.clear();
        }

        PolylineOptions options = new PolylineOptions().width(12).color(Color.parseColor("#2a2b5d")).geodesic(true).clickable(true);
        for (int z = 0; z < id.size(); z++) {
            LatLng point = new LatLng(lat.get(z), lng.get(z));
            options.add(point);
        }

        route = mMap.addPolyline(options);

        LatLng start = new LatLng(lat.get(0), lng.get(0));
        LatLng center = new LatLng(lat.get(id.size()/2), lng.get(id.size()/2));

        LatLngBounds.Builder routeBounds = new LatLngBounds.Builder();
        routeBounds.include(start).include(center);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(routeBounds.build(), 100);
        mMap.animateCamera(cameraUpdate);
    }



}


