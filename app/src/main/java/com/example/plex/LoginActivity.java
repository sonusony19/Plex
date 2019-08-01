package com.example.plex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActivity extends AppCompatActivity {

    private MaterialEditText email,password;
    private CheckBox checkBox;
    private Toolbar toolbar;
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private FirebaseUser user;
    private final String credentials="cred";
    DotProgressBar progressBar;
    Button button_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.emailtag);
        password=findViewById(R.id.passwordtag);
        checkBox = findViewById(R.id.checkbox);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Plex");
        progressBar=findViewById(R.id.progressbar);
        button_login = findViewById(R.id.btn_login);
        progressBar.setVisibility(View.GONE);
    }

    public void forgotPasswordClicked(View view) {
        startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
    }

    public void methodRegisterActivity(View view) {
        startActivity(new Intent(LoginActivity.this,RegisterActivity.class));

    }

    public void methodLogin(View view) {
        if(validateForm())
        {
            button_login.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            methodSignin(email.getText().toString().trim(),password.getText().toString().trim());
        }
    }

    private boolean validateForm() {
        boolean state = true;
        String s1 = email.getText().toString().trim();
        String s2 = password.getText().toString().trim();
        if(TextUtils.isEmpty(s1)){ email.setError("Required");state=false;}
        if(TextUtils.isEmpty(s2)){ password.setError("Required");state=false;}
        return state;
    }

    @Override
    protected void onStop() {
        if(validateForm() && checkBox.isChecked())
        {
            SharedPreferences sharedPreferences  = getSharedPreferences(credentials, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email",email.getText().toString().trim());
            editor.commit();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(credentials,Context.MODE_PRIVATE);
        String s1 =sharedPreferences.getString("email","");
        email.setText(s1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();
        user = auth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, MainPage.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }
    }

    private void methodSignin(String semail, String spassword) {

        auth.signInWithEmailAndPassword(semail,spassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Intent intent = new Intent(LoginActivity.this,MainPage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(LoginActivity.this,"Invalid Email or Password",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    button_login.setVisibility(View.VISIBLE);
                }

            }
        });
    }
}
