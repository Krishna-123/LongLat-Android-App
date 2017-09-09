package android.aitlindia.com.longlat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView lblLocation;
    private Button btnStartTracking, btnStopTracking;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lblLocation = (TextView) findViewById(R.id.lblLocation);
        btnStartTracking = (Button) findViewById(R.id.btnStartTracking);
        btnStopTracking = (Button) findViewById(R.id.btnStopTracking);
        intent = new Intent(this, LatLonService.class);
        btnStartTracking.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                startService(intent);
                lblLocation.setText("Tracking Service is started!! ");
            }
        });

        btnStopTracking.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                stopService(intent);
                lblLocation.setText("Tracking Service is stop!! ");
            }
        });
    }

}

