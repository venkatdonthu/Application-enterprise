package models;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Size;

public class HotelConfigDto {

    @Size(min = 1)
    public Long rooms;
    
    @DecimalMax(value = "100.00")
    public Double overbookingLevel;
    
    public HotelConfigDto() {}

}
