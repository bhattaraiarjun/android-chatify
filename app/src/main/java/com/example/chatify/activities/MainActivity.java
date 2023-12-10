package com.example.chatify.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.chatify.R;
import com.example.chatify.adapters.UsersAdapter;
import com.example.chatify.databinding.ActivityMainBinding;
import com.example.chatify.models.UserProfile;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ArrayList<UserProfile> userProfiles;
    UsersAdapter usersAdapter;
    ArrayList<String> offensiveWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        offensiveWords = new ArrayList<>();

        setContentView(R.layout.activity_main);
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("token", token);
                        database.getReference()
                                .child("users")
                                .child(FirebaseAuth.getInstance().getUid())
                                .updateChildren(map);
                        //Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });

        userProfiles = new ArrayList<>();
        usersAdapter = new UsersAdapter(this, userProfiles, this.offensiveWords);

        binding.recyclerView.setAdapter(usersAdapter);

        //get Data from Database
        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userProfiles.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    UserProfile userProfile = snapshot1.getValue(UserProfile.class);
                    if (userProfile != null && userProfile.getUid() != null
                            && !userProfile.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                        userProfiles.add(userProfile);
                    }
                }
                usersAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("offensive-words")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        offensiveWords.clear();
                        for(DataSnapshot snapshot1: snapshot.getChildren()){
                            String word = snapshot1.getValue(String.class);
                            offensiveWords.add(word);
                        }

                        usersAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            auth.signOut();
            Intent intent = new Intent(MainActivity.this,
                    PhoneNumberActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        if (item.getItemId() == R.id.search) {
            Toast.makeText(this, "Search Clicked!", Toast.LENGTH_SHORT).show();
        }
        if (item.getItemId() == R.id.setting) {
            Toast.makeText(this, "Setting Clicked!", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}