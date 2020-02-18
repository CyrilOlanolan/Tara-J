package com.garate.tara_j;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.garate.tara_j.directionHelpers.FetchURL;
import com.garate.tara_j.directionHelpers.TaskLoadedCallback;
import com.garate.tara_j.fragments.AboutFragment;
import com.garate.tara_j.fragments.FaresFragment;
import com.garate.tara_j.model.RoutesActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.android.SphericalUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.IOException;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {
    /* TODO (Priority): Add fragments - 75%
    */

    // MAPS DECLARATION
    public static GoogleMap mMap;
    public static LatLng origin = null;
    public static LatLng destination = null;
    public static Marker markerDestination = null;
    public static Marker markerOrigin = null;
    public static LatLng latLng;

    // DIRECTIONS API DECLARATIONS
    private Button buttonTara;
    public static Polyline currentPolyline;
    private double distanceInKm;
    public static double fare;

    // WIDGET DECLARATIONS
    public static TextView searchDestination;
    public static TextView searchOrigin;
    public static String nameOrigin;
    public static String nameDestination;
    public Button buttonOrigin;
    public Button buttonDestination;
    private Button getButtonDrawer;
    private Switch switchDiscount;
    public static Drawer result;

    // MISC DECLARATIONS
    LocationManager locationManager;
    private int isSwitched = 0;
    private boolean isFirstTime;
    private static final String TAG = "RoutesActivity";
    public static int forMarkerOriginFlag = 0;
    private boolean isDiscounted = false;

    /*
        MAIN METHODS
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FIRE TUTORIAL SCREEN
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);

        if (firstStart) {
            showWelcomeSplash();
        }
        else {
            Intent intent = new Intent(MapsActivity.this, SplashScreen.class);
            startActivity(intent);
        }

        setContentView(R.layout.activity_maps);

        // Initialize Places. For simplicity, the API key is hard-coded. In a production
        // environment we recommend using a secure mechanism to manage API keys.
        String apiKey = getString(R.string.google_maps_key);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        //INITIALIZE BUTTONS
        buttonOrigin = findViewById(R.id.buttonOrigin);
        buttonDestination = findViewById(R.id.buttonDestination);
        buttonTara = findViewById(R.id.buttonTara);
        getButtonDrawer = findViewById(R.id.buttonDrawer);
        switchDiscount = findViewById(R.id.switchDiscount);

        buttonOrigin.setOnClickListener(v -> {
            openOrigin();
        });
        buttonDestination.setOnClickListener(v -> {
            openDestination();
        });
        buttonTara.setOnClickListener(v -> {
            if (origin == null || destination == null) {
                Toast.makeText(MapsActivity.this, "Pick locations first!", Toast.LENGTH_SHORT).show();
            } else {
                new FetchURL(MapsActivity.this).execute(getUrl(markerOrigin.getPosition(), markerDestination.getPosition(), "transit"), "transit");
            }
        });

        switchDiscount.setTypeface(ResourcesCompat.getFont(MapsActivity.this, R.font.bold));
        switchDiscount.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isDiscounted == false) {
                isDiscounted = true;
            }
            else {
                isDiscounted = false;
            }
        });

        //INITIALIZE TEXTS
        searchOrigin = findViewById(R.id.origin_name);
        searchDestination = findViewById(R.id.destination_name);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // DRAWER STUFF
        if (origin == null || destination == null) {
            drawerLayout();
        }
    }

    private void showWelcomeSplash() {
        Intent intent = new Intent(MapsActivity.this, WelcomeSplash.class);
        startActivity(intent);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }

    /*
    TODO: The following code doesn't work if you want to switch locations
     after pressing "USE MY LOCATION" button in Origin or Destination classes.
     */
//    private void switchLocations() {
//        LatLng positionOrigin;
//        LatLng positionDestination;
//
//        if (origin != null && destination != null) {
//            LatLng originalOrigin = origin;
//            LatLng originalDestination = destination;
//            String originalNameOrigin = nameOrigin;
//            String originalNameDestination = nameDestination;
//            LatLng tempDestination;
//            LatLng tempOrigin;
//
//            mMap.clear();
//            switch (isSwitched) {
//                case 0:
//                    //STORING
//                    tempOrigin = origin;
//                    tempDestination = destination;
//                    String tempNameOrigin = nameOrigin;
//                    String tempNameDestination = nameDestination;
//
//                    //SWITCH
//                    origin = tempDestination;
//                    destination = tempOrigin;
//
//                    //SET TEXT
//                    searchOrigin.setText(tempNameDestination);
//                    searchDestination.setText(tempNameOrigin);
//
//                    //SET MARKER
//                    try {
//                        positionOrigin = Origin.place.getLatLng();
//                    }
//                    catch (NullPointerException e) {
//                        positionOrigin = latLng;
//                    }
//
//                    try {
//                        positionDestination = Destination.place.getLatLng();
//                    }
//                    catch (NullPointerException e) {
//                        positionDestination = latLng;
//                    }
//
//                    markerOrigin = mMap.addMarker(new MarkerOptions()
//                            .position(positionDestination)
//                            .title("ORIGIN: " + nameDestination)
//                            .snippet(Destination.addressDestination)
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_origin)));
//
//                    markerDestination = mMap.addMarker(new MarkerOptions()
//                            .position(positionOrigin)
//                            .title("DESTINATION: " + nameOrigin)
//                            .snippet(Origin.addressOrigin)
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_destination)));
//
//                    //ALERT
//                    Toast.makeText(MapsActivity.this, "Location switched", Toast.LENGTH_SHORT).show();
//
//                    isSwitched = 1;
//                    break;
//
//                case 1:
//                    //SWITCH
//                    origin = originalOrigin;
//                    destination = originalDestination;
//
//                    //SET TEXT
//                    searchOrigin.setText(originalNameOrigin);
//                    searchDestination.setText(originalNameDestination);
//
//                    //SET MARKER
//                    try {
//                        positionOrigin = Origin.place.getLatLng();
//                    }
//                    catch (NullPointerException e) {
//                        positionOrigin = latLng;
//                    }
//
//                    try {
//                        positionDestination = Destination.place.getLatLng();
//                    }
//                    catch (NullPointerException e) {
//                        positionDestination = latLng;
//                    }
//
//                    markerOrigin = mMap.addMarker(new MarkerOptions()
//                            .position(positionOrigin)
//                            .title("ORIGIN: " + nameOrigin)
//                            .snippet(Origin.addressOrigin)
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_origin)));
//
//                    markerDestination = mMap.addMarker(new MarkerOptions()
//                            .position(positionDestination)
//                            .title("DESTINATION: " + nameDestination)
//                            .snippet(Destination.addressDestination)
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_destination)));
//
//                    //ALERT
//                    Toast.makeText(MapsActivity.this, "Switched back to original locations", Toast.LENGTH_SHORT).show();
//
//                    //SET MARKER
//
//                    isSwitched = 0;
//                    break;
//            }
//        } else {
//            Toast.makeText(MapsActivity.this, "Select locations first", Toast.LENGTH_SHORT).show();
//        }
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //DISABLE COMPASS
        mMap.getUiSettings().setCompassEnabled(false);

        //Marker
        isFirstTime = true;
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Log.d("mylog", "Added Markers");

        //FIRE permissions
        Dexter.withActivity(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                // CHECK if all permissions are GRANTED
                if (report.areAllPermissionsGranted()) {
                    ShowDefault();
                    Geolocation();
                    mMap.setMyLocationEnabled(true);
                }
                else {
                    showSettingsDialog();
                    ShowDefault();
                }

                // CHECK if any permission is PERMANENTLY DENIED
                if (report.isAnyPermissionPermanentlyDenied()) {
                    showSettingsDialog();
                    ShowDefault();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest(); //PROMPT for the permission
            }
        }).withErrorListener(error -> Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show()).onSameThread().check(); //CHECKS FOR ERROR

        //ADD LONG CLICK LISTENERS THAT CAN ALSO SET FOR ORIGIN AND DESTINATION
        LongClickListeners();
    }

    @Override
    protected void onRestart() {
    //TODO Auto-generated method stub
        super.onRestart();
        if (origin != null && !"Pin location".equals(nameOrigin)) {
            Log.e(TAG, "Getting origin name");
            GetNameOrigin();
            forMarkerOriginFlag = 1;
        }

        if (destination != null && !"Pin location".equals(nameDestination)) {
            Log.e(TAG, "Getting destination name");
            GetNameDestination();
            forMarkerOriginFlag = 0;
        }
    }

    private void GetNameOrigin() {
        if (nameOrigin != null) {
            searchOrigin.setText(nameOrigin);
        }
    }

    private void GetNameDestination() {
        if (nameDestination != null) {
            searchDestination.setText(nameDestination);
        }
    }

    //METHOD for the GEOLOCATION
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void Geolocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //CHECK if network provider is ENABLED
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @SuppressWarnings("MoveFieldAssignmentToInitializer")
                @Override
                public void onLocationChanged(Location location) {

                    //GET latitude
                    double latitude = location.getLatitude();
                    //GET longitude
                    double longitude = location.getLongitude();
                    //INSTANTIATE LatLng class
                    latLng = new LatLng(latitude, longitude);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
        //GPS Backup
        else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //GET latitude
                    double latitude = location.getLatitude();
                    //GET longitude
                    double longitude = location.getLongitude();
                    //INSTANTIATE LatLng class
                    latLng = new LatLng(latitude, longitude);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
    }

    private void LongClickListeners() {
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                switch (forMarkerOriginFlag) {
                    case 0:
                        //SET NEW ORIGIN BASED ON PIN LOCATION
                        origin = latLng;
                        searchOrigin.setText("Pin Location");
                        nameOrigin = "Pin Location";

                        if (markerOrigin != null) {
                            markerOrigin.remove();
                            markerOrigin = null;
                        }

                        if (markerOrigin == null) {
                            markerOrigin = MapsActivity.mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("ORIGIN")
                                    .snippet("Pin location")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_origin)));
                            Toast.makeText(getApplicationContext(), "Origin set at pin location. Coordinates: " + latLng, Toast.LENGTH_LONG).show();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 13f));
                        }
                        forMarkerOriginFlag = 1;
                        break;
                    case 1:
                        //SET NEW DESTINATION BASED ON PIN LOCATION
                        destination = latLng;
                        searchDestination.setText("Pin Location");
                        nameDestination = "Pin Location";

                        if (markerDestination != null) {
                            markerDestination.remove();
                            markerDestination = null;
                        }

                        if (markerDestination == null) {
                            markerDestination = MapsActivity.mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("DESTINATION")
                                    .snippet("Pin location")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_destination)));
                            Toast.makeText(getApplicationContext(), "Destination set at pin location. Coordinates: " + latLng, Toast.LENGTH_LONG).show();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 13f));
                        }
                        forMarkerOriginFlag = 0;
                        break;

                }
                if (currentPolyline != null) {
                    currentPolyline.remove();
                }
            }
        });
    }

    //SET ORIGIN INSTANCE FOR BUTTON
    private void openOrigin() {
        Intent intent = new Intent(this, Origin.class);
        startActivity(intent);
    }

    //SET DESTINATION INSTANCE FOR BUTTON
    public void openDestination() {
        Intent intent = new Intent(this, Destination.class);
        startActivity(intent);

    }

    //DEFAULT MAP VIEW
    private void ShowDefault() {
        LatLng davaoDefault = new LatLng(7.0707, 125.6087);
        if (isFirstTime == true) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(davaoDefault, 12f));
        }
        isFirstTime = false;
    }

    //OPEN SETTINGS in case the user DENIES
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("TaraJ needs location and Storage permissions to function. You can grant them in the application's settings. (TaraJ > Permissions)");
        builder.setPositiveButton("OPEN SETTINGS", (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    // DIRECTIONS METHODS
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        Log.d("mylog", "URL " + url);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();

        // Set Polyline
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
        currentPolyline.setColor(Color.parseColor("#2a2b5d"));
        currentPolyline.setWidth(12);
        currentPolyline.setClickable(true);

        // Set Camera
        LatLngBounds.Builder routeBounds = new LatLngBounds.Builder();
        routeBounds.include(origin).include(destination);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(routeBounds.build(), 300);
        mMap.animateCamera(cameraUpdate);

        // Click Polyline
        mMap.setOnPolylineClickListener(polyline -> {
            // Calculate Distance
            distanceInKm = SphericalUtil.computeDistanceBetween(origin, destination) / 1000;
            fare = getFare(distanceInKm);

            Intent intent = new Intent(MapsActivity.this, FareInfo.class);
            startActivity(intent);

            Toast.makeText(getApplicationContext(), "Distance: " + distanceInKm + " kM, Fare: PHP" + fare, Toast.LENGTH_LONG).show();
            Log.d(TAG, (fare) + " Pesos");
        });
    }

    // DRAWER
    public void drawerLayout() {
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.white)
                .build();

        result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withHasStableIds(true)
                .withDrawerWidthDp(250)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Home").withIdentifier(1).withSelectable(false),
                        new PrimaryDrawerItem().withName("Routes").withIdentifier(2).withSelectable(false),
                        new PrimaryDrawerItem().withName("Fare").withIdentifier(3).withSelectable(false),
                        new PrimaryDrawerItem().withName("About").withIdentifier(4).withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Fragment fragment = null;
                        if (drawerItem != null) {
                            Intent intent = null;
                            if (drawerItem.getIdentifier() == 1) {
                                intent = new Intent(MapsActivity.this, MapsActivity.class);
                            } else if (drawerItem.getIdentifier() == 2) {
                                intent = new Intent(MapsActivity.this, RoutesActivity.class);
                            } else if (drawerItem.getIdentifier() == 3) {
                                fragment = new FaresFragment();
                            } else if (drawerItem.getIdentifier() == 4) {
                                fragment = new AboutFragment();
                            }

                            // OPEN ACTIVITY
                            if (intent != null) {
                                MapsActivity.this.startActivity(intent);
                            }

                            // OPEN FRAGMENTS
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            if (fragment != null) {
                                fragmentManager.beginTransaction().replace(R.id.fragment_contain, fragment).commit();
                            }
                        }
                        return false;
                    }
                })
                .build();

        getButtonDrawer.setOnClickListener(v -> {
            result.openDrawer();
        });

    }

    // FARE CALCULATOR
    public double getFare(double distance) {
        double fare = 8.00;
        if (isDiscounted == false) {
            if (distance > 4) {
                fare = 8.00 + (1.50 * Math.ceil(distance - 4));
            }
        }
        else {
            fare = 6.20;
            if (distance > 4) {
                fare = 6.20 + (1.20 * Math.ceil(distance - 4));
            }
        }
        return fare;
    }
}
