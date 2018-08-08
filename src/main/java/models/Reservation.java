package models;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@Entity
// @IdClass(UserRoomId.class)
public class Reservation {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public Long id;

    // @Id
    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "id")
    public User user;

    // @Id
    @ManyToOne
    @JoinColumn(name = "roomId", referencedColumnName = "id")
    public Room room;

    
    public Date checkInDate;
    public Date checkOutDate;

    public Date created; // for applying rate limiter
    public Date updated;

    public Reservation() {}

    public Reservation(User user, Room room, Date checkInDate, Date checkOutDate) {
        this.user = user;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        // this.id = new UserRoomId(user.id, room.id);
    }

    public Date getCheckInDate() {
        return this.checkInDate;
    }

    @PrePersist
    protected void onCreate() {
        created = new Date();
    }
  
    @PreUpdate
    protected void onUpdate() {
        // System.out.println("Hello pre-Update");
        updated = new Date();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Reservation that = (Reservation) o;
        return Objects.equals(user, that.user) && Objects.equals(room, that.room) && Objects.equals(checkInDate, that.checkInDate) && Objects.equals(checkOutDate, that.checkOutDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, room, checkInDate, checkOutDate);
    }
 
}
