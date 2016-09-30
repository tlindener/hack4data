package net.thinghub.joinhackathon;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import java.util.Calendar;

public class TrackingActivity extends AppCompatActivity implements View.OnClickListener {

    EditText startTimeText, endTimeText;
    private int mHour, mMinute;
    Button btStart, btEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        btStart = (Button)findViewById(R.id.startTimeButton);
        btEnd = (Button)findViewById(R.id.endTimeButton);
        startTimeText=(EditText)findViewById(R.id.startTimeText);
        endTimeText=(EditText)findViewById(R.id.endTimeText);

        btStart.setOnClickListener(this);
        btEnd.setOnClickListener(this);

        Button btSave = (Button)findViewById(R.id.saveButton);
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrackingActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);


        if (v == btStart) {
            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            startTimeText.setText(hourOfDay + ":" + minute);
                        }
                    }, mHour, mMinute, true);
            timePickerDialog.show();
        } else if (v == btEnd){
            // Launch Time Picker Dialog

            if (!startTimeText.getText().toString().isEmpty()) {
                mHour = Integer.parseInt(startTimeText.getText().toString().split(":")[0]);
                mMinute = Integer.parseInt(startTimeText.getText().toString().split(":")[1]);

            }

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            endTimeText.setText(hourOfDay + ":" + minute);
                        }
                    }, mHour, mMinute, true);
            timePickerDialog.show();
        }

    }
    // Intent intent = new Intent(TrackingActivity.this, Timer.class);
    // startActivity(intent);
}
