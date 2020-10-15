package com.tanbt;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.Signal;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.UUID;

/**
 * The root actor of the application
 */
public class RootActor extends AbstractBehavior<String> {

    public static Behavior<String> create() {
        return Behaviors.setup(RootActor::new);
    }

    private RootActor(ActorContext<String> context) {
        super(context);
        context.getLog().info("IoT Application started");
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder()
            .onSignal(PostStop.class, signal -> onPostStop())
            .onMessageEquals("subscribe", this::spawnNewSubscriber)
            .build();
    }

    private Behavior<String> spawnNewSubscriber() {
        String subscriberName = UUID.randomUUID().toString();
        ActorRef<String> firstRef = getContext().spawn(SubscriberActor.create(), subscriberName);
        firstRef.tell("printSelf");
        return Behaviors.same();
    }

    private <M extends Signal> Behavior<String> onPostStop() {
        getContext().getLog().info("IoT Application stopped");
        return this;
    }
}
