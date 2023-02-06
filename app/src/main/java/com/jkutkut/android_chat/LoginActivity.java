package com.jkutkut.android_chat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etxtEmail;
    private EditText etxtPassword;
    private Button btnLogin;

    private FirebaseAuth mAuth;

    private ActivityResultLauncher<Intent> chatLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        chatLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    System.err.println(result.getResultCode());
                    if (result.getResultCode() == ChatActivity.RESULT_LOGOUT) {
                        mAuth.signOut();
                    }
                    else if (result.getResultCode() == ChatActivity.RESULT_EXIT) {
                        finish();
                    }
                }
        );

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            login(currentUser.getEmail());
        }

        etxtEmail = findViewById(R.id.etxtEmail);
        etxtPassword = findViewById(R.id.etxtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String mail = etxtEmail.getText().toString().trim();
        String passw = etxtPassword.getText().toString().trim();

        if (mail.length() == 0) {
            etxtEmail.setError("User can not be empty");
            return;
        }
        if (passw.length() == 0) {
            etxtPassword.setError("Password can not be empty"); // TODO getText
            return;
        }

        // TODO check login with firebase
        mAuth.signInWithEmailAndPassword(mail, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            login(user.getEmail());
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void login(String mail) {
        mail = mail.replaceAll("@.+", "");
        mail = mail.replaceAll("[\\.#\\$\\[\\]]", "");
        System.out.println("mail" + mail);
        Intent i = new Intent(getBaseContext(), ChatActivity.class);
        i.putExtra(ChatActivity.USER_KEY, mail);
        chatLauncher.launch(i);
    }
}