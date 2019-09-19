package com.couchbase.demos.springbootdemo;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/airports")
public class AirportsController {

  private final Bucket bucket;

  @Autowired
  public AirportsController(Bucket bucket) {
    this.bucket = bucket;
  }

  @GetMapping(value = "/{id}")
  public ResponseEntity<Map<String, Object>> findById(final @PathVariable("id") String id) {
    return ResponseEntity.of(
      Optional.ofNullable(bucket.get(id)).map(doc -> doc.content().toMap())
    );
  }

}

