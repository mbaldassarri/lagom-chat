package it.unibo.channel.impl;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ChannelEventProcessor extends ReadSideProcessor<ChannelEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ChannelEventProcessor.class);

    private final CassandraSession session;
    private final CassandraReadSide readSide;

    private PreparedStatement writeChannel = null; // initialized in prepare

    @Inject
    public ChannelEventProcessor(CassandraSession session, CassandraReadSide readSide) {
        this.session = session;
        this.readSide = readSide;
    }

    private void setWriteChannel(PreparedStatement writeChannel) {
        this.writeChannel = writeChannel;
    }

    private CompletionStage<Done> prepareCreateTables(CassandraSession session) {
        logger.info("Creating Cassandra tables...");
        session.executeWrite("CREATE KEYSPACE IF NOT EXISTS channel_keyspace WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1};");
        return session.executeCreateTable("CREATE TABLE IF NOT EXISTS channel_keyspace.channeldb " +
                "(id text, name text, users list<text>, PRIMARY KEY (id));");
    }

    private CompletionStage<Done> prepareWriteChannel(CassandraSession session) {
        logger.info("Inserting into read-side channel_keyspace.table channeldb...");
        return session.prepare("INSERT INTO channel_keyspace.channeldb (id, name, users) VALUES (?, ?, ?)")
                .thenApply(ps -> {
                    setWriteChannel(ps);
                    return Done.getInstance();
                });
    }

    /**
     * Write a persistent event into the read-side optimized database.
     */
    private CompletionStage<List<BoundStatement>> processChannelCreated(ChannelCreated event) {
        BoundStatement bindWriteChannel = writeChannel.bind();
        bindWriteChannel.setString("id", event.getChannel().getId());
        bindWriteChannel.setString("name", event.getChannel().getName());
        bindWriteChannel.setList("users", event.getChannel().getUsers());
        logger.info("Persisted Channel {}", event.getChannel());
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteChannel));
    }

    private CompletionStage<List<BoundStatement>> processChannelUpdated(ChannelUpdated event) {
        BoundStatement bindWriteChannel = writeChannel.bind();
        bindWriteChannel.setString("id", event.getChannel().getId());
        bindWriteChannel.setString("name", event.getChannel().getName());
        bindWriteChannel.setList("users", event.getChannel().getUsers());
        logger.info("Persisted UPDATE Channel {}", event.getChannel());
        return CassandraReadSide.completedStatements(Collections.singletonList(bindWriteChannel));
    }

    @Override
    public ReadSideHandler<ChannelEvent> buildHandler() {
        CassandraReadSide.ReadSideHandlerBuilder<ChannelEvent> builder = readSide.builder("Channel_offset");
        builder.setGlobalPrepare(() -> prepareCreateTables(session));
        builder.setPrepare(tag -> prepareWriteChannel(session));
        logger.info("Setting up read-side event handlers...");
        builder.setEventHandler(ChannelCreated.class, this::processChannelCreated);
        builder.setEventHandler(ChannelUpdated.class, this::processChannelUpdated);
        return builder.build();
    }

    @Override
    public PSequence<AggregateEventTag<ChannelEvent>> aggregateTags() {
        return TreePVector.singleton(ChannelEventTag.INSTANCE);
    }
}