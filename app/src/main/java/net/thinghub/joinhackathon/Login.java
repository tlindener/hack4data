package net.thinghub.joinhackathon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String PHONE_REGEX = "^\\+?\\d{1,3}?[- .]?\\(?(?:\\d{2,3})\\)?[- .]?\\d\\d\\d[- .]?\\d\\d\\d\\d$";
        final Pattern pattern = Pattern.compile(PHONE_REGEX);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button bt = (Button)findViewById(R.id.submitButton);

        final TextInputLayout text_name = (TextInputLayout)findViewById(R.id.input_layout_name);
        final TextInputLayout text_phone = (TextInputLayout)findViewById(R.id.input_layout_phone);
        final TextInputLayout text_pin = (TextInputLayout)findViewById(R.id.input_layout_pin);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = text_name.getEditText().getText().toString();
                String phone = text_phone.getEditText().getText().toString();
                String pin = text_pin.getEditText().getText().toString();

                if (name.isEmpty()) {
                    Toast.makeText(Login.this, "The name cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (pin.length() != 4) {
                    Toast.makeText(Login.this, "The pin must have 4 numbers", Toast.LENGTH_SHORT).show();
                }
                else if ((!phone.contains("!") && phone.length()==9) || !pattern.matcher(phone).matches()) {
                    Toast.makeText(Login.this, "The phone number must be a real number", Toast.LENGTH_SHORT).show();
                } else {
                    storeData(name, phone, pin);
                    Intent intent = new Intent(Login.this, MapsActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
    private void storeData (String name, String phone, String pin) {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("name", name);
        editor.putString("phone", phone);
        editor.putString("pin", pin);
        editor.commit();
    }
}
