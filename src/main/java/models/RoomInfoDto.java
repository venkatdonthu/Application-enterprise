package models;

public class RoomInfoDto {
    public Long roomNumber;
    public String about;
    public boolean available = false;
    public String nextAvailable;
    public Long numberOfPeopleReserved = Long.valueOf(0);
    public RoomInfoDto() {}
    public RoomInfoDto(Room room) {
        this.roomNumber = room.roomNumber;
        this.about = room.about;
    }

}
