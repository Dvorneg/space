package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;

import java.util.Date;
import java.util.List;

/**
 * @author Denis
 *
 */

public interface ShipService {


    List<Ship> getAllShips();

    Ship updateShip(Ship oldShip, Ship newShip) ;

    void deleteShip(Ship shiphip) ;

    Ship getById(Long id);

    List<Ship> getShips(String name, String planet, ShipType shipType, Long afterDate, Long beforeDate, Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize, Double minRating, Double maxRating);

    List<Ship> getPage(List<Ship> ships, Integer pageNumber, Integer pageSize);
    
    List<Ship> sortShips(List<Ship> ships, ShipOrder order);

    double calculateRating(double speed, boolean isUsed, Date prod);

    boolean isShipValid(Ship ship);

    Ship saveShip(Ship ship);
}
