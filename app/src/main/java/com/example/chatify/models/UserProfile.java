package com.example.chatify.models;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class UserProfile {

    private String uid, name,phoneNumber, profileImage, token;
    private ArrayList<String> friends;
    public UserProfile(){

    }

    public UserProfile(String uid, String name, String phoneNumber, String profileImage, ArrayList<String> friends) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
        this.friends = friends;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public ArrayList<String> getFriends() {
        if(friends == null){
            return new ArrayList<>();
        }
        return friends;
    }

    public void setFriends(ArrayList<String> friends) {
        this.friends = friends;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @NonNull
    @Override
    public String toString() {
        return  this.uid + "\n" +
                this.name + "\n" +
                this.profileImage +
                "\n";
    }
}
