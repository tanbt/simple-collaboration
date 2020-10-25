package com.tanbt.protocol;

public class SetSharedData implements MessageProtocol {
    private final String newData;

    public SetSharedData(String newData) {
        this.newData = newData;
    }

    public String getNewData() {
        return newData;
    }
}
