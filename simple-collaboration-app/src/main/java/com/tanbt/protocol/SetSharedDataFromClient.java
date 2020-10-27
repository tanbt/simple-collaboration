package com.tanbt.protocol;

public class SetSharedDataFromClient implements MessageProtocol {
    private final String newData;

    public SetSharedDataFromClient(String newData) {
        this.newData = newData;
    }

    public String getNewData() {
        return newData;
    }
}
