package com.example.plex.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.plex.R;
import com.example.plex.constants.IntentRequestCodes;
import com.example.plex.model.User;
import com.example.plex.navigators.FillDetailsNavigator;
import com.example.plex.util.ImportantMethods;
import com.example.plex.util.LocationClass;
import com.example.plex.viewModel.FillDetailsViewModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements FillDetailsNavigator {

    private Toolbar toolbar;
    private String imageUri,cloudUri;
    private FillDetailsViewModel viewModel;
    private boolean flag = false;
    private TextView name,email,age,game;
    private CircleImageView circleImageView;
    private StorageTask uploadTask;
    private DatabaseReference reference;
    private StorageReference storageReference;
    private ProgressDialog dialog;
    private FirebaseUser fUser;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.toolbar);
        name = findViewById(R.id.username);
        age = findViewById(R.id.Age);
        email = findViewById(R.id.useremail);
        game = findViewById(R.id.game);
        circleImageView = findViewById(R.id.profile_image);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewModel = ViewModelProviders.of(this).get(FillDetailsViewModel.class);
        viewModel.setNavigator(this);
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        fUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent =getIntent();
        String UID=intent.getStringExtra("user");

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mUser = user;
                if(ImportantMethods.securityCheckPasses(mUser)){
                    name.setText(name.getText().toString()+user.getUserName());
                    age.setText(age.getText().toString()+user.getAge());
                    email.setText(fUser.getEmail());
                    game.setText(game.getText().toString()+user.getGame());
                    if(user.getImageLink().equals("default"))
                    {
                        imageUri = "default";
                        circleImageView.setImageResource(R.drawable.user_icon);
                    }else
                    {
                        Glide.with(getApplicationContext()).load(user.getImageLink()).into(circleImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void cameraButtonClicked(View view) {
        requestMultiplePermissions();
        viewModel.openViewModelCamera();

    }

    public void galaryButtonClicked(View view) {
        requestMultiplePermissions();
        viewModel.openViewModelGalary();
    }

    @Override
    public void openGalary() {
        startActivityForResult(Intent.createChooser(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*"), "Pic an Image"), IntentRequestCodes.PICK_PICTURE_ACTIVITY_REQUEST);
    }

    @Override
    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, IntentRequestCodes.CAPTURE_PICTURE_ACTIVITY_REQUEST);
        }
    }


    public void uploadImage() {
        final ProgressDialog progressDialog= new ProgressDialog(ProfileActivity.this);
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if(imageUri!=null)
        {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()+"."+ ImportantMethods.getFileExtension(ProfileActivity.this, Uri.parse(imageUri)));
            uploadTask = fileReference.putFile(Uri.parse(imageUri));
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful())
                    {
                        throw  task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                    {
                        cloudUri = task.getResult().toString();
                        progressDialog.dismiss();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }
        else
        {
            Toast.makeText(getApplicationContext(),"No image selected",Toast.LENGTH_SHORT).show();
        }

    }

    private void requestMultiplePermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            flag = true;
                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            flag = false;
                            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                            builder.setTitle("Permission Denied")
                                    .setMessage(" Some Permissions are permanently denied. you need to go to setting to allow the permissions.")
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                                            startActivity(intent);
                                        }
                                    })
                                    .show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case IntentRequestCodes.CAPTURE_PICTURE_ACTIVITY_REQUEST:
                if(resultCode==RESULT_OK)
                {
                    Bitmap tempBmp = (Bitmap)data.getExtras().get("data");
                    circleImageView.setImageBitmap(tempBmp);
                    imageUri= ImportantMethods.getImageUri(getApplication().getApplicationContext(),tempBmp).toString();
                    if(uploadTask!=null && uploadTask.isInProgress())
                    {
                        Toast.makeText(ProfileActivity.this,"Upload in Progress",Toast.LENGTH_LONG).show();
                    }
                    else {
                        uploadImage();
                    }
                }
                break;

            case IntentRequestCodes.PICK_PICTURE_ACTIVITY_REQUEST:
                if(resultCode==RESULT_OK)
                {
                    Uri tempUri = data.getData();
                    circleImageView.setImageURI(tempUri);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        getApplication().getContentResolver().takePersistableUriPermission(tempUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                    imageUri = tempUri.toString();
                    if(uploadTask!=null && uploadTask.isInProgress())
                    {
                        Toast.makeText(ProfileActivity.this,"Upload in Progress",Toast.LENGTH_LONG).show();
                    }
                    else uploadImage();
                }
                break;
        }

    }

    public void saveBtnClicked(View view) {
        updateUser(mUser);
        startActivity(new Intent(ProfileActivity.this, MainPage.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        updateUser(mUser);
        startActivity(new Intent(ProfileActivity.this,MainPage.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
        return true;
    }

    private void updateUser(final User user) {
        LocationClass.getDeviceLocation(ProfileActivity.this);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Saving");
        dialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Location mLocation = LocationClass.getDeviceLocation(ProfileActivity.this);
                reference = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
                HashMap<String, Object> map = new HashMap<>();
                if(cloudUri==null){ map.put("imageLink",user.getImageLink()); }
                else{ map.put("imageLink",cloudUri); }
                map.put("latitude",String.valueOf(mLocation.getLatitude()));
                map.put("longitude",String.valueOf(mLocation.getLongitude()));
                reference.updateChildren(map);
                dialog.dismiss();
            }
        },4000);

    }

    @Override
    protected void onDestroy() {
        if(dialog!=null && dialog.isShowing())dialog.dismiss();

        super.onDestroy();
    }

}
