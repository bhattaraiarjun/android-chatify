package com.example.chatify.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatify.adapters.MessagesAdapter;
import com.example.chatify.databinding.ActivityChatBinding;
import com.example.chatify.models.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    ArrayList<String> offensiveWords;
    ArrayList<String> friends;
    String senderRoom, receiverRoom;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String receiverUid;
    String senderUid;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending image...");
        dialog.setCancelable(false);


        messages = new ArrayList<>();

        String name = getIntent().getStringExtra("name");
        receiverUid = getIntent().getStringExtra("uid");
        offensiveWords = getIntent().getStringArrayListExtra("offensive-words");
        friends = getIntent().getStringArrayListExtra("friends");
        if(friends == null){
            friends = new ArrayList<>();
        }

        senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = receiverUid + senderUid;
        receiverRoom = senderUid + receiverUid;

        adapter = new MessagesAdapter(this, messages, offensiveWords,receiverRoom,senderRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                                .addValueEventListener(new ValueEventListener() {
                                    @SuppressLint("NotifyDataSetChanged")
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    messages.clear();
                                    for(DataSnapshot snapshot1: snapshot.getChildren()){
                                        Message message = snapshot1.getValue(Message.class);
                                        message.setMessageId(snapshot1.getKey());
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
            if(messageTxt.length() < 1){
                return;
            }
            Date date = new Date();
            Message message = new Message(messageTxt,senderUid,date.getTime());
            binding.messageBox.setText("");
            Log.d("MG", "onCreate: "+ senderRoom + " Receiver Room+  " + receiverRoom);

            String randomkey =database.getReference().push().getKey();

            HashMap<String,Object>  lastMsgObj = new HashMap<>();
            lastMsgObj.put("lastMsg",message.getMessage());
            lastMsgObj.put("lastMsgTime",date.getTime());

            database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
            database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

            database.getReference().child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(randomkey)
                    .setValue(message).addOnSuccessListener(unused -> database.getReference().child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(randomkey)
                            .setValue(message).addOnSuccessListener(unused1 -> {
                            }));
        });

        binding.attachment.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 25);
        });

        Objects.requireNonNull(getSupportActionBar()).setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==25) {
            if (data != null) {
                if (data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();
                                        String messageTxt = binding.messageBox.getText().toString();
                                        Date date = new Date();
                                        Message message = new Message(messageTxt,senderUid,date.getTime());
                                        message.setMessage("photo");

                                        message.setImageUrl(filePath);
                                        binding.messageBox.setText("");
                                        Log.d("MG", "onCreate: "+ senderRoom + " Receiver Room+  " + receiverRoom);

                                        String randomkey =database.getReference().push().getKey();

                                        HashMap<String,Object>  lastMsgObj = new HashMap<>();
                                        lastMsgObj.put("lastMsg",message.getMessage());
                                        lastMsgObj.put("lastMsgTime",date.getTime());
                                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomkey)
                                                .setValue(message).addOnSuccessListener(unused -> database.getReference().child("chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(randomkey)
                                                        .setValue(message).addOnSuccessListener(unused1 -> {

                                                        }));
                                        //Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    });

                }
            }
        }}

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}