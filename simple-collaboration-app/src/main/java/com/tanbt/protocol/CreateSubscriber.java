package com.tanbt.protocol;

public class CreateSubscriber implements MessageProtocol {
    private final String clientId;

    public CreateSubscriber(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }
}
