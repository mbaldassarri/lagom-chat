package it.unibo.user.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import it.unibo.channel.api.Channel;

import java.util.List;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

public interface UserService extends Service {

    /**
     *
     * This call is responsible of saving a unique username to the PostgreSql database.
     * Once logged, the user has access to the list of the chat channels
     *
     * @param username the name of the user
     * @return the list of currently active channels, previously created by other users
     *
     * example: curl http://localhost:3000/api/user/login/Marco
     */
    ServiceCall<NotUsed, List<Channel>> loginUser(String username);


    /**
     * This call is responsible of deleting the user from the database and represents a logout.
     *
     * @param username the name of the user who is going to logout
     * @return 200 Ok
     */
    ServiceCall<NotUsed, Done> logoutUser(String username);

    @Override
    default Descriptor descriptor() {
        return named("user-service").withCalls(
                restCall(Method.GET, "/api/user/login/:username", this::loginUser),
                restCall(Method.GET, "/api/user/logout/:username", this::logoutUser)

        ).withAutoAcl(true);
    }
}
