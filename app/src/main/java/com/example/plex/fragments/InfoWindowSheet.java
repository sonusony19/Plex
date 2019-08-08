package com.example.plex.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.plex.R;
import com.example.plex.model.User;
import com.example.plex.ui.AllUsers;
import com.example.plex.ui.MessageActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import de.hdodenhof.circleimageview.CircleImageView;

public class InfoWindowSheet extends BottomSheetDialogFragment {

    private BottomSheetListener mListener;
    Button cancel,chat,meet;
    CircleImageView userImage;
    TextView userName,userAge;
    private User mUser;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.infowindow_bottom_layout,container,false);


        cancel = view.findViewById(R.id.btn_cancel);
        chat = view.findViewById(R.id.btn_chat);
       // meet = view.findViewById(R.id.btn_meet);
        userImage = view.findViewById(R.id.profile_image);
        userAge = view.findViewById(R.id.userAgeInfoWIndow);
        userName = view.findViewById(R.id.userNameInfoWIndow);




        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                startActivity(new Intent(getActivity().getApplicationContext(), MessageActivity.class).putExtra("userid",mUser.getId()));
            }
        });
        return view;
    }

    public interface BottomSheetListener {
        void onButtonClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BottomSheetListener");
        }
    }

    public void updateWindow(User user){
        mUser=user;
        userName.setText(userName.getText().toString()+user.getUserName());
        userAge.setText(userAge.getText().toString()+user.getAge());
        if(user.getImageLink().equals("default"))
        {
            userImage.setImageResource(R.drawable.user_icon);
        }else
        {
            Glide.with(getActivity().getApplicationContext()).load(user.getImageLink()).into(userImage);
        }
    }


}
