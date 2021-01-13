package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Denis
 */

@Service
public class ShipServiceImpl implements ShipService {

    private final ShipRepository shipRepository;

    public ShipServiceImpl(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    @Override
    public List<Ship> getAllShips() {
        return (List<Ship>) shipRepository.findAll();
    }

    @Override
    public Ship getById(Long id) {
        return shipRepository.findById(id).orElse(null);
    }

    @Override
    public List<Ship> getShips(String name, String planet, ShipType shipType, Long afterDate, Long beforeDate, Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize, Integer maxCrewSize, Double minRating, Double maxRating) {
        Date aDate;
        if (afterDate == null)
            aDate = null;
        else
            aDate = new Date(afterDate);
        Date bDate;
        if (beforeDate == null)
            bDate = null;
        else
            bDate = new Date(beforeDate);

        List<Ship> list = new ArrayList<>();
        shipRepository.findAll().forEach((ship) -> {
            if (name != null && !ship.getName().contains(name)) return;
            if (planet != null && !ship.getPlanet().contains(planet)) return;
            if (shipType != null && ship.getShipType() != shipType) return;
            if (aDate != null && getYear(ship.getProdDate()) < getYear(aDate)) return;
            if (bDate != null && getYear(ship.getProdDate()) > getYear(bDate)) return;
            if (isUsed != null && ship.getUsed().booleanValue() != isUsed.booleanValue()) return;
            if (minSpeed != null && ship.getSpeed().compareTo(minSpeed) < 0) return;
            if (maxSpeed != null && ship.getSpeed().compareTo(maxSpeed) > 0) return;
            if (minCrewSize != null && ship.getCrewSize().compareTo(minCrewSize) < 0) return;
            if (maxCrewSize != null && ship.getCrewSize().compareTo(maxCrewSize) > 0) return;
            if (minRating != null && ship.getRating().compareTo(minRating) < 0) return;
            if (maxRating != null && ship.getRating().compareTo(maxRating) > 0) return;
            list.add(ship);
        });
        return list;
    }

    @Override
    public List<Ship> sortShips(List<Ship> ships, ShipOrder order) {
        if (order != null) {
            ships.sort((ship1, ship2) -> {
                switch (order) {
                    case ID:
                        return ship1.getId().compareTo(ship2.getId());
                    case SPEED:
                        return ship1.getSpeed().compareTo(ship2.getSpeed());
                    case DATE:
                        return ship1.getProdDate().compareTo(ship2.getProdDate());
                    case RATING:
                        return ship1.getRating().compareTo(ship2.getRating());
                    default:
                        return 0;
                }
            });
        }
        return ships;
    }

    @Override
    public List<Ship> getPage(List<Ship> ships, Integer pageNumber, Integer pageSize) {
        if (pageNumber == null)
            pageNumber = 0;
        if (pageSize == null)
            pageSize = 3;
        final int from = pageNumber * pageSize;
        int to = from + pageSize;
        if (to > ships.size()) to = ships.size();
        return ships.subList(from, to);
    }


    // ------------------Изменение данных------------------------------
    @Override
    public void deleteShip(Ship ship) {
        shipRepository.delete(ship);
    }

    @Override
    public Ship saveShip(Ship ship) {
        return shipRepository.save(ship);
    }

    @Override
    public Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException {
        boolean changeRating = false;

        String name = newShip.getName();
        if (name != null) {
            if (isStringValid(name)) {
                oldShip.setName(name);
            } else {
                throw new IllegalArgumentException();
            }
        }
        String planet = newShip.getPlanet();
        if (planet != null) {
            if (isStringValid(planet)) {
                oldShip.setPlanet(planet);
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (newShip.getShipType() != null) {
            oldShip.setShipType(newShip.getShipType());
        }
        Date prodDate = newShip.getProdDate();
        if (prodDate != null) {
            if (isProdDateValid(prodDate)) {
                oldShip.setProdDate(prodDate);
                changeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (newShip.getUsed() != null) {
            oldShip.setUsed(newShip.getUsed());
            changeRating = true;
        }
        final Double speed = newShip.getSpeed();
        if (speed != null) {
            if (isSpeedValid(speed)) {
                oldShip.setSpeed(speed);
                changeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }
        final Integer crewSize = newShip.getCrewSize();
        if (crewSize != null) {
            if (isCrewSizeValid(crewSize)) {
                oldShip.setCrewSize(crewSize);
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (changeRating) {
            double rating = calculateRating(oldShip.getSpeed(), oldShip.getUsed(), oldShip.getProdDate());
            oldShip.setRating(rating);
        }
        shipRepository.save(oldShip);
        return oldShip;
    }
//---------------------ПрРоверки---------------
    @Override
    public boolean isShipValid(Ship ship) {
        return ship != null && isStringValid(ship.getName()) && isStringValid(ship.getPlanet())
                && isProdDateValid(ship.getProdDate())
                && isSpeedValid(ship.getSpeed())
                && isCrewSizeValid(ship.getCrewSize());
    }

    private boolean isCrewSizeValid(Integer crewSize) {
        int minCrewSize = 1;
        int maxCrewSize = 9999;
        return crewSize != null && crewSize.compareTo(minCrewSize) >= 0 && crewSize.compareTo(maxCrewSize) <= 0;
    }

    private boolean isStringValid(String value) {
        int maxStringLength = 50;
        return value != null && !value.isEmpty() && value.length() <= maxStringLength;
    }

    private boolean isSpeedValid(Double speed) {
        double minSpeed = 0.01;
        double maxSpeed = 0.99;
        return speed != null && speed.compareTo(minSpeed) >= 0 && speed.compareTo(maxSpeed) <= 0;
    }

    private boolean isProdDateValid(Date prodDate) {
        Date startProd = getDateForYear(2800);
        Date endProd = getDateForYear(3019);
        return prodDate != null && prodDate.after(startProd) && prodDate.before(endProd);
    }

    // ------------------Вспомогательные------------------------------
    private double round(double value) {
        return Math.round(value * 100) / 100D;
    }

    public double calculateRating(double speed, boolean isUsed, Date prod) {
        final int now = 3019;
        final int prodYear = getYear(prod);
        final double k = isUsed ? 0.5 : 1;
        final double rating = 80 * speed * k / (now - prodYear + 1);
        return round(rating);
    }

    public static int getYear(Date date) {
        int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(date));
        return year;
    }

    private Date getDateForYear(int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

}
