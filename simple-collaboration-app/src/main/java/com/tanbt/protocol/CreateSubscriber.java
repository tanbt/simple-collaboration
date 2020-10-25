package com.tanbt.protocol;

import akka.actor.typed.ActorRef;
import io.rsocket.RSocket;

public class CreateSubscriber implements MessageProtocol {
    private final String clientId;
    private final RSocket clientSocket;
    private ActorRef<MessageProtocol> parentActor;

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

    public ActorRef<MessageProtocol> getParentActor() {
        return parentActor;
    }

    public void setParentActor(ActorRef<MessageProtocol> parentActor) {
        this.parentActor = parentActor;
    }
}
