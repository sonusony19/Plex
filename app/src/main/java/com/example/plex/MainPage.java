package com.example.plex;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.example.plex.model.Chat;
import com.example.plex.model.User;
import com.example.plex.util.LocationClass;
import com.example.plex.view.UserAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private UserAdapter adapter;
    private RecyclerView recyclerView;
    private List<User> mUsers;
    private List<String> userlist;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Plex");
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

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
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void showAllUsers(View view) {
        startActivity(new Intent(this,AllUsers.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setCheckable(false);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        switch (item.getItemId()){
            case R.id.profilemenu:
                startActivity(new Intent(this,ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.logoutmenu:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this,LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                break;
        }
        return true;
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
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }

    @Override
    public boolean isDestroyed() {
        status("offline");
        return super.isDestroyed();
    }
}
