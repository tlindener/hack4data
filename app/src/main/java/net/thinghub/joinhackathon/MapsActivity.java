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
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;

import ai.kitt.snowboy.SnowboyDetect;

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
    int progressT = defaultRadius;

    private static final String TAGR = "King";

    private static final String[] neededPermissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;

    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";

    public static final int RECORDER_BPP = 16;
    public static int RECORDER_SAMPLERATE = 16000;
    public static int RECORDER_CHANNELS = 1;
    public static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private boolean isExit = false;

    private SnowboyDetect snowboyDetector;
    Message msg = new Message();
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            toastMessage("Help! Ayuda!");

            return false;
        }
    });

    protected void startRecordingThread() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int i = 0;
            for (; i < neededPermissions.length; i++) {
                if (checkSelfPermission(neededPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(neededPermissions, REQUEST_CODE_ASK_PERMISSIONS);
                    break;
                }
            }
            if (i >= neededPermissions.length) {
                initial();
            }
        } else {
            initial();
        }
    }



    private void initial() {
        // Assume you put the model related files under /sdcard/snowboy/
        snowboyDetector = new SnowboyDetect(Environment.getExternalStorageDirectory().getAbsolutePath()+"/models/common.res",
                /*"/storage/emulated/legacy/snowboy.umdl");*/
                Environment.getExternalStorageDirectory().getAbsolutePath()+"/models/Ayuda.pmdl");
        snowboyDetector.SetSensitivity("0.5");         // Sensitivity for each hotword
        snowboyDetector.SetAudioGain(2.0f);              // Audio gain for detection
        Log.i(TAGR, "NumHotwords = "+snowboyDetector.NumHotwords()+", BitsPerSample = "+snowboyDetector.BitsPerSample()+", NumChannels = "+snowboyDetector.NumChannels()+", SampleRate = "+snowboyDetector.SampleRate());

        /*bufferSize = AudioRecord.getMinBufferSize
                (sampleRate, channels, audioEncoding) * 3;*/
        bufferSize = snowboyDetector.NumChannels() * snowboyDetector.SampleRate() * 1;
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                bufferSize);

        startRecord();
    }

    public void startRecord() {
        if (isRecording){
            return;
        }

        int i = recorder.getState();
        if (i == AudioRecord.STATE_INITIALIZED) {
            recorder.startRecording();
        }

        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private void writeAudioDataToFile() {
        /*FileOutputStream os = null;
        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/

        short data[] = new short[bufferSize/2];

        int read = 0;
        //try {
        while (isRecording) {
            read = recorder.read(data, 0, data.length);
            Log.i(TAGR, "read length = " + read);
            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    /*os.write(data, 0, read);
                    os.flush();*/
                int result = snowboyDetector.RunDetection(data, data.length);
                if (result == 1) {
                    handler.sendMessage(msg);
                    msg = new Message();
                }
                Log.i(TAGR, " ----> result = "+result);
            }
            //Thread.sleep(30);
        }/*
        }catch (InterruptedException e) {
            e.printStackTrace();
        }/* finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/


        if (recorder != null) {
            recorder.stop();
            recorder = null;
        }
        Log.i(TAGR, "detectSpeaking finished.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRecording = false;
        if (recorder != null) {
            recorder.stop();
            recorder = null;
        }
    }

    public void toastMessage(String message) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        String url ="";
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        try {
            Location location = locationManager.getLastKnownLocation(provider);
            if  (location != null)
                url = "http://maps.google.com/?q="+ location.getLatitude()+ URLEncoder.encode(",")+location.getLongitude();
        }catch (SecurityException ex)
        {        }
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String[] clientData = loadData();

            smsManager.sendTextMessage(clientData[1], null, clientData[0] + " just called for help! "+ url , null, null);
        } catch (Exception e) {
            Toast.makeText(this, "It crashed " + e, Toast.LENGTH_LONG).show();
        }
        Toast toast = Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(30);
        toast.show();
    }
    private String[] loadData() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        return new String[]{prefs.getString("name", null), prefs.getString("phone", null), prefs.getString("pin", null)};
    }
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
                    if(status.getStatus().isSuccess()) {
                        Toast.makeText(MapsActivity.this, "Geofence created successfully", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, status.getStatus().toString());
                    }
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
                progressT = progress;
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
                //Toast.makeText(MapsActivity.this, "Circle clicked Id=" + circle.getId() + " existing Id=" + userMarker.getId(), Toast.LENGTH_SHORT).show();
                if (circle.getId().equals(userMarker.getId())) // if marker source is clicked{
                {
                    /*LocationServices.GeofencingApi.removeGeofences(mApiClient,createGeofencePendingIntent());
                    userGeoFence = null;
                    userMarker.remove();*/
                    //Toast.makeText(MapsActivity.this, "Color from " + userMarker.getFillColor() + " to " + Color.GREEN, Toast.LENGTH_SHORT).show();
                    Toast.makeText(MapsActivity.this, "progress " + progressT, Toast.LENGTH_SHORT).show();
                    userGeoFence = new Geofence.Builder().setRequestId("userGeofenceId")
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                            .setCircularRegion(point.latitude, point.longitude, progressT)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .build();
                    startGeofence(userGeoFence);

                    userMarker.setFillColor(0x40ff669a);

                    radiusBar.setVisibility(View.GONE);

                    startRecordingThread();
                    //Intent intent = new Intent(MapsActivity.this, TrackingActivity.class);
                    //startActivity(intent);
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

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
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You should agree all of the permissions, force exit! please retry", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            initial();
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
