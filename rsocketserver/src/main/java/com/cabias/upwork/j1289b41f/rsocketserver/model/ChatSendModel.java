package com.cabias.upwork.j1289b41f.rsocketserver.model;

public class ChatSendModel {
    private String fingerprint;
    private String message;

    public ChatSendModel() {
    }

    public ChatSendModel(String fingerprint, String message) {
        this.fingerprint = fingerprint;
        this.message = message;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
