package com.tanbt;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.tanbt.protocol.CreateSubscriber;
import com.tanbt.protocol.MessageProtocol;
import com.tanbt.protocol.NotifyClient;
import com.tanbt.protocol.SendSharedData;
import com.tanbt.protocol.SendSharedDataRequest;
import com.tanbt.protocol.SetSharedData;
import io.rsocket.RSocket;
import io.rsocket.util.DefaultPayload;

public class SubscriberActor extends AbstractBehavior<MessageProtocol> {

    private String clientID;
    private RSocket clientSocket;
    private ActorRef<MessageProtocol> parentActor;

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
            .onMessage(SendSharedDataRequest.class, this::askForSharedData)
            .onMessage(SetSharedData.class, this::notifyClients)
            .onMessage(NotifyClient.class, this::notifyClient)
            .build();
    }

    private Behavior<MessageProtocol> notifyClients(SetSharedData message) {
        clientSocket.fireAndForget(DefaultPayload.create("Data updated: " + message.getNewData())).block();
        return this;
    }

    private Behavior<MessageProtocol> notifyClient(NotifyClient message) {
        clientSocket.fireAndForget(DefaultPayload.create("Data updated: " + message.getData())).block();
        return this;
    }

    private Behavior<MessageProtocol> askForSharedData(SendSharedDataRequest message) {
        parentActor.tell(new SendSharedData(getContext().getSelf()));
        return this;
    }

    private Behavior<MessageProtocol> createSelf(CreateSubscriber message) {
        clientSocket = message.getClientSocket();
        clientID = message.getClientId();
        parentActor = message.getParentActor();
        clientSocket.fireAndForget(DefaultPayload.create("Client subscribed: " + clientID)).block();
        System.out.println("Client subscribe actor: " + getContext().getSelf());
        return this;
    }

}
