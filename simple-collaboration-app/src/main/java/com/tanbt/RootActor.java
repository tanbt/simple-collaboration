package com.tanbt;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.Signal;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

/**
 * The root actor of the application
 */
public class RootActor extends AbstractBehavior<Void> {

    public static Behavior<Void> create() {
        return Behaviors.setup(RootActor::new);
    }

    private RootActor(ActorContext<Void> context) {
        super(context);
        context.getLog().info("IoT Application started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private <M extends Signal> Behavior<Void> onPostStop() {
        getContext().getLog().info("IoT Application stopped");
        return this;
    }
}
