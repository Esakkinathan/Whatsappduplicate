package com.example.whatsappduplicate.LogInSignUp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Whatsappduplicate.R;
import com.example.whatsappduplicate.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    EditText regEmail,regPassword;
    TextView alreadyHaveanAccount;
    Button createanewAccount;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    DatabaseReference RootRef;
    String deviceToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Intialize();
        auth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        alreadyHaveanAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });
        createanewAccount.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        }));
    }

    private void CreateNewAccount() {
        String userEmail = regEmail.getText().toString();
        String userPass = regPassword.getText().toString();
        if (TextUtils.isEmpty(userEmail)) {
            regEmail.setError("please enter email id");
        }
        if (TextUtils.isEmpty(userPass)) {
            regPassword.setError("please enter Password");
        }
        if (TextUtils.isEmpty(userEmail) && TextUtils.isEmpty(userPass)) {
            regEmail.setError("please enter email id ");
            regPassword.setError("please enter Password");
        }
        if (!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPass)) {
            progressDialog.setTitle("Create New Account");
            progressDialog.setMessage("please wait, while we are creating new account");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            auth.createUserWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseInstanceId.getInstance().getInstanceId()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        deviceToken = Objects.requireNonNull(task1.getResult()).getToken();
                                        Log.d("deviceToken:",deviceToken);

                                    }

                                });
                        if(deviceToken==null) {
                            deviceToken = FirebaseInstanceId.getInstance().getToken();
                        }
                        deviceToken= FirebaseInstanceId.getInstance().getToken();
                        String currentUserId=auth.getCurrentUser().getUid();
                        RootRef.child("Users").child(currentUserId).setValue("");
                        RootRef.child("Users").child(currentUserId).child("device_token").setValue(deviceToken);
                        SendUserToMainActivity();
                        Toast.makeText(getApplicationContext(), "account created successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "error occured while creating account", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.cancel();
                    progressDialog.dismiss();
                }
            });
        }

    }

    private void SendUserToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(intent);
    }
    private void SendUserToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void Intialize() {
        regEmail=findViewById(R.id.signup_email);
        regPassword=findViewById(R.id.signup_password);
        alreadyHaveanAccount=findViewById(R.id.already_have_acc);
        createanewAccount=findViewById(R.id.signup_btn);
        progressDialog = new ProgressDialog(this);


    }
}