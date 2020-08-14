package it.unibo.channel.impl;

import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import it.unibo.channel.api.Channel;
import it.unibo.channel.api.CreateChannelRequest;
import it.unibo.channel.api.CreateChannelResponse;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

public class ChannelEntityTest {

    private static ActorSystem system;
    private static String channelName;
    private static List<String> joinedUsers;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("ChannelEntityTest");
        channelName = "BigData";
        joinedUsers = new ArrayList<>();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void createChannel_Should_Create_ChannelCreatedEvent() {
        // given driver
        String entityId = UUID.randomUUID().toString();

        PersistentEntityTestDriver<ChannelCommand, ChannelEvent, ChannelState> driver =
                new PersistentEntityTestDriver<>(system, new ChannelEntity(), entityId);

        // when
        PersistentEntityTestDriver.Outcome<ChannelEvent, ChannelState> outcome = driver.run(
                CreateChannel.of(CreateChannelRequest.of(channelName, joinedUsers)));

        // then
        Assert.assertTrue(outcome.getReplies().get(0) instanceof CreateChannelResponse);
        Assert.assertEquals(CreateChannelResponse.of(entityId), outcome.getReplies().get(0));
        ChannelCreated channelCreated = ((ChannelCreated) outcome.events().get(0));
        Assert.assertEquals(entityId, channelCreated.getChannel().getId());
        Assert.assertEquals(channelName, channelCreated.getChannel().getName());
        Assert.assertEquals(joinedUsers, channelCreated.getChannel().getUsers());
        Assert.assertNotNull(((ChannelCreated) outcome.events().get(0)).getTimestamp());
        Assert.assertEquals(Collections.emptyList(), driver.getAllIssues());
    }

    @Test
    public void joinChannel_Should_Add_New_User_To_Channel() {
        // given driver
        String entityId = UUID.randomUUID().toString();
        List<String> updatedUserList = new ArrayList<>(1);
        updatedUserList.add("Bob");
        PersistentEntityTestDriver<ChannelCommand, ChannelEvent, ChannelState> driver =
                new PersistentEntityTestDriver<>(system, new ChannelEntity(), entityId);

        // when
        // we need to create a channel before updating it
        PersistentEntityTestDriver.Outcome<ChannelEvent, ChannelState> createOutcome = driver.run(
                CreateChannel.of(CreateChannelRequest.of(channelName, joinedUsers)));
        PersistentEntityTestDriver.Outcome<ChannelEvent, ChannelState> outcome = driver.run(
                UpdateChannel.of(CreateChannelRequest.of(channelName, updatedUserList)));

        // then
        Assert.assertTrue(outcome.getReplies().get(0) instanceof Done);
        Assert.assertEquals(Done.getInstance(), outcome.getReplies().get(0));
        ChannelUpdated channelUpdated = ((ChannelUpdated) outcome.events().get(0));
        Assert.assertEquals(entityId, channelUpdated.getChannel().getId());
        Assert.assertEquals(channelName, channelUpdated.getChannel().getName());
        Assert.assertEquals(updatedUserList, channelUpdated.getChannel().getUsers());
        Assert.assertNotNull(((ChannelUpdated) outcome.events().get(0)).getTimestamp());
        Assert.assertEquals(Collections.emptyList(), driver.getAllIssues());
    }

    @Test
    public void addExistingChannel_Should_ThrowInvalidCommandException() {
        // given
        UUID id = UUID.randomUUID();
        PersistentEntityTestDriver<ChannelCommand, ChannelEvent, ChannelState> driver = new PersistentEntityTestDriver<>(
                system, new ChannelEntity(), id.toString());
        driver.run(CreateChannel.of(CreateChannelRequest.of(channelName, joinedUsers)));

        // when
        PersistentEntityTestDriver.Outcome<ChannelEvent, ChannelState> outcome = driver.run(
                CreateChannel.of(CreateChannelRequest.of(channelName, joinedUsers)));

        // then
        Assert.assertEquals(PersistentEntity.InvalidCommandException.class, outcome.getReplies().get(0).getClass());
        Assert.assertEquals(Collections.emptyList(), outcome.events());
        Assert.assertEquals(Collections.emptyList(), driver.getAllIssues());
    }

    @Test
    public void createChannelWithoutName_Should_ThrowNullPointerException() {
        // given
        UUID id = UUID.randomUUID();
        PersistentEntityTestDriver<ChannelCommand, ChannelEvent, ChannelState> driver =
                new PersistentEntityTestDriver<>(system, new ChannelEntity(), id.toString());

        // when
        try {
            driver.run(CreateChannel.of(CreateChannelRequest.of(null, joinedUsers)));
            Assert.fail();
        } catch (NullPointerException e) {
            // then
            Assert.assertEquals("name", e.getMessage());
            Assert.assertEquals(Collections.emptyList(), driver.getAllIssues());
        }
    }

    @Test
    public void createChannelWithoutUsers_Should_ThrowNullPointerException() {
        // given
        UUID id = UUID.randomUUID();
        PersistentEntityTestDriver<ChannelCommand, ChannelEvent, ChannelState> driver =
                new PersistentEntityTestDriver<>(system, new ChannelEntity(), id.toString());

        // when
        try {
            driver.run(CreateChannel.of(CreateChannelRequest.of(channelName, null)));
            Assert.fail();
        } catch (NullPointerException e) {
            // then
            Assert.assertEquals(null, e.getMessage());
            Assert.assertEquals(Collections.emptyList(), driver.getAllIssues());
        }
    }

    @Test
    public void getChannels_Should_ReturnGetChannelReply() {
        // given
        String entityId = UUID.randomUUID().toString();
        PersistentEntityTestDriver<ChannelCommand, ChannelEvent, ChannelState> driver =
                new PersistentEntityTestDriver<>(system, new ChannelEntity(), entityId);
        driver.run(CreateChannel.of(CreateChannelRequest.of(channelName, joinedUsers)));
        Channel chair = Channel.of(entityId, channelName, joinedUsers);

        // when
        PersistentEntityTestDriver.Outcome<ChannelEvent, ChannelState> outcome = driver.run(GetChannel.of());

        // then
        Assert.assertEquals(GetChannelReply.of(Optional.of(chair)), outcome.getReplies().get(0));
        Assert.assertEquals(Collections.emptyList(), outcome.events());
        Assert.assertEquals(Collections.emptyList(), driver.getAllIssues());
    }
}
