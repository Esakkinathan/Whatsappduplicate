package com.example.whatsappduplicate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.Whatsappduplicate.R;
import com.example.whatsappduplicate.LogInSignUp.LoginActivity;
import com.example.whatsappduplicate.LogInSignUp.RegisterActivity;
import com.example.whatsappduplicate.helper.TabAccesorAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    ViewPager mainViewPager;
    TabAccesorAdapter tabAccesorAdapter;
    TabLayout tabLayout;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    DatabaseReference RootRef,UserRef;
    String deviceToken;
    String cUID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser=auth.getCurrentUser();

        if (currentUser!=null) {
            setContentView(R.layout.activity_main);
            RootRef = FirebaseDatabase.getInstance().getReference();
            cUID=auth.getCurrentUser().getUid();
            toolbar = findViewById(R.id.main_activity_toolbar);
            mainViewPager = findViewById(R.id.main_tab_viewPager);
            tabLayout = findViewById(R.id.main_tabs);
            tabAccesorAdapter = new TabAccesorAdapter(getSupportFragmentManager());
            mainViewPager.setAdapter(tabAccesorAdapter);
            setSupportActionBar(toolbar);
            tabLayout.setupWithViewPager(mainViewPager);
            getSupportActionBar().setTitle("Whatsapp");
            UserRef.child(cUID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.hasChild("device_token")) {
                        generateDeviceToken();
                    }
                    else{
                        String t=snapshot.child("device_token").getValue().toString();
                        Log.v("device_token",""+t);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else
        {
            auth.signOut();
            //removeDeviceToken();
            SendUserToLoginActivity();

        }

    }

    private void generateDeviceToken() {
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        deviceToken = Objects.requireNonNull(task1.getResult()).getToken();

                    }

                });
        if(deviceToken==null) {
            deviceToken = FirebaseInstanceId.getInstance().getToken();
            Log.d("deviceToken:",deviceToken);
        }
        String currentUserId=auth.getCurrentUser().getUid();
        UserRef.child(currentUserId).child("device_token").setValue(deviceToken);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=auth.getCurrentUser();
        if(currentUser==null)
        {
            auth.signOut();
            //removeDeviceToken();
            SendUserToLoginActivity();
        }
        else
        {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Loading Chats");
            progressDialog.setMessage("please wait, we are loading your chats");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            updateUserStatusStartActivity("online");
            VerifyExistenseUser();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser=auth.getCurrentUser();
        if(currentUser!=null){
            updateUserStatusActivity("offline");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        FirebaseUser currentUser=auth.getCurrentUser();
        if(currentUser!=null){
            updateUserStatusActivity("online");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent main =new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_DEFAULT);
        main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(main);
        FirebaseUser currentUser=auth.getCurrentUser();
        if(currentUser!=null){
            updateUserStatusActivity("offline");
        }

    }
    private void removeDeviceToken(String currentUserId){

        UserRef.child(currentUserId).child("device_token").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("tag", "device token removed successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("tag", "Failed to remove device token");
            }
        });
    }

    private void updateUserStatusActivity(String status) {
        String currentUserId=auth.getCurrentUser().getUid();
        String currentTime,currentDate;
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat dateFormat=new SimpleDateFormat("MMM dd,yyyy");
        currentDate =dateFormat.format(calendar.getTime());
        SimpleDateFormat timeFormat=new SimpleDateFormat("hh:mm a");
        currentTime =timeFormat.format(calendar.getTime());
        HashMap<String , Object> userStateMap = new HashMap<>();
        userStateMap.put("time",currentTime);
        userStateMap.put("date",currentDate);
        userStateMap.put("state",status);
        RootRef.child("Users").child(currentUserId).child("userState").updateChildren(userStateMap);
    }
    private void updateUserStatusStartActivity(String status) {
        String currentUserId=auth.getCurrentUser().getUid();
        String currentTime,currentDate;
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat dateFormat=new SimpleDateFormat("MMM dd,yyyy");
        currentDate =dateFormat.format(calendar.getTime());
        SimpleDateFormat timeFormat=new SimpleDateFormat("hh:mm a");
        currentTime =timeFormat.format(calendar.getTime());
        HashMap<String , Object> userStateMap = new HashMap<>();
        userStateMap.put("time",currentTime);
        userStateMap.put("date",currentDate);
        userStateMap.put("state",status);
        RootRef.child("Users").child(currentUserId).child("userState").updateChildren(userStateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.cancel();
            }
        });
    }

    private void VerifyExistenseUser() {
        String currentUserId=auth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!(snapshot.child("name")).exists())
                {
                    SendUserToSettingsActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void SendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }
    private void SendUserToSettingsActivity() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.options_menu,menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.logout)
        {
            FirebaseUser currentUser=auth.getCurrentUser();
            if(currentUser!=null){
                updateUserStatusActivity("offline");
            }
            auth.signOut();
            String userId = currentUser.getUid();
            removeDeviceToken(userId);
            SendUserToLoginActivity();
        }
        if(item.getItemId()==R.id.main_settings)
        {
            SendUserToSettingsActivity();
        }
        return true;
    }
}