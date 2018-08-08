package controllers;

import com.google.inject.Inject;

import dao.ReservationDao;
import dao.RoomDao;
import dao.UserDao;
import models.ReservationParam;
import models.RoomDto;
import models.RoomParam;
import models.RoomsDto;
import models.UserDto;
import ninja.Result;
import ninja.Results;

public class HotelController {

    @Inject
    UserDao userDao;

    @Inject
    RoomDao roomDao;

    @Inject
    ReservationDao reservationDao;

    public HotelController() {}

    public Result listUsers() {
        return Results.json().render(userDao.getUsers());
    }

    public Result listRooms() {
        return Results.json().render(roomDao.getRooms());
    }

    public Result getRoomInfoByDate(RoomParam roomParam) {
        Result result = Results.json().render("OK!!!");
        try{
            result = Results.json().render(reservationDao.getRoomInfoByDate(roomParam));
        } catch (Exception e) {
            result = Results.json().render(e.getMessage());
        }

        return result;
    }

    public Result createRoom(RoomDto roomDto) {
        return Results.json().render(roomDao.postRoom(roomDto));
    }

    public Result createRooms(RoomsDto roomsDto) {
        return Results.json().render(roomDao.postRooms(roomsDto));
    }

    public Result updateReservation(ReservationParam reservationUpdateParama) {
        Result result = Results.json().render("OK!!!");
        try{
            reservationDao.updateReservation(reservationUpdateParama);
        } catch (Exception e) {
            result = Results.json().render(e.getMessage());
        }
        return result;
    }

    public Result cancelReservation(ReservationParam reservationUpdateParama) {
        Result result = Results.json().render("OK!!!");
        try{
            reservationDao.cancelReservation(reservationUpdateParama);
        } catch (Exception e) {
            result = Results.json().render(e.getMessage());
        }
        return result;
    }
}
