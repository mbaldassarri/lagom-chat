package it.unibo.channel.impl;

import akka.Done;
import akka.stream.javadsl.Flow;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import it.unibo.channel.api.Channel;
import it.unibo.channel.api.ChannelService;
import it.unibo.channel.api.CreateChannelRequest;
import it.unibo.channel.api.CreateChannelResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

public class ChannelServiceTest {
    private static ServiceTest.TestServer server;
    private static final long TIME = 3;

    @BeforeClass
    public static void setUp() {
        server = ServiceTest.startServer(defaultSetup().withCassandra(true));
    }

    @AfterClass
    public static void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void createChannel_Should_GenerateId() throws Exception {
        // given
        String channelName = "Music";
        ChannelService service = server.client(ChannelService.class);
        CreateChannelRequest createChannelRequest = CreateChannelRequest.builder().name(channelName).build();
        CreateChannelResponse response = service
                .createChannel()
                .invoke(createChannelRequest)
                .toCompletableFuture()
                .get(TIME, SECONDS);

        // then
        assertNotNull(response.getId());
        assertTrue("Id is longer than 10 chars", response.getId().length() > 10);
        assertTrue("Id contains dashes -", response.getId().contains("-"));
    }

    @Test
    public void getChannels_Should_Return_Channel_List() throws Exception {
        // given
        String channelName = "Tech";
        ChannelService service = server.client(ChannelService.class);
        CreateChannelRequest createChannelRequest = CreateChannelRequest.builder().name(channelName).build();
        CreateChannelResponse createChannelResponse = service
                .createChannel()
                .invoke(createChannelRequest)
                .toCompletableFuture()
                .get(TIME, SECONDS);

        // when
        /*
        * Readside needs some time to get updated. As discussed, Event Sourcing introduces eventual consistency.
        * That is the reason why the test is waiting 100 seconds before stopping looking up the read-side table.
        * */
        ServiceTest.eventually(FiniteDuration.create(100, SECONDS), FiniteDuration.create(1000, MILLISECONDS), () -> {
            List<Channel> response = service
                    .getChannels()
                    .invoke()
                    .toCompletableFuture()
                    .get(TIME, SECONDS);

            // then
            assertTrue("Channel list is not empty", response.size() > 0);
            assertTrue(String.format("Contains channel %s", channelName), response.stream().anyMatch(i -> i.getName().equals(channelName)));
            assertFalse(String.format("Doesn't contain item %s", "OtherName"), response.stream().anyMatch(i -> i.getId().equals("OtherName")));
        });
    }

    @Test
    public void created_channel_topic_should_publish() throws Exception {
        Logger logger = LoggerFactory.getLogger(ChannelServiceTest.class);
        String channelName = "Art";
        ChannelService service = server.client(ChannelService.class);
        CreateChannelRequest createChannelRequest = CreateChannelRequest.builder().name(channelName).build();
        CreateChannelResponse createChannelResponse = service
                .createChannel()
                .invoke(createChannelRequest)
                .toCompletableFuture()
                .get(TIME, SECONDS);

        //creating subscriber to get the event message about created channel
        service.createdChannelTopic().subscribe()
                .atLeastOnce(Flow.fromFunction((it.unibo.channel.api.ChannelEvent channelEvent) -> {
                    logger.info("TEST -> " + channelEvent);
                    ChannelCreated created = ChannelCreated.builder().channel(Channel.of(createChannelResponse.getId(), channelName, new ArrayList<>())).build();
                    assertEquals(created, channelEvent);
                    return Done.getInstance();
                }));
    }

    @Test
    public void updated_channel_topic_should_publish() throws Exception {
        Logger logger = LoggerFactory.getLogger(ChannelServiceTest.class);
        String channelName = "Philosophy";
        ChannelService service = server.client(ChannelService.class);
        CreateChannelRequest createChannelRequest = CreateChannelRequest.builder().name(channelName).build();
        CreateChannelResponse createChannelResponse = service
                .createChannel()
                .invoke(createChannelRequest)
                .toCompletableFuture()
                .get(TIME, SECONDS);

        ArrayList<String> users = new ArrayList<>(1);
        users.add("Alice");
        CreateChannelRequest updateChannelRequest = CreateChannelRequest.builder().name(channelName).users(users).build();
        Done updateChannelResponse = service
                .joinChannel(createChannelResponse.getId())
                .invoke(updateChannelRequest)
                .toCompletableFuture()
                .get(TIME, SECONDS);

        //creating subscriber to get the event message about created channel and updated channel
        service.createdChannelTopic().subscribe()
                .atLeastOnce(Flow.fromFunction((it.unibo.channel.api.ChannelEvent channelEvent) -> {
                    ChannelCreated created = ChannelCreated.builder().channel(Channel.of(createChannelResponse.getId(), channelName, new ArrayList<>())).build();
                    assertEquals(created, channelEvent);
                    return Done.getInstance();
                }));
        service.createdChannelTopic().subscribe()
                .atLeastOnce(Flow.fromFunction((it.unibo.channel.api.ChannelEvent channelEvent) -> {
                    ChannelUpdated created = ChannelUpdated.builder().channel(Channel.of(createChannelResponse.getId(), channelName, users)).build();
                    assertEquals(created, channelEvent);
                    return Done.getInstance();
                }));
    }
}
