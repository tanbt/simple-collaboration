package com.tanbt.protocol;

import io.rsocket.RSocket;

public class SetCollaborationServer implements MessageProtocol {
    private final RSocket collaborationServerSocket;

    public SetCollaborationServer(RSocket collaborationServerSocket) {
        this.collaborationServerSocket = collaborationServerSocket;
    }

    public RSocket getCollaborationServerSocket() {
        return collaborationServerSocket;
    }
}
