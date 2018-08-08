package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class HotelConfig {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public Long id;
    public Long rooms;
    public Double overbookingLevel;
    
    public HotelConfig() {}
    
    public HotelConfig(Long rooms, Double overbookingLevel) {
        this.rooms = rooms;
        this.overbookingLevel = overbookingLevel;
    }
 
}