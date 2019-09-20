package com.couchbase.demos.springdatademo;

import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.repository.CouchbaseRepository;

public interface AirportsRepository extends CouchbaseRepository<Airport, String> {

  @Query("#{#n1ql.selectEntity} where type = 'airport' limit 10")
  Iterable<Airport> findTop10();

}
