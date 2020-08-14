package it.unibo.message.dispatcher.api;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

public interface MessageService extends Service {

    /**
     * Example of simple publish-subscribe mechanism, which is implemented without using reliable Event Sourcing underlying layer.
     * Publishes a new message to the 'message' topic containing the text the user wants to publish to a given Channel.
     * Message-Dispatcher service is responsible for listening to this topic.
     * Request: An object containing the sender user, the message and the channel in which the message has to be sent.
     * This mechanism is only used for intra-service (or intra-cluster) communication.
     * */
    ServiceCall<CreateMessageRequest, Done> publishMessage();

    /**
     * Example of simple publish-subscribe mechanism,which is implemented without using
     * reliable Event Sourcing underlying layer.
     * This is a Subscriber of the topic specified in publishMessage method.
     * Returns a stream Source of messages published to a given topic. This way anyone interested can listen
     * to the specified topic.
     * This mechanism is only used for intra-service (or intra-cluster) communication.
     * */
    ServiceCall<NotUsed, Source<CreateMessageRequest, ?>> getPublishedMessageStream();

    @Override
    default Descriptor descriptor() {
        return named("message-service").withCalls(
                restCall(Method.POST, "/api/message/send", this::publishMessage),
                Service.pathCall("/api/message/live", this::getPublishedMessageStream)
        ).withAutoAcl(true);
    }
}
