package com.space.service;


import com.space.exception.RequestException;
import com.space.exception.ShipNotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ShipService {

    @Autowired
    private ShipRepository shipRepository;

    private void paramsChecker(Ship shipRequired) {

        if (shipRequired.getName() != null && (shipRequired.getName().length() < 1 || shipRequired.getName().length() > 50)) {
            throw new RequestException("The ship name is too long or absent");
        }

        if (shipRequired.getPlanet() != null && shipRequired.getPlanet().length() > 50) {
            throw new RequestException("The planet name is too long or absent");
        }

        if (shipRequired.getSpeed() != null && (shipRequired.getSpeed() < 0.01D || shipRequired.getSpeed() > 0.99D)) {
            throw new RequestException("The ship speed is out of range");
        }

        if (shipRequired.getCrewSize() != null && (shipRequired.getCrewSize() < 1 || shipRequired.getCrewSize() > 9999)) {
            throw new RequestException("The crew size is out of range");
        }

        if (shipRequired.getProdDate() != null) {
            Calendar date = Calendar.getInstance();
            date.setTime(shipRequired.getProdDate());
            if (date.get(Calendar.YEAR) < 2800 || date.get(Calendar.YEAR) > 3019) {
                throw new RequestException("The date of ship manufacture is out of range");
            }
        }
    }

    public Long idChecker(String id) {
        if (id == null || id.equals("0") || id.equals("")) {
            throw new RequestException("ID is incorrect");
        }
        try {
            Long iD = Long.parseLong(id);
            return iD;
        } catch (NumberFormatException e) {
            throw new RequestException("ID is not a number", e);
        }
    }

    private Double calculateRating(Ship shipRequired) {
        Calendar date = Calendar.getInstance();
        date.setTime(shipRequired.getProdDate());
        int year = date.get(Calendar.YEAR);
        BigDecimal rating = new BigDecimal((80 * shipRequired.getSpeed() * (shipRequired.getUsed() ? 0.5 : 1)) / (3019 - year + 1));
        rating = rating.setScale(2, RoundingMode.HALF_EVEN);
        return rating.doubleValue();
    }

    public Ship createShip(Ship shipRequired) {
        if (shipRequired.getName() == null
                || shipRequired.getPlanet() == null
                || shipRequired.getShipType() == null
                || shipRequired.getProdDate() == null
                || shipRequired.getSpeed() == null
                || shipRequired.getCrewSize() == null) {
            throw new RequestException("Please fill in all required fields");
        }
        paramsChecker(shipRequired);
        if (shipRequired.getUsed() == null) {
            shipRequired.setUsed(false);
        }
        Double rating = calculateRating(shipRequired);
        shipRequired.setRating(rating);
        return shipRepository.saveAndFlush(shipRequired);
    }

    public Ship getShip(Long id) {
        if (!shipRepository.existsById(id)) {
            throw new ShipNotFoundException("Ship is not found");
        }
        return shipRepository.findById(id).get();
    }


    public void deleteByID(Long id) {
        if (shipRepository.existsById(id)) {
            shipRepository.deleteById(id);
        } else {
            throw new ShipNotFoundException("Ship is not found");
        }
    }

    public Ship updateShip(Long id, Ship shipRequired) {
        paramsChecker(shipRequired);

        if (!shipRepository.existsById(id))
            throw new ShipNotFoundException("Ship is not found");

        Ship changedShip = shipRepository.findById(id).get();

        if (shipRequired.getName() != null)
            changedShip.setName(shipRequired.getName());

        if (shipRequired.getPlanet() != null)
            changedShip.setPlanet(shipRequired.getPlanet());

        if (shipRequired.getShipType() != null)
            changedShip.setShipType(shipRequired.getShipType());

        if (shipRequired.getProdDate() != null)
            changedShip.setProdDate(shipRequired.getProdDate());

        if (shipRequired.getSpeed() != null)
            changedShip.setSpeed(shipRequired.getSpeed());

        if (shipRequired.getUsed() != null)
            changedShip.setUsed(shipRequired.getUsed());

        if (shipRequired.getCrewSize() != null)
            changedShip.setCrewSize(shipRequired.getCrewSize());

        Double rating = calculateRating(changedShip);
        changedShip.setRating(rating);

        return shipRepository.save(changedShip);
    }

    public List<Ship> getAllExistingShipsList(Specification<Ship> specification) {
        return shipRepository.findAll(specification);
    }

    public Page<Ship> getAllExistingShipsList(Specification<Ship> specification, Pageable sortedByName) {
        return shipRepository.findAll(specification, sortedByName);
    }

    public Specification<Ship> nameFilter(String name) {
        return (root, query, criteriaBuilder) -> name == null ? null : criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    public Specification<Ship> planetFilter(String planet) {
        return (root, query, criteriaBuilder) -> planet == null ? null : criteriaBuilder.like(root.get("planet"), "%" + planet + "%");
    }

    public Specification<Ship> shipTypeFilter(ShipType shipType) {
        return (root, query, criteriaBuilder) -> shipType == null ? null : criteriaBuilder.equal(root.get("shipType"), shipType);
    }

    public Specification<Ship> dateFilter(Long after, Long before) {
        return (root, query, criteriaBuilder) -> {
            if (after == null && before == null) {
                return null;
            }
            if (after == null) {
                Date before1 = new Date(before);
                return criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"), before1);
            }
            if (before == null) {
                Date after1 = new Date(after);
                return criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"), after1);
            }
            //time difference
            Date before1 = new Date(before - 3600001);
            Date after1 = new Date(after);
            return criteriaBuilder.between(root.get("prodDate"), after1, before1);
        };
    }

    public Specification<Ship> usageFilter(Boolean isUsed) {
        return (root, query, criteriaBuilder) -> {
            if (isUsed == null) {
                return null;
            }
            if (isUsed) {
                return criteriaBuilder.isTrue(root.get("isUsed"));
            } else {
                return criteriaBuilder.isFalse(root.get("isUsed"));
            }
        };
    }

    public Specification<Ship> speedFilter(Double min, Double max) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("speed"), max);
            }
            if (max == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("speed"), min);
            }
            return criteriaBuilder.between(root.get("speed"), min, max);
        };
    }

    public Specification<Ship> crewSizeFilter(Integer min, Integer max) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("crewSize"), max);
            }
            if (max == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("crewSize"), min);
            }
            return criteriaBuilder.between(root.get("crewSize"), min, max);
        };
    }

    public Specification<Ship> ratingFilter(Double min, Double max) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("rating"), max);
            }
            if (max == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), min);
            }
            return criteriaBuilder.between(root.get("rating"), min, max);
        };
    }
}
