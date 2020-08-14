package it.unibo.channel.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import it.unibo.channel.api.Channel;
import org.immutables.value.Value;
import java.time.LocalDateTime;
import java.util.Optional;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractChannelState extends Jsonable {

    @Value.Parameter
    Optional<Channel> getChannel();

    @Value.Parameter
    LocalDateTime getTimestamp();
}
