package com.tanbt.protocol;

public class NotifySubscriber implements MessageProtocol {
    private final String newData;

    public NotifySubscriber(String newData) {
        this.newData = newData;
    }

    public String getNewData() {
        return newData;
    }
}

