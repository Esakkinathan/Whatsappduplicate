package com.example.whatsappduplicate.LogInSignUp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    TextView neednewAccount,forgotpassword;
    Button phonenumberlogin,LogIn;
    EditText email,password;
    DatabaseReference UserRef;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    String deviceToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Initilize();
        progressDialog = new ProgressDialog(this);
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        neednewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });
        phonenumberlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPhoneActivity();
            }
        });
        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserLogin();
            }
        });
        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLinktoMail();
            }
        });
    }

    private void sendLinktoMail() {
        if(email.getText().toString().matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$") && email.getText().toString().length()>8){
            AlertDialog.Builder passwordreset = new AlertDialog.Builder(this);
            passwordreset.setTitle("Reset Password?");
            passwordreset.setMessage("press Yes to receive the reset link");
            passwordreset.setPositiveButton("YES",(dialogInterface,i)->
            {
                String resetEmail = email.getText().toString();
                auth.sendPasswordResetEmail(resetEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(),"reset email link has been to your email id",Toast.LENGTH_SHORT).show();
                    }
                });
            });
            passwordreset.setNegativeButton("NO",(dialogInterface,i)-> {});
            passwordreset.create().show();
        }
        else{
            email.setError("Please enter a valid email");
        }

    }

    private void AllowUserLogin() {
        String userEmail = email.getText().toString();
        String userPass = password.getText().toString();

        if (TextUtils.isEmpty(userEmail)) {
            email.setError("please enter email id");
        }
        if (TextUtils.isEmpty(userPass)) {
            password.setError("please enter Password");
        }
        if (TextUtils.isEmpty(userEmail) && TextUtils.isEmpty(userPass)) {
            email.setError("please enter email id ");
            password.setError("please enter Password");
        }
        if (!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPass)) {
            progressDialog.setTitle("Signing In");
            progressDialog.setMessage("please wait, while we are logging into your account");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            auth.signInWithEmailAndPassword(userEmail,userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
                        FirebaseInstanceId.getInstance().getInstanceId()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        deviceToken = Objects.requireNonNull(task1.getResult()).getToken();
                                        Log.d("deviceToken:",deviceToken);

                                    }

                                });
                        if(deviceToken==null) {
                            deviceToken = FirebaseInstanceId.getInstance().getToken();
                            Log.d("deviceToken:",deviceToken);
                        }
                        String currentUserId=auth.getCurrentUser().getUid();
                        UserRef.child(currentUserId).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                SendUserToMainActivity();
                                Toast.makeText(getApplicationContext(), "logged in successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else {
                        String message=task.getException().getLocalizedMessage();
                        Toast.makeText(getApplicationContext(), "Error"+message, Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.cancel();
                    progressDialog.dismiss();

                }
            });
        }
    }

    private void SendUserToRegisterActivity() {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
    }
    private void SendUserToPhoneActivity() {
        Intent intent = new Intent(LoginActivity.this, PhonelogInActivity.class);
        startActivity(intent);
    }
    private void SendUserToMainActivity() {
        Intent intent =new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void Initilize() {
        neednewAccount = findViewById(R.id.needanewaccount);
        phonenumberlogin = findViewById(R.id.phone_number_login);
        email=findViewById(R.id.login_email);
        password=findViewById(R.id.login_password);
        LogIn=findViewById(R.id.login_btn);
        forgotpassword=findViewById(R.id.forgot_password);
        auth = FirebaseAuth.getInstance();


    }
}