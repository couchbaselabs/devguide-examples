package com.couchbase.demos.springbootdemo;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

  @GetMapping(value = "/top/{limit}")
  public ResponseEntity<List<Map<String, Object>>> findTop(final @PathVariable("limit") int limit) {
    String query = "select `" + bucket.name() + "`.* from `" + bucket.name() +"` where type = \"airport\" limit " + limit;
    N1qlQueryResult result = bucket.query(N1qlQuery.simple(query));
    List<Map<String, Object>> data = result.allRows().stream().map(r -> r.value().toMap()).collect(Collectors.toList());
    return ResponseEntity.ok(data);
  }

}

