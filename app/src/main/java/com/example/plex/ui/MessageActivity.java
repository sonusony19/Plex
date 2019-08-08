package com.example.plex.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.plex.R;
import com.example.plex.model.Chat;
import com.example.plex.model.User;
import com.example.plex.view.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private CircleImageView profile_image;
    private TextView username;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private Toolbar toolbar;
    private Intent intent;
    private EditText msg;
    private String userid;

    private MessageAdapter messageAdapter;
    private List<Chat> mChat;
    private RecyclerView recyclerView;

    ValueEventListener seenListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        toolbar = findViewById(R.id.toolbar);
        profile_image = findViewById(R.id.profile_image);
        recyclerView=findViewById(R.id.recycler_view);
        msg = findViewById(R.id.text_send);
        username =findViewById(R.id.usernametag);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessageActivity.this, MainPage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        intent = getIntent();
        userid = intent.getStringExtra("userid");
      /*  username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageActivity.this,ProfileActivity.class);
                intent.putExtra("user",userid);
                startActivity(intent);
            }
        });*/

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUserName());
                if(user.getImageLink().equals("default"))
                {
                    profile_image.setImageResource(R.drawable.user_icon);
                }else
                {
                    Glide.with(getApplicationContext()).load(user.getImageLink()).into(profile_image);
                }
                readMessages(firebaseUser.getUid(),userid,user.getImageLink());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        setSeenListener(userid);

    }

    public void methodSend(View view) {
        String s1 = msg.getText().toString().trim();
        if(!TextUtils.isEmpty(s1))
        {
            SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
            String time = format.format(new Date());
            sendMessage(firebaseUser.getUid(),userid,s1,time);
            msg.setText("");
        }
    }

    private void setSeenListener(final String userid)
    {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void sendMessage(String senderid, String receiverid, String message,String time) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",senderid);
        hashMap.put("receiver",receiverid);
        hashMap.put("message",message);
        hashMap.put("isseen",false);
        hashMap.put("time",time);
        databaseReference.child("Chats").push().setValue(hashMap);
    }

    private void readMessages(final String myid, final String uid, final String imageurl)
    {
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getSender().equals(myid) && chat.getReceiver().equals(uid)|| chat.getReceiver().equals(myid)&& chat.getSender().equals(uid))
                    {
                        mChat.add(chat);
                    }
                }
                messageAdapter = new MessageAdapter(MessageActivity.this,mChat,imageurl);
                recyclerView.setAdapter(messageAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        reference.removeEventListener(seenListener);
        status("offline");
    }
}
