package fcu.app.distributionapp.model;

import com.google.firebase.Timestamp;

public class Message {
    private String senderId;
    private String senderName;
    private String content;
    private Timestamp timestamp;

    public Message(){

    }

    public Message(String senderId, String senderName, String content, Timestamp timestamp) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
