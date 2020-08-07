package com.example.viyer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";
    private Button btnLogin;
    private Button btnSignup;
    private TextInputLayout etEmail;
    private TextInputLayout etPassword;
    public FirebaseAuth mAuth;
    private TextView tvPhoneAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            getMainActivity();
        }

        etEmail = findViewById(R.id.etDate);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignUp);
        tvPhoneAuth = findViewById(R.id.tvPhoneAuth);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser(etEmail.getEditText().getText().toString().trim(), etPassword.getEditText().getText().toString().trim());
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSignUpActivity();
            }
        });

        tvPhoneAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPhoneAuthActivity();
            }
        });
    }

    private void getMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void getSignUpActivity() {
        Intent i = new Intent(this, SignUpActivity.class);
        startActivity(i);
    }

    private void getPhoneAuthActivity() {
        Intent i = new Intent(this, PhoneAuthActivity.class);
        startActivity(i);
    }

    public static FirebaseFirestore db() {
        return FirebaseFirestore.getInstance();
    };

    public static FirebaseAuth mAuth() {
        return FirebaseAuth.getInstance();
    }

    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            getMainActivity();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}