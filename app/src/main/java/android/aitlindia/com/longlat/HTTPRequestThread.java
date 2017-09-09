package android.aitlindia.com.longlat;

import android.util.Log;

/**
 * Created by krishna on 25/8/17.
 */


class HTTPRequestThread implements Runnable {

    private LatLonService latLon;

    HTTPRequestThread() {
        latLon = new LatLonService();
    }

    @Override
    public void run() {

        while (LatLonService.mGoogleApiClient.isConnected()) {
            // HTTP Post Call

            JSONParser myRquest = new JSONParser();
            String URL = "http://192.168.42.134:3000/";
            String received_Result = myRquest.makeHttpRequest(URL, "POST", latLon.displayLocation());
            Log.d("received data" , received_Result);
            int ten_Seconds = 10 * 1000;
            wait(ten_Seconds);
        }
    }

    //-------------------------------------------------------------------------------------------//

    private void wait(int interval_milisecond) {
        try {
            Thread.sleep(interval_milisecond);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            String TAG = "Tracker_HTTpCliThread";
            Log.e(TAG, "Wait Exception", e);
        }
    }

}


