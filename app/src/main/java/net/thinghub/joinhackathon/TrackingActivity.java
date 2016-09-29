package net.thinghub.joinhackathon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

public class TrackingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        final Button startButton = (Button)findViewById(R.id.mainButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            boolean state = true;
            @Override
            public void onClick(View v) {
                if (state == true) {
                    final TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
                    Toast.makeText(TrackingActivity.this, "Smartphone watched until " + timePicker.getHour() + "h "
                            + timePicker.getMinute() + "m", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(TrackingActivity.this, Timer.class);
                    startActivity(intent);
                }
            }
        });
    }
}
