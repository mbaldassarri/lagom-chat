package it.unibo.channel.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractCreateChannelResponse extends Jsonable {

    @Value.Parameter
    String getId();
}
