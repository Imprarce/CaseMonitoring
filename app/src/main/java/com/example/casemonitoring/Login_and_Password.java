package com.example.casemonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


public class Login_and_Password extends AppCompatActivity {

    Button enter;
    EditText login;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enter = (Button) findViewById(R.id.Enter);

        login = (EditText) findViewById(R.id.Login);
        password = (EditText) findViewById(R.id.Password);

        enter.setOnClickListener(view -> {
           // if(login.getText().toString().equals("login") && password.getText().toString().equals("password")){
                startActivity(new Intent(Login_and_Password.this, MainPage.class));
                finish();
          //  }
        });

    }
}