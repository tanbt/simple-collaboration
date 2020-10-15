package com.tanbt;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.tanbt.protocol.CreateSubscriber;
import com.tanbt.protocol.MessageProtocol;
import com.tanbt.protocol.PrintSelf;

/**
 * The root actor of the application
 */
public class RootActor extends AbstractBehavior<MessageProtocol> {

    public static Behavior<MessageProtocol> create() {
        return Behaviors.setup(RootActor::new);
    }

    private RootActor(ActorContext<MessageProtocol> context) {
        super(context);
        context.getLog().info("Root actor started.");
    }

    @Override
    public Receive<MessageProtocol> createReceive() {
        return newReceiveBuilder()
            .onSignal(PostStop.class, signal -> onPostStop())
            .onMessage(CreateSubscriber.class, this::spawnNewSubscriber)
            .build();
    }


    private Behavior<MessageProtocol> spawnNewSubscriber(CreateSubscriber message) {
        ActorRef<MessageProtocol> subscriber = getContext().spawn(SubscriberActor.create(), message.getClientId());
        subscriber.tell(new PrintSelf());
        return this;
    }

    private Behavior<MessageProtocol> onPostStop() {
        getContext().getLog().info("Root actor stopped.");
        return this;
    }
}
