package net.thinghub.joinhackathon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Pin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);
        String[] data = loadData();
        final String pin = data[2];
        final Button bt = (Button)findViewById(R.id.acceptPin);
        final TextInputLayout text = (TextInputLayout)findViewById(R.id.input_layout_pin_2);
        bt.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pin.compareTo(text.getEditText().getText().toString()) == 0) {
                    Intent intent = new Intent(Pin.this, MapsActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(Pin.this, "The pin code is not correct (" + pin + ")" , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String[] loadData () {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        return new String[] {prefs.getString("name", null), prefs.getString("phone", null), prefs.getString("pin", null)};
    }
}
