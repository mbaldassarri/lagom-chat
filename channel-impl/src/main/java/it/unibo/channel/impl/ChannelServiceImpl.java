package it.unibo.channel.impl;

import akka.Done;
import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import it.unibo.channel.api.ChannelEvent;
import it.unibo.channel.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class ChannelServiceImpl implements ChannelService {

    private static final Logger logger = LoggerFactory.getLogger(ChannelServiceImpl.class);

    private final PersistentEntityRegistry persistentEntities;
    private final CassandraSession db;

    @Inject
    public ChannelServiceImpl(PersistentEntityRegistry persistentEntities, ReadSide readSide, CassandraSession db) {
        this.persistentEntities = persistentEntities;
        this.db = db;

        persistentEntities.register(ChannelEntity.class);
        readSide.register(ChannelEventProcessor.class);

    }

    @Override
    public ServiceCall<NotUsed, List<Channel>> getChannels() {
        return (req) -> {
            logger.info("Looking up all Channel");
            CompletionStage<List<Channel>> result = db.selectAll("SELECT id, name, users FROM channel_keyspace.channeldb")
                    .thenApply(rows -> {
                        List<Channel> channelModels = rows.stream().map(row -> Channel.of(
                                row.getString("id"),
                                row.getString("name"),
                                row.getList("users", String.class)))
                                .collect(Collectors.toList());
                        return Collections.unmodifiableList(channelModels);
                    });
            return result;
        };
    }

    @Override
    public ServiceCall<CreateChannelRequest, CreateChannelResponse> createChannel() {
        return request -> {
            logger.info("Creating a brand new Channel: {}", request);
            UUID uuid = UUID.randomUUID();
            return convertErrors(persistentEntities
                    .refFor(ChannelEntity.class, uuid.toString())
                    .ask(CreateChannel.of(request)));
        };
    }

    @Override
    public ServiceCall<CreateChannelRequest, Done> joinChannel(String channelId) {
        return request -> {
            return convertErrors(persistentEntities
                    .refFor(ChannelEntity.class, channelId)
                    .ask(UpdateChannel.of(request)));
        };
    }

    @Override
    public Topic<ChannelEvent> createdChannelTopic() {
        return TopicProducer.singleStreamWithOffset(offset -> {
            return (Source<Pair<ChannelEvent, Offset>, ?>) persistentEntities
                    .eventStream(ChannelEventTag.INSTANCE, offset)
                    .filter(eventOffSet -> eventOffSet.first() instanceof ChannelCreated)
                    .map(this::convertCreatedChannel);
        });
    }

    @Override
    public Topic<ChannelEvent> updatedChannelTopic() {
        return TopicProducer.singleStreamWithOffset(offset -> {
            return persistentEntities
                    .eventStream(ChannelEventTag.INSTANCE, offset)
                    .filter(eventOffSet -> eventOffSet.first() instanceof ChannelUpdated)
                    .map(this::convertUpdatedChannel);
        });
    }

    private Pair<ChannelEvent, Offset> convertCreatedChannel(Pair<it.unibo.channel.impl.ChannelEvent, Offset> pair) {
        Channel channelModel = ((ChannelCreated) pair.first()).getChannel();
        logger.info("Converting ChannelEvent for creation: " + channelModel);
        return new Pair<>(new ChannelEvent.ChannelCreated(channelModel.getId(), channelModel.getName(), channelModel.getUsers()), pair.second());
    }

    private Pair<ChannelEvent, Offset> convertUpdatedChannel(Pair<it.unibo.channel.impl.ChannelEvent, Offset> pair) {
        Channel channel = ((ChannelUpdated) pair.first()).getChannel();
        logger.info("Converting ChannelEvent for updating: " + channel);
        return new Pair<>(new ChannelEvent.ChannelUpdated(channel.getId(), channel.getName(), channel.getUsers()), pair.second());
    }

    private <T> CompletionStage<T> convertErrors(CompletionStage<T> future) {
        return future.exceptionally(ex -> {
            if (ex instanceof ChannelException) {
                throw new BadRequest(ex.getMessage());
            } else {
                throw new BadRequest("Error updating Channel");
            }
        });
    }
}
