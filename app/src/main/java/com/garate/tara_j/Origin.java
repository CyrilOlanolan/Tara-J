package com.garate.tara_j;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Origin extends AppCompatActivity {

    private final String TAG = "Origin.java";
    public AutocompleteSupportFragment autocompleteFragmentOrigin;
    public String nameOrigin;
    public static String addressOrigin;
    public Button searchAnOrigin;
    public Button useMyLocationAsOrigin;
    public ImageButton backButton;
    public final int AUTOCOMPLETE_REQUEST_CODE = 1;

    //FOR SWITCH MARKER NAME
    public static Place place;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_origin);

        //INITIALIZE BACK BUTTON
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //CREATE API KEY OBJECT
        String apiKey = getString(R.string.google_maps_key);

        //CHECK IF INITIALIZED
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // CREATE NEW placesCLIENT INSTANCE
        PlacesClient placesClient = Places.createClient(this);

        //INITIALIZE BUTTONS
        searchAnOrigin = findViewById(R.id.search_an_origin);
        searchAnOrigin.setOnClickListener(v -> onSearchCalled());

        useMyLocationAsOrigin = findViewById(R.id.use_my_location_as_origin);
        useMyLocationAsOrigin.setOnClickListener(v -> {
            MapsActivity.origin = MapsActivity.latLng;
            Log.e(TAG, "SET TEXT");
            MapsActivity.searchOrigin.setText("My Location");
            MapsActivity.nameOrigin = "My Location";

            if (MapsActivity.markerOrigin != null) {
                MapsActivity.markerOrigin.remove();
                MapsActivity.markerOrigin = null;
            }

            if (MapsActivity.markerOrigin == null) {
                MapsActivity.markerOrigin = MapsActivity.mMap.addMarker(new MarkerOptions()
                        .position(MapsActivity.latLng)
                        .title("ORIGIN")
                        .snippet(MapsActivity.latLng.toString())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_origin)));
                Toast.makeText(getApplicationContext(), "Origin set at pin location. Coordinates: " + MapsActivity.latLng, Toast.LENGTH_LONG).show();
                MapsActivity.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MapsActivity.origin, 13f));
            }

            //RETURN TO MAIN ACTIVITY
            finish();
        });
    }

    public void onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // Create a RectangularBounds object.
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(6.957, 125.250),
                new LatLng(7.302, 125.638));

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("PH") //PHILIPPINES
                .setLocationRestriction(bounds)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                nameOrigin = place.getName();
                MapsActivity.nameOrigin = place.getName();
                addressOrigin = place.getAddress();
                MapsActivity.origin = place.getLatLng();

                if (MapsActivity.markerOrigin != null) {
                    MapsActivity.markerOrigin.remove();
                    MapsActivity.markerOrigin = null;
                }

                if (MapsActivity.markerOrigin == null) {
                    MapsActivity.markerOrigin = MapsActivity.mMap.addMarker(new MarkerOptions()
                            .position(place.getLatLng())
                            .title("ORIGIN: " + nameOrigin)
                            .snippet(addressOrigin)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_origin)));
                    MapsActivity.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MapsActivity.origin, 13f));
                    if (MapsActivity.currentPolyline != null) {
                        MapsActivity.currentPolyline.remove();
                    }
                }

                //FINISH ACTIVITY
                finish();

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(Origin.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        finish();
    }
}
