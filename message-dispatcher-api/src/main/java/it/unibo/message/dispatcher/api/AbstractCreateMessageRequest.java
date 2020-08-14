package it.unibo.message.dispatcher.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractCreateMessageRequest extends Jsonable {

    @Value.Parameter
    String getSender();

    @Value.Parameter
    String getChannel();

    @Value.Parameter
    String getMessage();
}
