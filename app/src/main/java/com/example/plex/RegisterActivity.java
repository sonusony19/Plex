package com.example.plex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private MaterialEditText name,email,password,cPassword,age;
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private Toolbar toolbar;
    private DotProgressBar progressBar;
    private Button registerbutton;
    private Spinner spinner;
    private ArrayList<String> spinnerContent = getSpinnerContent();

    private ArrayList<String> getSpinnerContent() {
        ArrayList<String> tempList = new ArrayList<>();
        tempList.add("Male");
        tempList.add("Female");
        tempList.add("Other");
        return tempList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        name = findViewById(R.id.nametag);
        email = findViewById(R.id.emailtag);
        password = findViewById(R.id.passwordtag);
        cPassword = findViewById(R.id.cpasswordtag);
        age = findViewById(R.id.agetag);
        spinner = findViewById(R.id.spinnertag);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,spinnerContent);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        progressBar=findViewById(R.id.progressbar);
        auth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.toolbar);
        registerbutton=findViewById(R.id.btn_register);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    public void methodRegister(View view){
        if(validateForm())
        {
            registerbutton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            Bundle userData = new Bundle();
            userData.putString("name",name.getText().toString().trim());
            userData.putString("email",email.getText().toString().trim());
            userData.putString("age",age.getText().toString().trim());
            userData.putString("sex",spinner.getSelectedItem().toString());
            userData.putString("pass",password.getText().toString().trim());
            registerUser(userData);
        }
    }

    private void registerUser(final Bundle userData) {
        String semail = userData.getString("email");
        String spassword = userData.getString("pass");

        auth.createUserWithEmailAndPassword(semail,spassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    final FirebaseUser user = auth.getCurrentUser();
                    assert user != null;
                    String userID = user.getUid();
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);

                    HashMap<String, String> values = new HashMap<>();
                    values.put("id",userID);
                    values.put("userName",userData.getString("name"));
                    values.put("age",userData.getString("age"));
                    values.put("sex",userData.getString("sex"));
                    values.put("imageLink","default");
                    values.put("status","offline");
                    values.put("latitude","0.00000");
                    values.put("longitude","0.00000");

                    reference.setValue(values).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Intent intent =  new Intent(RegisterActivity.this,FillDetails.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

                }else{
                    Toast.makeText(RegisterActivity.this,"You can't register with this email",Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    registerbutton.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private boolean validateForm() {
        boolean state =true;
        String semail = email.getText().toString().trim();
        String sname = name.getText().toString().trim();
        String sage =   age.getText().toString().trim();
        String spass = password.getText().toString().trim();
        String scpass = cPassword.getText().toString().trim();
        if(TextUtils.isEmpty(semail)) { state=false; email.setError("Required");}
        if(TextUtils.isEmpty(sage)) { state=false; age.setError("Required");}
        else{
            if( Integer.parseInt(sage)<15){ state = false; age.setError("Age should be more than 15");}
        }
        if(TextUtils.isEmpty(sname)) { state=false; name.setError("Required");}
        if(TextUtils.isEmpty(spass)) { state=false; password.setError("Required");}
        if(TextUtils.isEmpty(scpass)) { state=false; cPassword.setError("Required");}
        if(!spass.equals(scpass))
        {
            Toast.makeText(RegisterActivity.this, "Passwords Doesn't Match", Toast.LENGTH_SHORT).show();
            cPassword.requestFocus();
            state=false;
        }
        if(spass.length()<6) {
            Toast.makeText(RegisterActivity.this, "Password must be atleast 6 characters", Toast.LENGTH_SHORT).show();
            state=false;}

        return state;
    }
}
