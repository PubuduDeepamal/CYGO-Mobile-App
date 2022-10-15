package com.example.cygoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.CharacterPickerDialog;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class activity_sign_in extends AppCompatActivity {

    private Button btnSignIn,btnsignUp,btnForgotPass;
    private EditText txtEmail,txtPassword;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static final String TAG="activity_sign_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_in);

        btnSignIn=findViewById(R.id.btnSignIn);
        btnForgotPass=findViewById(R.id.btnForgotPass);

        txtEmail=findViewById(R.id.txtEmail);
        txtPassword=findViewById(R.id.txtPassword);
        progressBar=findViewById(R.id.progressBar);

        btnsignUp = findViewById(R.id.btnsignUp);


        authProfile = FirebaseAuth.getInstance();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = txtEmail.getText().toString();
                String pwd = txtPassword.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(activity_sign_in.this, "Enter Email",Toast.LENGTH_LONG).show();
                    txtEmail.setError("Email is Required");
                    txtEmail.requestFocus();
                }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(activity_sign_in.this, "Re-enter Email",Toast.LENGTH_LONG).show();
                    txtEmail.setError("Valid Email is Required");
                    txtEmail.requestFocus();
                }else if(TextUtils.isEmpty(pwd)){
                    Toast.makeText(activity_sign_in.this, "Enter Password",Toast.LENGTH_LONG).show();
                    txtPassword.setError("Password is Required");
                    txtPassword.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(email,pwd);
                }
            }
        });

        btnsignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity_sign_in.this, activity_sign_up.class);
                startActivity(intent);
            }
        });

        btnForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = txtEmail.getText().toString();
                String pwd = txtPassword.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(activity_sign_in.this, "Enter Email to reset",Toast.LENGTH_LONG).show();
                    txtEmail.setError("Email is Required");
                    txtEmail.requestFocus();
                }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(activity_sign_in.this, "Re-enter Email", Toast.LENGTH_LONG).show();
                    txtEmail.setError("Valid Email is Required");
                    txtEmail.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    resetPwd(email);
                }
            }
        });
    }

    private void resetPwd(String email) {
        authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(activity_sign_in.this, "Reset Password Link Sent!", Toast.LENGTH_LONG).show();
                }else{
                    try{
                        throw task.getException();
                    }catch(FirebaseAuthInvalidUserException e){
                        txtEmail.setError("User doesn't exist");
                    }catch(Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(activity_sign_in.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void loginUser(String email, String pwd) {
        authProfile.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    if(firebaseUser.isEmailVerified()){
                        Toast.makeText(activity_sign_in.this,"Signed In",Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(activity_sign_in.this, activity_home.class));
                        finish();

                    }else{

                        firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                authProfile.signOut();
                                showAlertDialog();
                            }
                        });

                    }

                }else{
                    try{
                        throw task.getException();
                    }catch(FirebaseAuthInvalidUserException e){
                        txtEmail.setError("User does not exist or no longer available");
                        txtEmail.requestFocus();
                    }catch(FirebaseAuthInvalidCredentialsException e){
                        txtEmail.setError("Invalid Credentials");
                        txtEmail.requestFocus();
                    }catch(Exception e){
                        Log.e(TAG,e.getMessage());
                        Toast.makeText(activity_sign_in.this, e.getMessage(),Toast.LENGTH_LONG).show();
                    }

                    Toast.makeText(activity_sign_in.this, "Something went wrong",Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity_sign_in.this);
        builder.setTitle("Email not verified");
        builder.setMessage("Please verify your email now. You can not login without verifying");

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(authProfile.getCurrentUser() != null){
            startActivity(new Intent(activity_sign_in.this, activity_home.class));
            finish();
        }else{

        }
    }
}