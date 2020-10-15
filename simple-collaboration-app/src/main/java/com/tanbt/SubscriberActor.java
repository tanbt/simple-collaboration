package com.tanbt;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.tanbt.protocol.MessageProtocol;
import com.tanbt.protocol.PrintSelf;

public class SubscriberActor extends AbstractBehavior<MessageProtocol> {

    static Behavior<MessageProtocol> create() {
        return Behaviors.setup(SubscriberActor::new);
    }

    private SubscriberActor(ActorContext<MessageProtocol> context) {
        super(context);
    }

    @Override
    public Receive<MessageProtocol> createReceive() {
        return newReceiveBuilder()
            .onMessage(PrintSelf.class, this::printSelf)
            .build();
    }

    private Behavior<MessageProtocol> printSelf(MessageProtocol message) {
        System.out.println("Subscriber: " + getContext().getSelf());
        return this;
    }

}
