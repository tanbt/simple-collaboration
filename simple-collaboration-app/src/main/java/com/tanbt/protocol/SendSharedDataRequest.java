package com.tanbt.protocol;

public class SendSharedDataRequest implements MessageProtocol {
    private final String clientId;

    public SendSharedDataRequest(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }
}

