package com.couchbase.demos.springdatademo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/airports")
public class AirportsController {

  private final AirportsRepository repository;

  @Autowired
  public AirportsController(AirportsRepository repository) {
    this.repository = repository;
  }

  @GetMapping(value = "/{id}")
  public ResponseEntity<Airport> findById(final @PathVariable("id") String id) {
    return ResponseEntity.of(repository.findById(id));
  }

}

