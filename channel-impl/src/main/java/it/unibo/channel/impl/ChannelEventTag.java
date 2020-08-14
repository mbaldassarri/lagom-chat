package it.unibo.channel.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class ChannelEventTag {
    public static final AggregateEventTag<ChannelEvent> INSTANCE =
            AggregateEventTag.of(ChannelEvent.class);
}
