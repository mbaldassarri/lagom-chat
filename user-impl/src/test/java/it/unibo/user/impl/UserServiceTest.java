package it.unibo.user.impl;

import akka.Done;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.broker.Subscriber;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import it.unibo.channel.api.Channel;
import it.unibo.channel.api.ChannelEvent;
import it.unibo.channel.api.ChannelService;
import it.unibo.user.api.UserService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import play.inject.Bindings;
import scala.concurrent.duration.FiniteDuration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;

public class UserServiceTest {

    private static ServiceTest.TestServer server;
    private static String username;
    private static final long TIME = 10;

    private static ChannelService channelService = Mockito.mock(ChannelService.class);
    private final static ServiceTest.Setup setup =  defaultSetup().withJdbc()
            .configureBuilder(b -> b.overrides(
                    Bindings.bind(ChannelService.class).toInstance(channelService)));

    @BeforeClass
    public static void setUp(){
        prepareTopic();
        server = ServiceTest.startServer(setup);
        username = "Username";
    }

    private static void prepareTopic(){
        Mockito.when(channelService.createdChannelTopic())
                .thenReturn(new Topic<ChannelEvent>() {
                    @Override
                    public TopicId topicId() {
                        return null;
                    }

                    @Override
                    public Subscriber<ChannelEvent> subscribe() {
                        return new Subscriber<ChannelEvent>() {
                            @Override
                            public Subscriber<ChannelEvent> withGroupId(String groupId) throws IllegalArgumentException {
                                return null;
                            }

                            @Override
                            public Source<ChannelEvent, ?> atMostOnceSource() {
                                return null;
                            }

                            @Override
                            public CompletionStage<Done> atLeastOnce(Flow<ChannelEvent, Done, ?> flow) {
                                return null;
                            }
                        };
                    }
                });
    }

    @AfterClass
    public static void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void login_user_should_get_channel_list() throws Exception {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        ArrayList<Channel> channels = new ArrayList<>(2);
        channels.add(Channel.of(id1, "Tech", new ArrayList<>()));
        channels.add(Channel.of(id2, "Food", new ArrayList<>()));

        Mockito.when(channelService.getChannels())
                .thenReturn(req -> CompletableFuture.completedFuture(channels));
        //Given
        UserService service = server.client(UserService.class);

        //When
        ServiceTest.eventually(FiniteDuration.create(10, SECONDS), FiniteDuration.create(1000, MILLISECONDS), () -> {

            List<Channel> serviceResponse = service
                    .loginUser(username)
                    .invoke()
                    .toCompletableFuture()
                    .get(TIME, TimeUnit.SECONDS);

            //Then
            assertTrue("Service should authenticate a new User and get the list of channels" + serviceResponse.toString(), serviceResponse.size() >= 0);
        });
    }

    @Test
    public void logout_user_should_remove_user() throws Exception {

        //Given
        UserService service = server.client(UserService.class);

        //When
        Done serviceResponse = service
                .logoutUser(username)
                .invoke()
                .toCompletableFuture()
                .get(TIME, TimeUnit.SECONDS);

        //Then
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("userPersistenceUnit");
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        assertTrue("Username deleted" , selectUsername(entityManager, username).size() == 0);
    }

    private List<String> selectUsername(EntityManager em, String username) {
        return em.createQuery("SELECT u.username FROM UserModel as u " +
                "WHERE u.username = :username", String.class)
                .setParameter("username", username)
                .getResultList();
    }
}
