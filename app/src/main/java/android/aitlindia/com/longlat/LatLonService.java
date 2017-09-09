package android.aitlindia.com.longlat;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LatLonService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "Service_Tracker";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    // Google client to interact with Google API
    public static GoogleApiClient mGoogleApiClient;
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters
    private final int NotificationId = 10;
    //    public String lat_lon;
    private NotificationManager myNotificationManager;
    private Location mLastLocation;
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private double Latitude;
    private double Longitude;
    private String DateOfLastLocation;
    private String CurrentDate;
    private float Bearing;
    private String DifferenceInLocTime;

    public LatLonService() {
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) getApplicationContext(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        myNotificationManager.cancel(NotificationId);
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void showNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.map)
                        .setContentTitle("Tracking")
                        .setContentText("Your tracking service is on !");
        mBuilder.setOngoing(true);
        mBuilder.setColor(Color.RED);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        myNotificationManager.notify(NotificationId, mBuilder.build());
    }

    public String displayLocation() {
        JSONObject objectLoc = new JSONObject();
        String myLocation = "{ }";

        try {
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        } catch (SecurityException ex) {
            Log.d(TAG, ex.toString());
        }

        if (mLastLocation != null) {
            Latitude = mLastLocation.getLatitude();
            Longitude = mLastLocation.getLongitude();
            Bearing = mLastLocation.getBearing();
            DateOfLastLocation = getDate(mLastLocation.getTime(), "dd/MM/yyyy hh:mm:ss");
            CurrentDate = getDate(System.currentTimeMillis(), "dd/MM/yyyy hh:mm:ss");
            DifferenceInLocTime = DifferInTime((System.currentTimeMillis() - mLastLocation.getTime()) / (long) (1000 * 60));

        /*    lat_lon = "CurrentDate : " + CurrentDate + ", " + "LastOfLoc : " + DateOfLastLocation + ", " + "DiffInLoc : " +
                    DifferenceInLocTime + ", " + "Lat : " + Latitude + ", " + "Lng : " + Longitude + ", " + "Bearing : " + Bearing;
        */
            try {
                objectLoc.put("CurrentDate", CurrentDate);
                objectLoc.put("LastOfLoc", DateOfLastLocation);
                objectLoc.put("DiffInLoc", DifferenceInLocTime);
                objectLoc.put("Lat", Latitude);
                objectLoc.put("Lng", Longitude);
                objectLoc.put("Bearing", Bearing);
            } catch (JSONException e) {
                e.printStackTrace();
            }

         }
        /* else {
            lat_lon = 00.00 + "," + 00.00;
        }*/
        myLocation = objectLoc.toString();
        Log.d(TAG, myLocation);
        return myLocation;
    }

    private String DifferInTime(long diffMin) {
        int Day, Hour = 0, Min;
        String DHM;

        Min = (int) (diffMin % 60);
        if (diffMin >= 60) {
            Hour = (int) (diffMin / 60);
        }

        if (Hour >= 24) {
            Day = Hour / 24;
            Hour = Hour % 24;
        } else {
            Day = 0;
        }

        DHM = Day + "day, " + Hour + "hrs, " + Min + "min";
        return DHM;
    }

    /**
     * Method to toggle periodic location updates
     */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            // Changing the button text

            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();

            //  Log.d(TAG, "Periodic location updates started!");

        } else {
            // Changing the button text
            mRequestingLocationUpdates = false;

            // Stopping the location updates
            stopLocationUpdates();

            // Log.d(TAG, "Periodic location updates stopped!");
        }
    }

/*
    public String sendLatLon() {
        displayLocation();
        return lat_lon;
    }
*/

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException ex) {
            Log.d(TAG, ex.toString());
        }
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayLocation();
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.

            // First we need to check availability of play services
            if (checkPlayServices()) {
                // Building the GoogleApi client
                buildGoogleApiClient();
                createLocationRequest();
            }

            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }

            togglePeriodicLocationUpdates();

            myNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            showNotification();

            if (mGoogleApiClient.isConnected()) {
                Thread Http_Thread = new Thread(new HTTPRequestThread());
                Http_Thread.setPriority(Thread.NORM_PRIORITY);
                Http_Thread.start();
            }

            //---------------------------------------------------------------------------------------//
            Toast.makeText(getApplicationContext(), "Tracker Service Started", Toast.LENGTH_LONG).show();

        }
    }
}
