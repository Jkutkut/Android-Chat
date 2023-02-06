package com.jkutkut.android_chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etxtEmail;
    private EditText etxtPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etxtEmail = findViewById(R.id.etxtEmail);
        etxtPassword = findViewById(R.id.etxtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String user = etxtEmail.getText().toString().trim();
        String passw = etxtPassword.getText().toString().trim();

        // TODO check input and login

        Intent i = new Intent(getBaseContext(), ChatActivity.class);
        i.putExtra(ChatActivity.USER_KEY, user);
        startActivity(i);
    }
}