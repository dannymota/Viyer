package com.example.viyer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    public static final String TAG = "SignUpActivity";
    private TextInputLayout etFirstName;
    private TextInputLayout etLastName;
    private TextInputLayout etLocation;
    private TextInputLayout etEmail;
    private TextInputLayout etPassword;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etLocation = findViewById(R.id.etLocation);
        etEmail = findViewById(R.id.etDate);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpUser(getTextInputString(etEmail), getTextInputString(etPassword));
            }
        });
    }

    private void signUpUser(String email, String password) {
        LoginActivity.mAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            addUserToCollection(LoginActivity.mAuth().getCurrentUser().getUid());
                            getMainActivity();
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static String getTextInputString(TextInputLayout id) {
        return id.getEditText().getText().toString().trim();
    }

    public void addUserToCollection(String uid) {
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", getTextInputString(etFirstName));
        user.put("lastName", getTextInputString(etLastName));
        user.put("location", getTextInputString(etLocation));
        user.put("email", getTextInputString(etEmail));
        user.put("updatedAt", new Timestamp(new Date()));
        user.put("id", false);

        LoginActivity.db().collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void getMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}