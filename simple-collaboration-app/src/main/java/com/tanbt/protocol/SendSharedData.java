package com.tanbt.protocol;

import akka.actor.typed.ActorRef;

public class SendSharedData implements MessageProtocol {
    private final ActorRef<MessageProtocol> subscriberActor;

    public SendSharedData(ActorRef<MessageProtocol> subscriberActor) {
        this.subscriberActor = subscriberActor;
    }

    public ActorRef<MessageProtocol> getSubscriberActor() {
        return subscriberActor;
    }
}
