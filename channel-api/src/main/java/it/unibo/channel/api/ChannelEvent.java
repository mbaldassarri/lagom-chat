package it.unibo.channel.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(ChannelEvent.ChannelCreated.class),
        @JsonSubTypes.Type(ChannelEvent.ChannelUpdated.class)
})
public interface ChannelEvent {

    String getId();

    @JsonTypeName("channel-created")
    final class ChannelCreated implements ChannelEvent {
        private final String id;
        private final String name;
        private final List<String> users;

        @JsonCreator
        public ChannelCreated(String id, String name, List<String> users) {
            this.id = id;
            this.name = name;
            this.users = users;
        }

        @Override
        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<String> getUsers() {
            return users;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChannelCreated that = (ChannelCreated) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            return users != null ? users.equals(that.users) : that.users == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (users != null ? users.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ChannelCreated{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", users=" + users +
                    '}';
        }
    }

    @JsonTypeName("channel-updated")
    final class ChannelUpdated implements ChannelEvent {
        private final String id;
        private final String name;
        private final List<String> users;

        @JsonCreator
        public ChannelUpdated(String id, String name, List<String> users) {
            this.id = id;
            this.name = name;
            this.users = users;
        }

        @Override
        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<String> getUsers() {
            return users;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChannelUpdated that = (ChannelUpdated) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            return users != null ? users.equals(that.users) : that.users == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (users != null ? users.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ChannelUpdated{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", users=" + users +
                    '}';
        }
    }
}