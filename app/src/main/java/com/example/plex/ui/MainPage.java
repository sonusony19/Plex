package com.example.plex.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.plex.R;
import com.example.plex.model.Chat;
import com.example.plex.model.User;
import com.example.plex.view.UserAdapter;

import android.view.View;

import androidx.annotation.NonNull;

import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainPage extends AppCompatActivity {

    private UserAdapter adapter;
    private RecyclerView recyclerView;
    private List<User> mUsers;
    private List<String> userlist;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    private CircleImageView imageView;
    private TextView userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        userName = findViewById(R.id.usernametag);
        imageView = findViewById(R.id.profile_image);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = (User)dataSnapshot.getValue(User.class);
                userName.setText(user.getUserName());
                if(user.getImageLink().equals("default"))
                {
                    imageView.setImageResource(R.drawable.user_icon);
                }else
                {
                    Glide.with(getApplicationContext()).load(user.getImageLink()).into(imageView);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mUsers = new ArrayList<>();
        userlist = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainPage.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter=new UserAdapter(MainPage.this,mUsers,true);
        recyclerView.setAdapter(adapter);

        reference= FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userlist.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    Chat chat=snapshot.getValue(Chat.class);
                    if(chat.getSender().equals(firebaseUser.getUid())) userlist.add(chat.getReceiver());
                    if(chat.getReceiver().equals(firebaseUser.getUid()))userlist.add(chat.getSender());
                }

                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });


    }


    private void showVarificationDialog(int selection) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Intent intent = new Intent(this,LoginActivity.class);
        builder.setTitle("Varify Email");
        if(selection==2)
            builder.setMessage("Email is not varified, Check your inbox of registered email for verification to use this App");
        else if(selection==1)
            builder.setMessage("Email is not Correct, Can't send varification email");
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        builder.setNegativeButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth.getInstance().signOut();
                dialogInterface.dismiss();
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });
        builder.show();
    }


    private void readChats() {

        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    User user = snapshot.getValue(User.class);
                    for(String id:userlist)
                    {
                        if(id.equals(user.getId()))
                        {
                            if(!mUsers.contains(user)) mUsers.add(user);
                        }
                    }
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainpage_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.profilemenu:
                startActivity(new Intent(this, ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.logoutmenu:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                break;
        }
        return true;
    }

    public void showAllUsers(View view) {
        startActivity(new Intent(this, AllUsers.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }


    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!firebaseUser.isEmailVerified()){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"Couldn't send varification email",Toast.LENGTH_LONG).show();
                        showVarificationDialog(1);
                    }
                    else
                    {
                        showVarificationDialog(2);
                    }
                }
            });

        }
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
}
