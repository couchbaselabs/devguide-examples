package com.couchbase.demos.springdatademo;

import org.springframework.data.couchbase.repository.CouchbaseRepository;

public interface AirportsRepository extends CouchbaseRepository<Airport, String> {

}
