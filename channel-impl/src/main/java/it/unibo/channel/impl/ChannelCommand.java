package it.unibo.channel.impl;


import akka.Done;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import it.unibo.channel.api.Channel;
import it.unibo.channel.api.CreateChannelRequest;
import it.unibo.channel.api.CreateChannelResponse;
import org.immutables.value.Value;

import java.util.Optional;

public interface ChannelCommand extends Jsonable {

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractCreateChannel extends ChannelCommand, CompressedJsonable, PersistentEntity.ReplyType<CreateChannelResponse> {

        @Value.Parameter
        CreateChannelRequest getCreateChannelRequest();
    }

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractUpdateChannel extends ChannelCommand, CompressedJsonable, PersistentEntity.ReplyType<Done> {

        @Value.Parameter
        CreateChannelRequest getUpdateChannelRequest();
    }

    @Value.Immutable(singleton = true)
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractGetChannel extends ChannelCommand, CompressedJsonable, PersistentEntity.ReplyType<GetChannelReply> {
    }

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize
    interface AbstractGetChannelReply extends Jsonable {

        @Value.Parameter
        Optional<Channel> getChannel();
    }
}
