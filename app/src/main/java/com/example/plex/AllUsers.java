package com.example.plex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.plex.model.User;
import com.example.plex.util.LocationClass;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsers extends AppCompatActivity implements OnMapReadyCallback {

    private Toolbar toolbar;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap map;
    private final int DEFAULT_ZOOM=1;
    private SupportMapFragment mapFragment;
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private int radius=5;
    private List<User> mUsers = readUsers();
    private Dialog dialog;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(AllUsers.this);
        getSupportActionBar().setTitle("Users In Your Area");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.users);
        mapFragment.getMapAsync(this);
        readUsers();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        LocationClass.getDeviceLocation(this);
       new Handler().postDelayed(new Runnable() {
           @Override
           public void run() {
               Location tempLocation = LocationClass.getDeviceLocation(AllUsers.this);
               map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(tempLocation.getLatitude(),tempLocation.getLongitude()), DEFAULT_ZOOM));
           }
       },4000);

        for(User user:mUsers){

            LatLng userPosition = new LatLng(Double.parseDouble(user.getLatitude()),Double.parseDouble(user.getLongitude()));
            map.addMarker(new MarkerOptions().position(userPosition).title(user.getUserName()).snippet("Click here to know more")).setTag(user);
            //map.moveCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 10F));
        }


        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(AllUsers.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(AllUsers.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(AllUsers.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(AllUsers.this, 51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker){
                User user = (User) marker.getTag();
                showAlertDialogue(user);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {

        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        mLastKnownLocation = locationResult.getLastLocation();
                                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        } else {
                            Toast.makeText(AllUsers.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }



    private List<User> readUsers() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        List<User> tempUserList = new ArrayList<>();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    User user = snapshot.getValue(User.class);
                    assert user!=null;
                    assert firebaseUser!=null;
                    if(!user.getId().equals(firebaseUser.getUid()))
                    {
                        mUsers.add(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return tempUserList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choose_radius,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void showAlertDialogue(final User user) {

         dialog= new Dialog(this);
         View view =getLayoutInflater().inflate(R.layout.infowindow_layout,null);
         dialog.setContentView(view);
         dialog.show();
         Button cancel,chat,meet;
         CircleImageView userImage;
         TextView userName,userAge;
         cancel = view.findViewById(R.id.btn_cancel);
         chat = view.findViewById(R.id.btn_chat);
         meet = view.findViewById(R.id.btn_meet);
         userImage = view.findViewById(R.id.profile_image);
         userAge = view.findViewById(R.id.userAgeInfoWIndow);
         userName = view.findViewById(R.id.userNameInfoWIndow);


        userName.setText(userName.getText().toString()+user.getUserName());
        userAge.setText(userAge.getText().toString()+user.getAge());
        if(user.getImageLink().equals("default"))
        {
            userImage.setImageResource(R.drawable.user_icon);
        }else
        {
            Glide.with(getApplicationContext()).load(user.getImageLink()).into(userImage);
        }

        cancel.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) { dialog.dismiss(); }});
        meet.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) { }});
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AllUsers.this,MessageActivity.class).putExtra("userid",user.getId())); }});
    }

    private void status(String status){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }

    @Override
    protected void onDestroy() {
        if(dialog.isShowing())dialog.dismiss();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(dialog.isShowing())dialog.dismiss();
        super.onBackPressed();
    }
}
