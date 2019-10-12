package com.example.plex.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.plex.R;
import com.example.plex.fragments.InfoWindowSheet;
import com.example.plex.model.User;
import com.example.plex.util.ImportantMethods;
import com.example.plex.util.LocationClass;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.polyak.iconswitch.IconSwitch;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.xw.repo.BubbleSeekBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.Gravity.RIGHT;

public class AllUsers extends AppCompatActivity implements OnMapReadyCallback, InfoWindowSheet.BottomSheetListener {

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap map;
    private final int DEFAULT_ZOOM = 15;
    private SupportMapFragment mapFragment;
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private float defaultRadius = 5, chosenRadius = -1;
    private List<User> mUsers = readUsers();
    private Dialog dialog;
    private DatabaseReference reference;
    private PopupWindow popupWindow;
    private FloatingActionButton chooseRadius, chooseSports;
    private String game = "Soccer";
    private IconSwitch actionSwitch;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        chooseRadius = findViewById(R.id.chooseRadiusBtn);
        popupWindow = new PopupWindow(this);
        actionSwitch = findViewById(R.id.actionSwitch);
        chooseSports = findViewById(R.id.chooseGameBtn);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(AllUsers.this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.users);
        mapFragment.getMapAsync(this);
        readUsers();


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                game = user.getGame();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.clear();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        LocationClass.getDeviceLocation(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Location myLocation = LocationClass.getDeviceLocation(AllUsers.this);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), DEFAULT_ZOOM));
                for (User user : mUsers) {
                    String otherUserGame = user.getGame();
                    LatLng userPosition = new LatLng(Double.parseDouble(user.getLatitude()), Double.parseDouble(user.getLongitude()));
                    Location otherUser = new Location("OtherUser");
                    otherUser.setLatitude(Double.parseDouble(user.getLatitude()));
                    otherUser.setLongitude(Double.parseDouble(user.getLongitude()));
                    float distance = myLocation.distanceTo(otherUser) / 1000;
                    if (chosenRadius == -1) defaultRadius = 5;
                    else defaultRadius = chosenRadius;
                    if (distance <= defaultRadius && game.equals(otherUserGame)) {
                        map.addMarker(
                                new MarkerOptions().position(userPosition).title(user.getUserName()).icon(ImportantMethods.bitmapDescriptorFromVector(AllUsers.this, R.drawable.ic_account_circle_white_24dp)).snippet("Click here to know more"))
                                .setTag(user);
                    }
                    //map.moveCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 10F));
                }
            }
        }, 3000);



       /* LocationRequest locationRequest = LocationRequest.create();

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
      */

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
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
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    assert firebaseUser != null;
                    if(ImportantMethods.securityCheckPasses(user)){
                        if (!user.getId().equals(firebaseUser.getUid())) {
                            mUsers.add(user);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return tempUserList;
    }


    private void showAlertDialogue(final User user) {

        dialog = new Dialog(this);
        View view = getLayoutInflater().inflate(R.layout.infowindow_layout, null);
        dialog.setContentView(view);
        dialog.show();
        Button cancel, chat, meet;
        CircleImageView userImage;
        TextView userName, userAge, userGame;
        cancel = view.findViewById(R.id.btn_cancel);
        chat = view.findViewById(R.id.btn_chat);
        meet = view.findViewById(R.id.btn_meet);

        userImage = view.findViewById(R.id.profile_image);
        userAge = view.findViewById(R.id.userAgeInfoWIndow);
        userName = view.findViewById(R.id.userNameInfoWIndow);
        userGame = view.findViewById(R.id.userGameInfoWIndow);


        userName.setText(userName.getText().toString() + user.getUserName());
        userAge.setText(userAge.getText().toString() + user.getAge());
        userGame.setText(userGame.getText().toString() + user.getGame());
        if (user.getImageLink().equals("default")) {
            userImage.setImageResource(R.drawable.user_icon);
        } else {
            Glide.with(getApplicationContext()).load(user.getImageLink()).into(userImage);
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        meet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AllUsers.this, MessageActivity.class).putExtra("userid", user.getId()));
            }
        });


       /* InfoWindowSheet windowSheet = new InfoWindowSheet();
        windowSheet.show(getSupportFragmentManager(),"sheet");
        Fragment sheet = (InfoWindowSheet)getSupportFragmentManager().findFragmentByTag("sheet");
        windowSheet.updateWindow(user);*/
    }

    private void status(String status) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
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
    public void onButtonClicked() {

    }

    public void showChooseRadius(View view) {
        if (popupWindow.isShowing()) popupWindow.dismiss();
        else {
            View v = getLayoutInflater().inflate(R.layout.choose_radius_layout, null);
            popupWindow.setContentView(v);
            popupWindow.showAtLocation(chooseRadius, Gravity.START | Gravity.BOTTOM, chooseRadius.getLeft(), chooseRadius.getHeight() + 180);
            final BubbleSeekBar seekBar = v.findViewById(R.id.chooseRadiusSeekBar);
            FloatingActionButton dismissPopup = v.findViewById(R.id.dismissPopup);
            seekBar.setProgress(defaultRadius);
            seekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
                @Override
                public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                    chosenRadius = progressFloat;
                    defaultRadius = progressFloat;
                }

                @Override
                public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                    popupWindow.dismiss();
                }

                @Override
                public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                }
            });

            dismissPopup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupWindow.dismiss();
                }
            });
        }
    }


    public void choseSports(View view) {
        if (popupWindow.isShowing()) popupWindow.dismiss();
        else {

            View v = getLayoutInflater().inflate(R.layout.choose_sports_layout, null);
            popupWindow.setContentView(v);
            popupWindow.showAtLocation(chooseSports, Gravity.BOTTOM | Gravity.LEFT, chooseSports.getRight() - chooseSports.getWidth(), chooseSports.getHeight() + 150);
            final FloatingActionButton badminton, swimming, pool, bowling, soccer;
            badminton = v.findViewById(R.id.batmintonBtn);
            swimming = v.findViewById(R.id.swimmingBtn);
            pool = v.findViewById(R.id.poolBtn);
            bowling = v.findViewById(R.id.bowlingBtn);
            soccer = v.findViewById(R.id.soccerBtn);
            badminton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    game = "Badminton";
                    updateGame(game);
                    chooseSports.setImageResource(R.drawable.ic_badminton_white);
                    popupWindow.dismiss();
                }
            });
            pool.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    game = "Eight Ball Pool";
                    updateGame(game);
                    chooseSports.setImageResource(R.drawable.ic_eight_ball_white);
                    popupWindow.dismiss();
                }
            });
            swimming.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    game = "Swimming";
                    updateGame(game);
                    chooseSports.setImageResource(R.drawable.ic_swimming_silhouette_white);
                    popupWindow.dismiss();
                }
            });

            soccer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    game = "Soccer";
                    updateGame(game);
                    chooseSports.setImageResource(R.drawable.ic_soccer_ball_variant);
                    popupWindow.dismiss();
                }
            });
            bowling.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    game = "Bowling";
                    updateGame(game);
                    chooseSports.setImageResource(R.drawable.ic_soccer_ball_variant);
                    popupWindow.dismiss();
                }
            });
        }

    }

    private void updateGame(String chosenGame) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("game", chosenGame);
        reference.updateChildren(hashMap);
    }


    public void okClicked(View view) {
        //write your code to sear for the places
        // my work is done
        if(popupWindow.isShowing())popupWindow.dismiss();
        onMapReady(map);
    }

}
