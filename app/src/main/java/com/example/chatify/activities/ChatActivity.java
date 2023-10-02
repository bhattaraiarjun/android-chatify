package com.example.chatify.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatify.adapters.MessagesAdapter;
import com.example.chatify.databinding.ActivityChatBinding;
import com.example.chatify.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;
    FirebaseDatabase database;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this,messages);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);


        String name = getIntent().getStringExtra("name");
        String receiverUid = getIntent().getStringExtra("uid");
        String senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = receiverUid + senderUid;
        receiverRoom = senderUid + receiverUid;
        database = FirebaseDatabase.getInstance();

        database.getReference().child("chats")
                .child(senderRoom)
                        .child("message")
                                .addValueEventListener(new ValueEventListener() {
                                    @SuppressLint("NotifyDataSetChanged")
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    messages.clear();
                                    for(DataSnapshot snapshot1: snapshot.getChildren()){
                                        Message message = snapshot1.getValue(Message.class);
                                        messages.add(message);
                                    }
                                    adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

        //Message Sending
        binding.sendBtn.setOnClickListener(v -> {
            String messageTxt = binding.messageBox.getText().toString();
            Date date = new Date();
            Message message = new Message(messageTxt,senderUid,date.getTime());
            binding.messageBox.setText("");
            database.getReference().child("chats")
                    .child(senderRoom)
                    .child(String.valueOf(messages))
                    .push()
                    .setValue(message).addOnSuccessListener(unused -> database.getReference().child("chats")
                            .child(receiverRoom)
                            .child(String.valueOf(messages))
                            .push()
                            .setValue(message).addOnSuccessListener(unused1 -> {

                            }));
        });

        Objects.requireNonNull(getSupportActionBar()).setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}