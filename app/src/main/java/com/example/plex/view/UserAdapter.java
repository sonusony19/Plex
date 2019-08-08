package com.example.plex.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plex.ui.MainPage;
import com.example.plex.ui.MessageActivity;
import com.example.plex.R;
import com.example.plex.model.Chat;
import com.example.plex.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    String theLastMsg;
    private boolean isChat;

    MainPage mainActivity = new MainPage();

    public UserAdapter(Context mContext, List<User> mUsers, boolean isChat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemview = LayoutInflater.from(mContext).inflate(R.layout.useritems_layout,viewGroup,false);
        MyViewHolder myViewHolder = new MyViewHolder(itemview);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        final User user = mUsers.get(i);

        myViewHolder.textView.setText(user.getUserName());

        if(user.getImageLink().equals("default"))
        {
            myViewHolder.imageView.setImageResource(R.drawable.user_icon);
        }else
        {
            Glide.with(mContext).load(user.getImageLink()).into(myViewHolder.imageView);
        }

        if(isChat&& (FirebaseAuth.getInstance().getCurrentUser()!=null)){
            setTheLastMsg(user.getId(),myViewHolder.last_msg);
        }else {
            myViewHolder.last_msg.setVisibility(View.GONE);
        }
        if(isChat){
            if(user.getStatus().equals("online")){
                myViewHolder.img_on.setVisibility(View.VISIBLE);
                myViewHolder.img_off.setVisibility(View.GONE);
            }
            else {myViewHolder.img_on.setVisibility(View.GONE);
                myViewHolder.img_off.setVisibility(View.VISIBLE);
            }
        }else {
            myViewHolder.img_on.setVisibility(View.GONE);
            myViewHolder.img_off.setVisibility(View.GONE);
        }

        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid",user.getId());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }


         public class MyViewHolder extends RecyclerView.ViewHolder {
         public CircleImageView imageView;
         public TextView textView;
         private CircleImageView img_on,img_off;
         private TextView last_msg;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (CircleImageView)itemView.findViewById(R.id.profile_image);
            textView = (TextView)itemView.findViewById(R.id.username);
            last_msg = (TextView)itemView.findViewById(R.id.last_msg);
            img_on = (CircleImageView)itemView.findViewById(R.id.img_on);
            img_off = (CircleImageView)itemView.findViewById(R.id.img_off);
        }
    }

    private void setTheLastMsg(final String userId, final TextView last_msg)
    {
        theLastMsg = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(firebaseUser.getUid()) &&chat.getSender().equals(userId)||
                            chat.getReceiver().equals(userId)&&chat.getSender().equals(firebaseUser.getUid())){
                        theLastMsg = chat.getMessage();
                    }
                }

                switch (theLastMsg)
                {
                    case "default":
                        last_msg.setText("No message");
                        break;
                        default:
                            last_msg.setText(theLastMsg);
                            break;
                }
                theLastMsg = "default";

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
