package it.unibo.channel.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.Method;

import java.util.List;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

public interface ChannelService extends Service {

    String CREATE_TOPIC_NAME = "created-channels";
    String UPDATE_TOPIC_NAME = "updated-channels";

    /**
     * curl http://localhost:3000/api/channel/list
     * @return the list of previously created channels
     *
     */
    ServiceCall<NotUsed, List<Channel>> getChannels();

    /**
     * Request: object representing the channel name
     * @return object containing the id of the created channel
     *
     * Example:
     * curl -v -H "Content-Type: application/json" -X POST -d '{"name": "Science" }' http://localhost:3000/api/channel/create
     *
     */
    ServiceCall<CreateChannelRequest, CreateChannelResponse> createChannel();

    /**
     * Request POST: object containing the username of the user intended to join the channel
     * @param channelId representing the channel id to join
     * @return 200 Ok
     *
     * Example:
     * curl -v -H "Content-Type: application/json" -X POST -d '{ "name":"Tech", "users": ["Username"] }' http://localhost:3000/api/channel/ch/7e76a334-c2ae-4841-92f7-abc345d87d2c
     *
     */
    ServiceCall<CreateChannelRequest, Done> joinChannel(String channelId);

    /**
     * Topic for created channels. Publishes a new message to the created-channel topic.
     * Once a new channel is being created and its event persistent, the message about this
     * event is being sent to the broker. Message-Dispatcher Service is responsible for listening to this topic and notify
     * all about the creation of a new channel.
     * This mechanism uses Event Sourcing to publish a topic taking advantage of the EventSourcing generated events.
     * Message Dispatcher Service is listening to these events. Thanks to this mechanism, communication is way more reliable.
     *
     * @return the generated topic the event is being pushed
     *
     */
    Topic<ChannelEvent> createdChannelTopic();

    /**
     * Topic for updated channels. Publishes a new message to the updated-channel topic.
     * Whenever the user wants to join a specific channel, his username is added to the list of users for that specific channel.
     * The user can interact with this endpoint to change the name of a given channel as well.
     * The new generated event is then made persistent, updating its current state. The message about this
     * event is being sent to the broker. Message-Dispatcher Service is then responsible for listening to this topic and notify
     * all about a new user joined the channel or a channel name editing.
     * This mechanism uses Event Sourcing to publish a topic taking advantage of the EventSourcing generated events.
     * Message Dispatcher Service is listening to these events. Thanks to this mechanism, communication is way more reliable.
     *
     * @return the generated topic the event is being pushed
     *
     */
    Topic<ChannelEvent> updatedChannelTopic();

    @Override
    default Descriptor descriptor() {
        return named("channel-service").withCalls(
                restCall(Method.GET, "/api/channel/list", this::getChannels),
                restCall(Method.POST, "/api/channel/create", this::createChannel),
                restCall(Method.POST, "/api/channel/ch/:channelId", this::joinChannel)
        ).withTopics(Service.topic(CREATE_TOPIC_NAME, this::createdChannelTopic),
                Service.topic(UPDATE_TOPIC_NAME, this::updatedChannelTopic)).withAutoAcl(true);
    }
}
