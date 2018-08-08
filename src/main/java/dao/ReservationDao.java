package dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import models.Reservation;
import models.ReservationDto;
import models.ReservationParam;
import models.ReservationsDto;
import models.Room;
import models.RoomInfoDto;
import models.RoomParam;
import models.User;
import ninja.jpa.UnitOfWork;

public class ReservationDao {
   
    @Inject
    HotelConfigDao hotelConfigDao;

    @Inject
    Provider<EntityManager> entitiyManagerProvider;

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    static SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @UnitOfWork
    public ReservationsDto getAllReservations() {
        EntityManager entityManager = entitiyManagerProvider.get();
        TypedQuery<Reservation> query = entityManager.createQuery("SELECT x FROM Reservation x", Reservation.class);
        List<Reservation> reservations = query.getResultList();
        List<ReservationDto> reservationsDtoList = new ArrayList<>();
        
        for (Reservation reservation : reservations) {
            // System.out.println("reservation.user.fullname:" + reservation.user.fullname);
            // System.out.println("reservation.room.roomNumber:" + reservation.room.roomNumber);
            reservationsDtoList.add( new ReservationDto(reservation.user.fullname, reservation.room.roomNumber, simpleDateFormat2.format(reservation.checkInDate), simpleDateFormat2.format(reservation.checkOutDate), reservation.user.email));
        }

        ReservationsDto reservationsDto = new ReservationsDto();
        reservationsDto.reservations = reservationsDtoList;
        return reservationsDto;
    }

    @UnitOfWork
    public RoomInfoDto getRoomInfoByDate(RoomParam roomParam) throws Exception {
        System.out.println("roomParam:" + roomParam.roomNumber + "-" + roomParam.date);
        EntityManager entityManager = entitiyManagerProvider.get();
        Calendar now = Calendar.getInstance();
        Date myDate = getDateWithCurrentTime(now, roomParam.date);
        if (simpleDateFormat.format(myDate).compareTo(simpleDateFormat.format(now.getTime())) < 0) {
            throw new Exception("You cannot get room info in the past!!!Start with at least today!!!");
        }

        RoomInfoDto roomInfoDto = null;
        TypedQuery<Room> query = entityManager.createQuery("SELECT x FROM Room x WHERE x.roomNumber = :roomNumber)", Room.class);
        List<Room> rooms = query.setParameter("roomNumber", roomParam.roomNumber).getResultList();
        if (rooms.isEmpty()) {
            throw new Exception("Room does not exist!!!");
        } else {
            Room room = rooms.get(0);
            roomInfoDto = new RoomInfoDto(room);
            List<Reservation> reservations = room.users;
            reservations.sort(Comparator.comparing(Reservation::getCheckInDate)); // sort by check in date

            Date farthestCheckoutDate = null;
            // check the number of people who have reserved the room based on myDate
            for (Reservation reservation : reservations) {
                if ((!myDate.before(reservation.checkInDate) && !myDate.after(reservation.checkOutDate))) {
                    roomInfoDto.numberOfPeopleReserved += Long.valueOf(1);
                    if (farthestCheckoutDate == null) {
                        farthestCheckoutDate = reservation.checkOutDate;
                    }
                    else {
                        if (reservation.checkOutDate.after(farthestCheckoutDate)) {
                            farthestCheckoutDate = reservation.checkOutDate;
                        }
                    }
                    System.out.println("lastOverlapDate:" + farthestCheckoutDate);
                }
            }

            roomInfoDto.available = roomInfoDto.numberOfPeopleReserved < hotelConfigDao.getMaxReservation();

            if (!roomInfoDto.available) {
                roomInfoDto.nextAvailable = simpleDateFormat.format(farthestCheckoutDate);
            }
        }
        return roomInfoDto;
    }

    /**
     * For a given date, am I allowed to overbook ?
     */
    public void checkBookable(Long roomNumber, Date checkInDate, Date checkOutDate) throws Exception {
        EntityManager entityManager = entitiyManagerProvider.get();
        TypedQuery<Reservation> query = entityManager.createQuery("SELECT x FROM Reservation x", Reservation.class);
        List<Reservation> reservationList = query.getResultList();
        // System.out.println("Finish running query");
        // System.out.println("resultList size:" + reservationList.size());
        if (reservationList.isEmpty()) {
            // System.out.println("PASSED");
            return;
        }
        Long maxReservation = hotelConfigDao.getMaxReservation();
        reservationList.removeIf(x -> x.room.roomNumber != roomNumber);
        Long currentReservationOfRoom = Long.valueOf(0);
        for (Reservation reservation : reservationList) {
            if (inBetween(checkInDate, reservation.checkInDate, reservation.checkOutDate) || inBetween(checkOutDate, reservation.checkInDate, reservation.checkOutDate) || (inBetween(reservation.checkInDate, checkInDate, checkOutDate) && inBetween(reservation.checkOutDate, checkInDate, checkOutDate))) {
                currentReservationOfRoom += Long.valueOf(1);
            }
        }

        // System.out.println("max reservation=" + maxReservation);
        // System.out.println("resultList size:" + reservationList.size());
        
        if (currentReservationOfRoom >= maxReservation) {
            // System.out.println("Reservation MAX!!!");
            throw new Exception("Overbooking max has reached. Please check back later!!!!");
        }
        // pass

    }

    @Transactional
    public void persistReservation(EntityManager entityManager, User user, Room room, Date checkInDate, Date checkOutDate) {
        user.addRoom(room, checkInDate, checkOutDate);
        entityManager.persist(user);
        entityManager.flush();
    }

    @Transactional
    public void persistUser(EntityManager entityManager, User user) {
        entityManager.persist(user);
        entityManager.flush();
    }

    @UnitOfWork
    public void postReservation(ReservationDto reservationDto) throws Exception {
        EntityManager entityManager = entitiyManagerProvider.get();
        System.out.println("reservationDao:" + reservationDto);
        Calendar now = Calendar.getInstance();
        Date nowDate = now.getTime();
        Date checkInDate = getDateWithCurrentTime(now, reservationDto.checkInDate);
        System.out.println("CheckInDate:" + checkInDate);
        System.out.println("NowDate:" + nowDate);
        if (simpleDateFormat.format(checkInDate).compareTo(simpleDateFormat.format(nowDate)) < 0) {
            throw new Exception("You cannot check in in the past!!!!");
        }
        Date checkOutDate = getDateWithCurrentTime(now, reservationDto.checkOutDate);
        
        if (simpleDateFormat.format(checkOutDate).compareTo(simpleDateFormat.format(nowDate)) < 0) {
            throw new Exception("You cannot check out in the past!!!!");
        }
        checkBookable(reservationDto.roomNumber, checkInDate, checkOutDate);
        
        TypedQuery<User> query = entityManager.createQuery("SELECT x FROM User x WHERE x.fullname = :fullname AND x.email = :email", User.class);
        List<User> userList = query.setParameter("fullname", reservationDto.fullname).setParameter("email", reservationDto.email).getResultList();

        List<Room> roomList = entityManager.createQuery("SELECT x FROM Room x WHERE x.roomNumber = :roomNumber", Room.class).setParameter("roomNumber", reservationDto.roomNumber).getResultList();
        if (roomList.isEmpty()) {
            throw new Exception("Room {" + reservationDto.roomNumber + "} does not exist!!!");
        }

        User user = null;
        if (userList.isEmpty()) { // for the sake of simplicity, username = email
            user = new User(reservationDto.email, "123", reservationDto.fullname);
            user.email = reservationDto.email;
            persistUser(entityManager, user);
        } else {
            user = userList.get(0); // got a user
        }

        persistReservation(entityManager, user, roomList.get(0), checkInDate, checkOutDate);
    }

    /**
     * Used to insert data into the database
     */
    @UnitOfWork
    public void insertReservation(ReservationDto reservationDto) throws Exception {
        EntityManager entityManager = entitiyManagerProvider.get();
        System.out.println("reservationDao:" + reservationDto);
        Calendar now = Calendar.getInstance();
        Date checkInDate = getDateWithCurrentTime(now, reservationDto.checkInDate);
        Date checkOutDate = getDateWithCurrentTime(now, reservationDto.checkOutDate);

        checkBookable(reservationDto.roomNumber, checkInDate, checkOutDate);
        
        TypedQuery<User> query = entityManager.createQuery("SELECT x FROM User x WHERE x.fullname = :fullname AND x.email = :email", User.class);
        List<User> userList = query.setParameter("fullname", reservationDto.fullname).setParameter("email", reservationDto.email).getResultList();
        // if (userList.isEmpty()) {
        //     throw new Exception("User {" + reservationDto.fullname + "} not found!!!");
        // }
        List<Room> roomList = entityManager.createQuery("SELECT x FROM Room x WHERE x.roomNumber = :roomNumber", Room.class).setParameter("roomNumber", reservationDto.roomNumber).getResultList();
        if (roomList.isEmpty()) {
            throw new Exception("Room {" + reservationDto.roomNumber + "} does not exist!!!");
        }

        User user = null;
        if (userList.isEmpty()) { // for the sake of simplicity, username = email
            user = new User(reservationDto.email, "123", reservationDto.fullname);
            user.email = reservationDto.email;
            persistUser(entityManager, user);
            System.out.println("Hey yo1!!!");
        } else {
            user = userList.get(0);
        }

        persistReservation(entityManager, user, roomList.get(0), checkInDate, checkOutDate);
    }

    @UnitOfWork
    public void updateReservation(ReservationParam reservationParam) throws Exception {
        Calendar now = Calendar.getInstance();
        EntityManager entityManager = entitiyManagerProvider.get();
        if (!reservationParam.action.equals("update")){
            throw new Exception("Wrong action!!!");
        }
        
        TypedQuery<User> query = entityManager.createQuery("SELECT x FROM User x WHERE x.fullname = :fullname AND x.email = :email", User.class);
        List<User> userList = query.setParameter("fullname", reservationParam.fullname).setParameter("email", reservationParam.email).getResultList();
        if (userList.isEmpty()) {
            throw new Exception("User {" + reservationParam.fullname + "} not found!!!");
        }
        List<Room> roomList = entityManager.createQuery("SELECT x FROM Room x WHERE x.roomNumber = :roomNumber", Room.class).setParameter("roomNumber", reservationParam.roomNumber).getResultList();
        if (roomList.isEmpty()) {
            throw new Exception("Room {" + reservationParam.roomNumber + "} does not exist!!!");
        }

        User user = userList.get(0);
        Room room = roomList.get(0);

        // String fromCheckInDate = simpleDateFormat.format(reservationParam.fromCheckInDate);
        // String fromCheckOutDate = simpleDateFormat.format(reservationParam.fromCheckOutDate);

        // check if the record for that exist
        Reservation requestReservation = null;
        for( Reservation reservation : room.users) {
            String checkInDate = simpleDateFormat.format(reservation.checkInDate);
            String checkOutDate = simpleDateFormat.format(reservation.checkOutDate);
            if (checkInDate.equals(reservationParam.fromCheckInDate) && checkOutDate.equals(reservationParam.fromCheckOutDate)) { // found the record we are looking at
                requestReservation = reservation;
                break;
            }    
        }

        if (requestReservation == null) {
            throw new Exception("You did not reserve room in this range you specified!!!");
        }

        // room exist, now check if the change reservation is within qualified range
        
        Date nowDate = now.getTime();
        if (reservationParam.toCheckInDate.compareTo(simpleDateFormat.format(nowDate)) < 0) {
            throw new Exception("You cannot check in in the past!!!!");
        }

        if (reservationParam.toCheckOutDate.compareTo(simpleDateFormat.format(nowDate)) < 0) {
            throw new Exception("You cannot check out in the past!!!!");
        }

        // rate limiter
        long duration = requestReservation.created.getTime() - now.getTime().getTime();
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        if (diffInMinutes <= 1) {
            throw new Exception("You are changing reservation within 1 minute. Sit tight and check back after 1 minute");
        }
        Date finalCheckInDate = getDateWithCurrentTime(now, reservationParam.toCheckInDate);
        Date finalCheckOutDate = getDateWithCurrentTime(now, reservationParam.toCheckOutDate);

        // now change the reservation
        changeReservationDate(entityManager, requestReservation, finalCheckInDate, finalCheckOutDate);
    }

    public void cancelReservation(ReservationParam reservationParam) throws Exception {
        Calendar now = Calendar.getInstance();
        EntityManager entityManager = entitiyManagerProvider.get();
        if (!reservationParam.action.equals("cancel")) {
            throw new Exception("Wrong action!!!");
        }
        
        TypedQuery<User> query = entityManager.createQuery("SELECT x FROM User x WHERE x.fullname = :fullname AND x.email = :email", User.class);
        List<User> userList = query.setParameter("fullname", reservationParam.fullname).setParameter("email", reservationParam.email).getResultList();
        if (userList.isEmpty()) {
            throw new Exception("User {" + reservationParam.fullname + "} not found!!!");
        }
        List<Room> roomList = entityManager.createQuery("SELECT x FROM Room x WHERE x.roomNumber = :roomNumber", Room.class).setParameter("roomNumber", reservationParam.roomNumber).getResultList();
        if (roomList.isEmpty()) {
            throw new Exception("Room {" + reservationParam.roomNumber + "} does not exist!!!");
        }

        User user = userList.get(0);
        Room room = roomList.get(0);

        // String fromCheckInDate = simpleDateFormat.format(reservationParam.fromCheckInDate);
        // String fromCheckOutDate = simpleDateFormat.format(reservationParam.fromCheckOutDate);

        // check if the record for that exist
        Reservation requestReservation = null;
        for( Reservation reservation : room.users) {
            String checkInDate = simpleDateFormat.format(reservation.checkInDate);
            String checkOutDate = simpleDateFormat.format(reservation.checkOutDate);
            if (checkInDate.equals(reservationParam.fromCheckInDate) && checkOutDate.equals(reservationParam.fromCheckOutDate)) { // found the record we are looking at
                requestReservation = reservation;
                break;
            }    
        }

        if (requestReservation == null) {
            throw new Exception("You did not reserve room in this range you specified!!!");
        }

        // room exist, now check if the change reservation is within qualified range
        // cancel, no need to check for date, it is ignore

        // rate limiter
        long duration = requestReservation.created.getTime() - now.getTime().getTime();
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        if (diffInMinutes <= 1) {
            throw new Exception("You are changing reservation within 1 minute. Sit tight and check back after 1 minute");
        }

        // now change the reservation
        removeReservation(entityManager, requestReservation);  
    }

    @Transactional
    public void changeReservationDate(EntityManager entityManager, Reservation reservation, Date checkInDate, Date checkOutDate) {
        reservation.checkInDate = checkInDate;
        reservation.checkOutDate = checkOutDate;
        entityManager.persist(reservation);
        entityManager.clear();
    }

    @Transactional
    public void removeReservation(EntityManager entityManager, Reservation reservation) {
        reservation.user.removeRoom(reservation.room);
        entityManager.remove(reservation);
        entityManager.clear();
    }

    static boolean inBetween(Date in, Date a, Date b) {
        return !in.before(a) && !in.after(b);
    }
    static Date getDateWithCurrentTime(Calendar at, String dateStr) throws Exception{
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(simpleDateFormat.parse(dateStr));
        int h = at.get(Calendar.HOUR_OF_DAY);
        int m = at.get(Calendar.MINUTE);
        int s = at.get(Calendar.SECOND);
        dateCalendar.set(Calendar.HOUR_OF_DAY, h);
        dateCalendar.set(Calendar.MINUTE, m);
        dateCalendar.set(Calendar.SECOND, s);
        Date finalDate = dateCalendar.getTime();
        System.out.println("final Date:" + finalDate);
        return finalDate;

    }

}
