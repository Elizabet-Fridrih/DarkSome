package com.example.mychat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;

import com.example.mychat.R;
import com.example.mychat.adapters.UserAdapter;
import com.example.mychat.models.Message;
import com.example.mychat.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private DatabaseReference usersDatabaseReference;
    private DatabaseReference messagesDatabaseReference;
    private ChildEventListener usersEventListener;
    private ArrayList<User> users, allUsers;
    private ArrayList<Message> messages;
    private RecyclerView recyclerUsers;
    private UserAdapter adapterDialogs;
    private RecyclerView.LayoutManager layoutManagerUsers;
    private FirebaseAuth auth;
    private String userName, userId;
    private FirebaseDatabase database;
    private  String keyUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Dialogs");

        database = FirebaseDatabase.getInstance();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DrawerActivity.this, ContactsActivity.class));
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        if(intent != null)
            userName = intent.getStringExtra("userName");
        else userName = "Default name";

        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        users = new ArrayList<User>();
        allUsers = new ArrayList<User>();
        messages = new ArrayList<Message>();
        messagesDatabaseReference = database.getReference().child("messages");

        checkOnlineStatus("online");
        attachUserListener();
        setLogin();
        attachMessageListener();
        buildRecyclerView();

    }

    private void setLogin() {
        DatabaseReference usersLoginReference = database.getReference().child("users");
        usersLoginReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    if(user.getId().equals(auth.getCurrentUser().getUid())&&
                            user.getLogin().equals(" ")){
                        String rnd = String.valueOf(randomLogin());
                        if(!equalLogin(rnd)){
                            usersLoginReference.child(keyUser).child("login").setValue(rnd);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }


    private void attachMessageListener() {
       messagesDatabaseReference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
               for(DataSnapshot ds: snapshot.getChildren()){
                   Message message = ds.getValue(Message.class);
                   if (message.getSender().equals(auth.getCurrentUser().getUid())
                           || message.getRecipient().equals(auth.getCurrentUser().getUid())) {
                       messages.add(message);
                   }
               }
               for(int i = 0; i<allUsers.size(); i++){
                   for(int j = 0; j<messages.size(); j++){
                       if(allUsers.get(i).getId().equals(messages.get(j).getSender())
                       || allUsers.get(i).getId().equals(messages.get(j).getRecipient())){
                           if(!equalUsers(users, allUsers.get(i))){
                               users.add(allUsers.get(i));
                               adapterDialogs.notifyDataSetChanged();
                           }
                       }
                   }
               }

           }

           @Override
           public void onCancelled(@NonNull @NotNull DatabaseError error) {

           }
       });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
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
                startActivity(new Intent(DrawerActivity.this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.nav_contacts:
                startActivity(new Intent(DrawerActivity.this, ContactsActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(DrawerActivity.this, SettingsActivity.class));
                break;
            case R.id.nav_support:
                startActivity(new Intent(DrawerActivity.this, SupportActivity.class));
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void attachUserListener() {
        usersDatabaseReference = database.getReference().child("users");
        if (usersEventListener == null) {
            usersEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);
                    allUsers.add(user);
                    if(user.getId().equals(auth.getCurrentUser().getUid())){
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
            usersDatabaseReference.addChildEventListener(usersEventListener);
        }
    }

    private int randomLogin() {
        final int min = 10000000;
        final int max = 100000000;
        return (int)(Math.random()*max)+min;
    }

    private void buildRecyclerView() {
        recyclerUsers = findViewById(R.id.recyclerUserList);
        recyclerUsers.setHasFixedSize(true);
        recyclerUsers.addItemDecoration(new DividerItemDecoration(recyclerUsers.getContext(), DividerItemDecoration.VERTICAL));
        layoutManagerUsers = new LinearLayoutManager(this);
        adapterDialogs = new UserAdapter(users, DrawerActivity.this);
        recyclerUsers.setLayoutManager(layoutManagerUsers);
        recyclerUsers.setAdapter(adapterDialogs);

        adapterDialogs.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                goToChat(position);
            }
        });
    }

    private void goToChat(int position) {
        Intent intent = new Intent(DrawerActivity.this, ChatActivity.class);
        intent.putExtra("recipUserId", users.get(position).getId());
        intent.putExtra("recipUserName", users.get(position).getName());
        intent.putExtra("userName", userName);
        intent.putExtra("recipUserAvatar", users.get(position).getAvatarMockUpResource());
        startActivity(intent);
    }

    private boolean equalUsers(ArrayList<User> users, User user){
        if(user.getId().equals(auth.getCurrentUser().getUid()))
            return true;
        for(User oneUser: users){
            if(oneUser.getId().equals(user.getId()))
                return true;
        }
        return false;
    }
    private boolean equalLogin(String login){
        for(User oneUser: allUsers){
            if(oneUser.getLogin().equals(login))
                return true;
        }
        return false;
    }
    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = database.getReference().child("users");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    if(ds.child("id").getValue().toString().equals(userId)){
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

