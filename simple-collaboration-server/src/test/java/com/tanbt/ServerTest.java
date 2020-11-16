package com.tanbt;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests how the rsocket server responses to subscribers.
 * 1. Start the server
 * 2. Create a rsocket connector that connects to the server
 * 3. The connector sends a request
 * 4. Use {@link reactor.test.StepVerifier} to test the responses
 */
public class ServerTest {

    private static InternalServer server;
    private static RSocket connector;

    @BeforeClass
    public static void setUp() {
        server = new InternalServer(Server.HOST, Server.PORT);
        server.start();

        connector = RSocketConnector.connectWith(
            TcpClientTransport.create(Server.HOST, Server.PORT)
        ).block();
    }

    @AfterClass
    public static void tearDown() {
        connector.dispose();
        server.stop();
    }

    @Test
    public void test_subscribeApp() {
        Mono<Payload> payloadMono =
            connector.requestResponse(DefaultPayload.create("client-123", "sub"));
        StepVerifier.create(payloadMono)
            .consumeNextWith(pl -> {
                assertEquals("serverUpdates", pl.getMetadataUtf8());
                assertEquals("Init data", pl.getDataUtf8());
            }).expectComplete().verify();

        assertEquals(1, server.getAppsRSockets().size());
    }
}
