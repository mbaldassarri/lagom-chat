package it.unibo.channel.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import it.unibo.channel.api.Channel;
import org.immutables.value.Value;

import java.time.Instant;

public interface ChannelEvent extends Jsonable, AggregateEvent<ChannelEvent> {

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractChannelCreated extends ChannelEvent {
        @Override
        default AggregateEventTag<ChannelEvent> aggregateTag() {
            return ChannelEventTag.INSTANCE;
        }

        @Value.Parameter
        Channel getChannel();

        @Value.Default
        default Instant getTimestamp() {
            return Instant.now();
        }
    }

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractChannelUpdated extends ChannelEvent {
        @Override
        default AggregateEventTag<ChannelEvent> aggregateTag() {
            return ChannelEventTag.INSTANCE;
        }

        @Value.Parameter
        Channel getChannel();

        @Value.Default
        default Instant getTimestamp() {
            return Instant.now();
        }
    }
}