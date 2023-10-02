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

import java.util.ArrayList;

public class UsersAdapter extends  RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {
    Context context;
    ArrayList<UserProfile> userProfiles;
    public UsersAdapter(Context context, ArrayList<UserProfile>userProfiles){
        this.context = context;
        this.userProfiles = userProfiles;

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
