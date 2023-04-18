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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {
    Button register;

    EditText login;

    EditText password;

    private static boolean login_check = false;
    private static boolean password_check = false;


    private Base_Login_and_Password mDBHelper;
    private SQLiteDatabase mDb;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mUsersRef = mDatabase.getReference("my_data");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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


        register.setOnClickListener(v -> {

            if(isPassword_check(password.getText().toString())){
                password_check = true;
            } else password_check = false;

            if(isLogin_check((login).getText().toString())){
                login_check = true;
            } else login_check = false;

            if(login_check == true && password_check == true){

                String new_User = "INSERT INTO Users (login, password) VALUES" + "('" + login.getText().toString() + "', '" + password.getText().toString() + "')";
                mDb.execSQL(new_User);

                mAuth.createUserWithEmailAndPassword(login.getText().toString() + "@example.com", password.getText().toString())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                String userId = mAuth.getCurrentUser().getUid();


                                Map<String, Object> user = new HashMap<>();
                                user.put("login", login.getText().toString());
                                user.put("password", password.getText().toString());


                                mUsersRef.child(userId).setValue(user);
                                Toast.makeText(getApplicationContext(), "Вы успешно зарегистировались", Toast.LENGTH_LONG).show();

                            } else {
                               Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT);
                            }
                        });


                startActivity(new Intent(Register.this, Authorization.class));


            } else if(password_check == false) {
                Toast.makeText(getApplicationContext(), "Вы написали некорректный пароль, правила выше", Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(getApplicationContext(), "Данный логин уже занят", Toast.LENGTH_LONG).show();
            }
        });

    }



    protected boolean isLogin_check(String login){
        String login_User = login;

        Cursor cursor = mDb.rawQuery("SELECT * FROM Users", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if(login_User.equals(cursor.getString(1)))
            {
                return false;
            }
            cursor.moveToNext();
        }
        cursor.close();
        return true;
    }

    protected boolean isPassword_check(String password){
        Pattern pattern = Pattern.compile("^(?=.*[A-Z])(?=.*[0-9]).{8,}$");
        Matcher matcher = pattern.matcher(password);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

}