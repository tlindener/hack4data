package net.thinghub.joinhackathon;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Timer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        new CountDownTimer(30000, 1000) {
            final TextView timer = (TextView) findViewById(R.id.textTimer);
            public void onTick(long millisUntilFinished) {
                timer.setText("seconds remaining: " + millisUntilFinished / 1000);
            }
            public void onFinish() {
                timer.setText("done!");
            }
        }.start();
    }
}
