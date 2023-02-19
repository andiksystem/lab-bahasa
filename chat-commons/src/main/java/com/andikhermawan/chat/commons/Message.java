package com.andikhermawan.chat.commons;

import java.io.Serializable;

public class Message implements Serializable {

    private long chatId;
    private long timestamp, ttl = 2000;
    private final Object data;

    public Message(long chatId, long timestamp, Object data) {
        this.chatId = chatId;
        this.timestamp = timestamp;
        this.data = data;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getChatId() {
        return chatId;
    }

    public Object getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public void setChId(long chId) {
        this.chatId = chId;
    }

}
