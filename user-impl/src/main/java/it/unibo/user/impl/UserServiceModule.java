package it.unibo.user.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import it.unibo.channel.api.ChannelService;
import it.unibo.user.api.UserService;

public class UserServiceModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindService(UserService.class, UserServiceImpl.class);
        bindClient(ChannelService.class);
    }
}
