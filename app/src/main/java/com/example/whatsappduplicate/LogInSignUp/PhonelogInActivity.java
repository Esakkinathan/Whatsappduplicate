package com.example.whatsappduplicate.LogInSignUp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.Whatsappduplicate.R;
import com.example.whatsappduplicate.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class PhonelogInActivity extends AppCompatActivity {
    EditText verification_text, phonenumber;
    Button sendVerificationCode,Verify;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callback;
    String verificationId;
    PhoneAuthProvider.ForceResendingToken token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonelog_in);
        Initialize();
        auth =FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        sendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String PhoneNumber = phonenumber.getText().toString();
                //Toast.makeText(getApplicationContext(),PhoneNumber, Toast.LENGTH_SHORT).show();
                if (TextUtils.isEmpty(PhoneNumber))
                {
                    phonenumber.setError("please enter phone number");
                }
                else
                {
                    //Toast.makeText(getApplicationContext(),"Coming", Toast.LENGTH_SHORT).show();
                    progressDialog.setTitle("Phone Verification");
                    progressDialog.setMessage("please wait, while we are authenticating");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthOptions options=PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber("+91"+PhoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(PhonelogInActivity.this)
                            .setCallbacks(callback)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                    auth.setLanguageCode("en");
                }
            }
        });
        callback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                progressDialog.cancel();
                verification_text.setText(phoneAuthCredential.getSmsCode());
                signInWithPhoneCredentials(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.cancel();
                Toast.makeText(getApplicationContext(),""+e, Toast.LENGTH_SHORT).show();
                Verify.setVisibility(View.INVISIBLE);
                verification_text.setVisibility(View.INVISIBLE);
                sendVerificationCode.setVisibility(View.VISIBLE);
                phonenumber.setVisibility(View.VISIBLE);

                if(e instanceof FirebaseAuthInvalidCredentialsException)
                {
                    Toast.makeText(getApplicationContext(),"invalid request", Toast.LENGTH_SHORT).show();

                }
                if(e instanceof FirebaseTooManyRequestsException)
                {
                    Toast.makeText(getApplicationContext(),"Your sms limit has been expired", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken Token) {
                verificationId=s;
                token=Token;
                Toast.makeText(getApplicationContext(),"Code Sent", Toast.LENGTH_SHORT).show();

                Verify.setVisibility(View.VISIBLE);
                verification_text.setVisibility(View.VISIBLE);
                sendVerificationCode.setVisibility(View.INVISIBLE);
                phonenumber.setVisibility(View.INVISIBLE);
            }

        };
        Verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phonenumber.setVisibility(View.INVISIBLE);
                sendVerificationCode.setVisibility(View.INVISIBLE);
                String code=verification_text.getText().toString();
                if(TextUtils.isEmpty(code))
                {
                    verification_text.setError("please enter verification code");
                }
                else{
                    progressDialog.setTitle("Verification Code");
                    progressDialog.setMessage("please wait, while we are verifying your code");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,code);
                    signInWithPhoneCredentials(credential);
                }

            }
        });
    }

    private void Initialize() {
        verification_text = findViewById(R.id.phone_number_verify_code);
        sendVerificationCode = findViewById(R.id.send_verify_code_btn);
        phonenumber = findViewById(R.id.phone_number_edit);
        Verify = findViewById(R.id.verify_btn);
        Verify.setVisibility(View.INVISIBLE);
        verification_text.setVisibility(View.INVISIBLE);
        sendVerificationCode.setVisibility(View.VISIBLE);
        phonenumber.setVisibility(View.VISIBLE);


    }
    public void signInWithPhoneCredentials(PhoneAuthCredential phoneAuthCredential)
    {
        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    String deviceToken= FirebaseInstanceId.getInstance().getToken();
                    String currentUserId=auth.getCurrentUser().getUid();
                    DatabaseReference UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
                    UserRef.child(currentUserId).child("device_token").setValue(deviceToken);
                    Toast.makeText(getApplicationContext(),"You are successfully logged in ", Toast.LENGTH_SHORT).show();
                    SendUserToMainActivity();
                }
                else
                {
                    String message=task.getException().getMessage();
                    Toast.makeText(getApplicationContext(),"Error"+message, Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
                progressDialog.cancel();

            }
        });

    }
    private void SendUserToMainActivity() {
        Intent intent = new Intent(PhonelogInActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}