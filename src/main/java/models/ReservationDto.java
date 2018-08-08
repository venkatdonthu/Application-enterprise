package models;

public class ReservationDto {

    public String fullname;
    public String email;
    public Long roomNumber;
    public String checkInDate;
    public String checkOutDate;

    public ReservationDto() { }

    public ReservationDto(String fullname, Long roomNumber, String checkInDate, String checkOutDate, String email) {
        this.fullname = fullname;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.email = email;
    }

    public String toString() {
        return fullname + "-" + roomNumber + "-" + checkInDate + "-" + checkOutDate;
    }

}
