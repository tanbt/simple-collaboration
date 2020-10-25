package com.tanbt;

import akka.actor.typed.ActorSystem;
import com.tanbt.protocol.CreateSubscriber;
import com.tanbt.protocol.MessageProtocol;
import com.tanbt.protocol.SendSharedDataRequest;
import com.tanbt.protocol.SetSharedData;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import java.io.IOException;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

/**
 * The web server
 */
public class App {

    private static int PORT = 7000;

    private static ActorSystem<MessageProtocol> appRootActor;

    public static void main(String[] args) throws IOException {
        appRootActor = ActorSystem.create(RootActor.create(), "webapp");

        if (args.length > 0) {
            PORT = Integer.valueOf(args[0]);
        }

        // This socket acceptor is to handle client requests like what a web server does.
        // It doesn't mean to modify the app's state (shared data) directly,
        // but it forwards the request from a client to RootActor then to SubscriberActor which communicates to that client.
        // For example, the request maybe a "set data" request, then SubscriberActor will talk to RootActor to modify the shared data.

        // This is a redundant round-trip, as the shared data can be modified from RootActor without forwarding to SubscriberActor,
        // which can be simplified by a real web server (which allows pushing data to client).
        SocketAcceptor socketAcceptor = (setup, sendingSocket) -> Mono.just(new RSocket() {
            @Override
            public Mono<Void> fireAndForget(Payload payload) {
                switch (payload.getMetadataUtf8()) {
                    case "subscribe":
                        subscribe(payload, sendingSocket);
                        break;
                    case "get":
                        get(payload);
                        break;
                    case "set":
                        set(payload.getDataUtf8());
                        break;
                    default:
                        System.out.println("Server received: " + payload.getDataUtf8());
                }
                return Mono.empty();
            }
        });

        Disposable server = RSocketServer.create(socketAcceptor)
            .bind(TcpServerTransport.create("localhost", PORT))
            .subscribe();
        System.out.println("Web server is running at port " + PORT);

        System.in.read();
        server.dispose();
    }

    private static void set(String newData) {
        appRootActor.tell(new SetSharedData(newData));
    }

    private static void get(Payload payload) {
        String clientId = payload.getDataUtf8();
        appRootActor.tell(new SendSharedDataRequest(clientId));
    }

    private static void subscribe(Payload payload, RSocket clientSocket) {
        appRootActor.tell(new CreateSubscriber(payload.getDataUtf8(), clientSocket));
    }
}
