package com.jkutkut.android_chat.model;

public class Msg {
    private String id;
    private String msg;
    private String sender;
    private String photoUrl;

    public Msg() {}

    public Msg(String id, String msg, String sender, String photoUrl) {
        this.id = id;
        this.msg = msg;
        this.sender = sender;
        this.photoUrl = photoUrl;
    }

    // GETTERS
    public String getId() {
        return id;
    }

    public String getMsg() {
        return msg;
    }

    public String getSender() {
        return sender;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    // SETTERS
    public void setId(String id) {
        this.id = id;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
