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
import com.tanbt.protocol.SetSharedDataFromClient;
import com.tanbt.protocol.SetSharedDataFromServer;
import io.rsocket.RSocket;
import io.rsocket.util.DefaultPayload;

/**
 * The root actor of the application
 */
public class RootActor extends AbstractBehavior<MessageProtocol> {

    private volatile String sharedData = "";
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
            .onMessage(SetSharedDataFromServer.class, this::setSharedDataFromServer) // keep this order because SetSharedDataFromServer extends SetSharedDataFromClient
            .onMessage(SetSharedDataFromClient.class, this::setSharedDataFromClient)
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

    private Behavior<MessageProtocol> setSharedDataFromClient(SetSharedDataFromClient message) {
        if (!sharedData.equals(message.getNewData())) {
            String change = new Change(sharedData, message.getNewData()).toJson();
            sharedData = message.getNewData();

            // Notify collaboration server
            if (collaborationServerSocket != null) {
                collaborationServerSocket.fireAndForget(DefaultPayload.create(change, "set")).block();
            }

            // Notify other subscribers
            System.out.println("Local data updated from client: " + sharedData);
            getContext().getChildren().forEach(subscriber -> subscriber.unsafeUpcast().tell(message));
        }
        return this;
    }

    private Behavior<MessageProtocol> setSharedDataFromServer(SetSharedDataFromServer message) {
        if (!sharedData.equals(message.getNewData())) {
            // No need to notify back to server. Notify other subscribers only.
            sharedData = message.getNewData();
            System.out.println("Local shared data updated from server: " + sharedData);
            getContext().getChildren().forEach(subscriber -> subscriber.unsafeUpcast().tell(message));
        }
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
