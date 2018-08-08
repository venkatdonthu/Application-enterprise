package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
public class Room {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public Long id;
    public Long roomNumber;
    public String about;

    @OneToMany(
        mappedBy = "room",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    public List<Reservation> users = new ArrayList<>();

    // maybe amenities

    public Room() {}
    public Room(Long roomNumber, String about) {
        this.roomNumber = roomNumber;
        this.about = about;
    }
    

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Room that = (Room) o;
        return Objects.equals(roomNumber, that.roomNumber) && Objects.equals(about, that.about);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomNumber, about);
    }
}
