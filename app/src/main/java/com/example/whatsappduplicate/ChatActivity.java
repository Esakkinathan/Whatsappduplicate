package com.example.whatsappduplicate;

import static com.example.whatsappduplicate.Fragments.StatusFragment.PICK_IMAGE_REQUEST;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
//import android.location.LocationRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;

import com.bumptech.glide.Glide;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.Whatsappduplicate.R;

import com.example.whatsappduplicate.helper.LocationHelper;
import com.example.whatsappduplicate.helper.MessageAdaptar;
import com.example.whatsappduplicate.helper.Messages;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationRequest;
//import android.location.LocationRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import top.defaults.colorpicker.ColorPickerPopup;
import yuku.ambilwarna.AmbilWarnaDialog;

public class ChatActivity extends AppCompatActivity {
    ImageButton sendMsg, sendFiles;
    String msgRecId, msRecname, msgRecImg = "", messageSenderid;
    String cUID;
    TextView username, lastSeen;
    FirebaseAuth auth;
    DatabaseReference RootRef, UserRef,backRef;
    RelativeLayout relativeLayout;
    Button ConatctSettings;
    CircleImageView userImg;
    Uri fileuri;
    EditText message;
    Toolbar toolbar;
    ProgressDialog progressDialog;
    LinearLayoutManager linearLayoutManager;
    MessageAdaptar messageAdapter;
    StorageReference storageReference,backgroundRefernce;
    List<Messages> messagesList = new ArrayList<>();
    RecyclerView recyclerView;
    String saveCurrentTime, saveCurrentDate, checker = "";
    UploadTask uploadTask;
    String recToken;
    String recName;
    FusedLocationProviderClient mFusedLocationClient;
    boolean toNotify = false;
    boolean onlineState;
    private static final int REQUEST_LOCATION = 1;
    int PERMISSION_ID = 44;
    LocationManager locationManager;
    private LocationCallback locationCallback;
    int resultCode;
    LocationRequest locationRequest;
    double longitude;
    double latitude;
    int mDefaultColor;
    private LocationHelper locationHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();
        cUID=auth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        backRef = FirebaseDatabase.getInstance().getReference().child("Background");

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        backgroundRefernce=FirebaseStorage.getInstance().getReference().child("Background Images/");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        saveCurrentDate = dateFormat.format(calendar.getTime());
        saveCurrentTime = timeFormat.format(calendar.getTime());
        msgRecId = getIntent().getExtras().get("uid").toString();
        msRecname = getIntent().getExtras().get("name").toString();
        msgRecImg = getIntent().getExtras().get("image").toString();
        relativeLayout = findViewById(R.id.chat_background);
        //SetBackground();
        setBackgroundColor();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        UserRef.child(msgRecId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("device_token")) {
                    recToken = snapshot.child("device_token").getValue().toString();
                    Log.d("device token got ",recToken);
                    //recName = snapshot.child("name").getValue().toString();
                    toNotify = true;
                } else {
                    toNotify = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        UserRef.child(cUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("name")) {
                    //recToken = snapshot.child("device_token").getValue().toString();
                    recName = snapshot.child("name").getValue().toString();
                    Log.d("Reciever name got:",recName);
                    toNotify = true;
                } else {
                    toNotify = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            String token = Objects.requireNonNull(task.getResult()).getToken();

                        }
                    }
                });

        message = findViewById(R.id.chat_input_message);
        toolbar = findViewById(R.id.custom_chat_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        ConatctSettings = findViewById(R.id.contactsettings);
        ConatctSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendToPorfileActivity();
            }
        });
        username = findViewById(R.id.custom_profile_name);
        lastSeen = findViewById(R.id.custom_user_last_seen);
        userImg = findViewById(R.id.custom_profile_image);
        sendMsg = findViewById(R.id.send_message_chat);
        sendFiles = findViewById(R.id.file_attachment);
        messageSenderid = auth.getCurrentUser().getUid();
        messageAdapter = new MessageAdaptar(messagesList, getApplicationContext());
        if (msgRecId != null && msRecname != null) {
            username.setText(msRecname);
            if (msgRecImg != null) {
                GetImage(msgRecImg, userImg);

            }

        }
        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView = findViewById(R.id.chat_recycler_view);
        DisplayLastSeen();
        progressDialog = new ProgressDialog(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);
        sendFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Ms Word Files",
                                "Share Live location",
                                "Change Background"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Image from here"),
                                    438);
                        }
                        if (i == 1) {
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setType("application/pdf");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Pdf from here"),
                                    438);
                        }
                        if (i == 2) {
                            checker = "docx";
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("*/*");
                            String[] mimetypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword"};
                            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                            startActivityForResult(intent, 438);
                        }
                        if (i == 3) {
                            gotolocationclass();

                            //SendMessageLocation(latitude, longitude);
                            //gotoFuse();
                        }
                        if(i==4){
                            //checker = "background-image";
                            //Intent intent = new Intent();
                            //intent.setType("image/*");
                            //intent.setAction(Intent.ACTION_GET_CONTENT);
                            //startActivityForResult(Intent.createChooser(intent, "Select Image from here"),
                            //       438);
                            chooseBackgroundColor();
                        }
                    }
                });
                builder.show();
            }
        });
        setRecyclerView();
    }

    private void gotolocationclass() {
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {
                progressDialog.setTitle("sending live location");
                progressDialog.setMessage("work in progress");
                progressDialog.setCancelable(false); // Prevent the user from canceling i
                progressDialog.show();
                locationHelper = new LocationHelper(getApplicationContext());

                locationHelper.startLocationUpdates(new LocationHelper.LocationCallbackListener() {
                    @Override
                    public void onLocationResult(Location location) {
                        // Handle the location update here
                        if(location!=null){
                            progressDialog.dismiss();
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            // Do something with the location data
                            SendMessageLocation(latitude, longitude);
                            locationHelper.stopLocationUpdates(); // Stop location updates after receiving one
                            locationHelper = null;
                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else {
                Toast.makeText(this, "please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");

    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");
    }

    private void updateUserStatus(String status) {
        FirebaseUser currentUser = auth.getCurrentUser();
        String currentUserId = currentUser.getUid();
        RootRef.child("Users").child(currentUserId).child("userState").child("state").setValue(status);
    }


    private void setRecyclerView() {
        RootRef.child("Messages").child(messageSenderid).child(msgRecId)
                .addChildEventListener(new ChildEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Messages messages = snapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    private boolean checkOffline() {


        RootRef.child("Users").child(msgRecId).child("userState").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("state").getValue().toString().equals("online")) {
                    onlineState = true;
                }
                else{
                    onlineState = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Toast.makeText(getApplicationContext(), "" +onlineState, Toast.LENGTH_SHORT).show();
        if (onlineState) {
            Log.d("oombu","false");
            return false;
        } else {
            Log.d("oombu","true");
            return true;
        }
    }
    private void DisplayLastSeen() {
        RootRef.child("Users").child(msgRecId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("userState").hasChild("state")) {
                    String state = snapshot.child("userState").child("state").getValue().toString();
                    String date = snapshot.child("userState").child("date").getValue().toString();
                    String time = snapshot.child("userState").child("time").getValue().toString();
                    if (state.equals("online")) {
                        lastSeen.setText("online");

                    } else if (state.equals("offline")) {

                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy");
                        String CurrentDate = dateFormat.format(calendar.getTime());
                        if (CurrentDate.equals(date)) {
                            lastSeen.setText(time.toLowerCase(Locale.ROOT));

                        } else {
                            lastSeen.setText(date);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void SendMessage() {

        String messageText = message.getText().toString();
        if (TextUtils.isEmpty(messageText)) {
            message.setError("Please enter a message");

        } else {
            String messageSenderRef = "Messages/" + messageSenderid + "/" + msgRecId;
            String messageRecieverRef = "Messages/" + msgRecId + "/" + messageSenderid;
            DatabaseReference userMessageRef = RootRef.child("Messages")
                    .child(messageSenderid).child(msgRecId).push();
            String messagePushId = userMessageRef.getKey();
            Map messageTextReady = new HashMap();
            messageTextReady.put("message", messageText);
            messageTextReady.put("type", "text");
            messageTextReady.put("from", messageSenderid);
            messageTextReady.put("to", msgRecId);
            messageTextReady.put("messageID", messagePushId);
            messageTextReady.put("time", saveCurrentTime);
            messageTextReady.put("date", saveCurrentDate);
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextReady);
            messageBodyDetails.put(messageRecieverRef + "/" + messagePushId, messageTextReady);
            RootRef.updateChildren(messageBodyDetails);
            message.setText("");
            //Toast.makeText(getApplicationContext(), "" + checkOffline(), Toast.LENGTH_SHORT).show();
            if (toNotify) {

                //Toast.makeText(this, "" + recName + recToken, Toast.LENGTH_SHORT).show();
                FcmNotificationsSender notificationsSender = new FcmNotificationsSender(recToken, "New Message from " + recName, messageText, getApplicationContext(), ChatActivity.this);
                notificationsSender.SendNotifications();
            }


        }
    }

    private void SendToPorfileActivity() {
        Intent profileIntent = new Intent(ChatActivity.this, ProfileActivity.class);
        profileIntent.putExtra("visited_uid", msgRecId);
        startActivity(profileIntent);
    }

    private void GetImage(String currentUser, CircleImageView imageView) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().
                child("Profile Images/" + currentUser + ".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(imageView);
            }
        });
    }
    private void SetBackground() {
        //StorageReference storageReference = FirebaseStorage.getInstance().getReference().
        //child("Profile Images/" + currentUser + ".jpg");
        StorageReference setbg = backgroundRefernce.child(cUID+msgRecId+ ".jpg");
        Log.v("Background Image name:",setbg.toString());
        setbg.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ChatActivity.this)
                        .load(uri)
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                relativeLayout.setBackground(resource);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                // Handle failure to load the image here
                                relativeLayout.setBackgroundResource(R.drawable.chat_wall);
                            }

                            @Override
                            public void onLoadStarted(@Nullable Drawable placeholder) {
                                // Handle image loading progress here (if needed)
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                relativeLayout.setBackgroundResource(R.drawable.chat_wall);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 438 && resultCode == RESULT_OK) {
            progressDialog.setTitle("Sending File");
            progressDialog.setMessage("please wait , we are sending the file");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            fileuri = data.getData();
            Log.d("FileUri",fileuri.toString());
            if (checker.equals("pdf") || checker.equals("docx")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                String messageSenderRef = "Messages/" + messageSenderid + "/" + msgRecId;
                String messageRecieverRef = "Messages/" + msgRecId + "/" + messageSenderid;
                DatabaseReference userMessageRef = RootRef.child("Messages")
                        .child(messageSenderid).child(msgRecId).push();
                String messagePushId = userMessageRef.getKey();
                StorageReference filePath = storageReference.child(messagePushId + "." + checker);
                filePath.putFile(fileuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Map messageTextReady = new HashMap();
                        messageTextReady.put("message", messagePushId);
                        messageTextReady.put("name", fileuri.getLastPathSegment());
                        messageTextReady.put("type", checker);
                        messageTextReady.put("from", messageSenderid);
                        messageTextReady.put("to", msgRecId);
                        messageTextReady.put("messageID", messagePushId);
                        messageTextReady.put("time", saveCurrentTime);
                        messageTextReady.put("date", saveCurrentDate);
                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextReady);
                        messageBodyDetails.put(messageRecieverRef + "/" + messagePushId, messageTextReady);
                        RootRef.updateChildren(messageBodyDetails);
                        message.setText("");
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        int progress = (int) ((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (toNotify) {
                            //Toast.makeText(this, "" + recName + recToken, Toast.LENGTH_SHORT).show();
                            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(recToken, "New Message from " + recName, "Sent You a document", getApplicationContext(), ChatActivity.this);
                            notificationsSender.SendNotifications();
                        }
                    }
                });


            } else if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                String messageSenderRef = "Messages/" + messageSenderid + "/" + msgRecId;
                String messageRecieverRef = "Messages/" + msgRecId + "/" + messageSenderid;
                DatabaseReference userMessageRef = RootRef.child("Messages")
                        .child(messageSenderid).child(msgRecId).push();
                String messagePushId = userMessageRef.getKey();
                StorageReference filePath = storageReference.child(messagePushId + ".jpg");
                uploadTask = filePath.putFile(fileuri);
                uploadTask.continueWith(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Map messageTextReady = new HashMap();
                        messageTextReady.put("message", messagePushId);
                        messageTextReady.put("name", fileuri.getLastPathSegment());
                        messageTextReady.put("type", checker);
                        messageTextReady.put("from", messageSenderid);
                        messageTextReady.put("to", msgRecId);
                        messageTextReady.put("messageID", messagePushId);
                        messageTextReady.put("time", saveCurrentTime);
                        messageTextReady.put("date", saveCurrentDate);
                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextReady);
                        messageBodyDetails.put(messageRecieverRef + "/" + messagePushId, messageTextReady);
                        RootRef.updateChildren(messageBodyDetails);
                        message.setText("");
                        progressDialog.dismiss();
                        if (toNotify) {
                            //Toast.makeText(this, "" + recName + recToken, Toast.LENGTH_SHORT).show();
                            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(recToken, "New Message from " + recName, "Sent you an image", getApplicationContext(), ChatActivity.this);
                            notificationsSender.SendNotifications();
                        }
                    }
                });

            } else if (checker.equals("background-image")) {
                //fileuri=data.getData();
                if (fileuri != null) {
                    StorageReference filePath = backgroundRefernce.child(messageSenderid+msgRecId + ".jpg");
                    filePath.putFile(fileuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //backRef.child(messageSenderid).child(msgRecId).child("image").setValue(messageSenderid+msgRecId + ".jpg");
                            progressDialog.dismiss();
                            SetBackground();
                            Toast.makeText(getApplicationContext(), "Background Image posted", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        } else {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "nothing is selected", Toast.LENGTH_SHORT).show();
        }
    }
    private void setBackgroundColor(){
        backRef.child(cUID).child(msgRecId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("color")){
                    String colorCode=snapshot.child("color").getValue().toString();
                    int colorc = Integer.parseInt(colorCode);
                    relativeLayout.setBackgroundColor(colorc);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void chooseBackgroundColor(){
        new ColorPickerPopup.Builder(this).initialColor(
                        Color.RED)
                .enableBrightness(
                        true)
                .enableAlpha(
                        true)
                .okTitle(
                        "Choose")
                .cancelTitle(
                        "Cancel")
                .showIndicator(
                        true)
                .showValue(
                        true)
                .build()
                .show(new ColorPickerPopup.ColorPickerObserver() {
                    @Override
                    public void
                    onColorPicked(int color) {
                        Log.v("Color code:",""+color);
                        mDefaultColor = color;
                        backRef.child(cUID).child(msgRecId).child("color").setValue(mDefaultColor);
                        //relativeLayout.setBackgroundColor(mDefaultColor);
                        setBackgroundColor();
                    }
                });
    }
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gotoFuse();;
            } else {

                // Location permission denied, handle the case when the user denies the permission
                // You may show a message or take appropriate action
            }
        }

    }
    private void getLocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5000);
            // Create a location callback to receive location updates
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        // Send the latitude and longitude as a message to the recipient
                        SendMessageLocation(latitude, longitude);
                    } else {
                        Toast.makeText(getApplicationContext(), "please try again later", Toast.LENGTH_SHORT).show();
                    }

                }
            };
        }
        else{
            Toast.makeText(getApplicationContext(), "please try again later", Toast.LENGTH_SHORT).show();
        }
    }
    private void getLastLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                latitude = locationGPS.getLatitude();
                longitude = locationGPS.getLongitude();
                //Toast.makeText(this, "" + locationGPS, Toast.LENGTH_SHORT).show();

                SendMessageLocation(latitude, longitude);
            } else {
                //gotoFuse();
                getLocation();
            }
        }
    }
    private void gotoFuse() {
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if (resultCode == ConnectionResult.SUCCESS) {
                    mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            if (location == null) {
                                getLastLocation();
                            } else {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();

                                SendMessageLocation(latitude, longitude);
                            }
                        }
                    });
                }else{
                    getLastLocation();
                }
            } else {
                Toast.makeText(this, "please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }

    }
    private void SendMessageLocation(double latitude,double longitude) {
        //String messageText="https://i.diawi.com/Nw5Sv3";
        String messageText="https://www.google.com/maps/search/?api=1&query="+latitude+","+longitude;
        String messageSenderRef = "Messages/" + messageSenderid + "/" + msgRecId;
        String messageRecieverRef = "Messages/" + msgRecId + "/" + messageSenderid;
        DatabaseReference userMessageRef = RootRef.child("Messages")
                .child(messageSenderid).child(msgRecId).push();
        String messagePushId = userMessageRef.getKey();
        Map messageTextReady = new HashMap();
        messageTextReady.put("message", messageText);
        messageTextReady.put("type", "text-link");
        messageTextReady.put("from", messageSenderid);
        messageTextReady.put("to", msgRecId);
        messageTextReady.put("messageID", messagePushId);
        messageTextReady.put("time", saveCurrentTime);
        messageTextReady.put("date", saveCurrentDate);
        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextReady);
        messageBodyDetails.put(messageRecieverRef + "/" + messagePushId, messageTextReady);
        RootRef.updateChildren(messageBodyDetails);
        message.setText("");
        if(toNotify) {
            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(recToken, "New Message from " + recName, "Shared live location", getApplicationContext(), ChatActivity.this);
            notificationsSender.SendNotifications();
        }

    }


}