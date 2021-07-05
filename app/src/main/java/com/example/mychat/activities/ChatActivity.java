package com.example.mychat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mychat.adapters.ChatAdapter;
import com.example.mychat.models.Message;
import com.example.mychat.R;
import com.example.mychat.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {
    private ArrayList<Message> messages;
    private RecyclerView recyclerChat;
    private ChatAdapter adapter;
    private LinearLayoutManager layoutManagerChat;
    private ProgressBar progressBar;;
    private ImageButton imagePhotoButton;
    private Button sendButton;
    private EditText messageEditText;
    private String userName, recipUserId, recipUserAvatar, userId;
    private FirebaseAuth auth;
    private Toolbar toolbar;
    private TextView recipUserNameTv, statusUserTv;
    private ImageView profileIm;
    private String key;

    private static final int RC_IMAGE = 111;

    FirebaseDatabase database;
    DatabaseReference messagesDatabaseReference;
    DatabaseReference usersDatabaseReference;
    ChildEventListener usersEventListener;
    ValueEventListener seenListener;
    DatabaseReference usersRefForSeen;
    FirebaseStorage storage;
    StorageReference chatImagesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        database = FirebaseDatabase.getInstance();
        recyclerChat = findViewById(R.id.recyclerChat);
        layoutManagerChat = new LinearLayoutManager(this);
        layoutManagerChat.setStackFromEnd(true);
        recyclerChat.setHasFixedSize(true);
        recyclerChat.addItemDecoration(new DividerItemDecoration(recyclerChat.getContext(), DividerItemDecoration.VERTICAL));
        recyclerChat.setLayoutManager(layoutManagerChat);
        checkOnlineStatus("online");


        profileIm = findViewById(R.id.profileIm);
        statusUserTv = findViewById(R.id.userStatusTv);
        recipUserNameTv = findViewById(R.id.nameUserTv);


        storage = FirebaseStorage.getInstance();
        messagesDatabaseReference = database.getReference().child("messages");
        usersDatabaseReference = database.getReference().child("users");
        chatImagesReference = storage.getReference().child("chat_images");



        Intent intent = getIntent();
        if(intent != null) {
            userName = intent.getStringExtra("userName");
            recipUserId = intent.getStringExtra("recipUserId");
            recipUserAvatar = intent.getStringExtra("recipUserAvatar");
            recipUserNameTv.setText(intent.getStringExtra("recipUserName"));


            try{
                if(!recipUserAvatar.equals(" ")){
                    Uri image = Uri.parse(recipUserAvatar);
                    if(image.toString().contains("%2Fstorage"))
                        Picasso.get().load(image).rotate(270).into(profileIm);
                    else
                        Picasso.get().load(image).rotate(90).into(profileIm);

                }

            }catch (Exception e){
                Picasso.get().load(R.drawable.user_image).into(profileIm);
            }

        }
        else
            userName = "Anonymous";

        progressBar = findViewById(R.id.progressBar);
        imagePhotoButton = findViewById(R.id.sendPhotoButton);
        sendButton = findViewById(R.id.sendMessageButton);
        messageEditText = findViewById(R.id.messageEditText);

        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        progressBar.setVisibility(ProgressBar.INVISIBLE);

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().equals(""))
                    sendButton.setEnabled(false);
                else  sendButton.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        messageEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(1000)});

        imagePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Choose an image"), RC_IMAGE);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();

                String time = String.valueOf(System.currentTimeMillis());
                message.setTime(time);
                message.setText(messageEditText.getText().toString());
                message.setName(userName);
                message.setImageUrl(null);
                message.setSender(auth.getCurrentUser().getUid());
                message.setRecipient(recipUserId);
                message.setSeen(false);
                messageEditText.setText("");

                messagesDatabaseReference.push().setValue(message);
            }
        });

        attachUserListener();
        attachMessagesListener();
        seenMessage();
    }

    private void attachUserListener() {
        usersEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User user = snapshot.getValue(User.class);
                if(user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    userName = user.getName();
                    key = snapshot.getKey();
                }
                if(user.getId().equals(recipUserId)){
                    String onlineStatus = user.getOnlineStatus();
                    if(onlineStatus.equals("online"))
                        statusUserTv.setText(onlineStatus);
                    else{
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(Long.parseLong(onlineStatus));
                        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a, d MMM");
                        String time = dateFormat.format(c.getTime());
                        statusUserTv.setText("last seen at " + time);
                    }
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
        usersDatabaseReference.addChildEventListener(usersEventListener);
    }

    private void attachMessagesListener() {
        messages = new ArrayList<>();

        messagesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                messages.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    Message message = ds.getValue(Message.class);
                    if (message.getSender().equals(auth.getCurrentUser().getUid()) && message.getRecipient().equals(recipUserId)
                            || message.getRecipient().equals(auth.getCurrentUser().getUid()) && message.getSender().equals(recipUserId)) {
                        messages.add(message);

                    }

                    adapter = new ChatAdapter(messages, ChatActivity.this);
                    adapter.notifyDataSetChanged();
                    recyclerChat.setAdapter(adapter);

                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void seenMessage() {
        usersRefForSeen = database.getReference().child("messages");
        seenListener = usersRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    Message message = ds.getValue(Message.class);
                    if(message.getRecipient().equals(auth.getCurrentUser().getUid()) &&
                    message.getSender().equals(recipUserId)){
                        HashMap<String, Object> hashMapSeen = new HashMap<>();
                        hashMapSeen.put("seen", true);
                        ds.getRef().updateChildren(hashMapSeen);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = database.getReference().child("users");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    if(ds.child("id").getValue().toString().equals(userId)){
                        key = ds.getKey();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        if(key != null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("onlineStatus", status);
            dbRef.child(key).updateChildren(hashMap);
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
        usersRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
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
                startActivity(new Intent(ChatActivity.this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_IMAGE && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();
            StorageReference imageReference = chatImagesReference.child(selectedImageUri.getLastPathSegment());

            UploadTask uploadTask = imageReference.putFile(selectedImageUri);

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
                        Message message = new Message();
                        String time = String.valueOf(System.currentTimeMillis());
                        message.setImageUrl(downloadUri.toString());
                        message.setName(userName);
                        message.setSender(auth.getCurrentUser().getUid());
                        message.setRecipient(recipUserId);
                        message.setTime(time);
                        message.setSeen(false);
                        if(!messageEditText.getText().toString().trim().equals("")) {
                            message.setText(messageEditText.getText().toString().trim());
                            messageEditText.setText("");
                        }
                        messagesDatabaseReference.push().setValue(message);
                    } else {

                    }
                }
            });
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}