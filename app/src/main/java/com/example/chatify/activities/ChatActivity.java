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

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatify.adapters.MessagesAdapter;
import com.example.chatify.databinding.ActivityChatBinding;
import com.example.chatify.models.Message;
import com.example.chatify.utils.ChatifyUtils;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
    String name;
    String token;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending image...");
        dialog.setCancelable(false);


        messages = new ArrayList<>();

        name = getIntent().getStringExtra("name");
        token = getIntent().getStringExtra("token");
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
            try {
                String messageTxt = binding.messageBox.getText().toString();
                if(messageTxt.length() < 1){
                    return;
                }
                Date date = new Date();
                // Encryption logic goes here
                String encryptedMessage = ChatifyUtils.encryptMessage(messageTxt);
                Message message = new Message(encryptedMessage,senderUid,date.getTime());
                binding.messageBox.setText("");

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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
                                        try {
                                            String filePath = uri.toString();
                                            String messageTxt = binding.messageBox.getText().toString();
                                            Date date = new Date();
                                            Message message = new Message(messageTxt, senderUid, date.getTime());
                                            message.setMessage( "photo");

                                            message.setImageUrl(filePath);
                                            binding.messageBox.setText("");
                                            Log.d("MG", "onCreate: " + senderRoom + " Receiver Room+  " + receiverRoom);

                                            String randomkey = database.getReference().push().getKey();

                                            HashMap<String, Object> lastMsgObj = new HashMap<>();
                                            lastMsgObj.put("lastMsg", message.getMessage());
                                            lastMsgObj.put("lastMsgTime", date.getTime());
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
                                                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    try {
                                                                        sendNotification(name, ChatifyUtils.decryptMessage(message.getMessage()), token);
                                                                    } catch (Exception e) {
                                                                        sendNotification(name, message.getMessage(), token);
                                                                    }
                                                                }
                                                            }));
                                        } catch (Exception e){
                                            throw new RuntimeException(e);
                                        }
                                        //Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    });

                }
            }
        }
    }

    void sendNotification(String name, String message, String token) {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", message);
            JSONObject notificationData = new JSONObject();
            notificationData.put("notification", data);
            notificationData.put("to",token);

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData
                    , response -> {
                        // Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_SHORT).show();
                    }, error -> Toast.makeText(ChatActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show()) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    String key = "Key=AAAASn2Fs4A:APA91bGdTVxFBP-V0NN_zLjQTUb7yr9Shy0sYcSN2MvHxTksz11FktDxUt44hKD3CyD2ghCX61RGJW25F0mBPpTBrSArmo9emaKP8HqRQGe5A8vrdygKbY-Kfph9YvaeQnPmif5a1Zr7";
                    map.put("Content-Type", "application/json");
                    map.put("Authorization", key);

                    return map;
                }
            };

            queue.add(request);


        } catch (Exception ex) {

        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}