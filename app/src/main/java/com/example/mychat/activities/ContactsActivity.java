package com.example.mychat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mychat.R;
import com.example.mychat.adapters.UserAdapter;
import com.example.mychat.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactsActivity extends AppCompatActivity {
    private DatabaseReference usersDatabaseReference, contactsDatabaseReference;
    private ArrayList<User> users, contacts;
    private RecyclerView recyclerContacts;
    private UserAdapter adapterContacts;
    private RecyclerView.LayoutManager layoutManagerUsers;
    private FirebaseAuth auth;
    private String userName = "Default name";
    private FloatingActionButton fab;
    private FirebaseDatabase database;
    private String key, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        Intent intent = getIntent();
        if(intent != null)
            userName = intent.getStringExtra("userName");

        setTitle("Contacts");
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }


        fab = findViewById(R.id.fabAddContact);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact();
            }
        });

        users = new ArrayList<User>();
        contacts = new ArrayList<User>();
        usersDatabaseReference = database.getReference().child("users");
        checkOnlineStatus("online");
        attachUserListener();
        attachContactListener();
        buildRecyclerView();

    }

    private void attachUserListener() {
        usersDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    users.add(user);
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void attachContactListener() {
        contactsDatabaseReference = database.getReference().child("userContacts").child(auth.getUid());

        contactsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String userID = ds.getValue(String.class);
                    for(int i = 0; i<users.size(); i++){
                        if(users.get(i).getId().equals(userID)){
                            contacts.add(users.get(i));
                            adapterContacts.notifyDataSetChanged();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void addContact(){
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.layout_add_contact, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(ContactsActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText emailEditText = view.findViewById(R.id.emailEditText);
        final EditText loginEditText = view.findViewById(R.id.loginEditText);

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });


        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().equals(""))
                    loginEditText.setEnabled(false);
                else
                    loginEditText.setEnabled(true);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        loginEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().trim().equals(""))
                    emailEditText.setEnabled(false);
                else
                    emailEditText.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(emailEditText.getText().toString().trim()) &&
                        TextUtils.isEmpty(loginEditText.getText().toString().trim())) {
                    Toast.makeText(ContactsActivity.this, "Enter users email or login", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }
                int count = 0;
                if(!users.isEmpty() && !TextUtils.isEmpty(emailEditText.getText().toString().trim())){
                    for(int i = 0; i < users.size(); i++){
                        if(users.get(i).getEmail().equals(emailEditText.getText().toString().trim())){
                            if(!containsUsers(contacts, users.get(i))) {
                                contactsDatabaseReference = database.getReference().child("userContacts").child(auth.getUid());
                                contactsDatabaseReference.push().setValue(users.get(i).getId());
                                count++;
                            }
                            else{
                                Toast.makeText(ContactsActivity.this, "User with this email at your contacts yet", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }
                }
                else if(!users.isEmpty() && !TextUtils.isEmpty(loginEditText.getText().toString().trim())){
                    for(int i = 0; i < users.size(); i++){
                        if(users.get(i).getLogin().equals(loginEditText.getText().toString().trim())){
                            if(!containsUsers(contacts, users.get(i))) {
                                contactsDatabaseReference = database.getReference().child("userContacts").child(auth.getUid());
                                contactsDatabaseReference.push().setValue(users.get(i).getId());
                                count++;
                            }
                            else{
                                Toast.makeText(ContactsActivity.this, "User with this login at your contacts yet", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }
                }
                if(count == 0){
                    Toast.makeText(ContactsActivity.this, "User not found", Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                    return;
                }
            }
        });

    }

    private boolean containsUsers(ArrayList<User> userList, User user){
        for(int i = 0; i < userList.size(); i++){
            if(userList.get(i).getEmail().equals(user.getEmail())){
                return true;
            }
        }
        return false;
    }

    private void buildRecyclerView() {
        recyclerContacts = findViewById(R.id.recyclerContacts);
        recyclerContacts.setHasFixedSize(true);
        recyclerContacts.addItemDecoration(new DividerItemDecoration(recyclerContacts.getContext(), DividerItemDecoration.VERTICAL));
        layoutManagerUsers = new LinearLayoutManager(this);
        adapterContacts = new UserAdapter(contacts, ContactsActivity.this);
        recyclerContacts.setLayoutManager(layoutManagerUsers);
        recyclerContacts.setAdapter(adapterContacts);

        adapterContacts.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                goToChat(position);
            }
        });
    }

    private void goToChat(int position) {
        Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);
        intent.putExtra("recipUserId", users.get(position).getId());
        intent.putExtra("recipUserName", users.get(position).getName());
        intent.putExtra("recipUserAvatar", users.get(position).getAvatarMockUpResource());
        intent.putExtra("userName", userName);
        startActivity(intent);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
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
                startActivity(new Intent(ContactsActivity.this, SignInActivity.class));
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
                    if(ds.child("id").getValue().toString().equals(userId)){
                        key = ds.getKey().toString();
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


}