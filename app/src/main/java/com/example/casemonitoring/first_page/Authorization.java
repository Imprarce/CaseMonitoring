package com.example.casemonitoring.first_page;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.casemonitoring.R;
import com.example.casemonitoring.second_page.MainPage;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;


public class Authorization extends AppCompatActivity {

    Button enter;

    Button register;
    EditText login;
    EditText password;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private Base_Login_and_Password mDBHelper;
    private SQLiteDatabase mDb;

    private static boolean login_check = false;
    private static boolean password_check = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enter = (Button) findViewById(R.id.Enter);
        register = (Button) findViewById(R.id.Register);

        login = (EditText) findViewById(R.id.Login);
        password = (EditText) findViewById(R.id.Password);

        mDBHelper = new Base_Login_and_Password(this);

        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            mDb = mDBHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }

        enter.setOnClickListener(view -> {

                mAuth.signInWithEmailAndPassword(login.getText().toString() + "@example.com", password.getText().toString()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(Authorization.this, MainPage.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Логин или пароль введены неверно", Toast.LENGTH_LONG).show();
                    }
                });

        });

        register.setOnClickListener(v -> {
            startActivity(new Intent(Authorization.this, Register.class));
        });
    }

    protected boolean isLogin_check(String login){
        String login_User = login;

        Cursor cursor = mDb.rawQuery("SELECT * FROM Users", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if(login_User.equals(cursor.getString(1)))
            {
                return true;
            }
            cursor.moveToNext();
        }
        cursor.close();
        return false;
    }

    protected boolean isPassword_check(String password){
        String password_User = password;

        Cursor cursor = mDb.rawQuery("SELECT * FROM Users", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if(password_User.equals(cursor.getString(2)))
            {
                return true;
            }
            cursor.moveToNext();
        }
        cursor.close();
        return false;
    }
}