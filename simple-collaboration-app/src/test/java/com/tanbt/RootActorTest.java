package com.tanbt;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import com.tanbt.protocol.CreateSubscriber;
import com.tanbt.protocol.MessageProtocol;
import static org.junit.Assert.assertEquals;
import org.junit.ClassRule;
import org.junit.Test;

public class RootActorTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void test_spawnNewSubscriber() {
        ActorRef<MessageProtocol> rootActor = testKit.spawn(RootActor.create());

        // A simple testing way is to pass a reference of a `replyTo` actor.
        // Otherwise, see https://doc.akka.io/docs/akka/2.6.9/typed/actor-discovery.html#receptionist.
        TestProbe<ActorRef> rootActorProbe = testKit.createTestProbe(ActorRef.class);

        rootActor.tell(new CreateSubscriber("client-id-1", null, rootActorProbe.getRef()));
        assertEquals(rootActor.path().toString() + "/client-id-1",
            rootActorProbe.receiveMessage().path().toString());
    }

}
