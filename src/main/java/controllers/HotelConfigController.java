package controllers;

import com.google.inject.Inject;

import dao.HotelConfigDao;
import models.HotelConfigDto;
import ninja.Result;
import ninja.Results;

public class HotelConfigController {

    @Inject
    HotelConfigDao hotelConfigDao;

    public HotelConfigController() {}

    public Result showConfig() {
        return Results.json().render(hotelConfigDao.getConfig());
    }

    public Result postConfig(HotelConfigDto hotelConfigDto) {
        return Results.json().render(hotelConfigDao.createConfig(hotelConfigDto));
    }

    public Result getCurrentMaxReservation() {
        Long maxReservation = hotelConfigDao.getMaxReservation();
        if (maxReservation == -1) {
            return Results.json().render("No Config set yet!!!!");
        }
        return Results.json().render(maxReservation);
    }
}
