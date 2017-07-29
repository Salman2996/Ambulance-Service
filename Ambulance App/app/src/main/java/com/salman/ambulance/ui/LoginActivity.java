package com.salman.ambulance.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import in.abhilash.ambulance.R;

import static in.abhilash.ambulance.ui.MainActivity.ID_KEY;

public class LoginActivity extends AppCompatActivity {

    EditText et_phoneNumber;
    Button button_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = pref.edit();
        ed.putBoolean("login_activity_executed", true);
        ed.commit();

        et_phoneNumber = (EditText) findViewById(R.id.input_phonenumber);
        button_login = (Button) findViewById(R.id.btn_login);
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_phoneNumber.getText().toString().isEmpty()){
                    et_phoneNumber.setError("Can't be empty");
                    return;
                }
                SharedPreferences.Editor ed = pref.edit();
                ed.putString(ID_KEY, et_phoneNumber.getText().toString());
                ed.commit();
                Bundle extras = getIntent().getExtras();
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                if(extras != null) intent.putExtras(extras);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


    }
}
