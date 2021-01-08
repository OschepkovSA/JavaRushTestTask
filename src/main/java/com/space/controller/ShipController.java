package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class ShipController {

    @Autowired
    private ShipService service;

    @GetMapping("/ships")
    @ResponseStatus(HttpStatus.OK)
    public List<Ship> getAllExistingShipsList(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating,
            @RequestParam(value = "order", required = false, defaultValue = "ID") ShipOrder order,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));

        return service.getAllExistingShipsList(
                Specification.where(
                        service.nameFilter(name)
                                .and(service.planetFilter(planet)))
                        .and(service.shipTypeFilter(shipType))
                        .and(service.dateFilter(after, before))
                        .and(service.usageFilter(isUsed))
                        .and(service.speedFilter(minSpeed, maxSpeed))
                        .and(service.crewSizeFilter(minCrewSize, maxCrewSize))
                        .and(service.ratingFilter(minRating, maxRating)), pageable)
                .getContent();
    }

    @GetMapping("/ships/count")
    @ResponseStatus(HttpStatus.OK)
    public Integer getCount(@RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "planet", required = false) String planet,
                            @RequestParam(value = "shipType", required = false) ShipType shipType,
                            @RequestParam(value = "after", required = false) Long after,
                            @RequestParam(value = "before", required = false) Long before,
                            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                            @RequestParam(value = "minRating", required = false) Double minRating,
                            @RequestParam(value = "maxRating", required = false) Double maxRating) {

        return service.getAllExistingShipsList(
                Specification.where(
                        service.nameFilter(name)
                                .and(service.planetFilter(planet)))
                        .and(service.shipTypeFilter(shipType))
                        .and(service.dateFilter(after, before))
                        .and(service.usageFilter(isUsed))
                        .and(service.speedFilter(minSpeed, maxSpeed))
                        .and(service.crewSizeFilter(minCrewSize, maxCrewSize))
                        .and(service.ratingFilter(minRating, maxRating))).size();
    }

    @PostMapping("/ships")
    @ResponseStatus(HttpStatus.OK)
    public Ship addShip(@RequestBody Ship ship) {
        return service.createShip(ship);
    }

    @GetMapping("/ships/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Ship getShip(@PathVariable("id") String id) {
        Long iD = service.idChecker(id);
        return service.getShip(iD);
    }

    @PostMapping("/ships/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Ship editShip(@PathVariable("id") String id, @RequestBody Ship ship) {
        Long iD = service.idChecker(id);
        return service.updateShip(iD, ship);
    }

    @DeleteMapping("/ships/{id}")
    @ResponseStatus(HttpStatus.OK)

    public void deleteShip(@PathVariable("id") String id) {
        Long iD = service.idChecker(id);
        service.deleteByID(iD);
    }

}
