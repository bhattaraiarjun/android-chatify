package com.example.chatify.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatify.R;
import com.example.chatify.activities.ChatActivity;
import com.example.chatify.databinding.RowConversationBinding;
import com.example.chatify.models.UserProfile;
import com.example.chatify.utils.ChatifyUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UsersAdapter extends  RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {
    Context context;
    ArrayList<UserProfile> userProfiles;
    private ArrayList<String> offensiveWords;

    public UsersAdapter(Context context, ArrayList<UserProfile>userProfiles, ArrayList<String> offensiveWords){
        this.context = context;
        this.userProfiles = userProfiles;
        this.offensiveWords = offensiveWords;
    }
    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);

        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        UserProfile userProfile = userProfiles.get(position);
        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId+ userProfile.getUid();
        FirebaseDatabase.getInstance().getReference()
                        .child("chats")
                                .child(senderRoom)
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists()) {
                                                    String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                                                    long time = snapshot.child("lastMsgTime").getValue(Long.class);
                                                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                                    holder.binding.msgTime.setVisibility(View.VISIBLE);
                                                    holder.binding.msgTime.setText(dateFormat.format(new Date(time)));
                                                    try {
                                                        holder.binding.lastMsg.setText(ChatifyUtils.censorOffensiveWords(ChatifyUtils.decryptMessage(lastMsg), offensiveWords));
                                                    } catch (Exception e) {
                                                        holder.binding.lastMsg.setText(ChatifyUtils.censorOffensiveWords(lastMsg, offensiveWords));
                                                    }
                                                } else {
                                                    holder.binding.lastMsg.setText("Tap to chat");
                                                    holder.binding.msgTime.setVisibility(View.INVISIBLE);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

        holder.binding.username.setText(userProfile.getName());
        Glide.with(context).load(userProfile.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.profile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name",userProfile.getName());
                intent.putExtra("uid",userProfile.getUid());
                intent.putExtra("friends", userProfile.getFriends());
                intent.putExtra("offensive-words", offensiveWords);
                intent.putExtra("token", userProfile.getToken());

                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return userProfiles.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder{
        RowConversationBinding binding;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }
}
