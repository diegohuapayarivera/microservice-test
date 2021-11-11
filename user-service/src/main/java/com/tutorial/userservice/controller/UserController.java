package com.tutorial.userservice.controller;

import com.tutorial.userservice.entity.User;
import com.tutorial.userservice.model.Bike;
import com.tutorial.userservice.model.Car;
import com.tutorial.userservice.service.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService service;

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        List<User> users = service.getAll();
        if (users.isEmpty())
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable("id") int id) {
        User user = service.getUserById(id);
        if (user == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<User> save(@RequestBody User user) {
        User userNew = service.save(user);
        return ResponseEntity.ok(userNew);
    }

    @CircuitBreaker(name = "carsCB", fallbackMethod = "fallBackGetCars")
    @GetMapping("/cars/{userId}")
    public ResponseEntity<List<Car>> getCars(@PathVariable("userId") int userId) {
        User user = service.getUserById(userId);
        if (user == null)
            return ResponseEntity.notFound().build();
        List<Car> cars = service.getCars(userId);
        return ResponseEntity.ok(cars);
    }

    @CircuitBreaker(name = "carsCB", fallbackMethod = "fallBackSaveCar")
    @PostMapping("/savecar/{userId}")
    public ResponseEntity<Car> saveCar(@PathVariable("userId") int userId, @RequestBody Car car) {
        if (service.getUserById(userId) == null)
            return ResponseEntity.notFound().build();
        Car carNew = service.saveCar(userId, car);
        return ResponseEntity.ok(car);
    }

    @CircuitBreaker(name = "bikesCB", fallbackMethod = "fallBackGetBikes")
    @GetMapping("/bikes/{userId}")
    public ResponseEntity<List<Bike>> getBikes(@PathVariable("userId") int userId) {
        User user = service.getUserById(userId);
        if (user == null)
            return ResponseEntity.notFound().build();
        List<Bike> bikes = service.getBikes(userId);
        return ResponseEntity.ok(bikes);
    }

    @CircuitBreaker(name = "bikesCB", fallbackMethod = "fallBackSaveBike")
    @PostMapping("/savebike/{userId}")
    public ResponseEntity<Bike> saveBike(@PathVariable("userId") int userId, @RequestBody Bike bike) {
        if (service.getUserById(userId) == null)
            return ResponseEntity.notFound().build();
        Bike bikeNew = service.saveBike(userId, bike);
        return ResponseEntity.ok(bike);
    }

    @CircuitBreaker(name = "allCB", fallbackMethod = "fallBackGetAll")
    @GetMapping("/getAll/{userId}")
    public ResponseEntity<Map<String, Object>> getAllVehicles(@PathVariable("userId") int userId) {
        Map<String, Object> result = service.getUserAndVehicles(userId);
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<List<Car>> fallBackGetCars(@PathVariable("userId") int userId, RuntimeException e) {
        return new ResponseEntity("El usuario " + userId + " tiene los coches en el taller", HttpStatus.OK);
    }

    public ResponseEntity<Car> fallBackSaveCar(@PathVariable("userId") int userId, @RequestBody Car car, RuntimeException e) {
        return new ResponseEntity("El usuario " + userId + " no tiene dinero para coches", HttpStatus.OK);
    }

    public ResponseEntity<List<Bike>> fallBackGetBikes(@PathVariable("userId") int userId, RuntimeException e) {
        return new ResponseEntity("El usuario " + userId + " tiene las motos en el taller", HttpStatus.OK);
    }

    public ResponseEntity<Bike> fallBackSaveBike(@PathVariable("userId") int userId, @RequestBody Bike bike, RuntimeException e) {
        return new ResponseEntity("El usuario " + userId + " no tiene dinero para motos", HttpStatus.OK);
    }

    public ResponseEntity<Map<String, Object>> fallBackGetAll(@PathVariable("userId") int userId, RuntimeException e) {
        return new ResponseEntity("El usuario " + userId + " tiene los vehiculos en el taller", HttpStatus.OK);
    }
}
