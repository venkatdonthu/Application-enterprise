package controllers;

import com.google.inject.Inject;

import dao.HotelConfigDao;
import dao.ReservationDao;
import models.HotelConfig;
import models.HotelConfigDto;
import models.ReservationDto;
import ninja.Result;
import ninja.Results;

public class ReservationController {

    @Inject
    HotelConfigDao hotelConfigDao;
    @Inject
    ReservationDao reservationDao;

    public ReservationController() {}

    public Result reserve(ReservationDto reservationDto) {
        return Results.ok();
    }

    
    public Result getAllReservations() {
        HotelConfig hotelConfig = hotelConfigDao.getConfig();
        if (hotelConfig == null) {
            return Results.json().render("Hotel Config not setup");
        }
        return Results.json().render(reservationDao.getAllReservations());
    }

    public Result makeReservation(ReservationDto reservationDto) {
        System.out.println("reservationDto:" + reservationDto);
        HotelConfig hotelConfig = hotelConfigDao.getConfig();
        if (hotelConfig == null) {
            return Results.json().render("Hotel Config not setup");
        }

        Result result = Results.json().render("OK!!!");
        try {
            reservationDao.postReservation(reservationDto);
        } catch(Exception e) {
            result = Results.json().render(e.getMessage());
        }
        return result;
    }

    public Result insertReservation(ReservationDto reservationDto) {
        System.out.println("reservationDto:" + reservationDto);
        HotelConfig hotelConfig = hotelConfigDao.getConfig();
        if (hotelConfig == null) {
            return Results.json().render("Hotel Config not setup");
        }

        Result result = Results.json().render("OK!!!");
        try {
            reservationDao.insertReservation(reservationDto);
        } catch(Exception e) {
            result = Results.json().render(e.getMessage());
        }
        return result;
    }

}
