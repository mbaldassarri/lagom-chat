package it.unibo.channel.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize
public class ChannelException extends RuntimeException implements Jsonable {
    public final String message;

    @JsonCreator
    public ChannelException(String message) {
        super(message);
        this.message = message;
    }
}
