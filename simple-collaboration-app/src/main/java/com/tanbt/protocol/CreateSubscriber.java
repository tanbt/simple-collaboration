package com.tanbt.protocol;

import io.rsocket.RSocket;

public class CreateSubscriber implements MessageProtocol {
    private final String clientId;
    private final RSocket clientSocket;

    public CreateSubscriber(String clientId, RSocket clientSocket) {
        this.clientId = clientId;
        this.clientSocket = clientSocket;
    }

    public String getClientId() {
        return clientId;
    }

    public RSocket getClientSocket() {
        return clientSocket;
    }
}
