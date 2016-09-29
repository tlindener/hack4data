package net.thinghub.joinhackathon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        Toast.makeText(Logo.this, "name: " + name + " phone: " + phone + " pin: " + pin, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(Logo.this, Login.class);
        startActivity(intent);
    }

    private String[] loadData () {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        return new String[] {prefs.getString("name", null), prefs.getString("phone", null), prefs.getString("pin", null)};
    }
}
