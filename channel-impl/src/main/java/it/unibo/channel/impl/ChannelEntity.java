package it.unibo.channel.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import it.unibo.channel.api.Channel;
import it.unibo.channel.api.CreateChannelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

public class ChannelEntity extends PersistentEntity<ChannelCommand, ChannelEvent, ChannelState> {

    private static final Logger logger = LoggerFactory.getLogger(ChannelEntity.class);

    @Override
    public Behavior initialBehavior(Optional<ChannelState> snapshotState) {
        logger.info("Setting up initialBehaviour with snapshotState = {}", snapshotState);
        BehaviorBuilder b = newBehaviorBuilder(snapshotState.orElse(
                ChannelState.of(Optional.empty(), LocalDateTime.now()))
        );

        // Create command handler
        b.setCommandHandler(CreateChannel.class, (cmd, ctx) -> {
            if (state().getChannel().isPresent()) {
                ctx.invalidCommand(String.format("Channel %s is already created", entityId()));
                return ctx.done();
            } else {
                Channel channelModel = Channel.of(entityId(),
                        cmd.getCreateChannelRequest().getName(),
                        cmd.getCreateChannelRequest().getUsers());
                final ChannelCreated channelCreated = ChannelCreated.builder().channel(channelModel).build();
                logger.info("Processed CreateChannel command into ChannelCreated event {}", channelCreated);
                return ctx.thenPersist(channelCreated, evt ->
                        ctx.reply(CreateChannelResponse.of(entityId())));
            }
        });

        // Update command handler
        b.setCommandHandler(UpdateChannel.class, (cmd, ctx) -> {
            if (state().getChannel().isPresent()) {
                Channel channelModel = Channel.of(entityId(),
                        cmd.getUpdateChannelRequest().getName(),
                        cmd.getUpdateChannelRequest().getUsers());
                final ChannelUpdated channelUpdated = ChannelUpdated.builder().channel(channelModel).build();
                logger.info("Processed UpdateChannel command into ChannelUpdated event {}", channelUpdated);
                return ctx.thenPersist(channelUpdated, evt -> ctx.reply(Done.getInstance()));
            } else {
                ctx.commandFailed( new ChannelException(String.format("Channel %s does not exist. " +
                        "Cannot update a non existing channel", entityId())));
                return ctx.done();
            }
        });

        // Register event handler
        b.setEventHandler(ChannelCreated.class, evt -> {
                    logger.info("Processed ChannelCreated event, updated Channel state");
                    return state().withChannel(evt.getChannel())
                            .withTimestamp(LocalDateTime.now());
                }
        );

        // Register event handler
        b.setEventHandler(ChannelUpdated.class, evt -> {
                logger.info("Processed ChannelUpdated event, updated Channel state");
                ChannelState state = state().withChannel(evt.getChannel()).withTimestamp(LocalDateTime.now());
                return state;
            }
        );

        // Register read-only handler eg a handler that doesn't result in events being created
        b.setReadOnlyCommandHandler(GetChannel.class,
                (cmd, ctx) -> {
                    logger.info("Processed GetChannel command, returned Channel");
                    ctx.reply(GetChannelReply.of(state().getChannel()));
                }
        );
        return b.build();
    }
}
