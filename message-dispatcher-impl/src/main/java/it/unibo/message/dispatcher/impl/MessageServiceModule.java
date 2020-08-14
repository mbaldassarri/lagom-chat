package it.unibo.message.dispatcher.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import it.unibo.channel.api.ChannelService;
import it.unibo.message.dispatcher.api.MessageService;

public class MessageServiceModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindService(MessageService.class, MessageServiceImpl.class);
        bindClient(ChannelService.class);
        // Bind the subscriber eagerly to ensure it starts up
        bind(ChatMessageEventsSubscriber.class).asEagerSingleton();
    }
}
