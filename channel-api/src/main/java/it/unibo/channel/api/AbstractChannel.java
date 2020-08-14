package it.unibo.channel.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize
public interface AbstractChannel {

    @Value.Parameter
    String getId();

    @Value.Parameter
    String getName();

    @Value.Parameter
    List<String> getUsers();
}
