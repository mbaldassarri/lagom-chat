package it.unibo.user.impl;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import it.unibo.channel.api.Channel;
import it.unibo.channel.api.ChannelService;
import it.unibo.user.api.UserService;
import it.unibo.user.model.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class UserServiceImpl implements UserService {

    private final ChannelService channelService;
    private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String PERSISTENCE_UNIT_NAME = "userPersistenceUnit";

    private EntityManagerFactory emf;

    @Inject
    public UserServiceImpl(ChannelService channelService) {
        this.channelService = channelService;
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    }

    @Override
    public ServiceCall<NotUsed, List<Channel>> loginUser(String username) {
        return notUsed -> {
            EntityManager entityManager = emf.createEntityManager();
            entityManager.getTransaction().begin();
            UserModel user = new UserModel();
            List<String> storedUser = selectUsername(entityManager, username);
            if (storedUser.size() > 0) {
                throw new BadRequest("User already exists");
            } else {
                user.setId(UUID.randomUUID().toString());
                user.setUsername(username);
                entityManager.persist(user);
                entityManager.getTransaction().commit();
                return channelService.getChannels().invoke().thenApply(channels -> {
                    log.info("Channel Service responded with channels");
                    return channels;
                });
            }
        };
    }

    private List<String> selectUsername(EntityManager em, String username) {
        return em.createQuery("SELECT u.username FROM UserModel as u " +
                "WHERE u.username = :username", String.class)
                .setParameter("username", username)
                .getResultList();
    }

    @Override
    public ServiceCall<NotUsed, Done> logoutUser(String username) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            String userId = entityManager.createQuery("SELECT um.id FROM UserModel  as um " +
                    "WHERE um.username = :username", String.class)
                    .setParameter("username", username)
                    .getSingleResult();
            Optional<UserModel> user = Optional.of(entityManager.find(UserModel.class, userId));
            if(user.isPresent()) {
                entityManager.remove(user.get());
                entityManager.getTransaction().commit();
            }
        } catch (Exception e) {
            throw new BadRequest("Something went wrong with the service call: " + e.getMessage());
        }
        return notUsed -> supplyAsync(() -> Done.getInstance());
    }
}