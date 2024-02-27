package com.example.mobileproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    EditText usernameEditText, passwordEditText;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
        usernameEditText = findViewById(R.id.email_login);
        passwordEditText = findViewById(R.id.password);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final Button loginButton = findViewById(R.id.login_button);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        String email = sharedPref.getString("email", null);
        if (Objects.nonNull(email)) {
            ToLogin();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!usernameEditText.getText().toString().isEmpty() && !passwordEditText.getText().toString().isEmpty()) {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    loginButton.setVisibility(View.GONE);
                    firebaseAuth.signInWithEmailAndPassword(usernameEditText.getText().toString(), passwordEditText.getText().toString())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Hoş Geldiniz! " + firebaseAuth.getCurrentUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                                    sharedPref.edit()
                                            .putString("email", usernameEditText.getText().toString())
                                            .putString("password", passwordEditText.getText().toString())
                                            .apply();
                                    loadingProgressBar.setVisibility(View.INVISIBLE);
                                    loginButton.setVisibility(View.VISIBLE);
                                    ToLogin();
                                } else {
                                    loadingProgressBar.setVisibility(View.INVISIBLE);
                                    loginButton.setVisibility(View.VISIBLE);
                                    Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    Toast.makeText(getApplicationContext(), "Kayıt Olmaya!", Toast.LENGTH_SHORT).show();
                                    ToRegister();
                                }
                            });
                    // LOGİN İŞLEMLERİ
                } else {
                    Toast.makeText(getApplicationContext(), "Kayıt Olmaya!", Toast.LENGTH_SHORT).show();
                    ToRegister();
                    // REGISTERA YÖNLENDİR
                }
            }
        });
    }

    private void ToLogin() {
        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
        MainActivity.this.startActivity(intent);
    }

    private void ToRegister() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        MainActivity.this.startActivity(intent);
    }


}
