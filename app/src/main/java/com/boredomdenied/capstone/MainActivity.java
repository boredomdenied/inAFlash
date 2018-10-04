package com.boredomdenied.capstone;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.String.valueOf;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    @BindView(R.id.requestor_button)
    Button requestorButton;
    @BindView(R.id.provider_button)
    Button providerButton;
    @BindView(R.id.location_textView)
    TextView locationTextView;
    @BindView(R.id.distance_textView)
    TextView distanceTextView;
    @BindView(R.id.flashImageView)
    ImageView flashImageView;
    @BindView(R.id.mapLayout)
    LinearLayout mapView;

    @BindString(R.string.locationTextView)
    String defaultLocation;
    @BindString(R.string.permission_rationale)
    String permissionNeeded;
    @BindString(R.string.ok)
    String ok;
    @BindString(R.string.permission_denied_explanation)
    String PermissionDenied;
    @BindString(R.string.settings)
    String settings;

    @BindString(R.string.distance_in_meters)
    String distanceInMeters;
    @BindString(R.string.distance)
    String stringDistance;
    @BindString(R.string.distance_between_called)
    String distanceBetweenCalled;
    @BindString(R.string.no_remove_updates)
    String noRemoveUpdates;
    @BindString(R.string.location_failed)
    String locationFailed;
    @BindString(R.string.no_google_map)
    String noGoogleMap;
    @BindString(R.string.has_google_map)
    String hasGoogleMap;
    @BindString(R.string.my_location)
    String myLocation;
    @BindString(R.string.my_color)
    String myTrueColor;
    @BindString(R.string.my_number)
    String myTrueNumber;
    @BindString(R.string.default_location)
    String theDefaultLocation;
    @BindString(R.string.package_scheme)
    String packageScheme;
    @BindString(R.string.interaction_cancelled)
    String interactionCancelled;
    @BindString(R.string.on_request_permission)
    String onRequestPermission;
    @BindString(R.string.gps_coordinates)
    String gpsCoordinates;
    @BindString(R.string.my_destination)
    String myDestination;
    @BindString(R.string.number_is)
    String numberIs;


    private static final String TAG = MainActivity.class.getSimpleName();

    private float distance = -1.1111f;

    public static boolean isProvider = false;
    public static boolean isRequestor = false;
    public boolean isMapLoaded = false;

    private DatabaseReference FirebaseDatabaseReference;

    public static final String LATITUDE_CHILD = "latitude";
    public static final String LONGITUDE_CHILD = "longitude";
    public static final String REQUESTOR_CHILD = "requestor";
    public static final String PROVIDER_CHILD = "provider";
    public static final String FLASH_CHILD = "flash";
    public static final String COLOR_CHILD = "color";
    public static final String NUMBER_CHILD = "number";
    public static final String CONNECTED_CHILD = "connected";

    public Double providerLatitude = null;
    public Double providerLongitude = null;
    public Double requestorLatitude = null;
    public Double requestorLongitude = null;
    public Double midLat = null;
    public Double midLong = null;
    public LatLng midPoint = null;

    public int myNumber = 0;
    public int myColor = 0;

    public boolean requestorConnected = false;
    public boolean providerConnected = false;

    public Marker currentLocation = null;
    public Marker currentDestination = null;

    public PolylineOptions lineOptions = null;
    public Polyline polyline = null;

    public GoogleMap googleMap;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private MyReceiver myReceiver;

    public GpsUpdateService Service = null;

    private boolean Bound = false;

    public static int randomNumber = 0;
    public static int randomColor = 0;


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GpsUpdateService.LocalBinder binder = (GpsUpdateService.LocalBinder) service;
            Service = binder.getService();
            Bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Service = null;
            Bound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        bindService(new Intent(this, GpsUpdateService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }



        final MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        flashImageView.setVisibility(View.GONE);
        distanceTextView.setVisibility(View.GONE);

        locationTextView.setText(defaultLocation);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseDatabaseReference.keepSynced(true);

        requestorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    providerButton.setEnabled(false);
                    isRequestor = true;
                    Service.requestLocationUpdates();
                    distanceTextView.setVisibility(View.VISIBLE);

                    randomColor = Utils.randomColor();
                    Log.d(numberIs, (valueOf(randomColor)));

                    randomNumber = Utils.randomNumber();
                    Log.d(numberIs, (valueOf(randomNumber)));
                }
            }
        });

        providerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    requestorButton.setEnabled(false);
                    isProvider = true;
                    Service.requestLocationUpdates();
                    distanceTextView.setVisibility(View.VISIBLE);
                }
            }
        });


        FirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                requestorLatitude = (Double) dataSnapshot.child(REQUESTOR_CHILD).child(LATITUDE_CHILD).getValue();
                requestorLongitude = (Double) dataSnapshot.child(REQUESTOR_CHILD).child(LONGITUDE_CHILD).getValue();
                providerLatitude = (Double) dataSnapshot.child(PROVIDER_CHILD).child(LATITUDE_CHILD).getValue();
                providerLongitude = (Double) dataSnapshot.child(PROVIDER_CHILD).child(LONGITUDE_CHILD).getValue();
                myNumber = dataSnapshot.child(FLASH_CHILD).child(NUMBER_CHILD).getValue(Integer.class);
                myColor = dataSnapshot.child(FLASH_CHILD).child(COLOR_CHILD).getValue(Integer.class);
                requestorConnected = dataSnapshot.child(REQUESTOR_CHILD).child(CONNECTED_CHILD).getValue(Boolean.class);
                providerConnected = dataSnapshot.child(PROVIDER_CHILD).child(CONNECTED_CHILD).getValue(Boolean.class);


                if (providerLatitude != null && providerLongitude != null && requestorLatitude != null && requestorLongitude != null) {

                    if (distance != -1.1111f) {
                        Intent widgetIntent = new Intent(getApplicationContext(), FlashWidgetProvider.class);
                        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                        widgetIntent.putExtra(stringDistance, distance);

                        sendBroadcast(widgetIntent);
                    }

                    if (mapView.getVisibility() == View.GONE) {
                        mapView.setVisibility(View.VISIBLE);
                    }

                    distanceMidpoint(providerLongitude, providerLatitude, requestorLongitude, requestorLatitude);
                    distanceBetween(providerLongitude, providerLatitude, requestorLongitude, requestorLatitude);

                    if (isMapLoaded) {

                        if (polyline != null) {
                            polyline.remove();
                        }

                        lineOptions = new PolylineOptions();
                        LatLng location = new LatLng(providerLatitude, providerLongitude);
                        LatLng destination = new LatLng(requestorLatitude, requestorLongitude);

                        currentLocation.setPosition(location);
                        currentDestination.setPosition(destination);

                        lineOptions.add(currentLocation.getPosition(), currentDestination.getPosition());
                        lineOptions.width(12);
                        lineOptions.color(Color.BLUE);
                        lineOptions.geodesic(true);
                        polyline = googleMap.addPolyline(lineOptions);


                        setMapDistance();

                        Log.d(TAG, hasGoogleMap);
                    } else {

                        Log.d(TAG, noGoogleMap);
                    }

                    Log.d(TAG, R.string.distance_between_called + providerLatitude + " " + providerLongitude + " " + requestorLatitude + " " + requestorLongitude);
                }

                Log.d(TAG, distanceInMeters + distance);
                String humanDistance = String.format("%.02f", distance);

                if (distance != 0.0f && distance <= 100.0f && requestorConnected && providerConnected) {
                    mapView.setVisibility(View.GONE);


                    flashImageView.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(getApplicationContext(), FlashActivity.class);
                    intent.putExtra(myTrueColor, myColor);
                    intent.putExtra(myTrueNumber, myNumber);
                    startActivity(intent);

                } else {
                    distanceTextView.setText(distanceInMeters + " " + humanDistance);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseReference setRequestor = FirebaseDatabaseReference.child(REQUESTOR_CHILD).child(CONNECTED_CHILD);
        DatabaseReference setProvider = FirebaseDatabaseReference.child(PROVIDER_CHILD).child(CONNECTED_CHILD);

        if (isRequestor) {
            setRequestor.setValue(false);
        } else if (isProvider) {
            setProvider.setValue(false);
        }
        FirebaseDatabaseReference.keepSynced(false);

    }

    public void distanceMidpoint(Double providerLongitude, Double providerLatitude, Double requestorLongitude, Double requestorLatitude) {
        midLat = (requestorLatitude + providerLatitude) / 2;
        midLong = (requestorLongitude + providerLongitude) / 2;
        midPoint = new LatLng(midLat, midLong);
    }


    public void setMapDistance() {
        if (distance > 700000f) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint, 3));
        } else if (distance > 400000f) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint, 4));
        } else if (distance > 250000f) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint, 5));
        } else if (distance > 150000f) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint, 6));
        } else if (distance > 35000f) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint, 7));
        } else if (distance > 15000f) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint, 8));
        } else if (distance > 8000f) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint, 9));
        } else if (distance > 4000f) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint, 10));
        } else if (distance > 2500f) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint, 11));
        } else if (distance > 1000f) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint, 12));
        } else if (distance > 470f) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint, 13));
        } else if (distance > 225f) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint, 14));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midPoint, 15));
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.clear();
        this.googleMap = googleMap;
        this.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_night));


        if (providerLatitude != null && providerLongitude != null && requestorLatitude != null && requestorLongitude != null) {

            LatLng location = new LatLng(providerLatitude, providerLongitude);
            LatLng destination = new LatLng(requestorLatitude, requestorLongitude);

            currentLocation = googleMap.addMarker(new MarkerOptions().position(location).title(myLocation));
            currentDestination = googleMap.addMarker(new MarkerOptions().position(destination).title(myDestination));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 11));
        } else {

            LatLng location = new LatLng(40.741895, -73.989308);
            LatLng destination = new LatLng(40.741895, -73.989308);

            currentLocation = googleMap.addMarker(new MarkerOptions().position(location).title(myLocation));
            currentDestination = googleMap.addMarker(new MarkerOptions().position(destination).title(myDestination));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 11));

            mapView.setVisibility(View.GONE);
        }

        isMapLoaded = true;

    }


    public float distanceBetween(Double providerLatitude, Double providerLongitude, Double requestorLatitude, Double requestorLongitude) {
        float[] results = new float[1];
        Location.distanceBetween(
                providerLatitude, providerLongitude,
                requestorLatitude, requestorLongitude,
                results);

        distance = results[0];
        return distance;
    }


    public void getGPSLocation(Location location, int randomColor, int randomNumber) {
        DatabaseReference latitudeReq = FirebaseDatabaseReference.child(REQUESTOR_CHILD).child(LATITUDE_CHILD);
        DatabaseReference longitudeReq = FirebaseDatabaseReference.child(REQUESTOR_CHILD).child(LONGITUDE_CHILD);
        DatabaseReference latitudePro = FirebaseDatabaseReference.child(PROVIDER_CHILD).child(LATITUDE_CHILD);
        DatabaseReference longitudePro = FirebaseDatabaseReference.child(PROVIDER_CHILD).child(LONGITUDE_CHILD);
        DatabaseReference flashNumber = FirebaseDatabaseReference.child(FLASH_CHILD).child(NUMBER_CHILD);
        DatabaseReference flashColor = FirebaseDatabaseReference.child(FLASH_CHILD).child(COLOR_CHILD);
        DatabaseReference setRequestor = FirebaseDatabaseReference.child(REQUESTOR_CHILD).child(CONNECTED_CHILD);
        DatabaseReference setProvider = FirebaseDatabaseReference.child(PROVIDER_CHILD).child(CONNECTED_CHILD);


        if (isRequestor) {
            requestorLatitude = location.getLatitude();
            requestorLongitude = location.getLongitude();
            latitudeReq.setValue(requestorLatitude);
            longitudeReq.setValue(requestorLongitude);
            setRequestor.setValue(true);

            if (randomColor != 0 && randomNumber != 0)
                flashNumber.setValue(randomNumber);
            flashColor.setValue(randomColor);

        } else if (isProvider) {
            providerLatitude = location.getLatitude();
            providerLongitude = location.getLongitude();
            latitudePro.setValue(providerLatitude);
            longitudePro.setValue(providerLongitude);
            setProvider.setValue(true);
            Log.d(TAG, gpsCoordinates + providerLatitude + " & " + providerLongitude);

            requestorButton.setEnabled(false);
        }

        locationTextView.setVisibility(View.GONE);

    }


    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(GpsUpdateService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        DatabaseReference setRequestor = FirebaseDatabaseReference.child(REQUESTOR_CHILD).child(CONNECTED_CHILD);
        DatabaseReference setProvider = FirebaseDatabaseReference.child(PROVIDER_CHILD).child(CONNECTED_CHILD);

        if (Bound) {
            unbindService(mServiceConnection);
            Bound = false;
            if (isRequestor) {
                setRequestor.setValue(false);
            } else if (isProvider) {
                setProvider.setValue(false);
            }
        }
        FirebaseDatabaseReference.keepSynced(false);

    }

    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);


        if (shouldProvideRationale) {
            Log.i(TAG, permissionNeeded);
            Snackbar.make(
                    findViewById(R.id.constraintLayout),
                    permissionNeeded,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, permissionNeeded);

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, onRequestPermission);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, interactionCancelled);
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Service.requestLocationUpdates();
            } else {
                Snackbar.make(
                        findViewById(R.id.constraintLayout),
                        PermissionDenied,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts(packageScheme,
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }


    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(GpsUpdateService.EXTRA_LOCATION);
            if (location != null) {

                Log.d(TAG, theDefaultLocation + location);
                getGPSLocation(location, randomColor, randomNumber);
            }

        }
    }


}
