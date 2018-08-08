package models;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class UserRoomId implements Serializable {

    // @Column(name = "user_id")
    private Long user;

    // @Column(name = "room_id")
    private Long room;

    public UserRoomId(){}
    public UserRoomId(Long user, Long room) {
        this.user = user;
        this.room = room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserRoomId that = (UserRoomId) o;   
        return Objects.equals(user, that.user) && Objects.equals(room, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, room);
    }
 
}
