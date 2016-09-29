package net.thinghub.joinhackathon;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static net.thinghub.joinhackathon.Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST;
import static net.thinghub.joinhackathon.Constants.GEOFENCE_EXPIRATION_TIME;

public class MapsActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener, GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMapLongClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private String TAG ="TAG";
    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;
    private Circle userMarker;

    // These will store hard-coded geofences in this sample app.
    private Geofence userGeoFence;

    private LocationServices mLocationService;
    // Stores the PendingIntent used to request geofence monitoring.
    private PendingIntent mGeofenceRequestIntent;
    private GoogleApiClient mApiClient;

    // SeekBar
    SeekBar radiusBar;
    private int defaultRadius = 120; // In meters
    int progress = defaultRadius;


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // If the error has a resolution, start a Google Play services activity to resolve it.
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.e("TAG", "Exception while resolving connection error.", e);
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            Log.e("TAG", "Connection to Google Play services failed with error code " + errorCode);
        }
    }

    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters

    // Create a Geofence
    private Geofence createGeofence( LatLng latLng, float radius ) {
        Log.d("TAG", "createGeofence");
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(GEOFENCE_EXPIRATION_TIME)
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }
    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence ) {
        Log.d("TAG", "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence( geofence )
                .build();
    }

    /**
     * Once the connection is available, send a request to add the Geofences.
     */
    @Override
    public void onConnected(Bundle connectionHint) {

    }
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private PendingIntent createGeofencePendingIntent() {
        Log.d("TAG", "createGeofencePendingIntent");
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent( this, GeofenceTransitionService.class);
        Log.d("TAG", "createGeofencePendingIntent getService");
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d("TAG", "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    mApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    //Toast.makeText(MapsActivity.this, "Result of addGeofence: "+status.getStatus().toString(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG,status.getStatus().toString());
                }
            });
    }
    // Start Geofence creation process
    private void startGeofence(Geofence geofence) {
        Log.i(TAG, "startGeofence()");
        if( userMarker != null ) {
            GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
            addGeofence( geofenceRequest );
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        radiusBar = (SeekBar)findViewById(R.id.radiusBar);


    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setIndoorEnabled(false);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapLongClickListener(this);
        enableMyLocation();
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onMapLongClick(final LatLng point) {
        //make sure you can only create a single geofence
        if(userGeoFence != null)
            return;
        // Instantiates a new CircleOptions object and defines the center and radius
        final CircleOptions circleOptions = new CircleOptions()
                .center(point)
                .clickable(true)
                .fillColor(0x40ff4081) // 0x means hex, pos 2 and 3 mean transparency
                .strokeColor(0x80ff4081)
                .strokeWidth(4.5f)
                .radius(defaultRadius); // In meters

        // Get back the mutable Circle
        userMarker = mMap.addCircle(circleOptions);

        // Make the SeekBar visible
        radiusBar.setMax(1000);
        radiusBar.setProgress(defaultRadius);
        radiusBar.setVisibility(View.VISIBLE);



        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                userMarker.setRadius(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userMarker.setFillColor(Color.TRANSPARENT);
                //userMarker.setStrokeColor(0x80ff4081);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userMarker.setFillColor(0x40ff4081);
                //userMarker.setStrokeColor(Color.TRANSPARENT);
            }
        });


        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                Toast.makeText(MapsActivity.this, "Circle clicked Id=" + circle.getId() + " existing Id=" + userMarker.getId(), Toast.LENGTH_SHORT).show();
                if (circle.getId().equals(userMarker.getId())) // if marker source is clicked{
                {
                    /*LocationServices.GeofencingApi.removeGeofences(mApiClient,createGeofencePendingIntent());
                    userGeoFence = null;
                    userMarker.remove();*/
                    //Toast.makeText(MapsActivity.this, "Color from " + userMarker.getFillColor() + " to " + Color.GREEN, Toast.LENGTH_SHORT).show();

                    userGeoFence = new Geofence.Builder().setRequestId("userGeofenceId")
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                            .setCircularRegion(point.latitude, point.longitude, progress)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .build();
                    startGeofence(userGeoFence);

                    userMarker.setFillColor(0x40ff669a);
                    Intent intent = new Intent(MapsActivity.this, TrackingActivity.class);
                    startActivity(intent);
                }

            }

        });


    }


    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
                    enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (null != mGeofenceRequestIntent) {
            LocationServices.GeofencingApi.removeGeofences(mApiClient, mGeofenceRequestIntent);
        }
    }


    /**
     * Checks if Google Play services is available.
     *
     * @return true if it is.
     */
    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Log.isLoggable("TAG", Log.DEBUG)) {
                Log.d("TAG", "Google Play services is available.");
            }
            return true;
        } else {
            Log.e("TAG", "Google Play services is unavailable.");
            return false;
        }
    }

}
