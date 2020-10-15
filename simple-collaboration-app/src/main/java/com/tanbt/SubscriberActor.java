package com.tanbt;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class SubscriberActor extends AbstractBehavior<String> {

    static Behavior<String> create() {
        return Behaviors.setup(SubscriberActor::new);
    }

    private SubscriberActor(ActorContext<String> context) {
        super(context);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onMessageEquals("printSelf", this::printSelf).build();
    }

    private Behavior<String> printSelf() {
        System.out.println("Second: " + getContext().getSelf());
        return this;
    }

}
