package com.example.cygoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class activity_change_email extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView lblAuth,lblEmail;
    private String oldEmail,newEmail , userPwd;
    private Button btnChange, btnBack, btnAuth;
    private EditText txtPass, txtNewEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        progressBar = findViewById(R.id.progressBar);
        lblAuth = findViewById(R.id.lblAuth);
        lblEmail = findViewById(R.id.lblEmail);
        btnChange = findViewById(R.id.btnChange);
        btnBack = findViewById(R.id.btnBack);
        btnAuth = findViewById(R.id.btnAuth);
        txtPass = findViewById(R.id.txtPassword);
        txtNewEmail = findViewById(R.id.txtNewEmail);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity_change_email.this,activity_user_details.class);
                startActivity(intent);
                finish();
            }
        });

        txtNewEmail.setEnabled(false);
        btnChange.setEnabled(false);

        authProfile=FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        oldEmail = firebaseUser.getEmail();
        lblEmail.setText(oldEmail);

        if(firebaseUser.equals("")){
            Toast.makeText(activity_change_email.this,"Something went wrong", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(activity_change_email.this,activity_user_details.class);
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
                    Toast.makeText(activity_change_email.this, "Enter Password",Toast.LENGTH_LONG).show();
                    txtPass.setError("Password is Required");
                    txtPass.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential authCredential = EmailAuthProvider.getCredential(oldEmail,userPwd);

                    firebaseUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(activity_change_email.this, "Authenticated",Toast.LENGTH_LONG).show();

                                lblAuth.setText("Authenticated");
                                txtNewEmail.setEnabled(true);
                                btnChange.setEnabled(true);
                                txtPass.setEnabled(false);
                                btnAuth.setEnabled(false);

                                btnChange.setBackgroundTintList(ContextCompat.getColorStateList(activity_change_email.this,R.color.primary_color));

                                btnChange.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        newEmail = txtNewEmail.getText().toString();

                                        if(TextUtils.isEmpty(newEmail)){
                                            Toast.makeText(activity_change_email.this, "Enter Email",Toast.LENGTH_LONG).show();
                                            txtNewEmail.setError("Email is Required");
                                            txtNewEmail.requestFocus();
                                        }else if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()){
                                            Toast.makeText(activity_change_email.this, "Re-enter Email",Toast.LENGTH_LONG).show();
                                            txtNewEmail.setError("Valid Email is Required");
                                            txtNewEmail.requestFocus();
                                        }else{
                                            progressBar.setVisibility(View.VISIBLE);

                                            updateEmail(firebaseUser);
                                        }
                                    }
                                });
                            }else{

                                try{
                                    throw task.getException();
                                }catch(Exception e){
                                    Toast.makeText(activity_change_email.this, e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {

        firebaseUser.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete()){

                    String uid = firebaseUser.getUid();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    DocumentReference ref = db.collection("customers").document(uid);

                    ref.update("email",newEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            Toast.makeText(activity_change_email.this, "Email Updated. Please Verify", Toast.LENGTH_LONG).show();

                            firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    authProfile.signOut();

                                    Intent intent = new Intent(activity_change_email.this, activity_sign_in.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(activity_change_email.this, "Error", Toast.LENGTH_LONG).show();
                        }
                    });

                    progressBar.setVisibility(View.GONE);

                }else{
                    try{
                        throw task.getException();
                    }catch(Exception e){
                        Toast.makeText(activity_change_email.this, e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }
}