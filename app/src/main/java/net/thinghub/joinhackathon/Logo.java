package net.thinghub.joinhackathon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Logo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        String[] data = loadData();
        String name = data[0];
        String phone = data[1];
        String pin = data[2];

        if (pin!=null && pin.compareTo("00000") == 0) {
            startActivity(new Intent(this, MapsActivity.class));
        } else if (name!=null && phone!=null && pin!=null) {
            startActivity(new Intent(this, Pin.class));
        } else {
            startActivity(new Intent(this, Login.class));
        }
    }

    private String[] loadData () {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        return new String[] {prefs.getString("name", null), prefs.getString("phone", null), prefs.getString("pin", null)};
    }
}
