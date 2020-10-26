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
import com.tanbt.protocol.NotifyClient;
import com.tanbt.protocol.SendSharedData;
import com.tanbt.protocol.SendSharedDataRequest;
import com.tanbt.protocol.SetCollaborationServer;
import com.tanbt.protocol.SetSharedData;
import io.rsocket.RSocket;
import io.rsocket.util.DefaultPayload;

/**
 * The root actor of the application
 */
public class RootActor extends AbstractBehavior<MessageProtocol> {

    private String sharedData = "";
    private RSocket collaborationServerSocket;

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
            .onMessage(SetSharedData.class, this::setSharedData)
            .onMessage(SendSharedDataRequest.class, this::forwardGetRequest)
            .onMessage(SendSharedData.class, this::sendSharedData)
            .onMessage(SetCollaborationServer.class, this::setCollaborationServer)
            .build();
    }

    private Behavior<MessageProtocol> setCollaborationServer(SetCollaborationServer message) {
        this.collaborationServerSocket = message.getCollaborationServerSocket();
        return this;
    }


    private Behavior<MessageProtocol> sendSharedData(SendSharedData message) {
        message.getSubscriberActor().tell(new NotifyClient(sharedData));
        return this;
    }

    /**
     * Notice that `SendSharedData` doesn't contain any data, it just signals the subscriber actor to ask for shared data.
     */
    private Behavior<MessageProtocol> forwardGetRequest(SendSharedDataRequest message) {
        getContext().getChild(message.getClientId()).get().unsafeUpcast().tell(message);
        return this;
    }

    private Behavior<MessageProtocol> setSharedData(SetSharedData message) {
        // send new data to collaboration server
        if (collaborationServerSocket != null && !sharedData.equals(message.getNewData())) {
            Change change = new Change(sharedData, message.getNewData());
            collaborationServerSocket.fireAndForget(DefaultPayload.create(change.toJson(), "set")).block();
        }

        // notify subscribers
        sharedData = message.getNewData();
        System.out.println("Local shared data updated: " + sharedData);
        getContext().getChildren().forEach(subscriber -> subscriber.unsafeUpcast().tell(message));
        return this;
    }

    private Behavior<MessageProtocol> spawnNewSubscriber(CreateSubscriber message) {
        ActorRef<MessageProtocol> subscriber = getContext().spawn(SubscriberActor.create(), message.getClientId());
        message.setParentActor(getContext().getSelf());
        subscriber.tell(message);
        return this;
    }


    private Behavior<MessageProtocol> onPostStop() {
        getContext().getLog().info("Root actor stopped.");
        return this;
    }
}
