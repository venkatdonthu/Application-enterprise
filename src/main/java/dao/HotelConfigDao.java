package dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import models.HotelConfig;
import models.HotelConfigDto;
import ninja.jpa.UnitOfWork;


public class HotelConfigDao {
    
    @Inject
    Provider<EntityManager> entityManagerProvider;
    
    @UnitOfWork
    public HotelConfig getConfig() {
        EntityManager entityManager = entityManagerProvider.get();
        TypedQuery<HotelConfig> q = entityManager.createQuery("SELECT x FROM HotelConfig x", HotelConfig.class);
        return getSingleResult(q);
    }

    @Transactional
    public HotelConfig createConfig(HotelConfigDto hotelConfigDto) {
        EntityManager entityManager = entityManagerProvider.get();
        TypedQuery<HotelConfig> q = entityManager.createQuery("SELECT x FROM HotelConfig x", HotelConfig.class);
        HotelConfig hotelConfig = getSingleResult(q);
        if (hotelConfig == null) {
            hotelConfig = createConfig(hotelConfigDto.rooms, hotelConfigDto.overbookingLevel);
        } else {
            hotelConfig.rooms = hotelConfigDto.rooms;
            hotelConfig.overbookingLevel = hotelConfigDto.overbookingLevel;
        }
        entityManager.persist(hotelConfig);
        return hotelConfig;
    }

    public HotelConfig createConfig(Long rooms, Double overbookingLevel) {
        return new HotelConfig(rooms, overbookingLevel);
    }

    @UnitOfWork
    public Long getMaxReservation() {
        Long maxReservation = Long.valueOf(-1);
        HotelConfig hotelConfig = getConfig();
        if (hotelConfig != null) {
            maxReservation = Double.valueOf(hotelConfig.rooms * ( 1 + (hotelConfig.overbookingLevel/100))).longValue();
        }
        return maxReservation;
    }

    /**
     * Get single result without throwing NoResultException, you can of course just catch the
     * exception and return null, it's up to you.
     */
    private static <T> T getSingleResult(TypedQuery<T> query) {
        query.setMaxResults(1);
        List<T> list = query.getResultList();
        if (list == null || list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

}
