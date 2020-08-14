package it.unibo.message.dispatcher.impl;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.pubsub.PubSubRef;
import com.lightbend.lagom.javadsl.pubsub.PubSubRegistry;
import com.lightbend.lagom.javadsl.pubsub.TopicId;
import it.unibo.channel.api.ChannelService;
import it.unibo.message.dispatcher.api.CreateMessageRequest;
import it.unibo.message.dispatcher.api.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class MessageServiceImpl implements MessageService {

    private final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final ChannelService channelService;
    private final PubSubRegistry pubSubRegistry;
    private final String messageTopic = "message";

    @Inject
    public MessageServiceImpl(ChannelService channelService, PubSubRegistry topics) {
        this.pubSubRegistry = topics;
        this.channelService = channelService;
    }

    // Using Publish-subscribe mechanism to publish a new message.
    // Event Sourcing layer is not involved
    @Override
    public ServiceCall<CreateMessageRequest, Done> publishMessage() {
        return request -> {
            PubSubRef<CreateMessageRequest> topic = pubSubRegistry
                    .refFor(TopicId.of(CreateMessageRequest.class, messageTopic));
            topic.publish(request);
            return CompletableFuture.completedFuture(Done.getInstance());
        };
    }

    // Using Publish-subscribe mechanism to listen to published Messages from the User.
    // Event Sourcing layer is not involved
    @Override
    public ServiceCall<NotUsed, Source<CreateMessageRequest, ?>> getPublishedMessageStream() {
        return request -> {
            final PubSubRef<CreateMessageRequest> topic =
                    pubSubRegistry.refFor(TopicId.of(CreateMessageRequest.class, messageTopic));
            return CompletableFuture.completedFuture(topic.subscriber());
        };
    }
}
