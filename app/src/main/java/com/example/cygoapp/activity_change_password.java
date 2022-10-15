package com.example.cygoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class activity_change_password extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private EditText txtPass, txtNewPass, txtConfirmPass;
    private TextView lblAuth;
    private Button btnChange, btnBack, btnAuth;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private String userPwd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        txtPass = findViewById(R.id.txtPassword);
        txtNewPass = findViewById(R.id.txtNewPassword);
        txtConfirmPass = findViewById(R.id.txtConfirmPassword);
        lblAuth = findViewById(R.id.lblAuth);
        btnChange =findViewById(R.id.btnChange);
        btnBack = findViewById(R.id.btnBack);
        btnAuth = findViewById(R.id.btnAuth);
        progressBar = findViewById(R.id.progressBar);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity_change_password.this,activity_user_details.class);
                startActivity(intent);
                finish();
            }
        });

        txtNewPass.setEnabled(false);
        txtConfirmPass.setEnabled(false);
        btnChange.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser.equals("")){
            Toast.makeText(activity_change_password.this,"Something went wrong", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(activity_change_password.this,activity_user_details.class);
            startActivity(intent);
            finish();
        }else{
            reAuthenticate(firebaseUser);
        }



    }

    private void reAuthenticate(FirebaseUser firebaseUser) {
        btnAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPwd = txtPass.getText().toString();

                if(TextUtils.isEmpty(userPwd)){
                    Toast.makeText(activity_change_password.this, "Enter Password",Toast.LENGTH_LONG).show();
                    txtPass.setError("Password is Required");
                    txtPass.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                lblAuth.setText("Authenticated");

                                txtNewPass.setEnabled(true);
                                txtConfirmPass.setEnabled(true);
                                btnChange.setEnabled(true);

                                btnAuth.setEnabled(false);
                                txtPass.setEnabled(false);

                                btnChange.setBackgroundTintList(ContextCompat.getColorStateList(activity_change_password.this,R.color.primary_color));

                                Toast.makeText(activity_change_password.this, "Authenticated",Toast.LENGTH_LONG).show();

                                btnChange.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            }else{
                                try{
                                    throw task.getException();
                                }catch(Exception e){
                                    Toast.makeText(activity_change_password.this, e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {

        String newPwd = txtNewPass.getText().toString();
        String confPwd = txtConfirmPass.getText().toString();


        if(TextUtils.isEmpty(newPwd)){
            Toast.makeText(activity_change_password.this, "Enter New Password",Toast.LENGTH_LONG).show();
            txtNewPass.setError("New Password is Required");
            txtNewPass.requestFocus();
        }else if(TextUtils.isEmpty(confPwd)){
            Toast.makeText(activity_change_password.this, "Enter Confirm Password",Toast.LENGTH_LONG).show();
            txtConfirmPass.setError("Confirm Password is Required");
            txtConfirmPass.requestFocus();
        }else if(!newPwd.matches(confPwd)){
            Toast.makeText(activity_change_password.this, "Enter Same Password",Toast.LENGTH_LONG).show();
            txtConfirmPass.setError("Password Did not match");
            txtConfirmPass.requestFocus();
        }else if(newPwd.matches(userPwd)){
            Toast.makeText(activity_change_password.this, "Enter a New Password, (Don't use the old password)",Toast.LENGTH_LONG).show();
            txtNewPass.setError("Same as Old Password");
            txtNewPass.requestFocus();
        }else{
            progressBar.setVisibility(View.VISIBLE);

            firebaseUser.updatePassword(newPwd).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(activity_change_password.this,"Password Changed",Toast.LENGTH_LONG).show();
                        authProfile.signOut();
                        Intent intent = new Intent(activity_change_password.this,activity_sign_in.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }else{
                        try{
                            throw task.getException();
                        }catch(Exception e){
                            Toast.makeText(activity_change_password.this,e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

    }
}