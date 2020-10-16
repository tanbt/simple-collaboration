package com.tanbt;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.tanbt.protocol.CreateSubscriber;
import com.tanbt.protocol.MessageProtocol;
import com.tanbt.protocol.NotifySubscriber;
import io.rsocket.RSocket;
import io.rsocket.util.DefaultPayload;

public class SubscriberActor extends AbstractBehavior<MessageProtocol> {

    private String clientID;
    private RSocket clientSocket;

    static Behavior<MessageProtocol> create() {
        return Behaviors.setup(SubscriberActor::new);
    }

    private SubscriberActor(ActorContext<MessageProtocol> context) {
        super(context);
    }

    @Override
    public Receive<MessageProtocol> createReceive() {
        return newReceiveBuilder()
            .onMessage(CreateSubscriber.class, this::createSelf)
            .onMessage(NotifySubscriber.class, this::notifyClient)
            .build();
    }

    private Behavior<MessageProtocol> notifyClient(NotifySubscriber message) {
        clientSocket.fireAndForget(DefaultPayload.create("Data updated: " + message.getNewData())).block();
        return this;
    }

    private Behavior<MessageProtocol> createSelf(CreateSubscriber message) {
        clientSocket = message.getClientSocket();
        clientID = message.getClientId();
        System.out.println("Subscriber created: " + getContext().getSelf());
        return this;
    }

}
