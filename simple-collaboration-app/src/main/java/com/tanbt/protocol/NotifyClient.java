package com.tanbt.protocol;

public class NotifyClient implements MessageProtocol {
    private final String data;

    public NotifyClient(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}
