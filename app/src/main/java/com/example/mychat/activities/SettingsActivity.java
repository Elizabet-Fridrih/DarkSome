package com.example.mychat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mychat.R;
import com.example.mychat.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {
    private String userName, userEmail, userPassword, userLogin;

    private static final String TAG_NAME = "Name", TAG_EMAIL = "Email", TAG_PASSWORD = "Password",
    TAG_LOGIN = "Login";


    private DatabaseReference refName, refEmail, refUsers, refAvatar, refContacts;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private ChildEventListener usersEventListener;
    private FirebaseStorage storage;
    private StorageReference avatarImagesReference;
    private ArrayList<User> users;
    private TextView usernameTextV, currentEmailTextV, loginTV;
    private  String keyUser, idUser;
    private ImageView avatarIv;
    private Uri avatarUri;
    private ProgressBar pb;

    private static final int CAMERA_CODE = 100;
    private static final int GALLERY_CODE = 101;
    private static final int STORAGE_REQUEST_CODE = 102;
    private static final int CAMERA_REQUEST_CODE = 103;


    private String cameraPermissions[];
    private String storagePermissions[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        usernameTextV = findViewById(R.id.usernameTV);
        loginTV = findViewById(R.id.loginTV);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        idUser = currentUser.getUid();
        storage = FirebaseStorage.getInstance();

        avatarImagesReference = storage.getReference().child("avatar_images");

        avatarIv = findViewById(R.id.imageAvatar);
        userEmail = currentUser.getEmail();
        currentEmailTextV = findViewById(R.id.currentEmail);
        currentEmailTextV.setText("Your current email: "+ userEmail);

        users = new ArrayList<User>();

        refUsers = database.getReference().child("users");
        refContacts = database.getReference().child("userContacts");
        checkOnlineStatus("online");
        Query query = refUsers.orderByChild("email").equalTo(userEmail);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){

                    try{
                        if(!ds.child("avatarMockUpResource").getValue().toString().equals(" ")){
                            Uri image = Uri.parse(ds.child("avatarMockUpResource").getValue().toString());
                            if(image.toString().contains("%2Fstorage"))
                                Picasso.get().load(image).rotate(270).into(avatarIv);
                            else
                                Picasso.get().load(image).rotate(90).into(avatarIv);
                        }

                    }catch (Exception e){
                        Picasso.get().load(R.drawable.user_image).into(avatarIv);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        setTitle("Settings");
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        attachUserListener();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


    public void changeName(View view) {
        createDialog(TAG_NAME);

    }

    public void changeEmail(View view) {
        createDialog(TAG_EMAIL);
    }

    public void changePassword(View view) {
        createDialog(TAG_PASSWORD);
    }

    public void createDialog(String hint){
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.layout_change, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(SettingsActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText changeEditText = view.findViewById(R.id.changeEditText);
        final EditText confirmEditText = view.findViewById(R.id.confirmEditText);
        final EditText oldEditText = view.findViewById(R.id.oldEditText);
        final TextView changeTitle = view.findViewById(R.id.changeTitle);
        changeTitle.setText("New "+hint);
        changeEditText.setHint(hint);

        if(hint.equals(TAG_NAME)||hint.equals(TAG_LOGIN))
        {
            confirmEditText.setVisibility(View.GONE);
            oldEditText.setVisibility(View.GONE);
            changeEditText.setInputType(1);
        }
        else if(hint.equals(TAG_EMAIL)){
            oldEditText.setVisibility(View.VISIBLE);
            confirmEditText.setVisibility(View.GONE);
            oldEditText.setHint("Current password");
            changeEditText.setInputType(32);
        }
        else
        {
            oldEditText.setVisibility(View.VISIBLE);
            confirmEditText.setVisibility(View.VISIBLE);
            oldEditText.setHint("Current password");
            confirmEditText.setHint("Confirm new password");
            changeEditText.setHint("New password");
            changeEditText.setInputType(confirmEditText.getInputType());
        }

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        }).setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {

                            }
                        });


        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(changeEditText.getText().toString().trim())) {
                    Toast.makeText(SettingsActivity.this, "Enter your " + hint, Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    alertDialog.dismiss();
                }

                if(hint.equals(TAG_NAME)){
                    userName = changeEditText.getText().toString().trim();
                    refName = database.getReference().child("users").child(keyUser).child("name");
                    refName.setValue(userName);
                    usernameTextV.setText(userName);
                }
                else if(hint.equals((TAG_LOGIN))){
                    userLogin = changeEditText.getText().toString().trim();
                    if(!equalLogin(userLogin)){
                        refUsers.child(keyUser).child("login").setValue(userLogin);
                        loginTV.setText("@"+userLogin);
                    }
                    else
                        Toast.makeText(SettingsActivity.this, "This login is just exists", Toast.LENGTH_LONG).show();

                }
                else if(hint.equals(TAG_EMAIL)){
                    if(TextUtils.isEmpty(oldEditText.getText().toString().trim())){
                        Toast.makeText(SettingsActivity.this, "Enter your password for changing email ", Toast.LENGTH_LONG).show();
                        return;

                    }
                    else{
                        String password = oldEditText.getText().toString().trim();
                        userEmail = changeEditText.getText().toString().trim();
                        updateUserEmail(password, userEmail);
                    }
                }
                else {
                    if(TextUtils.isEmpty(oldEditText.getText().toString().trim())){
                        Toast.makeText(SettingsActivity.this, "Enter your current password for changing", Toast.LENGTH_LONG).show();
                        return;

                    }
                    else if(!changeEditText.getText().toString().trim().equals((confirmEditText.getText().toString().trim()))){
                        Toast.makeText(SettingsActivity.this, "Passwords don't have to match", Toast.LENGTH_LONG);
                        return;
                    }
                    else if(changeEditText.getText().toString().trim().length() < 6){
                        Toast.makeText(SettingsActivity.this, "Password length must have more than 6 characters ", Toast.LENGTH_LONG);
                        return;
                    }
                    else{
                        String password = oldEditText.getText().toString().trim();
                        userPassword = changeEditText.getText().toString().trim();
                        updateUserPassword(password, userPassword);
                    }

                }
            }
        });
    }

    private void updateUserPassword(String password, String newPassword) {
        AuthCredential authCredential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
        currentUser.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                currentUser.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SettingsActivity.this, "Password changed", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void updateUserEmail(String password, String newEmail) {
        AuthCredential authCredential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
        currentUser.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                currentUser.updateEmail(newEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SettingsActivity.this, "Email changed", Toast.LENGTH_LONG).show();
                        refEmail = database.getReference().child("users").child(keyUser).child("email");
                        refEmail.setValue(newEmail);
                        currentEmailTextV.setText("Your current email: "+ userEmail);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void attachUserListener() {
        if (usersEventListener == null) {
            usersEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);
                    users.add(user);
                    if(user.getId().equals(auth.getCurrentUser().getUid())){
                        userName = user.getName();
                        usernameTextV.setText(userName);
                        userEmail = user.getEmail();
                        userLogin = user.getLogin();
                        loginTV.setText("@"+userLogin);
                        keyUser = snapshot.getKey();
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            refUsers.addChildEventListener(usersEventListener);
        }
    }

    public void setAvatar(View view) {
        String options[] = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Load image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    if(!checkCameraPermission())
                        requestCameraPermissions();
                    else{
                        pickFromCamera();
                    }
                }
                else if (which == 1){
                    if(!checkStoragePermission())
                        requestStoragePermissions();
                    else{
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private boolean checkStoragePermission(){
        return ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermissions(){
        ActivityCompat.requestPermissions(SettingsActivity.this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        return ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestCameraPermissions(){
        ActivityCompat.requestPermissions(SettingsActivity.this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && writeStorageAccepted){
                        pickFromCamera();
                    }
                    else{
                        Toast.makeText(SettingsActivity.this, "Please enable camera and storage permissions", Toast.LENGTH_LONG).show();
                    }
                }

            }
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(writeStorageAccepted){
                        pickFromGallery();
                    }
                    else{
                        Toast.makeText(SettingsActivity.this, "Please enable storage permissions", Toast.LENGTH_LONG).show();
                    }
                }

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp description");

        avatarUri = (SettingsActivity.this).getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, avatarUri);
        startActivityForResult(cameraIntent, CAMERA_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == GALLERY_CODE){
                avatarUri = data.getData();
                uploadAvatar(avatarUri);
            }
            if(requestCode == CAMERA_CODE){
                uploadAvatar(avatarUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadAvatar(Uri avatarUri) {
        StorageReference imageReference = avatarImagesReference.child(avatarUri.getLastPathSegment());
        UploadTask uploadTask = imageReference.putFile(avatarUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    refAvatar = database.getReference().child("users").child(keyUser).child("avatarMockUpResource");
                    refAvatar.setValue(downloadUri.toString());

                    try{
                        Picasso.get().load(downloadUri).into(avatarIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.user_image).into(avatarIv);
                    }
                } else {

                }
            }
        });
    }

    public void changeLogin(View view) {
        createDialog(TAG_LOGIN);
    }
    private boolean equalLogin(String login){
        for(User oneUser: users){
            if(oneUser.getLogin().equals(login))
                return true;
        }
        return false;
    }

    public void deleteAccount(View view) {
        FirebaseUser user = auth.getCurrentUser();
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view_delete = layoutInflaterAndroid.inflate(R.layout.layout_delete_account, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setView(view_delete);
        final EditText passwordDeleteEditText = view_delete.findViewById(R.id.passwordForDeleteET);
        final Button accountDeleteBtn = view_delete.findViewById(R.id.deleteAccountDialogBtn);
        final Button accountCancelBtn = view_delete.findViewById(R.id.cancelAccountDialogBtn);
        accountCancelBtn.setTextColor(getColor(R.color.white));
        accountDeleteBtn.setTextColor(getColor(R.color.white));
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        accountCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        accountDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(passwordDeleteEditText.getText().toString().trim())) {
                    Toast.makeText(SettingsActivity.this, "Enter your current password to delete account", Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    alertDialog.dismiss();
                }
                AuthCredential authCredential = EmailAuthProvider.getCredential(currentUser.getEmail(), (passwordDeleteEditText.getText().toString().trim()));
                user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Query queryUsers = refUsers.orderByChild("email").equalTo(userEmail);
                        queryUsers.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                for(DataSnapshot ds: snapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                        refContacts.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    if(ds.getKey().equals(idUser)){
                                        ds.getRef().removeValue();
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(SettingsActivity.this, "Account deleted", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(SettingsActivity.this, SignInActivity.class));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.signOut:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SettingsActivity.this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = database.getReference().child("users");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    if(ds.child("id").getValue().toString().equals(idUser)){
                        keyUser = ds.getKey().toString();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        if(keyUser != null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("onlineStatus", status);
            dbRef.child(keyUser).updateChildren(hashMap);
        }
    }

    @Override
    protected void onStart() {
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String time = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(time);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }
}