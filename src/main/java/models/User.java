package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class User {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public Long id;
    public String username;
    public String password;
    public String email;
    public String fullname;
    public boolean isAdmin;

    @OneToMany(
        mappedBy = "user", 
        cascade = CascadeType.ALL, 
        orphanRemoval = true
    )
    public List<Reservation> rooms = new ArrayList<>();


    public User() {}
    
    public User(String username, String password, String fullname) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
    }

    public void addRoom(Room room, Date checkInDate, Date checkOutDate) {
        Reservation reservation = new Reservation(this, room, checkInDate, checkOutDate);
        // System.out.println("reservation.user.fullname:" + reservation.user.fullname);
        // System.out.println("reservation.room.roomNUmber:" + reservation.room.roomNumber);
        rooms.add(reservation);
        room.users.add(reservation);
    }

    public void removeRoom(Room room) {
        for (Iterator<Reservation> iterator = rooms.iterator(); 
             iterator.hasNext(); ) {
            Reservation reservation = iterator.next();
 
            if (reservation.user.equals(this) &&
                    reservation.room.equals(room)) {
                iterator.remove();
                reservation.room.users.remove(reservation);
                reservation.user = null;
                reservation.room = null;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User that = (User) o;
        return Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(fullname, that.fullname) && Objects.equals(isAdmin, that.isAdmin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, fullname, isAdmin);
    }
 
}
