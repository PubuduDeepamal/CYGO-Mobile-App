package com.example.cygoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.cygoapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class activity_sign_up extends AppCompatActivity {

    private EditText txtName, txtContact,txtEmail,txtPassword, txtConfirmPassword,txtNIC;
    private Button btnRegister,btnSignIn;
    private ProgressBar progressBar;
    private CheckBox chkterms;
    private static final String TAG = "activity_sign_up";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);

        txtName=findViewById(R.id.txtName);
        txtContact=findViewById(R.id.txtContact);
        txtEmail=findViewById(R.id.txtEmail);
        txtPassword=findViewById(R.id.txtPassword);
        txtConfirmPassword=findViewById(R.id.txtConfirmPassword);
        txtNIC=findViewById(R.id.txtNIC);
        chkterms=findViewById(R.id.chkterms);

        progressBar=findViewById(R.id.progressBar);

        btnRegister = findViewById(R.id.btnRegister);
        btnSignIn = findViewById(R.id.btnSignIn);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = txtName.getText().toString();
                String contact = txtContact.getText().toString();
                String email = txtEmail.getText().toString();
                String pwd = txtPassword.getText().toString();
                String cpwd = txtConfirmPassword.getText().toString();
                String nic = txtNIC.getText().toString();

                String contactRegex = "[0][0-9]{9}";
                Matcher contactMatcher;
                Pattern contactPattern = Pattern.compile(contactRegex);
                contactMatcher = contactPattern.matcher(contact);

                if (TextUtils.isEmpty(name)){
                    Toast.makeText(activity_sign_up.this, "Enter Full Name",Toast.LENGTH_LONG).show();
                    txtName.setError("Full Name is Required");
                    txtName.requestFocus();
                }else if (TextUtils.isEmpty(contact)){
                    Toast.makeText(activity_sign_up.this, "Enter Contact",Toast.LENGTH_LONG).show();
                    txtContact.setError("Contact is Required");
                    txtContact.requestFocus();
                }else if (contact.length() != 10){
                    Toast.makeText(activity_sign_up.this, "Re-enter Contact",Toast.LENGTH_LONG).show();
                    txtContact.setError("Valid Contact is Required");
                    txtContact.requestFocus();
                }else if (!contactMatcher.find()){
                    Toast.makeText(activity_sign_up.this, "Re-enter Contact",Toast.LENGTH_LONG).show();
                    txtContact.setError("Valid Contact is Required");
                    txtContact.requestFocus();
                }else if(TextUtils.isEmpty(email)){
                    Toast.makeText(activity_sign_up.this, "Enter Email",Toast.LENGTH_LONG).show();
                    txtEmail.setError("Email is Required");
                    txtEmail.requestFocus();
                }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(activity_sign_up.this, "Re-enter Email",Toast.LENGTH_LONG).show();
                    txtEmail.setError("Valid Email is Required");
                    txtEmail.requestFocus();
                }else if (TextUtils.isEmpty(nic)){
                    Toast.makeText(activity_sign_up.this, "Enter NIC",Toast.LENGTH_LONG).show();
                    txtNIC.setError("NIC is Required");
                    txtNIC.requestFocus();
                }else if (nic.length() < 10 && nic.length() > 11){
                    Toast.makeText(activity_sign_up.this, "Re-enter NIC",Toast.LENGTH_LONG).show();
                    txtNIC.setError("Valid NIC is Required");
                    txtNIC.requestFocus();
                }else if(TextUtils.isEmpty(pwd)){
                    Toast.makeText(activity_sign_up.this, "Enter Password",Toast.LENGTH_LONG).show();
                    txtPassword.setError("Password is Required");
                    txtPassword.requestFocus();
                }else if(pwd.length() < 4){
                    Toast.makeText(activity_sign_up.this, "Password should be at least 4 characters",Toast.LENGTH_LONG).show();
                    txtPassword.setError("Password too weak");
                    txtPassword.requestFocus();
                }else if(TextUtils.isEmpty(cpwd)){
                    Toast.makeText(activity_sign_up.this, "Enter Confirm Password",Toast.LENGTH_LONG).show();
                    txtConfirmPassword.setError("Confirm Password is Required");
                    txtConfirmPassword.requestFocus();
                }else if(!pwd.equals(cpwd)){
                    Toast.makeText(activity_sign_up.this, "Enter Same Password",Toast.LENGTH_LONG).show();
                    txtConfirmPassword.setError("Same Password is Required");
                    txtConfirmPassword.requestFocus();
                }else if(!chkterms.isChecked()){
                    Toast.makeText(activity_sign_up.this, "Accept Terms and conditions",Toast.LENGTH_LONG).show();
                    chkterms.setError("Accept Terms and conditions");
                    chkterms.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(name,contact,email,pwd,nic);
                }

            }
        });


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity_sign_up.this, activity_sign_in.class);
                startActivity(intent);
            }
        });
    }

    //Register User
    private void registerUser(String name, String contact, String email, String pwd, String nic) {

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email,pwd).addOnCompleteListener(activity_sign_up.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                    firebaseUser.updateProfile(profileChangeRequest);

                    User customer = new User(firebaseUser.getUid(),name,contact,email,nic,"null",null,false,0,0);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("customers").document(firebaseUser.getUid()).set(customer)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    firebaseUser.sendEmailVerification();

                                    progressBar.setVisibility(View.GONE);

                                    Toast.makeText(activity_sign_up.this, "User Registered. Please Verify your email",Toast.LENGTH_LONG).show();

                                    auth.signOut();

                                    //Open User Profile after successful registration
                                    Intent intent = new Intent(activity_sign_up.this,activity_sign_in.class);
                                    //to prevent coming back to sign up
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                                    startActivity(intent);
                                    finish();
                                    progressBar.setVisibility(View.GONE);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(activity_sign_up.this, "User Registration Failed. Please Try Again",Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);

                                }
                            });


                }else{
                    try{
                        throw task.getException();
                    }catch(FirebaseAuthWeakPasswordException e){
                        txtPassword.setError("Password too weak");
                        txtPassword.requestFocus();

                    }catch(FirebaseAuthInvalidCredentialsException e){
                        txtEmail.setError("Email is invalid or already in use");
                        txtEmail.requestFocus();

                    }catch(FirebaseAuthUserCollisionException e){
                        txtEmail.setError("Email is already in use");
                        txtEmail.requestFocus();

                    }catch(Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(activity_sign_up.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }



}