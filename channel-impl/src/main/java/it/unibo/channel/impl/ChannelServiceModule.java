package it.unibo.channel.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import it.unibo.channel.api.ChannelService;

public class ChannelServiceModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindService(ChannelService.class, ChannelServiceImpl.class);
    }
}
