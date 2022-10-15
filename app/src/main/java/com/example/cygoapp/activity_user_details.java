package com.example.cygoapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cygoapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class activity_user_details extends AppCompatActivity {

    private ImageView profileImage,editImage;
    private Button btnBack,btnSave,btnChgEmail,btnChgPass;
    private EditText txtName,txtContact;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST=1;
    private Uri uriImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        profileImage = findViewById(R.id.profileImage);
        editImage = findViewById(R.id.editImage);

        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        btnChgEmail = findViewById(R.id.btnChgEmail);
        btnChgPass = findViewById(R.id.btnChgPass);

        txtName = findViewById(R.id.txtName);
        txtContact = findViewById(R.id.txtContact);

        progressBar = findViewById(R.id.progressBar);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("CustomerProPics");

        Uri uri = firebaseUser.getPhotoUrl();

        showProfile(firebaseUser);

        if(uri == null) {
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(activity_user_details.this, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_popup1, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.addImg:
                                    Intent intent = new Intent();
                                    intent.setType("image/**");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        if(uri != null){

            Picasso.get().load(uri).into(profileImage);

            editImage.setVisibility(View.VISIBLE);
            editImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(activity_user_details.this, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.addImg:
                                    Intent intent = new Intent();
                                    intent.setType("image/**");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.show();
                }
            });

        }


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = txtName.getText().toString();
                String contact = txtContact.getText().toString();

                String contactRegex = "[0][0-9]{9}";
                Matcher contactMatcher;
                Pattern contactPattern = Pattern.compile(contactRegex);
                contactMatcher = contactPattern.matcher(contact);

                if (TextUtils.isEmpty(name)){
                    Toast.makeText(activity_user_details.this, "Enter Full Name",Toast.LENGTH_LONG).show();
                    txtName.setError("Full Name is Required");
                    txtName.requestFocus();
                }else if (TextUtils.isEmpty(contact)){
                    Toast.makeText(activity_user_details.this, "Enter Contact",Toast.LENGTH_LONG).show();
                    txtContact.setError("Contact is Required");
                    txtContact.requestFocus();
                }else if (contact.length() != 10){
                    Toast.makeText(activity_user_details.this, "Re-enter Contact",Toast.LENGTH_LONG).show();
                    txtContact.setError("Valid Contact is Required");
                    txtContact.requestFocus();
                }else if (!contactMatcher.find()){
                    Toast.makeText(activity_user_details.this, "Re-enter Contact",Toast.LENGTH_LONG).show();
                    txtContact.setError("Valid Contact is Required");
                    txtContact.requestFocus();
                }else{
                    updateInfo(firebaseUser);
                }


            }
        });

        btnChgEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity_user_details.this,activity_change_email.class);
                startActivity(intent);
            }
        });

        btnChgPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity_user_details.this,activity_change_password.class);
                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity_user_details.this,activity_home.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void updateInfo(FirebaseUser firebaseUser) {

        String name = txtName.getText().toString();
        String contact = txtContact.getText().toString();

        String userId = firebaseUser.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference ref = db.collection("customers").document(userId);

        progressBar.setVisibility(View.VISIBLE);

        ref.update("name", name).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                    ref.update("contact",contact).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            UserProfileChangeRequest updateName = new UserProfileChangeRequest.Builder().setDisplayName(name).build();

                            firebaseUser.updateProfile(updateName);

                            Toast.makeText(activity_user_details.this,"Updated",Toast.LENGTH_LONG).show();

                            progressBar.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(activity_user_details.this,"Error",Toast.LENGTH_LONG).show();

                            progressBar.setVisibility(View.GONE);
                        }
                    });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity_user_details.this,"Error",Toast.LENGTH_LONG).show();

                progressBar.setVisibility(View.GONE);
            }
        });


    }

    private void showProfile(FirebaseUser firebaseUser) {
        String userid = firebaseUser.getUid();


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference ref = db.collection("customers").document(userid);

        progressBar.setVisibility(View.VISIBLE);

        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user =  document.toObject(User.class);

                        if(user != null ){
                            txtName.setText(firebaseUser.getDisplayName());
                            txtContact.setText(user.getContact());
                        }else{
                            Toast.makeText(activity_user_details.this,"Something went wrong",Toast.LENGTH_LONG).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                    progressBar.setVisibility(View.GONE);
                }else{
                    Toast.makeText(activity_user_details.this,"Something went wrong",Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);

                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){

            progressBar.setVisibility(View.VISIBLE);

            uriImage = data.getData();

            InputImage img;

            try {
                img = InputImage.fromFilePath(activity_user_details.this, uriImage);
                ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

                labeler.process(img)
                        .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                            @Override
                            public void onSuccess(List<ImageLabel> labels) {
                                int found=0;

                                for (ImageLabel label : labels) {
                                    String text = label.getText();
                                    float confidence = label.getConfidence();
                                    int index = label.getIndex();

                                    if(text.equals("Smile") && confidence>0.5){
                                        found=1;
                                        uploadPicFirebase(uriImage);
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }

                                if(found==0){
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(activity_user_details.this, "Upload a image of you with clear face and smile", Toast.LENGTH_LONG).show();
                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(activity_user_details.this, "Something went wrong", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(activity_user_details.this, "Error", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void uploadPicFirebase(Uri uriImage) {
        if(uriImage != null){
            StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getUid() + "." + getFileExt(uriImage));

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            DocumentReference ref = db.collection("customers").document(authProfile.getCurrentUser().getUid());

            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            profileImage.setImageURI(uriImage);

                            Uri downloadUri = uri;

                            ref.update("imgUri",downloadUri.toString());
                            ref.update("profileCreated",true);

                            firebaseUser = authProfile.getCurrentUser();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();

                            firebaseUser.updateProfile(profileUpdates);

                            Toast.makeText(activity_user_details.this, "Upload Successful", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(activity_user_details.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }else{
            Toast.makeText(activity_user_details.this, "No File Selected", Toast.LENGTH_LONG).show();
        }
    }

    private String getFileExt(Uri uriImage) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uriImage));
    }
}